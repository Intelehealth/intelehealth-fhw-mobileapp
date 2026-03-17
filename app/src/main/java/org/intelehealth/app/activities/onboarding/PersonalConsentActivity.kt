package org.intelehealth.app.activities.onboarding

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import org.intelehealth.abdm.abha_create.CreateAbhaAccountActivity
import org.intelehealth.abdm.abha_verify.AbhaCardVerificationActivity
import org.intelehealth.abdm.abha_verify.AbhaCardVerificationActivity.intentPatientNameTag
import org.intelehealth.abdm.constants.AbdmConstant
import org.intelehealth.abdm.dialog.AbhaChoiceDialogFragment
import org.intelehealth.abdm.enums.AbdmOutcomes
import org.intelehealth.abdm.listener.AbhaChoiceListener
import org.intelehealth.abdm.model.AbdmResult
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.R
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.ui.patient.activity.PatientRegistrationActivity
import org.intelehealth.app.utilities.BundleKeys
import org.intelehealth.app.utilities.ConfigUtils
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.PatientRegStage
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.WebViewStatus
import java.util.Locale


class PersonalConsentActivity : AppCompatActivity(), WebViewStatus, AbhaChoiceListener {
    private var personal_consent_string = ""
    private var webView: WebView? = null
    var ivBack: ImageView? = null
    private val context: Context = this
    private var sessionManager: SessionManager? = null
    private var loadingDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_personal_consent)
        //  mIntentFrom = getIntent().getIntExtra("IntentFrom", 0);
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            true

        window.statusBarColor = Color.WHITE

        ivBack = findViewById(R.id.iv_back_arrow_terms)
        webView = findViewById(R.id.consent_webview)
        sessionManager = SessionManager(context)

        webView?.webViewClient = GenericWebViewClient(this)

        loadingDialog = DialogUtils().showCommonLoadingDialog(
            this,
            getString(R.string.loading),
            getString(R.string.please_wait)
        )

        ivBack?.setOnClickListener { v: View? -> finish() }

        Thread {
            var text: String?
            text =
                "<html><body style='color:black;font-size: 0.8em;' >" //style='text-align:justify;text-justify: inter-word;'

            text += ConfigUtils(this).getPersonalDataConsentText(sessionManager?.appLanguage)
            text += "</body></html>"

            runOnUiThread {
                webView?.loadDataWithBaseURL(
                    null,
                    text,
                    "text/html",
                    "utf-8",
                    null
                )
            }
        }.start()

    }

    private val abdmLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { contractResult ->
            if (contractResult.resultCode != RESULT_OK) return@registerForActivityResult

            val data = contractResult.data ?: return@registerForActivityResult

            val result: AbdmResult? = data.getParcelableExtra<AbdmResult>(
                AbdmConstant.INTENT_ABDM_RESULT
            ) ?: return@registerForActivityResult


            val newIntent = Intent(context, PatientRegistrationActivity::class.java)
            newIntent.putExtra(AbdmConstant.ACCESS_TOKEN, result?.accessToken ?: "")

            when (result?.outcome) {
                AbdmOutcomes.NAVIGATE_TO_IDENTIFICATION_SCREEN_WITH_EXISTING_DETAILS_FOR_CREATION -> {
                    newIntent.putExtra(AbdmConstant.PAYLOAD, result.otpResponse)
                    newIntent.putExtra(BundleKeys.PATIENT_UUID, result.otpResponse?.uuID)
                    newIntent.putExtra(BundleKeys.PATIENT_CURRENT_STAGE, PatientRegStage.PERSONAL)
                    startActivity(newIntent)
                }

                AbdmOutcomes.NAVIGATE_TO_IDENTIFICATION_SCREEN_FOR_NEW_PATIENT_FOR_CREATION -> {
                    newIntent.putExtra(AbdmConstant.PAYLOAD, result.otpResponse)
                    startActivity(newIntent)
                }

                AbdmOutcomes.NAVIGATE_TO_IDENTIFICATION_SCREEN_AFTER_ABHA_SUGGESTIONS_FOR_CREATION -> {
                    newIntent.putExtra(AbdmConstant.PAYLOAD, result.otpResponse)
                    startActivity(newIntent)
                }

                AbdmOutcomes.NAVIGATE_TO_IDENTIFICATION_SCREEN_WITH_NEW_PATIENT_FOR_VERIFICATION -> {
                    newIntent.putExtra(AbdmConstant.MOBILE_PAYLOAD, result.abhaResponse)
                    startActivity(newIntent)
                }

                AbdmOutcomes.NAVIGATE_TO_PATIENT_DETAILS_SCREEN_WITH_EXISTING_PATIENT_AFTER_COMPARISON -> {
                    val detailIntent = Intent(context, PatientDetailActivity2::class.java)
                    detailIntent.putExtra("patientUuid", result.abhaResponse?.uuiD)
                    startActivity(detailIntent)
                }

                else -> {

                }
            }

            finish()
        }


    fun declineCon(view: View?) {
        setResult(AppConstants.PERSONAL_CONSENT_DECLINE)
        finish()
    }

    @Suppress("KotlinConstantConditions")
    fun acceptCon(view: View?) {
        if (BuildConfig.FLAVOR == "idaProduction") {
            AbhaChoiceDialogFragment()
                .apply {
                    listener = this@PersonalConsentActivity
                }
                .show(supportFragmentManager, AbhaChoiceDialogFragment.TAG)
        } else {
            PatientRegistrationActivity.startPatientRegistration(this@PersonalConsentActivity)
            setResult(AppConstants.PERSONAL_CONSENT_ACCEPT)
            this@PersonalConsentActivity.finish()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(setLocale(newBase))
    }

    fun setLocale(context: Context): Context? {
        val sessionManager1 = SessionManager(context)
        val appLanguage = sessionManager1.appLanguage
        val res = context.resources
        val conf = res.configuration
        val locale = Locale(appLanguage)
        Locale.setDefault(locale)
        conf.setLocale(locale)
        context.createConfigurationContext(conf)
        val dm = res.displayMetrics
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(LocaleList(locale))
        } else {
            conf.setLocale(locale)
        }
        res.displayMetrics.setTo(dm)
        res.configuration.setTo(conf)
        return context
    }

    override fun onPageStarted() {
        loadingDialog?.show()
    }

    override fun onPageFinish() {
        loadingDialog?.dismiss()
    }

    override fun onPageError(error: String) {
        loadingDialog?.dismiss()
    }

    override fun onHasAbha() {
        org.intelehealth.abdm.utils.DialogUtils.triggerTextViewDialogFragment(
            this@PersonalConsentActivity,
            AbhaCardVerificationActivity::class.java,
            intentPatientNameTag,
            abdmLauncher
        )
    }

    override fun onCreateAbha() {
        org.intelehealth.abdm.utils.DialogUtils.triggerTextViewDialogFragment(
            this@PersonalConsentActivity,
            CreateAbhaAccountActivity::class.java,
            intentPatientNameTag,
            abdmLauncher
        )
    }

    override fun onContinueWithoutAbha() {
        PatientRegistrationActivity.startPatientRegistration(this@PersonalConsentActivity)
        setResult(AppConstants.PERSONAL_CONSENT_ACCEPT)
        this@PersonalConsentActivity.finish()
    }
}
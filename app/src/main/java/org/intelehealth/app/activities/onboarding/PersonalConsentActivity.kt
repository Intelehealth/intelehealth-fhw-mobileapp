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
import org.intelehealth.abdm.api.AbdmFlow
import org.intelehealth.abdm.api.AbdmFlowResult
import org.intelehealth.abdm.api.AbdmNavigation
import org.intelehealth.abdm.api.AbdmPlugins
import org.intelehealth.abdm.api.AbhaChoiceCallbacks
import org.intelehealth.app.R
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.ImagesPushDAO
import org.intelehealth.app.database.dao.SyncDAO
import org.intelehealth.app.syncModule.SyncUtils
import org.intelehealth.app.ui.patient.activity.PatientRegistrationActivity
import org.intelehealth.app.ui.patient.data.PatientRepository
import org.intelehealth.app.utilities.BundleKeys
import org.intelehealth.app.utilities.ConfigUtils
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.NetworkConnection
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.WebViewStatus
import org.intelehealth.config.room.ConfigDatabase
import java.util.Locale


class PersonalConsentActivity : AppCompatActivity(), WebViewStatus, AbhaChoiceCallbacks {
    private var personal_consent_string = ""
    private var webView: WebView? = null
    var ivBack: ImageView? = null
    private val context: Context = this
    private var sessionManager: SessionManager? = null
    private var loadingDialog: AlertDialog? = null

    /** Server config flag `abha_section`; ABHA is offered only when this is on AND the module is bundled. */
    private var abhaSectionActive = false

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

        ConfigDatabase.getInstance(this).featureActiveStatusDao()
            .getFeatureActiveStatusLiveRecord()
            .observe(this) { abhaSectionActive = it?.activeStatusAbha == true }

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
            val result = AbdmPlugins.plugin?.parseResult(contractResult.data)
                ?: return@registerForActivityResult

            if (result.syncRequired) SyncUtils.syncOnServer()

            when (result.navigation) {
                AbdmNavigation.PATIENT_REGISTRATION -> {
                    val intent = Intent(context, PatientRegistrationActivity::class.java)
                    intent.putExtra(AbdmFlowResult.EXTRA, result)
                    result.patientUuid?.let { intent.putExtra(BundleKeys.PATIENT_UUID, it) }
                    startActivity(intent)
                }

                AbdmNavigation.PATIENT_DETAILS -> {
                    val intent = Intent(context, PatientDetailActivity2::class.java)
                    intent.putExtra("patientUuid", result.patientUuid)
                    startActivity(intent)
                }
            }

            finish()
        }

    fun declineCon(view: View?) {
        setResult(AppConstants.PERSONAL_CONSENT_DECLINE)
        finish()
    }

    fun acceptCon(view: View?) {
        val plugin = AbdmPlugins.plugin
        if (plugin != null && AbdmPlugins.isEnabled(abhaSectionActive)) {
            plugin.showAbhaChoice(this, this)
        } else {
            onContinueWithoutAbha()
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

    override fun onVerifyAbha() {
        AbdmPlugins.plugin?.startFlow(this, AbdmFlow.VERIFY_ABHA, abdmLauncher)
    }

    override fun onCreateAbha() {
        AbdmPlugins.plugin?.startFlow(this, AbdmFlow.CREATE_ABHA, abdmLauncher)
    }

    override fun onContinueWithoutAbha() {
        PatientRegistrationActivity.startPatientRegistration(this@PersonalConsentActivity)
        setResult(AppConstants.PERSONAL_CONSENT_ACCEPT)
        this@PersonalConsentActivity.finish()
    }
}
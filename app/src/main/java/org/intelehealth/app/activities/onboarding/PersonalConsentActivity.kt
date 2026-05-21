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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import org.intelehealth.app.R
import org.intelehealth.app.activities.filterPatientActivity.FilterPatientActivity
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.ui.patient.activity.PatientRegistrationActivity
import org.intelehealth.app.ui.rosterquestionnaire.ui.RosterQuestionnaireMainActivity.Companion.startRosterQuestionnaire
import org.intelehealth.app.ui.rosterquestionnaire.utilities.RosterQuestionnaireStage
import org.intelehealth.app.utilities.BundleKeys
import org.intelehealth.app.utilities.ConfigUtils
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.WebViewStatus
import java.util.Locale
import kotlin.jvm.java


class PersonalConsentActivity : AppCompatActivity(), WebViewStatus {
    private var personal_consent_string = ""
    private var webView: WebView? = null
    var ivBack: ImageView? = null
    private val context: Context = this
    private var sessionManager: SessionManager? = null
    private var loadingDialog: AlertDialog? = null
    var firstName: String? = null;
    var lastName: String? = null;
    var phone: String? = null;
    var gender: Int? = null;
    var dob: String? = null;

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
        firstName = intent.getStringExtra(BundleKeys.FIRST_NAME)
        lastName = intent.getStringExtra(BundleKeys.LAST_NAME)
        gender = intent.getIntExtra(BundleKeys.GENDER,0)
        phone = intent.getStringExtra(BundleKeys.PHONE)
        dob = intent.getStringExtra(BundleKeys.DOB)

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

    fun declineCon(view: View?) {
        setResult(AppConstants.PERSONAL_CONSENT_DECLINE)
        finish()
    }

    fun acceptCon(view: View?) {
       /* PatientRegistrationActivity.startPatientRegistration(
            this,
            firstName = firstName,
            lastName = lastName,
            gender = gender,
            phone = phone,
            dob = dob
        )
        //val intent = Intent(this, FilterPatientActivity::class.java)

        intent.putExtra("intentType", "navigateFurther")
        intent.putExtra("add_patient", "add_patient")

        startActivity(intent)
        setResult(AppConstants.PERSONAL_CONSENT_ACCEPT)
        finish()*/
        PatientRegistrationActivity.startPatientRegistration(this)
        setResult(AppConstants.PERSONAL_CONSENT_ACCEPT)
        finish()

//        startRosterQuestionnaire(
//            this,
//           " patient.uuid",
//            RosterQuestionnaireStage.GENERAL_ROSTER,
//            isPregnancyVisible = true,
//            isEditMode = false
//        )

        /*  startRosterQuestionnaire(
              this,
              "hgfdhbgdshj",
              RosterQuestionnaireStage.GENERAL_ROSTER
          )*/

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
}
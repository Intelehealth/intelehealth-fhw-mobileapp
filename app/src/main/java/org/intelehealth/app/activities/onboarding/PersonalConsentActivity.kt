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
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.view.WindowCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.intelehealth.abdm.presentation.AbdmCardDownloader
import org.intelehealth.abdm.presentation.AbdmLauncher
import org.intelehealth.abdm.presentation.abha_choice.AbhaChoiceDialogFragment
import org.intelehealth.abdm.result.AbdmOutcomes
import org.intelehealth.abdm.result.AbdmResult
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.R
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.models.dto.PatientAttributesDTO
import org.intelehealth.app.ui.patient.activity.PatientRegistrationActivity
import org.intelehealth.app.utilities.ConfigUtils
import org.intelehealth.app.utilities.ConsentUtils
import org.intelehealth.app.utilities.CustomLog
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.FlavorKeys
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.UuidDictionary
import org.intelehealth.app.utilities.WebViewStatus
import org.intelehealth.app.utilities.exception.DAOException
import java.util.Locale
import java.util.UUID


class PersonalConsentActivity : AppCompatActivity(), WebViewStatus {
    private var personal_consent_string = ""
    private var webView: WebView? = null
    var ivBack: ImageView? = null
    private val context: Context = this
    private var sessionManager: SessionManager? = null
    private var loadingDialog: AlertDialog? = null

    /**
     * NAS-1752 - the Patient_Consent value, built the moment Accept is tapped. No patient row
     * exists yet at that point (registration runs afterwards, possibly via the ABHA flow), so
     * this is carried forward to PatientRegistrationActivity instead of being written here.
     */
    private var patientConsentValue: String? = null

    private val abhaResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleAbhaResult(result)
        }

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

        registerAbhaChoiceListener()

        Thread {
            var text: String?
            text =
                "<html><body style='color:black;font-size: 0.8em;' >" //style='text-align:justify;text-justify: inter-word;'
            if (BuildConfig.FLAVOR_client === FlavorKeys.NAS){
                text += ConfigUtils(this).getPersonalDataConsentTextForWebrtcRecording(sessionManager?.appLanguage)
            }else{
                text += ConfigUtils(this).getPersonalDataConsentText(sessionManager?.appLanguage)
            }
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
//        startActivity(
//            Intent(
//                this,
//                IdentificationActivity_New::class.java
//            )
//        )
        // NAS-1752: this tap IS the "Patient_Consent given" moment, recorded now even though it
        // can only be written to the DB later, once a patient uuid exists.
        patientConsentValue = ConsentUtils.buildConsentValue(
            sessionManager?.providerID, sessionManager?.appLanguage
        )
        offerAbhaThenRegister()

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

    /**
     * Registered in onCreate rather than at click time so the choice survives a configuration
     * change while the dialog is open.
     */
    private fun registerAbhaChoiceListener() {
        AbhaChoiceDialogFragment.setResultListener(supportFragmentManager, this) { choice ->
            when (choice) {
                AbhaChoiceDialogFragment.Choice.VERIFY_ABHA ->
                    AbdmLauncher.startVerifyAbha(this, abhaResultLauncher)

                AbhaChoiceDialogFragment.Choice.CREATE_ABHA ->
                    AbdmLauncher.startCreateAbha(this, abhaResultLauncher)

                AbhaChoiceDialogFragment.Choice.CONTINUE_WITHOUT_ABHA -> continueWithoutAbha()
            }
        }
    }

    private fun offerAbhaThenRegister() {
        AbhaChoiceDialogFragment.show(supportFragmentManager)
    }

    private fun continueWithoutAbha() {
        PatientRegistrationActivity.startPatientRegistration(
            this, patientConsentValue = patientConsentValue
        )
        setResult(AppConstants.PERSONAL_CONSENT_ACCEPT)
        finish()
    }

    /**
     * A cancelled ABHA flow deliberately leaves this screen up so the user can pick again, rather
     * than dropping them into registration they did not ask for.
     *
     * The ABHA card is fetched here rather than at the end of registration. It needs only the xToken,
     * scope and ABHA number from this result — never the saved patient — and the cache file is keyed on
     * the ABHA number alone. Doing it here spends the one-shot token while it is freshest and keeps the
     * download independent of how far the user gets through registration, or whether they abandon it.
     */
    private fun handleAbhaResult(result: ActivityResult) {
        val data = result.data
        if (result.resultCode != RESULT_OK || data == null) return
        data.setExtrasClassLoader(AbdmResult::class.java.classLoader)
        val abdmResult = IntentCompat.getParcelableExtra(
            data, AbdmResult.EXTRA_ABDM_RESULT, AbdmResult::class.java
        ) ?: return

        AbdmCardDownloader.downloadInBackground(
            this, abdmResult.xToken, abdmResult.cardScope, abdmResult.profile?.abhaNumber
        )

        // NAS-1752: recorded now, at the moment the ABHA create/verify flow (which required its
        // own consent checkbox before it would send an OTP) actually completed.
        val abdmConsentValue = ConsentUtils.buildConsentValue(
            sessionManager?.providerID, sessionManager?.appLanguage
        )

        if (abdmResult.outcome ==
            AbdmOutcomes.NAVIGATE_TO_PATIENT_DETAILS_SCREEN_WITH_EXISTING_PATIENT_AFTER_COMPARISON
        ) {
            // The ABHA matched a patient this device already has locally - write both consent
            // attributes directly against that existing row rather than threading them through
            // registration (this HW did just accept personal-data processing for this patient
            // in this same interaction, so Patient_Consent is written here too, not skipped).
            abdmResult.uuid?.takeIf { it.isNotBlank() }
                ?.let { recordConsentForExistingPatient(it, patientConsentValue, abdmConsentValue) }
            Intent(this, PatientDetailActivity2::class.java).apply {
                putExtra("patientUuid", abdmResult.uuid)
                putExtra("tag", "newPatient")
            }.also { startActivity(it) }
        } else if (!abdmResult.uuid.isNullOrBlank()) {
            // Registration below opens in edit mode against this existing uuid (see
            // startPatientRegistrationFromAbha's doc comment) and never runs generatePatientId,
            // so the same direct write used above is needed here too.
            recordConsentForExistingPatient(abdmResult.uuid!!, patientConsentValue, abdmConsentValue)
            PatientRegistrationActivity.startPatientRegistrationFromAbha(this, abdmResult)
        } else {
            // Genuinely new patient - no row exists yet anywhere. Defer both values to
            // patient-creation time, same as the no-ABHA path.
            PatientRegistrationActivity.startPatientRegistrationFromAbha(
                this, abdmResult,
                patientConsentValue = patientConsentValue,
                abdmConsentValue = abdmConsentValue
            )
        }

        setResult(AppConstants.PERSONAL_CONSENT_ACCEPT)
        finish()
    }

    /**
     * NAS-1752 - writes Patient_Consent / ABDM_Consent directly for a patient that already has a
     * local row, since PatientRepository#createPatientAttributes (the deferred path used for a
     * brand-new patient) never runs for these two cases. tbl_patient_attribute has a
     * UNIQUE(patientuuid, person_attribute_type_uuid) constraint, so re-recording consent for the
     * same patient replaces the previous row rather than duplicating it.
     */
    private fun recordConsentForExistingPatient(
        patientUuid: String, patientConsentValue: String?, abdmConsentValue: String
    ) {
        try {
            val attributes = arrayListOf<PatientAttributesDTO>().apply {
                patientConsentValue?.let {
                    add(PatientAttributesDTO().apply {
                        uuid = UUID.randomUUID().toString()
                        this.patientuuid = patientUuid
                        personAttributeTypeUuid = UuidDictionary.PATIENT_CONSENT
                        value = it
                    })
                }
                add(PatientAttributesDTO().apply {
                    uuid = UUID.randomUUID().toString()
                    this.patientuuid = patientUuid
                    personAttributeTypeUuid = UuidDictionary.ABDM_CONSENT
                    value = abdmConsentValue
                })
            }
            PatientsDAO().insertPatientAttributes(attributes)
            // TODO(NAS-1752): temporary QA logging, remove once consent testing is done.
            CustomLog.d(
                "NAS1752", "consent stored for existing patient - patientUuid=$patientUuid " +
                        "patientConsent=$patientConsentValue abdmConsent=$abdmConsentValue"
            )
        } catch (e: DAOException) {
            FirebaseCrashlytics.getInstance().recordException(e)
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
}
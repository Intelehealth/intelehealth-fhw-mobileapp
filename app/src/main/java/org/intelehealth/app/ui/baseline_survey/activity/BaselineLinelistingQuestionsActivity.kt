package org.intelehealth.app.ui.baseline_survey.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.intelehealth.app.R
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2
import org.intelehealth.app.ui.baseline_survey.factory.BaselineSurveyViewModelFactory
import org.intelehealth.app.ui.baseline_survey.fragments.BaselineMedicalFragmentNEW
import org.intelehealth.app.utilities.DialogUtils

class BaselineLinelistingQuestionsActivity : AppCompatActivity() {
    private lateinit var patientUUID: String

    private val baselineSurveyViewModel by lazy {
        return@lazy BaselineSurveyViewModelFactory.create(this, this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_medical_baseline_required_for_linelisting)

        patientUUID = intent.getStringExtra("patientUuid") ?: return
        patientUUID.let { id ->
            baselineSurveyViewModel.apply {
                baselineEditMode = true
                this.patientId = id
            }
            fetchPatientDetails(id)
        }
        setupToolbar(patientUUID)

        if (savedInstanceState == null) {
            val fragment = BaselineMedicalFragmentNEW().apply {
                arguments = Bundle().apply {
                    putString("patientUuid", patientUUID)
                }
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }

    private fun setupToolbar(patientUuid:String) {
        val toolbar: View = findViewById(R.id.toolbar_common_line_list)
        val tvTitle: TextView = toolbar.findViewById(R.id.tv_screen_title_common)
        val tvBack: ImageView = toolbar.findViewById(R.id.iv_back_arrow_common)
        tvTitle.text = getString(R.string.ncd_baseline)
        tvBack.setOnClickListener {
         /*   val intent = Intent(this@BaselineLinelistingQuestionsActivity, PatientDetailActivity2::class.java)
            intent.putExtra("SKIP_DIALOG", true);
            intent.putExtra("patientUuid", patientUuid);
            startActivity(intent)
            finish()*/
            showBaselineMissingQuestionsDialog()
        }
    }

    private fun fetchPatientDetails(id: String) {
        baselineSurveyViewModel.loadBaselineData(id).observe(this) {
            it ?: return@observe
            baselineSurveyViewModel.handleResponse(it) { data ->
                baselineSurveyViewModel.updateBaselineData(data)
            }
        }
    }
    private fun navigateToPatientDetail(patientUuid: String) {
        val intent = Intent(
            this@BaselineLinelistingQuestionsActivity,
            PatientDetailActivity2::class.java
        )
        intent.putExtra("SKIP_DIALOG", true)
        intent.putExtra("patientUuid", patientUuid)
        startActivity(intent)
        finish()
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        showBaselineMissingQuestionsDialog()
    }
    private fun showBaselineMissingQuestionsDialog() {
        val dialogUtils = DialogUtils()
        dialogUtils.showCommonDialog(
            this,
            R.drawable.baseline_do_not_disturb_alt_24,
            getString(R.string.ncd_questions_are_incomplete_title),
            getString(R.string.ncd_questions_are_incomplete_body),
            false,
            getString(R.string.confirm_continue_changes_button_dialog),
            getString(R.string.confirm_discard_changes_button_dialog)
        ) { action ->
            if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
                navigateToPatientDetail(patientUUID)
            }
        }
    }

}

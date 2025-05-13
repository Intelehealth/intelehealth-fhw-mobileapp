package org.intelehealth.app.ui.prescriptionwithotp

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intelehealth.app.R
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.ObsDAO
import org.intelehealth.app.database.dao.VisitsDAO
import org.intelehealth.app.databinding.DialogShareprescBinding
import org.intelehealth.app.models.ClsDoctorDetails
import org.intelehealth.app.models.Patient
import org.intelehealth.app.models.hwprofile.Profile
import org.intelehealth.app.utilities.CustomLog
import org.intelehealth.app.utilities.DateAndTimeUtils
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.DialogUtils.CustomDialogListener
import org.intelehealth.app.utilities.Logger
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.UrlModifiers
import timber.log.Timber
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ShowPrescriptionDataPdfShareDialog(
    private val activity: Activity,
    private val prescriptionData: PrescriptionData,
    private val openMrsId: String,
    private val patientUuid: String,
    private val visitUuid: String,
    private val hasPrescription: Boolean
) {
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private var visitStartDate = ""
    private var hwMobileNumber = ""
     fun sharePrescriptionInPdf() {
         val sessionManager = SessionManager(activity)
         hwMobileNumber= sessionManager.healthWorkerNumber
         val binding = DialogShareprescBinding.inflate(LayoutInflater.from(activity))
         val dialogView = binding.root
         if (hasPrescription) {
             var isHWNumberAvailable =false
             var title =""
             var body =""
             if (!hwMobileNumber.isNullOrEmpty() && !hwMobileNumber.equals("NA", ignoreCase = true)) {
                 isHWNumberAvailable = true
                 title = activity.resources.getString(R.string.pdf_share_flow_title)
                 body = activity.resources.getString(R.string.pdf_share_flow_msg, hwMobileNumber)
             }else{
                 title = activity.resources.getString(R.string.pdf_share_flow_title)
                 body = activity.resources.getString(R.string.enter_mobile_number_in_profile)
             }
             DialogUtils.patientRegistrationDialog(activity, ContextCompat.getDrawable(activity, R.drawable.close_patient_svg), title, body,
                 activity.resources.getString(R.string.yes),
                 activity.resources.getString(R.string.no)
             ) { action -> if (action == CustomDialogListener.POSITIVE_CLICK)
                 if (isHWNumberAvailable) {
                     createAndSaveFile()
                 }
             }
         }
            /* val editText = binding.editTextMobileno
             val shareBtn = binding.sharebtn
             val message = binding.message
             val errorTextView = binding.errorTextView

             editText.setText(hwMobileNumber)
             editText.isEnabled = false
             message.text = activity.getString(R.string.hw_mobile_number)
             editText.hint = ""
             if (!hwMobileNumber.isNullOrEmpty() && !hwMobileNumber.equals("NA", ignoreCase = true)) {
                 errorTextView.visibility = View.GONE
                 shareBtn.isEnabled = true
             } else {
                 errorTextView.visibility = View.VISIBLE
                 shareBtn.isEnabled = false
             }

             val alertDialog = MaterialAlertDialogBuilder(activity)
                 .setView(dialogView)
                 .create().apply {
                     window?.apply {
                         setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg)
                         addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                         setLayout(
                             activity.resources.getDimensionPixelSize(R.dimen.internet_dialog_width),
                             WindowManager.LayoutParams.WRAP_CONTENT
                         )
                     }
                 }

             shareBtn.setOnClickListener {
                 if (!hwMobileNumber.isNullOrEmpty() && !hwMobileNumber.equals("NA", ignoreCase = true)) {
                     alertDialog.dismiss()
                     createAndSaveFile()
                 } else errorTextView.visibility = View.VISIBLE
             }
             alertDialog.show()

         }*/
     }
    private fun createAndSaveFile() {
        scope.launch {
            val visitStartDateDbValue = VisitsDAO().getVisitStartDate(visitUuid)
            visitStartDate = DateAndTimeUtils.date_formatter(visitStartDateDbValue, "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "dd-MM-yyyy")
            val patient = getPatientDetails(patientUuid)
            val fileName = String.format("%s_%s_%s_%s", patient.first_name, patient.last_name, openMrsId, visitStartDate).trim() + ".pdf"

            val outputDir = activity.getExternalFilesDir(null) // or use context.getCacheDir()
            val outputFile = File(outputDir, fileName)

            // 🔥 Delete existing file if present
            if (outputFile.exists()) {
                outputFile.delete()
            }

            try {
                // Pass the file to the builder
                val builder = buildAndSavePrescription(outputFile)
                val simpleFile = builder.simpleGeneratedFile
                if (simpleFile != null && simpleFile.exists()) {
                    val uri = FileProvider.getUriForFile(activity, "${activity.packageName}.fileprovider", simpleFile)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        setPackage("com.whatsapp")
                    }
                    activity.startActivity(intent)
                } else {
                    Toast.makeText(activity, "Simple PDF not generated yet.", Toast.LENGTH_SHORT).show()
                }
            } catch (exception: ActivityNotFoundException) {
                Toast.makeText(activity, activity.getString(R.string.please_install_whatsapp), Toast.LENGTH_LONG).show()
            }
        }
    }


    /*
        private fun createAndSaveFile() {
            scope.launch {
                val visitStartDateDbValue = VisitsDAO().getVisitStartDate(visitUuid)
                 visitStartDate = DateAndTimeUtils.date_formatter(visitStartDateDbValue, "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "dd-MM-yyyy")
                val patient = getPatientDetails(patientUuid)
                val fileName = String.format("%s_%s_%s_%s.pdf", patient.first_name, patient.last_name, openMrsId, visitStartDate).trim() + ".pdf"
                try {
                    val builder = buildAndSavePrescription(fileName)
                    // Check if the file was generated successfully
                    val simpleFile = builder.simpleGeneratedFile
                    if (simpleFile != null && simpleFile.exists()) {
                        val uri = FileProvider.getUriForFile(activity, "${activity.packageName}.fileprovider", simpleFile)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            setPackage("com.whatsapp")
                        }
                        activity.startActivity(intent)
                    } else {
                        Toast.makeText(activity, "Simple PDF not generated yet.", Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (exception: ActivityNotFoundException) {
                    Toast.makeText(activity, activity.getString(R.string.please_install_whatsapp), Toast.LENGTH_LONG).show()
                }
            }
        }
    */

    fun clear() {
        job.cancel()
    }

    private fun buildAndSavePrescription(
        fileName: File,
    ): PrescriptionWithPDFBuilder {
        val drDetails= getDrDetails()
        val complaint = formatComplaintsWithBullets(prescriptionData.adultInitials?.get("Presenting Complaint(s)").orEmpty())
        val diagnosis = prescriptionData.visitCompleteEncData?.get("Primary Diagnosis").orEmpty()
        val vital= formatVitalsAndDiagnostics(prescriptionData.vitals)
        val diagnostic= formatVitalsAndDiagnostics(prescriptionData.diagnostics)
        val formatedAdvice = formatGeneralAdvice(prescriptionData.visitCompleteEncData?.get("Advice").toString())

        val patientDataSections: Map<String, Map<String, String?>> = mapOf(
            "Vitals" to mapOf(PrescriptionDetailsDataKeys.Vitals.toString() to vital),
            "Diagnostics" to mapOf(PrescriptionDetailsDataKeys.Diagnostics.toString() to diagnostic),
            "Presenting Complaint(s)" to mapOf(PrescriptionDetailsDataKeys.Complaints.toString() to complaint),
            "Diagnosis" to mapOf(PrescriptionDetailsDataKeys.Diagnosis.toString() to diagnosis),
            "Medication Plan" to mapOf(PrescriptionDetailsDataKeys.MedicationPlan.toString() to prescriptionData.visitCompleteEncData?.get("Medicine")),
            "General Advice" to mapOf(PrescriptionDetailsDataKeys.GeneralAdvice.toString() to formatedAdvice),
            "Tests" to mapOf(PrescriptionDetailsDataKeys.Tests.toString() to prescriptionData.visitCompleteEncData?.get("Tests")),
            "Referral" to mapOf(PrescriptionDetailsDataKeys.Referral.toString() to prescriptionData.visitCompleteEncData?.get("Referral")),
            "Follow Up Date" to mapOf(PrescriptionDetailsDataKeys.FollowUp.toString() to prescriptionData.visitCompleteEncData?.get("Follow-up Date")))

        val patientData = createPatientData(prescriptionData.patient)

        val builder = PrescriptionWithPDFBuilder(activity, patientDataSections)
        builder.setPatientData(patientData)
        builder.setPatientDataSections(patientDataSections)
        builder.buildDynamicUI()
        drDetails?.let { builder.createSignatureBitmap(it.fontOfSign, activity, it.textOfSign, it) }
        builder.build(fileName.absolutePath)
        return builder
    }

   private fun formatComplaintsWithBullets(complaintHtml: String): String {
        val regex = Regex("<b>(.*?)</b>")
        val ignoreList = listOf("Associated symptoms")

        val matches = regex.findAll(complaintHtml)
        val titles = matches.mapNotNull {
            val title = it.groupValues[1].trim()
            if (!ignoreList.any { ignore -> title.equals(ignore, ignoreCase = true) }) {
                "• $title"
            } else {
                null
            }
        }.toList()

        return if (titles.isEmpty()) "NA" else titles.joinToString("\n")
    }

    private fun createPatientData(patient: Patient): String {
        val fullName = listOfNotNull(patient.first_name, patient.middle_name.takeIf { !it.isNullOrBlank() }, patient.last_name).joinToString(" ")
        val ageGender = "${activity.getString(R.string.label_age)} ${getPatientAge(patient.date_of_birth)} | ${activity.getString(R.string.label_gender)} ${patient.gender}"

        val patientIdLine = "${activity.getString(R.string.label_patient_id)} ${patient.openmrs_id}"
        val visitDateLine = "${activity.getString(R.string.label_visit_date)} $visitStartDate"

        // Combine all data into one string
        return listOf(fullName, ageGender, patientIdLine,visitDateLine)
            .filter { it.isNotBlank() }
            .joinToString("\n")
    }

    private fun getPatientAge(mPatientDob: String): Int {
        val today = Calendar.getInstance()
        val dob = Calendar.getInstance()

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val date = try {
            sdf.parse(mPatientDob)
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
        date?.let { dob.time = it }
        return today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
    }

    private suspend fun getPatientDetails(patientUuid: String): Patient {
        return withContext(Dispatchers.IO) {
            val db = IntelehealthApplication.inteleHealthDatabaseHelper.readableDatabase
            val cursor = db.query("tbl_patient", arrayOf("openmrs_id", "first_name", "middle_name", "last_name", "date_of_birth", "address1", "address2", "phone_number", "gender"), "uuid = ?", arrayOf(patientUuid), null, null, null
            )
            cursor.use {
                if (it.moveToFirst()) {
                    val patient = Patient().apply {
                        openmrs_id = it.getString(it.getColumnIndexOrThrow("openmrs_id"))
                        first_name = it.getString(it.getColumnIndexOrThrow("first_name"))
                        middle_name = it.getString(it.getColumnIndexOrThrow("middle_name"))
                        last_name = it.getString(it.getColumnIndexOrThrow("last_name"))
                        date_of_birth = it.getString(it.getColumnIndexOrThrow("date_of_birth"))
                        address1 = it.getString(it.getColumnIndexOrThrow("address1"))
                        address2 = it.getString(it.getColumnIndexOrThrow("address2"))
                        phone_number = it.getString(it.getColumnIndexOrThrow("phone_number"))
                        gender = it.getString(it.getColumnIndexOrThrow("gender"))
                    }
                    patient // Return the populated patient object
                } else {
                    throw Exception("Patient not found")
                }
            }
        }
    }

    private fun formatVitalsAndDiagnostics(data: HashMap<String, String>?): String {
        return data?.entries?.joinToString(" | ") { "${it.key}=${it.value}" } ?: ""
    }
    private fun formatGeneralAdvice(input: String): String {
        val htmlTagWithContentRegex = Regex("<[^>]+>.*?</[^>]+>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        // Step 1: Remove HTML tags and their content
        var cleaned = htmlTagWithContentRegex.replace(input, "").replace(Regex("<[^>]+>"), "").trim()

        // Step 2: Remove lines that only contain a bullet (with or without spaces)
        cleaned = cleaned.lines().filterNot { it.trim() == "•" }.joinToString("\n")
        return cleaned.trim()
    }

    private fun getDrDetails(): ClsDoctorDetails? {
        var doctorDetailsModel: ClsDoctorDetails? = null
        val drDetails: String = ObsDAO.fetchDrDetailsFromLocalDb(visitUuid)

        if (drDetails.isNullOrEmpty() || drDetails.equals("null", ignoreCase = true)) {
            Toast.makeText(activity, activity.getString(R.string.unablet_get_the_doct_info_alert), Toast.LENGTH_SHORT).show()
        } else {
            val gson = Gson()
            doctorDetailsModel = gson.fromJson(drDetails, ClsDoctorDetails::class.java)
        }
        return doctorDetailsModel
    }
}
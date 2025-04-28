package org.intelehealth.app.ui.prescriptionwithotp

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import org.intelehealth.app.R
import org.intelehealth.app.database.dao.VisitsDAO
import org.intelehealth.app.models.ClsDoctorDetails
import org.intelehealth.app.models.Patient
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ShowPrescriptionPdfShareDialog(
    private val activity: Activity,
    private val visitID: String,
    private val patientPhone: String?,
    private val openmrsID: String,
    private val patient: Patient,
    private val startDate: String,
    private val diagnosis: String,
    private val medication: String,
    private val tests: String,
    private val advice: String,
    private val followUp: String,
    private val doctorDetails: ClsDoctorDetails,
    private val complaint: String,
    private val vitals: Map<String, String>,
    private val fontFamily: String,
    private val doctorSignText: String,
    private val diagnostics:Map<String, String>,
    private val referredSpeciality: String,
    ) {


    fun createAndSaveFile(hwMobileNumber: String) {
        val fileNamePatientName = patient.first_name.replace(" ", "-")
        val prescriptionString = "Prescription"
        //[Patient_First_Name]_[Patient_Last_Name]_[OpenMRS_ID]_[Visit_Start_Date].pdf
        //val fileName = "$fileNamePatientName-$prescriptionString-$startDate.pdf"
        val fileName = java.lang.String.format("%s_%s_%s_%s.pdf", patient.first_name, patient.last_name, openmrsID, startDate).trim()+ ".pdf"
        Log.d("TAG", "createAndSaveFile: fileName : "+fileName)
        val builder = buildAndSavePrescription(fileName)

        try {
            val simpleFile = builder.simpleGeneratedFile

            if (simpleFile != null && simpleFile.exists()) {
                val uri = FileProvider.getUriForFile(
                    activity,
                    "${activity.packageName}.fileprovider",
                    simpleFile
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setPackage("com.whatsapp")
                    //data =i.parse("https://wa.me/$phoneNumber")
                }

                activity.startActivity(intent)
            } else {
                Toast.makeText(activity, "Simple PDF not generated yet.", Toast.LENGTH_SHORT).show()
            }
        } catch (exception: ActivityNotFoundException) {
            Toast.makeText(
                activity,
                activity.getString(R.string.please_install_whatsapp),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun buildAndSavePrescription(fileName: String):PrescriptionWithPDFBuilder {
        val vitalsData = mapOf(
            PrescriptionDetailsDataKeys.Vitals.HEIGHT to vitals[PrescriptionDetailsDataKeys.Vitals.HEIGHT],
            PrescriptionDetailsDataKeys.Vitals.WEIGHT to vitals[PrescriptionDetailsDataKeys.Vitals.WEIGHT],
            PrescriptionDetailsDataKeys.Vitals.BMI to vitals[PrescriptionDetailsDataKeys.Vitals.BMI],
            PrescriptionDetailsDataKeys.Vitals.TEMPERATURE to vitals[PrescriptionDetailsDataKeys.Vitals.TEMPERATURE],
            PrescriptionDetailsDataKeys.Vitals.SPO2 to vitals[PrescriptionDetailsDataKeys.Vitals.SPO2],
            PrescriptionDetailsDataKeys.Vitals.BP to vitals[PrescriptionDetailsDataKeys.Vitals.BP],
            PrescriptionDetailsDataKeys.Vitals.RESPIRATORY_RATE to vitals[PrescriptionDetailsDataKeys.Vitals.RESPIRATORY_RATE]
        )
        val diagnosticsData = mapOf(
               PrescriptionDetailsDataKeys.Diagnostics.GLUCOSE_RANDOM to diagnostics[PrescriptionDetailsDataKeys.Diagnostics.GLUCOSE_RANDOM],
            PrescriptionDetailsDataKeys.Diagnostics.HAEMOGLOBIN to diagnostics[PrescriptionDetailsDataKeys.Diagnostics.HAEMOGLOBIN],
           )

        val complaintDetails = formatComplaintsWithBullets(complaint);

        val complaintsData = mapOf(
            PrescriptionDetailsDataKeys.MedicationPlan.MEDICINE_DETAILS to complaintDetails,
        )

       val diagnosisData = mapOf(
            PrescriptionDetailsDataKeys.Diagnosis.PRIMARY to diagnosis
        )

        val medicineData = formatMedicineData(medication);
        val medicationData = mapOf(
           PrescriptionDetailsDataKeys.MedicationPlan.MEDICINE_DETAILS to medicineData,
       )

           val adviceData = mapOf(
             PrescriptionDetailsDataKeys.GeneralAdvice.ADVICE to advice
         )

         val followUpData = mapOf(
             PrescriptionDetailsDataKeys.FollowUp.DATE to followUp
         )
        val testsData = mapOf(
            PrescriptionDetailsDataKeys.Tests.TESTS to tests
        )
        val refferradSpeciality = mapOf(
            PrescriptionDetailsDataKeys.Tests.TESTS to referredSpeciality
        )


        val patientDataSections = mapOf(
            "Vitals" to vitalsData,
            "Diagnostics" to diagnosticsData,
            "Presenting Complaint(s)" to complaintsData,
            "Diagnosis" to diagnosisData,
            "Medication Plan" to medicationData,
            "General Advice" to adviceData,
            "Tests" to testsData,
            "Referral" to refferradSpeciality,
            "Follow Up Date" to followUpData

        )
        val builder = PrescriptionWithPDFBuilder(activity, patientDataSections)
        builder.setPatientData(createPatientData())
        builder.setPatientDataSections(patientDataSections)
        builder.buildDynamicUI()
        builder.createSignatureBitmap(fontFamily, activity, doctorSignText, doctorDetails)
        builder.build(fileName)
        return builder;
    }


    fun formatMedicineData(medicineData: String): String {
        val formattedData = StringBuilder()
        val medicines = medicineData.split("\n")  // Split by newline to separate each medicine

        var previousMedicine: String? = null

        for (medicine in medicines) {
            if (medicine.trim().isEmpty()) continue
            val medicineName = medicine.split(" ")[0].split(":")[0]
            if (previousMedicine == null || previousMedicine != medicineName) {
                if (previousMedicine != null) {
                    formattedData.append("\n")
                }
                formattedData.append("• $medicineName: ")
                previousMedicine = medicineName
            } else {
                formattedData.append(" | ")
            }
            val details = medicine.split(":").drop(1).joinToString(":")
            formattedData.append(details)
        }

        return formattedData.toString()
    }
    fun createPatientData(): String {
        val fullName = listOfNotNull(
            patient.first_name,
            patient.middle_name.takeIf { !it.isNullOrBlank() },
            patient.last_name
        ).joinToString(" ")

        val ageGender = "${activity.getString(R.string.label_age)} ${getPatientAge(patient.date_of_birth)} | ${activity.getString(R.string.label_gender)} ${patient.gender}"

        val patientIdLine = "${activity.getString(R.string.label_patient_id)} ${patient.openmrs_id}"
        val visitStartDate = VisitsDAO().getVisitStartDate(visitID)
        val visitDateLine = "${activity.getString(R.string.label_visit_date)} $visitStartDate"
        Log.d("TAG", "createPatientData: startDate : "+visitStartDate)
        Log.d("TAG", "createPatientData: visitID : "+visitID)

        // Combine all data into one string
        return listOf(fullName, ageGender, patientIdLine, visitDateLine)
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
    fun formatComplaintsWithBullets(complaintHtml: String): String {
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
// inside createAndSaveFile()


}
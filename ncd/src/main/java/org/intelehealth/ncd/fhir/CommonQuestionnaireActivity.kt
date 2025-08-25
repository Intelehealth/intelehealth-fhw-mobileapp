package org.intelehealth.ncd.fhir

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.commitNow
import androidx.lifecycle.lifecycleScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.datacapture.QuestionnaireFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.IntegerType
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.StringType
import org.intelehealth.ncd.R
import org.json.JSONObject
import java.text.SimpleDateFormat

class CommonQuestionnaireActivity : AppCompatActivity() {
    companion object {
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire_fragment_tag"
    }

    // create the filename & title list
    // for the questionnaire
    private val questionnaireFiles = listOf("hypertension_screening.json", "anemia_screening.json","diabetes_screening.json")
    private val questionnaireTitles = listOf("Hypertension Screening", "Anemia Screening","Diabetes Screening")
    private var isRecurring = false // set to true if you want to use recurring questionnaire

    var fragmentBuilder: QuestionnaireFragment.Builder? = null
    var questionnaireTitle: String? = null
    var questionnaireJSONObject: JSONObject? = null
    var patientAge: Float? = 0f
    var patientDOB: String? = null
    var patientGender: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_questionnaire)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        questionnaireTitle = intent.getStringExtra("questionnaire_title")
        patientAge = intent.getFloatExtra("patient_age", 0f)
        patientDOB = intent.getStringExtra("patient_dob")
        patientGender = intent.getStringExtra("patient_gender")
        supportActionBar?.title = questionnaireTitle
        if (questionnaireTitle.equals("Hypertension Screening", true)) {
            isRecurring = true // set to true if you want to use recurring questionnaire
        }
        Log.d("FHIR", "Questionnaire Title: $questionnaireTitle")
        Log.d("FHIR", "Patient Age: $patientAge")
        Log.d("FHIR", "Patient Gender: $patientGender")
        // ✅ Load questionnaire from assets
        /*val questionnaire: String =
            questionarFilepath?.let { assets.open(it).bufferedReader().use { it.readText() } }*/

        // ✅ Create Patient resource
        val patient = Patient().apply {
            id = "example"
            gender = Enumerations.AdministrativeGender.FEMALE
            birthDate = SimpleDateFormat("yyyy-MM-dd").parse("1990-06-15")
        }

        // ✅ Convert Patient to JSON (if needed later)
        //val patientJson = JsonParser.getInstance().encodeResourceToString(patient)
        /*val questionnaireJsonString =
            application.assets.open("hypertension_screening_poc_jing.json").bufferedReader()
                .use { it.readText() }*/

        if (savedInstanceState == null) {
            loadQuestionnaireFragment(null)
        }
        supportFragmentManager.setFragmentResultListener(
            QuestionnaireFragment.SUBMIT_REQUEST_KEY,
            this
        ) { requestKey, result ->
            lifecycleScope.launch {
                getQuestionnaireResponseManually()

            }
        }
        if (isRecurring)
            startQuestionnaireMonitoring()
    }

    private fun loadQuestionnaireFragment(questionnaireResponse: Any?) {

        // match with the questionnaireTitles then found the file name from questionnaireFiles
        val patient = Patient().apply {
            id = "intelehealth"
            // patientGender.equals("M") ? Enumerations.AdministrativeGender.MALE: Enumerations.AdministrativeGender.FEMALE
            gender = when (patientGender?.uppercase()) {
                "M" -> Enumerations.AdministrativeGender.MALE
                "F" -> Enumerations.AdministrativeGender.FEMALE
                else -> Enumerations.AdministrativeGender.OTHER
            }
            addExtension(
                Extension("http://intelehealth.org/fhir/StructureDefinition/patient-age",
                    patientAge?.toInt()?.let { IntegerType(it) })
            )
            birthDate = SimpleDateFormat("yyyy-MM-dd").parse(patientDOB)
        }


        val patientJson = FhirContext.forR4Cached()
            .newJsonParser()
            .encodeResourceToString(patient)

        val launchContextMap = mapOf("patient" to patientJson)
        // print console log
        println("Launch context map: $launchContextMap")

        val questionnaireFileName =
            questionnaireFiles[questionnaireTitles.indexOf(questionnaireTitle)]

        val questionnaire: String =
            assets.open(questionnaireFileName).bufferedReader().use { it.readText() }
        questionnaireJSONObject = JSONObject(questionnaire)
        supportFragmentManager.commitNow {
            setReorderingAllowed(true)
            fragmentBuilder = QuestionnaireFragment.builder()
                .setQuestionnaire(questionnaire)
                .setQuestionnaireLaunchContextMap(launchContextMap)
                .showAsterisk(true)
                .showRequiredText(false)
            // If you want your questionnaire to start with some answers already filled,
            // include a questionnaire response in your arguments bundle for your
            //.setQuestionnaireResponse(questionnaireResponse)
            //.setShowCancelButton(true)
            if (questionnaireResponse != null)
                fragmentBuilder!!.setQuestionnaireResponse(questionnaireResponse.toString())
            replace(
                R.id.fragment_container_view,
                fragmentBuilder!!.build(),
                QUESTIONNAIRE_FRAGMENT_TAG
            )
        }

    }

    private suspend fun getQuestionnaireResponseManually() {
        val fragment =
            supportFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as? QuestionnaireFragment
        val questionnaireResponse = fragment?.getQuestionnaireResponse()
        questionnaireResponse?.let {
            val jsonParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
            val json = jsonParser.encodeResourceToString(it)
            Log.d("FHIR", "Manual Response: $json")

            // show toast

            Toast.makeText(
                this@CommonQuestionnaireActivity,
                "Questionnaire submitted successfully",
                Toast.LENGTH_SHORT
            ).show()
            // clean finish the activity
            // set the result to the activity
            //
            lastQuestionnaireResponse = it
            lastQuestionnaireResponseString = json
            // convert this json string to json object
            val questionnaireJsonObject = JSONObject(json)
            val responseJsonObject = JSONObject(json)
            val summaryHtml = questionnaireJSONObject?.let { it1 ->
                questionnaireTitle?.let { it2 ->
                    /*QuestionnaireUtils.questionnaireResponseToSummary(
                        it2,
                        it1, responseJsonObject
                    )*/
                    /*QuestionnaireUtils.questionnaireResponseToSummaryV1(
                        it2,
                        it1, responseJsonObject
                    )*/

                    QuestionnaireUtils.questionnaireResponseToSummaryV3(
                        it2,
                        it1, responseJsonObject, false
                    )
                }
            }


            // set the result to the activity
            // Convert your result to JSON
            val resultIntent = Intent().apply {
                putExtra("questionnaire_response", summaryHtml)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            Log.d("FHIR", "sending back...")
            finish()
        }

    }

    private fun showHospitalPopup() {
        AlertDialog.Builder(this)
            .setTitle("Immediate Referral Needed")
            .setMessage("Your blood pressure is critically high. Please go to the hospital immediately.")
            .setPositiveButton("OK", null)
            .show()
    }

    var lastResponseHash: Int? = null
    var lastQuestionnaireResponse: QuestionnaireResponse? = null
    var lastQuestionnaireResponseString: String? = null

    // working
// "expression": "%resource.item.where(linkId='bp_measurement_page').item.where(linkId='sbp_dbp_measurement_1').item.where(linkId='sbp_m1').answer.value > 139"
    private fun startQuestionnaireMonitoring() {
        lifecycleScope.launch {
            while (isActive) {
                delay(5000) // Check every 5 second (adjust as needed)
                val fragment =
                    supportFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as? QuestionnaireFragment
                lastQuestionnaireResponse = fragment?.getQuestionnaireResponse()
//                for (item in lastQuestionnaireResponse?.getItem()!!) {
//                    Log.d("FHIR", "Item: ${item.linkId}")
//                    if (item.linkId == "referral") {
//                        if (!item.answer.isEmpty()) {
//                            val code = item.answerFirstRep.valueCoding.code
//                            Log.d("FHIR", "Referral Code: $code")
//                            if ("hospital" == code) {
//                                showHospitalPopup()
//                            }
//                        }
//                    }
//                }
                lastQuestionnaireResponse?.let {
                    val jsonParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
                    lastQuestionnaireResponseString = jsonParser.encodeResourceToString(it)
                    Log.d("FHIR", "Response: $lastQuestionnaireResponseString")
                    // Create a temp response with only item list
                    /*val tempResponse = QuestionnaireResponse().apply {
                        item = it.item
                    }
                    itemResponse = jsonParser.encodeResourceToString(tempResponse)*/
                }
                //val currentHash = itemResponse.hashCode()
                /* Log.d("FHIR", "Current Response Hash: $currentHash")
                 Log.d("FHIR", "Last Response Hash: $lastResponseHash")*/
                // Check if the response has changed
                /*if (currentHash != lastResponseHash) {
                    lastResponseHash = currentHash

                    loadQuestionnaireFragment(lastQuestionnaireResponseString)
                }*/

                lastQuestionnaireResponse?.let {
                    extractTimedBpReadings(it)
                    // Rebind response to re-evaluate enableWhenExpression
                    //fragment.setQuestionnaireResponse(it)
                    if (shouldShowAlertFromLatest())
                        showBpDialogOnceWithTimer()
                }
            }
        }

    }


    data class TimedBpReading(
        var sbp: Int,
        var dbp: Int,
        var timestamp: Long,
        var shownDialogOnce: Boolean = false
    )

    private val bpReadings: MutableList<TimedBpReading?> = MutableList(3) { null }

    private fun extractTimedBpReadings(response: QuestionnaireResponse): List<TimedBpReading?> {
        val readingGroups = listOf(
            Pair("sbp_m1", "dbp_m1"), // index 0
            Pair("sbp_m2", "dbp_m2"), // index 1
            Pair("sbp_m3", "dbp_m3")  // index 2
        )

        // List of fixed size 3, initialized with nulls
        //val readings = MutableList<TimedBpReading?>(3) { null }

        readingGroups.forEachIndexed { index, (sbpId, dbpId) ->
            val sbp = extractAnswer(response, sbpId)
            val dbp = extractAnswer(response, dbpId)
            if (sbp != null && dbp != null && sbp >= 70 && dbp >= 50) {
                /* bpReadings[index] = TimedBpReading(
                     sbp = sbp,
                     dbp = dbp,
                     timestamp = System.currentTimeMillis()
                 )*/
                // if already have the data then change in existing TimedBpReading object
                if (bpReadings[index] != null) {
                    bpReadings[index]?.sbp = sbp
                    bpReadings[index]?.dbp = dbp
                    bpReadings[index]?.timestamp = System.currentTimeMillis()
                } else {
                    // create new TimedBpReading object
                    bpReadings[index] = TimedBpReading(
                        sbp = sbp,
                        dbp = dbp,
                        timestamp = System.currentTimeMillis()
                    )
                }

            }
        }
        return bpReadings

    }


    /* private fun shouldShowAlert(sbp: Int?, dbp: Int?): Boolean {
         return (sbp != null && (sbp > 139 || sbp < 90)) || (dbp != null && (dbp > 89 || dbp < 60))
     }*/
    var foundIndexedValue: Int? = 0
    fun shouldShowAlertFromLatest(): Boolean {
        Log.d("FHIR", "Checking BP Readings: $bpReadings")
        // if all are null then also return
        if (bpReadings.all { it == null }) {
            return false
        }
        // If third reading (m3) exists, suppress alert entirely
        if (bpReadings[2] != null) {
            return false
        }

        // Check from second (m2) and first (m1)
        for (index in 1 downTo 0) {
            val reading = bpReadings[index]
            if (reading != null) {
                if (reading.shownDialogOnce) {
                    return false
                }
                val sbp = reading.sbp
                val dbp = reading.dbp
                //Log.d("FHIR", "Checking BP Reading at index $index: SBP=$sbp, DBP=$dbp")
                val isAbnormal = (sbp > 139 || sbp < 90) || (dbp > 89 || dbp < 60)
                if (isAbnormal) {
                    Log.d("FHIR", "Abnormal BP detected at index $index: SBP=$sbp, DBP=$dbp")
                    // Show dialog only once for the first abnormal reading
                    /*if (!reading.shownDialogOnce) {
                        reading.shownDialogOnce = true

                    }*/
                } else {
                    Log.d("FHIR", "Normal BP at index $index: SBP=$sbp, DBP=$dbp")
                }
                if (isAbnormal) foundIndexedValue = index
                return isAbnormal
            }
        }

        // No readings at all
        return false
    }

    private var hasShownDialog = false
    private fun showBpDialogOnce() {
        if (!hasShownDialog) {
            hasShownDialog = true
            AlertDialog.Builder(this)
                .setTitle("BP Alert")
                .setMessage("Abnormal BP detected. Refer to a doctor.")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private var lastDialogShownTime: Long = 0
    private val FIVE_MINUTES_MILLIS: Long = 5 * 1000
    private var isShownOnce0: Boolean = false
    private var isShownOnce1: Boolean = false

    // create array to keep flag
    // to check if dialog is shown once
    // and then show dialog only once

    private fun showBpDialogOnceWithTimer() {
        /* if (isShownOnce0 && foundIndexedValue == 0)
             return // If already shown once, do not show again
         if (isShownOnce1 && foundIndexedValue == 1)
             return // If already shown once, do not show again*/

        if (bpReadings[foundIndexedValue!!]?.shownDialogOnce == true)
            return // If already shown once, do not show again
        if (!hasShownDialog) {
            hasShownDialog = true
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastDialogShownTime >= FIVE_MINUTES_MILLIS) {
                lastDialogShownTime = currentTime

                val builder = AlertDialog.Builder(this)
                builder.setTitle("BP Alert")

                val messageView = TextView(this)
                messageView.setPadding(40, 40, 40, 40)
                messageView.textSize = 16f
                builder.setView(messageView)

                builder.setCancelable(false)
                // builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }

                val dialog = builder.create()
                dialog.show()
                /*if (foundIndexedValue == 0) {
                    isShownOnce0 = true
                } else if (foundIndexedValue == 1) {
                    isShownOnce1 = true
                }*/

                bpReadings[foundIndexedValue!!]?.shownDialogOnce = true
                //isShownOnce = true;
                object : CountDownTimer(FIVE_MINUTES_MILLIS, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val minutes = (millisUntilFinished / 1000) / 60
                        val seconds = (millisUntilFinished / 1000) % 60
                        messageView.text =
                            "Abnormal BP detected. Please recheck BP after 5 minutes.\n\n" +
                                    "This dialog will close in ${
                                        "%02d:%02d".format(
                                            minutes,
                                            seconds
                                        )
                                    }"
                    }

                    override fun onFinish() {
                        if (dialog.isShowing) {
                            dialog.dismiss()
                            hasShownDialog = false
                            // reload the fragment to reset the state
                            loadQuestionnaireFragment(lastQuestionnaireResponseString)
                        }
                    }
                }.start()

            }
        }
    }


    fun isBpAbnormal(sbp: Int, dbp: Int): Boolean {
        return sbp > 139 || sbp < 90 || dbp > 89 || dbp < 60
    }

    fun extractAnswer(response: QuestionnaireResponse, linkId: String): Int? {
        return response.item
            .flatMap { it.item ?: listOf(it) }
            .flatMap { it.item ?: listOf(it) }
            .find { it.linkId == linkId }
            ?.answer?.firstOrNull()
            ?.valueIntegerType
            ?.value
    }


}

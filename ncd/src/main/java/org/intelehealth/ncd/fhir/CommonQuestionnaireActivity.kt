package org.intelehealth.ncd.fhir

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.commitNow
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.IntegerType
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.intelehealth.ncd.R
import org.json.JSONObject
import java.text.SimpleDateFormat
import org.intelehealth.ncd.fhir.QuestionnaireUtils.checkRequiredWithConditionalsKotlin

class CommonQuestionnaireActivity : AppCompatActivity() {
    companion object {
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire_fragment_tag"
    }

    private var latestQuestionnaire: String? = null

    // create the filename & title list
    // for the questionnaire
    private val questionnaireFiles =
        listOf("hypertension_screening.json", "anemia_screening.json", "diabetes_screening.json","hypertension_followup.json")
    private val questionnaireTitles =
        listOf("Hypertension Screening", "Anemia Screening", "Diabetes Screening","Hypertension Followup")
    private var isRecurring = false // set to true if you want to use recurring questionnaire

    var fragmentBuilder: QuestionnaireFragment.Builder? = null
    var questionnaireTitle: String? = null
    var questionnaireJSONObject: JSONObject? = null
    var patientAge: Float? = 0f
    var patientDOB: String? = null
    var patientGender: String? = null
    var questionnaireFragment: QuestionnaireFragment? = null
    var bottomNav = null
    var bottomActionController: QuestionnaireBottomActionController? = null
    lateinit var rootView: View
    val matchedViews = mutableListOf<View>()

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
        if (questionnaireTitle.equals("Hypertension Screening", true) || questionnaireTitle.equals("Hypertension Followup", true)) {
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
            loadQuestionnaireFragment(null, false, -1)
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

    private fun loadQuestionnaireFragment(
        questionnaireResponse: Any?,
        isDisableRequired: Boolean,
        index: Int
    ) {

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
                Extension(
                    "http://intelehealth.org/fhir/StructureDefinition/patient-age",
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

        latestQuestionnaire =
            assets.open(questionnaireFileName).bufferedReader().use { it.readText() }
        // need to disable the sbp & dbp fields

        questionnaireJSONObject = latestQuestionnaire?.let { JSONObject(it) }
        supportFragmentManager.commitNow {
            setReorderingAllowed(true)
            fragmentBuilder = QuestionnaireFragment.builder()
                .setQuestionnaire(latestQuestionnaire!!)
                .setQuestionnaireLaunchContextMap(launchContextMap)
                .showAsterisk(true)
                .showRequiredText(false)
                .setShowSubmitAnywayButton(false)

            // If you want your questionnaire to start with some answers already filled,
            // include a questionnaire response in your arguments bundle for your
            //.setQuestionnaireResponse(questionnaireResponse)
            //.setShowCancelButton(true)
            if (questionnaireResponse != null)
                fragmentBuilder!!.setQuestionnaireResponse(questionnaireResponse.toString())

            questionnaireFragment = fragmentBuilder!!.build()

            replace(
                R.id.fragment_container_view,
                questionnaireFragment!!,
                QUESTIONNAIRE_FRAGMENT_TAG
            )

            // commitNow already used earlier, so view should exist — but run in post to be safe
            supportFragmentManager.executePendingTransactions()

            // Observe viewLifecycleOwnerLiveData so we run only after onCreateView/onViewCreated
            questionnaireFragment!!.viewLifecycleOwnerLiveData.observe(this@CommonQuestionnaireActivity) { owner ->
                if (owner != null) {
                    // Now it's safe to use requireView()
                    questionnaireFragment!!.requireView().post {
                        rootView = questionnaireFragment!!.requireView()
                        bottomActionController = QuestionnaireBottomActionController(rootView)

                        //bottomActionController.setBottomActionsEnabled(false)
                        bottomActionController!!.setBottomActionsEnabledSmooth(false)
                        //bottomActionController.attachAutoToggleForRequiredInputs()
                        // hideNextButtonIn(root)
                        updateUIComponents();
                    }
                }
            }

        }

    }
    private fun updateUIComponents(){
        Handler(Looper.getMainLooper()).postDelayed({
            updateUIComponentsNow()
        }, 1000) // 1000 ms = 1 second
    }
    private fun updateUIComponentsNow(){

        // Recursively find all TextInputEditText inside a view tree
        fun View.findAllTextInputs(result: MutableList<View>) {

            when (this) {
                // Text inputs
                is TextInputEditText,
                is AutoCompleteTextView,
                is EditText -> result.add(this)

                // Choice controls
                is CheckBox,
                is MaterialCheckBox,
                is RadioButton,
                is MaterialRadioButton,
                is Switch,
                is SwitchMaterial,
                is ToggleButton -> result.add(this)

                // Dropdowns / spinners
                is Spinner,
                is AppCompatSpinner -> result.add(this)

                // Buttons (date picker, add/remove item, etc.)
                is Button,
                is MaterialButton -> result.add(this)
            }

            if (this is ViewGroup) {
                for (i in 0 until childCount) {
                    getChildAt(i).findAllTextInputs(result)
                }
            }
        }

        // collect views matching text into a list
        /*val matched = ArrayList<View>()
        rootView.findViewsWithText(
            matched,
            "Your Question Text",
            View.FIND_VIEWS_WITH_TEXT
        )*/
        // collect all text inputs
        //val matched = mutableListOf<View>()
        rootView.findAllTextInputs(matchedViews)
        fun printInputDetails(view: View) {
            when (view) {
                is TextInputEditText -> {
                    val label = (view.parent?.parent as? com.google.android.material.textfield.TextInputLayout)?.hint
                    Log.d("Matched views", "TextInputEditText -> label=$label, hint=${view.hint}, value=${view.text}")
                }
                is AutoCompleteTextView -> {
                    Log.d("Matched views", "AutoCompleteTextView -> hint=${view.hint}, value=${view.text}")
                }
                is CheckBox -> {
                    Log.d("Matched views", "CheckBox -> text=${view.text}, checked=${view.isChecked}")
                }
                is RadioButton -> {
                    Log.d("Matched views", "RadioButton -> text=${view.text}, checked=${view.isChecked}")
                }
                is Switch -> {
                    Log.d("Matched views", "Switch -> text=${view.text}, checked=${view.isChecked}")
                }
                is ToggleButton -> {
                    Log.d("Matched views", "ToggleButton -> text=${view.text}, checked=${view.isChecked}")
                }
                is Spinner -> {
                    Log.d("Matched views", "Spinner -> prompt=${view.prompt}, selected=${view.selectedItem}")
                }
                is Button -> {
                    Log.d("Matched views", "Button -> text=${view.text}")
                }
                else -> {
                    Log.d("Matched views", "Other input type: ${view::class.java.simpleName}, id=${view.id}")
                }
            }
        }

        println("Matched views: $matchedViews")
        matchedViews.forEach { view ->
            printInputDetails(view)
            if (view is TextInputEditText) {
                // print TextInputEditText label

                val editId = view.resources.getResourceEntryName(view.id) // e.g., "text_input_edit_text"
                Log.d("Matched views", "EditText id: $editId")

                // Try to get its parent TextInputLayout for the label/hint
                val parentLayout = view.parent?.parent
                if (parentLayout is com.google.android.material.textfield.TextInputLayout) {
                    val label = parentLayout.hint // this is usually the Questionnaire question text
                    Log.d("Matched views", "Question label: $label")
                }
                view.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        Log.d("FHIR", "User typed: ${s.toString()}")
                        isAllowedForBottomActionEnable = false
                        bottomActionController?.setBottomActionsEnabledSmooth(isAllowedForBottomActionEnable)
                    }

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                        Log.d("FHIR", "Before text changed: ${s.toString()}")
                        bottomActionController?.setBottomActionsEnabledSmooth(false)
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        Log.d("FHIR", "On text changed: ${s.toString()}")
                        bottomActionController?.setBottomActionsEnabledSmooth(false)
                    }
                })
            }
        }
    }
    private fun hideNextButtonIn(root: View) {
        // 1) Try to find by resource id name (common patterns)
        val candidatesById = mutableListOf<View>()
        val res = root.resources
        fun findAllByIdName(view: View) {
            val id = view.id
            // show it

            if (id != View.NO_ID) {
                try {
                    val name = res.getResourceEntryName(id)
                    println("View: ${view.javaClass.simpleName}, id name=$name")
                    if (name.equals("text_input_edit_text")) {
                        // i need the more detail about the view
                        println("View Details: ${view.contentDescription}, ${view.visibility}, ${view.isShown}, ${view.isClickable}, ${view.isEnabled}, ${view.isFocusable}")
                        // need the view all details
                        println("View All Details: $view")

                    }
                    if (name.contains("next", ignoreCase = true) || name.contains(
                            "action_next",
                            ignoreCase = true
                        )
                    ) {
                        candidatesById.add(view)
                    }
                } catch (ex: Exception) {
                    // id might not be a resource id from this package; ignore
                }
            }
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) findAllByIdName(view.getChildAt(i))
            }
        }
        findAllByIdName(root)
        if (candidatesById.isNotEmpty()) {
            candidatesById.forEach { it.visibility = View.GONE }
            return
        }

        // 2) Find by visible Button text "Next" or content description containing "next"
        val textMatches = mutableListOf<View>()
        fun findByTextOrDesc(view: View) {
            if (view is Button || view is TextView) {
                val tvText = (view as TextView).text?.toString() ?: ""
                val cd = view.contentDescription?.toString() ?: ""
                if (tvText.contains("next", ignoreCase = true) || cd.contains(
                        "next",
                        ignoreCase = true
                    )
                ) {
                    textMatches.add(view)
                }
            }
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) findByTextOrDesc(view.getChildAt(i))
            }
        }
        findByTextOrDesc(root)
        if (textMatches.isNotEmpty()) {
            textMatches.forEach { it.visibility = View.GONE }
            return
        }

        // 3) Fallback: hide any Button near the bottom (best-effort)
        val bottomButtons = mutableListOf<View>()
        fun collectButtons(view: View) {
            if (view is Button && view.isShown) bottomButtons.add(view)
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) collectButtons(view.getChildAt(i))
            }
        }
        collectButtons(root)
        // heuristics: prefer the button with largest Y (lowest on screen)
        if (bottomButtons.isNotEmpty()) {
            val toHide = bottomButtons.maxByOrNull { it.y } // lowest position
            toHide?.visibility = View.GONE
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


            val missing =
                latestQuestionnaire?.let { it1 -> checkRequiredWithConditionalsKotlin(it1, json) }
            // print length of missing
            println("Missing required items count: ${missing?.size}")
            println("Missing required items: $missing")
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
    var isAllowedForBottomActionEnable: Boolean = false

    // working
// "expression": "%resource.item.where(linkId='bp_measurement_page').item.where(linkId='sbp_dbp_measurement_1').item.where(linkId='sbp_m1').answer.value > 139"
    private fun startQuestionnaireMonitoring() {
        lifecycleScope.launch {
            while (isActive) {
                delay(1000) // Check every 5 second (adjust as needed)
               // updateUIComponents()
                Log.d("BP_MONITOR", "bpReadings = $bpReadings")
                Log.d("BP_MONITOR", "bpReadingsHelper = $bpReadingsHelper")
                // check if bpReadings all  shownDialogOnceForTimer done then not need to refresh again and again
                // also check last item have false value for shownDialogOnceForTimer
                if (isAllowedForBottomActionEnable) {
                    Log.d("FHIR", "All BP readings have shown dialog once. Stopping monitoring.")
                    bottomActionController?.setBottomActionsEnabledSmooth(isAllowedForBottomActionEnable)
                    continue
                }

                val fragment =
                    supportFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as? QuestionnaireFragment
                lastQuestionnaireResponse = fragment?.getQuestionnaireResponse()

                lastQuestionnaireResponse?.let {
                    val jsonParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
                    lastQuestionnaireResponseString = jsonParser.encodeResourceToString(it)
                    Log.d("FHIR", "Response: $lastQuestionnaireResponseString")

                }
                lastQuestionnaireResponse?.let {
                    extractTimedBpReadings(it)

                    isAllowedForBottomActionEnable = bpReadingsHelper.all { it == null }
                    bottomActionController?.setBottomActionsEnabledSmooth(isAllowedForBottomActionEnable)


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
        var shownDialogOnceForTimer: Boolean = false,
        var isValidData: Boolean = false,
        var validationDialogShownOnce: Boolean = false

    )

    private val bpReadings: MutableList<TimedBpReading?> = MutableList(3) { null }
    private val bpReadingsHelper: MutableList<TimedBpReading?> = MutableList(3) { null }


    private fun extractTimedBpReadings(response: QuestionnaireResponse): List<TimedBpReading?> {
        val readingGroups = listOf(
            Pair("sbp_m1", "dbp_m1"), // index 0
            Pair("sbp_m2", "dbp_m2"), // index 1
            Pair("sbp_m3", "dbp_m3")  // index 2
        )

        // List of fixed size 3, initialized with nulls
        //val readings = MutableList<TimedBpReading?>(3) { null }

        readingGroups.forEachIndexed { index, (sbpId, dbpId) ->
            // check sbpid & dbpid linkid is exist or not exists in the response
            if (validateLinkIdExistInResponse(response, sbpId) == false ||
                validateLinkIdExistInResponse(response, dbpId) == false
            ) {
                // if not exist then continue
                println("LinkId $sbpId or $dbpId not found in response. Skipping index $index.")

                return@forEachIndexed
            }


            val sbp = extractAnswer(response, sbpId)
            val dbp = extractAnswer(response, dbpId)


            if (sbp == null || dbp == null) {
                Log.d("FHIR", "Extracted Reading at index $index: SBP=$sbp, DBP=$dbp")
                bpReadingsHelper[index] = TimedBpReading(
                    sbp = sbp ?: -1,
                    dbp = dbp ?: -1,
                    timestamp = System.currentTimeMillis(),
                    isValidData = false
                )

            }
            //if (sbp != null && dbp != null && sbp >= 70 && dbp >= 40 && sbp <= 220 && dbp <= 120) {
            if (
                sbp != null && dbp != null &&
                sbp in 70..220 &&
                dbp in 40..120 &&
                sbp > dbp
            ) {
                /* bpReadings[index] = TimedBpReading(
                     sbp = sbp,
                     dbp = dbp,
                     timestamp = System.currentTimeMillis()
                 )*/
                // if already have the data then change in existing TimedBpReading object

                // check sbp > dbp an also check the range of dbp & sbp


                if (bpReadings[index] != null) {
                    bpReadings[index]?.sbp = sbp
                    bpReadings[index]?.dbp = dbp
                    bpReadings[index]?.timestamp = System.currentTimeMillis()
                    bpReadings[index]?.isValidData = true
                } else {
                    // create new TimedBpReading object
                    bpReadings[index] = TimedBpReading(
                        sbp = sbp,
                        dbp = dbp,
                        timestamp = System.currentTimeMillis(),
                        isValidData = true
                    )
                }
                bpReadingsHelper[index] = null
            } else {
                if (sbp != null && dbp != null) {
                    if (bpReadingsHelper[index] != null) {
                        // check old values are changed or not if changed then only update the timestamp
                        if (bpReadingsHelper[index]?.sbp != sbp || bpReadingsHelper[index]?.dbp != dbp) {
                            bpReadingsHelper[index]?.sbp = sbp
                            bpReadingsHelper[index]?.dbp = dbp
                            bpReadingsHelper[index]?.timestamp = System.currentTimeMillis()
                            bpReadingsHelper[index]?.validationDialogShownOnce = false
                        }
                    } else {
                        bpReadingsHelper[index] = TimedBpReading(
                            sbp = sbp,
                            dbp = dbp,
                            timestamp = System.currentTimeMillis(),
                            isValidData = false
                        )
                    }
                    if (sbp <= dbp) {
                        Log.d("FHIR", "Invalid BP Reading at index $index: SBP=$sbp, DBP=$dbp")

                        if (!bpReadingsHelper[index]?.validationDialogShownOnce!!) {
                            bpReadingsHelper[index]?.validationDialogShownOnce = true
                            // show alert dialog new
                            AlertDialog.Builder(this)
                                .setTitle("Input Error")
                                .setCancelable(false)
                                .setMessage("Systolic BP (SBP) should be greater than Diastolic BP (DBP). Please correct the values.")
                                .setPositiveButton("OK") { dialog, _ ->
                                    dialog.dismiss()

                                }
                                .show()
                        }
                    }


                }
                // if the value is not valid then set to null
                bpReadings[index] = null
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
        Log.d("FHIR", "Checking BP bpReadingsHelper: $bpReadingsHelper")
        // if all are null then also return
        if (bpReadings.all { it == null }) {
            return false
        }
        // If third reading (m3) exists, suppress alert entirely
        if (bpReadings[2] != null) {
            if (!bpReadings[2]!!.shownDialogOnceForTimer) {
                bpReadings[2]?.shownDialogOnceForTimer = true
                // need to reset the other
                loadQuestionnaireFragment(lastQuestionnaireResponseString, true, 2)
            }
            return false
        }

        // Check from second (m2) and first (m1)
        for (index in 1 downTo 0) {
            val reading = bpReadings[index]
            if (reading != null) {
                if (reading.shownDialogOnceForTimer) {
                    return false
                }
                val sbp = reading.sbp
                val dbp = reading.dbp
                //Log.d("FHIR", "Checking BP Reading at index $index: SBP=$sbp, DBP=$dbp")
                val isAbnormal = (sbp > 139 || sbp < 90) || (dbp > 89 || dbp < 60)
                if (isAbnormal) {
                    Log.d("FHIR", "Abnormal BP detected at index $index: SBP=$sbp, DBP=$dbp")
                    // Show dialog only once for the first abnormal reading
                    /*if (!reading.shownDialogOnceForTimer) {
                        reading.shownDialogOnceForTimer = true

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

        if (bpReadings[foundIndexedValue!!]?.shownDialogOnceForTimer == true)
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
                isAllowedForBottomActionEnable = false
                bottomActionController?.setBottomActionsEnabledSmooth(isAllowedForBottomActionEnable)
                /*if (foundIndexedValue == 0) {
                    isShownOnce0 = true
                } else if (foundIndexedValue == 1) {
                    isShownOnce1 = true
                }*/

                bpReadings[foundIndexedValue!!]?.shownDialogOnceForTimer = true
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
                            isAllowedForBottomActionEnable = false
                            bottomActionController?.setBottomActionsEnabledSmooth(isAllowedForBottomActionEnable)
                            // reload the fragment to reset the state
                            loadQuestionnaireFragment(
                                lastQuestionnaireResponseString,
                                true,
                                foundIndexedValue!!
                            )
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

    fun validateAnswerExistForLinkId(response: QuestionnaireResponse?, linkId: String): Boolean {
        return searchItems(response?.item) { item ->
            item.linkId == linkId && !item.answer.isNullOrEmpty()
        }
    }

    fun validateLinkIdExistInResponse(response: QuestionnaireResponse?, linkId: String): Boolean {
        return searchItems(response?.item) { item ->
            item.linkId == linkId
        }
    }

    /** Generic recursive helper used by both validators */
    private fun searchItems(
        items: List<QuestionnaireResponse.QuestionnaireResponseItemComponent>?,
        predicate: (QuestionnaireResponse.QuestionnaireResponseItemComponent) -> Boolean
    ): Boolean {
        if (items.isNullOrEmpty()) return false
        for (item in items) {
            if (predicate(item)) return true
            if (searchItems(item.item, predicate)) return true
        }
        return false
    }


    /* fun hasLinkId(json: JSONObject, linkId: String): Boolean {
         // If current object has the linkId
         if (json.optString("linkId") == linkId) return true

         // If it has nested items, check them
         val items = json.optJSONArray("item")
         if (items != null) {
             for (i in 0 until items.length()) {
                 val child = items.getJSONObject(i)
                 if (hasLinkId(child, linkId)) {
                     return true
                 }
             }
         }
         return false
     }

     // Entry point
     fun validateLinkIdExistInAnswerResponse(response: JSONObject, linkId: String): Boolean {
         val items = response.optJSONArray("item") ?: return false
         for (i in 0 until items.length()) {
             if (hasLinkId(items.getJSONObject(i), linkId)) {
                 return true
             }
         }
         return false
     }*/


    /*  fun findAnswerExists(items: JSONArray?, linkId: String): Boolean {
          if (items == null) return false
          for (i in 0 until items.length()) {
              val item = items.getJSONObject(i)
              if (item.optString("linkId") == linkId) {
                  val answers = item.optJSONArray("answer")
                  if (answers != null && answers.length() > 0) return true
              }
              if (findAnswerExists(item.optJSONArray("item"), linkId)) return true
          }
          return false
      }

      // Lock a view by linkId (map linkId->EditText)
      fun lockFieldsFromQr(qrJson: JSONObject, linkIdToView: Map<String, EditText>) {
          val rootItems = qrJson.optJSONArray("item")
          for ((linkId, view) in linkIdToView) {
              if (findAnswerExists(rootItems, linkId)) {
                  view.isEnabled = false
                  view.isFocusable = false
                  view.isCursorVisible = false
                  view.keyListener = null
                  view.alpha = 0.6f
                  // set text from answer if you want:
                  val value = findAnswerInteger(rootItems, linkId)
                  if (value != null) view.setText(value.toString())
              } else {
                  view.isEnabled = true
              }
          }
      }

      // helper used above to get integer value
      fun findAnswerInteger(items: JSONArray?, linkId: String): Int? {
          if (items == null) return null
          for (i in 0 until items.length()) {
              val item = items.getJSONObject(i)
              if (item.optString("linkId") == linkId) {
                  val answers = item.optJSONArray("answer")
                  if (answers != null && answers.length() > 0) {
                      val firstAnswer = answers.getJSONObject(0)
                      if (firstAnswer.has("valueInteger")) return firstAnswer.getInt("valueInteger")
                  }
              }
              val nested = item.optJSONArray("item")
              val found = findAnswerInteger(nested, linkId)
              if (found != null) return found
          }
          return null
      }
  */
}

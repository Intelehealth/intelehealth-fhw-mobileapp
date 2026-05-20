package org.intelehealth.app.activities.filterPatientActivity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.text.TextWatcher
import android.util.Log
import android.widget.AdapterView
import java.util.Date
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.text.intl.Locale
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.RequestBody.Companion.toRequestBody
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.R
import org.intelehealth.app.activities.onboarding.PrivacyPolicyActivity_New
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.models.PatientSearchResult
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.models.dto.ResponseDTO
import org.intelehealth.app.shared.BaseActivity
import org.intelehealth.app.utilities.BundleKeys
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.ToastUtil
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.Credentials
import org.intelehealth.app.models.MatchGrade
import org.intelehealth.app.models.MatchSource
import org.intelehealth.app.models.dto.PatientSearchDTO
import org.intelehealth.app.ui.dialog.CalendarDialog
import org.intelehealth.app.ui.patient.activity.PatientRegistrationActivity
import org.intelehealth.klivekit.utils.DateTimeUtils
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.Calendar

class FilterPatientActivity : BaseActivity(), FilterPatientAdapter.AdapterClickListener {
    private var isFullyLoaded: Boolean = false
    private var isDataLoading: Boolean = false
    private val defaultPageSize: Int = 50
    private var offset = 0

    private lateinit var filterSuccessLayout: LinearLayout
    private lateinit var filterSuccessActionLayout: LinearLayout
    private lateinit var filterFailedLayout: LinearLayout
    private lateinit var filterRecyclerView: RecyclerView
    private lateinit var goWithSelectedButton: Button
    private lateinit var goContinueToRegistryCheck: Button
    private lateinit var loadingDialog: AlertDialog
    private lateinit var genderSpinner: Spinner
    private lateinit var firstNameTv: TextView
    private lateinit var lastNameTv: TextView
    private lateinit var phoneTv: TextView
    private lateinit var monthTv: TextView
    private lateinit var yearTv: TextView
    private lateinit var dayTv: TextView
    private lateinit var dobTv: TextView

    private val patientsDAO = PatientsDAO()
    private var selectedPatient: PatientDTO? = null
    private var patientList = mutableListOf<PatientSearchResult>()
    private val subscriptions: CompositeDisposable = CompositeDisposable()
    private val patientAdapter = FilterPatientAdapter(patientList, this)
    private var selectedDate: Long = System.currentTimeMillis()
    var isSpinnerFirstTime = true
    private var currentFirstName = ""
    private var currentLastName = ""
    private var currentPhone = ""
    private var currentGender = ""
    private var currentDob = ""
    private var handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_patient)

        filterRecyclerView = findViewById(R.id.filter_patient_container)
        filterRecyclerView.layoutManager = LinearLayoutManager(this@FilterPatientActivity)
        filterRecyclerView.adapter = patientAdapter

        loadingDialog = DialogUtils().showCommonLoadingDialog(
            this@FilterPatientActivity,
            getString(R.string.loading),
            getString(R.string.please_wait),
        ).apply {
            dismiss()
        }

        filterSuccessLayout = findViewById(R.id.filter_patient_success_ll)
        filterSuccessActionLayout = findViewById(R.id.success_action_layout)
        filterFailedLayout = findViewById(R.id.filter_patient_failed_ll)
        goContinueToRegistryCheck = findViewById(R.id.btn_continue_to_registry_check)
        //goWithSelectedButton = findViewById(R.id.btn_with_selected_patient)

        genderSpinner = findViewById(R.id.filter_txt_gender)
        firstNameTv = findViewById(R.id.filter_txt_first_name)
        lastNameTv = findViewById(R.id.filter_txt_last_name)
        phoneTv = findViewById(R.id.filter_txt_phone)
        dobTv= findViewById(R.id.filter_text_dob);
       /* monthTv = findViewById(R.id.filter_txt_dob_month)
        yearTv = findViewById(R.id.filter_txt_dob_year)
        dayTv = findViewById(R.id.filter_txt_dob_day)*/

        // changing status bar color
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor = Color.WHITE
        observeSearch()

        findViewById<ImageView>(R.id.iv_back_arrow)?.setOnClickListener {
            finish()
        }
        goContinueToRegistryCheck.setOnClickListener {

            Intent(this, FilterResultActivity::class.java).apply {
                putExtra("firstname", currentFirstName)
                putExtra("lastName", currentLastName)
                putExtra("dob", currentDob)
                putExtra("phone", currentPhone)
                putExtra("gender", currentGender)
            }.also {
                startActivity(it)
            }

        }


        /*findViewById<Button>(R.id.btn_filter_patient)?.setOnClickListener {
            if (validateUserInput()) {
                doFilter(
                    firstName = firstNameTv.text.toString(),
                    lastName = lastNameTv.text.toString(),
                    gender = when (genderSpinner.selectedItemPosition) {
                        1 -> "M"
                        2 -> "F"
                        3 -> "O"
                        else -> ""
                    },
                    phone = phoneTv.text.toString(),
                    dob = if (dayTv.text.isNotEmpty()) "${yearTv.text}-${
                        String.format(
                            "%2s",
                            monthTv.text
                        )
                    }-${String.format("%2s", dayTv.text)}" else ""
                )
            }
        }*/

        /*findViewById<Button>(R.id.btn_create_new_patient)?.setOnClickListener {
            goToCreateNewPatient()
        }*/
        patientAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                toggleSuccessLayout()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                toggleSuccessLayout()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                toggleSuccessLayout()
            }
        })

        genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                currentGender = when (position) {
                    1 -> "M"
                    2 -> "F"
                    3 -> "O"
                    else -> ""
                }

                triggerFilter()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        findViewById<LinearLayout>(R.id.add_new_patient_ll)?.setOnClickListener {
            goToCreateNewPatient()
        }

        /*goWithSelectedButton.setOnClickListener {
            goToPatientDetails()
        }*/
    }
    private fun toggleSuccessLayout() {
        filterSuccessActionLayout.visibility =
            if (patientAdapter.itemCount > 0) View.VISIBLE else View.GONE
    }
    private fun getGenderCode(position: Int): String = when (position) {
        1 -> "M"
        2 -> "F"
        3 -> "O"
        else -> ""
    }
    private fun showDatePickerDialog(selectedDate: Long) {

        CalendarDialog.Builder()
            .maxDate(Calendar.getInstance().timeInMillis)
            .selectedDate(selectedDate)
            .format(DateTimeUtils.MMM_DD_YYYY_FORMAT)
            .listener(dateListener)
            .build()
            .show(supportFragmentManager, CalendarDialog.TAG)
    }
    private val dateListener = object : CalendarDialog.OnDatePickListener {

        override fun onDatePick(day: Int, month: Int, year: Int, value: String?) {

            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)

            currentDob = DateTimeUtils.formatToLocalDate(
                calendar.time,
                DateTimeUtils.YYYY_MM_DD_HYPHEN
            )

            dobTv.setText(currentDob)

            triggerFilter()
        }
    }
    private fun observeSearch() {

        firstNameTv.addTextChangedListener {
            currentFirstName = it.toString().trim()
            triggerFilter()
        }
        lastNameTv.addTextChangedListener {
            currentLastName = it.toString().trim()
            triggerFilter()
        }
        phoneTv.addTextChangedListener {
            currentPhone = it.toString().trim()
            triggerFilter()
        }
        dobTv.apply {
            isFocusable = false
            isClickable = true

            setOnClickListener {
                showDatePickerDialog(selectedDate)
            }
        }
    }
    private fun triggerFilter() {

        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            doFilterLocal(
                firstName = currentFirstName,
                lastName = currentLastName,
                phone = currentPhone,
                gender = currentGender,
                dob = currentDob
            )
        }

        handler.postDelayed(runnable!!, 700)
    }

    override fun onItemClick(selectedItem: PatientDTO?) {
        selectedPatient = selectedItem as PatientDTO
        //goWithSelectedButton.isEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        subscriptions.clear()
    }

    private fun validateUserInput(): Boolean {
        var isValid = true

        if (firstNameTv.text.isNullOrEmpty()) {
            isValid = false
            firstNameTv.error = "required"
        }

        if (genderSpinner.selectedItemPosition == 0) {
            isValid = false
            ToastUtil.showLongToast(this@FilterPatientActivity, "Select Gender")
        }

        val day = dayTv.text.toString()
        val month = monthTv.text.toString()
        val year = yearTv.text.toString()

        if (!((day.isNotEmpty() && month.isNotEmpty() && year.isNotEmpty()) || (day.isEmpty() && month.isEmpty() && year.isEmpty()))) {
            isValid = false
            ToastUtil.showLongToast(this@FilterPatientActivity, "Type valid DoB")
        }

        if (day.isNotEmpty() && (day.length < 2 || day.toInt() > 31)) {
            isValid = false
            dayTv.error = "type valid day"
        }

        if (month.isNotEmpty() && (month.length < 2 || month.toInt() > 12)) {
            isValid = false
            monthTv.error = "type valid month"
        }

        if (year.isNotEmpty() && year.length < 4) {
            isValid = false
            monthTv.error = "type valid year"
        }

        return isValid
    }
    private fun doFilterLocal(
        firstName: String,
        lastName: String,
        gender: String,
        phone: String,
        dob: String
    ) {
        getSystemService(INPUT_METHOD_SERVICE)?.let { imm ->
            currentFocus?.let {
                (imm as InputMethodManager).hideSoftInputFromWindow(it.windowToken, 0)
            }
        }

        offset = 0
        isFullyLoaded = false
        initRecyclerScrollListener(firstName, lastName,  phone, gender,dob)

        subscriptions.add(
            Observable.fromCallable {
            }
                .concatMap {
                    Observable.just(
                        PatientsDAO.getFilteredPatients1(
                            firstName,
                            lastName,
                            gender,
                            phone,
                            dob,
                            offset,
                            defaultPageSize
                        )
                    )
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { loadingDialog.show() }
                .doOnTerminate { loadingDialog.dismiss() }
                .subscribe(
                    { updatePatientsAdapter(it) },
                    {
                        ToastUtil.showLongToast(
                            this@FilterPatientActivity,
                            "Error finding patients"
                        )
                    }
                )
        )
    }
    private fun doFilter(
        firstName: String,
        lastName: String,
        phone: String,
        gender: String,
        dob: String
    ) {

        offset = 0
        isFullyLoaded = false

        subscriptions.add(

            Single.fromCallable {
                PatientsDAO.getFilteredPatients(
                    firstName,
                    lastName,
                    phone,
                    offset,
                    defaultPageSize
                )
            }.flatMap { localList ->

                Single.zip(
                    fetchOpenMRSPatients(firstName, lastName, phone, 0),
                    fetchFhirPatients(firstName, lastName, phone, 0),
                    { remoteList: List<PatientSearchResult>,
                      fhirList: List<PatientSearchResult> ->

                        val finalList = mutableListOf<PatientSearchResult>()

                        finalList.addAll(localList)
                        finalList.addAll(remoteList)
                        finalList.addAll(fhirList)

                        finalList
                    }
                )
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { loadingDialog.show() }
                .doFinally { loadingDialog.dismiss() }
                .subscribe(
                    { finalList ->

                        val sortedList = finalList
                            .distinctBy { it.patient?.uuid ?: it.patient?.phonenumber }
                            .sortedByDescending { it.score }
                            .toMutableList()

                        updatePatientsAdapter(sortedList)
                    },
                    { error ->
                        Log.e("FILTER_ERROR", error.message ?: "Unknown Error")
                        ToastUtil.showLongToast(
                            this@FilterPatientActivity,
                            "Error finding patients"
                        )
                    }
                )
        )
    }
    private fun fetchOpenMRSPatients(
        firstName: String,
        lastName: String,
        phone: String,
        pageNo: Int
    ): Single<List<PatientSearchResult>> {

        return searchPatientOpenMRSObservable(
            firstName,
            lastName,
            phone,
            pageNo
        ).map { body ->

            val resultList = ArrayList<PatientSearchResult>()

            val entries = body.entry ?: emptyList()

            for (item in entries) {

                val resource = item.resource

                val name = resource?.name?.firstOrNull()
                val id = resource?.id
                val genders = resource?.gender
                val telecom = resource?.telecom?.firstOrNull()
                val phones = telecom?.value
                val dob = resource?.birthDate
                val address = resource?.address?.firstOrNull()

                val given = name?.given?.firstOrNull().orEmpty()
                val family = name?.family.orEmpty()

                val patient = PatientDTO().apply {
                    uuid = id
                    firstname = given
                    lastname = family
                    gender = genders ?: ""
                    phonenumber = phones ?: ""
                    dateofbirth = dob ?: ""
                    address1 = address?.text ?: ""
                }

                val result = PatientSearchResult().apply {
                    this.patient = patient
                    this.source = MatchSource.OPENMRS
                    this.score = item.search?.score ?: 0.0
                    this.grade = MatchGrade.CERTAIN
                    this.localDbResult = false
                }

                resultList.add(result)
            }

            resultList
        }
    }
    private fun searchPatientOpenMRSObservable(
        firstName: String,
        lastName: String,
        phone: String,
        pageNo: Int
    ): Single<PatientSearchDTO> {

        Log.e("TEST", "FUNCTION ENTERED");
        val offset = pageNo * defaultPageSize

        val jsonObject = JSONObject()

        val parameterArray = JSONArray()

        // resource parameter
        val resourceObject = JSONObject()
        resourceObject.put("resourceType", "Patient")

        // name
        val nameArray = JSONArray()
        val nameObject = JSONObject()

        if (lastName.isNotEmpty()) {
            nameObject.put("family", lastName)
        }

        val givenArray = JSONArray()
        givenArray.put(firstName)

        nameObject.put("given", givenArray)
        nameArray.put(nameObject)

        resourceObject.put("name", nameArray)

       /* // gender
        if (gender.isNotEmpty()) {
            resourceObject.put("gender", gender.lowercase())
        }

        // dob
        if (dob.isNotEmpty()) {
            resourceObject.put("birthDate", dob)
        }*/

        // telecom
        val telecomArray = JSONArray()
        val telecomObject = JSONObject()
        telecomObject.put("system", "phone")
        telecomObject.put("value", phone)

        telecomArray.put(telecomObject)
        resourceObject.put("telecom", telecomArray)

        val resourceParam = JSONObject()
        resourceParam.put("name", "resource")
        resourceParam.put("resource", resourceObject)

        parameterArray.put(resourceParam)

        // resourceType
        val resourceTypeParam = JSONObject()
        resourceTypeParam.put("name", "resourceType")
        resourceTypeParam.put("valueString", "Patient")

        parameterArray.put(resourceTypeParam)

        // count
        val countParam = JSONObject()
        countParam.put("name", "count")
        countParam.put("valueInteger", defaultPageSize)

        parameterArray.put(countParam)

        // offset
        val offsetParam = JSONObject()
        offsetParam.put("name", "offset")
        offsetParam.put("valueInteger", offset)

        parameterArray.put(offsetParam)

        // onlyCertainMatches
        val matchParam = JSONObject()
        matchParam.put("name", "onlyCertainMatches")
        matchParam.put("valueBoolean", false)

        parameterArray.put(matchParam)

        jsonObject.put("resourceType", "Parameters")
        jsonObject.put("parameter", parameterArray)

        val requestBody = jsonObject.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        val auth = Credentials.basic("admin", "apple@1Mango")

        return AppConstants.apiInterface.searchPatientOpenMRS(
            auth,
            requestBody
        )
    }

    private fun fetchFhirPatients(
        firstName: String,
        lastName: String,
        phone: String,
        pageNo: Int
    ): Single<List<PatientSearchResult>> {

        return searchPatientFhirObservable(firstName, lastName, phone, pageNo)
            .map { body ->

                val resultList = ArrayList<PatientSearchResult>()

                val entries = body.entry ?: emptyList()

                for (item in entries) {

                    val resource = item.resource ?: continue

                    val name = resource.name?.firstOrNull()
                    val given = name?.given?.firstOrNull().orEmpty()
                    val family = name?.family.orEmpty()

                    val patient = PatientDTO().apply {
                        uuid = resource.id
                        firstname = given
                        lastname = family
                        gender = resource.gender ?: ""
                        phonenumber = resource.telecom?.firstOrNull()?.value ?: ""
                        dateofbirth = resource.birthDate ?: ""
                        address1 = resource.address?.firstOrNull()?.text ?: ""
                    }

                    resultList.add(
                        PatientSearchResult().apply {
                            this.patient = patient
                            this.source = MatchSource.FHIR
                            this.score = item.search?.score ?: 0.0
                            this.grade = MatchGrade.CERTAIN
                            this.localDbResult = false
                        }
                    )
                }

                resultList.toList()   // 🔥 IMPORTANT FIX
            }
            .subscribeOn(Schedulers.io())
    }
    private fun searchPatientFhirObservable(
        firstName: String,
        lastName: String,
        phone: String,
        pageNo: Int
    ): Single<PatientSearchDTO> {

        Log.e("TEST", "FUNCTION ENTERED");
        val offset = pageNo * defaultPageSize

        val jsonObject = JSONObject()

        val parameterArray = JSONArray()

        // resource parameter
        val resourceObject = JSONObject()
        resourceObject.put("resourceType", "Patient")

        // name
        val nameArray = JSONArray()
        val nameObject = JSONObject()

        if (lastName.isNotEmpty()) {
            nameObject.put("family", lastName)
        }

        val givenArray = JSONArray()
        givenArray.put(firstName)
        if (firstName.isNotEmpty()) {

        nameObject.put("given", givenArray)}
        nameArray.put(nameObject)

        resourceObject.put("name", nameArray)


        // telecom
        val telecomArray = JSONArray()
        val telecomObject = JSONObject()
        if (phone.isNotEmpty()) {
        telecomObject.put("system", "phone")
        telecomObject.put("value", phone)

        telecomArray.put(telecomObject)
        resourceObject.put("telecom", telecomArray)}

        val resourceParam = JSONObject()
        resourceParam.put("name", "resource")
        resourceParam.put("resource", resourceObject)

        parameterArray.put(resourceParam)

        // resourceType
        val resourceTypeParam = JSONObject()
        resourceTypeParam.put("name", "resourceType")
        resourceTypeParam.put("valueString", "Patient")

        parameterArray.put(resourceTypeParam)

        // count
        val countParam = JSONObject()
        countParam.put("name", "count")
        countParam.put("valueInteger", defaultPageSize)

        parameterArray.put(countParam)

        // offset
        val offsetParam = JSONObject()
        offsetParam.put("name", "offset")
        offsetParam.put("valueInteger", offset)

        parameterArray.put(offsetParam)

        // onlyCertainMatches
        val matchParam = JSONObject()
        matchParam.put("name", "onlyCertainMatches")
        matchParam.put("valueBoolean", false)

        parameterArray.put(matchParam)

        jsonObject.put("resourceType", "Parameters")
        jsonObject.put("parameter", parameterArray)
        // %24 = $ encoding
        val urlfhir="http://192.168.19.152:6001/fhir/\$mdm-match"


        val requestBody = jsonObject.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        val auth = Credentials.basic("fhir_app", "Admin123")
        return AppConstants.apiInterface.searchPatientFhir(
            urlfhir,
            auth,
            requestBody
        )
    }

    private fun initRecyclerScrollListener(
        firstName: String,
        lastName: String,
        phone: String,
        gender: String,
        dob: String
    ) {
        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (isFullyLoaded) {
                    return
                }

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager

                if (layoutManager.findLastVisibleItemPosition() == patientAdapter.itemCount - 1) {
                    Toast.makeText(
                        this@FilterPatientActivity,
                        R.string.loading_more,
                        Toast.LENGTH_SHORT
                    ).show()

                    offset += defaultPageSize
                    loadMorePatientsAndUpdateAdapter(firstName, lastName, phone)
                }
            }
        }

        filterRecyclerView.removeOnScrollListener(scrollListener)
        filterRecyclerView.addOnScrollListener(scrollListener)
    }

    private fun loadMorePatientsAndUpdateAdapter(
        firstName: String,
        lastName: String,

        phone: String,

    ) {
        isDataLoading = true
        val patients = PatientsDAO.getFilteredPatients(
            firstName,
            lastName,

            phone,

            offset,
            defaultPageSize
        )

        if (patients.size < defaultPageSize) {
            isFullyLoaded = true
        }

        patientAdapter.addMorePatients(patients)
        isDataLoading = false
    }

    private fun updatePatientsAdapter(patients: MutableList<PatientSearchResult>) {
        if (patients.isNotEmpty()) {
            selectedPatient = null
            //goWithSelectedButton.isEnabled = false

            patientAdapter.updatePatientList(patients)
            filterFailedLayout.visibility = View.GONE
            filterSuccessLayout.visibility = View.VISIBLE
            //filterSuccessActionLayout.visibility = View.VISIBLE
        } else {
            //filterSuccessActionLayout.visibility = View.GONE
            filterSuccessLayout.visibility = View.GONE
            filterFailedLayout.visibility = View.VISIBLE
        }
    }

    private fun goToCreateNewPatient() {
        PatientRegistrationActivity.startPatientRegistration(
            this,
            firstName = currentFirstName,
            lastName = currentLastName,
            gender = 1,
            phone = currentPhone,
            dob = currentDob
        )
        /*Intent(this@FilterPatientActivity, PrivacyPolicyActivity_New::class.java).apply {
            putExtra("intentType", "navigateFurther")
            putExtra("add_patient", "add_patient")
            putExtra(BundleKeys.FIRST_NAME, firstNameTv.text.toString())
            putExtra(BundleKeys.LAST_NAME, lastNameTv.text.toString())
            putExtra(BundleKeys.PHONE, phoneTv.text.toString())
            putExtra(BundleKeys.GENDER, currentGender)
            putExtra(BundleKeys.DOB, currentDob)
            startActivity(this)
        }*/
    }

    private fun goToPatientDetails() {
        selectedPatient?.let { patient ->
            Intent(this@FilterPatientActivity, PatientDetailActivity2::class.java).apply {
                putExtra("patientUuid", patient.uuid)
                putExtra("patientName", patient.firstname + " " + patient.lastname)
                putExtra("tag", "searchPatient")
                putExtra("hasPrescription", "false")
                putExtra("BUNDLE", Bundle().apply { putSerializable("patientDTO", patient) })

                startActivity(this)
            }
        }
    }
}
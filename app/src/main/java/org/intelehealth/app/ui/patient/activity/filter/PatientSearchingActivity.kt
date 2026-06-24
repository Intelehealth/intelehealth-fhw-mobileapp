package org.intelehealth.app.ui.patient.activity.filter

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.R
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.models.MatchGrade
import org.intelehealth.app.models.MatchSource
import org.intelehealth.app.models.PatientSearchResult
import org.intelehealth.app.models.dto.Extension
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.models.dto.PatientSearchDTO
import org.intelehealth.app.ui2.utils.CheckInternetAvailability
import org.intelehealth.app.utilities.BundleKeys
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class PatientSearchingActivity : AppCompatActivity() {

    private lateinit var progressBarIH: ProgressBar
    private lateinit var progressBarNational: ProgressBar
    private lateinit var nameTv: TextView
    private lateinit var infoTv: TextView
    private lateinit var ihNetworkTv: TextView
    private lateinit var totalNetworkTv: TextView
    private lateinit var nrNetworkTv: TextView

    private var isIHApiDone = false
    private var isNationalApiDone = false
    private var isLocalDone = false
    private var isFhirDone = false
    private var isFhirEnabled = false
    private var patientDTO: PatientDTO? = null
    private val defaultPageSize: Int = 50
    private lateinit var loadingDialog: AlertDialog
    private var filteredPatientList = mutableListOf<PatientSearchResult>()
    private var filteredPatientFhirList = mutableListOf<PatientSearchResult>()
    private var filteredPatientFinalList = mutableListOf<PatientSearchResult>()
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_searching)

        progressBarIH = findViewById(R.id.progressBarIH)
        progressBarNational = findViewById(R.id.progressBarNational)
        nameTv = findViewById(R.id.nameTv)
        infoTv = findViewById(R.id.infoTv)
        ihNetworkTv = findViewById(R.id.ihNetworkTv)
        totalNetworkTv = findViewById(R.id.totalTv)
        nrNetworkTv = findViewById(R.id.nrNetworkTv)
        isFhirEnabled = intent.getBooleanExtra("isFhir",false)
        patientDTO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(
                "patientDTO",
                PatientDTO::class.java
            )
        } else {
            intent.getSerializableExtra("patientDTO") as? PatientDTO
        }

        val name ="${patientDTO?.firstname} ${patientDTO?.middlename} ${patientDTO?.lastname}"
        val gender = patientDTO?.gender
        val dob =patientDTO?.dateofbirth;
        val  phone=patientDTO?.phonenumber
        nameTv.text=name
        infoTv.text = "$gender . DOB $dob. $phone"

        findViewById<ImageView>(R.id.iv_back_arrow)?.setOnClickListener {
            finish()
        }
        callIHNetworkApi()
        callNationalRegistryApi()

    }
    private fun callIHNetworkApi() {

        progressBarIH.visibility = View.VISIBLE
        isIHApiDone = false

        doFilterLocal(
            patientDTO?.firstname ?: "",
            patientDTO?.lastname ?: "",
            patientDTO?.gender ?: "",
            patientDTO?.phonenumber ?: "",
            patientDTO?.dateofbirth ?: ""
        )
    }
    private fun getFullGenderStr(gender: String): String {
        return when (gender.lowercase(Locale.getDefault())) {
            "f" -> "Female"
            "m" -> "Male"
            else -> "Other"
        }
    }
    private fun doFilterFhir(
        firstName: String,
        lastName: String,
        gender: String,
        phone: String,
        dob: String
    ) {
        if (!CheckInternetAvailability.isNetworkAvailable(this)) {

            progressBarNational.visibility = View.GONE

            filteredPatientFhirList.clear()

            isFhirDone = true

            nrNetworkTv.text = "No internet connection"

            checkAndMerge()

            return
        }

        subscriptions.add(
            fetchFhirPatients(firstName, lastName, phone, getFullGenderStr(gender), dob, 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->

                    progressBarNational.visibility = View.GONE  //  STOP

                    filteredPatientFhirList.clear()
                    filteredPatientFhirList.addAll(list)

                    isFhirDone = true

                    nrNetworkTv.text =
                        if (list.isEmpty()) "No match found"
                        else "Matches found"

                    checkAndMerge()

                }, {

                    progressBarNational.visibility = View.GONE

                    isFhirDone = true
                    nrNetworkTv.text = "No match found"

                    checkAndMerge()
                })
        )
    }

    private fun checkAndMerge() {

        if (isLocalDone && isFhirDone) {

            mergeFinalPatientList()

            loadingDialog.dismiss()
        }
    }
    private fun mergeFinalPatientList() {

        val map = LinkedHashMap<String, PatientSearchResult>()

        var nullIdCounter = 0

        fun generateKey(item: PatientSearchResult): String {
            return item.patient?.uuid ?: "NULL_${nullIdCounter++}"
        }

        // =========================
        // LOCAL FIRST
        // =========================
        filteredPatientList.forEach { local ->

            val key = local.patient?.uuid ?: generateKey(local)

            map[key] = local
        }

        // =========================
        // FHIR MERGE (REMOTE PRIORITY)
        // =========================
        filteredPatientFhirList.forEach { fhir ->

            val key = fhir.patient?.uuid ?: generateKey(fhir)

            val local = map[key]

            map[key] = if (local != null) {
                val mergedPatient = fhir.patient?.apply {

                    // =========================
                    // LOCAL ID ALWAYS PRIORITY
                    // =========================
                    uuid = local.patient?.uuid ?: uuid
                    openmrsId = local.patient?.openmrsId ?: openmrsId
                    sourceId = local.patient?.sourceId ?: sourceId
                    mpiId = local.patient?.mpiId ?: mpiId

                }
                // SAME PATIENT → MERGE
                fhir.copy(
                    patient = mergedPatient,// REMOTE PRIORITY DATA

                    // LOCAL DATA KEEP
                    isIHNetwork = local.isIHNetwork,
                    ihscore = local.ihscore,

                    // FHIR DATA KEEP
                    isNRNetwork = fhir.isNRNetwork,
                    nrscore = fhir.nrscore
                )

            } else {
                // NEW PATIENT
                fhir
            }
        }

        // =========================
        // FINAL LIST
        // =========================
        val sortedList = map.values
            .distinctBy { it.patient?.uuid ?: it.patient?.phonenumber }
            .sortedByDescending { it.score }

        filteredPatientFinalList.clear()
        filteredPatientFinalList.addAll(sortedList)

        totalNetworkTv.visibility = View.VISIBLE
        totalNetworkTv.text = "${filteredPatientFinalList.size} matches found"

        checkAllApisCompleted()
    }
    private fun fetchFhirPatients(
        firstName: String,
        lastName: String,
        phone: String,
        genders: String,
        dob: String,
        pageNo: Int
    ): Single<List<PatientSearchResult>> {

        return searchPatientFhirObservable(firstName, lastName, phone,genders,dob, pageNo)
            .map { body ->

                val resultList = ArrayList<PatientSearchResult>()

                val entries = body.entry ?: emptyList()

                for (item in entries) {

                    val resource = item.resource ?: continue
                    //  Skip GOLDEN_RECORD patients
                    val isGoldenRecord = resource.meta?.tag?.any { tag ->
                        tag.code == "${BundleKeys.GOLDEN_RECORD}"
                    } == true

                    if (isGoldenRecord) {
                        continue
                    }

                    val name = resource.name?.firstOrNull()
                    val given = name?.given?.firstOrNull().orEmpty()
                    val family = name?.family.orEmpty()

                    val genders = resource?.gender
                    val telecom = resource?.telecom?.firstOrNull()
                    val phones = telecom?.value
                    val dob = resource?.birthDate
                    val address = resource?.address?.firstOrNull()
                    val extension = resource?.extension

                    val openMrsId = resource?.identifier
                        ?.firstOrNull {
                            it.use == "official" ||
                                    it.system?.contains("OpenMRS-ID", ignoreCase = true) == true
                        }
                        ?.value
                    val patient = PatientDTO().apply {
                        uuid = resource.identifier
                            ?.firstOrNull { identifier ->
                                !identifier.system.isNullOrEmpty() &&
                                        !identifier.value.isNullOrEmpty() &&
                                        identifier.id == null &&
                                        identifier.use == null
                            }
                            ?.value
                        sourceId=resource.id
                        openmrsId =openMrsId
                        firstname = given
                        lastname = family
                        gender = resource.gender ?: ""
                        phonenumber = resource.telecom?.firstOrNull()?.value ?: ""
                        dateofbirth = resource.birthDate ?: ""

                        firstname = given
                        lastname = family
                        gender = genders ?: ""
                        phonenumber = phones ?: ""
                        dateofbirth = dob ?: ""
                        country=address?.country?:""
                        postalcode=address?.postalCode?:""
                        stateprovince=address?.state
                        city=address?.city
                        address1=address?.use
                        contactType =getExtensionValueByEndPoint(extension,"Emergency-Contact-Type")
                        education =getExtensionValueByEndPoint(extension,"Education-Level")
                        economic =getExtensionValueByEndPoint(extension,"Economic-Status")
                        caste =getExtensionValueByEndPoint(extension,"Caste")
                        syncd=true
                    }

                    resultList.add(
                        PatientSearchResult().apply {
                            this.patient = patient
                            this.source = MatchSource.FHIR
                            this.score = item.search?.score ?: 0.0
                            this.grade = MatchGrade.CERTAIN
                            this.localDbResult = false
                            this.isNRNetwork=true
                            this.nrscore=item.search?.score ?: 0.0
                        }
                    )
                }

                resultList.toList()   //  IMPORTANT FIX
            }
            .subscribeOn(Schedulers.io())
    }
    private fun searchPatientFhirObservable(
        firstName: String,
        lastName: String,
        phone: String,
        gender: String,
        dob: String,
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
        // gender
        if (gender.isNotEmpty()) {
            resourceObject.put("gender", gender.lowercase())
        }

        // dob
        if (dob.isNotEmpty()) {
            resourceObject.put("birthDate", dob)
        }

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
        val url = BuildConfig.FHIR_URL+"/\$mdm-match"

        val requestBody = jsonObject.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        val auth = Credentials.basic("fhir_app", "Admin123")
        return AppConstants.apiInterface.searchPatientFhir(
            url,
            auth,
            requestBody
        )
    }
    private fun doFilterLocal(
        firstName: String,
        lastName: String,
        gender: String,
        phone: String,
        dob: String
    ) {

        subscriptions.add(
            Single.fromCallable {
                PatientsDAO.getFilteredPatients1(
                    firstName,
                    lastName,
                    gender,
                    phone,
                    dob,
                    0,
                    defaultPageSize
                )
            }
                .subscribeOn(Schedulers.io())
                .flatMap { localList ->

                    val isNetwork = CheckInternetAvailability.isNetworkAvailable(this)

                    if (!isNetwork) {
                        Single.just(localList)
                    } else {
                        fetchOpenMRSPatients(
                            firstName, lastName, phone,
                            getFullGenderStr(gender), dob, 0
                        )
                            .map { remote ->
                                mergePatientList(localList, remote)
                            }
                            .onErrorReturn { localList }
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ patientList ->

                    progressBarIH.visibility = View.GONE   //  STOP LOADING

                    filteredPatientList.clear()
                    filteredPatientList.addAll(patientList)

                    isLocalDone = true

                    ihNetworkTv.text =
                        if (patientList.isEmpty()) "No match found"
                        else "Matches found"

                    checkAndMerge()

                }, {

                    progressBarIH.visibility = View.GONE

                    isLocalDone = true
                    ihNetworkTv.text = "No match found"

                    checkAndMerge()
                })
        )
    }
    private fun mergePatientList(
        localList: List<PatientSearchResult>,
        remoteList: List<PatientSearchResult>
    ): List<PatientSearchResult> {

        val map = LinkedHashMap<String, PatientSearchResult>()

        // LOCAL FIRST
        localList.forEach { item ->
            val uuid = item.patient?.uuid
            if (!uuid.isNullOrEmpty()) {
                map[uuid] = item
            }
        }

        // REMOTE OVERRIDE
        remoteList.forEach { item ->
            val uuid = item.patient?.uuid
            if (!uuid.isNullOrEmpty()) {
                map[uuid] = item
            }
        }

        return map.values.toList()
    }
    private fun fetchOpenMRSPatients(
        firstName: String,
        lastName: String,
        phone: String,
        genderss: String,
        dob: String,
        pageNo: Int
    ): Single<List<PatientSearchResult>> {

        return searchPatientOpenMRSObservable(
            firstName,
            lastName,
            phone,
            genderss,
            dob,
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
                val extension = resource?.extension
                val openMrsId = resource?.identifier
                    ?.firstOrNull {
                        it.use == "official" ||
                                it.system?.contains("OpenMRS-ID", ignoreCase = true) == true
                    }
                    ?.value

                val patient = PatientDTO().apply {
                    uuid = id
                    firstname = given
                    lastname = family
                    gender = genders ?: ""
                    phonenumber = phones ?: ""
                    dateofbirth = dob ?: ""
                    country=address?.country?:""
                    postalcode=address?.postalCode?:""
                    stateprovince=address?.state
                    city=address?.city
                    openmrsId =openMrsId
                    address1=address?.line.toString()
                    contactType =getExtensionValueByEndPoint(extension,"Emergency-Contact-Type")
                    education =getExtensionValueByEndPoint(extension,"Education-Level")
                    economic =getExtensionValueByEndPoint(extension,"Economic-Status")
                    caste =getExtensionValueByEndPoint(extension,"Caste")
                    syncd=false
                }

                val result = PatientSearchResult().apply {
                    this.patient = patient
                    this.source = MatchSource.OPENMRS
                    this.score = item.search?.score ?: 0.0
                    this.grade = MatchGrade.CERTAIN
                    this.localDbResult = false
                    this.isIHNetwork=true
                    this.ihscore = item.search?.score ?: 0.0
                }

                resultList.add(result)
            }

            resultList
        }
    }
    fun getExtensionValueByEndPoint(
        extensions: List<Extension>?,
        endPoint: String
    ): String? {

        if (extensions.isNullOrEmpty()) return null

        return extensions.firstOrNull { ext ->

            val url = ext.url ?: return@firstOrNull false

            val lastSegment = url.substringAfterLast("/")

            lastSegment.equals(endPoint, ignoreCase = true)

        }?.valueString
    }
    private fun searchPatientOpenMRSObservable(
        firstName: String,
        lastName: String,
        phone: String,
        gender: String,
        dob: String,
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

         // gender
         if (gender.isNotEmpty()) {
             resourceObject.put("gender", gender.lowercase())
         }

         // dob
         if (dob.isNotEmpty()) {
             resourceObject.put("birthDate", dob)
         }

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

        val url = (BuildConfig.SERVER_URL + "/openmrs/ws/rest/v1/ihmodule/patient/\$match")

        return AppConstants.apiInterface.searchPatientOpenMRS(
            url,
            auth,
            requestBody
        )
    }
    private fun callNationalRegistryApi() {

        progressBarNational.visibility = View.VISIBLE
        isNationalApiDone = false

        doFilterFhir(
            patientDTO?.firstname ?: "",
            patientDTO?.lastname ?: "",
            patientDTO?.gender ?: "",
            patientDTO?.phonenumber ?: "",
            patientDTO?.dateofbirth ?: ""
        )}
    private fun checkAllApisCompleted() {

        if (isLocalDone && isFhirDone) {

            val intent = Intent(
                this,
                MatchResultActivity::class.java
            )

            intent.putExtra("patientDTO", patientDTO)
            intent.putExtra("isFhir",isFhirEnabled)
            intent.putExtra(
                "finalPatientList",
                ArrayList(filteredPatientFinalList)
            )

            startActivity(intent)
            finish()
        }
    }

}
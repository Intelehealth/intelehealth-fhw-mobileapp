package org.intelehealth.app.ui.patient.activity.filter

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.app.R
import org.intelehealth.app.activities.filterPatientActivity.FilterResultAdapter
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2
import org.intelehealth.app.models.PatientSearchResult
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.shared.BaseActivity
import org.intelehealth.app.ui.patient.activity.PatientRegistrationActivity.Companion.startPatientRegistration
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.NetworkConnection
import org.intelehealth.app.utilities.NetworkUtils
import org.intelehealth.app.utilities.PatientRegStage
import org.intelehealth.app.utilities.ToastUtil
import org.intelehealth.config.presenter.fields.factory.PatientViewModelFactory

class MatchResultActivity : BaseActivity() , NetworkUtils.InternetCheckUpdateInterface, FilterResultAdapter.AdapterClickListener{
    private var patientDTO= PatientDTO()

    private lateinit var filterRecyclerView: RecyclerView
    private lateinit var loadingDialog: AlertDialog
    private lateinit var filterSuccessLayout: LinearLayout
    private var patientList = mutableListOf<PatientSearchResult>()
    private lateinit var patientAdapter: FilterResultAdapter
    private lateinit var noneMatch: TextView
    private lateinit var matchFound: TextView
    private lateinit var emptyLayout: LinearLayout
    private lateinit var refresh: ImageView
    private lateinit var syncAnimator: ObjectAnimator
    private lateinit var networkUtils: NetworkUtils
    private var isFhirEnabled = false

    protected val patientViewModel by lazy {
        return@lazy PatientViewModelFactory.create(this, this)
    }
   // private val patientAdapter = FilterResultAdapter(patientList, this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_match_result)

        patientDTO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(
                "patientDTO",
                PatientDTO::class.java
            )
        } else {
            intent.getSerializableExtra("patientDTO") as? PatientDTO
        }?: PatientDTO()

       intent.getParcelableArrayListExtra<PatientSearchResult>("finalPatientList")
           ?.let {
               patientList = it.toMutableList()
           }

       isFhirEnabled = intent.getBooleanExtra("isFhir",false)
       filterRecyclerView = findViewById(R.id.filter_patient_container)
       noneMatch = findViewById(R.id.noneMatch)
       matchFound = findViewById(R.id.match_foundTv)
       refresh = findViewById(R.id.refresh)
       emptyLayout = findViewById(R.id.filter_patient_failed_ll)
       filterRecyclerView.layoutManager =
           LinearLayoutManager(this@MatchResultActivity)
       filterRecyclerView.setHasFixedSize(true)
       filterRecyclerView.isNestedScrollingEnabled = false
       patientAdapter =
           FilterResultAdapter(isFhirEnabled,patientList, this)

       filterRecyclerView.adapter = patientAdapter
       networkUtils = NetworkUtils(this, this)
       if(patientList.isEmpty()){
           matchFound.visibility= View.GONE
       }
       // initial UI state
       updateUIForInternetAvailability(networkUtils.isNetworkAvailable(this))

        filterSuccessLayout = findViewById(R.id.filter_patient_success_ll)

       patientAdapter.notifyDataSetChanged()
       filterSuccessLayout =
           findViewById(R.id.filter_patient_success_ll)

       // changing status bar color
       window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
       window.statusBarColor = Color.WHITE
        loadingDialog = DialogUtils().showCommonLoadingDialog(
            this@MatchResultActivity,
            getString(R.string.loading),
            getString(R.string.please_wait),
        ).apply {
            dismiss()
        }

        findViewById<ImageView>(R.id.iv_back_arrow)?.setOnClickListener {
            finish()
        }
       noneMatch.setOnClickListener {
           patientViewModel.updatedPatient(patientDTO)
           if (patientViewModel.activeStatusAddressSection) {
               startPatientRegistration(
                   this,
                   null,
                   PatientRegStage.ADDRESS,
                   patientDTO.firstname,
                   patientDTO.lastname,
                   getGenderCode(patientDTO.gender),
                   patientDTO.phonenumber,
                   patientDTO.dateofbirth,
                   patientDTO.contactType,
                   patientDTO.emContactNumber
               )
               //finish()
           } else if (patientViewModel.activeStatusOtherSection) {
               startPatientRegistration(
                   this,
                   null,
                   PatientRegStage.OTHER,
                   patientDTO.firstname,
                   patientDTO.lastname,
                   getGenderCode(patientDTO.gender),
                   patientDTO.phonenumber,
                   patientDTO.dateofbirth
               )
               //finish()
           } else {
               saveAndNavigateToDetails()
           }
       }
    }
    fun getGenderCode(gender: String?): Int {
        return when (gender?.uppercase()) {
            "M" -> 1
            "F" -> 2
            "O" -> 3
            else -> 0 // unknown / null case
        }
    }
    private fun saveAndNavigateToDetails() {
        patientViewModel.updatedPatient(patientDTO)
        patientViewModel.savePatient().observe(this) {
            it ?: return@observe
            patientViewModel.handleResponse(it) { result -> if (result) navigateToDetails(patientDTO) }
        }
    }
    override fun onItemClick(selectedItem: Any) {
        if (selectedItem is PatientDTO) {

            val finalPatient = mergePatient(selectedItem, patientDTO)
           //Toast.makeText(this, "Check Final"+finalPatient.dateofbirth, Toast.LENGTH_SHORT).show()
            patientViewModel.updatedPatient(finalPatient)
            patientViewModel.savePatient().observe(this) {
                //Toast.makeText(this, "Check Final"+it.status, Toast.LENGTH_SHORT).show()
                it ?: return@observe

                patientViewModel.handleResponse(it) { result ->

                    if (result) {
                        navigateToDetails(finalPatient)
                    }

                }

            }

        } else {
            ToastUtil.showShortToast(this, "Failed to select patient")
        }
    }
    private fun mergePatient(
        primary: PatientDTO,
        fallback: PatientDTO
    ): PatientDTO {

        val result = PatientDTO()

        // IDs
        result.uuid = primary.uuid ?: fallback.uuid
        result.openmrsId = primary.openmrsId ?: fallback.openmrsId
        result.sourceId = primary.sourceId ?: fallback.sourceId
        result.mpiId = primary.mpiId ?: fallback.mpiId

        // Name
        result.firstname = primary.firstname ?: fallback.firstname
        result.middlename = primary.middlename ?: fallback.middlename
        result.lastname = primary.lastname ?: fallback.lastname

        // Basic info
        result.dateofbirth = primary.dateofbirth ?: fallback.dateofbirth
        result.gender = primary.gender ?: fallback.gender
        result.phonenumber = primary.phonenumber ?: fallback.phonenumber

        // Address
        result.address1 = primary.address1 ?: fallback.address1
        result.address2 = primary.address2 ?: fallback.address2
        result.address3 = primary.address3 ?: fallback.address3
        result.address6 = primary.address6?:fallback.address6
        result.registrationAddressOfHf = primary.registrationAddressOfHf?:fallback.registrationAddressOfHf
        result.cityvillage = primary.cityvillage ?: fallback.cityvillage
        result.stateprovince = primary.stateprovince ?: fallback.stateprovince
        result.postalcode = primary.postalcode ?: fallback.postalcode
        result.country = primary.country ?: fallback.country
        result.district = primary.district ?: fallback.district
        // Social / extra info
        result.education = primary.education ?: fallback.education
        result.economic = primary.economic ?: fallback.economic
        result.caste = primary.caste ?: fallback.caste
        result.occupation = primary.occupation ?: fallback.occupation
        result.nationalID = primary.nationalID ?: fallback.nationalID
        result.tmhCaseNumber=primary.tmhCaseNumber?:fallback.tmhCaseNumber
        result.requestId = primary.requestId?:fallback.requestId
        result.discipline = primary.discipline?:fallback.discipline
        result.department = primary.department?:fallback.department
        result.relativePhoneNumber = primary.relativePhoneNumber?:fallback.relativePhoneNumber
        result.inn = primary.inn?:fallback.inn
        result.codeOfHealthFacility = primary.codeOfHealthFacility?:fallback.codeOfHealthFacility
        result.codeOfDepartment = primary.codeOfDepartment?:fallback.codeOfDepartment
        result.householdLinkingUUIDlinking=primary.householdLinkingUUIDlinking?:fallback.householdLinkingUUIDlinking


        // Guardian / contact
        result.guardianName = primary.guardianName ?: fallback.guardianName
        result.guardianType = primary.guardianType ?: fallback.guardianType
        result.contactType = primary.contactType ?: fallback.contactType
        result.emContactName = primary.emContactName ?: fallback.emContactName
        result.emContactNumber = primary.emContactNumber ?: fallback.emContactNumber

        // Patient metadata
        result.patientPhoto = primary.patientPhoto ?: fallback.patientPhoto
        result.patientImageFromImageDao = primary.patientImageFromImageDao ?: fallback.patientImageFromImageDao
        result.patientImageFromDownload = primary.patientImageFromDownload ?: fallback.patientImageFromDownload

        // Flags
        result.dead = primary.dead ?: fallback.dead
        result.syncd = primary.syncd ?: false

        result.setEmergency(primary.isEmergency || fallback.isEmergency)
        result.setPrescription_exists(primary.isPrescription_exists || fallback.isPrescription_exists)

        // Misc
        result.createdDate = primary.createdDate ?: fallback.createdDate
        result.createdTime = primary.createdTime ?: fallback.createdTime
        result.providerUUID = primary.providerUUID ?: fallback.providerUUID

        return result
    }
    private fun navigateToDetails(patient: PatientDTO) {
        Intent(this@MatchResultActivity, PatientDetailActivity2::class.java).apply {
            putExtra("patientUuid", patient.uuid)
            putExtra("tag", "searchPatient")
            putExtra("privacy", "false")
            putExtra("isFhir",isFhirEnabled)
            startActivity(this)
            finish()

        }
    }
    override fun onStart() {
        super.onStart()
        networkUtils.callBroadcastReceiver()
        updateUIForInternetAvailability(
            networkUtils.isNetworkAvailable(this)
        )
    }
    override fun onStop() {
        super.onStop()
        networkUtils.unregisterNetworkReceiver()
    }
    fun syncNow(view: View?) {
        if (NetworkConnection.isOnline(this)) {
            refresh.clearAnimation()
            val intent = Intent(
                this@MatchResultActivity,
                PatientSearchingActivity::class.java
            )

            intent.putExtra("patientDTO", patientDTO)
            intent.putExtra("isFhir",isFhirEnabled)

            startActivity(intent)

        }
    }
    override fun updateUIForInternetAvailability(isInternetAvailable: Boolean) {
        runOnUiThread {
            if (isInternetAvailable) {
                refresh.setImageResource(R.drawable.ui2_ic_internet_available)
            } else {
                refresh.setImageResource(R.drawable.ui2_ic_no_internet)
            }
        }
    }
}
package org.intelehealth.app.ui.patient.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.ajalt.timberkt.Timber
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.gson.Gson
import org.intelehealth.app.R
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.databinding.ActivityPatientRegistrationBinding
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.shared.BaseActivity
import org.intelehealth.app.ui.patient.adapter.PatientInfoPagerAdapter
import org.intelehealth.app.ui.patient.data.PatientRepository
import org.intelehealth.app.ui.patient.fragment.PatientAddressInfoFragment
import org.intelehealth.app.ui.patient.fragment.PatientOtherInfoFragment
import org.intelehealth.app.ui.patient.fragment.PatientPersonalInfoFragment
import org.intelehealth.app.ui.patient.viewmodel.PatientViewModel
import org.intelehealth.app.utilities.BundleKeys.Companion.PATIENT_UUID
import org.intelehealth.config.presenter.fields.factory.PatientViewModelFactory
import org.intelehealth.config.room.ConfigDatabase
import org.intelehealth.config.room.entity.FeatureActiveStatus
import java.util.LinkedList
import java.util.UUID

/**
 * Created by Vaghela Mithun R. on 27-06-2024 - 13:41.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PatientRegistrationActivity : BaseActivity() {
    private lateinit var binding: ActivityPatientRegistrationBinding
    private lateinit var pagerAdapter: PatientInfoPagerAdapter
    private val patientViewModel by lazy {
        return@lazy PatientViewModelFactory.create(this, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        manageTitleVisibilityOnScrolling()
        extractAndBindUI()
    }

    private fun extractAndBindUI() {
        Timber.d { "extractAndBindUI" }
        intent?.let {
            val patientId = if (it.hasExtra(PATIENT_UUID)) it.getStringExtra(PATIENT_UUID)
            else "623b0286-ddba-4ef5-9f40-0da37200465f"

            patientId?.let { id -> fetchPatientDetails(id) } ?: generatePatientId()
        }
    }

    private fun generatePatientId() {
        Timber.d { "generatePatientId" }
        patientViewModel.updatedPatient(PatientDTO().apply { uuid = UUID.randomUUID().toString() })
    }

    private fun fetchPatientDetails(id: String) {
        patientViewModel.loadPatientDetails(id).observe(this) {
            Timber.d { "Result => ${Gson().toJson(it)}" }
            it ?: return@observe
            patientViewModel.handleResponse(it) { patient ->
                patientViewModel.updatedPatient(patient)
            }
        }
    }

//    private fun manageTitleVisibilityOnScrolling() {
//        binding.appBarLayoutPatient.addOnOffsetChangedListener(object : OnOffsetChangedListener {
//            var scrollRange = -1;
//            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
//                if (scrollRange == -1) {
//                    scrollRange = appBarLayout?.totalScrollRange ?: -1
//                }
//
//                binding.collapsingToolbar.title = if (scrollRange + verticalOffset == 0) {
//                    resources.getString(R.string.add_new_patient)
//                } else ""
//            }
//        })
//    }

    private fun bindPagerAdapter(fragments: LinkedList<Fragment>) {
        pagerAdapter = PatientInfoPagerAdapter(supportFragmentManager, lifecycle)
        pagerAdapter.fragments = fragments
        binding.pagerPatientInfo.adapter = pagerAdapter
    }

    override fun onFeatureActiveStatusLoaded(activeStatus: FeatureActiveStatus?) {
        super.onFeatureActiveStatusLoaded(activeStatus)
        activeStatus?.let {
            patientViewModel.activeStatusAddressSection = it.activeStatusPatientAddress
            patientViewModel.activeStatusOtherSection = it.activeStatusPatientOther
            LinkedList<Fragment>().apply {
                add(PatientPersonalInfoFragment())
                if (it.activeStatusPatientAddress) add(PatientAddressInfoFragment())
                if (it.activeStatusPatientOther) add(PatientOtherInfoFragment())
            }.also { bindPagerAdapter(it) }
        }
    }

    companion object {
        fun startPatientRegistration(context: Context, patientId: String) {
            Intent(context, PatientRegistrationActivity::class.java).apply {
                putExtra(PATIENT_UUID, patientId)
            }.also { context.startActivity(it) }
        }
    }
}
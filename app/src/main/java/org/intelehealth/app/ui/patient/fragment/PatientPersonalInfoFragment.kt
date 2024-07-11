package org.intelehealth.app.ui.patient.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.ajalt.timberkt.Timber
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import org.intelehealth.app.R
import org.intelehealth.app.activities.identificationActivity.Fragment_FirstScreen
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.databinding.FragmentPatientPersonalInfoBinding
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.ui.dialog.CalendarDialog
import org.intelehealth.app.utilities.ArrayAdapterUtils
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.core.registry.PermissionRegistry
import org.intelehealth.core.registry.PermissionRegistry.Companion.CAMERA
import org.intelehealth.ihutils.ui.CameraActivity
import org.intelehealth.klivekit.utils.DateTimeUtils
import java.io.File
import java.util.Calendar

/**
 * Created by Vaghela Mithun R. on 27-06-2024 - 13:42.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PatientPersonalInfoFragment : BasePatientFragment(R.layout.fragment_patient_personal_info) {
    private lateinit var binding: FragmentPatientPersonalInfoBinding

    private val permissionRegistry by lazy {
        PermissionRegistry(requireContext(), requireActivity().activityResultRegistry)
    }

    private val sessionManager: SessionManager by lazy {
        SessionManager(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPatientPersonalInfoBinding.bind(view)
        setupGuardianType()
        setupEmContactType()
        setupDOB()
        setupProfilePicture()
//        setupErroMessageForInputType()
    }

    override fun onPatientDataLoaded(patient: PatientDTO) {
        super.onPatientDataLoaded(patient)
        Timber.d { "onPatientDataLoaded" }
        Timber.d { Gson().toJson(patient) }
        binding.patient = patient
    }

    private fun setupProfilePicture() {
        binding.patientImgview.setOnClickListener { requestPermission() }
    }

    private fun requestPermission() {
        permissionRegistry.requestPermission(CAMERA).observe(viewLifecycleOwner) {
            permissionRegistry.removePermissionObserve(viewLifecycleOwner)
            if (it[CAMERA] == true) takePicture()
            else showPermissionDeniedAlert()
        }
    }

    private fun takePicture() {
        val filePath = File(AppConstants.IMAGE_PATH + patient.uuid)
        if (!filePath.exists()) {
            filePath.mkdir()
        }

        val cameraIntent = Intent(activity, CameraActivity::class.java)
        cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, patient.uuid)
        cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, filePath.toString())
        cameraActivityResult.launch(cameraIntent)
    }

    private val cameraActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            patient.patientPhoto = result.data!!.getStringExtra("RESULT")
            binding.patient = patient
            Timber.d { "Profile path => ${patient.patientPhoto}" }
        }
    }

    private fun showPermissionDeniedAlert() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setMessage(R.string.reject_permission_results)
            setPositiveButton(R.string.retry_again) { _, _ -> setupProfilePicture() }
            setNegativeButton(R.string.ok_close_now) { _, _ -> requireActivity().finish() }
        }.show()
    }

    private fun setupDOB() {
        binding.textInputETDob.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun showDatePickerDialog() {
        CalendarDialog.Builder()
            .maxDate(Calendar.getInstance().timeInMillis)
            .format(DateTimeUtils.MMM_DD_YYYY_FORMAT)
            .listener(dateListener)
            .build().show(childFragmentManager, CalendarDialog.TAG)
    }

    private val dateListener = object : CalendarDialog.OnDatePickListener {
        override fun onDatePick(day: Int, month: Int, year: Int, value: String?) {
            binding.textInputETDob.setText(value)
        }
    }

    private fun setupErroMessageForInputType() {
        binding.textInputLayFirstName.error = getString(R.string.this_field_is_mandatory)
        binding.textInputLayMiddleName.error = getString(R.string.this_field_is_mandatory)
        binding.textInputLayLastName.error = getString(R.string.this_field_is_mandatory)
        binding.textInputLayDob.error = getString(R.string.this_field_is_mandatory)
        binding.textInputLayAge.error = getString(R.string.this_field_is_mandatory)
        binding.textInputLayGuardianType.error = getString(R.string.this_field_is_mandatory)
        binding.textInputLayGuardianName.error = getString(R.string.this_field_is_mandatory)
        binding.textInputLayEmContactType.error = getString(R.string.this_field_is_mandatory)
        binding.textInputLayEmContactName.error = getString(R.string.this_field_is_mandatory)
    }

    private fun setupGuardianType() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.guardian_type)
        binding.autoCompleteGuardianType.setAdapter(adapter)
    }

    private fun setupEmContactType() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.contact_type)
        binding.autoCompleteEmContactType.setAdapter(adapter)
    }

    private fun validateForm() {

    }
}
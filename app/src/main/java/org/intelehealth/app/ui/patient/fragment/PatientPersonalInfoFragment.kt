package org.intelehealth.app.ui.patient.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.github.ajalt.timberkt.Timber
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import org.intelehealth.app.R
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.databinding.Dialog2NumbersPickerBinding
import org.intelehealth.app.databinding.FragmentPatientPersonalInfoBinding
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.ui.dialog.CalendarDialog
import org.intelehealth.app.ui.filter.FirstLetterUpperCaseInputFilter
import org.intelehealth.app.utilities.AgeUtils
import org.intelehealth.app.utilities.ArrayAdapterUtils
import org.intelehealth.app.utilities.DateAndTimeUtils
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.extensions.addFilter
import org.intelehealth.app.utilities.extensions.hideDigitErrorOnTextChang
import org.intelehealth.app.utilities.extensions.hideErrorOnTextChang
import org.intelehealth.app.utilities.extensions.validate
import org.intelehealth.app.utilities.extensions.validateDigit
import org.intelehealth.core.registry.PermissionRegistry
import org.intelehealth.core.registry.PermissionRegistry.Companion.CAMERA
import org.intelehealth.ihutils.ui.CameraActivity
import org.intelehealth.klivekit.utils.DateTimeUtils
import org.joda.time.LocalDate
import org.joda.time.Period
import org.joda.time.PeriodType
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * Created by Vaghela Mithun R. on 27-06-2024 - 13:42.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PatientPersonalInfoFragment : BasePatientFragment(R.layout.fragment_patient_personal_info) {
    private lateinit var binding: FragmentPatientPersonalInfoBinding
    var selectedDate = Calendar.getInstance().timeInMillis
    private val permissionRegistry by lazy {
        PermissionRegistry(requireContext(), requireActivity().activityResultRegistry)
    }

    private val sessionManager: SessionManager by lazy {
        SessionManager(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPatientPersonalInfoBinding.bind(view)
    }

    private fun setupAge() {
        binding.textInputETAge.setOnClickListener {
            val dialogBinding = Dialog2NumbersPickerBinding.inflate(layoutInflater)
            MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogStyle).apply {
                setTitle(R.string.identification_screen_prompt_age)
                setView(dialogBinding.root)
                dialogBinding.dialog3NumbersUnit.visibility = View.VISIBLE
                dialogBinding.dialog2NumbersQuantity.minValue = 0
                dialogBinding.dialog2NumbersQuantity.maxValue = 100
                dialogBinding.dialog2NumbersUnit.minValue = 0
                dialogBinding.dialog2NumbersUnit.maxValue = 12
                dialogBinding.dialog3NumbersUnit.minValue = 0
                dialogBinding.dialog3NumbersUnit.maxValue = 31
                val period = getAgePeriod()
                dialogBinding.dialog2NumbersQuantity.value = period.years
                dialogBinding.dialog2NumbersUnit.value = period.months
                dialogBinding.dialog3NumbersUnit.value = period.days
            }.create().also { dialog ->

                dialog.window?.let {
                    it.setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg)
                    val width = resources.getDimensionPixelSize(R.dimen.internet_dialog_width)
                    it.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
                }

                dialogBinding.btnCancelPicker.setOnClickListener { dialog.dismiss() }
                dialogBinding.buttonOkPicker.setOnClickListener {
                    val month = dialogBinding.dialog2NumbersUnit.value
                    val year = dialogBinding.dialog2NumbersQuantity.value
                    val days = dialogBinding.dialog3NumbersUnit.value

                    bindAgeAndDobValue(year, month, days)
                    dialog.dismiss()
                }

            }.show()
        }
    }

    private fun bindAgeAndDobValue(year: Int, month: Int, days: Int) {
        bindAgeValue(year, month, days)

        Calendar.getInstance().apply {
            add(Calendar.YEAR, -year)
            add(Calendar.MONTH, -month)
            add(Calendar.DAY_OF_MONTH, -days)
        }.also { bindDobValue(it) }
    }

    private fun bindAgeValue(year: Int, month: Int, days: Int) {
        DateAndTimeUtils.formatAgeInYearsMonthsDate(
            context,
            year,
            month,
            days
        ).apply { binding.textInputETAge.setText(this) }

        updateGuardianVisibility(year, month, days)
    }

    private fun updateGuardianVisibility(year: Int, month: Int, days: Int) {
        val visibility = AgeUtils.isGuardianRequired(year, month, days)
        binding.textInputLayGuardianName.isVisible = visibility
        binding.textInputLayGuardianType.isVisible = visibility
    }

    private fun bindDobValue(calendar: Calendar) {
        selectedDate = calendar.timeInMillis
        val sdf = DateTimeUtils.getSimpleDateFormat(
            DateTimeUtils.MMM_DD_YYYY_FORMAT,
            TimeZone.getDefault()
        )
        val formattedDate = sdf.format(calendar.time)
        binding.textInputETDob.setText(formattedDate)
    }

    override fun onPatientDataLoaded(patient: PatientDTO) {
        super.onPatientDataLoaded(patient)
        Timber.d { "onPatientDataLoaded" }
        Timber.d { Gson().toJson(patient) }
        binding.patient = patient
        setupGuardianType()
        setupEmContactType()
        setupDOB()
        setupAge()
        setupProfilePicture()
        applyFilter()
        setInputTextChangListener()
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
            setPositiveButton(R.string.retry_again) { _, _ -> requestPermission() }
            setNegativeButton(R.string.ok_close_now) { _, _ -> requireActivity().finish() }
        }.show()
    }

    private fun setupDOB() {
        patient.dateofbirth?.let {
            parseDob(it, DateTimeUtils.YYYY_MM_DD_HYPHEN)

            DateTimeUtils.formatToLocalDate(
                Date(selectedDate),
                DateTimeUtils.MMM_DD_YYYY_FORMAT
            ).apply { binding.textInputETDob.setText(this) }
        }

        binding.textInputETDob.setOnClickListener {
            showDatePickerDialog(selectedDate)
        }
    }

    private fun parseDob(date: String, format: String) {
        selectedDate = DateTimeUtils.parseDate(date, format, TimeZone.getDefault()).time
        val period = getAgePeriod()
        bindAgeValue(period.years, period.months, period.days)
    }

    private fun getAgePeriod(): Period {
        val pastCalendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
        val birthdate = LocalDate(
            pastCalendar[Calendar.YEAR],
            pastCalendar[Calendar.MONTH] + 1,
            pastCalendar[Calendar.DAY_OF_MONTH]
        ) //Birth date
        val now = LocalDate() //Today's date
        return Period(birthdate, now, PeriodType.yearMonthDay())
    }

    private fun showDatePickerDialog(selectedDate: Long) {
        CalendarDialog.Builder()
            .maxDate(Calendar.getInstance().timeInMillis)
            .selectedDate(selectedDate)
            .format(DateTimeUtils.MMM_DD_YYYY_FORMAT)
            .listener(dateListener)
            .build().show(childFragmentManager, CalendarDialog.TAG)
    }

    private val dateListener = object : CalendarDialog.OnDatePickListener {
        override fun onDatePick(day: Int, month: Int, year: Int, value: String?) {
            value?.let { parseDob(it, DateTimeUtils.MMM_DD_YYYY_FORMAT) }
            binding.textInputETDob.setText(value)
        }
    }

    private fun applyFilter() {
        binding.textInputETFName.addFilter(FirstLetterUpperCaseInputFilter())
        binding.textInputETMName.addFilter(FirstLetterUpperCaseInputFilter())
        binding.textInputETLName.addFilter(FirstLetterUpperCaseInputFilter())
        binding.textInputETGuardianName.addFilter(FirstLetterUpperCaseInputFilter())
        binding.textInputETECName.addFilter(FirstLetterUpperCaseInputFilter())
    }

    private fun setInputTextChangListener() {
        binding.textInputLayFName.hideErrorOnTextChang(binding.textInputETFName)
        binding.textInputLayMName.hideErrorOnTextChang(binding.textInputETMName)
        binding.textInputLayLName.hideErrorOnTextChang(binding.textInputETLName)
        binding.textInputLayGuardianName.hideErrorOnTextChang(binding.textInputETGuardianName)
        binding.textInputLayECName.hideErrorOnTextChang(binding.textInputETECName)
        binding.textInputLayPhoneNumber.hideDigitErrorOnTextChang(
            binding.textInputETPhoneNumber,
            10
        )
        binding.textInputLayEMPhoneNumber.hideDigitErrorOnTextChang(
            binding.textInputETEMPhoneNumber,
            10
        )
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
        val error = R.string.this_field_is_mandatory
        val bFname = binding.textInputLayFName.validate(binding.textInputETFName, error)
        val bMname = binding.textInputLayFName.validate(binding.textInputETMName, error)
        val bLname = binding.textInputLayFName.validate(binding.textInputETLName, error)
        val bGname = binding.textInputLayFName.validate(binding.textInputETGuardianName, error)
        val bEmName = binding.textInputLayFName.validate(binding.textInputETECName, error)
        val bPhone = binding.textInputLayPhoneNumber.validateDigit(
            binding.textInputETPhoneNumber,
            R.string.invalid_mobile_no,
            10
        )
        val bEmPhone = binding.textInputLayEMPhoneNumber.validateDigit(
            binding.textInputETEMPhoneNumber,
            R.string.invalid_mobile_no,
            10
        )
    }
}
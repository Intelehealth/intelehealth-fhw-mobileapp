package org.intelehealth.app.activities.achievements.fragments

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.app.R
import org.intelehealth.app.activities.achievements.viewmodel.DateRangeMyAchievementsViewModel
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.databinding.LayoutDailyAchievementsFragmentBinding
import org.intelehealth.app.databinding.LayoutDateRangeAchievementsFragmentBinding
import org.intelehealth.app.utilities.DateAndTimeUtils
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.UuidDictionary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DateRangeAchievementsFragmentNew : Fragment() {
    private  val TAG = "DateRangeAchievementsFr"
    private var _binding: LayoutDateRangeAchievementsFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DateRangeMyAchievementsViewModel
    private lateinit var  sessionManager : SessionManager
    private var startDate: String =""
    private var endDate: String =""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutDateRangeAchievementsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setDefaultDates()
        initialize()
        setObservers()

    }

    private fun setObservers() {
        Log.d(TAG, "setObservers: startdate : "+startDate)
        Log.d(TAG, "setObservers: enddate : "+endDate)

        //1 Todays doctor visits
        viewModel.doctorVisitCountInDateRange.observe(viewLifecycleOwner) { count: Int ->
            binding.tvDrVisitsToday.text = count.toString()
        }
        viewModel.fetchDoctorVisitsCountInGivenDateRange(
            creatorUuid = sessionManager.creatorID,
            attributeTypeUuid = UuidDictionary.IS_NCD_VISIT_ATTRIBUTE,
            startDate = startDate,
            endDate = endDate,
        )

       // 2 Todays NCD visits
        viewModel.sevikaVisitCountInDateRange.observe(viewLifecycleOwner) { count: Int ->
            binding.tvSevikaVisitsDaily.text = count.toString()
        }
        viewModel.fetchNCDVisitsInGivenDateRange(
            creatorUuid = sessionManager.creatorID,
            attributeTypeUuid = UuidDictionary.IS_NCD_VISIT_ATTRIBUTE,
            startDate = DateAndTimeUtils.convertInputDateToRequiredFormat(startDate, AppConstants.DATE_FORMAT_YYYY_MM_DD, AppConstants.DATE_FORMAT_DD_MMM_YYYY_FULL_MONTH),
            endDate = DateAndTimeUtils.convertInputDateToRequiredFormat(endDate, AppConstants.DATE_FORMAT_YYYY_MM_DD, AppConstants.DATE_FORMAT_DD_MMM_YYYY_FULL_MONTH),
        )


        // 3 Todays registered patients by logged in hw
        viewModel.patientsRegisteredTodayByLoggedInHwInDateRange.observe(viewLifecycleOwner) { count: Int ->
           binding.tvPatientsBeneficiaryRegistered.text = count.toString()
       }
       viewModel.fetchPatientsRegisteredInGivenDateRangeByLoggedInHw(
           creatorUuid = sessionManager.providerID,
           startDate = DateAndTimeUtils.convertInputDateToRequiredFormat(startDate, AppConstants.DATE_FORMAT_YYYY_MM_DD, AppConstants.DATE_FORMAT_DD_MMM_YYYY_FULL_MONTH),
           endDate = DateAndTimeUtils.convertInputDateToRequiredFormat(endDate, AppConstants.DATE_FORMAT_YYYY_MM_DD, AppConstants.DATE_FORMAT_DD_MMM_YYYY_FULL_MONTH),
       )

        // 4 Todays HW active status
        viewModel.hwActiveStatusInDateRange.observe(viewLifecycleOwner) { count: Int ->
            binding.tvPatientsStatus.text = count.toString()
        }

        viewModel.fetchHWActiveStatusInRange(
            creatorUuid = sessionManager.creatorID,
            startDate = DateAndTimeUtils.convertInputDateToRequiredFormat(startDate, AppConstants.DATE_FORMAT_YYYY_MM_DD, AppConstants.DATE_FORMAT_DD_MMM_YYYY_FULL_MONTH),
            endDate= DateAndTimeUtils.convertInputDateToRequiredFormat(endDate, AppConstants.DATE_FORMAT_YYYY_MM_DD, AppConstants.DATE_FORMAT_DD_MMM_YYYY_FULL_MONTH)
        )

        // 5 Baseline survey registered patients
        viewModel.patientsWithBaselineSurveyInDateRange.observe(viewLifecycleOwner) { count: Int ->
            binding.tvHouseholdRegisteredValue.text = count.toString()
        }
        viewModel.fetchBaselineSurveyRegisteredPatientsInDateRange(
            creatorUuid = sessionManager.providerID,
            startDate = DateAndTimeUtils.convertInputDateToRequiredFormat(startDate, AppConstants.DATE_FORMAT_YYYY_MM_DD, AppConstants.DATE_FORMAT_DD_MMM_YYYY_FULL_MONTH),
            endDate = DateAndTimeUtils.convertInputDateToRequiredFormat(endDate, AppConstants.DATE_FORMAT_YYYY_MM_DD, AppConstants.DATE_FORMAT_DD_MMM_YYYY_FULL_MONTH)
        )

    }


    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateRunnable)
        _binding = null
    }


    private fun initialize() {
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val application = requireActivity().application as IntelehealthApplication
                return DateRangeMyAchievementsViewModel(application) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[DateRangeMyAchievementsViewModel::class.java]
        sessionManager=  SessionManager(requireActivity())


        binding.tvStartDate.setOnClickListener {
            selectDate(binding.tvStartDate, "startDate")
        }
        binding.tvEndDate.setOnClickListener {
            selectDate(binding.tvEndDate, "endDate")
        }
        tooltipCall();
        handler.post(updateRunnable)

    }
    private fun fetchAllStats() {
        Log.d(TAG, "fetchAllStats: startDate : "+startDate)
        Log.d(TAG, "fetchAllStats: enddate : "+endDate)

        viewModel.fetchDoctorVisitsCountInGivenDateRange(
            creatorUuid = sessionManager.creatorID,
            attributeTypeUuid = UuidDictionary.IS_NCD_VISIT_ATTRIBUTE,
            startDate = startDate,
            endDate = endDate,
        )

        viewModel.fetchNCDVisitsInGivenDateRange(
            creatorUuid = sessionManager.creatorID,
            attributeTypeUuid = UuidDictionary.IS_NCD_VISIT_ATTRIBUTE,
            startDate = DateAndTimeUtils.convertInputDateToRequiredFormat(startDate, AppConstants.DATE_FORMAT_YYYY_MM_DD, AppConstants.DATE_FORMAT_DD_MMM_YYYY_FULL_MONTH),
            endDate = DateAndTimeUtils.convertInputDateToRequiredFormat(endDate, AppConstants.DATE_FORMAT_YYYY_MM_DD, AppConstants.DATE_FORMAT_DD_MMM_YYYY_FULL_MONTH),
        )

        viewModel.fetchPatientsRegisteredInGivenDateRangeByLoggedInHw(
            creatorUuid = sessionManager.providerID,
            startDate = DateAndTimeUtils.convertInputDateToRequiredFormat(startDate, AppConstants.DATE_FORMAT_YYYY_MM_DD, AppConstants.DATE_FORMAT_DD_MMM_YYYY_FULL_MONTH),
            endDate =DateAndTimeUtils.convertInputDateToRequiredFormat(endDate, AppConstants.DATE_FORMAT_YYYY_MM_DD, AppConstants.DATE_FORMAT_DD_MMM_YYYY_FULL_MONTH)
        )


        viewModel.fetchHWActiveStatusInRange(
            creatorUuid = sessionManager.creatorID,
            startDate = DateAndTimeUtils.convertInputDateToRequiredFormat(startDate, AppConstants.DATE_FORMAT_DD_MMM_YYYY, AppConstants.DATE_FORMAT_DD_MMM_YYYY_FULL_MONTH),
            endDate= DateAndTimeUtils.convertInputDateToRequiredFormat(endDate, AppConstants.DATE_FORMAT_DD_MMM_YYYY, AppConstants.DATE_FORMAT_DD_MMM_YYYY_FULL_MONTH)
        )

        viewModel.fetchBaselineSurveyRegisteredPatientsInDateRange(
            creatorUuid = sessionManager.providerID,
            startDate = DateAndTimeUtils.convertInputDateToRequiredFormat(startDate, AppConstants.DATE_FORMAT_YYYY_MM_DD, AppConstants.DATE_FORMAT_DD_MMM_YYYY_FULL_MONTH),
            endDate = DateAndTimeUtils.convertInputDateToRequiredFormat(endDate, AppConstants.DATE_FORMAT_YYYY_MM_DD, AppConstants.DATE_FORMAT_DD_MMM_YYYY_FULL_MONTH)
        )
    }
    private fun selectDate(textView: TextView, type: String) {
        val language = sessionManager.appLanguage

        val displayFormat = "dd MMM, yyyy"
        val storageFormat = "yyyy-MM-dd"

        val currentDate = Calendar.getInstance()

        // Parse selected TextView's current text
        val existingDateCal = DateAndTimeUtils.convertStringToCalendarObjectGeneric(
            textView.text.toString(),
            arrayOf(storageFormat, displayFormat),
            language
        ) ?: Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireActivity(),
            R.style.datepicker,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }

                val selectedDate = selectedCal.time

                val dateForQuery = DateAndTimeUtils.convertDateObjectToString(selectedDate, storageFormat)
                val dateForDisplay = DateAndTimeUtils.convertDateObjectToString(selectedDate, displayFormat)

                textView.text = dateForDisplay

                if (type.equals("startDate", ignoreCase = true)) {
                    startDate = dateForQuery
                } else if (type.equals("endDate", ignoreCase = true)) {
                    endDate = dateForQuery
                }

                // Optional: trigger filter logic
                if (!startDate.isNullOrBlank() && !endDate.isNullOrBlank()) {
                    fetchAllStats()
                }
            },
            existingDateCal.get(Calendar.YEAR),
            existingDateCal.get(Calendar.MONTH),
            existingDateCal.get(Calendar.DAY_OF_MONTH)
        )

        val datePicker = datePickerDialog.datePicker

        // Set max date always to current date
        datePicker.maxDate = currentDate.timeInMillis

        // Additional logic to enforce start ≤ end
        if (type.equals("startDate", ignoreCase = true) && !endDate.isNullOrBlank()) {
            val endDateCal = DateAndTimeUtils.convertStringToCalendarObjectGeneric(
                endDate!!,
                arrayOf(storageFormat),
                language
            )
            endDateCal?.let {
                datePicker.maxDate = minOf(currentDate.timeInMillis, it.timeInMillis)
            }
        } else if (type.equals("endDate", ignoreCase = true) && !startDate.isNullOrBlank()) {
            val startDateCal = DateAndTimeUtils.convertStringToCalendarObjectGeneric(
                startDate!!,
                arrayOf(storageFormat),
                language
            )
            startDateCal?.let {
                datePicker.minDate = it.timeInMillis
            }
        }

        datePickerDialog.show()
    }
    private fun setDefaultDates() {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat(AppConstants.DATE_FORMAT_YYYY_MM_DD, Locale.ENGLISH)

        // Set startDate to 1st of current month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        startDate = sdf.format(calendar.time)
        binding.tvStartDate.text = DateAndTimeUtils().convertDateForDisplay(startDate)

        // Set endDate to today's date
        calendar.time = Date()
        endDate = sdf.format(calendar.time)
        binding.tvEndDate.text = DateAndTimeUtils().convertDateForDisplay(endDate)
    }

    private fun showTooltip(anchorView: View, title: String, description: String) {
        val inflater = LayoutInflater.from(anchorView.context)
        val popupView = inflater.inflate(R.layout.ui2_layout_tooltip_my_achievements, null)

        popupView.findViewById<TextView>(R.id.tooltip_title).text = title
        popupView.findViewById<TextView>(R.id.tooltip_description).text = description

        val popupWindow = PopupWindow(
            popupView,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 8f

        // Post ensures popupView is measured before showing
        popupView.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )

        anchorView.post {
            // Get anchor view location on screen
            val location = IntArray(2)
            anchorView.getLocationOnScreen(location)

            val anchorX = location[0]
            val anchorY = location[1]
            val anchorWidth = anchorView.width
            val anchorHeight = anchorView.height

            val popupWidth = popupView.measuredWidth

            // Calculate X so that the tooltip is horizontally centered on the icon
            val xOffset = (anchorWidth - popupWidth) / 2

            // Show tooltip just below the icon, aligned to its center
            popupWindow.showAsDropDown(anchorView, xOffset, 0)
        }
    }

    private fun tooltipCall(){

        binding.ivInfoDoctorVisits.setOnClickListener {
            showTooltip(binding.ivInfoDoctorVisits, getString(R.string.doctor_visits_uploaded_tooltip_title),getString(R.string.doctor_visits_uploaded_tooltip_content))
        }
        binding.ivInfoSevikaVisits.setOnClickListener {
            showTooltip(binding.ivInfoSevikaVisits,  getString(R.string.sevika_visits_uploaded_tooltip_title),getString(R.string.sevika_visits_uploaded_tooltip_content))
        }
        binding.ivInfoBeneficiaryRegistered.setOnClickListener {
            showTooltip(binding.ivInfoBeneficiaryRegistered,  getString(R.string.beneficiaries_registered_tooltip_title),getString(R.string.beneficiaries_registered_tooltip_content))
        }
        binding.ivHouseholdRegistered.setOnClickListener {
            showTooltip(binding.ivHouseholdRegistered,  getString(R.string.households_registered_tooltip_title),getString(R.string.households_registered_tooltip_content))
        }
        binding.ivInfoStatus.setOnClickListener {
            showTooltip(binding.ivInfoStatus,  getString(R.string.active_status_tooltip_title),getString(R.string.active_status_tooltip_content))
        }
        binding.ivDailyTimeSpent.setOnClickListener {
            showTooltip(binding.ivDailyTimeSpent,  getString(R.string.daily_time_spent_by_sevika_tooltip_title),getString(R.string.daily_time_spent_by_sevika_tooltip_content))
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            Log.d("AppUsageUI", "Runnable called")
            val app = requireContext().applicationContext as IntelehealthApplication
            //val usageMillis = app.appUsageTracker.getTotalUsageTimeMillis()
            val usageMillis = app?.appUsageTracker?.getTotalUsageTimeMillis() ?: 0L

            Log.d("AppUsageUI", "Usage millis: $usageMillis")
            // val usageFormatted = DateAndTimeUtils.convertMillisecondsToHoursAndMinutes(usageMillis)
            val usageFormatted = convertMillisecondsToHoursMinutesSeconds(usageMillis)

            Log.d("AppUsageUI", "Formatted usage: $usageFormatted")
            _binding?.let { binding ->
                binding.tvDailyTimeSpentValue.text = usageFormatted
                handler.postDelayed(this, 60_000)
            }
        }
    }
    fun convertMillisecondsToHoursMinutesSeconds(millis: Long): String {
        //val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60))
        return String.format("%02d hr %02d min", hours, minutes)
    }
}

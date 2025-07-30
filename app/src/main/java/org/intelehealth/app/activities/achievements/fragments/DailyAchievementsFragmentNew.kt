package org.intelehealth.app.activities.achievements.fragments

import android.app.DatePickerDialog
import android.app.usage.UsageStatsManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.app.R
import org.intelehealth.app.activities.achievements.model.SyncViewModel
import org.intelehealth.app.activities.achievements.utils.AppUsageTracker
import org.intelehealth.app.activities.achievements.viewmodel.DailyMyAchievementsViewModel
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.databinding.LayoutDailyAchievementsFragmentBinding
import org.intelehealth.app.syncModule.SyncUtils
import org.intelehealth.app.utilities.DateAndTimeUtils
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.UuidDictionary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DailyAchievementsFragmentNew : Fragment() {

    private var _binding: LayoutDailyAchievementsFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DailyMyAchievementsViewModel
    private lateinit var  sessionManager : SessionManager
    private var selectedDate = DateAndTimeUtils.getTodaysDateInRequiredFormat(AppConstants.DATE_FORMAT_YYYY_MM_DD)
    private val syncViewModel: SyncViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutDailyAchievementsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
        setObservers()
    }

    private fun setObservers() {
        //1 Todays doctor visits
        viewModel.doctorVisitCount.observe(viewLifecycleOwner) { count: Int ->
            binding.tvDrVisitsToday.text = count.toString()
        }
        viewModel.fetchTodaysDoctorVisits(
            creatorUuid = sessionManager.creatorID,
            attributeTypeUuid = UuidDictionary.IS_NCD_VISIT_ATTRIBUTE,
            selectedDate = selectedDate
        )

        // 2 Todays NCD visits
        viewModel.sevikaVisitCount.observe(viewLifecycleOwner) { count: Int ->
            binding.tvSevikaVisitsDaily.text = count.toString()
        }
        viewModel.fetchTodaysNCDVisits(
            creatorUuid = sessionManager.creatorID,
            attributeTypeUuid = UuidDictionary.IS_NCD_VISIT_ATTRIBUTE,
            selectedDate =  selectedDate
        )


        // 3 Todays registered patients by logged in hw
        viewModel.patientsRegisteredTodayByLoggedInHw.observe(viewLifecycleOwner) { count: Int ->
            binding.tvPatientsBeneficiaryRegistered.text = count.toString()
        }
        viewModel.fetchPatientsRegisteredTodayByLoggedInHw(
            creatorUuid = sessionManager.providerID,
            selectedDate = DateAndTimeUtils.convertInputDateToRequiredFormat(selectedDate, AppConstants.DATE_FORMAT_YYYY_MM_DD, AppConstants.DATE_FORMAT_DD_MMM_YYYY_FULL_MONTH)
        )

        // 4 Todays HW active status
        viewModel.hwTodaysActiveStatus.observe(viewLifecycleOwner) { isActive: Boolean ->
            binding.tvPatientsStatus.text = if (isActive) getString(R.string.active) else getString(R.string.non_active)
        }

        viewModel.fetchHWTodaysActiveStatus(
            creatorUuid = sessionManager.creatorID,
            selectedDate = DateAndTimeUtils.convertInputDateToRequiredFormat(selectedDate, AppConstants.DATE_FORMAT_YYYY_MM_DD, AppConstants.DATE_FORMAT_DD_MMM_YYYY_FULL_MONTH)
        )

        // 5 Baseline survey registered patients
        viewModel.patientsWithBaselineSurvey.observe(viewLifecycleOwner) { count: Int ->
            binding.tvHouseholdRegisteredValue.text = count.toString()
        }
        Log.d("TAG", "setObservers: selectedDate : "+selectedDate)
        viewModel.fetchBaselineSurveyRegisteredTodaysPatients(
            creatorUuid = sessionManager.providerID,
            selectedDate = DateAndTimeUtils.convertInputDateToRequiredFormat(selectedDate, AppConstants.DATE_FORMAT_YYYY_MM_DD, AppConstants.DATE_FORMAT_DD_MMM_YYYY_FULL_MONTH)
        )
       /* // 6 Daily time spent */
        setDailyTimeSpent()

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
                return DailyMyAchievementsViewModel(application) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[DailyMyAchievementsViewModel::class.java]
        sessionManager=  SessionManager(requireActivity())


        //set default date
        // Set endDate to today's date
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat(AppConstants.DATE_FORMAT_YYYY_MM_DD, Locale.ENGLISH)
        calendar.time = Date()
        selectedDate = sdf.format(calendar.time)
        binding.tvStartDate.text = DateAndTimeUtils().convertDateForDisplay(selectedDate)


        binding.tvStartDate.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val textView = v as TextView
                if (event.x >= (textView.width - textView.compoundPaddingEnd)) {
                    Log.d("TAG", "Icon Clicked")
                    v.performClick() // Important for accessibility and proper behavior
                    selectDate()
                    return@setOnTouchListener true
                }
            }
            false
        }

        binding.tvStartDate.setOnClickListener {
            // fallback or default action
            selectDate()
        }


        val dateValueSelected= DateAndTimeUtils.convertInputDateToRequiredFormat(selectedDate, AppConstants.DATE_FORMAT_YYYY_MM_DD, AppConstants.DATE_FORMAT_DD_MMM_YYYY)
        binding.tvStartDate.text = dateValueSelected

        syncAppAndUpdateUI()

        tooltipCall();

        handler.post(updateRunnable)


    }

    private fun selectDate() {
        val language = sessionManager.appLanguage
        val date = binding.tvStartDate.text.toString()

        val calendar = if (date.isNotBlank()) {
            DateAndTimeUtils.convertStringToCalendarObject(
                date,
                AppConstants.DATE_FORMAT_DD_MMM_YYYY,
                language
            )
        } else {
            Calendar.getInstance()
        }

        val datePickerDialog = DatePickerDialog(
            requireActivity(),
            R.style.datepicker,
            { _, year, month, dayOfMonth ->
                val newDate = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }

                val selectedDateValue = newDate.time
                binding.tvStartDate.text = DateAndTimeUtils.convertDateObjectToString(
                    selectedDateValue,
                    AppConstants.DATE_FORMAT_DD_MMM_YYYY
                )

                selectedDate = DateAndTimeUtils.convertInputDateToRequiredFormat(
                    binding.tvStartDate.text.toString(),
                    AppConstants.DATE_FORMAT_DD_MMM_YYYY,
                    AppConstants.DATE_FORMAT_YYYY_MM_DD
                )

                Log.d("TAG", "selectDate: selectedDate : $selectedDate")
                fetchAllStats()

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun fetchAllStats() {
        //1 Todays doctor visits
        viewModel.fetchTodaysDoctorVisits(
            creatorUuid = sessionManager.creatorID,
            attributeTypeUuid = UuidDictionary.IS_NCD_VISIT_ATTRIBUTE,
            selectedDate = selectedDate
        )

        // 2 Todays NCD visits
        viewModel.fetchTodaysNCDVisits(
            creatorUuid = sessionManager.creatorID,
            attributeTypeUuid = UuidDictionary.IS_NCD_VISIT_ATTRIBUTE,
            selectedDate =  selectedDate
        )


        // 3 Todays registered patients by logged in hw
        viewModel.fetchPatientsRegisteredTodayByLoggedInHw(
            creatorUuid = sessionManager.providerID,
            selectedDate = DateAndTimeUtils.convertInputDateToRequiredFormat(selectedDate, AppConstants.DATE_FORMAT_YYYY_MM_DD, AppConstants.DATE_FORMAT_DD_MMM_YYYY_FULL_MONTH)
        )

        // 4 Todays HW active status
        viewModel.fetchHWTodaysActiveStatus(
            creatorUuid = sessionManager.creatorID,
            selectedDate = DateAndTimeUtils.convertInputDateToRequiredFormat(selectedDate, AppConstants.DATE_FORMAT_DD_MMM_YYYY, AppConstants.DATE_FORMAT_DD_MMM_YYYY_FULL_MONTH)
        )

        // 5 Baseline survey registered patients
        Log.d("TAG", "setObservers: selectedDate : "+selectedDate)
        viewModel.fetchBaselineSurveyRegisteredTodaysPatients(
            creatorUuid = sessionManager.providerID,
            selectedDate = DateAndTimeUtils.convertInputDateToRequiredFormat(selectedDate, AppConstants.DATE_FORMAT_YYYY_MM_DD, AppConstants.DATE_FORMAT_DD_MMM_YYYY_FULL_MONTH)
        )
        /* // 6 Daily time spent */
        setDailyTimeSpent()
    }

    private fun syncAppAndUpdateUI() {
        val progressOverlay = binding.progressOverlay
        syncViewModel.isSyncing.observe(viewLifecycleOwner) { syncing ->
            progressOverlay.visibility = if (syncing) View.VISIBLE else View.GONE
        }

        syncViewModel.syncResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                fetchAllStats() // Refresh data
            } else {
                Toast.makeText(requireContext(), "Sync failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setDailyTimeSpent() {
        /*val firstLoginMs = DateAndTimeUtils.convertStringDateToMilliseconds(
            sessionManager.firstProviderLoginTime,
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            sessionManager.appLanguage
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val todayStartMs = calendar.timeInMillis
        val startDate = maxOf(todayStartMs, firstLoginMs)
        val endDate = System.currentTimeMillis()

        val usageStatsManager = requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val usageMap = usageStatsManager.queryAndAggregateUsageStats(startDate, endDate)
        val overallUsageStats = usageMap[requireContext().packageName]

        Log.d("DailyTimeSpent", "Start: $startDate, End: $endDate")
        Log.d("DailyTimeSpent", "Stats: $overallUsageStats")

        requireActivity().runOnUiThread {
            val timeSpent = overallUsageStats?.let {
                DateAndTimeUtils.convertMillisecondsToHoursAndMinutes(it.totalTimeInForeground)
            } ?: "0h 0m"
            binding.tvDailyTimeSpentValue.text = timeSpent
        }*/
 /*       val usageMillis = (context as IntelehealthApplication).appUsageTracker.getTotalUsageTimeMillis()
        val usageFormatted = DateAndTimeUtils.convertMillisecondsToHoursAndMinutes(usageMillis)*/
        val app = requireContext().applicationContext as IntelehealthApplication
        val usageMillis = (app.appUsageTracker?.getTotalUsageTimeMillis()) ?: 0L

        Log.d("TAG", "setDailyTimeSpent: usageFormatted : "+usageMillis)

    }

/*
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
*/

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
    override fun onResume() {
        super.onResume()
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

            val anchorWidth = anchorView.width

            val popupWidth = popupView.measuredWidth

            // Convert 12dp to pixels for left shift
            val leftShiftInDp = 52
            val density = anchorView.context.resources.displayMetrics.density
            val leftShiftInPx = (leftShiftInDp * density).toInt()

            // Calculate X so tooltip is centered on anchor, but shifted slightly left
            val xOffset = (anchorWidth - popupWidth) / 2 - leftShiftInPx

            // Show tooltip just below the anchor view with offset
            popupWindow.showAsDropDown(anchorView, xOffset, 0)
        }
    }

}

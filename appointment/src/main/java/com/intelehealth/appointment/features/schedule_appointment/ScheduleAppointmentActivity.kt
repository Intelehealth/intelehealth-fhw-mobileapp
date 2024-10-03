package com.intelehealth.appointment.features.schedule_appointment

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.intelehealth.appointment.AppointmentBuilder
import com.intelehealth.appointment.R
import com.intelehealth.appointment.data.remote.response.SlotInfo
import com.intelehealth.appointment.databinding.ActivityScheduleAppointmentBinding
import com.intelehealth.appointment.utils.CommonKeys
import com.intelehealth.appointment.utils.DateAndTimeUtils
import com.intelehealth.appointment.utils.IntentKeys
import com.intelehealth.appointment.utils.StringUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ScheduleAppointmentActivity : AppCompatActivity() {
    private var isRescheduled: Boolean = false
    lateinit var binding: ActivityScheduleAppointmentBinding
    private lateinit var scheduleAppointmentViewModel: ScheduleAppointmentViewModel
    private var TAG: String = "ScheduleAppointmentActi"
    private var appointmentId: Int = 0
    private var visitUuid: String? = null
    private var patientUuid: String? = null
    private var patientName: String? = null
    private var speciality: String? = null
    private var openMrsId: String? = null
    private var alertDialog: AlertDialog? = null
    private var actionTag: String = ""
    private var requestCode: Int = 0
    private var rescheduleReason: String? = null
    private var app_start_date: String? = null
    private var app_start_time: String? = null
    private var app_start_day: String? = null
    private var syncAnimator: ObjectAnimator? = null
    var selectedDateTime: String = ""
    var slotInfoForBookApp: SlotInfo? = null
    var simpleDateFormat: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
    private var mSelectedStartDate = ""
    private var mSelectedEndDate = ""

    var currentMonth: Int = 0
    var currentYear: Int = 0
    var calendarInstance: Calendar? = null
    var yearToCompare: String = ""
    var monthToCompare: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_schedule_appointment)
        binding.toolbarScheduleAppointments.tvScreenTitleCommon.text =
            getString(R.string.schedule_appointment)
        scheduleAppointmentViewModel =
            ViewModelProvider(this)[ScheduleAppointmentViewModel::class.java]
        mSelectedStartDate = simpleDateFormat.format(Date())
        mSelectedEndDate = simpleDateFormat.format(Date())
        binding.viewModel = scheduleAppointmentViewModel
        setupAnimator()
        getIntentData()
        populateCalenderData()
        observers()
        binding.toolbarScheduleAppointments.imageviewIsInternetCommon.setOnClickListener {
            scheduleAppointmentViewModel.sync()
        }
        binding.toolbarScheduleAppointments.ivBackArrowCommon.setOnClickListener {
            finish()
        }

    }

    private fun populateCalenderData() {
        calendarInstance = Calendar.getInstance()
        currentMonth = calendarInstance.getActualMaximum(Calendar.MONTH)
        currentYear = calendarInstance.get(Calendar.YEAR)
        monthToCompare = currentMonth.toString()
        yearToCompare = currentYear.toString()
        val month_date = SimpleDateFormat("MMMM", Locale.ENGLISH)
        var month_name = month_date.format(calendarInstance.getTime())
        if (AppointmentBuilder.language.equals("hi", ignoreCase = true)) month_name =
           StringUtils.en__hi_dob(month_name)
        tvSelectedMonthYear.setText("$month_name, $currentYear")
        currentMonth = calendarInstance.get(Calendar.MONTH) + 1
        monthToCompare = currentMonth.toString()

        if (monthToCompare == currentMonth.toString() && yearToCompare == currentYear.toString()) {
            enableDisablePreviousButton(false)
        } else {
            enableDisablePreviousButton(true)
        }
        getAllDatesOfSelectedMonth(
            calendarInstance,
            true,
            currentMonth.toString(),
            currentYear.toString(),
            currentMonth.toString()
        )

        ivNextMonth.setOnClickListener(View.OnClickListener { v: View? ->
            //get next months dates for horizontal calendar view
            getNextMonthDates()
        })
        ivPrevMonth.setOnClickListener(View.OnClickListener { v: View? ->
            //get this months dates for horizontal calendar view
            getPreviousMonthDates()
        })
    }

    private fun observers() {
        scheduleAppointmentViewModel.mutableSlotList.observe(this) {
            val slotInfoMorningList = it[CommonKeys.MORNING] ?: emptyList()
            val slotInfoAfternoonList = it[CommonKeys.AFTERNOON] ?: emptyList()
            val slotInfoEveningList = it[CommonKeys.EVENING] ?: emptyList()


            var isSlotNotAvailable: Boolean

            binding.tvMorningLabel.visibility =
                if (slotInfoMorningList.isEmpty()) View.GONE else View.VISIBLE
            binding.rvMorningTimeSlots.visibility =
                if (slotInfoMorningList.isEmpty()) View.GONE else View.VISIBLE
            setDataForMorningAppointments(it)
            isSlotNotAvailable = slotInfoMorningList.isEmpty()

            binding.tvAfternoonLabel.visibility =
                if (slotInfoAfternoonList.isEmpty()) View.GONE else View.VISIBLE
            binding.rvAfternoonTimeSlots.visibility =
                if (slotInfoAfternoonList.isEmpty()) View.GONE else View.VISIBLE
            setDataForAfternoonAppointments(it)
            if (isSlotNotAvailable) isSlotNotAvailable = slotInfoAfternoonList.isEmpty()

            binding.tvEveningLabel.visibility =
                if (slotInfoEveningList.isEmpty()) View.GONE else View.VISIBLE
            binding.rvEveningTimeSlots.visibility =
                if (slotInfoEveningList.isEmpty()) View.GONE else View.VISIBLE
            setDataForEveningAppointments(it)
            if (isSlotNotAvailable) isSlotNotAvailable = slotInfoEveningList.isEmpty()

            binding.emptyTv.text =
                getString(R.string.slot_empty_message)
            binding.emptyTv.visibility =
                if (isSlotNotAvailable) View.VISIBLE else View.GONE
            binding.tvTimeSlotTitle.visibility =
                if (isSlotNotAvailable) View.GONE else View.VISIBLE
        }
    }

    private fun getIntentData() {
        //for reschedule appointment as per old flow
        actionTag = intent.getStringExtra(IntentKeys.ACTION_TAG)!!.lowercase(Locale.getDefault())
        requestCode = intent.getIntExtra(IntentKeys.REQUEST_CODE, 0)
        if (actionTag.isNotEmpty() && actionTag == IntentKeys.RESCHEDULE_APPOINTMENT) {
            binding.tvPrevScheduledDetails.visibility = View.VISIBLE
            binding.tvTitleReschedule.visibility = View.VISIBLE


            appointmentId = intent.getIntExtra(IntentKeys.APPOINTMENT_ID, 0)
            visitUuid = intent.getStringExtra(IntentKeys.VISIT_UUID)
            patientUuid = intent.getStringExtra(IntentKeys.PATIENT_UUID)
            patientName = intent.getStringExtra(IntentKeys.PATIENT_NAME)
            speciality = intent.getStringExtra(IntentKeys.SPECIALITY)
            openMrsId = intent.getStringExtra(IntentKeys.OPEN_MRS_ID)
            app_start_date = intent.getStringExtra(IntentKeys.APP_START_DATE)
            app_start_time = intent.getStringExtra(IntentKeys.APP_START_TIME)
            app_start_day = intent.getStringExtra(IntentKeys.APP_START_DAY)
            rescheduleReason = intent.getStringExtra(IntentKeys.RESCHEDULE_REASON)
            val prevDetails =
                "$app_start_day ,  ${DateAndTimeUtils.getDateInDDMMMMYYYYFormat(app_start_date)}  ${
                    resources.getString(R.string.at)
                }  $app_start_time"
            binding.tvPrevScheduledDetails.text = prevDetails
            scheduleAppointmentViewModel.appStartDate = app_start_date
            scheduleAppointmentViewModel.appStartTime = app_start_time
        } else if (actionTag.isNotEmpty() && actionTag == IntentKeys.NEW_SCHEDULE) {
            visitUuid = intent.getStringExtra(IntentKeys.VISIT_UUID)
            patientUuid = intent.getStringExtra(IntentKeys.PATIENT_UUID)
            patientName = intent.getStringExtra(IntentKeys.PATIENT_NAME)
            appointmentId = intent.getIntExtra(IntentKeys.APPOINTMENT_ID, 0)
            speciality = intent.getStringExtra(IntentKeys.SPECIALITY)
            openMrsId = intent.getStringExtra(IntentKeys.OPEN_MRS_ID)
        }

        if (app_start_date != null && app_start_time != null) {
            isRescheduled = true
        }

        if (speciality != null) {
            getSlotsOperation()
        } else {
            Toast.makeText(
                this,
                resources.getString(R.string.speciality_must_not_null),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getSlotsOperation() {
        binding.tvMorningLabel.visibility = View.GONE
        binding.rvMorningTimeSlots.visibility = View.GONE

        binding.tvAfternoonLabel.visibility = View.GONE
        binding.rvAfternoonTimeSlots.visibility = View.GONE

        binding.tvEveningLabel.visibility = View.GONE
        binding.rvEveningTimeSlots.visibility = View.GONE

        binding.tvTimeSlotTitle.visibility = View.GONE
        binding.emptyTv.text = getString(R.string.loading_slots)
        scheduleAppointmentViewModel.getSlots(mSelectedStartDate, mSelectedEndDate, speciality!!,isRescheduled)

    }


    private fun setDataForAfternoonAppointments(slotMap: Map<String, List<SlotInfo>>) {
        val slotListingAdapter = PickUpTimeSlotsAdapter(
            this,
            slotMap[CommonKeys.AFTERNOON],
            CommonKeys.AFTERNOON
        ) { slotInfo ->
            val result: String = DateAndTimeUtils.getDayOfMonthSuffix(slotInfo.slotDate)
            selectedDateTime =
                result + " " + getResources().getString(R.string.at) + " " + slotInfo.slotTime

            slotInfoForBookApp = slotInfo
            setDataForMorningAppointments(slotMap)
            setDataForEveningAppointments(slotMap)
        }
        binding.rvAfternoonTimeSlots.setAdapter(slotListingAdapter)
    }

    private fun setDataForEveningAppointments(slotMap: Map<String, List<SlotInfo>>) {
        val slotListingAdapter = PickUpTimeSlotsAdapter(
            this,
            slotMap[CommonKeys.EVENING],
            CommonKeys.EVENING
        ) { slotInfo ->
            val result: String = DateAndTimeUtils.getDayOfMonthSuffix(slotInfo.slotDate)
            selectedDateTime =
                result + " " + getResources().getString(R.string.at) + " " + slotInfo.slotTime

            slotInfoForBookApp = slotInfo
            setDataForAfternoonAppointments(slotMap)
            setDataForMorningAppointments(slotMap)
        }
        binding.rvEveningTimeSlots.setAdapter(slotListingAdapter)
    }

    private fun setDataForMorningAppointments(slotMap: Map<String, List<SlotInfo>>) {
        val slotListingAdapter = PickUpTimeSlotsAdapter(
            this,
            slotMap[CommonKeys.MORNING],
            CommonKeys.MORNING
        ) { slotInfo ->
            slotInfoForBookApp = slotInfo
            val result: String = DateAndTimeUtils.getDayOfMonthSuffix(slotInfo.slotDate)
            selectedDateTime =
                result + " " + getResources().getString(R.string.at) + " " + slotInfo.slotTime
            setDataForAfternoonAppointments(slotMap)
            setDataForEveningAppointments(slotMap)
        }
        binding.rvMorningTimeSlots.setAdapter(slotListingAdapter)
    }



    private fun setupAnimator() {
        syncAnimator =
            ObjectAnimator.ofFloat<View>(
                binding.toolbarScheduleAppointments.imageviewIsInternetCommon,
                View.ROTATION,
                0f,
                359f
            ).setDuration(1200)
        syncAnimator!!.repeatCount = ValueAnimator.INFINITE
        syncAnimator!!.interpolator = LinearInterpolator()
    }
}


package com.intelehealth.appointment.features.schedule_appointment

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.intelehealth.appointment.AppointmentBuilder
import com.intelehealth.appointment.R
import com.intelehealth.appointment.data.remote.response.SlotInfo
import com.intelehealth.appointment.databinding.ActivityScheduleAppointmentBinding
import com.intelehealth.appointment.features.horizontalcalendar.CalendarModel
import com.intelehealth.appointment.features.horizontalcalendar.HorizontalCalendarViewAdapter
import com.intelehealth.appointment.utils.CommonKeys
import com.intelehealth.appointment.utils.DateAndTimeUtils
import com.intelehealth.appointment.utils.IntentKeys
import com.intelehealth.appointment.utils.StringUtils
import java.text.ParseException
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
        //populateCalenderData()
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
        currentMonth = calendarInstance!!.getActualMaximum(Calendar.MONTH)
        currentYear = calendarInstance!!.get(Calendar.YEAR)
        monthToCompare = currentMonth.toString()
        yearToCompare = currentYear.toString()
        val month_date = SimpleDateFormat("MMMM", Locale.ENGLISH)
        var month_name = month_date.format(calendarInstance!!.getTime())
        if (AppointmentBuilder.language.equals("hi", ignoreCase = true)) month_name =
           StringUtils.en__hi_dob(month_name)
        binding.tvSelectedMonthYear.text = "$month_name, $currentYear"
        currentMonth = calendarInstance!!.get(Calendar.MONTH) + 1
        monthToCompare = currentMonth.toString()

        if (monthToCompare == currentMonth.toString() && yearToCompare == currentYear.toString()) {
            enableDisablePreviousButton(false)
        } else {
            enableDisablePreviousButton(true)
        }
        getAllDatesOfSelectedMonth(
            calendarInstance!!,
            true,
            currentMonth.toString(),
            currentYear.toString(),
            currentMonth.toString()
        )

        binding.ivNextMonth1.setOnClickListener(View.OnClickListener { v: View? ->
            //get next months dates for horizontal calendar view
            getNextMonthDates()
        })
        binding.ivPrevMonth1.setOnClickListener(View.OnClickListener { v: View? ->
            //get this months dates for horizontal calendar view
            getPreviousMonthDates()
        })
    }

    private fun getPreviousMonthDates() {
        calendarInstance!!.add(Calendar.MONTH, -1)
        val nowCalendar = Calendar.getInstance()
        if (nowCalendar[Calendar.YEAR] <= calendarInstance!![Calendar.YEAR] && nowCalendar[Calendar.MONTH] > calendarInstance!![Calendar.MONTH]) {
            calendarInstance!!.add(Calendar.MONTH, 1)
            enableDisablePreviousButton(false)
            return
        }
        val monthNameNEw = calendarInstance!!.time
        var date: Date? = null
        val formatter = SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy", Locale.ENGLISH)
        try {
            date = formatter.parse(monthNameNEw.toString())
            val formateDate = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(date)

            val dateSplit = formateDate.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            yearToCompare = dateSplit[2]
            monthToCompare = dateSplit[1]
            val monthYear: Array<String?> =
                DateAndTimeUtils.getMonthAndYearFromGivenDate(
                    formateDate
                )

            if (monthYear.size > 0) {
                var selectedPrevMonth = monthYear[0]
                val selectedPrevMonthYear = monthYear[1]
                if (AppointmentBuilder.language
                        .equals("hi", ignoreCase = true)
                ) selectedPrevMonth =
                    StringUtils.en__hi_dob(selectedPrevMonth)
                binding.tvSelectedMonthYear.setText("$selectedPrevMonth, $selectedPrevMonthYear")
                if (calendarInstance!![Calendar.MONTH] + 1 == currentMonth && calendarInstance!![Calendar.YEAR] == currentYear) {
                    enableDisablePreviousButton(false)

                    getAllDatesOfSelectedMonth(
                        calendarInstance!!,
                        true,
                        monthToCompare,
                        selectedPrevMonthYear!!,
                        monthToCompare
                    )
                } else {
                    enableDisablePreviousButton(true)

                    getAllDatesOfSelectedMonth(
                        calendarInstance!!,
                        false,
                        monthToCompare,
                        selectedPrevMonthYear!!,
                        monthToCompare
                    )
                }
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }


    private fun getNextMonthDates() {
        enableDisablePreviousButton(true)

        calendarInstance!!.add(Calendar.MONTH, 1)
        val monthNameNEw = calendarInstance!!.time
        var date: Date? = null
        val formatter = SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy", Locale.ENGLISH)
        try {
            date = formatter.parse(monthNameNEw.toString())
            val formateDate = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(date)
            val monthYear: Array<String?> =
                DateAndTimeUtils.getMonthAndYearFromGivenDate(
                    formateDate
                )
            var selectedNextMonth: String
            val selectedMonthYear: String

            if (monthYear.size > 0) {
                selectedNextMonth = monthYear[0].toString()
                selectedMonthYear = monthYear[1].toString()
                val dateSplit = formateDate.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                if (AppointmentBuilder.language
                        .equals("hi", ignoreCase = true)
                ) selectedNextMonth =
                    StringUtils.en__hi_dob(selectedNextMonth)
                binding.tvSelectedMonthYear.setText("$selectedNextMonth, $selectedMonthYear")
                getAllDatesOfSelectedMonth(
                    calendarInstance!!,
                    calendarInstance!![Calendar.MONTH] + 1 == currentMonth && calendarInstance!![Calendar.YEAR] == currentYear,
                    selectedNextMonth,
                    selectedMonthYear,
                    dateSplit[1]
                )
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }


    private fun enableDisablePreviousButton(wantToEnable: Boolean) {
        //for enable and disable previous month button if month is less than current month
        if (wantToEnable) {
            binding.ivPrevMonth1.setEnabled(true)
            binding.ivPrevMonth1.setColorFilter(
                ContextCompat.getColor(this, R.color.colorPrimary),
                PorterDuff.Mode.SRC_IN
            )
        } else {
            binding.ivPrevMonth1.setEnabled(false)
            binding.ivPrevMonth1.setColorFilter(
                ContextCompat.getColor(this, R.color.font_black_3),
                PorterDuff.Mode.SRC_IN
            )
        }
    }
    private fun getAllDatesOfSelectedMonth(
        calendar: Calendar,
        isCurrentMonth: Boolean,
        selectedMonth: String,
        selectedYear: String,
        selectedMonthForDays: String
    ) {
        val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = if (isCurrentMonth) {
            calendar[Calendar.DAY_OF_MONTH]
        } else {
            1
        }
        val daysLeft = lastDay - currentDay

        var calendarModel: CalendarModel
        val inFormat = SimpleDateFormat("dd-MM-yyyy")
        val outFormat = SimpleDateFormat("EEEE")

        val listOfDates: MutableList<CalendarModel> = ArrayList<CalendarModel>()
        for (i in currentDay..lastDay) {
            try {
                val inputDate = "$i-$selectedMonthForDays-$selectedYear"
                val date = inFormat.parse(inputDate)
                if (date != null) {
                    val dayForDate = outFormat.format(date)
                    val dayForDateFinal = dayForDate.substring(0, 3)

                    if (i == currentDay) {
                        calendarModel = CalendarModel(
                            dayForDateFinal,
                            i,
                            currentDay,
                            true,
                            selectedMonth,
                            selectedYear,
                            false,
                            selectedMonthForDays
                        )
                    } else {
                        calendarModel = CalendarModel(
                            dayForDateFinal,
                            i,
                            currentDay,
                            false,
                            selectedMonth,
                            selectedYear,
                            false,
                            selectedMonthForDays
                        )
                    }

                    listOfDates.add(calendarModel)
                } else {
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }

        //  HorizontalCalendarViewAdapter horizontalCalendarViewAdapter = new HorizontalCalendarViewAdapter(this, listOfDates,this);
        binding.rvHorizontalCal.setAdapter(
            HorizontalCalendarViewAdapter(
                this,
                listOfDates,
                 HorizontalCalendarViewAdapter.OnItemClickListener { calendarModel1: CalendarModel ->
                    val date: Int = calendarModel1.getDate()
                    val month: String = calendarModel1.getSelectedMonthForDays()
                    val year: String = calendarModel1.getSelectedYear()
                    mSelectedStartDate = "$date/$month/$year"
                    mSelectedEndDate = "$date/$month/$year"
                    scheduleAppointmentViewModel.getSlots(mSelectedStartDate,mSelectedEndDate,speciality!!,isRescheduled)
                })
        )
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


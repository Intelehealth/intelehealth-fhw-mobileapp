package org.intelehealth.app.appointmentNew.MyAppointmentNew

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.LocaleList
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.intelehealth.app.R
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.appointment.dao.AppointmentDAO
import org.intelehealth.app.appointment.model.AppointmentInfo
import org.intelehealth.app.appointmentNew.TodaysMyAppointmentsFragment
import org.intelehealth.app.appointmentNew.UpdateAppointmentsCount
import org.intelehealth.app.appointmentNew.UpdateFragmentOnEvent
import org.intelehealth.app.database.dao.EncounterDAO
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.utilities.DateAndTimeUtils
import org.intelehealth.app.utilities.MyAppointmentLoadingListener
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.StringUtils
import org.intelehealth.app.utilities.ToastUtil.showShortToast
import org.intelehealth.app.utilities.constatnt.BundleConstants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.function.Consumer

class PastAppointmentsFragment(var myAppointmentLoadingListener: MyAppointmentLoadingListener) :
    Fragment() {
    var parentView: View? = null
    var rvUpcomingApp: RecyclerView? = null
    var rvCancelledApp: RecyclerView? = null
    var rvCompletedApp: RecyclerView? = null
    var layoutParent: RelativeLayout? = null
    var frameLayoutFilter: FrameLayout? = null
    var filterIm: ImageView? = null
    var ivClearText: ImageView? = null
    var filtersList: MutableList<FilterOptionsModel>? = null
    var filtersListNew: List<String>? = null

    var completedLay: LinearLayout? = null
    var cancelledLay: LinearLayout? = null
    var missedLay: LinearLayout? = null

    var completedTv: TextView? = null
    var cancelledTv: TextView? = null
    var missedTv: TextView? = null

    var completedIm: ImageView? = null
    var cancelledIm: ImageView? = null
    var missedIm: ImageView? = null

    var selectedAppointmentOption: String? = null
    var rbUpcoming: RadioButton? = null
    var scrollChips: HorizontalScrollView? = null
    var isChipInit = false
    var layoutUpcoming: LinearLayout? = null
    var layoutCancelled: LinearLayout? = null
    var layoutCompleted: LinearLayout? = null
    var layoutParentAll: LinearLayout? = null
    var fromDate: String? = ""
    var toDate: String? = ""
    var whichAppointment = ""
    var autotvSearch: EditText? = null
    var searchPatientText = ""
    var noDataFoundForUpcoming: View? = null
    var noDataFoundForCompleted: View? = null
    var noDataFoundForCancelled: View? = null
    var tvFromDate: TextView? = null
    var tvToDate: TextView? = null
    var MY_REQUEST_CODE = 5555
    private var listener: UpdateAppointmentsCount? = null
    var totalUpcomingApps = 0
    var totalCancelled = 0
    var totalCompleted = 0
    var sessionManager: SessionManager? = null
    var currentDate = ""
    private var nsvToday: NestedScrollView? = null
    private val upcomingLimit = 15
    private var upcomingStart = 0
    private var upcomingEnd = upcomingStart + upcomingLimit
    private var isUpcomingFullyLoaded = false
    private var upcomingAppointmentInfoList: MutableList<AppointmentInfo>? = null
    private val upcomingSearchList: MutableList<AppointmentInfo> = ArrayList()
    private var pastAppointmentsAdapter: PastMyAppointmentsAdapter? = null
    private var sortIm: ImageView? = null
    var tvResultsFor: TextView? = null

    private val disposables = CompositeDisposable()

    //true = ascending, false = descending
    var sortStatus = true
    override fun onResume() {
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(context)
        myAppointmentLoadingListener.onStartPast()
        (activity as MyAppointmentActivityNew?)!!.initUpdateFragmentOnEvent(
            1,
            object : UpdateFragmentOnEvent {
                override fun onStart(eventFlag: Int) {
                    Log.v(TAG, "onStart")
                }

                override fun onFinished(eventFlag: Int) {
                    Log.v(TAG, "onFinished")
                    appointments
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        setLocale(context)
        parentView = inflater.inflate(R.layout.fragment_past_appointments, container, false)
        initUI()
        clickListeners()
        return parentView
    }

    fun setLocale(context: Context?): Context? {
        val sessionManager1 = SessionManager(context)
        val appLanguage = sessionManager1.appLanguage
        val res = requireContext().resources
        val conf = res.configuration
        val locale = Locale(appLanguage)
        Locale.setDefault(locale)
        conf.setLocale(locale)
        context?.createConfigurationContext(conf)
        val dm = res.displayMetrics
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(LocaleList(locale))
        } else {
            conf.locale = locale
        }
        res.updateConfiguration(conf, dm)
        return context
    }

    private fun setFiltersToTheGroup(inputModel: FilterOptionsModel) {
        var result = false
        if (filtersList!!.size > 0) {
            for (i in filtersList!!.indices) {
                val optionModel1 = filtersList!![i]
                if (optionModel1.filterValue == inputModel.filterValue) {
                    return
                    result = false
                    filtersList!!.remove(optionModel1)
                    filtersList!!.add(inputModel)
                } else {
                    result = true
                }
            }
            if (result) {
                filtersList!!.add(inputModel)
                appointments
            }
        } else {
            filtersList!!.add(inputModel)
            appointments
        }
        if (filtersList!!.size > 0) {
            scrollChips!!.visibility = View.VISIBLE
        }

        val chipGroup = parentView!!.findViewById<ChipGroup>(R.id.chipgroup_filter)
        isChipInit = true
        chipGroup.removeAllViews()
        for (index in filtersList!!.indices) {
            val chip = layoutInflater.inflate(R.layout.chip_custom_ui2, chipGroup, false) as Chip
            val filterOptionsModel = filtersList!![index]
            val tagName = filterOptionsModel.filterValue
            var tagName1: String? = filterOptionsModel.filterValue
            if (sessionManager!!.appLanguage.equals("hi", ignoreCase = true)) tagName1 =
                StringUtils.en_hi_dob_updated(tagName)
            val paddingDp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10f,
                resources.displayMetrics
            ).toInt()
            chip.text = tagName1
            chip.isCloseIconVisible = true
            chip.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.ui2_ic_selcted_chip_bg)
            chipGroup.addView(chip)
            chip.setOnCloseIconClickListener { v: View? ->
                filtersList!!.remove(filterOptionsModel)
                chipGroup.removeView(chip)
                if (tagName.contains("appointment")) {
                    manageUIAsPerChips("upcoming")
                }
                if (filtersList != null && filtersList!!.size == 0) {
                    scrollChips!!.visibility = View.GONE
                    fromDate = ""
                    toDate = ""
                    whichAppointment = ""
                }
                appointments
                updateFilterUi()
            }
        }
    }

    private fun manageUIAsPerChips(tagName: String) {
        val textToMatch = tagName.lowercase(Locale.getDefault())
        if (textToMatch.contains("upcoming")) {
            updateMAinLayoutAsPerOptionSelected("upcoming")
        } else if (tagName.contains("cancelled")) {
            updateMAinLayoutAsPerOptionSelected("cancelled")
        } else if (tagName.contains("completed")) {
            updateMAinLayoutAsPerOptionSelected("completed")
        }
    }

    private fun initUI() {
        sessionManager = SessionManager(context)
        val dateFormat1 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        currentDate = dateFormat1.format(Date())
        rvUpcomingApp = parentView!!.findViewById(R.id.rv_upcoming_appointments)
        layoutParent = parentView!!.findViewById(R.id.layout_parent)
        frameLayoutFilter = parentView!!.findViewById(R.id.filter_frame_past_appointments)
        filterIm = parentView!!.findViewById(R.id.filter_im)

        completedLay = frameLayoutFilter?.findViewById(R.id.completed_lay)
        cancelledLay = frameLayoutFilter?.findViewById(R.id.cancelled_lay)
        missedLay = frameLayoutFilter?.findViewById(R.id.missed_lay)

        completedTv = frameLayoutFilter?.findViewById(R.id.completed_tv)
        cancelledTv = frameLayoutFilter?.findViewById(R.id.cancelled_tv)
        missedTv = frameLayoutFilter?.findViewById(R.id.missed_tv)

        completedIm = frameLayoutFilter?.findViewById(R.id.completed_im)
        cancelledIm = frameLayoutFilter?.findViewById(R.id.cancelled_im)
        missedIm = frameLayoutFilter?.findViewById(R.id.missed_im)

        scrollChips = parentView!!.findViewById(R.id.scroll_chips)
        tvResultsFor = parentView!!.findViewById(R.id.tv_results_for)
        layoutUpcoming = parentView!!.findViewById(R.id.layout_upcoming1)
        layoutParentAll = parentView!!.findViewById(R.id.layout_parent_past)
        autotvSearch = parentView!!.findViewById(R.id.et_search)
        ivClearText = parentView!!.findViewById(R.id.iv_clear)
        ivClearText?.setOnClickListener(View.OnClickListener { v: View? ->
            autotvSearch?.setText("")
            searchPatientText = ""
            resetData()
        })
        sortIm = parentView!!.findViewById(R.id.sort_im)
        noDataFoundForUpcoming = parentView!!.findViewById(R.id.layout_no_data_found_upcoming)
        noDataFoundForCompleted = parentView!!.findViewById(R.id.layout_no_data_found_completed)
        noDataFoundForCancelled = parentView!!.findViewById(R.id.layout_no_data_found_cancelled)
        if (isChipInit) {
            tvResultsFor?.visibility = View.VISIBLE
            scrollChips?.visibility = View.VISIBLE
        } else {
            tvResultsFor?.visibility = View.GONE
            scrollChips?.visibility = View.GONE
        }
        filtersList = ArrayList()
        filtersListNew = ArrayList()
        nsvToday = parentView!!.findViewById(R.id.nsv_today)
        nsvToday?.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (v.getChildAt(v.childCount - 1) != null) {
                if (scrollY > oldScrollY) {
                    if (upcomingAppointmentInfoList != null && upcomingAppointmentInfoList!!.size == 0) {
                        isUpcomingFullyLoaded = true
                    }
                    if (!isUpcomingFullyLoaded) {
                        setMoreDataIntoUpcomingRecyclerView()
                    }
                }
            }
        })
        sortIm?.setOnClickListener(View.OnClickListener { sortList() })

        completedLay?.setOnClickListener {
            setFiltersToTheGroup(FilterOptionsModel("status", "Completed"))
            updateFilterUi()
        }

        cancelledLay?.setOnClickListener {
            setFiltersToTheGroup(FilterOptionsModel("status", "Cancelled"))
            updateFilterUi()
        }

        missedLay?.setOnClickListener {
            setFiltersToTheGroup(FilterOptionsModel("status", "Missed"))
            updateFilterUi()
        }
        fragmentResultListener()
    }

    private fun updateFilterUi() {
        val isCompletedExist = filtersList?.find { it.filterValue == "Completed" }
        if (isCompletedExist != null) {
            completedIm?.visibility = View.VISIBLE
        } else {
            completedIm?.visibility = View.INVISIBLE
        }

        val isCancelledExist = filtersList?.find { it.filterValue == "Cancelled" }
        if (isCancelledExist != null) {
            cancelledIm?.visibility = View.VISIBLE
        } else {
            cancelledIm?.visibility = View.INVISIBLE
        }

        val isMissedExist = filtersList?.find { it.filterValue == "Missed" }
        if (isMissedExist != null) {
            missedIm?.visibility = View.VISIBLE
        } else {
            missedIm?.visibility = View.INVISIBLE
        }

        frameLayoutFilter?.visibility = View.GONE
        filterIm?.background = ContextCompat.getDrawable(requireActivity(), R.drawable.ui2_ic_filter_bg);
    }

    private fun sortList() {
        if (upcomingAppointmentInfoList!!.size > 1) {
            if (sortStatus) {
                showShortToast(requireActivity(), getString(R.string.sorted_by_descending_order))
            } else {
                showShortToast(requireActivity(), getString(R.string.sorted_by_ascending_order))
            }
            sortStatus = !sortStatus
            appointments
        }
    }

    /**
     * listening result from calender dialog
     */
    private fun fragmentResultListener() {
        parentFragmentManager.setFragmentResultListener(
            "requestKey",
            this@PastAppointmentsFragment
        ) { requestKey: String?, bundle: Bundle ->
            val selectedDate = bundle.getString(BundleConstants.SELECTED_DATE)
            if (selectedDate != null) {
                val whichDate = bundle.getString(BundleConstants.WHICH_DATE)
                if (!whichDate!!.isEmpty() && whichDate == BundleConstants.FROM_DATE) {
                    if (!toDate!!.isEmpty() && DateAndTimeUtils.isAfter(
                            selectedDate,
                            toDate,
                            DateAndTimeUtils.D_FORMAT_dd_M_yyyy
                        )
                    ) {
                        Toast.makeText(
                            requireContext(),
                            R.string.the_from_date_cannot_be_greater_than_the_to_date,
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setFragmentResultListener
                    }
                    fromDate = selectedDate
                    var dateToshow1 =
                        DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(fromDate)
                    if (sessionManager!!.appLanguage.equals("hi", ignoreCase = true)) dateToshow1 =
                        StringUtils.en_hi_dob_updated(
                            DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(fromDate)
                        )
                    if (!fromDate!!.isEmpty()) {
                        val splitedDate =
                            fromDate!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        tvFromDate!!.text = dateToshow1 + ", " + splitedDate[2]
                    }
                    dismissDateFilterDialog()
                }
                if (!whichDate.isEmpty() && whichDate == BundleConstants.TO_DATE) {
                    if (!fromDate!!.isEmpty() && DateAndTimeUtils.isBefore(
                            selectedDate,
                            fromDate,
                            DateAndTimeUtils.D_FORMAT_dd_M_yyyy
                        )
                    ) {
                        Toast.makeText(
                            requireContext(),
                            R.string.the_to_date_cannot_be_less_than_the_from_date,
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setFragmentResultListener
                    }
                    toDate = selectedDate
                    var dateToshow1 = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(toDate)
                    if (sessionManager!!.appLanguage.equals("hi", ignoreCase = true)) dateToshow1 =
                        StringUtils.en_hi_dob_updated(
                            DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(toDate)
                        )
                    if (!toDate!!.isEmpty()) {
                        val splitedDate =
                            toDate!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        tvToDate!!.text = dateToshow1 + ", " + splitedDate[2]
                    }
                    dismissDateFilterDialog()
                }
            }
        }
    }

    private fun setMoreDataIntoUpcomingRecyclerView() {
        if (upcomingSearchList.size > 0) {
            return
        }
        if (isUpcomingFullyLoaded) {
            return
        }
        showShortToast(requireActivity(), getString(R.string.loading_more))
        val tempList = AppointmentDAO().getPastAppointmentsWithFilters(
            upcomingLimit,
            upcomingStart,
            if (sortStatus) "ASC" else "DESC",
            searchPatientText,
            filtersList
        )
        if ((tempList?.size ?: 0) > 0) {
            upcomingAppointmentInfoList!!.addAll(tempList!!)
            pastAppointmentsAdapter!!.notifyDataSetChanged()
            upcomingStart = upcomingEnd
            upcomingEnd += upcomingLimit
        } else {
            isUpcomingFullyLoaded
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun clickListeners() {
        layoutParent!!.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (frameLayoutFilter!!.isShown) {
                    frameLayoutFilter!!.visibility = View.GONE
                    filterIm?.background = ContextCompat.getDrawable(requireActivity(), R.drawable.ui2_ic_filter_bg);
                }else{
                    filterIm?.background = ContextCompat.getDrawable(requireActivity(), R.drawable.ui2_ic_filter_border_bg);

                }
                return@setOnTouchListener true
            }
            false
        }
        layoutParent!!.setOnClickListener { v: View? ->
            if (frameLayoutFilter!!.isShown) {
                frameLayoutFilter!!.visibility = View.GONE
                filterIm?.background = ContextCompat.getDrawable(requireActivity(), R.drawable.ui2_ic_filter_bg);
            }else{
                filterIm?.background = ContextCompat.getDrawable(requireActivity(), R.drawable.ui2_ic_filter_border_bg);
            }
        }

        //click listeners for filters
        filterIm!!.setOnClickListener { v: View? ->

            // filter options
            if (frameLayoutFilter!!.visibility == View.VISIBLE) {
                frameLayoutFilter!!.visibility = View.GONE
                filterIm?.background = ContextCompat.getDrawable(requireActivity(), R.drawable.ui2_ic_filter_bg);
            } else {
                frameLayoutFilter!!.visibility = View.VISIBLE
                filterIm?.background = ContextCompat.getDrawable(requireActivity(), R.drawable.ui2_ic_filter_border_bg);
            }
        }


        //filter all appointments
        /*rgFilterAppointments!!.setOnCheckedChangeListener { group: RadioGroup, checkedId: Int ->
            val checkedRadioButton = group.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                selectedAppointmentOption = checkedRadioButton.text.toString()

                //onRadioButtonClicked(checkedRadioButton, selectedAppointmentOption);
            }
        }*/
        autotvSearch!!.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchText = autotvSearch!!.text.toString()
                searchPatientText = if (searchText.isNotEmpty()) {
                    autotvSearch!!.text.toString()
                } else {
                    ""
                }
                appointments
                return@setOnEditorActionListener true
            }
            false
        }
        autotvSearch!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.toString().isNotEmpty()) {
                    ivClearText!!.visibility = View.VISIBLE
                } else {
                    searchPatientText = ""
                    ivClearText!!.visibility = View.GONE
                    appointments
                }
            }
        })
    }

    private fun searchOperation(query: String) {
        Thread {
            upcomingAppointmentInfoList =
                AppointmentDAO().getPastAppointmentsWithFilters(
                    upcomingLimit,
                    upcomingStart, if (sortStatus) "ASC" else "DESC", query, filtersList
                )

            requireActivity().runOnUiThread {
                pastAppointmentsAdapter =
                    PastMyAppointmentsAdapter(activity, upcomingAppointmentInfoList, "upcoming")
                rvUpcomingApp!!.isNestedScrollingEnabled = true
                rvUpcomingApp!!.adapter = pastAppointmentsAdapter
            }

        }.start()
    }


    private fun updateMAinLayoutAsPerOptionSelected(cardName: String) {
        //adjust main layout as per option selected like figma prototype
        if (cardName == "upcoming") {
            layoutUpcoming!!.visibility = View.VISIBLE
            layoutCompleted!!.visibility = View.VISIBLE
            layoutCancelled!!.visibility = View.VISIBLE
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            params.weight = 1.0f
            params.gravity = Gravity.TOP
            layoutUpcoming!!.layoutParams = params
        }
    }

    inner class FilterOptionsModel(var filterType: String, var filterValue: String)

    private val appointments: Unit
        get() {
            //whichAppointment = "";
            upcomingAppointments
        }
    private val upcomingAppointments: Unit
        get() {
            //recyclerview for upcoming appointments
            val pastAppointmentDisposable = getPastDataObserver
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ appointments ->
                    noDataFoundForUpcoming!!.visibility = View.VISIBLE
                    appointments?.let {
                        if (it.isNotEmpty()) {
                            rvUpcomingApp?.visibility = View.VISIBLE
                            noDataFoundForUpcoming?.visibility = View.GONE
                            totalUpcomingApps = appointments.size ?: 0
                            pastAppointmentsAdapter =
                                PastMyAppointmentsAdapter(activity, appointments, "upcoming")
                            rvUpcomingApp!!.adapter = pastAppointmentsAdapter
                            upcomingStart = upcomingEnd
                            upcomingEnd += upcomingLimit
                        } else {
                            rvUpcomingApp!!.visibility = View.GONE
                            noDataFoundForUpcoming!!.visibility = View.VISIBLE
                        }
                    }
                    myAppointmentLoadingListener.onStopPast()
                },
                    { error ->
                        myAppointmentLoadingListener.onStopPast()
                        error.printStackTrace()
                    })

            disposables.add(pastAppointmentDisposable)
        }

    private val getPastDataObserver = Observable.create<MutableList<AppointmentInfo>?> {
        initLimits()
        upcomingAppointmentInfoList = AppointmentDAO().getPastAppointmentsWithFilters(
            upcomingLimit,
            upcomingStart,
            if (sortStatus) "ASC" else "DESC",
            searchPatientText,
            filtersList
        )

        if ((upcomingAppointmentInfoList?.size ?: 0) > 0) {
            upcomingAppointmentInfoList?.forEach(Consumer { appointmentInfo: AppointmentInfo ->
                val visitDTO = PatientsDAO.isVisitPresentForPatient_fetchVisitValues(
                    appointmentInfo.patientId
                )
                if (visitDTO.uuid != null && visitDTO.startdate != null) {
                    val encounteruuid =
                        EncounterDAO.getStartVisitNoteEncounterByVisitUUID(visitDTO.uuid)
                    appointmentInfo.isPrescription_exists =
                        encounteruuid.isNotEmpty() && !encounteruuid.equals("", ignoreCase = true)
                }
                val patientProfilePath = getPatientProfile(appointmentInfo.patientId)
                appointmentInfo.patientProfilePhoto = patientProfilePath


            })
        }
        if (upcomingAppointmentInfoList != null) {
            it.onNext(upcomingAppointmentInfoList!!)
        } else {
            it.onNext(mutableListOf())
        }
        it.onComplete()

    }

    private fun getPatientProfile(patientUuid: String): String {
        Log.d(TAG, "getPatientProfile: patientUuid : $patientUuid")
        val db = IntelehealthApplication.inteleHealthDatabaseHelper.writableDatabase
        var imagePath = ""
        val idCursor =
            db.rawQuery("SELECT * FROM tbl_patient where uuid = ? ", arrayOf(patientUuid))
        if (idCursor.moveToFirst()) {
            do {
                imagePath =
                    idCursor.getString(idCursor.getColumnIndexOrThrow("patient_photo")) ?: ""
            } while (idCursor.moveToNext())
            idCursor.close()
        }
        return imagePath
    }

    private fun filterAsPerSelectedOptions() {
        initLimits()
        if (whichAppointment.isEmpty() && fromDate!!.isEmpty() && toDate!!.isEmpty()) {
            //all data
            upcomingAppointments
        } else if (whichAppointment.isEmpty() && !fromDate!!.isEmpty() && !toDate!!.isEmpty()) {
            //all
            upcomingAppointments
        } else if (whichAppointment == "upcoming" && !fromDate!!.isEmpty() && !toDate!!.isEmpty()) {
            //upcoming
            upcomingAppointments
        }
    }

    private fun dismissDateFilterDialog() {
        if (!fromDate!!.isEmpty() && !toDate!!.isEmpty()) {
            filterAsPerSelectedOptions()
            Handler().postDelayed({
                val date =
                    DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(fromDate) + " - " + DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(
                        toDate
                    )
                val filterOptionsModel: FilterOptionsModel = FilterOptionsModel("date", date)
                setFiltersToTheGroup(filterOptionsModel)
            }, 2000)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null) {
            val bundle = data.extras
            val selectedDate = bundle!!.getString(BundleConstants.SELECTED_DATE)
            val whichDate = bundle.getString(BundleConstants.WHICH_DATE)
            if (!whichDate!!.isEmpty() && whichDate == BundleConstants.FROM_DATE) {
                if (!toDate!!.isEmpty() && DateAndTimeUtils.isAfter(
                        selectedDate,
                        toDate,
                        DateAndTimeUtils.D_FORMAT_dd_M_yyyy
                    )
                ) {
                    Toast.makeText(
                        requireContext(),
                        "The 'from' date cannot be greater than the 'to' date",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                fromDate = selectedDate
                var dateToshow1 = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(fromDate)
                if (sessionManager!!.appLanguage.equals("hi", ignoreCase = true)) dateToshow1 =
                    StringUtils.en_hi_dob_updated(
                        DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(fromDate)
                    )
                if (!fromDate!!.isEmpty()) {
                    val splitedDate = fromDate!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    tvFromDate!!.text = dateToshow1 + ", " + splitedDate[2]
                }
                dismissDateFilterDialog()
            }
            if (!whichDate.isEmpty() && whichDate == BundleConstants.TO_DATE) {
                if (!fromDate!!.isEmpty() && DateAndTimeUtils.isBefore(
                        selectedDate,
                        fromDate,
                        DateAndTimeUtils.D_FORMAT_dd_M_yyyy
                    )
                ) {
                    Toast.makeText(
                        requireContext(),
                        "The 'to' date cannot be less than the 'from' date",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                toDate = selectedDate
                var dateToshow1 = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(toDate)
                if (sessionManager!!.appLanguage.equals("hi", ignoreCase = true)) dateToshow1 =
                    StringUtils.en_hi_dob_updated(
                        DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(toDate)
                    )
                if (!toDate!!.isEmpty()) {
                    val splitedDate =
                        toDate!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    tvToDate!!.text = dateToshow1 + ", " + splitedDate[2]
                }
                dismissDateFilterDialog()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is UpdateAppointmentsCount) {
            listener = context
            val totalAllApps = totalUpcomingApps + totalCancelled + totalCompleted
            listener!!.updateCount("all", 2000)
        } else {
            throw RuntimeException(
                context.toString()
                        + " must implement OnFragmentCommunicationListener"
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    private fun initLimits() {
        upcomingStart = 0
        isUpcomingFullyLoaded = false
        upcomingEnd = upcomingStart + upcomingLimit
    }

    private fun resetData() {
        upcomingSearchList.clear()
        initLimits()
        appointments
    }

    companion object {
        private const val TAG = "AllAppointmentsFragment"
        fun newInstance(): TodaysMyAppointmentsFragment {
            return TodaysMyAppointmentsFragment()
        }
    }
}
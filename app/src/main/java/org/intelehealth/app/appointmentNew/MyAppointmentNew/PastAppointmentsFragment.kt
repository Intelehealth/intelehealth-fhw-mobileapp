package org.intelehealth.app.appointmentNew.MyAppointmentNew

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
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
import android.widget.RelativeLayout
import android.widget.TextView
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
import org.intelehealth.app.enums.DataLoadingType
import org.intelehealth.app.utilities.CustomLog
import org.intelehealth.app.utilities.MyAppointmentLoadingListener
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.StringUtils
import org.intelehealth.app.utilities.ToastUtil.showShortToast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.function.Consumer

class PastAppointmentsFragment :
    Fragment() {
    private var parentView: View? = null
    private var rvUpcomingApp: RecyclerView? = null
    private var layoutParent: RelativeLayout? = null
    private var frameLayoutFilter: FrameLayout? = null
    private var filterIm: ImageView? = null
    private var ivClearText: ImageView? = null
    private var filtersList: MutableList<FilterOptionsModel>? = null
    private var filtersListNew: List<String>? = null

    private var completedLay: LinearLayout? = null
    private var cancelledLay: LinearLayout? = null
    private var missedLay: LinearLayout? = null

    private var completedTv: TextView? = null
    private var cancelledTv: TextView? = null
    private var missedTv: TextView? = null

    private var completedIm: ImageView? = null
    private var cancelledIm: ImageView? = null
    private var missedIm: ImageView? = null

    private var scrollChips: HorizontalScrollView? = null
    private var isChipInit = false
    private var layoutUpcoming: LinearLayout? = null
    private var layoutParentAll: LinearLayout? = null
    private var mainLayout: RelativeLayout? = null
    private var fromDate: String? = ""
    private var toDate: String? = ""
    private var whichAppointment = ""
    private var autotvSearch: EditText? = null
    private var searchPatientText = ""
    private var noDataFoundForPast: View? = null
    private var tvFromDate: TextView? = null
    private var tvToDate: TextView? = null
    private var listener: UpdateAppointmentsCount? = null
    private var totalUpcomingApps = 0
    private var totalCancelled = 0
    private var totalCompleted = 0
    private var sessionManager: SessionManager? = null
    private var currentDate = ""
    private var nsvToday: NestedScrollView? = null
    private val pastLimit = 15
    private var offset = 0
    private var isPastFullyLoaded = false
    private var pastAppointmentInfoList: MutableList<AppointmentInfo>? = null
    private val pastSearchList: MutableList<AppointmentInfo> = ArrayList()
    private var pastAppointmentsAdapter: PastMyAppointmentsAdapter? = null
    private var sortIm: ImageView? = null
    private var tvResultsFor: TextView? = null

    private val disposables = CompositeDisposable()

    private lateinit var myAppointmentLoadingListener: MyAppointmentLoadingListener

    fun setListener(myAppointmentLoadingListener: MyAppointmentLoadingListener){
        this.myAppointmentLoadingListener = myAppointmentLoadingListener
    }

    //true = ascending, false = descending
    private var sortStatus = true
    override fun onResume() {
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(context)
        if(::myAppointmentLoadingListener.isInitialized){
            myAppointmentLoadingListener.onStartPast()
        }
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
            tvResultsFor?.visibility = View.VISIBLE
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
            chip.setOnCloseIconClickListener {
                filtersList!!.remove(filterOptionsModel)
                chipGroup.removeView(chip)
                if (filtersList != null && filtersList!!.size == 0) {
                    scrollChips!!.visibility = View.GONE
                    tvResultsFor?.visibility = View.GONE
                    fromDate = ""
                    toDate = ""
                    whichAppointment = ""
                }
                appointments
                updateFilterUi()
            }
        }
    }

    private fun initUI() {
        sessionManager = SessionManager(context)
        val dateFormat1 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        currentDate = dateFormat1.format(Date())
        rvUpcomingApp = parentView!!.findViewById(R.id.rv_upcoming_appointments)
        layoutParent = parentView!!.findViewById(R.id.layout_parent)
        mainLayout = parentView!!.findViewById(R.id.main_layout)
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
        noDataFoundForPast = parentView!!.findViewById(R.id.layout_no_data_found_upcoming)
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
        nsvToday?.setOnScrollChangeListener { v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            val view = v.getChildAt(v.childCount - 1)
            val bottom: Int = view.bottom - (v.height + v.scrollY)
            if (bottom == 0) {
                setMoreDataIntoUpcomingRecyclerView()
            }
        }
        sortIm?.setOnClickListener(View.OnClickListener { sortList() })

        completedLay?.setOnClickListener {
            setFiltersToTheGroup(FilterOptionsModel("status", getString(R.string.completed)))
            updateFilterUi()
        }

        cancelledLay?.setOnClickListener {
            setFiltersToTheGroup(FilterOptionsModel("status", getString(R.string.cancelled)))
            updateFilterUi()
        }

        missedLay?.setOnClickListener {
            setFiltersToTheGroup(FilterOptionsModel("status", getString(R.string.missed)))
            updateFilterUi()
        }
    }

    private fun updateFilterUi() {
        val isCompletedExist = filtersList?.find { it.filterValue == getString(R.string.completed) }
        if (isCompletedExist != null) {
            completedIm?.visibility = View.VISIBLE
        } else {
            completedIm?.visibility = View.INVISIBLE
        }

        val isCancelledExist = filtersList?.find { it.filterValue == getString(R.string.cancelled) }
        if (isCancelledExist != null) {
            cancelledIm?.visibility = View.VISIBLE
        } else {
            cancelledIm?.visibility = View.INVISIBLE
        }

        val isMissedExist = filtersList?.find { it.filterValue == getString(R.string.missed) }
        if (isMissedExist != null) {
            missedIm?.visibility = View.VISIBLE
        } else {
            missedIm?.visibility = View.INVISIBLE
        }

        frameLayoutFilter?.visibility = View.GONE
        filterIm?.background = ContextCompat.getDrawable(requireActivity(), R.drawable.ui2_ic_filter_bg);
    }

    private fun sortList() {
        if (pastAppointmentInfoList!!.size > 1) {
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
     * this function will call whenever we paginate the list
     */
    private fun setMoreDataIntoUpcomingRecyclerView() {
        if (pastSearchList.size > 0) {
            return
        }
        if ((activity as MyAppointmentActivityNew).totalPast <= (pastAppointmentInfoList?.size?:0)) {
            return
        }
        showShortToast(requireActivity(),getString(R.string.loading_more))

        val upcomingAppointmentDisposable = getPastDataObserver(DataLoadingType.PAGINATION)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ appointments ->
                if (appointments.size > 0) {
                    pastAppointmentInfoList?.addAll(appointments)
                    pastAppointmentsAdapter?.notifyDataSetChanged()
                    offset = pastAppointmentInfoList?.size?:0
                } else {
                    isPastFullyLoaded = true
                }

            },
                { error ->
                    error.printStackTrace()
                })

        disposables.add(upcomingAppointmentDisposable)
    }

    private fun clickListeners() {
        layoutParent?.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (frameLayoutFilter!!.isShown) {
                    frameLayoutFilter!!.visibility = View.GONE
                    filterIm?.background = ContextCompat.getDrawable(requireActivity(), R.drawable.ui2_ic_filter_bg);
                }
                v?.performClick()
                return@setOnTouchListener true
            }
            false
        }
        layoutParent?.setOnClickListener { v: View? ->
            if (frameLayoutFilter!!.isShown) {
                frameLayoutFilter!!.visibility = View.GONE
                filterIm?.background = ContextCompat.getDrawable(requireActivity(), R.drawable.ui2_ic_filter_bg);
            }
        }

        nsvToday?.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (frameLayoutFilter!!.isShown) {
                    frameLayoutFilter!!.visibility = View.GONE
                    filterIm?.background = ContextCompat.getDrawable(requireActivity(), R.drawable.ui2_ic_filter_bg);
                }
                v?.performClick()
                return@setOnTouchListener true
            }
            false
        }

        autotvSearch?.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (frameLayoutFilter!!.isShown) {
                    frameLayoutFilter!!.visibility = View.GONE
                    filterIm?.background = ContextCompat.getDrawable(requireActivity(), R.drawable.ui2_ic_filter_bg);
                }
                v?.performClick()
                return@setOnTouchListener false
            }
            false
        }

        nsvToday?.setOnClickListener {
            if (frameLayoutFilter!!.isShown) {
                frameLayoutFilter!!.visibility = View.GONE
                filterIm?.background = ContextCompat.getDrawable(requireActivity(), R.drawable.ui2_ic_filter_bg);
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


    inner class FilterOptionsModel(var filterType: String, var filterValue: String)

    private val appointments: Unit
        get() {
            //whichAppointment = "";
            pastAppointments
        }
    private val pastAppointments: Unit
        get() {
            //recyclerview for upcoming appointments
            val pastAppointmentDisposable = getPastDataObserver(DataLoadingType.INITIAL)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ appointments ->
                    noDataFoundForPast!!.visibility = View.VISIBLE
                    appointments?.let {
                        if (it.isNotEmpty()) {
                            rvUpcomingApp?.visibility = View.VISIBLE
                            noDataFoundForPast?.visibility = View.GONE
                            totalUpcomingApps = appointments.size ?: 0
                            pastAppointmentsAdapter =
                                PastMyAppointmentsAdapter(requireActivity(), appointments)
                            rvUpcomingApp!!.adapter = pastAppointmentsAdapter
                            offset = appointments.size
                        } else {
                            rvUpcomingApp!!.visibility = View.GONE
                            noDataFoundForPast!!.visibility = View.VISIBLE
                        }
                    }
                    if(::myAppointmentLoadingListener.isInitialized){
                        myAppointmentLoadingListener.onStopPast()
                    }
                },
                    { error ->
                        if(::myAppointmentLoadingListener.isInitialized){
                            myAppointmentLoadingListener.onStopPast()
                        }
                        error.printStackTrace()
                    })

            disposables.add(pastAppointmentDisposable)
        }


    /**
     * getting past appointment here
     */
    private fun getPastDataObserver(dataLoadingType: DataLoadingType): Observable<MutableList<AppointmentInfo>> {
        return Observable.create {
            //if calling type is initial like first time or from search
            // then we are resetting the offset
            if (dataLoadingType == DataLoadingType.INITIAL){
                initLimits()
            }
            val tempList = AppointmentDAO().getPastAppointmentsWithFilters(
                requireActivity(),
                pastLimit,
                offset,
                if (sortStatus) "ASC" else "DESC",
                searchPatientText,
                filtersList
            )
            if ((tempList?.size ?: 0) > 0) {
                tempList?.forEach(Consumer { appointmentInfo: AppointmentInfo ->
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
            //if type is initial then returning the actual list
            if (dataLoadingType == DataLoadingType.INITIAL){
                pastAppointmentInfoList = tempList
                if (pastAppointmentInfoList != null) {
                    it.onNext(pastAppointmentInfoList!!)
                } else {
                    it.onNext(mutableListOf())
                }
            }
            //if type is from pagination returning the tem list
            else{
                if (tempList != null) {
                    it.onNext(tempList)
                } else {
                    it.onNext(mutableListOf())
                }
            }
            it.onComplete()

        }
    }


    private fun getPatientProfile(patientUuid: String): String {
        CustomLog.d(TAG, "getPatientProfile: patientUuid : $patientUuid")
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
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is UpdateAppointmentsCount) {
            listener = context
            listener?.updateCount("all", 2000)
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
        offset = 0
        isPastFullyLoaded = false
    }

    private fun resetData() {
        pastSearchList.clear()
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
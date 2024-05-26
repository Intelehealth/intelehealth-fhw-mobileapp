package org.intelehealth.app.appointmentNew.MyAppointmentNew

import android.content.Context
import android.content.res.Configuration
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.R
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.appointment.api.ApiClientAppointment
import org.intelehealth.app.appointment.dao.AppointmentDAO
import org.intelehealth.app.appointment.model.AppointmentInfo
import org.intelehealth.app.appointment.model.AppointmentListingResponse
import org.intelehealth.app.appointmentNew.UpdateAppointmentsCount
import org.intelehealth.app.appointmentNew.UpdateFragmentOnEvent
import org.intelehealth.app.enums.DataLoadingType
import org.intelehealth.app.utilities.DateAndTimeUtils
import org.intelehealth.app.utilities.MyAppointmentLoadingListener
import org.intelehealth.app.utilities.NavigationUtils
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.ToastUtil
import org.intelehealth.app.utilities.ToastUtil.showShortToast
import org.intelehealth.app.utilities.exception.DAOException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.function.Consumer

class UpcomingAppointmentsFragment : Fragment() {
    private var cardUpcomingAppointments: LinearLayout? = null
    private var layoutMainAppOptions: LinearLayout? = null
    private var layoutUpcoming: LinearLayout? = null
    private var layoutCancelled: LinearLayout? = null
    private var layoutCompleted: LinearLayout? = null
    private var rvUpcomingApp: RecyclerView? = null
    private var layoutParentAll: LinearLayout? = null
    private var sessionManager: SessionManager? = null
    private var db: SQLiteDatabase? = null
    private var ivRefresh: ImageView? = null
    private var ivClearText: ImageView? = null
    private var noDataFoundForUpcoming: View? = null
    private var autotvSearch: EditText? = null
    private var searchPatientText = ""
    private var currentDate = ""
    private var totalUpcomingApps = 0
    private var totalCancelled = 0
    private var totalCompleted = 0
    private var listener: UpdateAppointmentsCount? = null
    private var nsvToday: NestedScrollView? = null
    private val upcomingLimit = 15
    private var totalData = 0;
    private var offset = 0
    private var isUpcomingFullyLoaded = false
    private var upcomingAppointmentInfoList: MutableList<AppointmentInfo>? = null
    private val upcomingSearchList: MutableList<AppointmentInfo> = ArrayList()
    private var upcomingMyAppointmentsAdapter: UpcomingMyAppointmentsAdapter? = null
    private val disposables = CompositeDisposable()


    private var sortIm: ImageView? = null

    private lateinit var upcommingView: View

    private lateinit var myAppointmentLoadingListener: MyAppointmentLoadingListener
    fun setListener(myAppointmentLoadingListener: MyAppointmentLoadingListener){
        this.myAppointmentLoadingListener = myAppointmentLoadingListener
    }

    //true = ascending, false = descending
    private var sortStatus = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(context)
        if(::myAppointmentLoadingListener.isInitialized){
            myAppointmentLoadingListener.onStartUpcoming()
        }
        (activity as MyAppointmentActivityNew?)!!.initUpdateFragmentOnEvent(
            0,
            object : UpdateFragmentOnEvent {
                override fun onStart(eventFlag: Int) {
                    Log.v(TAG, "onStart")
                }

                override fun onFinished(eventFlag: Int) {
                    Log.v(TAG, "onFinished"+" "+eventFlag)
                    if(eventFlag == AppConstants.EVENT_FLAG_SUCCESS){
                        appointments
                    }

                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        setLocale(context)
        upcommingView = inflater.inflate(R.layout.fragment_upcoming_appointments, container, false)
        initUI()
        return upcommingView
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

    override fun onResume() {
        super.onResume()
        //getAppointments();
    }

    private fun initUI() {
        sessionManager = SessionManager(activity)
        val dateFormat1 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        currentDate = dateFormat1.format(Date())
        val language = sessionManager!!.appLanguage
        if (!language.equals("", ignoreCase = true)) {
            val locale = Locale(language)
            Locale.setDefault(locale)
            val config = Configuration()
            config.locale = locale
            requireActivity().resources.updateConfiguration(
                config,
                requireActivity().resources.displayMetrics
            )
        }
        sessionManager!!.currentLang = resources.configuration.locale.toString()
        db = IntelehealthApplication.inteleHealthDatabaseHelper.writableDatabase

        //initialize all the views
        rvUpcomingApp = upcommingView.findViewById(R.id.rv_upcoming_appointments)
        cardUpcomingAppointments = upcommingView.findViewById(R.id.card_upcoming_appointments)
        layoutMainAppOptions = upcommingView.findViewById(R.id.layout_main_app_options)
        layoutUpcoming = upcommingView.findViewById(R.id.layout_upcoming)
        layoutCancelled = upcommingView.findViewById(R.id.layout_cancelled)
        layoutCompleted = upcommingView.findViewById(R.id.layout_completed)
        layoutParentAll = upcommingView.findViewById(R.id.layout_parent_all)
        ivRefresh = requireActivity().findViewById(R.id.imageview_is_internet_common)

        sortIm = upcommingView.findViewById(R.id.sort_im)

        //no data found
        noDataFoundForUpcoming = upcommingView.findViewById(R.id.layout_no_data_found_upcoming)
        autotvSearch = upcommingView.findViewById(R.id.et_search)
        ivClearText = upcommingView.findViewById(R.id.iv_clear_today)
        ivClearText?.setOnClickListener {
            autotvSearch?.setText("")
            searchPatientText = ""
            resetData()
        }
        layoutMainAppOptions?.background = ContextCompat.getDrawable(
            requireActivity(),
            R.drawable.ui2_ic_bg_options_appointment
        )
        cardUpcomingAppointments?.background = ContextCompat.getDrawable(
            requireActivity(), R.drawable.ui2_bg_selcted_card
        )
        layoutUpcoming?.visibility = View.VISIBLE
        layoutCompleted?.visibility = View.VISIBLE
        layoutCancelled?.visibility = View.VISIBLE
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        params.weight = 1.0f
        params.gravity = Gravity.TOP
        layoutUpcoming?.layoutParams = params
        nsvToday = upcommingView.findViewById(R.id.nsv_today)

        sortIm?.setOnClickListener {
            sortList()
        }

        nsvToday?.setOnScrollChangeListener { v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            val view = v.getChildAt(v.childCount - 1)
            val bottom: Int = view.bottom - (v.height + v.scrollY)
            if (bottom == 0) {
                setMoreDataIntoUpcomingRecyclerView()
            }
        }
        searchPatient()
    }

    private fun sortList() {
        if ((upcomingAppointmentInfoList?.size ?: 0) > 1) {
            if (sortStatus) {
                showShortToast(requireActivity(), getString(R.string.sorted_by_descending_order))
            } else {
                showShortToast(requireActivity(), getString(R.string.sorted_by_ascending_order))
            }
            sortStatus = !sortStatus
            appointments
        }
    }


    private fun resetData() {
        upcomingSearchList.clear()
        appointments
    }

    private fun initLimits() {
        offset = 0
        isUpcomingFullyLoaded = false
    }

    /**
     * this function will call whenever we paginate the list
     */
    private fun setMoreDataIntoUpcomingRecyclerView() {
        if (upcomingSearchList.size > 0) {
            return
        }
        if ((activity as MyAppointmentActivityNew).totalUpcoming <= (upcomingAppointmentInfoList?.size?:0)) {
            return
        }
        showShortToast(requireActivity(),getString(R.string.loading_more))

        val upcomingAppointmentDisposable = getUpcomingDataObserver(DataLoadingType.PAGINATION)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ appointments ->
                if (appointments.size > 0) {
                    upcomingAppointmentInfoList!!.addAll(appointments)
                    upcomingMyAppointmentsAdapter!!.notifyDataSetChanged()
                    offset = upcomingAppointmentInfoList?.size?:0
                } else {
                    isUpcomingFullyLoaded = true
                }

            },
                { error ->
                    error.printStackTrace()
                })

        disposables.add(upcomingAppointmentDisposable)
    }

    private fun searchPatient() {
        autotvSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.toString().isNotEmpty()) {
                    ivClearText!!.visibility = View.VISIBLE
                } else {
                    searchPatientText = ""
                    appointments
                    ivClearText?.visibility = View.GONE
                }
            }
        })

        autotvSearch?.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchText = autotvSearch!!.text.toString()
                searchPatientText = if (searchText.isNotEmpty()) {
                    autotvSearch?.text.toString()
                }else{
                    ""
                }
                appointments
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    /**
     * to getting appointment for first time, from search, sort
     */
    private val appointments: Unit
        get() {
            //recyclerview for upcoming appointments
            val upcomingAppointmentDisposable = getUpcomingDataObserver(DataLoadingType.INITIAL)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ appointments ->
                    if (appointments.size > 0) {
                        rvUpcomingApp?.visibility = View.VISIBLE
                        noDataFoundForUpcoming?.visibility = View.GONE
                        totalUpcomingApps = appointments?.size ?: 0

                        upcomingMyAppointmentsAdapter =
                            UpcomingMyAppointmentsAdapter(requireActivity(), appointments)
                        rvUpcomingApp?.adapter = upcomingMyAppointmentsAdapter
                        offset = appointments.size
                    } else {
                        rvUpcomingApp?.visibility = View.GONE
                        noDataFoundForUpcoming?.visibility = View.VISIBLE
                    }

                    if(::myAppointmentLoadingListener.isInitialized){
                        myAppointmentLoadingListener.onStopUpcoming()
                    }
                },
                    { error ->
                        if(::myAppointmentLoadingListener.isInitialized){
                            myAppointmentLoadingListener.onStopUpcoming()
                        }
                        error.printStackTrace()
                    })

            disposables.add(upcomingAppointmentDisposable)
        }

    /**
     * getting upcoming appointment here
     */
    private fun getUpcomingDataObserver(dataLoadingType: DataLoadingType): Observable<MutableList<AppointmentInfo>> {
        return Observable.create {
            //if calling type is initial like first time or from search
            // then we are resetting the offset
            if (dataLoadingType == DataLoadingType.INITIAL){
                initLimits()
            }
            val tempList = AppointmentDAO().getUpcomingAppointments(
                upcomingLimit,
                offset,
                if (sortStatus) "ASC" else "DESC",
                searchPatientText
            )
            if ((tempList?.size ?: 0) > 0) {
                tempList?.forEach(Consumer { appointmentInfo: AppointmentInfo ->
                    val patientProfilePath = getPatientProfile(appointmentInfo.patientId)?:""
                    appointmentInfo.patientProfilePhoto = patientProfilePath
                })
            }
            //if type is initial then returning the actual list
            if (dataLoadingType == DataLoadingType.INITIAL){
                upcomingAppointmentInfoList = tempList
                if (upcomingAppointmentInfoList != null) {
                    it.onNext(upcomingAppointmentInfoList!!)
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


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is UpdateAppointmentsCount) {
            listener = context
            val totalTodayApps = totalUpcomingApps + totalCancelled + totalCompleted
            listener!!.updateCount("today", 1000)
        } else {
            throw RuntimeException("$context must implement OnFragmentCommunicationListener")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    companion object {
        private const val TAG = "TodaysMyAppointmentsFra"
        /*fun newInstance(): UpcomingAppointmentsFragment {
            return UpcomingAppointmentsFragment(myAppointmentLoadingListener)
        }*/
    }
}
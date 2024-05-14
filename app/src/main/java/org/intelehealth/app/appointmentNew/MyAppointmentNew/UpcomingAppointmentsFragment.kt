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
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.R
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.appointment.api.ApiClientAppointment
import org.intelehealth.app.appointment.dao.AppointmentDAO
import org.intelehealth.app.appointment.model.AppointmentInfo
import org.intelehealth.app.appointment.model.AppointmentListingResponse
import org.intelehealth.app.appointmentNew.TodaysMyAppointmentsAdapter
import org.intelehealth.app.appointmentNew.UpdateAppointmentsCount
import org.intelehealth.app.appointmentNew.UpdateFragmentOnEvent
import org.intelehealth.app.database.dao.EncounterDAO
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.utilities.DateAndTimeUtils
import org.intelehealth.app.utilities.NavigationUtils
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.ToastUtil
import org.intelehealth.app.utilities.exception.DAOException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.function.Consumer

class UpcomingAppointmentsFragment : Fragment() {
    var cardUpcomingAppointments: LinearLayout? = null
    var layoutMainAppOptions: LinearLayout? = null
    var layoutUpcoming: LinearLayout? = null
    var layoutCancelled: LinearLayout? = null
    var layoutCompleted: LinearLayout? = null
    var rvUpcomingApp: RecyclerView? = null
    var layoutParentAll: LinearLayout? = null
    var sessionManager: SessionManager? = null
    private var db: SQLiteDatabase? = null
    var ivRefresh: ImageView? = null
    var ivClearText: ImageView? = null
    var noDataFoundForUpcoming: View? = null
    var autotvSearch: EditText? = null
    var searchPatientText = ""
    var currentDate = ""
    var totalUpcomingApps = 0
    var totalCancelled = 0
    var totalCompleted = 0
    private var listener: UpdateAppointmentsCount? = null
    private var nsvToday: NestedScrollView? = null
    private val upcomingLimit = 15
    private val completedLimit = 15
    private val cancelledLimit = 15
    private var upcomingStart = 0
    private var upcomingEnd = upcomingStart + upcomingLimit
    private var completedStart = 0
    private var completedEnd = completedStart + completedLimit
    private var cancelledStart = 0
    private var cancelledEnd = cancelledStart + cancelledLimit
    private var isUpcomingFullyLoaded = false
    private var upcomingAppointmentInfoList: MutableList<AppointmentInfo>? = null
    private val upcomingSearchList: MutableList<AppointmentInfo> = ArrayList()
    private var upcomingMyAppointmentsAdapter: UpcomingMyAppointmentsAdapter? = null

    private var sortIm: ImageView? = null

    lateinit var upcommingView: View

    //true = ascending, false = descending
    var sortStatus = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(context)
        (activity as MyAppointmentActivityNew?)!!.initUpdateFragmentOnEvent(
            0,
            object : UpdateFragmentOnEvent {
                override fun onStart(eventFlag: Int) {
                    Log.v(TAG, "onStart")
                }

                override fun onFinished(eventFlag: Int) {
                    Log.v(TAG, "onFinished")
                    initLimits()
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
        ivClearText?.setOnClickListener(View.OnClickListener { v: View? ->
            autotvSearch?.setText("")
            searchPatientText = ""
            resetData()
        })
        layoutMainAppOptions?.setBackground(
            ContextCompat.getDrawable(
                requireActivity(),
                R.drawable.ui2_ic_bg_options_appointment
            )
        )
        cardUpcomingAppointments?.setBackground(
            ContextCompat.getDrawable(
                requireActivity(), R.drawable.ui2_bg_selcted_card
            )
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

        nsvToday?.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (v.getChildAt(v.childCount - 1) != null) {
                if (scrollY > oldScrollY) {
                    if (upcomingAppointmentInfoList != null && upcomingAppointmentInfoList!!.size == 0) {
                        isUpcomingFullyLoaded = true
                    }
                    if (!isUpcomingFullyLoaded) {
                        setMoreDataIntoUpcomingRecyclerView()
                    }
                    /*if (isUpcomingFullyLoaded && !isCancelledFullyLoaded) {
                        setMoreDataIntoCancelledRecyclerView()
                    }
                    if (scrollY >= v.getChildAt(v.childCount - 1).measuredHeight - v.measuredHeight) {
                        if (completedAppointmentInfoList != null && completedAppointmentInfoList!!.size == 0) {
                            isCompletedFullyLoaded = true
                            return@OnScrollChangeListener
                        }
                        if (!isCompletedFullyLoaded) {
                            setMoreDataIntoCompletedRecyclerView()
                        }
                    }*/
                }
            }
        })
        searchPatient()
        //getSlots();
    }

    private fun sortList() {
        if ((upcomingAppointmentInfoList?.size ?: 0) > 1) {
            if (sortStatus) {
                upcomingAppointmentInfoList?.sortWith(compareByDescending<AppointmentInfo> { it.slotDate }.thenByDescending { it.slotTime })
                ToastUtil.showShortToast(requireActivity(), "Ds")
            } else {
                upcomingAppointmentInfoList?.sortWith(compareBy({ it.slotDate }, { it.slotTime }))
                ToastUtil.showShortToast(requireActivity(), "As")
            }
            upcomingMyAppointmentsAdapter =
                UpcomingMyAppointmentsAdapter(activity, upcomingAppointmentInfoList, "upcoming")
            rvUpcomingApp?.adapter = upcomingMyAppointmentsAdapter
            sortStatus = !sortStatus
        }
    }

    fun stringToTimestampUsingSimpleDateFormat(datetimeString: String, format: String): Long {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        val date = dateFormat.parse(datetimeString)
        return date?.time ?: 0L
    }

    private fun resetData() {
        upcomingSearchList.clear()
        initLimits()
        appointments
    }

    private fun initLimits() {
        upcomingStart = 0
        cancelledStart = 0
        completedStart = 0
        upcomingEnd = upcomingStart + upcomingLimit
        cancelledEnd = cancelledStart + cancelledLimit
        completedEnd += completedLimit
    }

    private fun setMoreDataIntoUpcomingRecyclerView() {
        if (upcomingSearchList.size > 0) {
            return
        }
        if (upcomingAppointmentInfoList != null && upcomingAppointmentInfoList!!.size == 0) {
            isUpcomingFullyLoaded = true
            return
        }
        val tempList = AppointmentDAO().getUpcomingAppointmentsForToday(
            currentDate,
            upcomingLimit,
            upcomingStart
        )

        if (tempList.size > 0) {
            upcomingAppointmentInfoList!!.addAll(tempList)
            if(sortStatus){
                upcomingAppointmentInfoList?.sortWith(compareByDescending<AppointmentInfo> { it.slotDate }.thenByDescending { it.slotTime })
            }else{
                upcomingAppointmentInfoList?.sortWith(compareBy({ it.slotDate }, { it.slotTime }))
            }
            upcomingMyAppointmentsAdapter!!.notifyDataSetChanged()
            upcomingStart = upcomingEnd
            upcomingEnd += upcomingLimit
        }
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
                    ivClearText!!.visibility = View.GONE
                }
            }
        })

        autotvSearch?.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchText = autotvSearch?.text.toString()
                if (searchText.isNotEmpty()) {
                    searchOperation(searchText)
                } else {
                    appointments
                }
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private val appointments: Unit
        get() {
            upcomingAppointments
        }
    private val upcomingAppointments: Unit
        get() {
            //recyclerview for upcoming appointments
            upcomingAppointmentInfoList = AppointmentDAO().getUpcomingAppointmentsForToday(
                currentDate,
                upcomingLimit,
                upcomingStart
            )
            upcomingAppointmentInfoList?.sortWith(compareByDescending<AppointmentInfo> { it.slotDate }.thenByDescending { it.slotTime })

            if ((upcomingAppointmentInfoList?.size ?: 0) > 0) {
                rvUpcomingApp?.visibility = View.VISIBLE
                noDataFoundForUpcoming?.visibility = View.GONE
                totalUpcomingApps = upcomingAppointmentInfoList?.size ?: 0
                upcomingAppointmentInfoList?.forEach(Consumer { appointmentInfo: AppointmentInfo ->
                    val patientProfilePath = getPatientProfile(appointmentInfo.patientId)
                    appointmentInfo.patientProfilePhoto = patientProfilePath
                })
                upcomingMyAppointmentsAdapter =
                    UpcomingMyAppointmentsAdapter(activity, upcomingAppointmentInfoList, "upcoming")
                rvUpcomingApp?.adapter = upcomingMyAppointmentsAdapter
                upcomingStart = upcomingEnd
                upcomingEnd += upcomingLimit
            } else {
                rvUpcomingApp?.visibility = View.GONE
                noDataFoundForUpcoming?.visibility = View.VISIBLE
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

    private val slots: Unit
        get() {
            val serverUrl = BuildConfig.SERVER_URL + ":3004"
            ApiClientAppointment.getInstance(serverUrl).api.getSlotsAll(
                DateAndTimeUtils.getCurrentDateInDDMMYYYYFormat(),
                DateAndTimeUtils.getCurrentDateInDDMMYYYYFormat(),
                SessionManager(
                    activity
                ).locationUuid
            ).enqueue(object : Callback<AppointmentListingResponse?> {

                override fun onResponse(
                    call: Call<AppointmentListingResponse?>,
                    response: Response<AppointmentListingResponse?>,
                ) {
                    if (response.body() == null) return
                    val slotInfoResponse = response.body()
                    val appointmentDAO = AppointmentDAO()
                    appointmentDAO.deleteAllAppointments()
                    for (i in slotInfoResponse!!.data.indices) {
                        try {
                            appointmentDAO.insert(slotInfoResponse.data[i])
                        } catch (e: DAOException) {
                            e.printStackTrace()
                        }
                    }
                    /*if (slotInfoResponse.getCancelledAppointments() != null) {
                            if (slotInfoResponse != null && slotInfoResponse.getCancelledAppointments().size() > 0) {
                                for (int i = 0; i < slotInfoResponse.getCancelledAppointments().size(); i++) {
                                    try {
                                        appointmentDAO.insert(slotInfoResponse.getCancelledAppointments().get(i));

                                    } catch (DAOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else {
                        }*/appointments
                }

                override fun onFailure(call: Call<AppointmentListingResponse?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                    //log out operation if response code is 401
                    NavigationUtils().logoutOperation(activity, t)
                }
            })
        }

    private fun searchOperation(query: String) {
        var query = query
        query = query.lowercase(Locale.getDefault()).trim { it <= ' ' }
        query = query.replace(" {2}".toRegex(), " ")
        val finalQuery = query
        Thread {
            val allUpcomingList = AppointmentDAO().getAllUpcomingAppointmentsForToday(currentDate)
            if (!finalQuery.isEmpty()) {
                upcomingSearchList.clear()
                if (allUpcomingList.size > 0) {
                    for (info in allUpcomingList) {
                        val patientName = info.patientName.lowercase(Locale.getDefault())
                        if (patientName.contains(finalQuery) || patientName.equals(
                                finalQuery,
                                ignoreCase = true
                            )
                        ) {
                            upcomingSearchList.add(info)
                        }
                    }
                }
                requireActivity().runOnUiThread {
                    upcomingMyAppointmentsAdapter =
                        UpcomingMyAppointmentsAdapter(activity, upcomingSearchList, "upcoming")
                    rvUpcomingApp!!.isNestedScrollingEnabled = true
                    rvUpcomingApp!!.adapter = upcomingMyAppointmentsAdapter
                }
            }
        }.start()
    }

    companion object {
        private const val TAG = "TodaysMyAppointmentsFra"
        fun newInstance(): UpcomingAppointmentsFragment {
            return UpcomingAppointmentsFragment()
        }
    }
}
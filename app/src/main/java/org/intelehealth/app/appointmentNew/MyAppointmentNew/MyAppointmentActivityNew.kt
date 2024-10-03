package org.intelehealth.app.appointmentNew.MyAppointmentNew

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.R
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.appointment.api.ApiClientAppointment
import org.intelehealth.app.appointment.dao.AppointmentDAO
import org.intelehealth.app.appointment.model.AppointmentListingResponse
import org.intelehealth.app.appointmentNew.UpdateAppointmentsCount
import org.intelehealth.app.appointmentNew.UpdateFragmentOnEvent
import org.intelehealth.app.enums.AppointmentTabType
import org.intelehealth.app.shared.BaseActivity
import org.intelehealth.app.utilities.CustomLog
import org.intelehealth.app.utilities.DateAndTimeUtils
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.MyAppointmentLoadingListener
import org.intelehealth.app.utilities.NavigationUtils
import org.intelehealth.app.utilities.NetworkConnection
import org.intelehealth.app.utilities.NetworkUtils
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.exception.DAOException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyAppointmentActivityNew : BaseActivity(), UpdateAppointmentsCount,
    NetworkUtils.InternetCheckUpdateInterface, MyAppointmentLoadingListener {
    var totalUpcoming: Int = -1
    var totalPast: Int = -1
    private lateinit var loadingDialog: androidx.appcompat.app.AlertDialog
    private var bottomNav: BottomNavigationView? = null
    private var tabLayout: TabLayout? = null
    var viewPager: ViewPager2? = null
    var networkUtils: NetworkUtils? = null
    private val syncAnimator: ObjectAnimator? = null
    private var mIsInternetAvailable = false
    private val mUpdateFragmentOnEventHashMap = HashMap<Int, UpdateFragmentOnEvent>()
    private var onStartUpcoming = false
    private var onStartPast = false
    var disposable = CompositeDisposable()

    fun initUpdateFragmentOnEvent(tab: Int, listener: UpdateFragmentOnEvent) {
        Log.v(TAG, "initUpdateFragmentOnEvent")
        mUpdateFragmentOnEventHashMap[tab] = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_appointment_new_ui2)
        networkUtils = NetworkUtils(this@MyAppointmentActivityNew, this)
        initUI()
    }

    override fun onResume() {
        super.onResume()
        loadAllAppointments()
    }

    private fun loadAllAppointments() {
        Log.v(TAG, "loadAllAppointments")
        val baseurl = BuildConfig.SERVER_URL + ":3004"
        var tabIndex = 0
        if (!isFinishing && !isDestroyed) {
            tabIndex = tabLayout?.selectedTabPosition ?: 0
        } else {
            try {
                Thread.sleep(2000)
                loadAllAppointments()
                return
            } catch (_: Exception) {
            }
        }
        if (mUpdateFragmentOnEventHashMap.containsKey(tabIndex))
            mUpdateFragmentOnEventHashMap[tabIndex]?.onFinished(AppConstants.EVENT_FLAG_START)
        if (NetworkConnection.isCapableNetwork(this)) {
            ApiClientAppointment.getInstance(baseurl).api
                .getSlotsAll(
                    DateAndTimeUtils.getCurrentDateInDDMMYYYYFormat(),
                    DateAndTimeUtils.getOneMonthAheadDateInDDMMYYYYFormat(),
                    SessionManager(this).locationUuid
                )
                .enqueue(object : Callback<AppointmentListingResponse?> {
                    override fun onResponse(
                        call: Call<AppointmentListingResponse?>,
                        response: Response<AppointmentListingResponse?>,
                    ) {
                        if (response.body() == null) return
                        Log.v(TAG, "onResponse - " + Gson().toJson(response.body()))
                        val slotInfoResponse = response.body()
                        val appointmentDAO = AppointmentDAO()
                        appointmentDAO.deleteAllAppointments()
                        if (slotInfoResponse!!.data.size > 0) {
                            for (i in slotInfoResponse.data.indices) {
                                try {
                                    appointmentDAO.insert(slotInfoResponse.data[i])
                                } catch (e: DAOException) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        Log.v(TAG, "onFinished - " + Gson().toJson(slotInfoResponse))
                        if (!isFinishing && !isDestroyed) {
                            setTabCount()
                        }

                        mUpdateFragmentOnEventHashMap[tabIndex]?.onFinished(AppConstants.EVENT_FLAG_SUCCESS)
                    }

                    override fun onFailure(call: Call<AppointmentListingResponse?>, t: Throwable) {
                        Log.v("onFailure", t.message!!)
                        mUpdateFragmentOnEventHashMap[tabIndex]?.onFinished(AppConstants.EVENT_FLAG_FAILED)
                        //log out operation if response code is 401
                        NavigationUtils().logoutOperation(this@MyAppointmentActivityNew, t)
                    }
                })
        } else {
            mUpdateFragmentOnEventHashMap[tabIndex]?.onFinished(AppConstants.EVENT_FLAG_FAILED)
        }
    }

    private fun initUI() {
        val toolbar = findViewById<View>(R.id.toolbar_my_appointments)
        val tvTitle = toolbar.findViewById<TextView>(R.id.tv_screen_title_common)
        val ivBackArrow = toolbar.findViewById<ImageView>(R.id.iv_back_arrow_common)
        tvTitle.text = resources.getString(R.string.my_appointments)

        ivBackArrow.setOnClickListener { v: View? ->
            val intent = Intent(this@MyAppointmentActivityNew, HomeScreenActivity_New::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        configureTabLayout()

        WindowCompat.getInsetsController(window,window.decorView).isAppearanceLightStatusBars = true
        window.statusBarColor = Color.WHITE

        bottomNav = findViewById(R.id.bottom_nav_my_appointments)
        bottomNav?.setOnItemSelectedListener(navigationItemSelectedListener)
        bottomNav?.itemIconTintList = null
        bottomNav?.menu?.findItem(R.id.bottom_nav_home_menu)?.isChecked = false
    }

    private fun setTabCount() {
        val upcomingAndPastCountObserver = getCountObserver(AppointmentTabType.UPCOMING)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .concatMap {
                tabLayout?.getTabAt(0)?.text = getString(R.string.upcoming) + " (" + it + ")"
                totalUpcoming = it
                getCountObserver(AppointmentTabType.PAST)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    tabLayout?.getTabAt(1)?.text = getString(R.string.past) + " (" + it + ")"
                    totalPast = it
                },
                { error ->
                    error.printStackTrace()
                }
            )
        disposable.add(upcomingAndPastCountObserver)
    }

    private fun getCountObserver(appointmentTabType: AppointmentTabType): Observable<Int> {
        val appointmentDao = AppointmentDAO()
        return Observable.create<Int> { emitter ->
            val count = appointmentDao.getAppointmentCountsByStatus(appointmentTabType)
            emitter.onNext(count)
            emitter.onComplete()
        }
    }

    private fun configureTabLayout() {
        tabLayout = findViewById(R.id.tablayout_appointments)
        viewPager =
            findViewById(R.id.pager_appointments)
        val adapter =
            NewMyAppointmentsPagerAdapter(supportFragmentManager, 2, this@MyAppointmentActivityNew)
        viewPager?.adapter = adapter
        viewPager?.offscreenPageLimit = adapter.itemCount - 1

        TabLayoutMediator(
            tabLayout!!,
            viewPager!!
        ) { tab, position ->
            if (position == 0) {
                tab.text = getString(R.string.upcoming)
            } else {
                tab.text = getString(R.string.past)
            }
        }.attach()
        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager?.currentItem = tab.position
                loadAllAppointments()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        viewPager?.getAdapter()?.notifyDataSetChanged()
    }

    private var navigationItemSelectedListener = NavigationBarView.OnItemSelectedListener { item ->
        var fragment: Fragment
        when (item.itemId) {
            R.id.bottom_nav_home_menu -> return@OnItemSelectedListener true

            R.id.bottom_nav_achievements -> return@OnItemSelectedListener true

            R.id.bottom_nav_help -> return@OnItemSelectedListener true

            R.id.bottom_nav_add_patient -> return@OnItemSelectedListener true
        }
        false
    }

    override fun updateCount(whichFrag: String, count: Int) {

    }

    override fun onStop() {
        super.onStop()
        try {
            //unregister receiver for internet check
            networkUtils?.unregisterNetworkReceiver()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }

        if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        //register receiver for internet check
        try {
            networkUtils?.callBroadcastReceiver()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    override fun onStartUpcoming() {
        if (!onStartPast) {
            try {
                if (!isFinishing && !isDestroyed) {
                    loadingDialog = DialogUtils().showCommonLoadingDialog(
                        this,
                        getString(R.string.loading),
                        getString(R.string.please_wait)
                    )
                }
            } catch (e: Exception) {
                CustomLog.d("EEEEE", "" + e.message)
            }
        }
        onStartUpcoming = true
    }

    override fun onStartPast() {
        if (!onStartUpcoming) {
            try {
                if (!isFinishing && !isDestroyed) {
                    loadingDialog = DialogUtils().showCommonLoadingDialog(
                        this,
                        getString(R.string.loading),
                        getString(R.string.please_wait)
                    )
                }
            } catch (e: Exception) {
                CustomLog.d("EEEEE", "" + e.message)
            }
        }
        onStartPast = true
    }

    override fun onStopUpcoming() {
        if (loadingDialog.isShowing) {
            try {
                if (!isFinishing && !isDestroyed) {
                    loadingDialog.dismiss()
                }
            } catch (e: Exception) {
                CustomLog.d("EEEEE", "" + e.message)
            }
        }

    }

    override fun onStopPast() {
        if (loadingDialog.isShowing) {
            try {
                if (!isFinishing && !isDestroyed) {
                    loadingDialog.dismiss()
                }
            } catch (e: Exception) {
                CustomLog.d("EEEEE_DIS", "" + e.message)
            }
        }

    }

    //update ui as per internet availability
    override fun updateUIForInternetAvailability(isInternetAvailable: Boolean) {
        mIsInternetAvailable = isInternetAvailable
        /*if (isInternetAvailable) {
            ivIsInternet?.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ui2_ic_internet_available
                )
            )
        } else {
            ivIsInternet?.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ui2_ic_no_internet
                )
            )
        }*/
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    companion object {
        private const val TAG = "MyAppointmentActivity"
    }
}
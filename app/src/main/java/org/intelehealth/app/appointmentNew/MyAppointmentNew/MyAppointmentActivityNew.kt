package org.intelehealth.app.appointmentNew.MyAppointmentNew

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
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
import org.intelehealth.app.syncModule.SyncUtils
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
import java.util.Objects

class MyAppointmentActivityNew : BaseActivity(), UpdateAppointmentsCount,
    NetworkUtils.InternetCheckUpdateInterface, MyAppointmentLoadingListener {
    lateinit var loadingDialog: androidx.appcompat.app.AlertDialog
    var bottomNav: BottomNavigationView? = null
    var tabLayout: TabLayout? = null
    var viewPager: ViewPager2? = null
    var fromFragment = ""
    var totalCount = 0
    var networkUtils: NetworkUtils? = null
    var ivIsInternet: ImageView? = null
    private val syncAnimator: ObjectAnimator? = null
    private var mIsInternetAvailable = false
    private val mUpdateFragmentOnEventHashMap = HashMap<Int, UpdateFragmentOnEvent>()
    private var onStartUpcoming = false
    private var onStartPast = false
    var disposable =  CompositeDisposable()

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
        val tabIndex = tabLayout!!.selectedTabPosition
        if (mUpdateFragmentOnEventHashMap.containsKey(tabIndex)) Objects.requireNonNull(
            mUpdateFragmentOnEventHashMap[tabIndex]
        )?.onFinished(AppConstants.EVENT_FLAG_START)
        if(NetworkConnection.isCapableNetwork(this)){
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

                        /*if (slotInfoResponse.getCancelledAppointments() != null) {
                                if (slotInfoResponse.getCancelledAppointments().size() > 0) {

                                    for (int i = 0; i < slotInfoResponse.getCancelledAppointments().size(); i++) {

                                        try {
                                            appointmentDAO.insert(slotInfoResponse.getCancelledAppointments().get(i));

                                        } catch (DAOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }*/

                        //getAppointments();
                        Log.v(TAG, "onFinished - " + Gson().toJson(slotInfoResponse))
                        setTabCount()
                        Objects.requireNonNull(
                            mUpdateFragmentOnEventHashMap[tabIndex]
                        )?.onFinished(AppConstants.EVENT_FLAG_SUCCESS)
                    }

                    override fun onFailure(call: Call<AppointmentListingResponse?>, t: Throwable) {
                        Log.v("onFailure", t.message!!)
                        Objects.requireNonNull(
                            mUpdateFragmentOnEventHashMap[tabIndex]
                        )?.onFinished(AppConstants.EVENT_FLAG_FAILED)
                        //log out operation if response code is 401
                        NavigationUtils().logoutOperation(this@MyAppointmentActivityNew, t)
                    }
                })
        }else{
            Objects.requireNonNull(
                mUpdateFragmentOnEventHashMap[tabIndex]
            )?.onFinished(AppConstants.EVENT_FLAG_FAILED)
        }
    }

    private fun initUI() {
        val toolbar = findViewById<View>(R.id.toolbar_my_appointments)
        val tvTitle = toolbar.findViewById<TextView>(R.id.tv_screen_title_common)
        ivIsInternet = toolbar.findViewById(R.id.imageview_is_internet_common)
        val ivBackArrow = toolbar.findViewById<ImageView>(R.id.iv_back_arrow_common)
        tvTitle.text = resources.getString(R.string.my_appointments)

        ivIsInternet?.setOnClickListener(View.OnClickListener { v: View? ->
            SyncUtils.syncNow(
                this@MyAppointmentActivityNew,
                ivIsInternet,
                syncAnimator
            )
        })
        ivBackArrow.setOnClickListener { v: View? ->
            val intent = Intent(this@MyAppointmentActivityNew, HomeScreenActivity_New::class.java)
            startActivity(intent)
        }

        configureTabLayout()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
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
                getCountObserver(AppointmentTabType.PAST)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe (
                {
                    tabLayout?.getTabAt(1)?.text = getString(R.string.past) + " (" + it + ")"
                },
                {error->
                    error.printStackTrace()
                }
            )
        disposable.add(upcomingAndPastCountObserver)
    }

    private fun getCountObserver(appointmentTabType: AppointmentTabType): Observable<Int> {
        val appointmentDao = AppointmentDAO()
        return Observable.create<Int> {emitter->
            val count = appointmentDao.getAppointmentCountsByStatus(appointmentTabType)
            emitter.onNext(count)
            emitter.onComplete()
        }
    }

    private fun configureTabLayout() {
        tabLayout = findViewById(R.id.tablayout_appointments)

        /* tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.todays)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.all_appointments)));*/viewPager =
            findViewById(R.id.pager_appointments)
        val adapter =
            NewMyAppointmentsPagerAdapter(supportFragmentManager, 2, this@MyAppointmentActivityNew)
        viewPager?.adapter = adapter
        viewPager?.offscreenPageLimit = adapter.itemCount - 1

        // int limit = (adapter.getCount() > 1 ? adapter.getCount() - 1 : 1);

        //viewPager.setOffscreenPageLimit(limit);

        /*viewPager.addOnPageChangeListener(new
                TabLayout.TabLayoutOnPageChangeListener(tabLayout));*/TabLayoutMediator(
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
                /*  Log.d(TAG, "onTabSelected:position : : " + tab.getPosition());
                if (fromFragment != null && !fromFragment.isEmpty() && fromFragment.equals("today")) {
                    if (tab.getPosition() == 0) {
                        tab.setText("Today's (" + totalCount + ")");

                    }
                } else if (fromFragment != null && !fromFragment.isEmpty() && fromFragment.equals("all")) {
                    if (tab.getPosition() == 1) {
                        tab.setText("All appointments (" + totalCount + ")");

                    }

                }*/loadAllAppointments()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        Objects.requireNonNull(viewPager?.getAdapter()).notifyDataSetChanged()
    }

    var navigationItemSelectedListener = NavigationBarView.OnItemSelectedListener { item ->
        var fragment: Fragment
        when (item.itemId) {
            R.id.bottom_nav_home_menu ->                            /* Log.d(TAG, "onNavigationItemSelected: bottom_nav_home_menu");
                            tvTitleHomeScreenCommon.setText(getResources().getString(R.string.title_home_screen));
                            fragment = new HomeFragment_New();
                            loadFragment(fragment);*/return@OnItemSelectedListener true

            R.id.bottom_nav_achievements ->                          /*   tvTitleHomeScreenCommon.setText(getResources().getString(R.string.my_achievements));
                            fragment = new MyAchievementsFragment();
                            loadFragmentForBottomNav(fragment);*/return@OnItemSelectedListener true

            R.id.bottom_nav_help ->                        /*     tvTitleHomeScreenCommon.setText(getResources().getString(R.string.help));
                            fragment = new HelpFragment_New();
                            loadFragmentForBottomNav(fragment);*/return@OnItemSelectedListener true

            R.id.bottom_nav_add_patient -> return@OnItemSelectedListener true
        }
        false
    }

    override fun updateCount(whichFrag: String, count: Int) {
        //  Log.d(TAG, "updateCount:selected tab : " + tabLayout.getSelectedTabPosition());

        //  Log.d(TAG, "updateCount: count : " + count);

        /*   fromFragment = whichFrag;
        totalCount = count;*/

        /*        new TabLayoutMediator(tabLayout, viewPager,
                (TabLayout.Tab tab, int position) -> {
                    if (position == 0)
                        tab.setText("Received (" + count + ")").setIcon(R.drawable.presc_tablayout_icon);
                    else
                        tab.setText("Pending (" + count + ")").setIcon(R.drawable.presc_tablayout_icon);

                }
        ).attach();*/
    }

    override fun onStop() {
        super.onStop()
        try {
            //unregister receiver for internet check
            networkUtils!!.unregisterNetworkReceiver()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    override fun onStart() {
        super.onStart()
        //register receiver for internet check
        networkUtils!!.callBroadcastReceiver()
    }

    override fun onStartUpcoming() {
        if (!onStartPast) {
            loadingDialog = DialogUtils().showCommonLoadingDialog(
                this,
                getString(R.string.loading),
                getString(R.string.please_wait)
            )
        }
        onStartUpcoming = true
    }

    override fun onStartPast() {
        if (!onStartUpcoming) {
            loadingDialog = DialogUtils().showCommonLoadingDialog(
                this,
                getString(R.string.loading),
                getString(R.string.please_wait)
            )
        }
        onStartPast = true
    }

    override fun onStopUpcoming() {
        if (loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }

    }

    override fun onStopPast() {
        if (loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }

    }

    //update ui as per internet availability
    override fun updateUIForInternetAvailability(isInternetAvailable: Boolean) {
        mIsInternetAvailable = isInternetAvailable
        if (isInternetAvailable) {
            ivIsInternet!!.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ui2_ic_internet_available
                )
            )
        } else {
            ivIsInternet!!.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ui2_ic_no_internet
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    companion object {
        private const val TAG = "MyAppointmentActivity"
    }
}
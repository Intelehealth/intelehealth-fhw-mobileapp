package org.intelehealth.app.activities.notification;

import static org.intelehealth.app.database.dao.EncounterDAO.check_visit_is_VISIT_COMPLETE_ENC;
import static org.intelehealth.app.database.dao.NotificationDAO.deleteNotification;
import static org.intelehealth.app.database.dao.NotificationDAO.fetchAllFrom_NotificationTbl;
import static org.intelehealth.app.database.dao.NotificationDAO.insertNotifications;
import static org.intelehealth.app.database.dao.NotificationDAO.showOnly_NonDeletedNotification;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.DocumentObject;
import org.intelehealth.app.models.NotificationModel;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.exception.DAOException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Prajwal Waingankar on 27/09/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class NotificationActivity extends BaseActivity implements AdapterInterface, NetworkUtils.InternetCheckUpdateInterface {
    private SessionManager sessionManager;
    private SQLiteDatabase db;
    private ImageButton backbtn, clearAll_btn, refresh, filter, arrow_right;
    private RecyclerView recycler_today, recycler_yesterday;
    private TextView notifi_header_title, today_nodata, yesterday_nodata, reminder, incomplete_act, archieved_notifi;
    private NotificationAdapter adapter;
    public static final String TAG = NotificationActivity.class.getSimpleName();
    private FrameLayout filter_framelayout;
    private List<NotificationModel> todayPresc_list, yesterdayPresc_list;
    private NetworkUtils networkUtils;
    private ObjectAnimator syncAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        //In case of crash still the app should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());
        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        initViews();
        networkUtils = new NetworkUtils(this, this);
        viewsActions();
        clickListeners();
    }

    public void syncNow(View view) {
        if (NetworkConnection.isOnline(this)) {
            new SyncUtils().syncBackground();
            //Toast.makeText(this, getString(R.string.sync_strated), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(setLocale(newBase));
    }

    public Context setLocale(Context context) {
        SessionManager sessionManager1 = new SessionManager(context);
        String appLanguage = sessionManager1.getAppLanguage();
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        conf.setLocale(locale);
        context.createConfigurationContext(conf);
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
        return context;
    }

    private void showNotifications() {
        try {
            todays_Presc_notification();
            yesterdays_Presc_notification();
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    private void viewsActions() {
        showNotifications();
        count();
    }

    private void count() {
        //  int total_presc_count = todayPresc_list.size() + yesterdayPresc_list.size();
        int total_presc_count = 0;
        total_presc_count = adapter != null ? adapter.getItemCount() : total_presc_count;
        String prescCount = String.format(getString(R.string.five_presc_received, String.valueOf(total_presc_count)));
        notifi_header_title.setText(prescCount);
    }

    private void initViews() {
        backbtn = findViewById(R.id.backbtn);
        clearAll_btn = findViewById(R.id.clearAll_btn);
        arrow_right = findViewById(R.id.arrow_right);
        notifi_header_title = findViewById(R.id.notifi_header_title);
        refresh = findViewById(R.id.refresh);
        filter = findViewById(R.id.filter);
        reminder = findViewById(R.id.reminder);
        incomplete_act = findViewById(R.id.incomplete_act);
        archieved_notifi = findViewById(R.id.archieved_notifi);
        recycler_today = findViewById(R.id.recycler_today);
        recycler_yesterday = findViewById(R.id.recycler_yesterday);
        filter_framelayout = findViewById(R.id.filter_framelayout);
        today_nodata = findViewById(R.id.today_nodata);
        yesterday_nodata = findViewById(R.id.yesterday_nodata);
    }

    private void clickListeners() {
        backbtn.setOnClickListener(v -> {
            finish();
        });

        clearAll_btn.setOnClickListener(v -> {
            // clears the recyclerview for both today and yesterday.
            todayPresc_list.clear();
            yesterdayPresc_list.clear();

            today_nodata.setVisibility(View.VISIBLE);
            yesterday_nodata.setVisibility(View.VISIBLE);

            adapter.notifyDataSetChanged();
        });

        refresh.setOnClickListener(v -> {
            SyncUtils.syncNow(NotificationActivity.this, refresh, syncAnimator);
        });

        filter.setOnClickListener(v -> {
            // filter options
            if (filter_framelayout.getVisibility() == View.VISIBLE)
                filter_framelayout.setVisibility(View.GONE);
            else filter_framelayout.setVisibility(View.VISIBLE);
        });

        reminder.setOnClickListener(v -> {
            // filter options
            if (filter_framelayout.getVisibility() == View.VISIBLE)
                filter_framelayout.setVisibility(View.GONE);
            else filter_framelayout.setVisibility(View.VISIBLE);
        });
        incomplete_act.setOnClickListener(v -> {
            // filter options
            if (filter_framelayout.getVisibility() == View.VISIBLE)
                filter_framelayout.setVisibility(View.GONE);
            else filter_framelayout.setVisibility(View.VISIBLE);
        });
        archieved_notifi.setOnClickListener(v -> {
            // filter options
            if (filter_framelayout.getVisibility() == View.VISIBLE)
                filter_framelayout.setVisibility(View.GONE);
            else filter_framelayout.setVisibility(View.VISIBLE);
        });

        arrow_right.setOnClickListener(v -> {
            // call api and pass mobile no and presc link so that this link can be passed to all the users.
            apicall_tosend_presclink();
        });

    }

    private void apicall_tosend_presclink() {
        // TODO: need to implement this later.
    }

    private void todays_Presc_notification() throws DAOException {
        // current date
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        String currentDate = dateFormat.format(cal.getTime());
        Log.v("Notifi_Activity", "todaysDate: " + currentDate);

        todayPresc_list = check_visit_is_VISIT_COMPLETE_ENC(currentDate);
        List<NotificationModel> list = new ArrayList<>();
        boolean value = false;
        for (NotificationModel model : todayPresc_list) {
            value = fetchAllFrom_NotificationTbl(model);
            if (!value) list.add(model);
        }
        todayPresc_list.clear();
        todayPresc_list.addAll(list);
        insertNotifications(todayPresc_list);

        if (todayPresc_list.size() <= 0) {
            today_nodata.setVisibility(View.VISIBLE);
        } else {
            Date c = Calendar.getInstance().getTime();

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String todaysDate = df.format(c);
            System.out.println("Current time => " + todaysDate); // todays date: 2023-01-05

            List<NotificationModel> todaysList = new ArrayList<>();
            for (NotificationModel notificationModel : todayPresc_list) {
                todaysList.add(showOnly_NonDeletedNotification(notificationModel, todaysDate));
            }

            today_nodata.setVisibility(View.GONE);
            adapter = new NotificationAdapter(this, todaysList, this);
            recycler_today.setAdapter(adapter);
        }

    }

    private void yesterdays_Presc_notification() throws DAOException {
        // yesterdays date
        DateFormat dateFormat_yesterday = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal_yesterday = Calendar.getInstance();
        cal_yesterday.add(Calendar.DATE, -1);
        String yesterdayDate = dateFormat_yesterday.format(cal_yesterday.getTime());
        Log.v("Notifi_Activity", "yesterdaysDate: " + yesterdayDate);

        yesterdayPresc_list = check_visit_is_VISIT_COMPLETE_ENC(yesterdayDate);
        List<NotificationModel> list = new ArrayList<>();
        boolean value = false;
        for (NotificationModel model : yesterdayPresc_list) {
            value = fetchAllFrom_NotificationTbl(model);
            if (!value) list.add(model);
        }
        yesterdayPresc_list.clear();
        yesterdayPresc_list.addAll(list);
        insertNotifications(yesterdayPresc_list);

        if (yesterdayPresc_list.size() <= 0) {
            yesterday_nodata.setVisibility(View.VISIBLE);
        } else {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String yesterdaysDate = df.format(cal.getTime());
            System.out.println("Current time => " + yesterdaysDate); // yesterdays date: 2023-01-04

            List<NotificationModel> yesterdaysList = new ArrayList<>();
            for (NotificationModel notificationModel : yesterdayPresc_list) {
                yesterdaysList.add(showOnly_NonDeletedNotification(notificationModel, yesterdaysDate));
            }

            yesterday_nodata.setVisibility(View.GONE);
            adapter = new NotificationAdapter(this, yesterdaysList, this);
            recycler_yesterday.setAdapter(adapter);
        }
    }

    @Override
    public void deleteNotifi_Item(List<NotificationModel> patientDTOList, int position) {
        boolean isDeleted = false;
        isDeleted = deleteNotification(patientDTOList.get(position));
        if (isDeleted) {
            patientDTOList.remove(position);
            if (patientDTOList.size() <= 0) {
                today_nodata.setVisibility(View.VISIBLE);
                yesterday_nodata.setVisibility(View.VISIBLE);
            } else {
                today_nodata.setVisibility(View.GONE);
                yesterday_nodata.setVisibility(View.GONE);
            }
        }
        count();

    }

    @Override
    public void deleteAddDoc_Item(List<DocumentObject> list, int position) {

    }

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        Log.d("TAG", "updateUIForInternetAvailability: ");
        if (isInternetAvailable) {
            refresh.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ui2_ic_internet_available));
        } else {
            refresh.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ui2_ic_no_internet));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //register receiver for internet check
        networkUtils.callBroadcastReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            //unregister receiver for internet check
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
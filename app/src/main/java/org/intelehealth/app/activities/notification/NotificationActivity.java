package org.intelehealth.app.activities.notification;

import static org.intelehealth.app.database.dao.EncounterDAO.check_visit_is_VISIT_COMPLETE_ENC;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.SessionManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Prajwal Waingankar on 27/09/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class NotificationActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private SQLiteDatabase db;
    private ImageButton backbtn, clearAll_btn, refresh, filter;
    private RecyclerView recycler_today, recycler_yesterday;
    private NotificationAdapter adapter;
    public static final String TAG = NotificationActivity.class.getSimpleName();
    private FrameLayout filter_framelayout;

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
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        initViews();
        clickListeners();
    }

    private void initViews() {
        backbtn = findViewById(R.id.backbtn);
        clearAll_btn = findViewById(R.id.clearAll_btn);
        refresh = findViewById(R.id.refresh);
        filter = findViewById(R.id.filter);
        recycler_today = findViewById(R.id.recycler_today);
        recycler_yesterday = findViewById(R.id.recycler_yesterday);
        filter_framelayout = findViewById(R.id.filter_framelayout);
    }

    private void clickListeners() {
        todays_Presc_notification();
        yesterdays_Presc_notification();

        backbtn.setOnClickListener(v -> {
            finish();
        });

        clearAll_btn.setOnClickListener(v -> {
            // clears the recyclerview for both today and yesterday.
        });

        refresh.setOnClickListener(v -> {
            // refresh data.
            todays_Presc_notification();
            yesterdays_Presc_notification();
            Toast.makeText(this, "Refreshed Successfully", Toast.LENGTH_SHORT).show();
        });

        filter.setOnClickListener(v -> {
            // filter options
            if (filter_framelayout.getVisibility() == View.VISIBLE)
                filter_framelayout.setVisibility(View.GONE);
            else
                filter_framelayout.setVisibility(View.VISIBLE);
        });

    }

    private void todays_Presc_notification() {
        // current date
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        String currentDate = dateFormat.format(cal.getTime());
        Log.v("Notifi_Activity", "todaysDate: " + currentDate);

        List<PatientDTO> todayPresc_list = check_visit_is_VISIT_COMPLETE_ENC(currentDate);
        adapter = new NotificationAdapter(this, todayPresc_list);
        recycler_today.setAdapter(adapter);
    }

    private void yesterdays_Presc_notification() {
        // yesterdays date
        DateFormat dateFormat_yesterday = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal_yesterday = Calendar.getInstance();
        cal_yesterday.add(Calendar.DATE, -1);
        String yesterdayDate = dateFormat_yesterday.format(cal_yesterday.getTime());
        Log.v("Notifi_Activity", "yesterdaysDate: " + yesterdayDate);

        List<PatientDTO> yesterdayPresc_list = check_visit_is_VISIT_COMPLETE_ENC(yesterdayDate);
        adapter = new NotificationAdapter(this, yesterdayPresc_list);
        recycler_yesterday.setAdapter(adapter);
    }

}
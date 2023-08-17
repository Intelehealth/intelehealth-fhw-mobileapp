package org.intelehealth.app.activities.visit;

import static org.intelehealth.app.database.dao.VisitsDAO.thisMonths_NotEndedVisits;
import static org.intelehealth.app.database.dao.VisitsDAO.olderNotEndedVisits;
import static org.intelehealth.app.database.dao.VisitsDAO.recentNotEndedVisits;
import static org.intelehealth.app.syncModule.SyncUtils.syncNow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.ImageButton;
import android.widget.TextView;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.PrescriptionModel;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.SessionManager;

import java.util.List;
import java.util.Locale;

public class EndVisitActivity extends AppCompatActivity implements NetworkUtils.InternetCheckUpdateInterface {
    RecyclerView rvCloseVisitRecentData, rvCloseVisitOlderData, recycler_month;
    private static SQLiteDatabase db;
    private int total_counts = 0, todays_count = 0, weeks_count = 0, months_count = 0;
    private ImageButton imgBtnCloseVisitBackArrow, imgBtnCloseVisitRefresh;
    TextView tvCloseVisitRecentNoData, tvCloseVisitOlderNoData, month_nodata;
    private NetworkUtils networkUtils;
    private ObjectAnimator syncAnimator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_visit);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        networkUtils = new NetworkUtils(this, this);
        initViews();
        endVisits_data();

        imgBtnCloseVisitRefresh.setOnClickListener(v -> {
            syncNow(EndVisitActivity.this, imgBtnCloseVisitRefresh, syncAnimator);
        });
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

    private void initViews() {
        rvCloseVisitRecentData = findViewById(R.id.rvCloseVisitRecentData);
        rvCloseVisitOlderData = findViewById(R.id.rvCloseVisitOlderData);
        recycler_month = findViewById(R.id.recycler_month);
        tvCloseVisitRecentNoData = findViewById(R.id.tvCloseVisitRecentNoData);
        tvCloseVisitOlderNoData = findViewById(R.id.tvCloseVisitOlderNoData);
        month_nodata = findViewById(R.id.month_nodata);
        imgBtnCloseVisitBackArrow = findViewById(R.id.imgBtnCloseVisitBackArrow);
        imgBtnCloseVisitRefresh = findViewById(R.id.imgBtnCloseVisitRefresh);

        imgBtnCloseVisitBackArrow.setOnClickListener(v -> {
            finish();
        });
    }

    private void endVisits_data() {
        todays_EndVisits();
        thisWeeks_EndVisits();
//        thisMonths_EndVisits();
    }

    private void todays_EndVisits() {
        List<PrescriptionModel> arrayList = recentNotEndedVisits();
        EndVisitAdapter adapter_new = new EndVisitAdapter(this, arrayList);
        rvCloseVisitRecentData.setNestedScrollingEnabled(false); // Note: use NestedScrollView in xml and in xml add nestedscrolling to false as well as in java for Recyclerview in case you are recyclerview and scrollview together.
        rvCloseVisitRecentData.setAdapter(adapter_new);
        todays_count = arrayList.size();
        if (todays_count == 0 || todays_count < 0)
            tvCloseVisitRecentNoData.setVisibility(View.VISIBLE);
        else
            tvCloseVisitRecentNoData.setVisibility(View.GONE);
    }

    private void thisWeeks_EndVisits() {
        List<PrescriptionModel> arrayList = olderNotEndedVisits();
        EndVisitAdapter adapter_new = new EndVisitAdapter(this, arrayList);
        rvCloseVisitOlderData.setNestedScrollingEnabled(false);
        rvCloseVisitOlderData.setAdapter(adapter_new);
        weeks_count = arrayList.size();
        if (weeks_count == 0 || weeks_count < 0)
            tvCloseVisitOlderNoData.setVisibility(View.VISIBLE);
        else
            tvCloseVisitOlderNoData.setVisibility(View.GONE);
    }

    private void thisMonths_EndVisits() {
        List<PrescriptionModel> arrayList = thisMonths_NotEndedVisits();
        EndVisitAdapter adapter_new = new EndVisitAdapter(this, arrayList);
        recycler_month.setNestedScrollingEnabled(false);
        recycler_month.setAdapter(adapter_new);
        months_count = arrayList.size();
        if (months_count == 0 || months_count < 0)
            month_nodata.setVisibility(View.VISIBLE);
        else
            month_nodata.setVisibility(View.GONE);
    }

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        Log.d("TAG", "updateUIForInternetAvailability: ");
        if (isInternetAvailable) {
            imgBtnCloseVisitRefresh.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));
        } else {
            imgBtnCloseVisitRefresh.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));
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


  /*  @Override
    public int getTotalCounts() {
        total_counts = todays_count + weeks_count + months_count;
        return total_counts;
    }*/

}
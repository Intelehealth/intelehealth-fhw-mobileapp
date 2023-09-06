package org.intelehealth.app.activities.visit;

import static org.intelehealth.app.database.dao.VisitsDAO.thisMonths_NotEndedVisits;
import static org.intelehealth.app.database.dao.VisitsDAO.olderNotEndedVisits;
import static org.intelehealth.app.database.dao.VisitsDAO.recentNotEndedVisits;
import static org.intelehealth.app.syncModule.SyncUtils.syncNow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import android.widget.Toast;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.PrescriptionModel;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.SessionManager;

import java.util.List;
import java.util.Locale;

public class EndVisitActivity extends AppCompatActivity implements NetworkUtils.InternetCheckUpdateInterface {
    RecyclerView recycler_recent, recycler_older, recycler_month;
    NestedScrollView nestedscrollview;
    private static SQLiteDatabase db;
    private int total_counts = 0, todays_count = 0, weeks_count = 0, months_count = 0;
    private ImageButton backArrow, refresh;
    TextView recent_nodata, older_nodata, month_nodata;
    private NetworkUtils networkUtils;
    private ObjectAnimator syncAnimator;
    private final int recentLimit = 15, olderLimit = 15;
    private int recentStart = 0, recentEnd = recentStart + recentLimit;
    private boolean isRecentFullyLoaded = false;

    private int olderStart = 0, olderEnd = olderStart + olderLimit;
    private boolean isolderFullyLoaded = false;
    private List<PrescriptionModel> recentCloseVisitsList, olderCloseVisitsList;
    private EndVisitAdapter recentVisitsAdapter, olderVisitsAdapter;


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

        refresh.setOnClickListener(v -> {
            syncNow(EndVisitActivity.this, refresh, syncAnimator);
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
        recycler_recent = findViewById(R.id.rvCloseVisitRecentData);
        LinearLayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
        recycler_recent.setLayoutManager(reLayoutManager);

        recycler_older = findViewById(R.id.rvCloseVisitOlderData);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recycler_older.setLayoutManager(layoutManager);

        nestedscrollview = findViewById(R.id.nestedscrollview);
        nestedscrollview.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

            if (v.getChildAt(v.getChildCount() - 1) != null) {
                // Scroll Down
                if (scrollY > oldScrollY) {
                    // update recent data as it will not go at very bottom of list.
                    if (recentCloseVisitsList != null && recentCloseVisitsList.size() == 0) {
                        isRecentFullyLoaded = true;
                    }
                    if (!isRecentFullyLoaded)
                        setRecentMoreDataIntoRecyclerView();

                    // Last Item Scroll Down.
                    if (scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) {
                        // update older data as it will not go at very bottom of list.
                        if (olderCloseVisitsList != null && olderCloseVisitsList.size() == 0) {
                            isolderFullyLoaded = true;
                            return;
                        }
                        if (!isolderFullyLoaded) {
                            Toast.makeText(EndVisitActivity.this, getString(R.string.loading_more), Toast.LENGTH_SHORT).show();
                            setOlderMoreDataIntoRecyclerView();
                        }
                    }
                }
            }
        });


        recycler_month = findViewById(R.id.recycler_month);
        recent_nodata = findViewById(R.id.tvCloseVisitRecentNoData);
        older_nodata = findViewById(R.id.tvCloseVisitOlderNoData);
        month_nodata = findViewById(R.id.month_nodata);
        backArrow = findViewById(R.id.imgBtnCloseVisitBackArrow);
        refresh = findViewById(R.id.imgBtnCloseVisitRefresh);

        backArrow.setOnClickListener(v -> {
            finish();
        });
    }

    private void endVisits_data() {
        recentCloseVisits();
        olderCloseVisits();
//        thisMonths_EndVisits();
    }

    private void recentCloseVisits() {
        recentCloseVisitsList = recentNotEndedVisits(recentLimit, recentStart);
        recentVisitsAdapter = new EndVisitAdapter(this, recentCloseVisitsList);
        recycler_recent.setNestedScrollingEnabled(false); // Note: use NestedScrollView in xml and in xml add nestedscrolling to false as well as in java for Recyclerview in case you are recyclerview and scrollview together.
        recycler_recent.setAdapter(recentVisitsAdapter);

        recentStart = recentEnd;
        recentEnd += recentLimit;

        todays_count = recentCloseVisitsList.size();
        if (todays_count == 0 || todays_count < 0)
            recent_nodata.setVisibility(View.VISIBLE);
        else
            recent_nodata.setVisibility(View.GONE);
    }

    private void olderCloseVisits() {
        olderCloseVisitsList = olderNotEndedVisits(olderLimit, olderStart);
        olderVisitsAdapter = new EndVisitAdapter(this, olderCloseVisitsList);
        recycler_older.setNestedScrollingEnabled(false);
        recycler_older.setAdapter(olderVisitsAdapter);

        olderStart = olderEnd;
        olderEnd += olderLimit;

        weeks_count = olderCloseVisitsList.size();
        if (weeks_count == 0 || weeks_count < 0)
            older_nodata.setVisibility(View.VISIBLE);
        else
            older_nodata.setVisibility(View.GONE);
    }

    // This method will be accessed every time the person scrolls the recyclerView further.
    private void setRecentMoreDataIntoRecyclerView() {
        if (recentCloseVisitsList != null && recentCloseVisitsList.size() == 0) {
            isRecentFullyLoaded = true;
            return;
        }

        recentCloseVisitsList = recentNotEndedVisits(recentLimit, recentStart); // for n iteration limit be fixed == 15 and start - offset will keep skipping each records.
        Log.d("TAG", "setRecentMoreDataIntoRecyclerView: " + recentCloseVisitsList.size());
        recentVisitsAdapter.arrayList.addAll(recentCloseVisitsList);
        recentVisitsAdapter.notifyDataSetChanged();
        recentStart = recentEnd;
        recentEnd += recentLimit;
    }

    private void setOlderMoreDataIntoRecyclerView() {
        if (olderCloseVisitsList != null && olderCloseVisitsList.size() == 0) {
            isolderFullyLoaded = true;
            return;
        }

        olderCloseVisitsList = olderNotEndedVisits(olderLimit, olderStart); // for n iteration limit be fixed == 15 and start - offset will keep skipping each records.
        Log.d("TAG", "setOlderMoreDataIntoRecyclerView: " + olderCloseVisitsList.size());
        olderVisitsAdapter.arrayList.addAll(olderCloseVisitsList);
        olderVisitsAdapter.notifyDataSetChanged();
        olderStart = olderEnd;
        olderEnd += olderLimit;
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
            refresh.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));
        } else {
            refresh.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));
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
package org.intelehealth.app.activities.visit;

import static org.intelehealth.app.database.dao.VisitsDAO.thisMonths_NotEndedVisits;
import static org.intelehealth.app.database.dao.VisitsDAO.thisWeeks_NotEndedVisits;
import static org.intelehealth.app.database.dao.VisitsDAO.todays_NotEndedVisits;
import static org.intelehealth.app.syncModule.SyncUtils.syncNow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.PrescriptionModel;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.NetworkUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EndVisitActivity extends AppCompatActivity implements NetworkUtils.InternetCheckUpdateInterface {
    RecyclerView recycler_today, recycler_week, recycler_month;
    private static SQLiteDatabase db;
    private int total_counts = 0, todays_count = 0, weeks_count = 0, months_count = 0;
    private ImageButton backArrow, refresh;
    TextView today_nodata, week_nodata, month_nodata;
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

        refresh.setOnClickListener(v -> {
            syncNow(EndVisitActivity.this, refresh, syncAnimator);
        });
    }

    private void initViews() {
        recycler_today = findViewById(R.id.recycler_today);
        recycler_week = findViewById(R.id.recycler_week);
        recycler_month = findViewById(R.id.recycler_month);
        today_nodata = findViewById(R.id.today_nodata);
        week_nodata = findViewById(R.id.week_nodata);
        month_nodata = findViewById(R.id.month_nodata);
        backArrow = findViewById(R.id.backArrow);
        refresh = findViewById(R.id.refresh);

        backArrow.setOnClickListener(v -> {
            finish();
        });
    }

    private void endVisits_data() {
        todays_EndVisits();
        thisWeeks_EndVisits();
        thisMonths_EndVisits();
    }

    private void todays_EndVisits() {
        List<PrescriptionModel> arrayList = todays_NotEndedVisits();
        EndVisitAdapter adapter_new = new EndVisitAdapter(this, arrayList);
        recycler_today.setNestedScrollingEnabled(false); // Note: use NestedScrollView in xml and in xml add nestedscrolling to false as well as in java for Recyclerview in case you are recyclerview and scrollview together.
        recycler_today.setAdapter(adapter_new);
        todays_count = arrayList.size();
        if (todays_count == 0 || todays_count < 0)
            today_nodata.setVisibility(View.VISIBLE);
        else
            today_nodata.setVisibility(View.GONE);
    }

    private void thisWeeks_EndVisits() {
        List<PrescriptionModel> arrayList = thisWeeks_NotEndedVisits();
        EndVisitAdapter adapter_new = new EndVisitAdapter(this, arrayList);
        recycler_week.setNestedScrollingEnabled(false);
        recycler_week.setAdapter(adapter_new);
        weeks_count = arrayList.size();
        if (weeks_count == 0 || weeks_count < 0)
            week_nodata.setVisibility(View.VISIBLE);
        else
            week_nodata.setVisibility(View.GONE);
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
        }
        else {
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
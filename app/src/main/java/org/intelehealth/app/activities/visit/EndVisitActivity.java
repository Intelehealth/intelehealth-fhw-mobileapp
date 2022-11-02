package org.intelehealth.app.activities.visit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.followuppatients.FollowUpPatientAdapter_New;

public class EndVisitActivity extends AppCompatActivity {
    RecyclerView recycler_today, recycler_week, recycler_month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_visit);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        initViews();
        followup_data();

    }

    private void initViews() {
//        toolbar_title = findViewById(R.id.toolbar_title);
//        today_nodata = findViewById(R.id.today_nodata);
//        week_nodata = findViewById(R.id.week_nodata);
//        month_nodata = findViewById(R.id.month_nodata);
//        refresh = findViewById(R.id.refresh);

        recycler_today = findViewById(R.id.recycler_today);
        recycler_week = findViewById(R.id.recycler_week);
        recycler_month = findViewById(R.id.recycler_month);
    }

    private void followup_data() {
        todays_EndVisits();
        thisWeeks_EndVisits();
        thisMonths_EndVisits();
    }

    private void todays_EndVisits() {
        EndVisitAdapter adapter_new = new EndVisitAdapter(this);
        recycler_today.setAdapter(adapter_new);
    }

    private void thisWeeks_EndVisits() {
        EndVisitAdapter adapter_new = new EndVisitAdapter(this);
        recycler_week.setAdapter(adapter_new);
    }

    private void thisMonths_EndVisits() {
        EndVisitAdapter adapter_new = new EndVisitAdapter(this);
        recycler_month.setAdapter(adapter_new);
    }


}
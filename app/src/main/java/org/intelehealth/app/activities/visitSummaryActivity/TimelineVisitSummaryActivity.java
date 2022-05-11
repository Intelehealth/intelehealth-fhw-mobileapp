package org.intelehealth.app.activities.visitSummaryActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;

import org.intelehealth.app.R;

import java.util.ArrayList;

public class TimelineVisitSummaryActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    TimelineAdapter adapter;
    Context context;
    private String encounterAdultIntials = "", EncounterAdultInitial_LatestVisit = "", patientUuid = "";
    Intent intent;
    ArrayList<String> timeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline_visit_summary);

        initUI();
        adapter = new TimelineAdapter(context, intent, timeList);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_viewepartogram, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void initUI() {
        timeList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerview_timeline);
        LinearLayoutManager linearLayout = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayout);
        context = TimelineVisitSummaryActivity.this;
        intent = this.getIntent(); // The intent was passed to the activity

        if(intent != null) {
            timeList.add(intent.getStringExtra("startdate"));
        }
    }
}
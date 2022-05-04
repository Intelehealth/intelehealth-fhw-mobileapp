package org.intelehealth.app.activities.visitSummaryActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.LinearLayout;

import org.intelehealth.app.R;

public class TimelineVisitSummaryActivity extends AppCompatActivity {
RecyclerView recyclerView;
TimelineAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline_visit_summary);

        recyclerView = findViewById(R.id.recyclerview_timeline);
        LinearLayoutManager linearLayout = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayout);
        recyclerView.setLayoutManager(linearLayout);
        adapter = new TimelineAdapter();
        recyclerView.setAdapter(adapter);

    }
}
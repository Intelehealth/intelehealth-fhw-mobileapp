package org.intelehealth.app.activities.visit;

import static org.intelehealth.app.database.dao.VisitsDAO.thisMonths_NotEndedVisits;
import static org.intelehealth.app.database.dao.VisitsDAO.thisWeeks_NotEndedVisits;
import static org.intelehealth.app.database.dao.VisitsDAO.todays_NotEndedVisits;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.PrescriptionModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EndVisitActivity extends AppCompatActivity {
    RecyclerView recycler_today, recycler_week, recycler_month;
    private static SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_visit);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        initViews();
        followup_data();

    }

    private void initViews() {
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
        List<PrescriptionModel> arrayList = todays_NotEndedVisits();
        EndVisitAdapter adapter_new = new EndVisitAdapter(this, arrayList);
        recycler_today.setAdapter(adapter_new);
    }

    private void thisWeeks_EndVisits() {
        List<PrescriptionModel> arrayList = thisWeeks_NotEndedVisits();
        EndVisitAdapter adapter_new = new EndVisitAdapter(this, arrayList);
        recycler_week.setAdapter(adapter_new);
    }

    private void thisMonths_EndVisits() {
        List<PrescriptionModel> arrayList = thisMonths_NotEndedVisits();
        EndVisitAdapter adapter_new = new EndVisitAdapter(this, arrayList);
        recycler_month.setAdapter(adapter_new);
    }


}
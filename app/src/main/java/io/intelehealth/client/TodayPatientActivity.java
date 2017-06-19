package io.intelehealth.client;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.intelehealth.client.db.LocalRecordsDatabaseHelper;
import io.intelehealth.client.objects.TodayPatientModel;

/**
 * This class retrieves information about patients with visits on current date and sets data to a RecyclerView.
 */
public class TodayPatientActivity extends AppCompatActivity {

    final String TAG = TodayPatientActivity.class.getSimpleName();
    Toolbar mToolbar;

    RecyclerView mTodayPatientList;
    TodayPatientAdapter mTodayPatientAdapter;

    LocalRecordsDatabaseHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_patient);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTodayPatientList = (RecyclerView) findViewById(R.id.today_patient_recycler_view);
        setSupportActionBar(mToolbar);

        mDbHelper = new LocalRecordsDatabaseHelper(this);

        doQuery();
    }

    /**
     * This method retrieves visit details about patient for a particular date.
     * @return void
     */
    private void doQuery() {
        List<TodayPatientModel> todayPatientList = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Date cDate = new Date();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
        String query =
                "SELECT visit._id, visit.patient_id, visit.start_datetime, visit.end_datetime," +
                        "visit.openmrs_visit_uuid, patient.first_name, patient.middle_name, patient.last_name, " +
                        "patient.date_of_birth,patient.phone_number FROM visit, patient WHERE visit.patient_id = patient._id " +
                        "AND visit.start_datetime LIKE '" + currentDate + "T%'";
              //  "SELECT * FROM visit, patient WHERE visit.patient_id = patient._id AND visit.start_datetime LIKE '" + currentDate + "T%'";
        Log.i(TAG, query);
        final Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    todayPatientList.add(new TodayPatientModel(
                            cursor.getInt(cursor.getColumnIndexOrThrow("_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("patient_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("start_datetime")),
                            cursor.getString(cursor.getColumnIndexOrThrow("end_datetime")),
                            cursor.getString(cursor.getColumnIndexOrThrow("openmrs_visit_uuid")),
                            cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("middle_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")),
                            cursor.getString(cursor.getColumnIndexOrThrow("phone_number"))
                    ));
                } while (cursor.moveToNext());
            }
        }
        cursor.close();

        if (!todayPatientList.isEmpty()) {
            for (TodayPatientModel todayPatientModel : todayPatientList)
                Log.i(TAG, todayPatientModel.getFirst_name() + " " + todayPatientModel.getLast_name());

            mTodayPatientAdapter = new TodayPatientAdapter(todayPatientList, TodayPatientActivity.this);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TodayPatientActivity.this);
            mTodayPatientList.setLayoutManager(linearLayoutManager);
            mTodayPatientList.addItemDecoration(new
                    DividerItemDecoration(this,
                    DividerItemDecoration.VERTICAL));
            mTodayPatientList.setAdapter(mTodayPatientAdapter);
        }

    }


}

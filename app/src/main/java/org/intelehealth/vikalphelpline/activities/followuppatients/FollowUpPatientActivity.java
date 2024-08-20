package org.intelehealth.vikalphelpline.activities.followuppatients;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import android.content.res.Configuration;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.vikalphelpline.R;
import org.intelehealth.vikalphelpline.utilities.SessionManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.vikalphelpline.app.AppConstants;
import org.intelehealth.vikalphelpline.models.dto.PatientDTO;
import org.intelehealth.vikalphelpline.utilities.Logger;
import org.intelehealth.vikalphelpline.utilities.StringUtils;
import org.intelehealth.vikalphelpline.utilities.UuidDictionary;
import org.intelehealth.vikalphelpline.utilities.exception.DAOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class FollowUpPatientActivity extends AppCompatActivity {
    //    SearchView searchView;
    String query;
    private FollowUpPatientAdapter recycler;
    RecyclerView recyclerView;
    SessionManager sessionManager = null;
    TextView msg;
    MaterialAlertDialogBuilder dialogBuilder;
    private String TAG = FollowUpPatientActivity.class.getSimpleName();
    private SQLiteDatabase db;
    //    FloatingActionButton new_patient;
    int limit = Integer.MAX_VALUE, offset = 0;
//    boolean fullyLoaded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_patient);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Get the intent, verify the action and get the query
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

        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        msg = findViewById(R.id.textviewmessage);
        recyclerView = findViewById(R.id.recycle);
        LinearLayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(reLayoutManager);

        if (sessionManager.isPullSyncFinished()) {
            msg.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            firstQuery();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void firstQuery() {
        try {
            recycler = new FollowUpPatientAdapter(getAllPatientsFromDB(offset), FollowUpPatientActivity.this);
            recyclerView.setAdapter(recycler);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Logger.logE("firstquery", "exception", e);
        }
    }

    public List<PatientDTO> getAllPatientsFromDB(int offset) {
        List<PatientDTO> modelList = new ArrayList<PatientDTO>();
        String table = "tbl_patient";
        Date cDate = new Date();
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(cDate);
        String currentDate_new = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH).format(cDate);
        String query = "SELECT * FROM tbl_patient as p where p.uuid in (select v.patientuuid from tbl_visit as v where v.uuid in (select e.visituuid from tbl_encounter as e where e.uuid in (select o.encounteruuid from tbl_obs as o where (o.conceptuuid = ? and o.value like '%"+ currentDate_new +"%') or (o.conceptuuid = ? and o.value like '%"+ currentDate +"%'))))";
        final Cursor searchCursor = db.rawQuery(query,  new String[]{UuidDictionary.CONCEPT_RESOLUTION, UuidDictionary.FOLLOW_UP_VISIT});
        try {
            if (searchCursor.moveToFirst()) {

                do {
                    PatientDTO model = new PatientDTO();
                    model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                    model.setFirstname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")));
                    model.setLastname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")));
                    model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                    model.setUuid(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")));
                    model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
                    model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))));
                    model.comment = getSeverity(model.getUuid());
                    modelList.add(model);
                } while (searchCursor.moveToNext());
            }   // 3b25aeb7-0b92-42cc-b1bd-a9fa1ceb3b3f ace9b14f-9265-4ff1-b11b-2ddd78aff72b
            //(99edab6c-03b7-48eb-8f8b-8e1537d8ffa2, 0254c43a-1731-48af-9d57-d64eb5f04c77, 1745ae93-2f81-484c-9a74-36f365e17e09)  (6e7d7922-8be5-4e85-be65-9c5a89944d93, 7d2391f9-6d13-48b3-bd11-ea62ba977239, 04aa6827-9dde-4a02-950a-7eb6d065a44d)
            searchCursor.close();
            Collections.sort(modelList, new Comparator<PatientDTO>() {
                @Override
                public int compare(PatientDTO p1, PatientDTO p2) {
                    return p2.getSeverity() - p1.getSeverity();
                }
            });
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return modelList;

    }

    public List<PatientDTO> getQueryPatients(String query) {
        String search = query.trim().replaceAll("\\s", "");
        // search = StringUtils.mobileNumberEmpty(phoneNumber());
        List<PatientDTO> modelList = new ArrayList<PatientDTO>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        String table = "tbl_patient";

        List<String> patientUUID_List = new ArrayList<>();

        final Cursor search_mobile_cursor = db.rawQuery("SELECT DISTINCT patientuuid FROM tbl_patient_attribute WHERE value = ?",
                new String[]{search});
        /* DISTINCT will get remove the duplicate values. The duplicate value will come when you have created
         * a patient with mobile no. 12345 and patient is pushed than later you edit the mobile no to
         * 12344 or something. In this case, the local db maintains two separate rows both with value: 12344 */

        //if no data is present against that corresponding cursor than cursor count returns = 0 ... i.e cursor_count = 0 ...
        try {
            if (search_mobile_cursor.moveToFirst()) {
                do {
                    patientUUID_List.add(search_mobile_cursor.getString
                            (search_mobile_cursor.getColumnIndexOrThrow("patientuuid")));
                }
                while (search_mobile_cursor.moveToNext());
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        Log.d("patientUUID_list", "list: " + patientUUID_List.toString());

        if (patientUUID_List.size() != 0) {
            for (int i = 0; i < patientUUID_List.size(); i++) {

                final Cursor searchCursor = db.rawQuery("SELECT * FROM " + table + " WHERE first_name LIKE " + "'%" + search + "%' OR middle_name LIKE '%" + search + "%' OR uuid = ? OR last_name LIKE '%" + search + "%' OR (first_name || middle_name) LIKE '%" + search + "%' OR (middle_name || last_name) LIKE '%" + search + "%' OR (first_name || last_name) LIKE '%" + search + "%' OR openmrs_id LIKE '%" + search + "%' " + "ORDER BY first_name ASC",
                        new String[]{patientUUID_List.get(i)});
                //  if(searchCursor.getCount() != -1) { //all values are present as per the search text entered...
                try {
                    if (searchCursor.moveToFirst()) {
                        do {
                            PatientDTO model = new PatientDTO();
                            model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                            model.setFirstname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")));
                            model.setLastname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")));
                            model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                            model.setMiddlename(searchCursor.getString(searchCursor.getColumnIndexOrThrow("middle_name")));
                            model.setUuid(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")));
                            model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
                            model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))));
                            model.comment = getSeverity(model.getUuid());
                            modelList.add(model);
                        } while (searchCursor.moveToNext());
                    }
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

            }

        } else {
            final Cursor searchCursor = db.rawQuery("SELECT * FROM " + table + " WHERE first_name LIKE " + "'%" + search + "%' OR middle_name LIKE '%" + search + "%' OR last_name LIKE '%" + search + "%' OR (first_name || middle_name) LIKE '%" + search + "%' OR (middle_name || last_name) LIKE '%" + search + "%' OR (first_name || last_name) LIKE '%" + search + "%' OR openmrs_id LIKE '%" + search + "%' " + "ORDER BY first_name ASC",
                    null);
            //  if(searchCursor.getCount() != -1) { //all values are present as per the search text entered...
            try {
                if (searchCursor.moveToFirst()) {
                    do {
                        PatientDTO model = new PatientDTO();
                        model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                        model.setFirstname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")));
                        model.setLastname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")));
                        model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                        model.setMiddlename(searchCursor.getString(searchCursor.getColumnIndexOrThrow("middle_name")));
                        model.setUuid(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")));
                        model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
                        model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))));
                        model.comment = getSeverity(model.getUuid());
                        if (model.comment != null && model.comment.contains("Asymptomatic"))
                            continue;
                        modelList.add(model);
                    } while (searchCursor.moveToNext());
                }
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

        }

        return modelList;

    }

    private String getSeverity(String patientUid) {
        String severity = null;
        final Cursor obsCursor = db.rawQuery("select o.value from tbl_obs as o where o.conceptuuid = ? and encounteruuid in (select e.uuid from tbl_encounter as e where e.visituuid in (select v.uuid from tbl_visit as v where v.patientuuid = ?))", new String[]{UuidDictionary.PHYSICAL_EXAMINATION, patientUid});
        if (obsCursor.moveToFirst()) {
            do {
                severity = obsCursor.getString(obsCursor.getColumnIndexOrThrow("value"));
            } while (obsCursor.moveToNext());
            obsCursor.close();
        }
        return severity;
    }

    private String phoneNumber(String patientuuid) throws DAOException {
        String phone = null;
        Cursor idCursor = db.rawQuery("SELECT value  FROM tbl_patient_attribute where patientuuid = ? AND person_attribute_type_uuid='14d4f066-15f5-102d-96e4-000c29c2a5d7' ", new String[]{patientuuid});
        try {
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {

                    phone = idCursor.getString(idCursor.getColumnIndexOrThrow("value"));

                }
            }
        } catch (SQLException s) {
            FirebaseCrashlytics.getInstance().recordException(s);
        }
        idCursor.close();

        return phone;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}

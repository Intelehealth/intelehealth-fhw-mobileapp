package org.intelehealth.app.activities.followuppatients;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.FollowUpModel;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.LocaleHelper;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.app.webrtc.activity.BaseActivity;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Nishita Goyal on 27/09/21.
 * Github : @nishitagoyal
 */

public class FollowUpPatientActivity extends BaseActivity {

    private FollowUpPatientAdapter recycler;
    RecyclerView recyclerView;
    SessionManager sessionManager = null;
    TextView msg;
    private SQLiteDatabase db;
    int limit = Integer.MAX_VALUE, offset = 0;
    JSONObject jsonObject = new JSONObject();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_up_patient);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();

        //this language code is no longer required as we are moving towards more optimised as well as generic code for localisation. Check "attachBaseContext".
        //In case of crash still the app should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }

        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        setTitle(getString(R.string.title_follow_up));

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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
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


    public List<FollowUpModel> getAllPatientsFromDB(int offset) {
        List<FollowUpModel> modelList = new ArrayList<FollowUpModel>();
        String table = "tbl_patient";
        Date cDate = new Date();
        String currentDate = new SimpleDateFormat("dd-MMMM-yyyy", Locale.ENGLISH).format(cDate);
        String oldQuery = "SELECT * FROM " + table + " as p where p.uuid in (select v.patientuuid from tbl_visit as v where v.uuid in (select e.visituuid from tbl_encounter as e where e.uuid in (select o.encounteruuid from tbl_obs as o where o.conceptuuid = ? and o.value like '%" + currentDate + "%')))";
        String query = "SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.uuid, b.first_name, b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, c.value AS speciality, o.value FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE a.uuid = c.visit_uuid AND  a.enddate is NOT NULL AND a.patientuuid = b.uuid AND a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND o.conceptuuid = ?  AND o.value is NOT NULL GROUP BY a.patientuuid";
        final Cursor searchCursor = db.rawQuery(query, new String[]{UuidDictionary.FOLLOW_UP_VISIT});  //"e8caffd6-5d22-41c4-8d6a-bc31a44d0c86"
        if (searchCursor.moveToFirst()) {
            do {
                try {
                    String followUpDate = getValue(searchCursor.getString(searchCursor.getColumnIndexOrThrow("value")), sessionManager.getAppLanguage()).substring(0, 13);
                    Date followUp = new SimpleDateFormat("dd-MMMM-yyyy", Locale.ENGLISH).parse(followUpDate);
                    Date currentD = new SimpleDateFormat("dd-MMMM-yyyy", Locale.ENGLISH).parse(currentDate);
                    int value = followUp.compareTo(currentD);
                    if (value == -1) {
                        modelList.add(new FollowUpModel(
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("patientuuid")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")),
                                StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("speciality")),
                                followUpDate,
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("sync"))));
                    } else if (value == 0) {
                        modelList.add(new FollowUpModel(
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("patientuuid")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")),
                                StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("speciality")),
                                "null",
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("sync"))));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (searchCursor.moveToNext());
        }
        searchCursor.close();

        return modelList;
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

    public String getValue(String value, String language) {
        try {
            jsonObject = new JSONObject(value);
            if (TextUtils.isEmpty(language))
                return jsonObject.optString("en");
            else
                return jsonObject.optString(language);
        } catch (Exception e) {
            return value;
        }
    }

}
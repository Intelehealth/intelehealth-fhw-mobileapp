package org.intelehealth.app.activities.followuppatients;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.FollowUpModel;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Created by Prajwal Waingankar on 21/08/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class FollowUpPatientActivity_New extends AppCompatActivity {
    RecyclerView rv_today, rv_week, rv_month;
    FollowUpPatientAdapter_New adapter_new;
    SessionManager sessionManager = null;
    private SQLiteDatabase db;
    private int offset = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.follow_up_visits);

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

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

//        Window window = this.getWindow();
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            window.setStatusBarColor(ContextCompat.getColor(this,R.color.white));
//        }

        rv_today = findViewById(R.id.recycler_today);
        rv_week = findViewById(R.id.rv_thisweek);
        rv_month = findViewById(R.id.rv_thismonth);

        todays_FollowupVisits();
        thisWeeks_FollowupVisits();
        thisMonths_FollowupVisits();

//        rv_today.setAdapter(adapter_new);
//        rv_week.setAdapter(adapter_new);
//        rv_month.setAdapter(adapter_new);
    }

    private void todays_FollowupVisits() {
        try {
            Date cDate = new Date();
            String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(cDate);
            List<FollowUpModel> followUpModels = getAllPatientsFromDB_Today(offset, currentDate);
            adapter_new = new FollowUpPatientAdapter_New(followUpModels, this);
            rv_today.setAdapter(adapter_new);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Logger.logE("todays_followupvisits", "exception: ", e);
        }
    }

    private void thisWeeks_FollowupVisits() {
        try {
            List<FollowUpModel> followUpModels = getAllPatientsFromDB_thisWeek(offset);
            adapter_new = new FollowUpPatientAdapter_New(followUpModels, this);
            rv_week.setAdapter(adapter_new);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Logger.logE("todays_followupvisits", "exception: ", e);
        }
    }

    private void thisMonths_FollowupVisits() {
        try {
            List<FollowUpModel> followUpModels = getAllPatientsFromDB_thisMonth(offset);
            adapter_new = new FollowUpPatientAdapter_New(followUpModels, this);
            rv_month.setAdapter(adapter_new);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Logger.logE("todays_followupvisits", "exception: ", e);
        }
    }


    public List<FollowUpModel> getAllPatientsFromDB_Today(int offset, String currentDate) {
        List<FollowUpModel> modelList = new ArrayList<FollowUpModel>();
        String table = "tbl_patient";
/*
        String query = "SELECT * FROM " + table +" as p where p.uuid in (select v.patientuuid from tbl_visit as v " +
                "where v.uuid in (select e.visituuid from tbl_encounter as e where e.uuid in " +
                "(select o.encounteruuid from tbl_obs as o where o.conceptuuid = ? and o.value like '%"+ currentDate +"%')))";
*/

/*
        String query = "SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.uuid, b.first_name, " +
                "b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, c.value AS speciality, substr(o.value, 1, 10) as value1 " +
                "FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE " +
                "a.uuid = c.visit_uuid AND  a.enddate is NOT NULL AND a.patientuuid = b.uuid AND " +
                "a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND o.conceptuuid = ? AND " +
                "value1 like '%"+ currentDate +"%' AND " +
                "value1 is NOT NULL GROUP BY a.patientuuid";
*/

        // TODO: encounter is not null -- statement is removed | Add this later...
        String query = "SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.uuid, b.first_name, " +
                "b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, c.value AS speciality, SUBSTR(o.value,1,10) AS value_date " +
                "FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE " +
                "a.uuid = c.visit_uuid AND a.patientuuid = b.uuid AND " +
                "a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND o.conceptuuid = ? AND " +
                "date(substr(o.value, 7, 4)||'-'||substr(o.value, 4,2)||'-'||substr(o.value, 1,2)) = DATE('now') AND " +
                "o.value is NOT NULL GROUP BY a.patientuuid";

        final Cursor cursor = db.rawQuery(query, new String[]{UuidDictionary.FOLLOW_UP_VISIT});  //"e8caffd6-5d22-41c4-8d6a-bc31a44d0c86"
        if (cursor.moveToFirst()) {
            do {
                try {
                    modelList.add(new FollowUpModel(
                                cursor.getString(cursor.getColumnIndexOrThrow("uuid")),
                                cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                                cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")),
                                cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                                cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                                cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")),
                                StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))),
                                cursor.getString(cursor.getColumnIndexOrThrow("speciality")),
                                cursor.getString(cursor.getColumnIndexOrThrow("value_date")),
                                cursor.getString(cursor.getColumnIndexOrThrow("sync"))));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        return modelList;
    }

    public List<FollowUpModel> getAllPatientsFromDB_thisWeek(int offset) {
        List<FollowUpModel> modelList = new ArrayList<FollowUpModel>();
        String table = "tbl_patient";

//        String query = "SELECT * FROM " + table +" as p where p.uuid in (select v.patientuuid from tbl_visit as v " +
//                "where v.uuid in (select e.visituuid from tbl_encounter as e where e.uuid in " +
//                "(select o.encounteruuid from tbl_obs as o where o.conceptuuid = ? and " +
//                "o.obsservermodifieddate >= date(date('now', 'weekday 0', '-7 days'), 'weekday 0'))))";

/*
        String query = "SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.uuid, b.first_name, " +
                "b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, c.value AS speciality, o.value " +
                "FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE " +
                "a.uuid = c.visit_uuid AND a.enddate is NOT NULL AND a.patientuuid = b.uuid AND " +
                "a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND o.conceptuuid = ?  AND " +
                "o.obsservermodifieddate >= date(date('now', 'weekday 0', '-7 days'), 'weekday 0') AND " +
                "o.value is NOT NULL GROUP BY a.patientuuid";
*/
        // TODO: end date is removed later add it again.
        String query = "SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.uuid, b.first_name, " +
                "b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, c.value AS speciality, SUBSTR(o.value,1,10) AS value_text " +
                "FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE " +
                "a.uuid = c.visit_uuid AND a.patientuuid = b.uuid AND " +
                "a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND o.conceptuuid = ? AND " +
                "STRFTIME('%Y',date(substr(o.value, 7, 4)||'-'||substr(o.value, 4,2)||'-'||substr(o.value, 1,2))) = STRFTIME('%Y',DATE('now')) " +
                "AND STRFTIME('%W',date(substr(o.value, 7, 4)||'-'||substr(o.value, 4,2)||'-'||substr(o.value, 1,2))) = STRFTIME('%W',DATE('now')) AND " +
                "o.value is NOT NULL GROUP BY a.patientuuid";

        final Cursor cursor = db.rawQuery(query, new String[]{UuidDictionary.FOLLOW_UP_VISIT});  //"e8caffd6-5d22-41c4-8d6a-bc31a44d0c86"
        if (cursor.moveToFirst()) {
            do {
                try {
                    //   String followUpDate = cursor.getString(cursor.getColumnIndexOrThrow("value")).substring(0, 10);
                    modelList.add(new FollowUpModel(
                            cursor.getString(cursor.getColumnIndexOrThrow("uuid")),
                            cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                            cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")),
                            StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))),
                            cursor.getString(cursor.getColumnIndexOrThrow("speciality")),
                            cursor.getString(cursor.getColumnIndexOrThrow("value_text")),
                            cursor.getString(cursor.getColumnIndexOrThrow("sync"))));

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        return modelList;
    }

    public List<FollowUpModel> getAllPatientsFromDB_thisMonth(int offset) {
        List<FollowUpModel> modelList = new ArrayList<FollowUpModel>();
        String table = "tbl_patient";

//        String query = "SELECT * FROM " + table +" as p where p.uuid in (select v.patientuuid from tbl_visit as v " +
//                "where v.uuid in (select e.visituuid from tbl_encounter as e where e.uuid in " +
//                "(select o.encounteruuid from tbl_obs as o where o.conceptuuid = ? and " +
//                "o.obsservermodifieddate >= date(date('now', 'weekday 0', '-7 days'), 'weekday 0'))))";

/*
        String query = "SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.uuid, b.first_name, " +
                "b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, c.value AS speciality, o.value " +
                "FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE " +
                "a.uuid = c.visit_uuid AND a.enddate is NOT NULL AND a.patientuuid = b.uuid AND " +
                "a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND o.conceptuuid = ?  AND " +
                "o.obsservermodifieddate >= date(date('now', 'weekday 0', '-7 days'), 'weekday 0') AND " +
                "o.value is NOT NULL GROUP BY a.patientuuid";
*/
        // TODO: end date is removed later add it again.
        String query = "SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.uuid, b.first_name, " +
                "b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, c.value AS speciality, SUBSTR(o.value,1,10) AS value_text " +
                "FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE " +
                "a.uuid = c.visit_uuid AND a.patientuuid = b.uuid AND " +
                "a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND o.conceptuuid = ? AND " +
                "STRFTIME('%Y',date(substr(o.value, 7, 4)||'-'||substr(o.value, 4,2)||'-'||substr(o.value, 1,2))) = STRFTIME('%Y',DATE('now')) AND " +
                "STRFTIME('%m',date(substr(o.value, 7, 4)||'-'||substr(o.value, 4,2)||'-'||substr(o.value, 1,2))) = STRFTIME('%m',DATE('now')) AND " +
                "o.value is NOT NULL GROUP BY a.patientuuid";

        final Cursor cursor = db.rawQuery(query, new String[]{UuidDictionary.FOLLOW_UP_VISIT});  //"e8caffd6-5d22-41c4-8d6a-bc31a44d0c86"
        if (cursor.moveToFirst()) {
            do {
                try {
                    //   String followUpDate = cursor.getString(cursor.getColumnIndexOrThrow("value")).substring(0, 10);
                    modelList.add(new FollowUpModel(
                            cursor.getString(cursor.getColumnIndexOrThrow("uuid")),
                            cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                            cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")),
                            StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))),
                            cursor.getString(cursor.getColumnIndexOrThrow("speciality")),
                            cursor.getString(cursor.getColumnIndexOrThrow("value_text")),
                            cursor.getString(cursor.getColumnIndexOrThrow("sync"))));

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        return modelList;
    }


    private String phoneNumber(String patientuuid) throws DAOException {
        String phone = null;
        Cursor idCursor = db.rawQuery("SELECT value FROM tbl_patient_attribute where patientuuid = ? AND " +
                "person_attribute_type_uuid='14d4f066-15f5-102d-96e4-000c29c2a5d7'", new String[]{patientuuid});
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


}
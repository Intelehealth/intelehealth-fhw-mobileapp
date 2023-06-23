package org.intelehealth.app.activities.followuppatients;

import static org.intelehealth.app.database.dao.PatientsDAO.phoneNumber;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.models.FollowUpModel;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Prajwal Waingankar on 21/08/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class FollowUpPatientActivity_New extends AppCompatActivity {
    public static final String TAG = FollowUpPatientActivity_New.class.getName();
    RecyclerView rv_today, rv_week, rv_month;
    FollowUpPatientAdapter_New adapter_new;
    SessionManager sessionManager = null;
    private SQLiteDatabase db;
    private int offset = 0;
    private EncounterDAO encounterDAO;
    TextView toolbar_title, today_nodata, week_nodata, month_nodata;
    ImageButton refresh;
    int totalCounts = 0, totalCounts_today = 0, totalCounts_week = 0, totalCounts_month = 0;

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

        initViews();
        followup_data();

        refresh.setOnClickListener(v -> {
            followup_data();
            Toast.makeText(this, getResources().getString(R.string.refreshed_successfully), Toast.LENGTH_SHORT).show();
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

    private RelativeLayout mTodayRelativeLayout, mWeekRelativeLayout, mMonthRelativeLayout;
    private NestedScrollView mBodyNestedScrollView;
    private TextView mEmptyTextView;

    private void initViews() {
        mTodayRelativeLayout = findViewById(R.id.rl_today);
        mWeekRelativeLayout = findViewById(R.id.rl_week);
        mMonthRelativeLayout = findViewById(R.id.rl_month);
        mBodyNestedScrollView = findViewById(R.id.nestedscrollview);
        mEmptyTextView = findViewById(R.id.empty_view_tv);

        toolbar_title = findViewById(R.id.toolbar_title);
        today_nodata = findViewById(R.id.today_nodata);
        week_nodata = findViewById(R.id.week_nodata);
        month_nodata = findViewById(R.id.month_nodata);
        rv_today = findViewById(R.id.recycler_today);
        rv_week = findViewById(R.id.rv_thisweek);
        rv_month = findViewById(R.id.rv_thismonth);
        refresh = findViewById(R.id.refresh);
        ImageButton ibButtonBack = findViewById(R.id.vector);
        ibButtonBack.setOnClickListener(v -> {
            Intent intent = new Intent(FollowUpPatientActivity_New.this, HomeScreenActivity_New.class);
            startActivity(intent);
        });

    }

    private void followup_data() {
        todays_FollowupVisits();
        thisWeeks_FollowupVisits();
        thisMonths_FollowupVisits();
        totalCounts = totalCounts_today + totalCounts_week + totalCounts_month;
//        if (totalCounts == 0) {
//            mBodyNestedScrollView.setVisibility(View.GONE);
//            mEmptyTextView.setVisibility(View.VISIBLE);
//            toolbar_title.setText("Follow-up visits");
//        } else {
//            mBodyNestedScrollView.setVisibility(View.VISIBLE);
//            mEmptyTextView.setVisibility(View.GONE);
//            toolbar_title.setText("Follow-up visits(" + totalCounts_month + ")"); // eg. Follow-up visits(6)
//
//        }
    }

    private void todays_FollowupVisits() {
        try {
            Date cDate = new Date();
            String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(cDate);
            List<FollowUpModel> followUpModels = getAllPatientsFromDB_Today(offset, currentDate);
            followUpModels = getChiefComplaint(followUpModels);
            totalCounts_today = followUpModels.size();
            if (totalCounts_today <= 0) {
                today_nodata.setVisibility(View.VISIBLE);
            } else {
                today_nodata.setVisibility(View.GONE);
            }
            adapter_new = new FollowUpPatientAdapter_New(followUpModels, this);
            rv_today.setNestedScrollingEnabled(false);
            rv_today.setAdapter(adapter_new);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Logger.logE("todays_followupvisits", "exception: ", e);
        }
    }

    private List<FollowUpModel> getChiefComplaint(List<FollowUpModel> followUpModels) {
        for (int i = 0; i < followUpModels.size(); i++) {
            if (followUpModels.get(i).getUuid() != null) {
                String visitUUID = followUpModels.get(i).getUuid();
                Log.v("Followup", "visitid: " + visitUUID);
                String complaint_query = "select e.uuid, o.value  from tbl_encounter e, tbl_obs o where " +
                        "e.visituuid = ? " +
                        "and e.encounter_type_uuid = '8d5b27bc-c2cc-11de-8d13-0010c6dffd0f' " + // adult_initial
                        "and e.uuid = o.encounteruuid and o.conceptuuid = '3edb0e09-9135-481e-b8f0-07a26fa9a5ce'"; // chief complaint

                final Cursor cursor = db.rawQuery(complaint_query, new String[]{visitUUID});
                if (cursor.moveToFirst()) {
                    do {
                        try {
                            String complaint = cursor.getString(cursor.getColumnIndexOrThrow("value"));
                            followUpModels.get(i).setChiefComplaint(complaint);
                            Log.v("Followup", "chiefcomplaint: " + complaint);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        }
        return followUpModels;
    }

    private void thisWeeks_FollowupVisits() {
        try {
            List<FollowUpModel> followUpModels = getAllPatientsFromDB_thisWeek(offset);
            followUpModels = getChiefComplaint(followUpModels);
            totalCounts_week = followUpModels.size();
            if (totalCounts_week <= 0)
                week_nodata.setVisibility(View.VISIBLE);
            else
                week_nodata.setVisibility(View.GONE);
            adapter_new = new FollowUpPatientAdapter_New(followUpModels, this);
            rv_week.setNestedScrollingEnabled(false);
            rv_week.setAdapter(adapter_new);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Logger.logE("todays_followupvisits", "exception: ", e);
        }
    }

    private void thisMonths_FollowupVisits() {
        try {
            List<FollowUpModel> followUpModels = getAllPatientsFromDB_thisMonth(offset);
            followUpModels = getChiefComplaint(followUpModels);
            totalCounts_month = followUpModels.size();
            if (totalCounts_month <= 0)
                month_nodata.setVisibility(View.VISIBLE);
            else
                month_nodata.setVisibility(View.GONE);
            adapter_new = new FollowUpPatientAdapter_New(followUpModels, this);
            rv_month.setNestedScrollingEnabled(false);
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
        String query = "SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.uuid, b.first_name, " +
                "b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, c.value AS speciality, substr(o.value, 1, 10) as value1 " +
                "FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE " +
                "a.uuid = c.visit_uuid AND  a.enddate is NOT NULL AND a.patientuuid = b.uuid AND " +
                "a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND o.conceptuuid = ? AND " +
                "value1 like '%"+ currentDate +"%' AND " +
                "value1 is NOT NULL GROUP BY a.patientuuid";
*/

        // TODO: encounter is not null -- statement is removed | Add this later... " a.enddate is NOT NULL " --> Added...
        String query = "SELECT a.uuid as visituuid, a.sync, a.patientuuid, substr(a.startdate, 1, 10) as startdate,  " +
                "date(substr(o.value, 1, 10)) as followup_date, o.value as follow_up_info," +
                "b.patient_photo, a.enddate, b.uuid, b.first_name, " +
                "b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, b.gender, c.value AS speciality, " +
                "SUBSTR(o.value,1,10) AS value_text, o.obsservermodifieddate " +
                "FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE " +
                "a.uuid = c.visit_uuid AND  a.enddate is NOT NULL AND a.patientuuid = b.uuid AND " +
                "a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND o.conceptuuid = ? AND " +
                "date(substr(o.value, 1, 10)) = DATE('now') AND " +
                "o.value is NOT NULL GROUP BY a.patientuuid";
        Log.v(TAG, "query - " + query);
        final Cursor cursor = db.rawQuery(query, new String[]{UuidDictionary.FOLLOW_UP_VISIT});  //"e8caffd6-5d22-41c4-8d6a-bc31a44d0c86"
        if (cursor.moveToFirst()) {
            do {
                try {
                    String visitUuid = cursor.getString(cursor.getColumnIndexOrThrow("visituuid"));
                    boolean isCompletedExitedSurvey = new EncounterDAO().isCompletedExitedSurvey(visitUuid);
                    if (isCompletedExitedSurvey) {
                        // Fetch encounters who have emergency set and udpate modelist.

                        Log.v("Followup::", "::" + visitUuid);
                        String emergencyUuid = "";
                        encounterDAO = new EncounterDAO();
                        try {
                            emergencyUuid = encounterDAO.getEmergencyEncounters(visitUuid, encounterDAO.getEncounterTypeUuid("EMERGENCY"));
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                            emergencyUuid = "";
                        }

                        if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) { // ie. visit is emergency visit.
                            modelList.add(new FollowUpModel(
                                    visitUuid,
                                    cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")),
                                    StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))),
                                    cursor.getString(cursor.getColumnIndexOrThrow("gender")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("startdate")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("speciality")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("follow_up_info")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("sync")),
                                    true,
                                    cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")), // ie. visit is emergency visit.
                                    cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")))); // ie. visit is emergency visit.
                        } else {
                            modelList.add(new FollowUpModel( // ie. visit is NOT emergency visit.
                                    cursor.getString(cursor.getColumnIndexOrThrow("visituuid")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")),
                                    StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))),
                                    cursor.getString(cursor.getColumnIndexOrThrow("gender")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("startdate")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("speciality")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("follow_up_info")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("sync")),
                                    false,
                                    cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")))); // ie. visit is NOT emergency visit.
                        }
                    }
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

/*
        String query = "SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.uuid, b.first_name, " +
                "b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, c.value AS speciality, o.value " +
                "FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE " +
                "a.uuid = c.visit_uuid AND a.enddate is NOT NULL AND a.patientuuid = b.uuid AND " +
                "a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND o.conceptuuid = ?  AND " +
                "o.obsservermodifieddate >= date(date('now', 'weekday 0', '-7 days'), 'weekday 0') AND " +
                "o.value is NOT NULL GROUP BY a.patientuuid";
*/
        // TODO: end date is removed later add it again. --> Added... Only ended visits will show up for follow up.
        String query = "SELECT a.uuid as visituuid, a.sync, a.patientuuid, substr(a.startdate, 1, 10) as startdate, " +
                "date(substr(o.value, 1, 10)) as followup_date, o.value as follow_up_info," +
                "b.patient_photo, a.enddate, b.uuid, b.first_name, " +
                "b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, b.gender, c.value AS speciality, SUBSTR(o.value,1,10) AS value_text, o.obsservermodifieddate " +
                "FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE " +
                "a.uuid = c.visit_uuid AND   a.enddate is NOT NULL AND a.patientuuid = b.uuid AND " +
                "a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND o.conceptuuid = ? AND " +
                "STRFTIME('%Y',date(substr(o.value, 1, 10))) = STRFTIME('%Y',DATE('now')) " +
                "AND STRFTIME('%W',date(substr(o.value, 1, 10))) = STRFTIME('%W',DATE('now')) AND " +
                "o.value is NOT NULL GROUP BY a.patientuuid";

        final Cursor cursor = db.rawQuery(query, new String[]{UuidDictionary.FOLLOW_UP_VISIT});  //"e8caffd6-5d22-41c4-8d6a-bc31a44d0c86"
        if (cursor.moveToFirst()) {
            do {
                try {
                    // Fetch encounters who have emergency set and udpate modelist.
                    String visitUuid = cursor.getString(cursor.getColumnIndexOrThrow("visituuid"));
                    boolean isCompletedExitedSurvey = new EncounterDAO().isCompletedExitedSurvey(visitUuid);
                    if (isCompletedExitedSurvey) {
                        String patientID = cursor.getString(cursor.getColumnIndexOrThrow("patientuuid"));
                        Log.v("Followup::", "::" + visitUuid + "\n" + patientID);
                        String emergencyUuid = "";
                        encounterDAO = new EncounterDAO();
                        try {
                            emergencyUuid = encounterDAO.getEmergencyEncounters(visitUuid, encounterDAO.getEncounterTypeUuid("EMERGENCY"));
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                            emergencyUuid = "";
                        }

                        if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) { // ie. visit is emergency visit.
                            modelList.add(new FollowUpModel(
                                    visitUuid,
                                    cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")),
                                    StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))),
                                    cursor.getString(cursor.getColumnIndexOrThrow("gender")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("startdate")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("speciality")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("follow_up_info")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("sync")),
                                    true,
                                    cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")))); // ie. visit is emergency visit.
                        } else {
                            modelList.add(new FollowUpModel( // ie. visit is NOT emergency visit.
                                    cursor.getString(cursor.getColumnIndexOrThrow("visituuid")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")),
                                    StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))),
                                    cursor.getString(cursor.getColumnIndexOrThrow("gender")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("startdate")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("speciality")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("follow_up_info")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("sync")),
                                    false,
                                    cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")))); // ie. visit is NOT emergency visit.
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
//                    Toast.makeText(this, "error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        // TODO: end date is removed later add it again. --> Added...
        String query = "SELECT a.uuid as visituuid, a.sync, a.patientuuid, substr(a.startdate, 1, 10) as startdate, " +
                "date(substr(o.value, 1, 10)) as followup_date, o.value as follow_up_info," +
                "b.patient_photo, a.enddate, b.uuid, b.first_name, " +
                "b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, b.gender, c.value AS speciality, SUBSTR(o.value,1,10) AS value_text, o.obsservermodifieddate " +
                "FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE " +
                "a.uuid = c.visit_uuid AND   a.enddate is NOT NULL AND a.patientuuid = b.uuid AND " +
                "a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND o.conceptuuid = ? AND " +
                "STRFTIME('%Y',date(substr(o.value, 1, 10))) = STRFTIME('%Y',DATE('now')) AND " +
                "STRFTIME('%m',date(substr(o.value, 1, 10))) = STRFTIME('%m',DATE('now')) AND " +
                "o.value is NOT NULL GROUP BY a.patientuuid";

        final Cursor cursor = db.rawQuery(query, new String[]{UuidDictionary.FOLLOW_UP_VISIT});  //"e8caffd6-5d22-41c4-8d6a-bc31a44d0c86"
        if (cursor.moveToFirst()) {
            do {
                try {
                    // Fetch encounters who have emergency set and udpate modelist.
                    String visitUuid = cursor.getString(cursor.getColumnIndexOrThrow("visituuid"));
                    boolean isCompletedExitedSurvey = new EncounterDAO().isCompletedExitedSurvey(visitUuid);
                    if (isCompletedExitedSurvey) {
                        String emergencyUuid = "";
                        encounterDAO = new EncounterDAO();
                        try {
                            emergencyUuid = encounterDAO.getEmergencyEncounters(visitUuid, encounterDAO.getEncounterTypeUuid("EMERGENCY"));
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                            emergencyUuid = "";
                        }

                        if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) { // ie. visit is emergency visit.
                            modelList.add(new FollowUpModel(
                                    visitUuid,
                                    cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")),
                                    StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))),
                                    cursor.getString(cursor.getColumnIndexOrThrow("gender")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("startdate")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("speciality")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("follow_up_info")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("sync")),
                                    true,
                                    cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")))); // ie. visit is emergency visit.
                        } else {
                            modelList.add(new FollowUpModel( // ie. visit is NOT emergency visit.
                                    cursor.getString(cursor.getColumnIndexOrThrow("visituuid")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")),
                                    StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))),
                                    cursor.getString(cursor.getColumnIndexOrThrow("gender")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("startdate")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("speciality")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("follow_up_info")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("sync")),
                                    false,
                                    cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")))); // ie. visit is NOT emergency visit.
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
//                    Toast.makeText(this, "error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        return modelList;
    }


}
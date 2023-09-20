package org.intelehealth.app.activities.followuppatients;

import static org.intelehealth.app.database.dao.PatientsDAO.phoneNumber;
import static org.intelehealth.app.database.dao.VisitsDAO.olderNotEndedVisits;
import static org.intelehealth.app.database.dao.VisitsDAO.recentNotEndedVisits;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.activities.onboarding.PrivacyPolicyActivity_New;
import org.intelehealth.app.activities.visit.EndVisitAdapter;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.models.FollowUpModel;
import org.intelehealth.app.models.PrescriptionModel;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private androidx.appcompat.widget.SearchView searchview_received;
    private ImageView closeButton;
    private Context context = FollowUpPatientActivity_New.this;
    private RelativeLayout no_patient_found_block;
    private LinearLayout main_block;
    List<FollowUpModel> todays_modelList, weeks_modelList, months_modelList;
    private List<FollowUpModel> todaysFollowUpDates, weeksFollowUpDates, finalMonthsFollowUpDates;

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
        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();

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

        searchview_received = findViewById(R.id.searchview_received);
        closeButton = searchview_received.findViewById(R.id.search_close_btn);
        no_patient_found_block = findViewById(R.id.no_patient_found_block);
        main_block = findViewById(R.id.main_block);
        ((TextView) findViewById(R.id.search_pat_hint_txt)).setText(getString(R.string.empty_message_for_patinet_search_visit_screen));
        LinearLayout addPatientTV = findViewById(R.id.add_new_patientTV);
        addPatientTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PrivacyPolicyActivity_New.class);
                intent.putExtra("intentType", "navigateFurther");
                intent.putExtra("add_patient", "add_patient");
                startActivity(intent);
                finish();
            }
        });

        ibButtonBack.setOnClickListener(v -> {
            Intent intent = new Intent(FollowUpPatientActivity_New.this, HomeScreenActivity_New.class);
            startActivity(intent);
        });

        // Search - start
        searchview_received.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchOperation(query);
                return false;   // setting to false will close the keyboard when clicked on search btn.
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.equalsIgnoreCase("")) {
                    searchview_received.setBackground(getResources().getDrawable(R.drawable.blue_border_bg));
                } else {
                    searchview_received.setBackground(getResources().getDrawable(R.drawable.ui2_common_input_bg));
                }
                return false;
            }
        });

        closeButton.setOnClickListener(v -> {
            no_patient_found_block.setVisibility(View.GONE);
            main_block.setVisibility(View.VISIBLE);
            resetData();
            searchview_received.setQuery("", false);
        });
        // Search - end

    }

    private void resetData() {
        //  recent_older_visibility(todays_modelList, weeks_modelList, months_modelList);
//        Log.d("TAG", "resetData followup: " + todays_modelList.size() + ", " + weeks_modelList.size() + ", " + months_modelList.size());

        adapter_new = new FollowUpPatientAdapter_New(todaysFollowUpDates, this);
        rv_today.setNestedScrollingEnabled(false);
        rv_today.setAdapter(adapter_new);

        adapter_new = new FollowUpPatientAdapter_New(weeksFollowUpDates, this);
        rv_week.setNestedScrollingEnabled(false);
        rv_week.setAdapter(adapter_new);

        adapter_new = new FollowUpPatientAdapter_New(finalMonthsFollowUpDates, this);
        rv_month.setNestedScrollingEnabled(false);
        rv_month.setAdapter(adapter_new);
    }

/*
    private void recent_older_visibility(List<FollowUpModel> todays, List<FollowUpModel> weeks, List<FollowUpModel> months) {

        if (recent.size() == 0 || recent.size() < 0)
            recent_nodata.setVisibility(View.VISIBLE);
        else
            recent_nodata.setVisibility(View.GONE);

        if (older.size() == 0 || older.size() < 0)
            older_nodata.setVisibility(View.VISIBLE);
        else
            older_nodata.setVisibility(View.GONE);
    }
*/

    private void allCountVisibility(int allCount) {
        if (allCount == 0 || allCount < 0) {
            no_patient_found_block.setVisibility(View.VISIBLE);
            main_block.setVisibility(View.GONE);
        } else {
            no_patient_found_block.setVisibility(View.GONE);
            main_block.setVisibility(View.VISIBLE);
        }
    }

    private void searchOperation(String query) {
        Log.v("Search", "Search Word: " + query);
        query = query.toLowerCase().trim();
        query = query.replaceAll(" {2}", " ");
        Log.d("TAG", "searchOperation: " + query);

        List<FollowUpModel> todays = new ArrayList<>();
        List<FollowUpModel> weeks = new ArrayList<>();
        List<FollowUpModel> months = new ArrayList<>();

        String finalQuery = query;
        new Thread(new Runnable() {
            @Override
            public void run() {
//                List<FollowUpModel> todayList = getAllPatientsFromDB_Today();
//                todayList = getChiefComplaint(todayList);
//
//                List<FollowUpModel> weekList = getAllPatientsFromDB_thisWeek();
//                weekList = getChiefComplaint(weekList);
//
//                List<FollowUpModel> monthList = getAllPatientsFromDB_thisMonth();
//                monthList = getChiefComplaint(monthList);


                List<FollowUpModel> finalTodayList = todaysFollowUpDates;
                List<FollowUpModel> finalWeekList = weeksFollowUpDates;
                List<FollowUpModel> finalMonthList = finalMonthsFollowUpDates;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!finalQuery.isEmpty()) {

                            if (finalTodayList.size() > 0) {
                                for (FollowUpModel model : finalTodayList) {
                                    //
                                    if (model.getMiddle_name() != null) {
                                        String firstName = model.getFirst_name().toLowerCase();
                                        String middleName = model.getMiddle_name().toLowerCase();
                                        String lastName = model.getLast_name().toLowerCase();
                                        String fullPartName = firstName + " " + lastName;
                                        String fullName = firstName + " " + middleName + " " + lastName;

                                        if (firstName.contains(finalQuery) || middleName.contains(finalQuery) || lastName.contains(finalQuery) || fullPartName.contains(finalQuery) || fullName.contains(finalQuery)) {
                                            todays.add(model);
                                        } else {
                                            // dont add in list value.
                                        }
                                    } else {
                                        String firstName = model.getFirst_name().toLowerCase();
                                        String lastName = model.getLast_name().toLowerCase();
                                        String fullName = firstName + " " + lastName;

                                        if (firstName.contains(finalQuery) || lastName.contains(finalQuery) || fullName.contains(finalQuery)) {
                                            todays.add(model);
                                        } else {
                                            // dont add in list value.
                                        }
                                    }
                                    //
                                }
                            }

                            if (finalWeekList.size() > 0) {
                                for (FollowUpModel model : finalWeekList) {
                                    //
                                    if (model.getMiddle_name() != null) {
                                        String firstName = model.getFirst_name().toLowerCase();
                                        String middleName = model.getMiddle_name().toLowerCase();
                                        String lastName = model.getLast_name().toLowerCase();
                                        String fullPartName = firstName + " " + lastName;
                                        String fullName = firstName + " " + middleName + " " + lastName;

                                        if (firstName.contains(finalQuery) || middleName.contains(finalQuery) || lastName.contains(finalQuery) || fullPartName.contains(finalQuery) || fullName.contains(finalQuery)) {
                                            weeks.add(model);
                                        } else {
                                            // dont add in list value.
                                        }
                                    } else {
                                        String firstName = model.getFirst_name().toLowerCase();
                                        String lastName = model.getLast_name().toLowerCase();
                                        String fullName = firstName + " " + lastName;

                                        if (firstName.contains(finalQuery) || lastName.contains(finalQuery) || fullName.contains(finalQuery)) {
                                            weeks.add(model);
                                        } else {
                                            // dont add in list value.
                                        }
                                    }
                                    //
                                }
                            }

                            if (finalMonthList.size() > 0) {
                                for (FollowUpModel model : finalMonthList) {
                                    //
                                    if (model.getMiddle_name() != null) {
                                        String firstName = model.getFirst_name().toLowerCase();
                                        String middleName = model.getMiddle_name().toLowerCase();
                                        String lastName = model.getLast_name().toLowerCase();
                                        String fullPartName = firstName + " " + lastName;
                                        String fullName = firstName + " " + middleName + " " + lastName;

                                        if (firstName.contains(finalQuery) || middleName.contains(finalQuery) || lastName.contains(finalQuery) || fullPartName.contains(finalQuery) || fullName.contains(finalQuery)) {
                                            months.add(model);
                                        } else {
                                            // dont add in list value.
                                        }
                                    } else {
                                        String firstName = model.getFirst_name().toLowerCase();
                                        String lastName = model.getLast_name().toLowerCase();
                                        String fullName = firstName + " " + lastName;

                                        if (firstName.contains(finalQuery) || lastName.contains(finalQuery) || fullName.contains(finalQuery)) {
                                            months.add(model);
                                        } else {
                                            // dont add in list value.
                                        }
                                    }
                                    //
                                }
                            }

                            adapter_new = new FollowUpPatientAdapter_New(todays, context);
                            rv_today.setNestedScrollingEnabled(false);
                            rv_today.setAdapter(adapter_new);

                            adapter_new = new FollowUpPatientAdapter_New(weeks, context);
                            rv_week.setNestedScrollingEnabled(false);
                            rv_week.setAdapter(adapter_new);

                            adapter_new = new FollowUpPatientAdapter_New(months, context);
                            rv_month.setNestedScrollingEnabled(false);
                            rv_month.setAdapter(adapter_new);

                            /**
                             * Checking here the query that is entered and it is not empty so check the size of all of these
                             * arraylists; if there size is 0 than show the no patient found view.
                             */
                            int allCount = todays.size() + weeks.size() + months.size();
                            allCountVisibility(allCount);
                            //   recent_older_visibility(recent, older);
                        }
                    }
                });
            }
        }).start();

    }

    private void followup_data() {
        fetchAndSegregateData();

//        todays_FollowupVisits();
//        thisWeeks_FollowupVisits();
//        thisMonths_FollowupVisits();
//        totalCounts = totalCounts_today + totalCounts_week + totalCounts_month;
    }

    private void fetchAndSegregateData() {
        AlertDialog commonLoadingDialog = new DialogUtils().showCommonLoadingDialog(this, getString(R.string.loading), "");
        commonLoadingDialog.show();

        Executors.newSingleThreadExecutor().execute(() -> {
            List<FollowUpModel> initialFollowUpPatients = getAllPatientsFromDB_thisMonth();

            if (initialFollowUpPatients.isEmpty()) {
                runOnUiThread(() -> shouldShowNoDataTextViewForAllRecyclerViews(true));
            } else {
                finalMonthsFollowUpDates = initialFollowUpPatients;
                runOnUiThread(() -> shouldShowNoDataTextViewForAllRecyclerViews(false));
                getChiefComplaint(initialFollowUpPatients);

                todaysFollowUpDates = getTodaysVisitsFromList(initialFollowUpPatients);
                initialFollowUpPatients.removeAll(todaysFollowUpDates);

                weeksFollowUpDates = getWeeksVisitsFromList(initialFollowUpPatients);
                finalMonthsFollowUpDates.removeAll(todaysFollowUpDates);
                finalMonthsFollowUpDates.removeAll(weeksFollowUpDates);

                runOnUiThread(() -> {
                    setTodaysDatesInRecyclerView(todaysFollowUpDates);
                    setWeeksDatesInRecyclerView(weeksFollowUpDates);
                    setMonthsDatesInRecyclerView(finalMonthsFollowUpDates);
                    commonLoadingDialog.dismiss();
                });
            }
        });
    }

    private void setTodaysDatesInRecyclerView(List<FollowUpModel> todaysFollowUpDates) {
        if (todaysFollowUpDates.isEmpty()) {
            today_nodata.setVisibility(View.VISIBLE);
        } else {
            today_nodata.setVisibility(View.GONE);
            adapter_new = new FollowUpPatientAdapter_New(todaysFollowUpDates, this);
            rv_today.setNestedScrollingEnabled(false);
            rv_today.setAdapter(adapter_new);
        }
    }

    private void setWeeksDatesInRecyclerView(List<FollowUpModel> weeksFollowUpDates) {
        if (weeksFollowUpDates.isEmpty()) {
            week_nodata.setVisibility(View.VISIBLE);
        } else {
            week_nodata.setVisibility(View.GONE);
            adapter_new = new FollowUpPatientAdapter_New(weeksFollowUpDates, this);
            rv_week.setNestedScrollingEnabled(false);
            rv_week.setAdapter(adapter_new);
        }
    }

    private void setMonthsDatesInRecyclerView(List<FollowUpModel> monthFollowUpDates) {
        if (monthFollowUpDates.isEmpty()) {
            month_nodata.setVisibility(View.VISIBLE);
        } else {
            month_nodata.setVisibility(View.GONE);
            adapter_new = new FollowUpPatientAdapter_New(monthFollowUpDates, this);
            rv_month.setNestedScrollingEnabled(false);
            rv_month.setAdapter(adapter_new);
        }
    }

    private List<FollowUpModel> getWeeksVisitsFromList(List<FollowUpModel> followUpList) {
        List<FollowUpModel> weekFollowUpList = new ArrayList<>();

        for (FollowUpModel followUpModel : followUpList) {
            String followUpDate = DateAndTimeUtils.extractDateFromString(followUpModel.getFollowup_date());
            Date followUpDateObject = DateAndTimeUtils.convertStringToDateObject(followUpDate, "yyyy-MM-dd", "en");
            if (DateAndTimeUtils.isDateInCurrentWeek(followUpDateObject)) {
                weekFollowUpList.add(followUpModel);
            }
        }

        return weekFollowUpList;
    }

    private List<FollowUpModel> getTodaysVisitsFromList(List<FollowUpModel> followUpList) {
        List<FollowUpModel> todaysFollowUpList = new ArrayList<>();
        Date todaysDate = DateAndTimeUtils.getCurrentDateWithoutTime();

        for (FollowUpModel followUpModel : followUpList) {
            String followUpDate = DateAndTimeUtils.extractDateFromString(followUpModel.getFollowup_date());
            Date followUpDateObject = DateAndTimeUtils.convertStringToDateObject(followUpDate, "yyyy-MM-dd", "en");
            if (todaysDate.compareTo(followUpDateObject) == 0) {
                todaysFollowUpList.add(followUpModel);
            }
        }

        return todaysFollowUpList;
    }

    private void shouldShowNoDataTextViewForAllRecyclerViews(boolean isVisible) {
        if (isVisible) {
            today_nodata.setVisibility(View.VISIBLE);
            week_nodata.setVisibility(View.VISIBLE);
            month_nodata.setVisibility(View.VISIBLE);
        } else {
            today_nodata.setVisibility(View.GONE);
            week_nodata.setVisibility(View.GONE);
            month_nodata.setVisibility(View.GONE);
        }
    }

    private void todays_FollowupVisits() {
        try {
            Date cDate = new Date();
            String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(cDate);
            todays_modelList = getAllPatientsFromDB_Today();
            todays_modelList = getChiefComplaint(todays_modelList);

            totalCounts_today = todays_modelList.size();
            if (totalCounts_today == 0 || totalCounts_today < 0) {
                today_nodata.setVisibility(View.VISIBLE);
            } else {
                today_nodata.setVisibility(View.GONE);
            }

            adapter_new = new FollowUpPatientAdapter_New(todays_modelList, this);
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
                String complaint_query = "select e.uuid, o.value  from tbl_encounter e, tbl_obs o where " + "e.visituuid = ? " + "and e.encounter_type_uuid = '8d5b27bc-c2cc-11de-8d13-0010c6dffd0f' " + // adult_initial
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
            weeks_modelList = getAllPatientsFromDB_thisWeek();
            weeks_modelList = getChiefComplaint(weeks_modelList);

            totalCounts_week = weeks_modelList.size();
            if (totalCounts_week == 0 || totalCounts_week < 0)
                week_nodata.setVisibility(View.VISIBLE);
            else week_nodata.setVisibility(View.GONE);

            adapter_new = new FollowUpPatientAdapter_New(weeks_modelList, this);
            rv_week.setNestedScrollingEnabled(false);
            rv_week.setAdapter(adapter_new);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Logger.logE("todays_followupvisits", "exception: ", e);
        }
    }

    private void thisMonths_FollowupVisits() {
        try {
            months_modelList = getAllPatientsFromDB_thisMonth();
            months_modelList = getChiefComplaint(months_modelList);

            totalCounts_month = months_modelList.size();
            if (totalCounts_month == 0 || totalCounts_month < 0)
                month_nodata.setVisibility(View.VISIBLE);
            else month_nodata.setVisibility(View.GONE);

            adapter_new = new FollowUpPatientAdapter_New(months_modelList, this);
            rv_month.setNestedScrollingEnabled(false);
            rv_month.setAdapter(adapter_new);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Logger.logE("todays_followupvisits", "exception: ", e);
        }
    }


    public List<FollowUpModel> getAllPatientsFromDB_Today() {
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
        String query = "SELECT a.uuid as visituuid, a.sync, a.patientuuid, substr(a.startdate, 1, 10) as startdate,  " + "date(substr(o.value, 1, 10)) as followup_date, o.value as follow_up_info," + "b.patient_photo, a.enddate, b.uuid, b.first_name, " + "b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, b.gender, c.value AS speciality, " + "SUBSTR(o.value,1,10) AS value_text, o.obsservermodifieddate " + "FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE " + "a.uuid = c.visit_uuid AND  a.enddate is NOT NULL AND a.patientuuid = b.uuid AND " + "a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND o.conceptuuid = ? AND " + "date(substr(o.value, 1, 10)) = DATE('now') AND " + "o.value is NOT NULL GROUP BY a.patientuuid";
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
                            modelList.add(new FollowUpModel(visitUuid, cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")), cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")), cursor.getString(cursor.getColumnIndexOrThrow("first_name")), cursor.getString(cursor.getColumnIndexOrThrow("middle_name")), cursor.getString(cursor.getColumnIndexOrThrow("last_name")), cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")), StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))), cursor.getString(cursor.getColumnIndexOrThrow("gender")), cursor.getString(cursor.getColumnIndexOrThrow("startdate")), cursor.getString(cursor.getColumnIndexOrThrow("speciality")), cursor.getString(cursor.getColumnIndexOrThrow("follow_up_info")), cursor.getString(cursor.getColumnIndexOrThrow("sync")), true, cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")), // ie. visit is emergency visit.
                                    cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")))); // ie. visit is emergency visit.
                        } else {
                            modelList.add(new FollowUpModel( // ie. visit is NOT emergency visit.
                                    cursor.getString(cursor.getColumnIndexOrThrow("visituuid")), cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")), cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")), cursor.getString(cursor.getColumnIndexOrThrow("first_name")), cursor.getString(cursor.getColumnIndexOrThrow("middle_name")), cursor.getString(cursor.getColumnIndexOrThrow("last_name")), cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")), StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))), cursor.getString(cursor.getColumnIndexOrThrow("gender")), cursor.getString(cursor.getColumnIndexOrThrow("startdate")), cursor.getString(cursor.getColumnIndexOrThrow("speciality")), cursor.getString(cursor.getColumnIndexOrThrow("follow_up_info")), cursor.getString(cursor.getColumnIndexOrThrow("sync")), false, cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")), cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")))); // ie. visit is NOT emergency visit.
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

    public List<FollowUpModel> getAllPatientsFromDB_thisWeek() {
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
        String query = "SELECT a.uuid as visituuid, a.sync, a.patientuuid, substr(a.startdate, 1, 10) as startdate, " + "date(substr(o.value, 1, 10)) as followup_date, o.value as follow_up_info," + "b.patient_photo, a.enddate, b.uuid, b.first_name, " + "b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, b.gender, c.value AS speciality, SUBSTR(o.value,1,10) AS value_text, o.obsservermodifieddate " + "FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE " + "a.uuid = c.visit_uuid AND   a.enddate is NOT NULL AND a.patientuuid = b.uuid AND " + "a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND o.conceptuuid = ? AND " + "STRFTIME('%Y',date(substr(o.value, 1, 10))) = STRFTIME('%Y',DATE('now')) " + "AND STRFTIME('%W',date(substr(o.value, 1, 10))) = STRFTIME('%W',DATE('now')) AND " + "o.value is NOT NULL GROUP BY a.patientuuid";

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
                            modelList.add(new FollowUpModel(visitUuid, cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")), cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")), cursor.getString(cursor.getColumnIndexOrThrow("first_name")), cursor.getString(cursor.getColumnIndexOrThrow("middle_name")), cursor.getString(cursor.getColumnIndexOrThrow("last_name")), cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")), StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))), cursor.getString(cursor.getColumnIndexOrThrow("gender")), cursor.getString(cursor.getColumnIndexOrThrow("startdate")), cursor.getString(cursor.getColumnIndexOrThrow("speciality")), cursor.getString(cursor.getColumnIndexOrThrow("follow_up_info")), cursor.getString(cursor.getColumnIndexOrThrow("sync")), true, cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")), cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")))); // ie. visit is emergency visit.
                        } else {
                            modelList.add(new FollowUpModel( // ie. visit is NOT emergency visit.
                                    cursor.getString(cursor.getColumnIndexOrThrow("visituuid")), cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")), cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")), cursor.getString(cursor.getColumnIndexOrThrow("first_name")), cursor.getString(cursor.getColumnIndexOrThrow("middle_name")), cursor.getString(cursor.getColumnIndexOrThrow("last_name")), cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")), StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))), cursor.getString(cursor.getColumnIndexOrThrow("gender")), cursor.getString(cursor.getColumnIndexOrThrow("startdate")), cursor.getString(cursor.getColumnIndexOrThrow("speciality")), cursor.getString(cursor.getColumnIndexOrThrow("follow_up_info")), cursor.getString(cursor.getColumnIndexOrThrow("sync")), false, cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")), cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")))); // ie. visit is NOT emergency visit.
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

    public List<FollowUpModel> getAllPatientsFromDB_thisMonth() {
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
        String query = "SELECT a.uuid as visituuid, a.sync, a.patientuuid, substr(a.startdate, 1, 10) as startdate, "
                + "date(substr(o.value, 1, 10)) as followup_date, o.value as follow_up_info,"
                + "b.patient_photo, a.enddate, b.uuid, b.first_name, "
                + "b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, b.gender, c.value AS speciality, SUBSTR(o.value,1,10) AS value_text, MAX(o.obsservermodifieddate) AS obsservermodifieddate "
                + "FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE "
                + "a.uuid = c.visit_uuid AND   a.enddate is NOT NULL AND a.patientuuid = b.uuid AND "
                + "a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND o.conceptuuid = ? AND "
                + "STRFTIME('%Y',date(substr(o.value, 1, 10))) = STRFTIME('%Y',DATE('now')) AND "
                + "STRFTIME('%m',date(substr(o.value, 1, 10))) = STRFTIME('%m',DATE('now')) AND "
                + "o.value is NOT NULL GROUP BY a.patientuuid";

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
                            modelList.add(new FollowUpModel(visitUuid, cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")), cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")), cursor.getString(cursor.getColumnIndexOrThrow("first_name")), cursor.getString(cursor.getColumnIndexOrThrow("middle_name")), cursor.getString(cursor.getColumnIndexOrThrow("last_name")), cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")), StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))), cursor.getString(cursor.getColumnIndexOrThrow("gender")), cursor.getString(cursor.getColumnIndexOrThrow("startdate")), cursor.getString(cursor.getColumnIndexOrThrow("speciality")), cursor.getString(cursor.getColumnIndexOrThrow("follow_up_info")), cursor.getString(cursor.getColumnIndexOrThrow("sync")), true, cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")), cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")))); // ie. visit is emergency visit.
                        } else {
                            modelList.add(new FollowUpModel( // ie. visit is NOT emergency visit.
                                    cursor.getString(cursor.getColumnIndexOrThrow("visituuid")), cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")), cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")), cursor.getString(cursor.getColumnIndexOrThrow("first_name")), cursor.getString(cursor.getColumnIndexOrThrow("middle_name")), cursor.getString(cursor.getColumnIndexOrThrow("last_name")), cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")), StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))), cursor.getString(cursor.getColumnIndexOrThrow("gender")), cursor.getString(cursor.getColumnIndexOrThrow("startdate")), cursor.getString(cursor.getColumnIndexOrThrow("speciality")), cursor.getString(cursor.getColumnIndexOrThrow("follow_up_info")), cursor.getString(cursor.getColumnIndexOrThrow("sync")), false, cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")), cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")))); // ie. visit is NOT emergency visit.
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(context, HomeScreenActivity_New.class);
        startActivity(intent);
    }
}
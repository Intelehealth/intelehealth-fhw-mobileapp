package org.intelehealth.app.activities.achievements.fragments;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UuidDictionary;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DailyAchievementsFragment extends Fragment {
    private View view;
    public HomeScreenActivity_New activity1;

    private TextView tvPatientsCreatedToday;
    private TextView tvVisitsEndedToday;
    private TextView tvAvgSatisfactionScore;
    private TextView tvDailyTimeSpent;

    private String todaysDate;
    private String todaysDateInYYYYMMDD;

    private SessionManager sessionManager;
    private UsageStats overallUsageStats;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocale(getContext());
        //sessionManager = ((MyAchievementsFragment) requireParentFragment()).sessionManager;
        sessionManager = new SessionManager(requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setLocale(getContext());
        view = inflater.inflate(R.layout.fragment_daily_achievements_ui2, container, false);
        setLocale(getContext());
        initUI();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchAndSetUIData();
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

    private void initUI() {
        //View layoutToolbar = requireActivity().findViewById(R.id.toolbar_home);
        //ImageView ivBackArrow = layoutToolbar.findViewById(R.id.iv_hamburger);
        /*ivBackArrow.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ui2_ic_arrow_back_new));
        ivBackArrow.setOnClickListener(v -> {

          *//*  FragmentManager fm = Objects.requireNonNull(getActivity()).getFragmentManager();
            fm.popBackStack();*//*
            //Intent intent = new Intent(getActivity(), HomeScreenActivity_New.class);
            //startActivity(intent);
            getActivity().onBackPressed();
        });*/

        todaysDate = DateAndTimeUtils.getTodaysDateInRequiredFormat("dd MMMM, yyyy");
        todaysDateInYYYYMMDD = DateAndTimeUtils.getTodaysDateInRequiredFormat("yyyy-MM-dd");

        TextView tvTodaysDate = view.findViewById(R.id.tv_todays_date);
        tvTodaysDate.setText(DateAndTimeUtils.getTodaysDateInRequiredFormat("dd MMMM, yyyy", sessionManager.getAppLanguage()));

        tvPatientsCreatedToday = view.findViewById(R.id.tv_patients_created_today);
        tvVisitsEndedToday = view.findViewById(R.id.tv_visits_ended_today);
        tvAvgSatisfactionScore = view.findViewById(R.id.tv_avg_satisfaction_score);
        tvDailyTimeSpent = view.findViewById(R.id.tv_daily_time_spent);
    }

    private void fetchAndSetUIData() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            setPatientsCreatedToday();
            setVisitsEndedToday();
            setAveragePatientSatisfactionScore();
            setDailyTimeSpent();
        });
    }

    // get the number patients who were Created today as per their provider uuid
    private void setPatientsCreatedToday() {
        String patientsCreatedTodayQuery = "SELECT COUNT(DISTINCT patientuuid) FROM tbl_patient_attribute WHERE person_attribute_type_uuid = \"84f94425-789d-4293-a0d8-9dc01dbb4f07\" AND value = ? AND patientuuid IN (SELECT patientuuid FROM tbl_patient_attribute WHERE person_attribute_type_uuid = \"ffc8ebee-f70c-4743-bc3c-2fe4ac843245\" AND value = ?)";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        final Cursor todayPatientsCursor = db.rawQuery(patientsCreatedTodayQuery, new String[]{sessionManager.getProviderID(), todaysDate});
        todayPatientsCursor.moveToFirst();
        String todayPatientsCount = todayPatientsCursor.getString(todayPatientsCursor.getColumnIndex(todayPatientsCursor.getColumnName(0)));
        requireActivity().runOnUiThread(() -> tvPatientsCreatedToday.setText(todayPatientsCount));
        todayPatientsCursor.close();
    }

    // get the number of visits that were ended by the current health worker today
    private void setVisitsEndedToday() {
        String date = DateAndTimeUtils.getDateTimeFromTimestamp(System.currentTimeMillis(), "MMM d, yyyy");
        String unsyncDateFormat = DateAndTimeUtils.getDateTimeFromTimestamp(System.currentTimeMillis(), "yyyy-MM-dd");
        //here added two logic for date filter
        //because if sync status = 1 then the date format is "MMM d, yyyy"
        //and sync status = 0 then the date format is "yyyy-MM-dd"
        String visitsEndedTodayQuery = "SELECT COUNT(DISTINCT visituuid) FROM tbl_encounter as e, tbl_visit as v " +
                "WHERE e.visituuid = v.uuid AND e.provider_uuid = ? " +
                "AND e.encounter_type_uuid = '" + UuidDictionary.ENCOUNTER_PATIENT_EXIT_SURVEY + "' " +
                "AND (CASE WHEN v.sync = 1 then v.enddate LIKE '" + date + "%' else substr(v.enddate,1,10) LIKE '" + unsyncDateFormat + "%' END)";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        final Cursor todayVisitsEndedCursor = db.rawQuery(visitsEndedTodayQuery, new String[]{
                sessionManager.getProviderID()
        });

        todayVisitsEndedCursor.moveToFirst();
        String todayVisitsEndedCount = todayVisitsEndedCursor.getString(todayVisitsEndedCursor.getColumnIndex(todayVisitsEndedCursor.getColumnName(0)));
        requireActivity().runOnUiThread(() -> tvVisitsEndedToday.setText(todayVisitsEndedCount));
        todayVisitsEndedCursor.close();
    }

    // get today's average patient satisfaction score for the health worker
    private void setAveragePatientSatisfactionScore() {
        double averageScore = 0.0, totalScore = 0.0;

        String todaysAverageSatisfactionScoreQuery = "SELECT value FROM tbl_obs WHERE conceptuuid = \"78284507-fb71-4354-9b34-046ab205e18f\" AND encounteruuid IN (SELECT uuid FROM tbl_encounter WHERE provider_uuid = ? AND modified_date LIKE '" + todaysDateInYYYYMMDD + "%')";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        final Cursor satisfactionScoreCursor = db.rawQuery(todaysAverageSatisfactionScoreQuery, new String[]{sessionManager.getProviderID()});

        if (satisfactionScoreCursor.moveToFirst()) {
            do {
                double currentScore = Double.parseDouble(satisfactionScoreCursor.getString(satisfactionScoreCursor.getColumnIndexOrThrow("value")));
                totalScore = totalScore + currentScore;
            } while (satisfactionScoreCursor.moveToNext());

            averageScore = totalScore / satisfactionScoreCursor.getCount();
        }

        double finalAverageScore = averageScore;
        requireActivity().runOnUiThread(() -> tvAvgSatisfactionScore.setText(StringUtils.formatDoubleValues(finalAverageScore)));
        satisfactionScoreCursor.close();
    }

    private void setDailyTimeSpent() {
        long firstProviderLoginTimeInMilliseconds = DateAndTimeUtils.convertStringDateToMilliseconds(sessionManager.getFirstProviderLoginTime(), "yyyy-MM-dd'T'HH:mm:ss.SSSZ", sessionManager.getAppLanguage());
        long todaysDateInMilliseconds = DateAndTimeUtils.getTodaysDateInMilliseconds();

        long endDate = System.currentTimeMillis();
        long startDate = Math.max(todaysDateInMilliseconds, firstProviderLoginTimeInMilliseconds);

        UsageStatsManager usageStatsManager = ((MyAchievementsFragment) requireParentFragment()).usageStatsManager;
        Map<String, UsageStats> aggregateStatsMap = usageStatsManager.queryAndAggregateUsageStats(startDate, endDate);
        overallUsageStats = aggregateStatsMap.get("org.intelehealth.app");

        requireActivity().runOnUiThread(() -> {
            String totalTimeSpent = "";
            if (overallUsageStats != null) {
                totalTimeSpent = String.format(Locale.ENGLISH, DateAndTimeUtils.convertMillisecondsToHoursAndMinutes(overallUsageStats.getTotalTimeInForeground()));
            } else {
                totalTimeSpent = "0h 0m";
            }
            tvDailyTimeSpent.setText(totalTimeSpent);
        });
    }
}
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
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.shared.builder.VisitQueryBuilder;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OverallAchievementsFragment extends Fragment {

    private TextView tvOverallPatientsAdded;
    private TextView tvOverallVisitsEnded;
    private TextView tvOverallPatientSatisfactionScore;
    private TextView tvOverallTimeSpent;

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
        View view = inflater.inflate(R.layout.fragment_overall_achievements_ui2, container, false);
        setLocale(getContext());
        initUI(view);
        return view;
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchAndSetUIData();
    }

    private void initUI(View view) {
        tvOverallPatientsAdded = view.findViewById(R.id.tv_overall_patients_added);
        tvOverallVisitsEnded = view.findViewById(R.id.tv_overall_visits_ended);
        tvOverallPatientSatisfactionScore = view.findViewById(R.id.tv_overall_patient_satisfaction_score);
        tvOverallTimeSpent = view.findViewById(R.id.tv_overall_time_spent);
    }

    private void fetchAndSetUIData() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            setOverallPatientsCreated();
            setOverallVisitsEnded();
            setOverallPatientSatisfactionScore();
            setOverallTimeSpent();
        });
    }

    // get the overall number of visits that were ended by the current health worker
    private void setOverallPatientsCreated() {
        String patientsCreatedTodayQuery = "SELECT COUNT(DISTINCT patientuuid) FROM tbl_patient_attribute WHERE person_attribute_type_uuid = \"84f94425-789d-4293-a0d8-9dc01dbb4f07\" AND value = ?";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        final Cursor overallPatientsCreatedCursor = db.rawQuery(patientsCreatedTodayQuery, new String[]{sessionManager.getProviderID()});
        overallPatientsCreatedCursor.moveToFirst();
        String overallPatientsCreatedCount = overallPatientsCreatedCursor.getString(overallPatientsCreatedCursor.getColumnIndex(overallPatientsCreatedCursor.getColumnName(0)));
        requireActivity().runOnUiThread(() -> tvOverallPatientsAdded.setText(overallPatientsCreatedCount));
        overallPatientsCreatedCursor.close();
    }

    // get the overall patient satisfaction score for the health worker
    private void setOverallVisitsEnded() {
        String query = new VisitQueryBuilder().visitCompletedByCreatorCount(sessionManager.getProviderID());
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        final Cursor overallVisitsEndedCursor = db.rawQuery(query, null);

        if (overallVisitsEndedCursor.moveToFirst()) {
            String overallVisitsEndedCount = overallVisitsEndedCursor.getString(overallVisitsEndedCursor.getColumnIndexOrThrow("completed"));
            requireActivity().runOnUiThread(() -> tvOverallVisitsEnded.setText(overallVisitsEndedCount));
        }
        overallVisitsEndedCursor.close();
    }

    // get the overall average patient satisfaction score for the health worker
    private void setOverallPatientSatisfactionScore() {
        double averageScore = 0.0, totalScore = 0.0;

        String overallAverageSatisfactionScoreQuery = "SELECT value FROM tbl_obs WHERE conceptuuid = \"78284507-fb71-4354-9b34-046ab205e18f\" AND encounteruuid IN (SELECT uuid FROM tbl_encounter WHERE provider_uuid = ?)";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        final Cursor satisfactionScoreCursor = db.rawQuery(overallAverageSatisfactionScoreQuery, new String[]{sessionManager.getProviderID()});

        if (satisfactionScoreCursor.moveToFirst()) {
            do {
                double currentScore = Double.parseDouble(satisfactionScoreCursor.getString(satisfactionScoreCursor.getColumnIndexOrThrow("value")));
                totalScore = totalScore + currentScore;
            } while (satisfactionScoreCursor.moveToNext());

            averageScore = totalScore / satisfactionScoreCursor.getCount();
        }

        double finalAverageScore = averageScore;
        requireActivity().runOnUiThread(() -> tvOverallPatientSatisfactionScore.setText(StringUtils.formatDoubleValues(finalAverageScore)));
        satisfactionScoreCursor.close();
    }

    private void setOverallTimeSpent() {
        long startTime = DateAndTimeUtils.convertStringDateToMilliseconds(sessionManager.getFirstProviderLoginTime(), "yyyy-MM-dd'T'HH:mm:ss.SSSZ", sessionManager.getAppLanguage());
        long endDate = System.currentTimeMillis();

        UsageStatsManager usageStatsManager = ((MyAchievementsFragment) requireParentFragment()).usageStatsManager;
        Map<String, UsageStats> aggregateStatsMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endDate);
        overallUsageStats = aggregateStatsMap.get("org.intelehealth.app");

        requireActivity().runOnUiThread(() -> {
            String totalTimeSpent = "";
            if (overallUsageStats != null) {
                totalTimeSpent = String.format(Locale.ENGLISH, DateAndTimeUtils.convertMillisecondsToHoursAndMinutes(overallUsageStats.getTotalTimeInForeground()));
            } else {
                totalTimeSpent = "0h 0m";
            }
            tvOverallTimeSpent.setText(totalTimeSpent);
        });
    }
}
package org.intelehealth.unicef.activities.achievements.fragments;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.unicef.app.AppConstants;
import org.intelehealth.unicef.utilities.DateAndTimeUtils;
import org.intelehealth.unicef.utilities.SessionManager;
import org.intelehealth.unicef.utilities.StringUtils;

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
        sessionManager = ((MyAchievementsFragment) requireParentFragment()).sessionManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_daily_achievements_ui2, container, false);
        initUI();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchAndSetUIData();
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

        if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
            todaysDate = StringUtils.en__ru_dob(todaysDate);
        }

        TextView tvTodaysDate = view.findViewById(R.id.tv_todays_date);
        tvTodaysDate.setText(todaysDate);

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
        String patientsCreatedTodayQuery = "SELECT COUNT(DISTINCT uuid) FROM tbl_patient WHERE creator_uuid = ? AND date_created LIKE '" + todaysDateInYYYYMMDD + "%'";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
        final Cursor todayPatientsCursor = db.rawQuery(patientsCreatedTodayQuery, new String[]{sessionManager.getCreatorID()});
        todayPatientsCursor.moveToFirst();
        String todayPatientsCount = todayPatientsCursor.getString(todayPatientsCursor.getColumnIndex(todayPatientsCursor.getColumnName(0)));
        requireActivity().runOnUiThread(() -> tvPatientsCreatedToday.setText(todayPatientsCount));
        todayPatientsCursor.close();
    }

    // get the number of visits that were ended by the current health worker today
    private void setVisitsEndedToday() {
        String visitsEndedTodayQuery = "SELECT COUNT(DISTINCT visituuid) FROM tbl_encounter WHERE provider_uuid = ? AND encounter_type_uuid = \"629a9d0b-48eb-405e-953d-a5964c88dc30\" AND modified_date LIKE '" + todaysDateInYYYYMMDD + "%'";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
        final Cursor todayVisitsEndedCursor = db.rawQuery(visitsEndedTodayQuery, new String[]{sessionManager.getProviderID()});

        todayVisitsEndedCursor.moveToFirst();
        String todayVisitsEndedCount = todayVisitsEndedCursor.getString(todayVisitsEndedCursor.getColumnIndex(todayVisitsEndedCursor.getColumnName(0)));
        requireActivity().runOnUiThread(() -> tvVisitsEndedToday.setText(todayVisitsEndedCount));
        todayVisitsEndedCursor.close();
    }

    // get today's average patient satisfaction score for the health worker
    private void setAveragePatientSatisfactionScore() {
        double averageScore = 0.0, totalScore = 0.0;

        String todaysAverageSatisfactionScoreQuery = "SELECT value FROM tbl_obs WHERE conceptuuid = \"78284507-fb71-4354-9b34-046ab205e18f\" AND encounteruuid IN (SELECT uuid FROM tbl_encounter WHERE provider_uuid = ? AND modified_date LIKE '" + todaysDateInYYYYMMDD + "%')";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
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
        long firstProviderLoginTimeInMilliseconds = DateAndTimeUtils.convertStringDateToMilliseconds(sessionManager.getFirstProviderLoginTime(), "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        long todaysDateInMilliseconds = DateAndTimeUtils.getTodaysDateInMilliseconds();

        long endDate = System.currentTimeMillis();
        long startDate = Math.max(todaysDateInMilliseconds, firstProviderLoginTimeInMilliseconds);

        UsageStatsManager usageStatsManager = ((MyAchievementsFragment) requireParentFragment()).usageStatsManager;
        Map<String, UsageStats> aggregateStatsMap = usageStatsManager.queryAndAggregateUsageStats(startDate, endDate);
        overallUsageStats = aggregateStatsMap.get("org.intelehealth.unicef");

        requireActivity().runOnUiThread(() -> {
            String totalTimeSpent = "";
            if (overallUsageStats != null) {
                totalTimeSpent = String.format(Locale.ENGLISH, DateAndTimeUtils.convertMillisecondsToHoursAndMinutes(
                        overallUsageStats.getTotalTimeInForeground(),
                        getString(R.string.achievements_hours),
                        getString(R.string.achievements_minutes)
                ));
            } else {
                totalTimeSpent = "0h 0m";
            }
            tvDailyTimeSpent.setText(totalTimeSpent);
        });
    }
}
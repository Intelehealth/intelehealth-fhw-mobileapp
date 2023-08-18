package org.intelehealth.unicef.activities.achievements.fragments;

import android.app.DatePickerDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.app.AppConstants;
import org.intelehealth.unicef.models.Patient;
import org.intelehealth.unicef.models.dto.EncounterDTO;
import org.intelehealth.unicef.models.dto.ObsDTO;
import org.intelehealth.unicef.models.dto.PatientAttributesDTO;
import org.intelehealth.unicef.models.dto.PatientDTO;
import org.intelehealth.unicef.utilities.DateAndTimeUtils;
import org.intelehealth.unicef.utilities.SessionManager;
import org.intelehealth.unicef.utilities.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DateRangeAchievementsFragment extends Fragment {
    private TextView tvStartDate;
    private TextView tvEndDate;
    private TextView tvRangePatientsCreated;
    private TextView tvRangeVisitsEnded;
    private TextView tvAveragePatientSatisfactionScore;
    private TextView tvTotalTimeSpentInRange;

    private String startDate;
    private String endDate;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private SessionManager sessionManager;
    private UsageStats overallUsageStats;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = ((MyAchievementsFragment) requireParentFragment()).sessionManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_date_range_achievements_ui2, container, false);
        initUI(view);
        return view;
    }

    private void initUI(View view) {
        LinearLayout selectStartDate = view.findViewById(R.id.layout_start_date);
        LinearLayout selectEndDate = view.findViewById(R.id.layout_end_date);

        tvStartDate = view.findViewById(R.id.tv_start_date);
        tvEndDate = view.findViewById(R.id.tv_end_date);
        tvRangePatientsCreated = view.findViewById(R.id.tv_range_patients_created);
        tvRangeVisitsEnded = view.findViewById(R.id.tv_range_visits_ended);
        tvAveragePatientSatisfactionScore = view.findViewById(R.id.tv_average_patient_satisfaction_score);
        tvTotalTimeSpentInRange = view.findViewById(R.id.tv_time_spent_in_range);

        startDate = DateAndTimeUtils.getTodaysDateInRequiredFormat("dd MMM, yyyy");
        endDate = DateAndTimeUtils.getTodaysDateInRequiredFormat("dd MMM, yyyy");

        String displayStartDate = "", displayEndDate = "";

        if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
            displayStartDate = StringUtils.getFullMonthName(startDate);
            displayStartDate = StringUtils.en__ru_dob(displayStartDate);
            displayEndDate = StringUtils.getFullMonthName(endDate);
            displayEndDate = StringUtils.en__ru_dob(displayEndDate);
        } else {
            displayStartDate = StringUtils.getFullMonthName(startDate);
            displayEndDate = StringUtils.getFullMonthName(endDate);
        }

        tvStartDate.setText(displayStartDate);
        tvEndDate.setText(displayEndDate);

        selectStartDate.setOnClickListener(v -> selectDate(tvStartDate));
        selectEndDate.setOnClickListener(v -> selectDate(tvEndDate));
        fetchAndSetUIData();
    }

    private void selectDate(TextView textView) {
        String date = textView.getText().toString();
        Calendar calendar = DateAndTimeUtils.convertStringToCalendarObject(date, "dd MMM, yyyy");

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), R.style.datepicker, (datePicker, year, month, day) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(Calendar.YEAR, year);
            newDate.set(Calendar.MONTH, month);
            newDate.set(Calendar.DAY_OF_MONTH, day);

            Date selectedDate = newDate.getTime();
            String selectedDateString = DateAndTimeUtils.convertDateObjectToString(selectedDate, "dd MMM, yyyy");
            if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                selectedDateString = StringUtils.getFullMonthName(selectedDateString);
                selectedDateString = StringUtils.en__ru_dob(selectedDateString);
            }

            textView.setText(selectedDateString);
            fetchAndSetUIData();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        DatePicker datePicker = datePickerDialog.getDatePicker();
        datePicker.setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void fetchAndSetUIData() {
        startDate = tvStartDate.getText().toString();
        endDate = tvEndDate.getText().toString();

        if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
            startDate = StringUtils.hi_or_bn_en_noEdit(startDate, "ru");
            startDate = StringUtils.getShortMonthName(startDate);
            endDate = StringUtils.hi_or_bn_en_noEdit(endDate, "ru");
            endDate = StringUtils.getShortMonthName(endDate);
        } else {
            startDate = StringUtils.getShortMonthName(startDate);
            endDate = StringUtils.getShortMonthName(endDate);
        }

        executorService.execute(() -> {
            setPatientsCreatedInRange();
            setVisitsEndedInRange();
            setAveragePatientSatisfactionScore();
            setTimeSpentInRange();
        });
    }

    private void setPatientsCreatedInRange() {
        List<String> patientDTOList = new ArrayList<>();

        String patientsCreatedTodayQuery = "SELECT date_created FROM tbl_patient WHERE creator_uuid = ?";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
        final Cursor rangePatientsCreatedCursor = db.rawQuery(patientsCreatedTodayQuery, new String[]{sessionManager.getCreatorID()});

        if (rangePatientsCreatedCursor.moveToFirst()) {
            do {
                String dateCreated = rangePatientsCreatedCursor.getString(rangePatientsCreatedCursor.getColumnIndexOrThrow("date_created"));
                patientDTOList.add(dateCreated);
            } while (rangePatientsCreatedCursor.moveToNext());
        }

        int numberOfPatients = 0;
        if (!patientDTOList.isEmpty()) {
            numberOfPatients = countPatientsCreatedBetweenRange(patientDTOList);
        }

        int finalCount = numberOfPatients;
        requireActivity().runOnUiThread(() -> tvRangePatientsCreated.setText(String.valueOf(finalCount)));
        rangePatientsCreatedCursor.close();
    }

    private void setVisitsEndedInRange() {
        int numberOfVisitsEnded = 0;

        List<EncounterDTO> encounterDTOList = new ArrayList<>();
        String visitEndedQuery = "SELECT DISTINCT visituuid, modified_date FROM tbl_encounter WHERE provider_uuid = ?  AND encounter_type_uuid = \"629a9d0b-48eb-405e-953d-a5964c88dc30\"";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
        final Cursor rangePatientsCreatedCursor = db.rawQuery(visitEndedQuery, new String[]{sessionManager.getProviderID()});

        if (rangePatientsCreatedCursor.moveToFirst()) {
            do {
                String visitUuid = rangePatientsCreatedCursor.getString(rangePatientsCreatedCursor.getColumnIndexOrThrow("visituuid"));
                String encounterTime = rangePatientsCreatedCursor.getString(rangePatientsCreatedCursor.getColumnIndexOrThrow("modified_date"));
                EncounterDTO encounterDTO = new EncounterDTO();
                encounterDTO.setVisituuid(visitUuid);
                encounterDTO.setEncounterTime(encounterTime);
                encounterDTOList.add(encounterDTO);
            } while (rangePatientsCreatedCursor.moveToNext());

            if (!encounterDTOList.isEmpty()) {
                numberOfVisitsEnded = countVisitsEndedBetweenRange(encounterDTOList);
            }
        }

        int finalCount = numberOfVisitsEnded;
        requireActivity().runOnUiThread(() -> tvRangeVisitsEnded.setText(String.valueOf(finalCount)));
        rangePatientsCreatedCursor.close();
    }

    private void setAveragePatientSatisfactionScore() {
        List<ObsDTO> obsList = new ArrayList<>();
        String query = "SELECT value, modified_date FROM tbl_obs WHERE conceptuuid = \"78284507-fb71-4354-9b34-046ab205e18f\" AND encounteruuid IN (SELECT uuid FROM tbl_encounter WHERE provider_uuid = ?)";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
        final Cursor rangeAverageSatisfactionCursor = db.rawQuery(query, new String[]{sessionManager.getProviderID()});
        double averageScore = 0.0;

        if (rangeAverageSatisfactionCursor.moveToFirst()) {
            do {
                String value = rangeAverageSatisfactionCursor.getString(rangeAverageSatisfactionCursor.getColumnIndexOrThrow("value"));
                String obsTime = rangeAverageSatisfactionCursor.getString(rangeAverageSatisfactionCursor.getColumnIndexOrThrow("modified_date"));
                ObsDTO obsDTO = new ObsDTO();
                obsDTO.setValue(value);
                obsDTO.setObsServerModifiedDate(obsTime);
                obsList.add(obsDTO);
            } while (rangeAverageSatisfactionCursor.moveToNext());

            if (!obsList.isEmpty()) {
                averageScore = getAveragePatientSatisfactionScore(obsList);
            }
        }

        double finalAverageScore = averageScore;
        requireActivity().runOnUiThread(() -> tvAveragePatientSatisfactionScore.setText(StringUtils.formatDoubleValues(finalAverageScore)));
        rangeAverageSatisfactionCursor.close();
    }

    private void setTimeSpentInRange() {
        long firstLoginTimeInMilliseconds = DateAndTimeUtils.convertStringDateToMilliseconds(sessionManager.getFirstProviderLoginTime(), "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        long startTimeInMilliseconds = DateAndTimeUtils.convertStringDateToMilliseconds(startDate, "dd MMM, yyyy");

        long finalEndTimeInMs = DateAndTimeUtils.getEndDateInMilliseconds(endDate, "dd MMM, yyyy");
        long finalStartTimeInMs = Math.max(firstLoginTimeInMilliseconds, startTimeInMilliseconds);

        UsageStatsManager usageStatsManager = ((MyAchievementsFragment) requireParentFragment()).usageStatsManager;
        Map<String, UsageStats> aggregateStatsMap = usageStatsManager.queryAndAggregateUsageStats(finalStartTimeInMs, finalEndTimeInMs);
        overallUsageStats = aggregateStatsMap.get("org.intelehealth.unicef");

        requireActivity().runOnUiThread(() -> {
            String totalTimeSpent = "";
            if (overallUsageStats != null) {
                totalTimeSpent = String.format(Locale.ENGLISH, DateAndTimeUtils.convertMillisecondsToHoursAndMinutes(overallUsageStats.getTotalTimeInForeground()));
            } else {
                totalTimeSpent = "0h 0m";
            }
            tvTotalTimeSpentInRange.setText(totalTimeSpent);
        });
    }

    private int countPatientsCreatedBetweenRange(List<String> patientDTOList) {
        int numberOfPatients = 0;
        for (String date : patientDTOList) {
            String convertedDate = DateAndTimeUtils.formatDateFromOnetoAnother(date, "yyyy-MM-dd hh:mm:ss", "dd MMM, yyyy");
            if (DateAndTimeUtils.isGivenDateBetweenTwoDates(convertedDate, startDate, endDate, "dd MMM, yyyy")) {
                numberOfPatients++;
            }
        }
        return numberOfPatients;
    }

    private int countVisitsEndedBetweenRange(List<EncounterDTO> encounterDTOList) {
        int numberOfVisitsEnded = 0;
        for (EncounterDTO dto : encounterDTOList) {
            String tempDate = DateAndTimeUtils.formatDateFromOnetoAnother(dto.getEncounterTime(), "yyyy-MM-dd'T'hh:mm:ss", "dd MMM, yyyy");
            if (DateAndTimeUtils.isGivenDateBetweenTwoDates(tempDate, startDate, endDate, "dd MMM, yyyy")) {
                numberOfVisitsEnded++;
            }
        }
        return numberOfVisitsEnded;
    }

    private double getAveragePatientSatisfactionScore(List<ObsDTO> obsDTOList) {
        double totalScore = 0.0;
        int numberOfObservations = 0;
        for (ObsDTO dto : obsDTOList) {
            String tempDate = DateAndTimeUtils.formatDateFromOnetoAnother(dto.getObsServerModifiedDate(), "yyyy-MM-dd'T'hh:mm:ss", "dd MMM, yyyy");
            if (DateAndTimeUtils.isGivenDateBetweenTwoDates(tempDate, startDate, endDate, "dd MMM, yyyy")) {
                totalScore += Double.parseDouble(dto.getValue());
                numberOfObservations++;
            }
        }

        if (totalScore == 0.0 || numberOfObservations == 0)
            return 0;
        else
            return totalScore / numberOfObservations;
    }
}



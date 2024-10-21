package org.intelehealth.app.activities.achievements.fragments;

import android.app.DatePickerDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.intelehealth.app.R;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UuidDictionary;

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
        setLocale(getContext());
        sessionManager = new SessionManager(requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_date_range_achievements_ui2, container, false);
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

    private void initUI(View view) {
        LinearLayout selectStartDate = view.findViewById(R.id.layout_start_date);
        LinearLayout selectEndDate = view.findViewById(R.id.layout_end_date);

        tvStartDate = view.findViewById(R.id.tv_start_date);
        tvEndDate = view.findViewById(R.id.tv_end_date);
        tvRangePatientsCreated = view.findViewById(R.id.tv_range_patients_created);
        tvRangeVisitsEnded = view.findViewById(R.id.tv_range_visits_ended);
        tvAveragePatientSatisfactionScore = view.findViewById(R.id.tv_average_patient_satisfaction_score);
        tvTotalTimeSpentInRange = view.findViewById(R.id.tv_time_spent_in_range);

        tvStartDate.setText(DateAndTimeUtils.getYesterdaysDateInRequiredFormat("dd MMM, yyyy", sessionManager.getAppLanguage()));
        tvEndDate.setText(DateAndTimeUtils.getTodaysDateInRequiredFormat("dd MMM, yyyy", sessionManager.getAppLanguage()));

        selectStartDate.setOnClickListener(v -> selectDate(tvStartDate, "startDate"));
        selectEndDate.setOnClickListener(v -> selectDate(tvEndDate, "endDate"));
        fetchAndSetUIData();
    }

    private void selectDate(TextView textView, String value) {
        String maxDateforStart = tvEndDate.getText().toString();
        String minDateforEnd = tvStartDate.getText().toString();
        Calendar maxDateforStartCal = DateAndTimeUtils.convertStringToCalendarObject(maxDateforStart, "dd MMM, yyyy", sessionManager.getAppLanguage());
        Calendar minDateforEndCal = DateAndTimeUtils.convertStringToCalendarObject(minDateforEnd, "dd MMM, yyyy", sessionManager.getAppLanguage());
        String date = textView.getText().toString();
        Calendar calendar = DateAndTimeUtils.convertStringToCalendarObject(date, "dd MMM, yyyy", sessionManager.getAppLanguage());

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), R.style.datepicker, (datePicker, year, month, day) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(Calendar.YEAR, year);
            newDate.set(Calendar.MONTH, month);
            newDate.set(Calendar.DAY_OF_MONTH, day);

            Date selectedDate = newDate.getTime();
            textView.setText(DateAndTimeUtils.convertDateObjectToString(selectedDate, "dd MMM, yyyy"));
            fetchAndSetUIData();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        DatePicker datePicker = datePickerDialog.getDatePicker();
        if (value.equalsIgnoreCase("startDate"))
            datePicker.setMaxDate(maxDateforStartCal.getTimeInMillis());
        else if (value.equalsIgnoreCase("endDate")) {
            datePicker.setMaxDate(System.currentTimeMillis());
            datePicker.setMinDate(minDateforEndCal.getTimeInMillis());
        }

        datePickerDialog.show();

        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
                .setTextColor(
                        ContextCompat.getColor(
                                requireContext(),
                                R.color.colorPrimary
                        )
                ); // Change to your desired color

        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));

    }

    private void fetchAndSetUIData() {
        startDate = tvStartDate.getText().toString();
        endDate = tvEndDate.getText().toString();

        executorService.execute(() -> {
            setPatientsCreatedInRange();
            setVisitsEndedInRange();
            setAveragePatientSatisfactionScore();
            setTimeSpentInRange();
        });
    }

    private void setPatientsCreatedInRange() {
        List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();

        String patientsCreatedTodayQuery = "SELECT DISTINCT(patientuuid), value FROM tbl_patient_attribute WHERE patientuuid IN (SELECT DISTINCT(patientuuid) FROM tbl_patient_attribute WHERE person_attribute_type_uuid = \"84f94425-789d-4293-a0d8-9dc01dbb4f07\" AND value = ?) AND person_attribute_type_uuid = \"ffc8ebee-f70c-4743-bc3c-2fe4ac843245\" ";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        final Cursor rangePatientsCreatedCursor = db.rawQuery(patientsCreatedTodayQuery, new String[]{sessionManager.getProviderID()});

        if (rangePatientsCreatedCursor.moveToFirst()) {
            do {
                String patientUuid = rangePatientsCreatedCursor.getString(rangePatientsCreatedCursor.getColumnIndexOrThrow("patientuuid"));
                String value = rangePatientsCreatedCursor.getString(rangePatientsCreatedCursor.getColumnIndexOrThrow("value"));
                PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setPatientuuid(patientUuid);
                patientAttributesDTO.setValue(value);
                patientAttributesDTOList.add(patientAttributesDTO);
            } while (rangePatientsCreatedCursor.moveToNext());
        }

        int numberOfPatients = 0;
        if (!patientAttributesDTOList.isEmpty()) {
            numberOfPatients = countPatientsCreatedBetweenRange(patientAttributesDTOList);
        }

        int finalCount = numberOfPatients;
        requireActivity().runOnUiThread(() -> tvRangePatientsCreated.setText(String.valueOf(finalCount)));
        rangePatientsCreatedCursor.close();
    }

    private void setVisitsEndedInRange() {
        //int numberOfVisitsEnded = 0;

        String startDate = DateAndTimeUtils.getDateTimeFromTimestamp(DateAndTimeUtils.getTimeStampFromString(tvStartDate.getText().toString(), "dd MMM, yyyy"), "yyyy-MM-dd");
        String endDate = DateAndTimeUtils.getDateTimeFromTimestamp(DateAndTimeUtils.getTimeStampFromString(tvEndDate.getText().toString(), "dd MMM, yyyy"), "yyyy-MM-dd");

        //normally sqlite doesn't support filter for "MMM d, yyyy" this date format
        //that's why here added two logics for date filter
        //if sync status = 1 then the date format is "MMM d, yyyy"
        //and sync status = 0 then the date format is "yyyy-MM-dd"

        //ex date if sync is 1: Sep 1, 2024
        //ex date if sync is 0: 2024-09-01

        //whenever we substr(v.enddate, 5, 2) sometimes the result is like "1," for (Sep 1, 2024)  and "11" for (Sep 11, 2024)
        //here to check "," exist or not, added instr function
        //if the function returns 0 then we are taking substr(v.enddate, 5, 2). output will be like "11" Ex date:Sep 11, 2024
        //if the function returns > 0 then we are taking substr(v.enddate, 5, 1). output will be like "1" Ex date:Sep 1, 2024
        String formattedDay = "(CASE " +
                "WHEN instr(substr(v.enddate, 5, 2), ',') > 0 THEN substr(v.enddate, 5, 1) " +
                "ELSE substr(v.enddate, 5, 2) " +
                "END)";

        //as we know sqlite doesn't support "Sep 11, 2024" this format for filter
        //we are formatting month and year here to "yyyy-MM-dd" this format
        //converting month text to digit
        String formattedEndDate = "(CASE WHEN v.sync = 1 THEN REPLACE((substr(v.enddate, 8, 5) || '-' || " +
                "(CASE substr(v.enddate, 1, 3) " +
                "WHEN 'Jan' THEN '01' " +
                "WHEN 'Feb' THEN '02' " +
                "WHEN 'Mar' THEN '03' " +
                "WHEN 'Apr' THEN '04' " +
                "WHEN 'May' THEN '05' " +
                "WHEN 'Jun' THEN '06' " +
                "WHEN 'Jul' THEN '07' " +
                "WHEN 'Aug' THEN '08' " +
                "WHEN 'Sep' THEN '09' " +
                "WHEN 'Oct' THEN '10' " +
                "WHEN 'Nov' THEN '11' " +
                "WHEN 'Dec' THEN '12' " +
                "END) || '-' || " +
                //checking length of the formatted date here
                //if length is 1 then adding another 0 before the digit
                //if length is more than 1, that means it's in correct format
                "CASE WHEN LENGTH(" + formattedDay + ") = 1 THEN '0'||" + formattedDay + " ELSE " + formattedDay + " END ),' ','') else substr(v.enddate,1,10) END)";

        //if the end date is "Sep 11, 2024" then the final output will be "11-09-2024" for formattedEndDate

        String visitEndedQuery = "SELECT COUNT(DISTINCT visituuid) FROM tbl_encounter as e, tbl_visit as v " +
                "WHERE e.visituuid = v.uuid AND e.provider_uuid = ? " +
                "AND e.encounter_type_uuid = '" + UuidDictionary.ENCOUNTER_PATIENT_EXIT_SURVEY + "' " +
                "AND " + formattedEndDate + " >= '" + startDate + "' and " + formattedEndDate + "<= '" + endDate + "'";

        CustomLog.d("visitEndedQuery", "" + visitEndedQuery);

        //String visitEndedQuery = "SELECT DISTINCT visituuid, modified_date FROM tbl_encounter WHERE provider_uuid = ?  AND encounter_type_uuid = \"629a9d0b-48eb-405e-953d-a5964c88dc30\"";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        final Cursor rangePatientsCreatedCursor = db.rawQuery(visitEndedQuery, new String[]{sessionManager.getProviderID()});

        rangePatientsCreatedCursor.moveToFirst();
        String count = rangePatientsCreatedCursor.getString(rangePatientsCreatedCursor.getColumnIndex(rangePatientsCreatedCursor.getColumnName(0)));


        /*if (rangePatientsCreatedCursor.moveToFirst()) {
            do {
                String visitUuid = rangePatientsCreatedCursor.getString(rangePatientsCreatedCursor.getColumnIndexOrThrow("visituuid"));
                //String encounterTime = rangePatientsCreatedCursor.getString(rangePatientsCreatedCursor.getColumnIndexOrThrow("modified_date"));
                EncounterDTO encounterDTO = new EncounterDTO();
                encounterDTO.setVisituuid(visitUuid);
                //encounterDTO.setEncounterTime(encounterTime);
                encounterDTOList.add(encounterDTO);

                CustomLog.d("VISIT_UID",""+visitUuid);

            } while (rangePatientsCreatedCursor.moveToNext());

            if (!encounterDTOList.isEmpty()) {
                numberOfVisitsEnded = countVisitsEndedBetweenRange(encounterDTOList);
            }
        }
*/
        //int finalCount = numberOfVisitsEnded;
        requireActivity().runOnUiThread(() -> tvRangeVisitsEnded.setText(count));
        rangePatientsCreatedCursor.close();
    }

    private void setAveragePatientSatisfactionScore() {
        List<ObsDTO> obsList = new ArrayList<>();
        String query = "SELECT value, modified_date FROM tbl_obs WHERE conceptuuid = \"78284507-fb71-4354-9b34-046ab205e18f\" AND encounteruuid IN (SELECT uuid FROM tbl_encounter WHERE provider_uuid = ?)";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
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
        long firstLoginTimeInMilliseconds = DateAndTimeUtils.convertStringDateToMilliseconds(sessionManager.getFirstProviderLoginTime(), "yyyy-MM-dd'T'HH:mm:ss.SSSZ", sessionManager.getAppLanguage());
        long startTimeInMilliseconds = DateAndTimeUtils.convertStringDateToMilliseconds(startDate, "dd MMM, yyyy", sessionManager.getAppLanguage());

        long finalEndTimeInMs = DateAndTimeUtils.getEndDateInMilliseconds(endDate, "dd MMM, yyyy", sessionManager.getAppLanguage());
        long finalStartTimeInMs = Math.max(firstLoginTimeInMilliseconds, startTimeInMilliseconds);

        UsageStatsManager usageStatsManager = ((MyAchievementsFragment) requireParentFragment()).usageStatsManager;
        Map<String, UsageStats> aggregateStatsMap = usageStatsManager.queryAndAggregateUsageStats(finalStartTimeInMs, finalEndTimeInMs);
        overallUsageStats = aggregateStatsMap.get("org.intelehealth.app");

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

    private int countPatientsCreatedBetweenRange(List<PatientAttributesDTO> patientAttributesDTOList) {
        int numberOfPatients = 0;
        for (PatientAttributesDTO dto : patientAttributesDTOList) {
            if (DateAndTimeUtils.isGivenDateBetweenTwoDates(dto.getValue(), startDate, endDate, "dd MMM, yyyy", sessionManager.getAppLanguage())) {
                numberOfPatients++;
            }
        }
        return numberOfPatients;
    }

    private int countVisitsEndedBetweenRange(List<EncounterDTO> encounterDTOList) {
        int numberOfVisitsEnded = 0;
        for (EncounterDTO dto : encounterDTOList) {
            String tempDate = DateAndTimeUtils.formatDateFromOnetoAnother(dto.getEncounterTime(), "yyyy-MM-dd'T'hh:mm:ss", "dd MMM, yyyy");
            if (DateAndTimeUtils.isGivenDateBetweenTwoDates(tempDate, startDate, endDate, "dd MMM, yyyy", sessionManager.getAppLanguage())) {
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
            if (DateAndTimeUtils.isGivenDateBetweenTwoDates(tempDate, startDate, endDate, "dd MMM, yyyy", sessionManager.getAppLanguage())) {
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



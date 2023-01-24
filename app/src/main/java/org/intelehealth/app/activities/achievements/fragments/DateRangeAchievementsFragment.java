package org.intelehealth.app.activities.achievements.fragments;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.ObsImageModel.Encounter;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DateRangeAchievementsFragment extends Fragment {
    private TextView tvStartDate;
    private TextView tvEndDate;
    private TextView tvRangePatientsCreated;
    private TextView tvRangeVisitsEnded;
    private TextView tvAveragePatientSatisfactionScore;

    private String startDate;
    private String endDate;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private SessionManager sessionManager;

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

        tvStartDate.setText(DateAndTimeUtils.getTodaysDateInRequiredFormat("dd MMM, yyyy"));
        tvEndDate.setText(DateAndTimeUtils.getTodaysDateInRequiredFormat("dd MMM, yyyy"));

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
            textView.setText(DateAndTimeUtils.convertDateObjectToString(selectedDate, "dd MMM, yyyy"));
            fetchAndSetUIData();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void fetchAndSetUIData() {
        startDate = tvStartDate.getText().toString();
        endDate = tvEndDate.getText().toString();

        executorService.execute(() -> {
            setPatientsCreatedInRange();
            setVisitsEndedInRange();
            setAveragePatientSatisfactionScore();
        });
    }

    private void setPatientsCreatedInRange() {
        List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();

        String patientsCreatedTodayQuery = "SELECT DISTINCT(patientuuid), value FROM tbl_patient_attribute WHERE patientuuid IN (SELECT DISTINCT(patientuuid) FROM tbl_patient_attribute WHERE person_attribute_type_uuid = \"84f94425-789d-4293-a0d8-9dc01dbb4f07\" AND value = ?) AND person_attribute_type_uuid = \"ffc8ebee-f70c-4743-bc3c-2fe4ac843245\" ";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
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

            int numberOfVisitsEnded = 0;
            if (!encounterDTOList.isEmpty()) {
                numberOfVisitsEnded = countVisitsEndedBetweenRange(encounterDTOList);
            }

            int finalCount = numberOfVisitsEnded;
            requireActivity().runOnUiThread(() -> tvRangeVisitsEnded.setText(String.valueOf(finalCount)));
            rangePatientsCreatedCursor.close();
        }
    }

    private void setAveragePatientSatisfactionScore() {
        List<ObsDTO> obsList = new ArrayList<>();
        String query = "SELECT value, modified_date FROM tbl_obs WHERE conceptuuid = \"78284507-fb71-4354-9b34-046ab205e18f\" AND encounteruuid IN (SELECT uuid FROM tbl_encounter WHERE provider_uuid = ?)";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
        final Cursor rangeAverageSatisfactionCursor = db.rawQuery(query, new String[]{sessionManager.getProviderID()});

        if (rangeAverageSatisfactionCursor.moveToFirst()) {
            do {
                String value = rangeAverageSatisfactionCursor.getString(rangeAverageSatisfactionCursor.getColumnIndexOrThrow("value"));
                String obsTime = rangeAverageSatisfactionCursor.getString(rangeAverageSatisfactionCursor.getColumnIndexOrThrow("modified_date"));
                ObsDTO obsDTO = new ObsDTO();
                obsDTO.setValue(value);
                obsDTO.setObsServerModifiedDate(obsTime);
                obsList.add(obsDTO);
            } while (rangeAverageSatisfactionCursor.moveToNext());

            double averageScore = 0.0;
            if (!obsList.isEmpty()) {
                averageScore = getAveragePatientSatisfactionScore(obsList);
            }

            double finalAverageScore = averageScore;
            requireActivity().runOnUiThread(() -> tvAveragePatientSatisfactionScore.setText(StringUtils.formatDoubleValues(finalAverageScore)));
            rangeAverageSatisfactionCursor.close();
        }
    }

    private int countPatientsCreatedBetweenRange(List<PatientAttributesDTO> patientAttributesDTOList) {
        int numberOfPatients = 0;
        for (PatientAttributesDTO dto : patientAttributesDTOList) {
            if (DateAndTimeUtils.isGivenDateBetweenTwoDates(dto.getValue(), startDate, endDate, "dd MMM, yyyy")) {
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

        return totalScore / numberOfObservations;
    }
}



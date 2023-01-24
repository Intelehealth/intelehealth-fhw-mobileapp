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
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DateRangeAchievementsFragment extends Fragment {
    private LinearLayout selectStartDate;
    private LinearLayout selectEndDate;
    private DatePickerDialog datePickerDialog;
    private TextView tvStartDate;
    private TextView tvEndDate;
    private TextView tvRangePatientsCreated;

    private String startDate;
    private String endDate;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

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
        selectStartDate = view.findViewById(R.id.layout_start_date);
        selectEndDate = view.findViewById(R.id.layout_end_date);

        tvStartDate = view.findViewById(R.id.tv_start_date);
        tvEndDate = view.findViewById(R.id.tv_end_date);
        tvRangePatientsCreated = view.findViewById(R.id.tv_range_patients_created);

        tvStartDate.setText(DateAndTimeUtils.getTodaysDateInRequiredFormat("dd MMM, yyyy"));
        tvEndDate.setText(DateAndTimeUtils.getTodaysDateInRequiredFormat("dd MMM, yyyy"));

        selectStartDate.setOnClickListener(v -> selectDate(tvStartDate));
        selectEndDate.setOnClickListener(v -> selectDate(tvEndDate));
        fetchAndSetUIData();
    }


    private void selectDate(TextView textView) {
        String date = textView.getText().toString();
        Calendar calendar = DateAndTimeUtils.convertStringToCalendarObject(date, "dd MMM, yyyy");

        datePickerDialog = new DatePickerDialog(getActivity(), R.style.datepicker, (datePicker, year, month, day) -> {
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
}



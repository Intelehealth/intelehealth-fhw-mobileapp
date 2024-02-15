package org.intelehealth.ekalarogya.activities.surveyActivity.fragments;

import static org.intelehealth.ekalarogya.activities.surveyActivity.SurveyActivity.patientAttributesDTOList;
import static org.intelehealth.ekalarogya.utilities.StringUtils.setSelectedCheckboxes;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.activities.surveyActivity.SurveyActivity;
import org.intelehealth.ekalarogya.app.AppConstants;
import org.intelehealth.ekalarogya.database.dao.PatientsDAO;
import org.intelehealth.ekalarogya.databinding.FragmentSecondScreenBinding;
import org.intelehealth.ekalarogya.models.dto.PatientAttributesDTO;
import org.intelehealth.ekalarogya.utilities.Logger;
import org.intelehealth.ekalarogya.utilities.SessionManager;
import org.intelehealth.ekalarogya.utilities.StringUtils;
import org.intelehealth.ekalarogya.utilities.exception.DAOException;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class SecondScreenFragment extends Fragment {

    private FragmentSecondScreenBinding binding;
    private String patientUuid;
    private final PatientsDAO patientsDAO = new PatientsDAO();
    private Resources updatedResources = null;
    private SessionManager sessionManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = requireActivity().getIntent();
        if (intent != null)
            patientUuid = intent.getStringExtra("patientUuid");
        updatedResources = ((SurveyActivity) requireActivity()).getUpdatedResources();
        sessionManager = ((SurveyActivity) requireActivity()).getSessionManager();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSecondScreenBinding.inflate(inflater, container, false);
        setListeners();
        setData(patientUuid);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setListeners() {

    }

    private void insertData() throws DAOException {
        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO;

        // mgnregaCardStatus
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("mgnregaCardStatus"));
        patientAttributesDTO.setValue(StringUtils.getSurveyStrings(
                ((RadioButton) binding.mgnregaRadioGroup.findViewById(binding.mgnregaRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                requireContext().getResources(),
                updatedResources,
                sessionManager.getAppLanguage()
        ));
        patientAttributesDTOList.add(patientAttributesDTO);

        boolean isPatientUpdated = patientsDAO.surveyUpdatePatientToDB(patientUuid, patientAttributesDTOList);
        Logger.logD("TAG", String.valueOf(isPatientUpdated));

        getParentFragmentManager().beginTransaction()
                .replace(R.id.frame_layout_container, new ThirdScreenFragment())
                .addToBackStack(null)
                .commit();
    }

    private boolean areFieldsValid() {
        AtomicBoolean validations = new AtomicBoolean(true);

        // Validation for MGNREGA Radio Group
        if (binding.mgnregaRadioGroup.getCheckedRadioButtonId() == -1) {
            validations.set(false);
            return validations.get();
        }

        return validations.get();
    }

    private void setData(String patientUuid) {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        String patientSelection1 = "patientuuid = ?";
        String[] patientArgs1 = {patientUuid};
        String[] patientColumns1 = {"value", "person_attribute_type_uuid"};
        final Cursor idCursor1 = db.query("tbl_patient_attribute", patientColumns1, patientSelection1, patientArgs1, null, null, null);
        String name = "";

        if (idCursor1.moveToFirst()) {
            do {
                try {
                    name = patientsDAO.getAttributesName(idCursor1.getString(idCursor1.getColumnIndexOrThrow("person_attribute_type_uuid")));
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

                // mgnregaCardStatus
                if (name.equalsIgnoreCase("mgnregaCardStatus")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.mgnregaRadioGroup, value1, updatedResources, requireContext().getResources(), sessionManager.getAppLanguage());
                    }
                }
            } while (idCursor1.moveToNext());
        }

        idCursor1.close();
    }
}

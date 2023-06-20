package org.intelehealth.ezazi.ui.patient;

import android.util.Log;

import org.intelehealth.ezazi.activities.searchPatientActivity.SearchPatientActivity;
import org.intelehealth.ezazi.activities.searchPatientActivity.SearchPatientAdapter;
import org.intelehealth.ezazi.database.dao.EncounterDAO;
import org.intelehealth.ezazi.database.dao.ObsDAO;
import org.intelehealth.ezazi.database.dao.VisitsDAO;
import org.intelehealth.ezazi.executor.TaskCompleteListener;
import org.intelehealth.ezazi.executor.TaskExecutor;
import org.intelehealth.ezazi.models.dto.EncounterDTO;
import org.intelehealth.ezazi.models.dto.PatientDTO;

import java.util.List;

/**
 * Created by Vaghela Mithun R. on 20-06-2023 - 14:18.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class PatientStageBinder {
    private static final String TAG = "PatientStageBinder";
    private OnProcessCompleteListener listener;
    private final TaskExecutor<List<PatientDTO>> executor = new TaskExecutor<>();

    public interface OnProcessCompleteListener {
        void onCompleted(List<PatientDTO> patients);
    }

    public void bindStage(List<PatientDTO> patients, OnProcessCompleteListener listener) {
        this.listener = listener;
        executor.executeTask(new TaskCompleteListener<List<PatientDTO>>() {
            @Override
            public List<PatientDTO> call() throws Exception {
                for (int i = 0; i < patients.size(); i++) {
                    PatientDTO patientDTO = patients.get(i);
                    String stage = getCompletedVisitStage(patientDTO);
                    Log.e(TAG, "Call result=>" + stage);
                    patientDTO.setStage(stage);
                }
//                for (PatientDTO patient : patients) {
//
//                    Log.e(TAG, "Call result=>" + stage);
//                }
                return patients;
            }

            @Override
            public void onComplete(List<PatientDTO> result) {
                TaskCompleteListener.super.onComplete(result);
                findPatientCurrentState(result, 0);
            }
        });
    }


    private void findPatientCurrentState(List<PatientDTO> patients, int index) {

        if (patients.size() == 0) return;
        final int[] currentIndex = {index};

        executor.executeTask(new TaskCompleteListener<List<PatientDTO>>() {
            @Override
            public List<PatientDTO> call() throws Exception {
                PatientDTO patient = patients.get(currentIndex[0]);
                if (patient.getStage() != null && patient.getStage().length() > 0 &&
                        (patient.getVisitUuid() == null || patient.getVisitUuid().length() == 0)) {
                    currentIndex[0] = currentIndex[0] + 1;
                    if (currentIndex[0] < patients.size() - 1) {
                        patient = patients.get(currentIndex[0]);
                    }
                }

                getStage(patient);
                return patients;
            }

            @Override
            public void onComplete(List<PatientDTO> result) {
                TaskCompleteListener.super.onComplete(result);
                Log.e(TAG, "onComplete: stage");
                if (index < result.size() - 1) {
                    findPatientCurrentState(result, currentIndex[0] + 1);
                } else {
                    listener.onCompleted(result);
                }
            }
        });
    }

    private void getStage(PatientDTO patient) {
        if (patient.getVisitUuid() == null || patient.getVisitUuid().length() == 0) return;
        EncounterDAO encounterDAO = new EncounterDAO();
        EncounterDTO encounterDTO = encounterDAO.getEncounterByVisitUUIDLimit1(patient.getVisitUuid()); // get latest encounter by visit uuid
        Log.e(TAG, "encounterDTO Id =>" + encounterDTO.getEncounterTypeUuid());
        if (encounterDTO.getEncounterTypeUuid() != null) {
            String latestEncounterName = new EncounterDAO().getEncounterTypeNameByUUID(encounterDTO.getEncounterTypeUuid());
            Log.e(TAG, "latestEncounterName =>" + latestEncounterName);
            Log.e(TAG, "Patient =>" + patient.getFullName());
            if (latestEncounterName.toLowerCase().contains("stage2")) {
                patient.setStage("Stage-2");
            } else if (latestEncounterName.toLowerCase().contains("stage1")) {
                patient.setStage("Stage-1");
            } else {
                patient.setStage("");
            }
        } else patient.setStage("");
    }

    private String getCompletedVisitStage(PatientDTO patient) {
        String visitUuid = new VisitsDAO().getPatientVisitUuid(patient.getUuid());
//        visitsDAO.fetchVisitUUIDFromPatientUUID(patient.getUuid());
        Log.e(TAG, "Visit Id =>" + visitUuid);
        Log.e(TAG, "Patient =>" + patient.getFullName());
        if (visitUuid != null && visitUuid.length() > 0) {
            patient.setVisitUuid(visitUuid);
            String completedEncounterId = new EncounterDAO().getCompletedEncounterId(visitUuid);
            Log.e(TAG, "completedEncounterId =>" + completedEncounterId);
            if (!completedEncounterId.equalsIgnoreCase("")) { // birthoutcome
                String birthoutcome = new ObsDAO().getCompletedBirthStageStatus(completedEncounterId);
                Log.e(TAG, "birthoutcome =>" + birthoutcome);
                if (!birthoutcome.equalsIgnoreCase("")) {
                    patient.setStage(birthoutcome);
                    Log.e(TAG, "Stage =>" + birthoutcome);
                    return birthoutcome;

                }
            }
        }

        return "";
    }
}

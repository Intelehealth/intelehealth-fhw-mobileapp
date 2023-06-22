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
                for (PatientDTO patient : patients) {
                    String stage = getCompletedVisitStage(patient);
                    if (stage != null && stage.length() > 0) {
                        patient.setStage(stage);
                    } else {
                        getStage(patient);
                    }
                }
                return patients;
            }

            @Override
            public void onComplete(List<PatientDTO> result) {
                TaskCompleteListener.super.onComplete(result);
                listener.onCompleted(result);
            }
        });
    }


//    private void findPatientCurrentState(final List<PatientDTO> patients, int index) {
//
//        if (patients.size() == 0) return;
//        final int[] currentIndex = {index};
//
//        executor.executeTask(new TaskCompleteListener<List<PatientDTO>>() {
//            @Override
//            public List<PatientDTO> call() throws Exception {
//                PatientDTO patient = patients.get(currentIndex[0]);
//                Log.e(TAG, "Patient -> " + patient.getFirstname());
//                Log.e(TAG, "Patient -> getStage " + patient.getStage());
//                if (patient.getStage() != null && patient.getStage().length() > 0 &&
//                        (patient.getVisitUuid() == null || patient.getVisitUuid().length() == 0)) {
//                    currentIndex[0] = currentIndex[0] + 1;
//                    if (currentIndex[0] < patients.size() - 1) {
//                        patient = patients.get(currentIndex[0]);
//                    }
//                }
//
//                getStage(patient);
//                return patients;
//            }
//
//            @Override
//            public void onComplete(List<PatientDTO> result) {
//                TaskCompleteListener.super.onComplete(result);
//                Log.e(TAG, "onComplete: stage");
//                if (currentIndex[0] < result.size() - 1) {
//                    findPatientCurrentState(result, currentIndex[0] + 1);
//                } else {
//                    listener.onCompleted(result);
//                }
//            }
//        });
//    }

    private void getStage(PatientDTO patient) {
        if (patient.getVisitUuid() == null || patient.getVisitUuid().length() == 0) return;
        Log.e(TAG, "VisitUuid Id =>" + patient.getVisitUuid());
        String latestEncounterName = new EncounterDAO().findCurrentStage(patient.getVisitUuid());
        Log.e(TAG, "latestEncounterName =>" + latestEncounterName);
        Log.e(TAG, "Patient =>" + patient.getFullName());
        if (latestEncounterName.toLowerCase().contains("stage2")) {
            patient.setStage("Stage-2");
        } else if (latestEncounterName.toLowerCase().contains("stage1")) {
            patient.setStage("Stage-1");
        } else {
            patient.setStage("");
        }
//        EncounterDTO encounterDTO = encounterDAO.getEncounterByVisitUUIDLimit1(patient.getVisitUuid()); // get latest encounter by visit uuid
//        Log.e(TAG, "encounterDTO Id =>" + encounterDTO.getUuid());
//        if (encounterDTO.getEncounterTypeUuid() != null) {
//
//        } else patient.setStage("");
    }

    private String getCompletedVisitStage(PatientDTO patient) {
        String visitUuid = new VisitsDAO().getPatientVisitUuid(patient.getUuid());
        if (visitUuid != null && visitUuid.length() > 0) {
            patient.setVisitUuid(visitUuid);
            String completedEncounterId = new EncounterDAO().getCompletedEncounterId(visitUuid);
            if (!completedEncounterId.equalsIgnoreCase("")) { // birthoutcome
                String birthoutcome = new ObsDAO().getCompletedVisitType(completedEncounterId);
                if (!birthoutcome.equalsIgnoreCase("")) {
                    patient.setStage(birthoutcome);
                    return birthoutcome;

                }
            }
        }

        return null;
    }
}

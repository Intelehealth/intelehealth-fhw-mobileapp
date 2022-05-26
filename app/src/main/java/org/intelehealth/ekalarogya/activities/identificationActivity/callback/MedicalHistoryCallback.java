package org.intelehealth.ekalarogya.activities.identificationActivity.callback;

import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.MedicalHistory;

public interface MedicalHistoryCallback {
    void saveMedicalHistoryData(MedicalHistory medicalHistory);

    void saveMedicalHistoryDataAtPosition(MedicalHistory medicalHistory, int position);
}

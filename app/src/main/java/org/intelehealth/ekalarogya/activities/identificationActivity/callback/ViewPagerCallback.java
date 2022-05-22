package org.intelehealth.ekalarogya.activities.identificationActivity.callback;

import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.MedicalHistory;

public interface ViewPagerCallback {
    void getMedicalHistory(MedicalHistory medicalHistory, int position);
}

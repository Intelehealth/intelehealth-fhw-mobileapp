package org.intelehealth.ekalarogya.activities.identificationActivity.callback;

import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.AlcoholConsumptionHistory;
import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.MedicalHistory;
import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.SmokingHistory;
import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.TobaccoHistory;

public interface ViewPagerCallback {
    void getMedicalHistory(MedicalHistory medicalHistory, int position);

    void getSmokingHistory(SmokingHistory smokingHistory, int position);

    void getTobaccoHistory(TobaccoHistory tobaccoHistory, int position);

    void getAlcoholHistory(AlcoholConsumptionHistory alcoholConsumptionHistory, int position);
}

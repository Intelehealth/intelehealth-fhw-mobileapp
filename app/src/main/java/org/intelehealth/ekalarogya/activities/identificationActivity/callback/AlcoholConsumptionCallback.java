package org.intelehealth.ekalarogya.activities.identificationActivity.callback;

import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.AlcoholConsumptionHistory;

public interface AlcoholConsumptionCallback {
    void saveAlcoholConsumptionData(AlcoholConsumptionHistory alcoholConsumptionHistory);
}
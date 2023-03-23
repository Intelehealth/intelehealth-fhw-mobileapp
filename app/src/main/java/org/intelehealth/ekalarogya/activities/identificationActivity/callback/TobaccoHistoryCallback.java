package org.intelehealth.ekalarogya.activities.identificationActivity.callback;

import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.SmokingHistory;
import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.TobaccoHistory;

public interface TobaccoHistoryCallback {

    void saveTobaccoHistory(TobaccoHistory tobaccoHistory);

    void saveTobaccoHistoryAtPosition(TobaccoHistory tobaccoHistory, int position);
}

package org.intelehealth.ekalarogya.activities.identificationActivity.callback;

import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.SmokingHistory;

public interface SmokingHistoryCallback {
    void saveSmokingHistory(SmokingHistory smokingHistory);

    void saveSmokingHistoryAtPosition(SmokingHistory smokingHistory, int position);
}

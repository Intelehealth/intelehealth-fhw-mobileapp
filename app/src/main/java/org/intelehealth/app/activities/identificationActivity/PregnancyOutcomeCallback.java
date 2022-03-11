package org.intelehealth.app.activities.identificationActivity;

public interface PregnancyOutcomeCallback {
    void savePregnancyData(PregnancyRosterData data);

    void savePregnancyDataAtPosition(PregnancyRosterData data, int position);
}
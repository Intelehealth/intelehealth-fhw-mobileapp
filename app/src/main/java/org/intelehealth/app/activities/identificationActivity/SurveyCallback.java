package org.intelehealth.app.activities.identificationActivity;

public interface SurveyCallback {
    void saveSurveyData(HealthIssues survey);

    void saveSurveyDataAtPosition(HealthIssues survey, int position);
}

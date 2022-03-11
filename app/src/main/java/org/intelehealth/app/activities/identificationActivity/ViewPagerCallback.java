package org.intelehealth.app.activities.identificationActivity;


interface ViewPagerCallback {
    void getIssueClicked(HealthIssues survey, int position);

    void getPregnancyIssueClicked(PregnancyRosterData data, int position);
}


package org.intelehealth.unicef.appointmentNew;

import android.os.Bundle;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.activities.base.LocalConfigActivity;

public class VisitSummaryDetailsActivity extends LocalConfigActivity {
    private static final String TAG = "VisitSummaryDetailsActi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_summary_details_ui2);
    }
}
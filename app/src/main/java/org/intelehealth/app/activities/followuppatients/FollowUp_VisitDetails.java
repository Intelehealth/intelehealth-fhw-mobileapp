package org.intelehealth.app.activities.followuppatients;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import org.intelehealth.app.R;

/**
 * Created by Prajwal Waingankar on 16/09/2022.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class FollowUp_VisitDetails extends AppCompatActivity {
    private String patientName, gender, age, openmrsID, chiefComplaint,
    visitID, visit_startDate, visit_speciality, followupDate;
    private boolean isEmergency;
    private TextView patName_txt, gender_age_txt, openmrsID_txt, chiefComplaint_txt, visitID_txt,
    visit_startDate_txt, visit_speciality_txt, followupDate_txt, followup_info;
    private ImageView priorityTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_up_visit_details);

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientName = intent.getStringExtra("patientname");
            gender = intent.getStringExtra("gender");
            age = intent.getStringExtra("age");
            openmrsID = intent.getStringExtra("openmrsID");
            chiefComplaint = intent.getStringExtra("chief_complaint");
            visitID = intent.getStringExtra("visit_ID");
            visit_startDate = intent.getStringExtra("visit_startDate");
            visit_speciality = intent.getStringExtra("visit_speciality");
            followupDate = intent.getStringExtra("followup_date");
            isEmergency = intent.getBooleanExtra("priority_tag", false);
        }

        patName_txt = findViewById(R.id.patname_txt);
        patName_txt.setText(patientName);

        gender_age_txt = findViewById(R.id.gender_age_txt);
        gender_age_txt.setText(gender + " " + age);

        openmrsID_txt = findViewById(R.id.openmrsID_txt);
        openmrsID_txt.setText(openmrsID);

        chiefComplaint_txt = findViewById(R.id.chief_complaint_txt);
        chiefComplaint_txt.setText(chiefComplaint);

        visitID_txt = findViewById(R.id.visitID);
        visitID_txt.setText(visitID);

        visit_startDate_txt = findViewById(R.id.visit_startDate);
        visit_startDate_txt.setText("Visit ID " + visit_startDate);

        visit_speciality_txt = findViewById(R.id.visit_speciality);
        visit_speciality_txt.setText(visit_speciality);

        followupDate_txt = findViewById(R.id.followup_date_txtv);
        followupDate_txt.setText("Follow up on " + followupDate);

        followup_info = findViewById(R.id.followup_info);
        followup_info.setText("Please take " + patientName + "'s follow-up visit.");



    }
}
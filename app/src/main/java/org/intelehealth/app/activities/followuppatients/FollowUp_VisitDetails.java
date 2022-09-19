package org.intelehealth.app.activities.followuppatients;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.intelehealth.app.R;
import org.intelehealth.app.utilities.DateAndTimeUtils;

/**
 * Created by Prajwal Waingankar on 16/09/2022.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class FollowUp_VisitDetails extends AppCompatActivity {
    private String patientName, gender, age, openmrsID, chiefComplaint,
    visitID, visit_startDate, visit_speciality, followupDate, patient_photo_path, chief_complaint_value;
    private boolean isEmergency;
    private TextView patName_txt, gender_age_txt, openmrsID_txt, chiefComplaint_txt, visitID_txt,
    visit_startDate_txt, visit_speciality_txt, followupDate_txt, followup_info, chief_complaint_txt;
    private ImageView priorityTag, profile_image;

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
            patient_photo_path = intent.getStringExtra("patient_photo");
            chief_complaint_value = intent.getStringExtra("chief_complaint");
        }

        // Patient Photo
        profile_image = findViewById(R.id.profile_image);
        if (patient_photo_path != null) {
            Glide.with(this)
                    .load(patient_photo_path)
                    .thumbnail(0.3f)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(profile_image);
        }
        else {
            profile_image.setImageDrawable(getResources().getDrawable(R.drawable.avatar1));
        }
        patName_txt = findViewById(R.id.patname_txt);
        patName_txt.setText(patientName);

        gender_age_txt = findViewById(R.id.gender_age_txt);
        gender_age_txt.setText(gender + " " + age);

        openmrsID_txt = findViewById(R.id.openmrsID_txt);
        openmrsID_txt.setText(openmrsID);

        priorityTag = findViewById(R.id.priority_tag);
        if (isEmergency)
            priorityTag.setVisibility(View.VISIBLE);
        else
            priorityTag.setVisibility(View.GONE);

        chiefComplaint_txt = findViewById(R.id.chief_complaint_txt);
        chiefComplaint_txt.setText(chiefComplaint);

        chief_complaint_txt = findViewById(R.id.chief_complaint_txt);
        chief_complaint_txt.setText(chief_complaint_value);

        visitID_txt = findViewById(R.id.visitID);
        String hideVisitUUID = visitID;
        hideVisitUUID = hideVisitUUID.substring(hideVisitUUID.length() - 4, hideVisitUUID.length());
        visitID_txt.setText("Visit ID XXXX" + hideVisitUUID);

        visit_startDate_txt = findViewById(R.id.visit_startDate);
        Log.v("Followup", "actual date: " + visit_startDate);
        visit_startDate = DateAndTimeUtils.followup_dates_formatter(visit_startDate, "yyyy-MM-dd", "dd MMMM yyyy");
        Log.v("Followup", "foramted date: " + visit_startDate);
        visit_startDate_txt.setText(visit_startDate);

        visit_speciality_txt = findViewById(R.id.visit_speciality);
        visit_speciality_txt.setText(visit_speciality);

        followupDate_txt = findViewById(R.id.followup_date_txtv);
        followupDate = DateAndTimeUtils.followup_dates_formatter(followupDate, "dd-MM-yyyy", "dd MMMM");
        followupDate_txt.setText("Follow up on " + followupDate);

        followup_info = findViewById(R.id.followup_info);
        followup_info.setText("Please take " + patientName + "'s follow-up visit.");



    }
}
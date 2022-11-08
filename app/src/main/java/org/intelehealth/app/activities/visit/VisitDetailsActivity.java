package org.intelehealth.app.activities.visit;

import static org.intelehealth.app.database.dao.EncounterDAO.getChiefComplaint;
import static org.intelehealth.app.database.dao.VisitAttributeListDAO.fetchSpecialityValue;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.intelehealth.app.R;
import org.intelehealth.app.models.dto.VisitAttribute_Speciality;
import org.intelehealth.app.utilities.DateAndTimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Prajwal Waingankar on 16/09/2022.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class VisitDetailsActivity extends AppCompatActivity {
    private String patientName, gender, age, openmrsID,
    visitID, visit_startDate, visit_speciality, followupDate, patient_photo_path, chief_complaint_value;
    private boolean isEmergency, hasPrescription;
    private TextView patName_txt, gender_age_txt, openmrsID_txt, chiefComplaint_txt, visitID_txt,
    visit_startDate_txt, visit_speciality_txt, followupDate_txt, followup_info, chief_complaint_txt;
    private ImageView priorityTag, profile_image;
    public static final String TAG = "FollowUp_visitDetails";
    private RelativeLayout prescription_block;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_details);

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientName = intent.getStringExtra("patientname");
            gender = intent.getStringExtra("gender");
            age = intent.getStringExtra("age");
            Log.d("TAG", "getAge_FollowUp: s : "+age);

            openmrsID = intent.getStringExtra("openmrsID");
            visitID = intent.getStringExtra("visit_ID");
            visit_startDate = intent.getStringExtra("visit_startDate");
            visit_speciality = intent.getStringExtra("visit_speciality");
            followupDate = intent.getStringExtra("followup_date");
            isEmergency = intent.getBooleanExtra("priority_tag", false);
            hasPrescription = intent.getBooleanExtra("hasPrescription", false);
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

        // presc block - start
        prescription_block = findViewById(R.id.prescription_block);
        if (hasPrescription) {
            prescription_block.setVisibility(View.VISIBLE);
        }
        else {
            prescription_block.setVisibility(View.GONE);
        }
        // presc block - end

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

        chief_complaint_txt = findViewById(R.id.chief_complaint_txt);
        if (chief_complaint_value != null) {
            int first = chief_complaint_value.indexOf("<b>");
            int last = chief_complaint_value.indexOf("</b>");
            chief_complaint_value = chief_complaint_value.substring(first, last + 4);
            Log.v(TAG, "chief_Complaint: " + chief_complaint_value);
            Log.v(TAG, "a: " + first + " b: " + last + " C: " + chief_complaint_value);
            chief_complaint_txt.setTextColor(getResources().getColor(R.color.headline_text_color));
            chief_complaint_txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.fu_name_txt_size));
            chief_complaint_txt.setText(Html.fromHtml(chief_complaint_value));
        }
        else {  // ie. here user is coming from Prescription screen and not Follow up screen.
            chief_complaint_value = getChiefComplaint(visitID);
            Log.v(TAG, "chief_Complaint: " + chief_complaint_value);
            int first = chief_complaint_value.indexOf("<b>");
            int last = chief_complaint_value.indexOf("</b>");
            chief_complaint_value = chief_complaint_value.substring(first, last + 4);
            Log.v(TAG, "chief_Complaint: " + chief_complaint_value);
            Log.v(TAG, "a: " + first + " b: " + last + " C: " + chief_complaint_value);
            chief_complaint_txt.setTextColor(getResources().getColor(R.color.headline_text_color));
            chief_complaint_txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.fu_name_txt_size));
            chief_complaint_txt.setText(Html.fromHtml(chief_complaint_value));
        }

        visitID_txt = findViewById(R.id.visitID);
        String hideVisitUUID = visitID;
        hideVisitUUID = hideVisitUUID.substring(hideVisitUUID.length() - 4, hideVisitUUID.length());
        visitID_txt.setText("Visit ID XXXX" + hideVisitUUID);

        visit_startDate_txt = findViewById(R.id.visit_startDate);
        Log.v("Followup", "actual date: " + visit_startDate);
        visit_startDate = DateAndTimeUtils.date_formatter(visit_startDate, "yyyy-MM-dd", "dd MMMM yyyy");
        Log.v("Followup", "foramted date: " + visit_startDate);
        visit_startDate_txt.setText(visit_startDate);

        // speciality - start
        visit_speciality_txt = findViewById(R.id.visit_speciality);
        if (visit_speciality != null)
            visit_speciality_txt.setText(visit_speciality);
        else {
            visit_speciality = fetchSpecialityValue(visitID);
            visit_speciality_txt.setText(visit_speciality);
        }
        // speciality - end

        followupDate_txt = findViewById(R.id.followup_date_txtv);
        if (followupDate != null) {
            followupDate = DateAndTimeUtils.date_formatter(followupDate, "dd-MM-yyyy", "dd MMMM");
            followupDate_txt.setText("Follow up on " + followupDate);
        }


        followup_info = findViewById(R.id.followup_info);
        followup_info.setText("Please take " + patientName + "'s follow-up visit.");



    }
}
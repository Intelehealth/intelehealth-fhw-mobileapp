package org.intelehealth.app.activities.visit;

import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterModifiedDateForPrescGiven;
import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterAdultInitials;
import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterVitals;
import static org.intelehealth.app.database.dao.EncounterDAO.getChiefComplaint;
import static org.intelehealth.app.database.dao.ObsDAO.getFollowupDataForVisitUUID;
import static org.intelehealth.app.database.dao.VisitAttributeListDAO.fetchSpecialityValue;
import static org.intelehealth.app.database.dao.VisitsDAO.fetchVisitModifiedDateForPrescPending;
import static org.intelehealth.app.database.dao.VisitsDAO.isVisitNotEnded;
import static org.intelehealth.app.utilities.DateAndTimeUtils.timeAgoFormat;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.PrescriptionModel;
import org.intelehealth.app.models.dto.VisitAttribute_Speciality;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.VisitUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Prajwal Waingankar on 16/09/2022.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class VisitDetailsActivity extends AppCompatActivity {
    private String patientName, patientUuid, gender, age, openmrsID,
    visitID, visit_startDate, visit_speciality, followupDate, patient_photo_path, chief_complaint_value;
    private boolean isEmergency, hasPrescription;
    private TextView patName_txt, gender_age_txt, openmrsID_txt, chiefComplaint_txt, visitID_txt, presc_time,
    visit_startDate_txt, visit_startTime, visit_speciality_txt, followupDate_txt, followup_info, chief_complaint_txt;
    private ImageView priorityTag, profile_image;
    public static final String TAG = "FollowUp_visitDetails";
    private RelativeLayout prescription_block, endvisit_relative_block, presc_remind_block,
            followup_relative_block, followup_start_card;
    private ImageButton presc_arrowRight, vs_arrowRight;
    private String vitalsUUID, adultInitialUUID;
    private Button btn_end_visit;

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
            patientUuid = intent.getStringExtra("patientUuid");
            gender = intent.getStringExtra("gender");
            age = intent.getStringExtra("age");
            Log.d("TAG", "getAge_FollowUp: s : "+age);

            openmrsID = intent.getStringExtra("openmrsID");
            visitID = intent.getStringExtra("visit_ID");
            visit_startDate = intent.getStringExtra("visit_startDate");
            visit_speciality = intent.getStringExtra("visit_speciality");

            followupDate = intent.getStringExtra("followup_date");
            if (followupDate == null)
                followupDate = getFollowupDataForVisitUUID(visitID);
            isEmergency = intent.getBooleanExtra("priority_tag", false);
            hasPrescription = intent.getBooleanExtra("hasPrescription", false);
            patient_photo_path = intent.getStringExtra("patient_photo");
            chief_complaint_value = intent.getStringExtra("chief_complaint");
        }

        // end visit - start
        endvisit_relative_block = findViewById(R.id.endvisit_relative_block);
        btn_end_visit = findViewById(R.id.btn_end_visit);
        // end visit - end

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

        // visit summary - start
        vs_arrowRight = findViewById(R.id.vs_arrowRight);
        vitalsUUID = fetchEncounterUuidForEncounterVitals(visitID);
        adultInitialUUID = fetchEncounterUuidForEncounterAdultInitials(visitID);

        vs_arrowRight.setOnClickListener(v -> {
            Intent in = new Intent(this, VisitSummaryActivity_New.class);
            in.putExtra("patientUuid", patientUuid);
            in.putExtra("visitUuid", visitID);
            in.putExtra("gender", gender);
            in.putExtra("name", patientName);
            in.putExtra("encounterUuidVitals", vitalsUUID);
            in.putExtra("encounterUuidAdultIntial", adultInitialUUID);
            in.putExtra("float_ageYear_Month", age);
            in.putExtra("tag", "VisitDetailsActivity");
            startActivity(in);
        });
        // visit summary - end

        // presc block - start
        prescription_block = findViewById(R.id.prescription_block);
        presc_time = findViewById(R.id.presc_time);
        presc_arrowRight = findViewById(R.id.presc_arrowRight);
        presc_remind_block = findViewById(R.id.presc_remind_block);

        if (hasPrescription) {
            presc_arrowRight.setVisibility(View.VISIBLE);
            presc_remind_block.setVisibility(View.GONE);
            String modifiedDate = fetchEncounterModifiedDateForPrescGiven(visitID);
            modifiedDate = timeAgoFormat(modifiedDate);
            presc_time.setText("Received " + modifiedDate);
            presc_arrowRight.setOnClickListener(v -> {
                Intent in = new Intent(this, PrescriptionActivity.class);
                in.putExtra("patientname", patientName);
                in.putExtra("patientUuid", patientUuid);
                in.putExtra("patient_photo", patient_photo_path);
                in.putExtra("visit_ID", visitID);
                in.putExtra("visit_startDate", visit_startDate);
                in.putExtra("gender", gender);
                in.putExtra("encounterUuidVitals", vitalsUUID);
                in.putExtra("encounterUuidAdultIntial", adultInitialUUID);
                in.putExtra("age", age);
                in.putExtra("tag", "VisitDetailsActivity");
                in.putExtra("openmrsID", openmrsID);
                startActivity(in);
            });
        }
        else {
            // if no presc given than show the dialog of remind and pending based on time passed from visit uplaoded.
            presc_arrowRight.setVisibility(View.GONE);
            String modifiedDate = fetchVisitModifiedDateForPrescPending(visitID);
            modifiedDate = timeAgoFormat(modifiedDate);
            if (modifiedDate.contains("minutes") || modifiedDate.contains("hours")) {
                // here dont show remind block
                presc_remind_block.setVisibility(View.GONE);
            }
            else {
                // here show remind block as its pending from more than 1 day.
                presc_remind_block.setVisibility(View.VISIBLE); // show remind btn for presc to be given as its more than days.
            }
            presc_time.setText("Pending since " + modifiedDate.replace("ago", ""));
            presc_time.setTextColor(getResources().getColor(R.color.red));
        }
        // presc block - end

        patName_txt = findViewById(R.id.patname_txt);
        patName_txt.setText(patientName);

        gender_age_txt = findViewById(R.id.gender_age_txt);
        gender_age_txt.setText(gender + " " + age);

        openmrsID_txt = findViewById(R.id.openmrsID_txt);
        openmrsID_txt.setText(openmrsID);

        // priority - start
        priorityTag = findViewById(R.id.priority_tag);
        if (isEmergency)
            priorityTag.setVisibility(View.VISIBLE);
        else
            priorityTag.setVisibility(View.GONE);
        // priority - end

        chief_complaint_txt = findViewById(R.id.chief_complaint_txt);
        if (chief_complaint_value != null) {
            int first = chief_complaint_value.indexOf("<b>");
            int last = chief_complaint_value.indexOf("</b>");
            chief_complaint_value = chief_complaint_value.substring(first, last + 4);
            Log.v(TAG, "chief_Complaint: " + chief_complaint_value);
            Log.v(TAG, "a: " + first + " b: " + last + " C: " + chief_complaint_value);
        }
        else {  // ie. here user is coming from Prescription screen and not Follow up screen.
            chief_complaint_value = getChiefComplaint(visitID);
            Log.v(TAG, "chief_Complaint: " + chief_complaint_value);
            int first = chief_complaint_value.indexOf("<b>");
            int last = chief_complaint_value.indexOf("</b>");
            chief_complaint_value = chief_complaint_value.substring(first, last + 4);
            Log.v(TAG, "chief_Complaint: " + chief_complaint_value);
            Log.v(TAG, "a: " + first + " b: " + last + " C: " + chief_complaint_value);
        }
        chief_complaint_txt.setTextColor(getResources().getColor(R.color.headline_text_color));
        chief_complaint_txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.fu_name_txt_size));
        chief_complaint_txt.setText(Html.fromHtml(chief_complaint_value));

        visitID_txt = findViewById(R.id.visitID);
        String hideVisitUUID = visitID;
        hideVisitUUID = hideVisitUUID.substring(hideVisitUUID.length() - 4, hideVisitUUID.length());
        visitID_txt.setText("Visit ID XXXX" + hideVisitUUID);

        // Start Date and Time - start
        visit_startDate_txt = findViewById(R.id.visit_startDate);
        visit_startTime = findViewById(R.id.visit_startTime);

        if (visit_startDate != null) {
            Log.v("Followup", "actual date: " + visit_startDate);

            // Time - start
            String startTime = DateAndTimeUtils.date_formatter(visit_startDate,
                    "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                    "HH:mm a");    // Eg. 26 Sep 2022 at 03:15 PM
            Log.v("SearchPatient", "date: " + startTime);
            visit_startTime.setText(startTime);
            // Time - end

            visit_startDate = DateAndTimeUtils.date_formatter(visit_startDate, "yyyy-MM-dd", "dd MMMM yyyy");
            Log.v("Followup", "foramted date: " + visit_startDate);
            visit_startDate_txt.setText(visit_startDate);
        }

        // Start Date and Time - end

        // speciality - start
        visit_speciality_txt = findViewById(R.id.visit_speciality);
        if (visit_speciality != null)
            visit_speciality_txt.setText(visit_speciality);
        else {
            visit_speciality = fetchSpecialityValue(visitID);
            visit_speciality_txt.setText(visit_speciality);
        }
        // speciality - end

        // follow up - start
        followup_relative_block = findViewById(R.id.followup_relative_block);   // entire block of follow up section.
        followup_start_card = findViewById(R.id.followup_start_card);   // Block that shows to Start Follow up.
        followupDate_txt = findViewById(R.id.followup_date_txtv);
        followup_info = findViewById(R.id.followup_info);

        if (followupDate != null) {
            followup_relative_block.setVisibility(View.VISIBLE);
            followup_start_card.setVisibility(View.VISIBLE);
            followupDate = DateAndTimeUtils.date_formatter(followupDate, "dd-MM-yyyy", "dd MMMM");
            followupDate_txt.setText("Follow up on " + followupDate);
            followup_info.setText("Please take " + patientName + "'s follow-up visit.");
            Log.v("vd", "vd: " + followup_info);
        }
        else {
            followup_relative_block.setVisibility(View.GONE);
            followup_start_card.setVisibility(View.GONE);

        }

        // follow up - end

        // end visit - start
        PrescriptionModel pres = isVisitNotEnded(visitID);
        if (pres.getVisitUuid() != null) {
            endvisit_relative_block.setVisibility(View.VISIBLE);
            btn_end_visit.setOnClickListener(v -> {
                VisitUtils.endVisit(VisitDetailsActivity.this, visitID, patientUuid, followupDate,
                        vitalsUUID, adultInitialUUID, "state", patientName, "VisitDetailsActivity");
            });
        }
        else {
            endvisit_relative_block.setVisibility(View.GONE);
        }
        // end visit - end
    }

}
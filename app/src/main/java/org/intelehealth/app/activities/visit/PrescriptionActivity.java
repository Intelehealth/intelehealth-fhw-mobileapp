package org.intelehealth.app.activities.visit;

import static org.intelehealth.app.database.dao.ObsDAO.fetchDrDetailsFromLocalDb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.models.ClsDoctorDetails;
import org.intelehealth.app.utilities.SessionManager;

/**
 * Created by Prajwal Waingankar on 4/11/2022.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class PrescriptionActivity extends AppCompatActivity {
    private String patientName, patientUuid, gender, age, openmrsID, vitalsUUID, adultInitialUUID, intentTag,
            visitID, visit_startDate, visit_speciality, followupDate, patient_photo_path, chief_complaint_value;
    private ImageButton btn_up_header, btnup_drdetails_header, btnup_diagnosis_header, btnup_medication_header,
            btnup_test_header, btnup_speciality_header, btnup_followup_header;
    private RelativeLayout vs_header_expandview, vs_drdetails_header_expandview,
            vs_diagnosis_header_expandview, vs_medication_header_expandview, vs_testheader_expandview,
            vs_speciality_header_expandview, vs_followup_header_expandview;
    private TextView patName_txt, gender_age_txt, openmrsID_txt, chiefComplaint_txt, visitID_txt, presc_time,
            mCHWname, drname, dr_age_gender, qualification, dr_speciality,
            visit_startDate_txt, visit_startTime, visit_speciality_txt, followupDate_txt, followup_info, chief_complaint_txt;
    private ImageView priorityTag, profile_image;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription2);

        // Status Bar color -> White
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        initUI();
        fetchIntent();
        setDataToView();
        expandableCardVisibilityHandling();
    }

    private void initUI() {
        patName_txt = findViewById(R.id.textView_name_value);
        profile_image = findViewById(R.id.profile_image);
        gender_age_txt = findViewById(R.id.textView_gender_value);
        openmrsID_txt = findViewById(R.id.textView_id_value);
        mCHWname = findViewById(R.id.chw_details);
        visitID_txt = findViewById(R.id.textView_visit_value);

        drname = findViewById(R.id.drname);
        dr_age_gender = findViewById(R.id.dr_age_gender);
        qualification = findViewById(R.id.qualification);
        dr_speciality = findViewById(R.id.dr_speciality);

        btn_up_header = findViewById(R.id.btn_up_header);
        btnup_drdetails_header = findViewById(R.id.btnup_drdetails_header);
        btnup_diagnosis_header = findViewById(R.id.btnup_diagnosis_header);
        btnup_medication_header = findViewById(R.id.btnup_medication_header);
        btnup_test_header = findViewById(R.id.btnup_test_header);
        btnup_speciality_header = findViewById(R.id.btnup_speciality_header);
        btnup_followup_header = findViewById(R.id.btnup_followup_header);

        vs_header_expandview = findViewById(R.id.vs_header_expandview);
        vs_drdetails_header_expandview = findViewById(R.id.vs_drdetails_header_expandview);
        vs_diagnosis_header_expandview = findViewById(R.id.vs_diagnosis_header_expandview);
        vs_medication_header_expandview = findViewById(R.id.vs_medication_header_expandview);
        vs_testheader_expandview = findViewById(R.id.vs_testheader_expandview);
        vs_speciality_header_expandview = findViewById(R.id.vs_speciality_header_expandview);
        vs_followup_header_expandview = findViewById(R.id.vs_followup_header_expandview);
    }

    private void fetchIntent() {
        Intent intent = this.getIntent(); // The intent was passed to the activity
        sessionManager = new SessionManager(this);
        if (intent != null) {
            patientName = intent.getStringExtra("patientname");
            patientUuid = intent.getStringExtra("patientUuid");
            gender = intent.getStringExtra("gender");
            age = intent.getStringExtra("age");
            Log.d("TAG", "getAge_FollowUp: s : "+age);
            openmrsID = intent.getStringExtra("openmrsID");
            visitID = intent.getStringExtra("visit_ID");
            vitalsUUID = intent.getStringExtra("encounterUuidVitals");
            adultInitialUUID = intent.getStringExtra("encounterUuidAdultIntial");
            visit_startDate = intent.getStringExtra("visit_startDate");
            patient_photo_path = intent.getStringExtra("patient_photo");
            intentTag = intent.getStringExtra("tag");
        }
    }


    private void setDataToView() {
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
        // end

        patName_txt.setText(patientName);
        gender_age_txt.setText(gender + " " + age);
        openmrsID_txt.setText(openmrsID);
        mCHWname.setText(sessionManager.getChwname()); //session manager provider

        String hideVisitUUID = visitID;
        hideVisitUUID = hideVisitUUID.substring(hideVisitUUID.length() - 4, hideVisitUUID.length());
        visitID_txt.setText("XXXX" + hideVisitUUID);

        // dr details - start
        String drDetails = fetchDrDetailsFromLocalDb(visitID);
        parseDoctorDetails(drDetails);
        // dr details - end
    }

    private void expandableCardVisibilityHandling() {
        btn_up_header.setOnClickListener(v -> {
            if (vs_header_expandview.getVisibility() == View.VISIBLE)
                vs_header_expandview.setVisibility(View.GONE);
            else
                vs_header_expandview.setVisibility(View.VISIBLE);
        });

        btnup_drdetails_header.setOnClickListener(v -> {
            if (vs_drdetails_header_expandview.getVisibility() == View.VISIBLE)
                vs_drdetails_header_expandview.setVisibility(View.GONE);
            else
                vs_drdetails_header_expandview.setVisibility(View.VISIBLE);
        });

        btnup_diagnosis_header.setOnClickListener(v -> {
            if (vs_diagnosis_header_expandview.getVisibility() == View.VISIBLE)
                vs_diagnosis_header_expandview.setVisibility(View.GONE);
            else
                vs_diagnosis_header_expandview.setVisibility(View.VISIBLE);
        });

        btnup_medication_header.setOnClickListener(v -> {
            if (vs_medication_header_expandview.getVisibility() == View.VISIBLE)
                vs_medication_header_expandview.setVisibility(View.GONE);
            else
                vs_medication_header_expandview.setVisibility(View.VISIBLE);
        });

        btnup_test_header.setOnClickListener(v -> {
            if (vs_testheader_expandview.getVisibility() == View.VISIBLE)
                vs_testheader_expandview.setVisibility(View.GONE);
            else
                vs_testheader_expandview.setVisibility(View.VISIBLE);
        });

        btnup_speciality_header.setOnClickListener(v -> {
            if (vs_speciality_header_expandview.getVisibility() == View.VISIBLE)
                vs_speciality_header_expandview.setVisibility(View.GONE);
            else
                vs_speciality_header_expandview.setVisibility(View.VISIBLE);
        });

        btnup_followup_header.setOnClickListener(v -> {
            if (vs_followup_header_expandview.getVisibility() == View.VISIBLE)
                vs_followup_header_expandview.setVisibility(View.GONE);
            else
                vs_followup_header_expandview.setVisibility(View.VISIBLE);
        });
    }

    // parse dr details - start
    ClsDoctorDetails details;
    private void parseDoctorDetails(String dbValue) {
        Gson gson = new Gson();
        details = gson.fromJson(dbValue, ClsDoctorDetails.class);
        Log.e("TAG", "TEST VISIT: " + details.toString());

        drname.setText(details.getName());
        dr_age_gender.setText(details.getEmailId());
        qualification.setText(details.getQualification());
        dr_speciality.setText(details.getSpecialization());




//        if (objClsDoctorDetails != null) {
//           // doctorSign = objClsDoctorDetails.getTextOfSign();
//           // doctrRegistartionNum = !TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? getString(R.string.dr_registration_no) + objClsDoctorDetails.getRegistrationNumber() : "";
//
//            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;margin-top:3px;\">" +
//                    "<span style=\"font-size:12pt; color:#448AFF;padding: 0px;\">" +
//                    (!TextUtils.isEmpty(objClsDoctorDetails.getName()) ? objClsDoctorDetails.getName() : "") + "</span><br>" +
//                   /* "<span style=\"font-size:12pt; color:#448AFF;padding: 0px;\">" + "  " +
//                    (!TextUtils.isEmpty(objClsDoctorDetails.getQualification()) ? objClsDoctorDetails.getQualification() : "") + ", "
//                    +*/
//                    (!TextUtils.isEmpty(objClsDoctorDetails.getSpecialization()) ?
//                            objClsDoctorDetails.getSpecialization() : "") + "</span><br>" +
//
//                    // "<span style=\"font-size:12pt;color:#448AFF;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getPhoneNumber()) ? "Phone Number: " + objClsDoctorDetails.getPhoneNumber() : "") + "</span><br>" +
//
//                    "<span style=\"font-size:12pt;color:#448AFF;padding: 0px;\">" +
//                    (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ? "Email: " + objClsDoctorDetails.getEmailId() : "") +
//                    "</span><br>" + (!TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? "Registration No: " +
//                    objClsDoctorDetails.getRegistrationNumber() : "") +
//                    "</div>";
//
//            mDoctorName.setText(Html.fromHtml(doctorDetailStr).toString().trim());
//        }
    }

    // parse dr details - end

}
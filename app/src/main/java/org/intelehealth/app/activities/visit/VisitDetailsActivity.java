package org.intelehealth.app.activities.visit;

import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterModifiedDateForPrescGiven;
import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterAdultInitials;
import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterVitals;
import static org.intelehealth.app.database.dao.EncounterDAO.getChiefComplaint;
import static org.intelehealth.app.database.dao.ObsDAO.fetchDrDetailsFromLocalDb;
import static org.intelehealth.app.database.dao.ObsDAO.getFollowupDataForVisitUUID;
import static org.intelehealth.app.database.dao.PatientsDAO.phoneNumber;
import static org.intelehealth.app.database.dao.VisitAttributeListDAO.fetchSpecialityValue;
import static org.intelehealth.app.database.dao.VisitsDAO.fetchVisitModifiedDateForPrescPending;
import static org.intelehealth.app.database.dao.VisitsDAO.isVisitNotEnded;
import static org.intelehealth.app.utilities.DateAndTimeUtils.timeAgoFormat;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.ClsDoctorDetails;
import org.intelehealth.app.models.PrescriptionModel;
import org.intelehealth.app.models.dto.VisitAttribute_Speciality;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.VisitUtils;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Prajwal Waingankar on 16/09/2022.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class VisitDetailsActivity extends AppCompatActivity implements NetworkUtils.InternetCheckUpdateInterface {
    private String patientName, patientUuid, gender, age, openmrsID,
    visitID, visit_startDate, visit_speciality, followupDate, patient_photo_path, chief_complaint_value;
    private boolean isEmergency, hasPrescription;
    private TextView patName_txt, gender_age_txt, openmrsID_txt, chiefComplaint_txt, visitID_txt, presc_time,
    visit_startDate_txt, visit_startTime, visit_speciality_txt, followupDate_txt, followup_info, chief_complaint_txt;
    private ImageView priorityTag, profile_image, icon_presc_details;
    public static final String TAG = "FollowUp_visitDetails";
    private RelativeLayout prescription_block, endvisit_relative_block, presc_remind_block,
            followup_relative_block, followup_start_card, yes_no_followup_relative,
            vs_card, presc_relative;
    private ImageButton presc_arrowRight, vs_arrowRight, backArrow, refresh,
            pat_call_btn, pat_whatsapp_btn, dr_call_btn, dr_whatsapp_btn;
    private String vitalsUUID, adultInitialUUID, obsservermodifieddate, pat_phoneno, dr_MobileNo, dr_WhatsappNo, drDetails;
    private Button btn_end_visit, yes_followup_btn;
    private ClsDoctorDetails clsDoctorDetails;
    private NetworkUtils networkUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_details);

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        networkUtils = new NetworkUtils(this, this);

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
            obsservermodifieddate = intent.getStringExtra("obsservermodifieddate");
        }

        // end visit - start
        endvisit_relative_block = findViewById(R.id.endvisit_relative_block);
        btn_end_visit = findViewById(R.id.btn_end_visit);
        backArrow = findViewById(R.id.backArrow);
        refresh = findViewById(R.id.refresh);
        // end visit - end

        pat_call_btn = findViewById(R.id.pat_call_btn);
        pat_whatsapp_btn = findViewById(R.id.pat_whatsapp_btn);
        dr_call_btn = findViewById(R.id.dr_call_btn);
        dr_whatsapp_btn = findViewById(R.id.dr_whatsapp_btn);

        try {
            pat_phoneno = StringUtils.mobileNumberEmpty(phoneNumber(patientUuid));
        } catch (DAOException e) {
            e.printStackTrace();
        }
        Log.v("VD", "vd_pat_phone: " + pat_phoneno);

        // Fetching dr details from Local db.
        drDetails = fetchDrDetailsFromLocalDb(visitID);
        Gson gson = new Gson();
        clsDoctorDetails = gson.fromJson(drDetails, ClsDoctorDetails.class);
        Log.e("TAG", "TEST VISIT: " + clsDoctorDetails.toString());

        dr_MobileNo = clsDoctorDetails.getPhoneNumber();
        dr_WhatsappNo = clsDoctorDetails.getWhatsapp();
        // end

        // calling and whatsapp - start
        pat_call_btn.setOnClickListener(v -> {
            calling_feature(pat_phoneno);
        });

        pat_whatsapp_btn.setOnClickListener(v -> {
            whatsapp_feature(pat_phoneno);
        });

        dr_call_btn.setOnClickListener(v -> {
            calling_feature(dr_MobileNo);
        });

        dr_whatsapp_btn.setOnClickListener(v -> {
            whatsapp_feature(dr_WhatsappNo);
        });
        // calling and whatsapp - end

        backArrow.setOnClickListener(v -> {
            finish();
        });

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
        vs_card = findViewById(R.id.vs_card);
        presc_relative = findViewById(R.id.presc_relative);
        vitalsUUID = fetchEncounterUuidForEncounterVitals(visitID);
        adultInitialUUID = fetchEncounterUuidForEncounterAdultInitials(visitID);

/*
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
*/
        vs_card.setOnClickListener(v -> {
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
        icon_presc_details = findViewById(R.id.icon_presc_details);

        if (hasPrescription) {
            presc_arrowRight.setVisibility(View.VISIBLE);
            presc_relative.setClickable(true);
            presc_remind_block.setVisibility(View.GONE);
            if (!obsservermodifieddate.equalsIgnoreCase("")) {
              //  String modifiedDate = fetchEncounterModifiedDateForPrescGiven(visitID);
                String modifiedDate = obsservermodifieddate;
                modifiedDate = timeAgoFormat(modifiedDate);
                presc_time.setText("Received " + modifiedDate);
                icon_presc_details.setImageDrawable(getResources().getDrawable(R.drawable.prescription_icon));
            }

/*
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
                in.putExtra("followupDate", followupDate);
                in.putExtra("openmrsID", openmrsID);
                startActivity(in);
            });
*/
            presc_relative.setOnClickListener(v -> {
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
                in.putExtra("followupDate", followupDate);
                in.putExtra("openmrsID", openmrsID);
                startActivity(in);
            });
        }
        else {
            // if no presc given than show the dialog of remind and pending based on time passed from visit uplaoded.
            presc_arrowRight.setVisibility(View.GONE);
            presc_relative.setClickable(false);
         //   String modifiedDate = fetchVisitModifiedDateForPrescPending(visitID);

            String modifiedDate = "";
            if (!obsservermodifieddate.equalsIgnoreCase("")) {
                modifiedDate = obsservermodifieddate;
                modifiedDate = timeAgoFormat(modifiedDate);
            }
            else {
                modifiedDate = fetchVisitModifiedDateForPrescPending(visitID);
                modifiedDate = timeAgoFormat(modifiedDate);
            }

            if (modifiedDate.contains("minutes") || modifiedDate.contains("hours") || modifiedDate.contains("minute") || modifiedDate.contains("hour")) {
                // here dont show remind block
                presc_remind_block.setVisibility(View.GONE);
            } else {
                // here show remind block as its pending from more than 1 day.
                presc_remind_block.setVisibility(View.VISIBLE); // show remind btn for presc to be given as its more than days.
            }
            presc_time.setText("Pending since " + modifiedDate.replace("ago", ""));
            presc_time.setTextColor(getResources().getColor(R.color.red));
            icon_presc_details.setImageDrawable(getResources().getDrawable(R.drawable.prescription_red_icon));
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
        yes_no_followup_relative = findViewById(R.id.yes_no_followup_relative);   // yes no button for follow up.
        followupDate_txt = findViewById(R.id.followup_date_txtv);
        followup_info = findViewById(R.id.followup_info);
        yes_followup_btn = findViewById(R.id.yes_followup_btn);

        if (followupDate != null) {
            followup_relative_block.setVisibility(View.VISIBLE);
            yes_no_followup_relative.setVisibility(View.VISIBLE);
            followupDate = DateAndTimeUtils.date_formatter(followupDate, "dd-MM-yyyy", "dd MMMM");
            followupDate_txt.setText("Follow up on " + followupDate);
            followup_info.setText("Please take " + patientName + "'s follow-up visit.");
            Log.v("vd", "vd: " + followup_info);
        }
        else {
            followup_relative_block.setVisibility(View.GONE);
            yes_no_followup_relative.setVisibility(View.GONE);

        }

        yes_followup_btn.setOnClickListener(v -> {
            yes_no_followup_relative.setVisibility(View.GONE);
            followup_start_card.setVisibility(View.VISIBLE);
        });
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

    /**
     * This will open Whatsapp with a pre-defined message to be sent to the user.
     * @param phoneno
     */
    // TODO: check with Sagar for this message to be passed...
    private void whatsapp_feature(String phoneno) {
        if (phoneno != null) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(
                            String.format("https://api.whatsapp.com/send?phone=%s&text=%s",
                                    phoneno, "Hi"))));
        }
        else {
            Toast.makeText(VisitDetailsActivity.this, getResources().getString(R.string.mobile_no_not_provided), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This will open up Dialer with the phone number passed for user to initiate call.
     * @param phoneno
     */
    private void calling_feature(String phoneno) {
        if (phoneno != null) {
            Intent i1 = new Intent(Intent.ACTION_DIAL);
            i1.setData(Uri.parse("tel:" + phoneno));
            startActivity(i1);
        }
        else {
            Toast.makeText(VisitDetailsActivity.this, getResources().getString(R.string.mobile_no_not_provided), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        Log.d("TAG", "updateUIForInternetAvailability: ");
        if (isInternetAvailable) {
            refresh.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));
        }
        else {
            refresh.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //register receiver for internet check
        networkUtils.callBroadcastReceiver();
    }
    @Override
    public void onStop() {
        super.onStop();
        try {
            //unregister receiver for internet check
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
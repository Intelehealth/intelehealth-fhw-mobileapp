package org.intelehealth.app.utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.patientSurveyActivity.PatientSurveyActivity;
import org.intelehealth.app.activities.patientSurveyActivity.PatientSurveyActivity_New;
import org.intelehealth.app.app.IntelehealthApplication;

public class VisitUtils {
    public static void endVisit(Context activityContext, String visitUUID, String patientUuid,
                                String followUpDate, String encounterVitals, String encounterUuidAdultIntial,
                                String state, String patientName, String intentTag) {
        //end visit
        if (visitUUID != null && !visitUUID.isEmpty()) {

            if (followUpDate != null && !followUpDate.equalsIgnoreCase("")) {

                MaterialAlertDialogBuilder followUpAlert = new MaterialAlertDialogBuilder(activityContext);
                followUpAlert.setMessage(activityContext.getString(R.string.visit_summary_follow_up_reminder) + followUpDate);
                followUpAlert.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       // Intent intent = new Intent(activityContext, PatientSurveyActivity.class);
                        Intent intent = new Intent(activityContext, PatientSurveyActivity_New.class);
                        intent.putExtra("patientUuid", patientUuid);
                        intent.putExtra("visitUuid", visitUUID);
                        intent.putExtra("encounterUuidVitals", encounterVitals);
                        intent.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
                        intent.putExtra("state", state);
                        intent.putExtra("name", patientName);
                        intent.putExtra("tag", intentTag);
                        activityContext.startActivity(intent);
                    }
                });
                followUpAlert.show();
            } else {
                Intent intent = new Intent(activityContext, PatientSurveyActivity_New.class);
                intent.putExtra("patientUuid", patientUuid);
                intent.putExtra("visitUuid", visitUUID);
                intent.putExtra("encounterUuidVitals", encounterVitals);
                intent.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
                intent.putExtra("state", state);
                intent.putExtra("name", patientName);
                intent.putExtra("tag", intentTag);
                activityContext.startActivity(intent);
            }
        } else {
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(activityContext);
            alertDialogBuilder.setMessage(activityContext.getString(R.string.visit_summary_upload_reminder));
            alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            IntelehealthApplication.setAlertDialogCustomTheme(activityContext, alertDialog);
        }
    }
}

package org.intelehealth.app.activities.visitSummaryActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.epartogramActivity.Epartogram;
import org.intelehealth.app.activities.homeActivity.HomeActivity;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.utilities.NotificationReceiver;
import org.intelehealth.app.utilities.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class TimelineVisitSummaryActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    TimelineAdapter adapter;
    Context context;
    private String patientName;
    Intent intent;
    ArrayList<String> timeList;
    String startVisitTime, patientUuid, visitUuid, whichScreenUserCameFromTag, providerID, Stage1_Hour1_1;
    SessionManager sessionManager;
    EncounterDAO encounterDAO;
    ArrayList<EncounterDTO> encounterListDTO;
    Button endStageButton;
    int stageNo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline_visit_summary);
        initUI();
//        adapter = new TimelineAdapter(context, intent, encounterDTO, sessionManager);
//        recyclerView.setAdapter(adapter);
        //  triggerAlarm5MinsBefore(); // Notification to show 5min before for every 30min interval.

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_viewepartogram, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.epartogramView:
                showEpartogram();
                break;
        }

        return true;
    }

    private void showEpartogram() {
        // Call webview here...
        Intent intent = new Intent(context, Epartogram.class);
        intent.putExtra("patientuuid", patientUuid);
        intent.putExtra("visituuid", visitUuid);
        startActivity(intent);
    }

    private void initUI() {
        timeList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerview_timeline);
        endStageButton = findViewById(R.id.endStageButton);
        LinearLayoutManager linearLayout = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayout);
        context = TimelineVisitSummaryActivity.this;
        intent = this.getIntent(); // The intent was passed to the activity

        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        //In case of crash still the org should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        if (intent != null) {
            startVisitTime = intent.getStringExtra("startdate");
            timeList.add(startVisitTime);
            patientName = intent.getStringExtra("patientNameTimeline");
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            providerID = intent.getStringExtra("providerID");
            whichScreenUserCameFromTag = intent.getStringExtra("tag");
            Stage1_Hour1_1 = intent.getStringExtra("Stage1_Hour1_1");

            Log.v("timeline", "patientname_1 " + patientName + " " + patientUuid + " " + visitUuid);

            if (whichScreenUserCameFromTag != null &&
                    whichScreenUserCameFromTag.equalsIgnoreCase("new")) {
                triggerAlarm_Stage1_every30mins(); // Notification to show every 30min.
                Log.v("timeline", "whichscreen: " + whichScreenUserCameFromTag);
            } else {
                // do nothing
            }

            fetchAllEncountersFromVisitForTimelineScreen(visitUuid); // fetch all records...
        }

        setTitle(patientName);

        EncounterDTO encounterDTO = encounterDAO.getEncounterByVisitUUIDLimit1(visitUuid); // get latest encounter.
        // String latestEncounterTypeId = encounterDTO.getEncounterTypeUuid();
        String latestEncounterName = new EncounterDAO().getEncounterTypeNameByUUID(encounterDTO.getEncounterTypeUuid());
        if (latestEncounterName.toLowerCase().contains("stage2")) {
            stageNo = 2;
            endStageButton.setText(context.getResources().getText(R.string.end2StageButton));
        } else if (latestEncounterName.toLowerCase().contains("stage1")) {
            stageNo = 1;
            endStageButton.setText(context.getResources().getText(R.string.endStageButton));
        } else {
            stageNo = 0;
            // do nothing
        }

        // clicking on this open dialog to confirm and start stage 2 | If stage 2 already open then ends visit.
        endStageButton.setOnClickListener(v -> {
            if (stageNo == 1) {
                cancelStage1_ConfirmationDialog(); // cancel and start stage 2
            } else if (stageNo == 2) {
                // end visit on stage 2 ie. visit complete encoutner call....
            }

        });
    }

    // Timeline stage 1 end confirmation dialog
    private void cancelStage1_ConfirmationDialog() {
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(context);
        alertDialog.setTitle("Are you sure you want to End Stage 1?");
        // alertDialog.setMessage("");
        alertDialog.setPositiveButton(context.getResources().getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        endStageButton.setText(context.getResources().getText(R.string.end2StageButton));
                        cancelStage1_30minAlarm();
                        dialog.dismiss();
                    }
                });

        alertDialog.setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = alertDialog.show();
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));

        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    private void cancelStage1_30minAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        Log.v("timeline", "visituuid_int " + visitUuid.replaceAll("[^\\d]", ""));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                Integer.parseInt(visitUuid.replaceAll("[^\\d]", "").substring(0, 6)), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // to set different alarms for different patients.
        alarmManager.cancel(pendingIntent);

        // now start 15mins alarm for Stage 2 -> since 30mins is cancelled for Stage 1.
        triggerAlarm_Stage2_every15mins();
    }

    // create a new encounter for the first interval so that a new card is populated for Stage1Hr1_1...
/*
    private void createNewEncounter(String visitUuid) {
        EncounterDAO encounterDAO = new EncounterDAO();
        EncounterDTO encounterDTO = new EncounterDTO();

        encounterDTO.setUuid(UUID.randomUUID().toString());
        encounterDTO.setVisituuid(visitUuid);
        encounterDTO.setEncounterTime(AppConstants.dateAndTimeUtils.currentDateTime());
        encounterDTO.setProvideruuid(sessionManager.getProviderID());
        encounterDTO.setEncounterTypeUuid(encounterDAO.getEncounterTypeUuid("Stage1_Hour1_1"));
        encounterDTO.setSyncd(false); // false as this is the one that is started and would be pushed in the payload...
        encounterDTO.setVoided(0);
        encounterDTO.setPrivacynotice_value("true");

        try {
            encounterDAO.createEncountersToDB(encounterDTO);
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }
*/

    // fetch all encounters from encounter tbl local db for this particular visit and show on timeline...
    private void fetchAllEncountersFromVisitForTimelineScreen(String visitUuid) {
        encounterDAO = new EncounterDAO();
        encounterListDTO = encounterDAO.getEncountersByVisitUUID(visitUuid);

        adapter = new TimelineAdapter(context, intent, encounterListDTO, sessionManager);
        recyclerView.setAdapter(adapter);
    }

/*
    public void triggerAlarm5MinsBefore() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 25); // So that just before 5mins of 30mins we get the notification
        Log.v("timeline", "25min: "+ calendar.getTime().toString());

        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("patientNameTimeline", patientName);
        intent.putExtra("timeTag", 5);
        intent.putExtra("patientUuid", patientUuid);
        intent.putExtra("visitUuid", visitUuid);
        intent.putExtra("providerID", providerID);

        Log.v("timeline", "patientname_5 "+ patientName);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                5, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() ,
                    AlarmManager.INTERVAL_HALF_HOUR, pendingIntent);
            // trigger in millisec: here we state at what time do we want to trigger this notifi so when user comes to this screen from there
            // TODO: after 25mins this trigger should start up....
            //  triggerTime: 25mins from the time user came to this screen & repeatTime: 30mins everytime...
        }
    }
*/

    private void triggerAlarm_Stage2_every15mins() { // TODO: change 1min to 15mins.....
        Calendar calendar = Calendar.getInstance(); // current time and from there evey 15mins notifi will be triggered...
        calendar.add(Calendar.MINUTE, 15); // So that after 15mins this notifi is triggered and scheduled...
        //  calendar.add(Calendar.MINUTE, 1); // Testing

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("patientNameTimeline", patientName);
        intent.putExtra("timeTag", 15);
        intent.putExtra("patientUuid", patientUuid);
        intent.putExtra("visitUuid", visitUuid);
        intent.putExtra("providerID", providerID);
        intent.putExtra("Stage1_Hour1_1", "Stage1_Hour1_1");

        Log.v("timeline", "patientname_3 " + patientName + " " + patientUuid + " " + visitUuid);
        Log.v("timeline", "visituuid_int_15min " + visitUuid.replaceAll("[^\\d]", ""));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                Integer.parseInt(visitUuid.replaceAll("[^\\d]", "").substring(0, 6)), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // to set different alarams for different patients.

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    /*60000*/AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
        }
    }

    private void triggerAlarm_Stage1_every30mins() { // TODO: change 1min to 15mins.....
        Calendar calendar = Calendar.getInstance(); // current time and from there evey 15mins notifi will be triggered...
        calendar.add(Calendar.MINUTE, 30); // So that after 15mins this notifi is triggered and scheduled...
        //  calendar.add(Calendar.MINUTE, 2); // Testing

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("patientNameTimeline", patientName);
        intent.putExtra("timeTag", 30);
        intent.putExtra("patientUuid", patientUuid);
        intent.putExtra("visitUuid", visitUuid);
        intent.putExtra("providerID", providerID);
        intent.putExtra("Stage1_Hour1_1", "Stage1_Hour1_1");

        Log.v("timeline", "patientname_3 " + patientName + " " + patientUuid + " " + visitUuid);
        Log.v("timeline", "visituuid_int_30min " + visitUuid.replaceAll("[^\\d]", ""));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                Integer.parseInt(visitUuid.replaceAll("[^\\d]", "").substring(0, 6)), intent, PendingIntent.FLAG_UPDATE_CURRENT); // to set different alarams for different patients.

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    /*120000*/AlarmManager.INTERVAL_HALF_HOUR, pendingIntent);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() { // when finish() called in Epartogram screen than onStart() is called here.
        super.onStart();
        adapter.notifyDataSetChanged();
    }


}
package org.intelehealth.ekalhelpline.activities.completedvisits;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.ekalhelpline.R;
import org.intelehealth.ekalhelpline.activities.ClosedVisitsActivity.ClosedAdapter;
import org.intelehealth.ekalhelpline.activities.ClosedVisitsActivity.ClosedVisitsActivity;
import org.intelehealth.ekalhelpline.activities.followuppatients.FollowUpPatientActivity;
import org.intelehealth.ekalhelpline.app.AppConstants;
import org.intelehealth.ekalhelpline.database.dao.ProviderDAO;
import org.intelehealth.ekalhelpline.models.ActivePatientModel;
import org.intelehealth.ekalhelpline.utilities.Logger;
import org.intelehealth.ekalhelpline.utilities.SessionManager;
import org.intelehealth.ekalhelpline.utilities.StringUtils;
import org.intelehealth.ekalhelpline.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CompletedVisitsActivity extends AppCompatActivity {

    private static final String TAG = FollowUpPatientActivity.class.getSimpleName();
    private ClosedAdapter closedAdapter;
    RecyclerView recyclerView;
    SessionManager sessionManager = null;
    TextView msg;
    private SQLiteDatabase db;
    int limit = Integer.MAX_VALUE, offset = 0;
    MaterialAlertDialogBuilder dialogBuilder;
    ProviderDAO providerDAO = new ProviderDAO();
    TextView no_records_found_textview;
    Context context;
    String chw_name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_patient);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        context = CompletedVisitsActivity.this;
        chw_name = sessionManager.getProviderID();
        no_records_found_textview = findViewById(R.id.no_records_found_textview);


        //In case of crash still the app should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }

        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        msg = findViewById(R.id.textviewmessage);
        recyclerView = findViewById(R.id.today_patient_recycler_view);
        LinearLayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(reLayoutManager);
        if (sessionManager.isPullSyncFinished()) {
            msg.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            firstQuery();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        // inflater.inflate(R.menu.menu_today_patient, menu);
        inflater.inflate(R.menu.today_filter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_filter:
                displaySingleSelectionDialog();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void firstQuery() {
        try {
            List<ActivePatientModel> allPatients = getAllPatientsFromDB(chw_name,offset);
            if(allPatients.size()>0) {
                closedAdapter = new ClosedAdapter(allPatients, CompletedVisitsActivity.this);
                recyclerView.setAdapter(closedAdapter);
                no_records_found_textview.setVisibility(View.GONE);
            }
            else {
                closedAdapter = new ClosedAdapter(allPatients, CompletedVisitsActivity.this);
                recyclerView.setAdapter(closedAdapter);
                no_records_found_textview.setVisibility(View.VISIBLE);
                no_records_found_textview.setHint(R.string.no_records_found);
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Logger.logE("firstquery", "exception", e);
        }
    }

    private List<ActivePatientModel> getAllPatientsFromDB(String userUuid, int offset) {
        List<ActivePatientModel> todayPatientList = new ArrayList<>();

        /*String query = "SELECT distinct a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.uuid, b.first_name, b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, o.value AS obsvalue FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o WHERE b.uuid = a.patientuuid " +
                "AND a.enddate is NOT NULL AND a.uuid = d.visituuid AND d.uuid = o.encounteruuid  AND d.provider_uuid = ? AND o.conceptuuid = '36d207d6-bee7-4b3e-9196-7d053c6eddce'  " +
                "AND (obsvalue like '%TLD Closed%' or obsvalue like '%Doctor Resolution Closed%') ORDER BY a.startdate DESC limit ? offset ?";*/

        String query = "SELECT distinct a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.uuid, b.first_name, b.middle_name, b.last_name, b.date_of_birth, " +
                "b.openmrs_id, o.value, c.value AS speciality, c.visit_uuid FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE b.uuid = a.patientuuid " +
                "AND  a.enddate is NOT NULL  AND a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND a.uuid = c.visit_uuid  AND d.provider_uuid = ? " +
                "AND o.conceptuuid = '36d207d6-bee7-4b3e-9196-7d053c6eddce' AND (o.value like '%TLD Resolved%' or o.value " +
                "like '%Doctor Resolution Resolved%' or speciality like '%Agent Resolution%'  or speciality like '%Curiosity Resolution%') GROUP BY c.visit_uuid ORDER BY a.startdate DESC limit ? offset ?";

        final Cursor cursor = db.rawQuery(query, new String[]{userUuid,String.valueOf(limit), String.valueOf(offset)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    try {
                        todayPatientList.add(new ActivePatientModel(
                                cursor.getString(cursor.getColumnIndexOrThrow("uuid")),
                                cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                                cursor.getString(cursor.getColumnIndexOrThrow("startdate")),
                                cursor.getString(cursor.getColumnIndexOrThrow("enddate")),
                                cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")),
                                cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                                cursor.getString(cursor.getColumnIndexOrThrow("middle_name")),
                                cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                                cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")),
                                StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")))),
                                cursor.getString(cursor.getColumnIndexOrThrow("sync")),
                                cursor.getString(cursor.getColumnIndexOrThrow("speciality")),
                                cursor.getString(cursor.getColumnIndexOrThrow("value")))
                        );
                    } catch (DAOException e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
        }
        if (cursor != null) {
            cursor.close();
        }

        return todayPatientList;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recyclerView.clearOnScrollListeners();
    }

    public static long getActiveVisitsCount(SQLiteDatabase db, String chwUser) {
        int count = 0;
        List<ActivePatientModel> allPatientsFromDB = doQuery_(chwUser);
        count = allPatientsFromDB.size();
        Log.v("main", "completed count:: " + allPatientsFromDB.size());
        return count;

    }

    private static String phoneNumber(String patientUuid) throws DAOException {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        String phone = null;
        Cursor idCursor = db.rawQuery("SELECT value  FROM tbl_patient_attribute where patientuuid = ? AND person_attribute_type_uuid='14d4f066-15f5-102d-96e4-000c29c2a5d7' ", new String[]{patientUuid});
        try {
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    phone = idCursor.getString(idCursor.getColumnIndexOrThrow("value"));
                }
            }
        } catch (SQLException s) {
            FirebaseCrashlytics.getInstance().recordException(s);
        }
        idCursor.close();
        return phone;
    }

    private static List<ActivePatientModel> doQuery_(String user_uuid) {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        List<ActivePatientModel> activePatientList = new ArrayList<>();
        String query = "SELECT distinct a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.uuid, b.first_name, b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, o.value, c.value AS speciality, c.visit_uuid " +
                "FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE b.uuid = a.patientuuid AND  a.enddate is NOT NULL  " +
                "AND a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND a.uuid = c.visit_uuid  AND d.provider_uuid = ? " +
                "AND o.conceptuuid = '36d207d6-bee7-4b3e-9196-7d053c6eddce'  AND (o.value like '%TLD Resolved%' or o.value like '%Doctor Resolution Resolved%' or " +
                "speciality like '%Agent Resolution%'  or speciality like '%Curiosity Resolution%') GROUP BY c.visit_uuid ORDER BY a.startdate DESC";
        final Cursor cursor = db.rawQuery(query, new String[]{user_uuid});
        Log.v("main", "doquery: "+ query);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    try {
                        activePatientList.add(new ActivePatientModel(
                                cursor.getString(cursor.getColumnIndexOrThrow("uuid")),
                                cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                                cursor.getString(cursor.getColumnIndexOrThrow("startdate")),
                                cursor.getString(cursor.getColumnIndexOrThrow("enddate")),
                                cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")),
                                cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                                cursor.getString(cursor.getColumnIndexOrThrow("middle_name")),
                                cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                                cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")),
                                StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")))),
                                cursor.getString(cursor.getColumnIndexOrThrow("sync")),
                                cursor.getString(cursor.getColumnIndexOrThrow("speciality")),
                                cursor.getString(cursor.getColumnIndexOrThrow("value")))
                        );
                    } catch (DAOException e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return activePatientList;
    }

    private void displaySingleSelectionDialog() {
        ArrayList selectedItems = new ArrayList<>();
        String[] creator_names = null;
        String[] creator_uuid = null;
        try {
            creator_names = providerDAO.getProvidersList().toArray(new String[0]);
            creator_uuid = providerDAO.getProvidersUuidList().toArray(new String[0]);

        } catch (DAOException e) {
            e.printStackTrace();
        }
        dialogBuilder = new MaterialAlertDialogBuilder(CompletedVisitsActivity.this);
        dialogBuilder.setTitle(getString(R.string.filter_by_creator));

        String[] finalCreator_names = creator_names;
        String[] finalCreator_uuid = creator_uuid;

        dialogBuilder.setMultiChoiceItems(creator_names, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                Logger.logD(TAG, "multichoice" + which + isChecked);
                if (isChecked) {
                    // If the user checked the item, add it to the selected items
                    selectedItems.add(finalCreator_uuid[which]);
                    Logger.logD(TAG, finalCreator_names[which] + finalCreator_uuid[which]);
                } else if (selectedItems.contains(finalCreator_uuid[which])) {
                    // Else, if the item is already in the array, remove it
                    selectedItems.remove(finalCreator_uuid[which]);
                    Logger.logD(TAG, finalCreator_names[which] + finalCreator_uuid[which]);
                }
            }
        });

        dialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                List<ActivePatientModel> requiredPatients = doQueryWithProviders(selectedItems);
                if(requiredPatients.size()>0) {
                    closedAdapter = new ClosedAdapter(requiredPatients, CompletedVisitsActivity.this);
                    no_records_found_textview.setVisibility(View.GONE);
                }
                else
                {
                    closedAdapter = new ClosedAdapter(requiredPatients, CompletedVisitsActivity.this);
                    no_records_found_textview.setVisibility(View.VISIBLE);
                    no_records_found_textview.setHint(R.string.no_records_found);
                }
                recyclerView.setAdapter(closedAdapter);
                closedAdapter.notifyDataSetChanged();
            }
        });

        dialogBuilder.setNegativeButton(getString(R.string.cancel), null);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));

        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));

    }

    private List<ActivePatientModel> doQueryWithProviders(List<String> providersUuids) {
        List<ActivePatientModel> todayPatientList = new ArrayList<>();
        String query = "SELECT distinct a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.uuid, b.first_name, b.middle_name, b.last_name, b.date_of_birth, " +
                "b.openmrs_id, o.value, c.value AS speciality FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE b.uuid = a.patientuuid " +
                "AND  a.enddate is NOT NULL  AND a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND a.uuid = c.visit_uuid  AND d.provider_uuid in ('" + StringUtils.convertUsingStringBuilder(providersUuids) + "')  " +
                "AND o.conceptuuid = '36d207d6-bee7-4b3e-9196-7d053c6eddce' AND (o.value like '%TLD Resolved%' or o.value " +
                "like '%Doctor Resolution Resolved%' or speciality like '%Agent Resolution%'  or speciality like '%Curiosity Resolution%') ORDER BY a.startdate DESC";
        final Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    try {
                        todayPatientList.add(new ActivePatientModel(
                                cursor.getString(cursor.getColumnIndexOrThrow("uuid")),
                                cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                                cursor.getString(cursor.getColumnIndexOrThrow("startdate")),
                                cursor.getString(cursor.getColumnIndexOrThrow("enddate")),
                                cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")),
                                cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                                cursor.getString(cursor.getColumnIndexOrThrow("middle_name")),
                                cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                                cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")),
                                StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")))),
                                cursor.getString(cursor.getColumnIndexOrThrow("sync")),
                                cursor.getString(cursor.getColumnIndexOrThrow("speciality")),
                                cursor.getString(cursor.getColumnIndexOrThrow("value")))
                        );
                    } catch (DAOException e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return todayPatientList;
    }

}
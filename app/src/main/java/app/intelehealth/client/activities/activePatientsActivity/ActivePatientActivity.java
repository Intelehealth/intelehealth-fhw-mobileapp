package app.intelehealth.client.activities.activePatientsActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import app.intelehealth.client.R;
import app.intelehealth.client.app.AppConstants;
import app.intelehealth.client.app.IntelehealthApplication;
import app.intelehealth.client.database.dao.EncounterDAO;
import app.intelehealth.client.database.dao.ProviderDAO;
import app.intelehealth.client.database.dao.VisitsDAO;
import app.intelehealth.client.models.ActivePatientModel;
import app.intelehealth.client.models.dto.EncounterDTO;
import app.intelehealth.client.models.dto.VisitDTO;
import app.intelehealth.client.utilities.Logger;
import app.intelehealth.client.utilities.SessionManager;

import app.intelehealth.client.activities.homeActivity.HomeActivity;
import app.intelehealth.client.utilities.StringUtils;
import app.intelehealth.client.utilities.exception.DAOException;

public class ActivePatientActivity extends AppCompatActivity {
    private static final String TAG = ActivePatientActivity.class.getSimpleName();
    private SQLiteDatabase db;
    SessionManager sessionManager = null;
    Toolbar mToolbar;
    RecyclerView mActivePatientList;
    TextView textView;
    RecyclerView recyclerView;
    MaterialAlertDialogBuilder dialogBuilder;

    private ArrayList<String> listPatientUUID = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_active_patient);
        setContentView(R.layout.activity_active_patient);
        mToolbar = findViewById(R.id.toolbar);


        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),
                R.drawable.ic_sort_white_24dp);
//        mToolbar.setOverflowIcon(drawable);

        mActivePatientList = findViewById(R.id.today_patient_recycler_view);

        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        mToolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        textView = findViewById(R.id.textviewmessage);
        recyclerView = findViewById(R.id.today_patient_recycler_view);
        sessionManager = new SessionManager(this);
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        if (sessionManager.isPullSyncFinished()) {
            textView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            doQuery();
        }

        getVisits();
    }

    private void getVisits() {

        ArrayList<String> encounterVisitUUID = new ArrayList<String>();
        HashSet<String> hsPatientUUID = new HashSet<String>();

        //Get all Visits
        VisitsDAO visitsDAO = new VisitsDAO();
        List<VisitDTO> visitsDTOList = visitsDAO.getAllVisits();

        //Get all Encounters
        EncounterDAO encounterDAO = new EncounterDAO();
        List<EncounterDTO> encounterDTOList = encounterDAO.getAllEncounters();

        //Get Visit Complete Encounters only, visit complete encounter id - bd1fbfaa-f5fb-4ebd-b75c-564506fc309e
        if (encounterDTOList.size() > 0) {
            for (int i = 0; i < encounterDTOList.size(); i++) {
                if (encounterDTOList.get(i).getEncounterTypeUuid().equalsIgnoreCase("bd1fbfaa-f5fb-4ebd-b75c-564506fc309e")) {
                    encounterVisitUUID.add(encounterDTOList.get(i).getVisituuid());
                }
            }
        }

        //Get patientUUID from visitList
        for (int i = 0; i < encounterVisitUUID.size(); i++) {

            for (int j = 0; j < visitsDTOList.size(); j++) {

                if (encounterVisitUUID.get(i).equalsIgnoreCase(visitsDTOList.get(j).getUuid())) {
                    listPatientUUID.add(visitsDTOList.get(j).getPatientuuid());
                }
            }
        }

        if (listPatientUUID.size() > 0) {

            hsPatientUUID.addAll(listPatientUUID);
            listPatientUUID.clear();
            listPatientUUID.addAll(hsPatientUUID);

        }
    }

    /**
     * This method retrieves visit details about patient for a particular date.
     *
     * @return void
     */
    private void doQuery() {
        List<ActivePatientModel> activePatientList = new ArrayList<>();
        Date cDate = new Date();
        String query = "SELECT   a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.first_name, b.middle_name, b.last_name, b.date_of_birth,b.openmrs_id  " +
                "FROM tbl_visit a, tbl_patient b " +
                "WHERE a.patientuuid = b.uuid " +
                "AND a.enddate is NULL OR a.enddate='' GROUP BY a.uuid ORDER BY a.startdate ASC";
        final Cursor cursor = db.rawQuery(query, null);

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
                                cursor.getString(cursor.getColumnIndexOrThrow("sync")))
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

        if (!activePatientList.isEmpty()) {
            for (ActivePatientModel activePatientModel : activePatientList)
                Logger.logD(TAG, activePatientModel.getFirst_name() + " " + activePatientModel.getLast_name());

            ActivePatientAdapter mActivePatientAdapter = new ActivePatientAdapter(activePatientList, ActivePatientActivity.this, listPatientUUID);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ActivePatientActivity.this);
            recyclerView.setLayoutManager(linearLayoutManager);
           /* recyclerView.addItemDecoration(new
                    DividerItemDecoration(this,
                    DividerItemDecoration.VERTICAL));*/
            recyclerView.setAdapter(mActivePatientAdapter);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_today_patient, menu);
        inflater.inflate(R.menu.today_filter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.summary_endAllVisit:
                endAllVisit();

            case R.id.action_filter:
                //function call
                displaySingleSelectionDialog();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void displaySingleSelectionDialog() {

        ProviderDAO providerDAO = new ProviderDAO();
        ArrayList selectedItems = new ArrayList<>();
        String[] creator_names = null;
        String[] creator_uuid = null;
        try {
            creator_names = providerDAO.getProvidersList().toArray(new String[0]);
            Log.d("PRAJWAL", "CREATOR PRAJWAL: " + creator_names.length);
            creator_uuid = providerDAO.getProvidersUuidList().toArray(new String[0]);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        dialogBuilder = new MaterialAlertDialogBuilder(ActivePatientActivity.this);
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
                } else if (selectedItems.contains(which)) {
                    // Else, if the item is already in the array, remove it
                    selectedItems.remove(finalCreator_uuid[which]);
                    Logger.logD(TAG, finalCreator_names[which] + finalCreator_uuid[which]);
                }
            }
        });

        dialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //display filter query code on list menu
                Logger.logD(TAG, "onclick" + i);
                doQueryWithProviders(selectedItems);
            }
        });

        dialogBuilder.setNegativeButton(getString(R.string.cancel), null);
        //dialogBuilder.show();
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));

        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);

    }


    private void endAllVisit() {

        int failedUploads = 0;

        String query = "SELECT tbl_visit.patientuuid, tbl_visit.enddate, tbl_visit.uuid," +
                "tbl_patient.first_name, tbl_patient.middle_name, tbl_patient.last_name FROM tbl_visit, tbl_patient WHERE" +
                " tbl_visit.patientuid = tbl_patient.uuid AND tbl_visit.enddate IS NULL OR tbl_visit.enddate = ''";

        final Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    boolean result = endVisit(
                            cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                            cursor.getString(cursor.getColumnIndexOrThrow("first_name")) + " " +
                                    cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("uuid"))
                    );
                    if (!result) failedUploads++;
                } while (cursor.moveToNext());
            }
        }
        cursor.close();

        if (failedUploads == 0) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        } else {
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
            //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(getString(R.string.unable_to_end) + failedUploads +
                    getString(R.string.upload_before_end_visit_active));
            alertDialogBuilder.setNeutralButton(getString(R.string.generic_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
        }

    }

    private boolean endVisit(String patientUuid, String patientName, String visitUUID) {

        return visitUUID != null;

    }

    private void doQueryWithProviders(List<String> providersuuids) {
        List<ActivePatientModel> activePatientList = new ArrayList<>();
        String query = "select  distinct a.uuid, a.sync, c.uuid AS patientuuid,a.startdate AS startdate,a.enddate AS enddate, c.first_name,c.middle_name,c.last_name,c.openmrs_id,c.date_of_birth " +
                "from tbl_visit a,tbl_encounter b ,tbl_patient c " +
                "where b.visituuid=a.uuid and b.provider_uuid in ('" + StringUtils.convertUsingStringBuilder(providersuuids) + "')  " +
                "and a.patientuuid=c.uuid and (a.enddate is null OR a.enddate='')  order by a.startdate ASC";
        Logger.logD(TAG, query);
        final Cursor cursor = db.rawQuery(query, null);

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
                                cursor.getString(cursor.getColumnIndexOrThrow("sync")))
                        );
                    } catch (DAOException e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
        } else {
            // activePatientList.clear();
            //Toast.makeText(this, "No patients where looked by this health worker!", Toast.LENGTH_SHORT).show();
        }
        if (cursor != null) {
            cursor.close();
        }

        if (!activePatientList.isEmpty()) {
            for (ActivePatientModel activePatientModel : activePatientList)
                Logger.logD(TAG, activePatientModel.getFirst_name() + " " + activePatientModel.getLast_name());

            ActivePatientAdapter mActivePatientAdapter = new ActivePatientAdapter(activePatientList, ActivePatientActivity.this, listPatientUUID);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ActivePatientActivity.this);
            recyclerView.setLayoutManager(linearLayoutManager);
            /*recyclerView.addItemDecoration(new
                    DividerItemDecoration(this,
                    DividerItemDecoration.VERTICAL));
            recyclerView.setAdapter(mActivePatientAdapter);*/
            recyclerView.setVisibility(View.VISIBLE);
            TextView t = findViewById(R.id.ttt);
            t.setVisibility(View.GONE);
        } else {
            TextView t = findViewById(R.id.ttt);
            t.setVisibility(View.VISIBLE);
            t.setHint(getString(R.string.no_data_active_patients));
            recyclerView.setVisibility(View.GONE);
            //recyclerView.addView(t);
            // Intent i = new Intent(this, HomeActivity.class);
            //startActivity(i);
            //recyclerView.setVisibility(View.GONE);
        }

    }

    private String phoneNumber(String patientuuid) throws DAOException {
        String phone = null;
        Cursor idCursor = db.rawQuery("SELECT value  FROM tbl_patient_attribute where patientuuid = ? AND person_attribute_type_uuid='14d4f066-15f5-102d-96e4-000c29c2a5d7' ", new String[]{patientuuid});
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

}




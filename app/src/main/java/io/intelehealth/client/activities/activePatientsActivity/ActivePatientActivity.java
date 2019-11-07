package io.intelehealth.client.activities.activePatientsActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.homeActivity.HomeActivity;
import io.intelehealth.client.database.InteleHealthDatabaseHelper;
import io.intelehealth.client.database.dao.EncounterDAO;
import io.intelehealth.client.database.dao.ImagesDAO;
import io.intelehealth.client.database.dao.ProviderDAO;
import io.intelehealth.client.database.dao.VisitsDAO;
import io.intelehealth.client.models.ActivePatientModel;
import io.intelehealth.client.models.dto.EncounterDTO;
import io.intelehealth.client.models.dto.VisitDTO;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.StringUtils;
import io.intelehealth.client.utilities.exception.DAOException;

public class ActivePatientActivity extends AppCompatActivity {
    private static final String TAG = ActivePatientActivity.class.getSimpleName();
    InteleHealthDatabaseHelper mDbHelper;
    private SQLiteDatabase db;
    SessionManager sessionManager = null;
    Toolbar mToolbar;
    RecyclerView mActivePatientList;
    TextView textView;
    RecyclerView recyclerView;
    AlertDialog.Builder dialogBuilder;

    private ArrayList<String> listPatientUUID = new ArrayList<String>();
    public static int index = -1;
    public static int top = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_active_patient);
        setContentView(R.layout.activity_active_patient);
        mToolbar = findViewById(R.id.toolbar);


        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),
                R.drawable.ic_sort_white_24dp);
        mToolbar.setOverflowIcon(drawable);

        mActivePatientList = findViewById(R.id.today_patient_recycler_view);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        textView = findViewById(R.id.textviewmessage);
        recyclerView = findViewById(R.id.today_patient_recycler_view);
        sessionManager = new SessionManager(this);
        mDbHelper = new InteleHealthDatabaseHelper(this);
        db = mDbHelper.getWritableDatabase();

        getVisits();

        if (sessionManager.isPullSyncFinished()) {
            textView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            doQuery();
        }

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

        //Get Visit Note Encounters only, visit note encounter id - d7151f82-c1f3-4152-a605-2f9ea7414a79
        if (encounterDTOList.size() > 0) {
            for (int i = 0; i < encounterDTOList.size(); i++) {
                if (encounterDTOList.get(i).getEncounterTypeUuid().equalsIgnoreCase("d7151f82-c1f3-4152-a605-2f9ea7414a79")) {
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
        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
        String query =
//                "SELECT tbl_visit.uuid, tbl_visit.patientuuid, tbl_visit.startdate, tbl_visit.enddate," +
//                        "tbl_patient.first_name, tbl_patient.middle_name, tbl_patient.last_name, " +
//                        "tbl_patient.date_of_birth,tbl_patient.openmrs_id,a.value AS phone_number " +
//                        "FROM tbl_visit, tbl_patient, tbl_patient_attribute a " +
//                        "WHERE tbl_visit.patientuuid = tbl_patient.uuid " +
//                        "AND tbl_visit.enddate IS NULL " +
//                        "AND a.patientuuid=tbl_patient.uuid and a.person_attribute_type_uuid='14d4f066-15f5-102d-96e4-000c29c2a5d7' " +
//                        "OR tbl_visit.enddate = '' " +
//                        "ORDER BY tbl_visit.startdate ASC";
//
                "SELECT   a.uuid, a.patientuuid, a.startdate, a.enddate, b.first_name, b.middle_name, b.last_name, b.date_of_birth,b.openmrs_id  " +
                        "FROM tbl_visit a, tbl_patient b " +
                        "WHERE a.patientuuid = b.uuid " +
                        "AND a.enddate is NULL OR a.enddate='' GROUP BY a.uuid ORDER BY a.startdate ASC";
        //  "SELECT * FROM visit, patient WHERE visit.patient_id = patient._id AND visit.start_datetime LIKE '" + currentDate + "T%'";
//        Logger.logD(TAG, query);
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
                                StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("patientuuid"))))
                        ));
                    } catch (DAOException e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
        }
        cursor.close();

        if (!activePatientList.isEmpty()) {
            for (ActivePatientModel activePatientModel : activePatientList)
                Logger.logD(TAG, activePatientModel.getFirst_name() + " " + activePatientModel.getLast_name());

            ActivePatientAdapter mActivePatientAdapter = new ActivePatientAdapter(activePatientList, ActivePatientActivity.this, listPatientUUID);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ActivePatientActivity.this);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.addItemDecoration(new
                    DividerItemDecoration(this,
                    DividerItemDecoration.VERTICAL));
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
                //alert box.
                displaySingleSelectionDialog();    //function call


            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void displaySingleSelectionDialog() {

       /* View checkBoxView = View.inflate(this, R.layout.checkbox_view, null);
        CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                // Save to shared preferences
            }
        });
        checkBox.setText("Text to the right of the check box.");*/
        ProviderDAO providerDAO = new ProviderDAO();
        ArrayList selectedItems = new ArrayList<>();
        String[] creator_names = null;
        String[] creator_uuid = null;
        try {
            creator_names = providerDAO.getProvidersList().toArray(new String[0]);
            creator_uuid = providerDAO.getProvidersUuidList().toArray(new String[0]);
        } catch (DAOException e) {
            e.printStackTrace();
        }
//        boolean[] checkedItems = {false, false, false, false};
        // ngo_numbers = getResources().getStringArray(R.array.ngo_numbers);
        dialogBuilder = new AlertDialog.Builder(ActivePatientActivity.this);
        dialogBuilder.setTitle("Filter by creator");

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

        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //display filter query code on list menu
                Logger.logD(TAG, "onclick" + i);
                doQueryWithProviders(selectedItems);
//                select distinct a.uuid,c.first_name,c.middle_name,c.last_name,c.openmrs_id,c.phone_number,c.date_of_birth from tbl_visit a,tbl_encounter b ,tbl_patient c where b.visituuid=a.uuid and b.provider_uuid in ('163b48e5-26fb-40c1-8d94-a6c873dd2869') and a.patientuuid=c.uuid and a.enddate is null order by c.first_name
            }
        });

        dialogBuilder.setNegativeButton("Cancel", null);
        //dialogBuilder.setView(checkBoxView);
        //dialogBuilder.setIcon(R.drawable.ic_sort_white_24dp);
        //  dialogBuilder.setItems(creator_names, new DialogInterface.OnClickListener() {
        // @Override
        //  public void onClick(DialogInterface dialog, int which) {
        // the user clicked on colors[which]
               /* final String a = "tel:"+"9769025715";
                final String b = "tel:"+"7304154312";

                if("Team 1".equals(ngo_numbers[which]))
                {
                    Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse(a));
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(i);
                }

                else if("Team 2".equals(ngo_numbers[which]))
                {
                    Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse(b));
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(i);
                }*/
        // }
        // });
        // dialogBuilder.show();
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));

        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));


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
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Unable to end " + failedUploads +
                    " visits.Please upload visit before attempting to end the visit.");
            alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

    }

    private boolean endVisit(String patientUuid, String patientName, String visitUUID) {

        return visitUUID != null;

    }

    private void doQueryWithProviders(List<String> providersuuids) {
        List<ActivePatientModel> activePatientList = new ArrayList<>();
        String query =
//                "select distinct a.uuid,c.uuid AS patientuuid,a.startdate AS startdate,a.enddate AS enddate," +
//                "c.first_name,c.middle_name,c.last_name,c.openmrs_id,c.phone_number,c.date_of_birth " +
//                "from tbl_visit a,tbl_encounter b ,tbl_patient c " +
//                "where b.visituuid=a.uuid and b.provider_uuid in ('" + StringUtils.convertUsingStringBuilder(providersuuids) + "') " +
//                "and a.patientuuid=c.uuid and a.enddate is null OR a.enddate='' order by a.startdate ASC";

                "select  distinct a.uuid,c.uuid AS patientuuid,a.startdate AS startdate,a.enddate AS enddate, c.first_name,c.middle_name,c.last_name,c.openmrs_id,c.date_of_birth " +
                        "from tbl_visit a,tbl_encounter b ,tbl_patient c " +
                        "where b.visituuid=a.uuid and b.provider_uuid in ('" + StringUtils.convertUsingStringBuilder(providersuuids) + "')  " +
                        "and a.patientuuid=c.uuid and (a.enddate is null OR a.enddate='')  order by a.startdate ASC";

//                "SELECT tbl_visit.uuid, tbl_visit.patientuuid, tbl_visit.startdate, tbl_visit.enddate," +
//                        "tbl_patient.first_name, tbl_patient.middle_name, tbl_patient.last_name, " +
//                        "tbl_patient.date_of_birth,tbl_patient.openmrs_id,tbl_patient.phone_number FROM tbl_visit, tbl_patient WHERE tbl_visit.patientuuid = tbl_patient.uuid " +
//                        "AND tbl_visit.enddate IS NULL " +
//                        "OR tbl_visit.enddate = '' " +
//                        "ORDER BY tbl_visit.startdate ASC";
        //  "SELECT * FROM visit, patient WHERE visit.patient_id = patient._id AND visit.start_datetime LIKE '" + currentDate + "T%'";
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
                                StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("patientuuid"))))
                        ));
                    } catch (DAOException e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
        }
        cursor.close();

        if (!activePatientList.isEmpty()) {
            for (ActivePatientModel activePatientModel : activePatientList)
                Logger.logD(TAG, activePatientModel.getFirst_name() + " " + activePatientModel.getLast_name());

            ActivePatientAdapter mActivePatientAdapter = new ActivePatientAdapter(activePatientList, ActivePatientActivity.this, listPatientUUID);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ActivePatientActivity.this);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.addItemDecoration(new
                    DividerItemDecoration(this,
                    DividerItemDecoration.VERTICAL));
            recyclerView.setAdapter(mActivePatientAdapter);
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
            Crashlytics.getInstance().core.logException(s);
        }
        idCursor.close();

        return phone;
    }

}




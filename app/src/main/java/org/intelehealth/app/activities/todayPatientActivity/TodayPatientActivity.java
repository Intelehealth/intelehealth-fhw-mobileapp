package org.intelehealth.app.activities.todayPatientActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.database.InteleHealthDatabaseHelper;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ProviderDAO;
import org.intelehealth.app.database.dao.VisitsDAO;
import org.intelehealth.app.models.TodayPatientModel;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.VisitDTO;
import org.intelehealth.app.utilities.LocaleHelper;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.VisitUtils;
import org.intelehealth.app.utilities.exception.DAOException;

public class TodayPatientActivity extends AppCompatActivity {
    private static final String TAG = TodayPatientActivity.class.getSimpleName();
    InteleHealthDatabaseHelper mDbHelper;
    private SQLiteDatabase db;
    SessionManager sessionManager = null;
    RecyclerView mTodayPatientList;
   MaterialAlertDialogBuilder dialogBuilder;

    private ArrayList<String> listPatientUUID = new ArrayList<String>();
    int limit = 20, offset = 0;
    boolean fullyLoaded = false;
    private TodayPatientAdapter todayPatientAdapter;

    public static long getTodayVisitsCount(SQLiteDatabase db) {
        int count =0;
        Date cDate = new Date();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(cDate);
        String query = "SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.first_name, b.middle_name, b.last_name, b.date_of_birth,b.openmrs_id " +
                "FROM tbl_visit a, tbl_patient b  " +
                "WHERE a.patientuuid = b.uuid " +
                "AND a.startdate LIKE '" + currentDate + "T%'";
        Logger.logD(TAG, query);
        final Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    count++;
                } while (cursor.moveToNext());
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return count;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
//        String language = sessionManager.getAppLanguage();
        //In case of crash still the app should hold the current lang fix.
        //this language code is no longer required as we are moving towards more optimised as well as generic code for localisation. Check "attachBaseContext".
//        if (!language.equalsIgnoreCase("")) {
//            Locale locale = new Locale(language);
//            Locale.setDefault(locale);
//            Configuration config = new Configuration();
//            config.locale = locale;
//            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
//        }
//        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_patient);
        setTitle(getString(R.string.title_activity_today_patient));
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_today_patient);

        Toolbar toolbar = findViewById(R.id.toolbar);

        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),
                R.drawable.ic_sort_white_24dp);
//        toolbar.setOverflowIcon(drawable);


        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        mTodayPatientList = findViewById(R.id.today_patient_recycler_view);
        LinearLayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
        mTodayPatientList.setLayoutManager(reLayoutManager);
        mTodayPatientList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!fullyLoaded && newState == RecyclerView.SCROLL_STATE_IDLE && reLayoutManager.findLastVisibleItemPosition() == todayPatientAdapter.getItemCount() -1) {
                    Toast.makeText(TodayPatientActivity.this, R.string.loading_more, Toast.LENGTH_SHORT).show();
                    offset += limit;
                    List<TodayPatientModel> allPatientsFromDB = doQuery(offset);
                    if (allPatientsFromDB.size() < limit) {
                        fullyLoaded = true;
                    }

                    todayPatientAdapter.todayPatientModelList.addAll(allPatientsFromDB);
                    todayPatientAdapter.notifyDataSetChanged();
                }
            }
        });

        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        if (sessionManager.isPullSyncFinished()) {
            List<TodayPatientModel> todayPatientModels = doQuery(offset);
            todayPatientAdapter = new TodayPatientAdapter(todayPatientModels, this, listPatientUUID);
            mTodayPatientList.setAdapter(todayPatientAdapter);
            todayPatientAdapter.setActionListener(new TodayPatientAdapter.OnActionListener() {
                @Override
                public void onEndVisitClicked(TodayPatientModel todayPatientModel, boolean hasPrescription) {
                    onEndVisit(todayPatientModel, hasPrescription);
                }
            });
        }

        getVisits();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTodayPatientList.clearOnScrollListeners();
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
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


    private List<TodayPatientModel> doQuery(int offset) {
        List<TodayPatientModel> todayPatientList = new ArrayList<>();
        Date cDate = new Date();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH).format(cDate);
        String query = "SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.first_name, b.middle_name, b.last_name, b.date_of_birth,b.openmrs_id, b.gender " +
                "FROM tbl_visit a, tbl_patient b  " +
                "WHERE a.patientuuid = b.uuid " +
                "AND a.startdate LIKE '" + currentDate + "T%'   " +
                "GROUP BY a.uuid ORDER BY a.patientuuid ASC  limit ? offset ?";
        Logger.logD(TAG, query);
        final Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(limit), String.valueOf(offset)});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    try {
                        TodayPatientModel model = new TodayPatientModel(
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
                                cursor.getString(cursor.getColumnIndexOrThrow("sync")));
                        model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                        todayPatientList.add(model
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

//        if (!todayPatientList.isEmpty()) {
//            for (TodayPatientModel todayPatientModel : todayPatientList)
//                Log.i(TAG, todayPatientModel.getFirst_name() + " " + todayPatientModel.getLast_name());
//
//            TodayPatientAdapter mTodayPatientAdapter = new TodayPatientAdapter(todayPatientList, TodayPatientActivity.this, listPatientUUID);
//            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TodayPatientActivity.this);
//            mTodayPatientList.setLayoutManager(linearLayoutManager);
//           /* mTodayPatientList.addItemDecoration(new
//                    DividerItemDecoration(TodayPatientActivity.this,
//                    DividerItemDecoration.VERTICAL));*/
//            mTodayPatientList.setAdapter(mTodayPatientAdapter);
//            mTodayPatientAdapter.setActionListener(new TodayPatientAdapter.OnActionListener() {
//                @Override
//                public void onEndVisitClicked(TodayPatientModel todayPatientModel, boolean hasPrescription) {
//                    onEndVisit(todayPatientModel, hasPrescription);
//                }
//            });
//        }
        return todayPatientList;
    }

    private void onEndVisit(TodayPatientModel todayPatientModel, boolean hasPrescription) {
        String encounterAdultIntialslocal = "";
        String encounterVitalslocal = null;
        String encounterIDSelection = "visituuid = ?";

        String visitUuid = todayPatientModel.getUuid();
        String visitnote = "", followupdate = "";
        String[] encounterIDArgs = {visitUuid};
        EncounterDAO encounterDAO = new EncounterDAO();
        Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
        if (encounterCursor != null && encounterCursor.moveToFirst()) {
            do {
                if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VITALS").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                    encounterVitalslocal = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                }
                if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_ADULTINITIAL").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                    encounterAdultIntialslocal = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                }

                if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VISIT_NOTE").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                    visitnote = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                }

            } while (encounterCursor.moveToNext());
        }
        encounterCursor.close();

        String[] visitArgs = {visitnote, UuidDictionary.FOLLOW_UP_VISIT};
        String[] columns = {"value", " conceptuuid"};
        String visitSelection = "encounteruuid = ? AND conceptuuid = ? and voided!='1' ";
        Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
        if (visitCursor.moveToFirst()) {
            do {
//                            String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                followupdate = dbValue;
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();

        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(TodayPatientActivity.this);
        if (hasPrescription) {
            alertDialogBuilder.setMessage(TodayPatientActivity.this.getResources().getString(R.string.end_visit_msg));
            alertDialogBuilder.setNegativeButton(TodayPatientActivity.this.getResources().getString(R.string.generic_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            String finalFollowupdate = followupdate;
            String finalEncounterVitalslocal = encounterVitalslocal;
            String finalEncounterAdultIntialslocal = encounterAdultIntialslocal;
            alertDialogBuilder.setPositiveButton(TodayPatientActivity.this.getResources().getString(R.string.generic_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    VisitUtils.endVisit(TodayPatientActivity.this,
                            visitUuid,
                            todayPatientModel.getPatientuuid(),
                            finalFollowupdate,
                            finalEncounterVitalslocal,
                            finalEncounterAdultIntialslocal,
                            null,
                            String.format("%s %s", todayPatientModel.getFirst_name(), todayPatientModel.getLast_name()),
                            ""
                    );
                    AppointmentDAO appointmentDAO = new AppointmentDAO();
                    //AppointmentInfo appointmentInfo=appointmentDAO.getAppointmentByVisitId(visitUuid);
                    //if(appointmentInfo!=null && appointmentInfo.getStatus().equalsIgnoreCase("booked")) {
                    appointmentDAO.deleteAppointmentByVisitId(visitUuid);

                }
            });
            AlertDialog alertDialog = alertDialogBuilder.show();
            //alertDialog.show();
            IntelehealthApplication.setAlertDialogCustomTheme(TodayPatientActivity.this, alertDialog);

        } else {
            alertDialogBuilder.setMessage(TodayPatientActivity.this.getResources().getString(R.string.error_no_data));
            alertDialogBuilder.setNeutralButton(TodayPatientActivity.this.getResources().getString(R.string.generic_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.show();
            //alertDialog.show();
            IntelehealthApplication.setAlertDialogCustomTheme(TodayPatientActivity.this, alertDialog);
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
        dialogBuilder = new MaterialAlertDialogBuilder(TodayPatientActivity.this);
        dialogBuilder.setTitle(R.string.filter_by_creator);

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

        dialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //display filter query code on list menu
                Logger.logD(TAG, "onclick" + i);
                doQueryWithProviders(selectedItems);
            }
        });

        dialogBuilder.setNegativeButton(R.string.generic_cancel, null);
//        dialogBuilder.show();

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));

        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));

        IntelehealthApplication.setAlertDialogCustomTheme(this,alertDialog);
    }

    private void doQueryWithProviders(List<String> providersuuids) {
        List<TodayPatientModel> todayPatientList = new ArrayList<>();
        Date cDate = new Date();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH).format(cDate);
        String query = "SELECT  distinct a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.first_name, b.middle_name, b.last_name, b.date_of_birth,b.openmrs_id, b.gender " +
                "FROM tbl_visit a, tbl_patient b, tbl_encounter c " +
                "WHERE a.patientuuid = b.uuid " +
                "AND c.visituuid=a.uuid and c.provider_uuid in ('" + StringUtils.convertUsingStringBuilder(providersuuids) + "')  " +
                "AND a.startdate LIKE '" + currentDate + "T%'" +
                "ORDER BY a.patientuuid ASC ";
        Logger.logD(TAG, query);
        final Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    try {
                        TodayPatientModel model = new TodayPatientModel(
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
                                cursor.getString(cursor.getColumnIndexOrThrow("sync")));
                        model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                        todayPatientList.add(model
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

        if (!todayPatientList.isEmpty()) {
            for (TodayPatientModel todayPatientModel : todayPatientList)
                Log.i(TAG, todayPatientModel.getFirst_name() + " " + todayPatientModel.getLast_name());

            TodayPatientAdapter mTodayPatientAdapter = new TodayPatientAdapter(todayPatientList, TodayPatientActivity.this, listPatientUUID);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TodayPatientActivity.this);
            mTodayPatientList.setLayoutManager(linearLayoutManager);
            mTodayPatientList.addItemDecoration(new
                    DividerItemDecoration(this,
                    DividerItemDecoration.VERTICAL));
            mTodayPatientList.setAdapter(mTodayPatientAdapter);
            mTodayPatientAdapter.setActionListener(new TodayPatientAdapter.OnActionListener() {
                @Override
                public void onEndVisitClicked(TodayPatientModel todayPatientModel, boolean hasPrescription) {
                    onEndVisit(todayPatientModel, hasPrescription);
                }
            });
        }

    }


    private void endAllVisit() {

        int failedUploads = 0;

        String query = "SELECT tbl_visit.patientuuid, tbl_visit.enddate, tbl_visit.uuid," +
                "tbl_patient.first_name, tbl_patient.middle_name, tbl_patient.last_name FROM tbl_visit, tbl_patient WHERE" +
                " tbl_visit.patientuuid = tbl_patient.uuid AND tbl_visit.enddate IS NULL OR tbl_visit.enddate = ''";

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
        if (cursor != null) {
            cursor.close();
        }

        if (failedUploads == 0) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        } else {
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
            alertDialogBuilder.setMessage(getString(R.string.unable_to_end) + failedUploads +
                    getString(R.string.upload_before_end_visit_active));
            alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            IntelehealthApplication.setAlertDialogCustomTheme(this,alertDialog);
        }

    }

    private boolean endVisit(String patientID, String patientName, String visitUUID) {

        return visitUUID != null;

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





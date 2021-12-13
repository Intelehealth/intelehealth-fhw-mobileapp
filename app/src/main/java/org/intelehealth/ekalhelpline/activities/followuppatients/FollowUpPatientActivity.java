package org.intelehealth.ekalhelpline.activities.followuppatients;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.ekalhelpline.R;
import org.intelehealth.ekalhelpline.app.AppConstants;
import org.intelehealth.ekalhelpline.database.dao.ProviderDAO;
import org.intelehealth.ekalhelpline.models.ActivePatientModel;
import org.intelehealth.ekalhelpline.models.FollowUpModel;
import org.intelehealth.ekalhelpline.models.dto.PatientDTO;
import org.intelehealth.ekalhelpline.utilities.Logger;
import org.intelehealth.ekalhelpline.utilities.SessionManager;
import org.intelehealth.ekalhelpline.utilities.StringUtils;
import org.intelehealth.ekalhelpline.utilities.UuidDictionary;
import org.intelehealth.ekalhelpline.utilities.exception.DAOException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Nishita Goyal on 27/09/21.
 * Github : @nishitagoyal
 */

public class FollowUpPatientActivity extends AppCompatActivity {
    private static final String TAG = FollowUpPatientActivity.class.getSimpleName();
    private FollowUpPatientAdapter recycler;
    RecyclerView recyclerView;
    SessionManager sessionManager = null;
    TextView msg;
    private SQLiteDatabase db;
    int limit = Integer.MAX_VALUE, offset = 0;
    MaterialAlertDialogBuilder dialogBuilder;
    ProviderDAO providerDAO = new ProviderDAO();
    Context context;
    String date_string = " ";
    String currentDate = " ";
    List<String> creatorsSelected = new ArrayList<>();
    TextView no_records_found_textview;

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
        context = FollowUpPatientActivity.this;
        no_records_found_textview = findViewById(R.id.no_records_found_textview);


        //In case of crash still the app should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }

        Date cDate = new Date();
        currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(cDate);

        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());
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
                displaySelectionDialog();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void firstQuery() {
        try {
            List<FollowUpModel> allPatients = getAllPatientsFromDB(offset,currentDate);
            if(allPatients.size()>0) {
                recycler = new FollowUpPatientAdapter(allPatients, FollowUpPatientActivity.this);
                recyclerView.setAdapter(recycler);
                no_records_found_textview.setVisibility(View.GONE); }
            else
            {   recycler = new FollowUpPatientAdapter(allPatients, FollowUpPatientActivity.this);
                recyclerView.setAdapter(recycler);
                no_records_found_textview.setVisibility(View.VISIBLE);
                no_records_found_textview.setHint(R.string.no_records_found); }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Logger.logE("firstquery", "exception", e);
        }
    }

    public List<FollowUpModel> getAllPatientsFromDB(int offset, String date) {
        List<FollowUpModel> modelList = new ArrayList<FollowUpModel>();
        String query = "SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.uuid, b.first_name, b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, c.value AS speciality, o.value FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE a.uuid = c.visit_uuid AND  a.enddate is NOT NULL AND a.patientuuid = b.uuid AND a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND o.conceptuuid = ?  AND o.value is NOT NULL GROUP BY a.patientuuid";
        final Cursor searchCursor = db.rawQuery(query,  new String[]{UuidDictionary.FOLLOW_UP_VISIT});  //"e8caffd6-5d22-41c4-8d6a-bc31a44d0c86"
        if (searchCursor.moveToFirst()) {
            do {
                try {
                    String followUpDate = searchCursor.getString(searchCursor.getColumnIndexOrThrow("value")).substring(0, 10);
                    Date followUp = new SimpleDateFormat("dd-MM-yyyy").parse(followUpDate);
                    Date currentD = new SimpleDateFormat("dd-MM-yyyy").parse(date);
                    int value = followUp.compareTo(currentD);
                    if (value == -1 || value == 0 || value == 1) {
                        modelList.add(new FollowUpModel(
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("patientuuid")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")),
                                StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("speciality")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("value")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("sync")),value));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (searchCursor.moveToNext());
        }
        searchCursor.close();

        return modelList;
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

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void displaySelectionDialog() {
        ArrayList selectedItems = new ArrayList<>();
        String[] filter_by = {"Date", "Creator"};
        dialogBuilder = new MaterialAlertDialogBuilder(FollowUpPatientActivity.this);
        dialogBuilder.setTitle(getString(R.string.filter_by));
        dialogBuilder.setMultiChoiceItems(filter_by, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                if (isChecked) {
                    // If the user checked the item, add it to the selected items
                    if(filter_by[which].equalsIgnoreCase("Date"))
                        showDateSelectionDialog();
                    else if(filter_by[which].equalsIgnoreCase("Creator")) {
                        showCreatorSelectionDialog();
                    }
                    selectedItems.add(filter_by[which]);
                } else if (selectedItems.contains(which)) {
                    // Else, if the item is already in the array, remove it
                    selectedItems.remove(filter_by[which]);
                }
            }
        });

        dialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //display filter query code on list menu
                if(selectedItems.size()==1 && selectedItems.contains("Date") && date_string!=null && date_string!=" ")
                {
                    List<FollowUpModel> requiredPatients = doQueryWithDate(offset,date_string);
                    recycler = new FollowUpPatientAdapter(requiredPatients, FollowUpPatientActivity.this);
                    if(requiredPatients.size()>0)
                        no_records_found_textview.setVisibility(View.GONE);
                    else
                    {
                        no_records_found_textview.setVisibility(View.VISIBLE);
                        no_records_found_textview.setHint(R.string.no_records_found);
                    }
                }
                else if(selectedItems.size()==1 && selectedItems.contains("Creator") && !creatorsSelected.isEmpty())
                {
                    List<FollowUpModel> requiredPatients = doQueryWithProviders(creatorsSelected,currentDate);
                    recycler = new FollowUpPatientAdapter(requiredPatients, FollowUpPatientActivity.this);
                    if(requiredPatients.size()>0)
                        no_records_found_textview.setVisibility(View.GONE);
                    else
                    {   no_records_found_textview.setVisibility(View.VISIBLE);
                        no_records_found_textview.setHint(R.string.no_records_found); }
                }
                else if(selectedItems.size()==2 && selectedItems.contains("Date") && selectedItems.contains("Creator") && date_string!=null && date_string!=" " && !creatorsSelected.isEmpty())
                {
                    List<FollowUpModel> requiredPatients = doQueryWithProvidersWithDate(creatorsSelected,date_string);
                    recycler = new FollowUpPatientAdapter(requiredPatients, FollowUpPatientActivity.this);
                    if(requiredPatients.size()>0)
                        no_records_found_textview.setVisibility(View.GONE);
                    else
                    {   no_records_found_textview.setVisibility(View.VISIBLE);
                        no_records_found_textview.setHint(R.string.no_records_found); }
                }
                recyclerView.setAdapter(recycler);
                recycler.notifyDataSetChanged();
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

    private void showDateSelectionDialog() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                SimpleDateFormat todayDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                date_string = todayDateFormat.format(calendar.getTime());
            }
        }, year, month, day);
        datePickerDialog.show();

        Button positiveButton = datePickerDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = datePickerDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));    }

    private List<String> showCreatorSelectionDialog() {
        ArrayList selectedCreators = new ArrayList<>();
        String[] creator_names = null;
        String[] creator_uuid = null;
        try {
            creator_names = providerDAO.getProvidersList().toArray(new String[0]);
            creator_uuid = providerDAO.getProvidersUuidList().toArray(new String[0]);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        dialogBuilder = new MaterialAlertDialogBuilder(FollowUpPatientActivity.this);
        dialogBuilder.setTitle(getString(R.string.filter_by_creator));
        selectedCreators.clear();
        String[] finalCreator_names = creator_names;
        String[] finalCreator_uuid = creator_uuid;
        dialogBuilder.setMultiChoiceItems(creator_names, null, new DialogInterface.OnMultiChoiceClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                Logger.logD(TAG, "multichoice" + which + isChecked);
                if (isChecked) {
                    // If the user checked the item, add it to the selected items
                    selectedCreators.add(finalCreator_uuid[which]);
                    Logger.logD(TAG, finalCreator_names[which] + finalCreator_uuid[which]);
                } else if (selectedCreators.contains(finalCreator_uuid[which])) {
                    // Else, if the item is already in the array, remove it
                    selectedCreators.remove(finalCreator_uuid[which]);
                    Logger.logD(TAG, finalCreator_names[which] + finalCreator_uuid[which]);
                }
            }
        });

        dialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //display filter query code on list menu
                Logger.logD(TAG, "onclick" + i);
                creatorsSelected.clear();
                creatorsSelected.addAll(selectedCreators);
//                doQueryWithProviders(selectedCreators,currentDate);
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
        //   IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);

        return selectedCreators;
    }

    private List<FollowUpModel> doQueryWithProvidersWithDate(List<String> providersUuids, String date) {
        List<FollowUpModel> modelList = new ArrayList<FollowUpModel>();
        String query = "SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.uuid, b.first_name, b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, c.value AS speciality, o.value FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE a.uuid = c.visit_uuid AND  a.enddate is NOT NULL AND a.patientuuid = b.uuid AND a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND " +
                "d.provider_uuid in ('" + StringUtils.convertUsingStringBuilder(providersUuids) + "') " +
                "AND o.conceptuuid = ?  AND o.value is NOT NULL GROUP BY a.patientuuid";

        final Cursor searchCursor = db.rawQuery(query,  new String[]{UuidDictionary.FOLLOW_UP_VISIT});  //"e8caffd6-5d22-41c4-8d6a-bc31a44d0c86"
        if (searchCursor.moveToFirst()) {
            do {
                try {
                    String followUpDate = searchCursor.getString(searchCursor.getColumnIndexOrThrow("value")).substring(0, 10);
                    Date followUp = new SimpleDateFormat("dd-MM-yyyy").parse(followUpDate);
                    Date currentD = new SimpleDateFormat("dd-MM-yyyy").parse(date);
                    int value = followUp.compareTo(currentD);
                    if (value == -1) {
                        modelList.add(new FollowUpModel(
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("patientuuid")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")),
                                StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("speciality")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("value")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("sync")),value));
                    } else if (value == 0) {
                        modelList.add(new FollowUpModel(
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("patientuuid")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")),
                                StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("speciality")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("value")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("sync")),value));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (searchCursor.moveToNext());
        }
        searchCursor.close();

        return modelList;
    }

    private List<FollowUpModel> doQueryWithProviders(List<String> providersUuids, String date) {
        List<FollowUpModel> modelList = new ArrayList<FollowUpModel>();
        String query = "SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.uuid, b.first_name, b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, c.value AS speciality, o.value FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE a.uuid = c.visit_uuid AND  a.enddate is NOT NULL AND a.patientuuid = b.uuid AND a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND " +
                "d.provider_uuid in ('" + StringUtils.convertUsingStringBuilder(providersUuids) + "') " +
                "AND o.conceptuuid = ?  AND o.value is NOT NULL GROUP BY a.patientuuid";

        final Cursor searchCursor = db.rawQuery(query,  new String[]{UuidDictionary.FOLLOW_UP_VISIT});  //"e8caffd6-5d22-41c4-8d6a-bc31a44d0c86"
        if (searchCursor.moveToFirst()) {
            do {
                try {
                    String followUpDate = searchCursor.getString(searchCursor.getColumnIndexOrThrow("value")).substring(0, 10);
                    Date followUp = new SimpleDateFormat("dd-MM-yyyy").parse(followUpDate);
                    Date currentD = new SimpleDateFormat("dd-MM-yyyy").parse(date);
                    int value = followUp.compareTo(currentD);
                    if (value == -1 || value == 0 || value == 1) {
                        modelList.add(new FollowUpModel(
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("patientuuid")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")),
                                StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("speciality")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("value")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("sync")),value));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (searchCursor.moveToNext());
        }
        searchCursor.close();

        return modelList;
    }


    public List<FollowUpModel> doQueryWithDate(int offset, String date) {
        List<FollowUpModel> modelList = new ArrayList<FollowUpModel>();
        String query = "SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.uuid, b.first_name, b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, c.value AS speciality, o.value FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE a.uuid = c.visit_uuid AND  a.enddate is NOT NULL AND a.patientuuid = b.uuid AND a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND o.conceptuuid = ?  AND o.value is NOT NULL GROUP BY a.patientuuid";
        final Cursor searchCursor = db.rawQuery(query,  new String[]{UuidDictionary.FOLLOW_UP_VISIT});  //"e8caffd6-5d22-41c4-8d6a-bc31a44d0c86"
        if (searchCursor.moveToFirst()) {
            do {
                try {
                    String followUpDate = searchCursor.getString(searchCursor.getColumnIndexOrThrow("value")).substring(0, 10);
                    Date followUp = new SimpleDateFormat("dd-MM-yyyy").parse(followUpDate);
                    Date currentD = new SimpleDateFormat("dd-MM-yyyy").parse(date);
                    int value = followUp.compareTo(currentD);
                    if (value == -1) {
                        modelList.add(new FollowUpModel(
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("patientuuid")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")),
                                StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("speciality")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("value")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("sync")),value));
                    } else if (value == 0) {
                        modelList.add(new FollowUpModel(
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("patientuuid")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")),
                                StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("speciality")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("value")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("sync")),value));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (searchCursor.moveToNext());
        }
        searchCursor.close();

        return modelList;
    }

}

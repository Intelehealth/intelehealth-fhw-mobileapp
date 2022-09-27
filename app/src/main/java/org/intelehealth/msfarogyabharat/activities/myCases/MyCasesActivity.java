package org.intelehealth.msfarogyabharat.activities.myCases;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.msfarogyabharat.R;
import org.intelehealth.msfarogyabharat.app.AppConstants;
import org.intelehealth.msfarogyabharat.database.dao.ProviderDAO;
import org.intelehealth.msfarogyabharat.models.MyCasesModel;
import org.intelehealth.msfarogyabharat.utilities.Logger;
import org.intelehealth.msfarogyabharat.utilities.SessionManager;
import org.intelehealth.msfarogyabharat.utilities.StringUtils;
import org.intelehealth.msfarogyabharat.utilities.exception.DAOException;
import org.intelehealth.msfarogyabharat.widget.materialprogressbar.CustomProgressDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyCasesActivity extends AppCompatActivity {

    private static final String TAG = MyCasesActivity.class.getSimpleName();
    private MyCasesAdapter myCasesAdapter;
    RecyclerView recyclerView;
    SessionManager sessionManager = null;
    TextView msg;
    private SQLiteDatabase db;
    int limit = Integer.MAX_VALUE, offset = 0;
    MaterialAlertDialogBuilder dialogBuilder;
    ProviderDAO providerDAO = new ProviderDAO();
    Context context;
    List<String> creatorsSelected = new ArrayList<>();
    List<String> ngoSelected = new ArrayList<>();
    TextView no_records_found_textview;
    String chw_name = "";
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    CustomProgressDialog customProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_cases);
        setTitle(getResources().getString(R.string.my_cases));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        context = MyCasesActivity.this;
        no_records_found_textview = findViewById(R.id.no_records_found_textview);
        chw_name = sessionManager.getProviderID();
        customProgressDialog = new CustomProgressDialog(MyCasesActivity.this);

        //In case of crash still the app should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }

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
        inflater.inflate(R.menu.my_cases_filter, menu);
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
        executorService.execute(() -> {
            runOnUiThread(() -> customProgressDialog.show());

            try {
                List<MyCasesModel> allPatients = getAllPatientsFromDB(chw_name, offset);

                runOnUiThread(() -> {
                    if (allPatients.size() > 0) {
                        myCasesAdapter = new MyCasesAdapter(allPatients, MyCasesActivity.this);
                        recyclerView.setAdapter(myCasesAdapter);
                        no_records_found_textview.setVisibility(View.GONE);
                    } else {
                        myCasesAdapter = new MyCasesAdapter(allPatients, MyCasesActivity.this);
                        recyclerView.setAdapter(myCasesAdapter);
                        no_records_found_textview.setVisibility(View.VISIBLE);
                        no_records_found_textview.setHint(R.string.no_cases);
                    }
                });

            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                Logger.logE("firstquery", "exception", e);
            }

            runOnUiThread(() -> customProgressDialog.dismiss());
        });
    }

    public List<MyCasesModel> getAllPatientsFromDB(String userUuid, int offset) {
        List<MyCasesModel> modelList = new ArrayList<MyCasesModel>();
        String query = "SELECT b.uuid, b.first_name, b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, c.value FROM tbl_patient b, tbl_patient_attribute c WHERE b.uuid = c.patientuuid AND c.person_attribute_type_uuid = 'ee0d5b25-f44c-4573-8cbe-4ac2dd88287f' AND c.value = ? GROUP BY c.patientuuid ORDER BY b.first_name";
        final Cursor searchCursor = db.rawQuery(query, new String[]{userUuid});
        if (searchCursor.moveToFirst()) {
            do {
                String query1 = "Select count(*) from tbl_visit where patientuuid = ?";
                Cursor mCount = db.rawQuery(query1, new String[]{searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid"))});
                mCount.moveToFirst();
                int count = mCount.getInt(0);
                mCount.close();
                if (count == 0) {
                    try {
                        modelList.add(new MyCasesModel(
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")),
                                StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))),
                                StringUtils.mobileNumberEmpty(getNGO(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid"))))));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            while (searchCursor.moveToNext());
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

    private String getNGO(String patientuuid) throws DAOException {
        String phone = null;
        Cursor idCursor = db.rawQuery("SELECT value FROM tbl_patient_attribute where patientuuid = ? AND person_attribute_type_uuid='a8e652d9-8ef5-48c8-8565-03a8e88886a2' ", new String[]{patientuuid});
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

    private List<String> showCreatorSelectionDialog() {
        ArrayList selectedCreators = new ArrayList<>();
        String[] creator_names = null;
        String[] creator_uuid = null;
        try {
            creator_names = providerDAO.getProvidersListUpdated().toArray(new String[0]);
            creator_uuid = providerDAO.getProvidersUuidListUpdated().toArray(new String[0]);
        } catch (DAOException e) {
            e.printStackTrace();
        }

        if (creator_names.length == 0) { //no dialog should show up when none of the HWs have cases assigned to them.
            Toast.makeText(MyCasesActivity.this, "No HWs have cases assigned to them.", Toast.LENGTH_LONG).show();
            return null;
        }
        dialogBuilder = new MaterialAlertDialogBuilder(MyCasesActivity.this);
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
                List<MyCasesModel> requiredCases = doQueryWithProviders(selectedCreators);
                if (requiredCases.size() > 0) {
                    myCasesAdapter = new MyCasesAdapter(requiredCases, MyCasesActivity.this);
                    recyclerView.setAdapter(myCasesAdapter);
                    no_records_found_textview.setVisibility(View.GONE);
                    myCasesAdapter.notifyDataSetChanged();
                } else {
                    myCasesAdapter = new MyCasesAdapter(requiredCases, MyCasesActivity.this);
                    recyclerView.setAdapter(myCasesAdapter);
                    no_records_found_textview.setVisibility(View.VISIBLE);
                    no_records_found_textview.setHint(R.string.no_cases);
                    myCasesAdapter.notifyDataSetChanged();
                }
            }
        });

        dialogBuilder.setNegativeButton(getString(R.string.cancel), null);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));

        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));

        return selectedCreators;
    }

    private List<String> showNGOSelectionDialog() {
        ArrayList selectedNGOs = new ArrayList<>();
        String[] ngos_names = null;
        try {
            ngos_names = getNGOListUpdated().toArray(new String[0]);
        } catch (DAOException e) {
            e.printStackTrace();
        }

        if (ngos_names.length == 0) { //no dialog should show up when none of the HWs have cases assigned to them.
            Toast.makeText(MyCasesActivity.this, "No NGOs have cases associated to them.", Toast.LENGTH_LONG).show();
            return null;
        }
        dialogBuilder = new MaterialAlertDialogBuilder(MyCasesActivity.this);
        dialogBuilder.setTitle(getString(R.string.filter_by_ngo));
        selectedNGOs.clear();
        String[] finalNgo_names = ngos_names;
        dialogBuilder.setMultiChoiceItems(ngos_names, null, new DialogInterface.OnMultiChoiceClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                Logger.logD(TAG, "multichoice" + which + isChecked);
                if (isChecked) {
                    // If the user checked the item, add it to the selected items
                    selectedNGOs.add(finalNgo_names[which]);

                } else if (selectedNGOs.contains(finalNgo_names[which])) {
                    // Else, if the item is already in the array, remove it
                    selectedNGOs.remove(finalNgo_names[which]);
                }
            }
        });

        dialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //display filter query code on list menu
                Logger.logD(TAG, "onclick" + i);
                ngoSelected.clear();
                ngoSelected.addAll(selectedNGOs);
                List<MyCasesModel> requiredCases = doQueryWithNGOs(selectedNGOs);
                if (requiredCases.size() > 0) {
                    myCasesAdapter = new MyCasesAdapter(requiredCases, MyCasesActivity.this);
                    recyclerView.setAdapter(myCasesAdapter);
                    no_records_found_textview.setVisibility(View.GONE);
                    myCasesAdapter.notifyDataSetChanged();
                } else {
                    myCasesAdapter = new MyCasesAdapter(requiredCases, MyCasesActivity.this);
                    recyclerView.setAdapter(myCasesAdapter);
                    no_records_found_textview.setVisibility(View.VISIBLE);
                    no_records_found_textview.setHint(R.string.no_cases);
                    myCasesAdapter.notifyDataSetChanged();
                }
            }
        });

        dialogBuilder.setNegativeButton(getString(R.string.cancel), null);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));

        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));

        return selectedNGOs;
    }

    private List<MyCasesModel> doQueryWithProviders(List<String> providersUuids) {
        List<MyCasesModel> modelList = new ArrayList<MyCasesModel>();
        String query = "SELECT b.uuid, b.first_name, b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, c.value FROM tbl_patient b, tbl_patient_attribute c WHERE b.uuid = c.patientuuid AND c.person_attribute_type_uuid = 'ee0d5b25-f44c-4573-8cbe-4ac2dd88287f' AND c.value in ('" + StringUtils.convertUsingStringBuilder(providersUuids) + "') GROUP BY c.patientuuid ORDER BY b.first_name";
        final Cursor searchCursor = db.rawQuery(query, null);
        if (searchCursor.moveToFirst()) {
            do {
                String query1 = "Select count(*) from tbl_visit where patientuuid = ?";
                Cursor mCount = db.rawQuery(query1, new String[]{searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid"))});
                mCount.moveToFirst();
                int count = mCount.getInt(0);
                mCount.close();
                if (count == 0) {
                    try {
                        modelList.add(new MyCasesModel(
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")),
                                StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))),
                                StringUtils.mobileNumberEmpty(getNGO(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid"))))));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            while (searchCursor.moveToNext());
        }
        searchCursor.close();
        return modelList;
    }

    private List<MyCasesModel> doQueryWithNGOs(List<String> selectedNGOs) {
        List<MyCasesModel> modelList = new ArrayList<MyCasesModel>();
        String query = "SELECT b.uuid, b.first_name, b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, c.value FROM tbl_patient b, tbl_patient_attribute c WHERE b.uuid = c.patientuuid AND c.person_attribute_type_uuid = 'a8e652d9-8ef5-48c8-8565-03a8e88886a2' AND c.value in ('" + StringUtils.convertUsingStringBuilder(selectedNGOs) + "') GROUP BY c.patientuuid ORDER BY b.first_name ";
        final Cursor searchCursor = db.rawQuery(query, null);
        if (searchCursor.moveToFirst()) {
            do {
                String query1 = "Select count(*) from tbl_visit where patientuuid = ?";
                Cursor mCount = db.rawQuery(query1, new String[]{searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid"))});
                mCount.moveToFirst();
                int count = mCount.getInt(0);
                mCount.close();
                if (count == 0) {
                    try {
                        modelList.add(new MyCasesModel(
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")),
                                searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")),
                                StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))),
                                StringUtils.mobileNumberEmpty(getNGO(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid"))))));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            while (searchCursor.moveToNext());
        }
        searchCursor.close();
        return modelList;
    }

    private void displaySelectionDialog() {
        final int[] checkedItems = {0};
        ArrayList selectedItems = new ArrayList<>();
        String[] filter_by = {"NGO", "Creator"};
        dialogBuilder = new MaterialAlertDialogBuilder(MyCasesActivity.this);
        dialogBuilder.setTitle(getString(R.string.filter_by));
        dialogBuilder.setSingleChoiceItems(filter_by, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if (filter_by[which].equalsIgnoreCase("NGO"))
                    showNGOSelectionDialog();
                else if (filter_by[which].equalsIgnoreCase("Creator")) {
                    showCreatorSelectionDialog();
                }
            }
        });
//        dialogBuilder.setMultiChoiceItems(filter_by, null, new DialogInterface.OnMultiChoiceClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
//                if (isChecked) {
//                    // If the user checked the item, add it to the selected items
//                    if(filter_by[which].equalsIgnoreCase("NGO"))
//                        showNGOSelectionDialog();
//                    else if(filter_by[which].equalsIgnoreCase("Creator")) {
//                        showCreatorSelectionDialog();
//                    }
//                    selectedItems.add(filter_by[which]);
//                } else if (selectedItems.contains(which)) {
//                    // Else, if the item is already in the array, remove it
//                    selectedItems.remove(filter_by[which]);
//                }
//            }
//        });

        dialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //display filter query code on list menu
                if (selectedItems.size() == 1 && selectedItems.contains("NGO") && !ngoSelected.isEmpty()) {
                    List<MyCasesModel> requiredPatients = doQueryWithNGOs(ngoSelected);
                    myCasesAdapter = new MyCasesAdapter(requiredPatients, MyCasesActivity.this);
                    if (requiredPatients.size() > 0)
                        no_records_found_textview.setVisibility(View.GONE);
                    else {
                        no_records_found_textview.setVisibility(View.VISIBLE);
                        no_records_found_textview.setHint(R.string.no_records_found);
                    }
                } else if (selectedItems.size() == 1 && selectedItems.contains("Creator") && !creatorsSelected.isEmpty()) {
                    List<MyCasesModel> requiredPatients = doQueryWithProviders(creatorsSelected);
                    myCasesAdapter = new MyCasesAdapter(requiredPatients, MyCasesActivity.this);
                    if (requiredPatients.size() > 0)
                        no_records_found_textview.setVisibility(View.GONE);
                    else {
                        no_records_found_textview.setVisibility(View.VISIBLE);
                        no_records_found_textview.setHint(R.string.no_records_found);
                    }
                }
//                else if(selectedItems.size()==2 && selectedItems.contains("NGO") && selectedItems.contains("Creator") && !ngoSelected.isEmpty() && !creatorsSelected.isEmpty())
//                {
//                    List<MyCasesModel> requiredPatients = doQueryWithProvidersWithNGO(creatorsSelected,ngoSelected);
//                    myCasesAdapter = new MyCasesAdapter(requiredPatients, MyCasesActivity.this);
//                    if(requiredPatients.size()>0)
//                        no_records_found_textview.setVisibility(View.GONE);
//                    else
//                    {   no_records_found_textview.setVisibility(View.VISIBLE);
//                        no_records_found_textview.setHint(R.string.no_records_found); }
//                }
                recyclerView.setAdapter(myCasesAdapter);
                myCasesAdapter.notifyDataSetChanged();
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

    //this function is created to query all the ngo which have cases associated with them in patient table attribute.
    public List<String> getNGOListUpdated() throws DAOException {
        List<String> ngosList = new ArrayList<>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            String query = "select distinct value from tbl_patient_attribute where person_attribute_type_uuid = 'a8e652d9-8ef5-48c8-8565-03a8e88886a2'";
            Cursor cursor = db.rawQuery(query, null);
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    ngosList.add(cursor.getString(cursor.getColumnIndexOrThrow("value")));
                }
            }
            cursor.close();
            db.setTransactionSuccessful();
        } catch (SQLException s) {
            FirebaseCrashlytics.getInstance().recordException(s);
            throw new DAOException(s);
        } finally {
            db.endTransaction();

        }
        return ngosList;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }
}
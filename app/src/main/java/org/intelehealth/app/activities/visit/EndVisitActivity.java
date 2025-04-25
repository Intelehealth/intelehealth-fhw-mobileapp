package org.intelehealth.app.activities.visit;

import static org.intelehealth.app.ayu.visit.common.VisitUtils.convertCtoFNew;
import static org.intelehealth.app.database.dao.ObsDAO.fetchDrDetailsFromLocalDb;
import static org.intelehealth.app.database.dao.VisitsDAO.allNotEndedVisits;
import static org.intelehealth.app.database.dao.VisitsDAO.thisMonths_NotEndedVisits;
import static org.intelehealth.app.database.dao.VisitsDAO.olderNotEndedVisits;
import static org.intelehealth.app.database.dao.VisitsDAO.recentNotEndedVisits;
import static org.intelehealth.app.syncModule.SyncUtils.syncNow;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.LocaleList;
import android.util.DisplayMetrics;

import org.intelehealth.app.activities.prescription.PrescriptionBuilder;
import org.intelehealth.app.ayu.visit.notification.LocalPrescriptionInfo;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.ClsDoctorDetails;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.models.VitalsObject;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.utilities.CustomLog;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.followuppatients.FollowUpPatientActivity_New;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.activities.onboarding.PrivacyPolicyActivity_New;
import org.intelehealth.app.activities.settingsActivity.Language_ProtocolsActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.PrescriptionModel;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class EndVisitActivity extends BaseActivity implements NetworkUtils.InternetCheckUpdateInterface, EndVisitAdapter.OnVisitClickListener {
    RecyclerView recycler_recent, recycler_older, recycler_month;
    NestedScrollView nestedscrollview;
    private static SQLiteDatabase db;
    private int total_counts = 0, todays_count = 0, weeks_count = 0, months_count = 0;
    private ImageButton backArrow, refresh;
    TextView recent_nodata, older_nodata, month_nodata;
    private NetworkUtils networkUtils;
    private ObjectAnimator syncAnimator;
    private int recentLimit = 15, olderLimit = 15;
    private int recentStart = 0, recentEnd = recentStart + recentLimit;
    private boolean isRecentFullyLoaded = false;

    private int olderStart = 0, olderEnd = olderStart + olderLimit;
    private boolean isolderFullyLoaded = false;
    private List<PrescriptionModel> recentCloseVisitsList, olderCloseVisitsList;
    private EndVisitAdapter recentVisitsAdapter, olderVisitsAdapter;
    private androidx.appcompat.widget.SearchView searchview_received;
    private ImageView closeButton;
    // int totalCounts_recent = 0, totalCounts_older = 0;

    private Context context = EndVisitActivity.this;
    private RelativeLayout no_patient_found_block, main_block;
    List<PrescriptionModel> recent = new ArrayList<>();
    List<PrescriptionModel> older = new ArrayList<>();
    ObsDTO complaint = new ObsDTO();
    ObsDTO famHistory = new ObsDTO();
    ObsDTO patHistory = new ObsDTO();
    ObsDTO phyExam = new ObsDTO();
    ObsDTO height = new ObsDTO();
    ObsDTO weight = new ObsDTO();
    ObsDTO pulse = new ObsDTO();
    ObsDTO bpSys = new ObsDTO();
    ObsDTO bpDias = new ObsDTO();
    ObsDTO temperature = new ObsDTO();
    ObsDTO spO2 = new ObsDTO();
    ObsDTO mBloodGroupObsDTO = new ObsDTO();
    ObsDTO haemoglobinDTO = new ObsDTO();
    ObsDTO sugarRandomDTO = new ObsDTO();
    ObsDTO resp = new ObsDTO();

    String mHeight, mWeight, mBMI, mBP, mPulse, mTemp, mSPO2, mresp;

    String diagnosisReturned = "";
    String rxReturned = "";
    String testsReturned = "";
    String adviceReturned = "";
    String doctorName = "";
    String additionalReturned = "";
    String followUpDate = "";
    String referredSpeciality = "";

    String appLanguage, patientUuid, visitUuid, state, patientName, patientGender, intentTag, visitUUID, medicalAdvice_string = "", medicalAdvice_HyperLink = "", isSynedFlag = "";
    ClsDoctorDetails objClsDoctorDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_visit);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(Color.WHITE);
        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        networkUtils = new NetworkUtils(this, this);

        handleBackPress();
        initViews();
        endVisits_data();
        refresh.setOnClickListener(v -> {
            syncNow(EndVisitActivity.this, refresh, syncAnimator);
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(setLocale(newBase));
    }

    public Context setLocale(Context context) {
        SessionManager sessionManager1 = new SessionManager(context);
        String appLanguage = sessionManager1.getAppLanguage();
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        conf.setLocale(locale);
        context.createConfigurationContext(conf);
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
        return context;
    }

    private void initViews() {
        recycler_recent = findViewById(R.id.recycler_recent);
        LinearLayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
        recycler_recent.setLayoutManager(reLayoutManager);
        recycler_older = findViewById(R.id.recycler_older);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recycler_older.setLayoutManager(layoutManager);
        searchview_received = findViewById(R.id.searchview_received);
        closeButton = searchview_received.findViewById(androidx.appcompat.R.id.search_close_btn);
        no_patient_found_block = findViewById(R.id.no_patient_found_block);
        main_block = findViewById(R.id.main_block);
        ((TextView) findViewById(R.id.search_pat_hint_txt)).setText(getString(R.string.empty_message_for_patinet_search_visit_screen));
        LinearLayout addPatientTV = findViewById(R.id.add_new_patientTV);
        addPatientTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PrivacyPolicyActivity_New.class);
                intent.putExtra("intentType", "navigateFurther");
                intent.putExtra("add_patient", "add_patient");
                startActivity(intent);
                finish();
            }
        });

        nestedscrollview = findViewById(R.id.nestedscrollview);
        nestedscrollview.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (v.getChildAt(v.getChildCount() - 1) != null) {
                // Scroll Down
                if (scrollY > oldScrollY) {
                    // update recent data as it will not go at very bottom of list.
                    CustomLog.d("TAG", "recentCloseVisitsList size: " + "A: " + recentCloseVisitsList.size());
                    if (recentCloseVisitsList != null && recentCloseVisitsList.size() == 0) {
                        isRecentFullyLoaded = true;
                    }
                    if (!isRecentFullyLoaded)
                        setRecentMoreDataIntoRecyclerView();
                    // Last Item Scroll Down.
                    if (scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) {
                        // update older data as it will not go at very bottom of list.
                        if (olderCloseVisitsList != null && olderCloseVisitsList.size() == 0) {
                            isolderFullyLoaded = true;
                            return;
                        }
                        if (!isolderFullyLoaded) {
                            if (recent != null && older != null) {
                                if (recent.size() > 0 || older.size() > 0) {
                                } else {
                                    Toast.makeText(EndVisitActivity.this, getString(R.string.loading_more), Toast.LENGTH_SHORT).show();
                                    setOlderMoreDataIntoRecyclerView();
                                }
                            }
                        }
                    }
                }
            }
        });

        recycler_month = findViewById(R.id.recycler_month);
        recent_nodata = findViewById(R.id.recent_nodata);
        older_nodata = findViewById(R.id.older_nodata);
        month_nodata = findViewById(R.id.month_nodata);
        backArrow = findViewById(R.id.backArrow);
        refresh = findViewById(R.id.refresh);
        backArrow.setOnClickListener(v -> {
            finish();
        });
        searchview_received.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchOperation(query);
                return false;   // setting to false will close the keyboard when clicked on search btn.
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.equalsIgnoreCase("")) {
                    searchview_received.setBackground(ContextCompat.getDrawable(EndVisitActivity.this, R.drawable.blue_border_bg));
                } else {
                    searchview_received.setBackground(ContextCompat.getDrawable(EndVisitActivity.this, R.drawable.ui2_common_input_bg));
                }
                return false;
            }
        });
        closeButton.setOnClickListener(v -> {
            no_patient_found_block.setVisibility(View.GONE);
            main_block.setVisibility(View.VISIBLE);
            resetData();
            searchview_received.setQuery("", false);
        });
    }

    private void resetData() {
        initLimits();
        recent.clear();
        older.clear();
        recentCloseVisitsList = recentNotEndedVisits(recentLimit, recentStart);
        olderCloseVisitsList = olderNotEndedVisits(olderLimit, olderStart);
        recentStart = recentEnd;
        recentEnd += recentLimit;
        olderStart = olderEnd;
        olderEnd += olderLimit;
        recent_older_visibility(recentCloseVisitsList, olderCloseVisitsList);
        CustomLog.d("TAG", "recentCloseVisitsList size: " + "B: " + recentCloseVisitsList.size());
        CustomLog.d("TAG", "resetData: " + recentCloseVisitsList.size() + ", " + olderCloseVisitsList.size());
        recentVisitsAdapter = new EndVisitAdapter(this, recentCloseVisitsList, this);
        recycler_recent.setNestedScrollingEnabled(false); // Note: use NestedScrollView in xml and in xml add nestedscrolling to false as well as in java for Recyclerview in case you are recyclerview and scrollview together.
        recycler_recent.setAdapter(recentVisitsAdapter);
        olderVisitsAdapter = new EndVisitAdapter(this, olderCloseVisitsList, this);
        recycler_older.setNestedScrollingEnabled(false);
        recycler_older.setAdapter(olderVisitsAdapter);
    }

    private void endVisits_data() {
        recentCloseVisits();
        olderCloseVisits();
    }

    private void initLimits() {
        recentLimit = 15;
        olderLimit = 15;
        recentStart = 0;
        recentEnd = recentStart + recentLimit;
        olderStart = 0;
        olderEnd = olderStart + olderLimit;
    }

    private void recentCloseVisits() {
        recentCloseVisitsList = recentNotEndedVisits(recentLimit, recentStart);
        CustomLog.d("TAG", "recentCloseVisitsList size: " + "C: " + recentCloseVisitsList.size());
        recentVisitsAdapter = new EndVisitAdapter(this, recentCloseVisitsList, this);
        recycler_recent.setNestedScrollingEnabled(false); // Note: use NestedScrollView in xml and in xml add nestedscrolling to false as well as in java for Recyclerview in case you are recyclerview and scrollview together.
        recycler_recent.setAdapter(recentVisitsAdapter);

        recentStart = recentEnd;
        recentEnd += recentLimit;

        todays_count = recentCloseVisitsList.size();
        if (todays_count == 0 || todays_count < 0)
            recent_nodata.setVisibility(View.VISIBLE);
        else
            recent_nodata.setVisibility(View.GONE);
    }

    private void olderCloseVisits() {
        olderCloseVisitsList = olderNotEndedVisits(olderLimit, olderStart);
        olderVisitsAdapter = new EndVisitAdapter(this, olderCloseVisitsList, this);
        recycler_older.setNestedScrollingEnabled(false);
        recycler_older.setAdapter(olderVisitsAdapter);

        olderStart = olderEnd;
        olderEnd += olderLimit;

        weeks_count = olderCloseVisitsList.size();
        if (weeks_count == 0 || weeks_count < 0)
            older_nodata.setVisibility(View.VISIBLE);
        else
            older_nodata.setVisibility(View.GONE);
    }

    // This method will be accessed every time the person scrolls the recyclerView further.
    private void setRecentMoreDataIntoRecyclerView() {
        if (recent.size() > 0 || older.size() > 0) {    // on scroll, new data loads issue fix.

        } else {
            CustomLog.d("TAG", "recentCloseVisitsList size: " + "D: " + recentCloseVisitsList.size());
            if (recentCloseVisitsList != null && recentCloseVisitsList.size() == 0) {
                isRecentFullyLoaded = true;
                return;
            }

            List<PrescriptionModel> tempList = recentNotEndedVisits(recentLimit, recentStart); // for n iteration limit be fixed == 15 and start - offset will keep skipping each records.
            if (tempList.size() > 0) {
                recentCloseVisitsList.addAll(tempList);
                CustomLog.d("TAG", "recentCloseVisitsList size: " + "E: " + recentCloseVisitsList.size());
                CustomLog.d("TAG", "setRecentMoreDataIntoRecyclerView: " + recentCloseVisitsList.size());
                recentVisitsAdapter.arrayList.addAll(tempList);
                recentVisitsAdapter.notifyDataSetChanged();
                recentStart = recentEnd;
                recentEnd += recentLimit;
            }
        }
    }

    private void setOlderMoreDataIntoRecyclerView() {
        if (recent.size() > 0 || older.size() > 0) {
        } else {
            if (olderCloseVisitsList != null && olderCloseVisitsList.size() == 0) {
                isolderFullyLoaded = true;
                return;
            }
            List<PrescriptionModel> tempList = olderNotEndedVisits(olderLimit, olderStart); // for n iteration limit be fixed == 15 and start - offset will keep skipping each records.
            if (tempList.size() > 0) {
                olderCloseVisitsList.addAll(tempList);
                CustomLog.d("TAG", "setOlderMoreDataIntoRecyclerView: " + olderCloseVisitsList.size());
                olderVisitsAdapter.arrayList.addAll(tempList);
                olderVisitsAdapter.notifyDataSetChanged();
                olderStart = olderEnd;
                olderEnd += olderLimit;
            }
        }
    }

    private void thisMonths_EndVisits() {
        List<PrescriptionModel> arrayList = thisMonths_NotEndedVisits();
        EndVisitAdapter adapter_new = new EndVisitAdapter(this, arrayList, this);
        recycler_month.setNestedScrollingEnabled(false);
        recycler_month.setAdapter(adapter_new);
        months_count = arrayList.size();
        if (months_count == 0 || months_count < 0)
            month_nodata.setVisibility(View.VISIBLE);
        else
            month_nodata.setVisibility(View.GONE);
    }

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        CustomLog.d("TAG", "updateUIForInternetAvailability: ");
        if (isInternetAvailable) {
            refresh.setImageDrawable(ContextCompat.getDrawable(EndVisitActivity.this, R.drawable.ui2_ic_internet_available));
        } else {
            refresh.setImageDrawable(ContextCompat.getDrawable(EndVisitActivity.this, R.drawable.ui2_ic_no_internet));
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

    public void onShareIconClicked(PrescriptionModel model) {
        SessionManager sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();

        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }

        String[] columnsToReturn = {"startdate"};
        String visitIdOrderBy = "startdate";
        String visitIDSelection = "uuid = ?";
        String[] visitIDArgs = {model.getVisitUuid()};
        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();

        final Cursor visitIDCursor = db.query("tbl_visit", columnsToReturn, visitIDSelection, visitIDArgs, null, null, visitIdOrderBy);
        visitIDCursor.moveToLast();
        String startDateTime = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("startdate"));
        /*visitIDCursor.close();*/

        String[] eColumns = {"visituuid", "encounter_type_uuid"};
        String[] eValues = {model.getVisitUuid(), UuidDictionary.ENCOUNTER_VITALS};
        EncounterDTO mEncounter = queryAndGetRowAsObject("tbl_encounter", eColumns, eValues, EncounterDTO.class);

        preparePrescriptionDiagnosisInfo(model.getVisitUuid());
        preparePrescriptionVitals(mEncounter.getUuid());

        String[] pColumns = {"uuid"};
        String[] pValues = {model.getPatientUuid()};
        Patient mPatient = queryAndGetRowAsObject("tbl_patient", pColumns, pValues, Patient.class);

        String visitStartDate = DateAndTimeUtils.SimpleDatetoLongDate(startDateTime);

        String fileNamePatientName = mPatient.getFirst_name() + "-" + mPatient.getLast_name().charAt(0);
        String prescriptionString = "Prescription";

        String fileName = fileNamePatientName.concat("-").concat(prescriptionString).concat("-").concat(visitStartDate).concat(".pdf");


        buildAndSavePrescription(fileName, mPatient, visitStartDate, model.getVisitUuid());

        try {
            File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", pdfFile);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setPackage("com.whatsapp");
            startActivity(intent);
            updateLocalPrescriptionInformations(model.getVisitUuid());
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, getString(R.string.please_install_whatsapp), Toast.LENGTH_LONG).show();
        }
    }

    public <T> T queryAndGetRowAsObject(String tableName, String[] conditionColumns, String[] conditionValues, Class<T> targetClass) {
        if (conditionColumns == null || conditionValues == null || conditionColumns.length != conditionValues.length) {
            throw new IllegalArgumentException("Condition columns and values must not be null and must have the same length.");
        }

        // Build the WHERE clause
        StringBuilder selectionBuilder = new StringBuilder();
        String[] selectionArgs = new String[conditionValues.length];

        for (int i = 0; i < conditionColumns.length; i++) {
            if (i > 0) {
                selectionBuilder.append(" AND ");
            }
            selectionBuilder.append(conditionColumns[i]).append(" = ?");
            selectionArgs[i] = conditionValues[i];
        }

        String selection = selectionBuilder.toString();

        // Query the database
        Cursor cursor = db.query(tableName, null, selection, selectionArgs, null, null, null);
        T obj = null;
        if (cursor != null && cursor.moveToFirst()) {
            obj = mapCursorToObject(cursor, targetClass);
            cursor.close();
        }

        return obj;
    }

    public <T> T mapCursorToObject(Cursor cursor, Class<T> targetClass) {
        try {
            T obj = targetClass.newInstance();
            for (Field field : targetClass.getDeclaredFields()) {
                field.setAccessible(true);
                String columnName = field.getName();
                int columnIndex = cursor.getColumnIndex(columnName);
                if (columnIndex != -1 && !cursor.isNull(columnIndex)) {
                    Class<?> fieldType = field.getType();
                    if (fieldType == int.class || fieldType == Integer.class) {
                        field.set(obj, cursor.getInt(columnIndex));
                    } else if (fieldType == long.class || fieldType == Long.class) {
                        field.set(obj, cursor.getLong(columnIndex));
                    } else if (fieldType == float.class || fieldType == Float.class) {
                        field.set(obj, cursor.getFloat(columnIndex));
                    } else if (fieldType == double.class || fieldType == Double.class) {
                        field.set(obj, cursor.getDouble(columnIndex));
                    } else if (fieldType == String.class) {
                        field.set(obj, cursor.getString(columnIndex));
                    } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                        field.set(obj, cursor.getInt(columnIndex) != 0);
                    }
                }
            }

            return obj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void preparePrescriptionDiagnosisInfo(String visitUuid) {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        String visitnote = "";
        EncounterDAO encounterDAO = new EncounterDAO();
        String encounterIDSelection = "visituuid = ? AND voided = ?";
        String[] encounterIDArgs = {visitUuid, "0"};
        Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
        if (encounterCursor != null && encounterCursor.moveToFirst()) {
            do {
                if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VISIT_NOTE").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                    visitnote = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                }
            } while (encounterCursor.moveToNext());

        }
        encounterCursor.close();

        String[] columns = {"value", " conceptuuid"};
        String visitSelection = "encounteruuid = ? and voided = ? and sync = ?";
        String[] visitArgs = {visitnote, "0", "TRUE"};
        Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
        if (visitCursor.moveToFirst()) {
            do {
                String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                parseData(dbConceptID, dbValue);
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();
    }


    public void preparePrescriptionVitals(String mEncounterUUID) {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        String[] columns = {"value", " conceptuuid"};
        String visitSelection = "encounteruuid = ? and voided = ? and sync = ?";
        String[] visitArgs = {mEncounterUUID, "0", "TRUE"};
        Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
        if (visitCursor.moveToFirst()) {
            do {
                String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                parseData(dbConceptID, dbValue);
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();
    }

    private void buildAndSavePrescription(String fileName, Patient patient, String visitStartDate, String visitUuid) {
        fetchAndSaveDoctorDetails(visitUuid);

        PrescriptionBuilder builder = new PrescriptionBuilder(this);
        builder.setPatientData(patient, visitStartDate);
        builder.setVitals(getVitals());
        builder.setComplaintData(formatComplaintData(complaint.getValue()));
        builder.setDiagnosis(diagnosisReturned);
        builder.setMedication(rxReturned);
        builder.setTests(testsReturned);
        builder.setAdvice(medicalAdvice_string);
        builder.setFollowUp(followUpDate);
        builder.setDoctorData(objClsDoctorDetails);
        builder.build(fileName);
    }

    private void fetchAndSaveDoctorDetails(String visitUuid) {
        if (objClsDoctorDetails != null) {
            return;
        }
        String drDetails = fetchDrDetailsFromLocalDb(visitUuid);
        objClsDoctorDetails = new Gson().fromJson(drDetails, ClsDoctorDetails.class);
    }

    private VitalsObject getVitals() {
        VitalsObject vitalsObject = new VitalsObject();
        vitalsObject.setHeight(checkAndReturnVitalsValue(height));
        vitalsObject.setWeight(checkAndReturnVitalsValue(weight));
        if (weight.getValue() != null) {
            String mWeight = weight.getValue().split(" ")[0];
            String mHeight = height.getValue().split(" ")[0];
            if ((mHeight != null && mWeight != null) && !mHeight.isEmpty() && !mWeight.isEmpty()) {
                double numerator = Double.parseDouble(mWeight) * 10000;
                double denominator = Double.parseDouble(mHeight) * Double.parseDouble(mHeight);
                double bmi_value = numerator / denominator;
                mBMI = String.format(Locale.ENGLISH, "%.2f", bmi_value);
            } else {
                mBMI = "";
            }
        }
        vitalsObject.setBmi(mBMI);
        vitalsObject.setBpsys(checkAndReturnVitalsValue(bpSys));
        vitalsObject.setBpdia(checkAndReturnVitalsValue(bpDias));
        vitalsObject.setPulse(checkAndReturnVitalsValue(pulse));
        vitalsObject.setTemperature(checkAndReturnTemperatureValue(temperature));
        vitalsObject.setResp(checkAndReturnVitalsValue(resp));
        vitalsObject.setHaemoglobin(checkAndReturnVitalsValue(haemoglobinDTO));
        vitalsObject.setBloodGroup(checkAndReturnVitalsValue(mBloodGroupObsDTO));
        vitalsObject.setSugarRandom(checkAndReturnVitalsValue(sugarRandomDTO));
        vitalsObject.setSpo2(checkAndReturnVitalsValue(spO2));
        return vitalsObject;
    }

    private void parseData(String concept_id, String value) {
        switch (concept_id) {
            case UuidDictionary.CURRENT_COMPLAINT: {
                complaint.setValue(value.replace("?<b>", Node.bullet_arrow));
                break;
            }
            case UuidDictionary.PHYSICAL_EXAMINATION: {
                phyExam.setValue(value);
                break;
            }
            case UuidDictionary.HEIGHT: {
                height.setValue(value);
                break;
            }
            case UuidDictionary.WEIGHT: {
                weight.setValue(value);
                break;
            }
            case UuidDictionary.PULSE: {
                pulse.setValue(value);
                break;
            }
            case UuidDictionary.SYSTOLIC_BP: {
                bpSys.setValue(value);
                break;
            }
            case UuidDictionary.DIASTOLIC_BP: {
                bpDias.setValue(value);
                break;
            }
            case UuidDictionary.TEMPERATURE: {
                temperature.setValue(value);
                break;
            }
            case UuidDictionary.RESPIRATORY: {
                resp.setValue(value);
                break;
            }
            case UuidDictionary.SPO2: {
                spO2.setValue(value);
                break;
            }
            case UuidDictionary.BLOOD_GROUP: {
                mBloodGroupObsDTO.setValue(value);
                break;
            }
            case UuidDictionary.HAEMOGLOBIN: {
                haemoglobinDTO.setValue(value);
                break;
            }
            case UuidDictionary.SUGAR_LEVEL_RANDOM: {
                sugarRandomDTO.setValue(value);
                break;
            }
            case UuidDictionary.TELEMEDICINE_DIAGNOSIS: {
                if (!diagnosisReturned.isEmpty()) {
                    diagnosisReturned = diagnosisReturned + ",\n" + value;
                } else {
                    diagnosisReturned = value;
                }
              /*  if (diagnosisCard.getVisibility() != View.VISIBLE) {
                    diagnosisCard.setVisibility(View.VISIBLE);
                }
                diagnosisTextView.setText(diagnosisReturned);*/
                //checkForDoctor();
                break;
            }
            case UuidDictionary.JSV_MEDICATIONS: {
                if (!rxReturned.trim().isEmpty()) {
                    rxReturned = rxReturned + "\n" + value;
                } else {
                    rxReturned = value;
                }
               /* if (prescriptionCard.getVisibility() != View.VISIBLE) {
                    prescriptionCard.setVisibility(View.VISIBLE);
                }
                prescriptionTextView.setText(rxReturned);*/
                //checkForDoctor();
                break;
            }
            case UuidDictionary.MEDICAL_ADVICE: {
                if (!adviceReturned.isEmpty()) {
                    adviceReturned = adviceReturned + "\n" + value;
                    CustomLog.d("GAME", "GAME: " + adviceReturned);
                } else {
                    adviceReturned = value;
                    CustomLog.d("GAME", "GAME_2: " + adviceReturned);
                }
              /*  if (medicalAdviceCard.getVisibility() != View.VISIBLE) {
                    medicalAdviceCard.setVisibility(View.VISIBLE);
                }*/
                //medicalAdviceTextView.setText(adviceReturned);
                CustomLog.d("Hyperlink", "hyper_global: " + medicalAdvice_string);

                int j = adviceReturned.indexOf('<');
                int i = adviceReturned.lastIndexOf('>');
                if (i >= 0 && j >= 0) {
                    medicalAdvice_HyperLink = adviceReturned.substring(j, i + 1);
                } else {
                    medicalAdvice_HyperLink = "";
                }

                CustomLog.d("Hyperlink", "Hyperlink: " + medicalAdvice_HyperLink);

                medicalAdvice_string = adviceReturned.replaceAll(medicalAdvice_HyperLink, "");
                CustomLog.d("Hyperlink", "hyper_string: " + medicalAdvice_string);

                /*
                 * variable a contains the hyperlink sent from webside.
                 * variable b contains the string data (medical advice) of patient.
                 * */
               /* medicalAdvice_string = medicalAdvice_string.replace("\n\n", "\n");
                medicalAdviceTextView.setText(Html.fromHtml(medicalAdvice_HyperLink +
                        medicalAdvice_string.replaceAll("\n", "<br><br>")));*/

                adviceReturned = adviceReturned.replaceAll("\n", "<br><br>");
                //  medicalAdviceTextView.setText(Html.fromHtml(adviceReturned));
               /* medicalAdviceTextView.setText(Html.fromHtml(adviceReturned.replace("Doctor_", "Doctor")));
                medicalAdviceTextView.setMovementMethod(LinkMovementMethod.getInstance());
                CustomLog.d("hyper_textview", "hyper_textview: " + medicalAdviceTextView.getText().toString());*/
                //checkForDoctor();
                break;
            }
            case UuidDictionary.REQUESTED_TESTS: {
                if (!testsReturned.isEmpty()) {
                    testsReturned = testsReturned + "\n\n" + Node.bullet + " " + value;
                } else {
                    testsReturned = Node.bullet + " " + value;
                }
              /*  if (requestedTestsCard.getVisibility() != View.VISIBLE) {
                    requestedTestsCard.setVisibility(View.VISIBLE);
                }
                requestedTestsTextView.setText(testsReturned);*/
                //checkForDoctor();
                break;
            }

            case UuidDictionary.REFERRED_SPECIALIST: {
                if (!referredSpeciality.isEmpty() && !referredSpeciality.contains(value)) {
                    referredSpeciality = referredSpeciality + "\n\n" + Node.bullet + " " + value;
                } else {
                    referredSpeciality = Node.bullet + " " + value;
                }
            }

            case UuidDictionary.ADDITIONAL_COMMENTS: {

//                additionalCommentsCard.setVisibility(View.GONE);

                if (!additionalReturned.isEmpty()) {
                    additionalReturned = additionalReturned + "," + value;
                } else {
                    additionalReturned = value;
                }
////                if (additionalCommentsCard.getVisibility() != View.VISIBLE) {
////                    additionalCommentsCard.setVisibility(View.VISIBLE);
////                }
//                additionalCommentsTextView.setText(additionalReturned);
                //checkForDoctor();
                break;
            }
            case UuidDictionary.FOLLOW_UP_VISIT: {
                if (!followUpDate.isEmpty()) {
                    followUpDate = followUpDate + "," + value;
                } else {
                    followUpDate = value;
                }
              /*  if (followUpDateCard.getVisibility() != View.VISIBLE) {
                    followUpDateCard.setVisibility(View.VISIBLE);
                }
                followUpDateTextView.setText(followUpDate);*/
                //checkForDoctor();
                break;
            }

            default:
                break;
        }
    }

    private String formatComplaintData(String mComplaint) {
        String[] mComplaints = org.apache.commons.lang3.StringUtils.split(mComplaint, Node.bullet_arrow);
        StringBuilder formattedData = new StringBuilder();
        String colon = ":";
        String result = "";

        if (mComplaints != null) {
            for (String mc : mComplaints) {
                String[] complaints = {mc};
                if (complaints != null) {
                    for (String value : complaints) {
                        if (value == null || value.trim().isEmpty()) {
                            continue;
                        }

                        if (value.contains("Associated symptoms")) {
                            continue;
                        }

                        try {
                            int colonIndex = value.indexOf(colon);
                            if (colonIndex > 0) {
                                String formattedValue = value.substring(0, colonIndex).trim();
                                formattedData.append(Node.big_bullet).append(" ").append(formattedValue).append("\n");
                            }
                        } catch (Exception e) {
                            Log.e("FormatComplaint", "Error formatting complaint data", e);
                        }
                    }
                }
            }
        }

        if (formattedData.length() > 0) {
            result = formattedData.toString()
                    .replaceAll("<b>", "")
                    .replaceAll("</b>", "");

            if (result.endsWith("\n")) {
                result = result.substring(0, result.lastIndexOf("\n"));
            }
        }

        return result;
    }

    public String checkAndReturnVitalsValue(ObsDTO dto) {
        if (dto == null) {
            return "NA";
        } else if (dto.getValue() == null || dto.getValue().equalsIgnoreCase("0")) {
            return "NA";
        } else {
            return dto.getValue();
        }
    }

    public String checkAndReturnTemperatureValue(ObsDTO dto) {
        if (dto == null || dto.getValue() == null || dto.getValue().isEmpty()) {
            return "NA";
        } else {
            return convertCtoFNew(dto.getValue());
        }
    }

    private void updateLocalPrescriptionInformations(String visituuid) {
        List<LocalPrescriptionInfo> prescriptionDataList = new ArrayList<>();
        Gson gson = new Gson();
        SharedPreferences sharedPreference = IntelehealthApplication.getAppContext().getSharedPreferences(IntelehealthApplication.getAppContext().getString(R.string.prescription_share_key), Context.MODE_PRIVATE);
        String prescriptionListJson = sharedPreference.getString(AppConstants.PRESCRIPTION_DATA_LIST, "");
        if (!prescriptionListJson.isEmpty()) {
            Type type = new TypeToken<List<LocalPrescriptionInfo>>() {
            }.getType();
            prescriptionDataList = gson.fromJson(prescriptionListJson, type);
            for (LocalPrescriptionInfo lpi : prescriptionDataList) {
                if (lpi.getVisitUUID().equals(visituuid)) {
                    lpi.setShareStatus(true);
                }
            }
        }
        String prescriptionDataListJson = gson.toJson(prescriptionDataList);
        sharedPreference.edit().putString(AppConstants.PRESCRIPTION_DATA_LIST, prescriptionDataListJson).apply();
        sharedPreference.edit().putBoolean(AppConstants.SHARED_ANY_PRESCRIPTION, true).apply();
    }


  /*  @Override
    public int getTotalCounts() {
        total_counts = todays_count + weeks_count + months_count;
        return total_counts;
    }*/

    private void searchOperation(String query) {
        CustomLog.v("Search", "Search Word: " + query);
        query = query.toLowerCase().trim();
        query = query.replaceAll(" {2}", " ");
        CustomLog.d("TAG", "searchOperation: " + query);

//        List<PrescriptionModel> recent = new ArrayList<>();
//        List<PrescriptionModel> older = new ArrayList<>();

        String finalQuery = query;
        new Thread(new Runnable() {
            @Override
            public void run() {
                //  List<PrescriptionModel> allCloseList = allNotEndedVisits();
                List<PrescriptionModel> allRecentList = recentNotEndedVisits();
                List<PrescriptionModel> allOlderList = olderNotEndedVisits();
                CustomLog.d("TAG", "searchListReturned: " + allRecentList.size() + ", " + allOlderList.size());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!finalQuery.isEmpty()) {
                            // recent- start
                            recent.clear();
                            older.clear();

                            if (allRecentList.size() > 0) {
                                for (PrescriptionModel model : allRecentList) {
                                    if (model.getMiddle_name() != null) {
                                        String firstName = model.getFirst_name().toLowerCase();
                                        String middleName = model.getMiddle_name().toLowerCase();
                                        String lastName = model.getLast_name().toLowerCase();
                                        String fullPartName = firstName + " " + lastName;
                                        String fullName = firstName + " " + middleName + " " + lastName;

                                        if (firstName.contains(finalQuery) || middleName.contains(finalQuery) ||
                                                lastName.contains(finalQuery) || fullPartName.contains(finalQuery) || fullName.contains(finalQuery)) {
                                            recent.add(model);
                                        } else {
                                            // dont add in list value.
                                        }
                                    } else {
                                        String firstName = model.getFirst_name().toLowerCase();
                                        String lastName = model.getLast_name().toLowerCase();
                                        String fullName = firstName + " " + lastName;

                                        if (firstName.contains(finalQuery) || lastName.contains(finalQuery) || fullName.contains(finalQuery)) {
                                            recent.add(model);
                                        } else {
                                            // dont add in list value.
                                        }
                                    }
                                }
                            }

                            if (allOlderList.size() > 0) {
                                for (PrescriptionModel model : allOlderList) {
                                    if (model.getMiddle_name() != null) {
                                        String firstName = model.getFirst_name().toLowerCase();
                                        String middleName = model.getMiddle_name().toLowerCase();
                                        String lastName = model.getLast_name().toLowerCase();
                                        String fullPartName = firstName + " " + lastName;
                                        String fullName = firstName + " " + middleName + " " + lastName;

                                        if (firstName.contains(finalQuery) || middleName.contains(finalQuery)
                                                || lastName.contains(finalQuery) || fullPartName.contains(finalQuery) || fullName.contains(finalQuery)) {
                                            older.add(model);
                                        } else {
                                            // do nothing
                                        }
                                    } else {
                                        String firstName = model.getFirst_name().toLowerCase();
                                        String lastName = model.getLast_name().toLowerCase();
                                        String fullName = firstName + " " + lastName;

                                        if (firstName.contains(finalQuery) || lastName.contains(finalQuery) || fullName.contains(finalQuery)) {
                                            older.add(model);
                                        } else {
                                            // do nothing
                                        }
                                    }
                                }
                            }

                            recentVisitsAdapter = new EndVisitAdapter(context, recent, EndVisitActivity.this);
                            recycler_recent.setNestedScrollingEnabled(false);
                            recycler_recent.setAdapter(recentVisitsAdapter);

                            olderVisitsAdapter = new EndVisitAdapter(context, older, EndVisitActivity.this);
                            recycler_older.setNestedScrollingEnabled(false);
                            recycler_older.setAdapter(olderVisitsAdapter);

                            /**
                             * Checking here the query that is entered and it is not empty so check the size of all of these
                             * arraylists; if there size is 0 than show the no patient found view.
                             */
                            int allCount = recent.size() + older.size();
                            allCountVisibility(allCount);
                            recent_older_visibility(recent, older);
                        }
                    }
                });
            }
        }).start();

    }

    private void recent_older_visibility(List<PrescriptionModel> recent, List<PrescriptionModel> older) {
        if (recent.size() == 0 || recent.size() < 0)
            recent_nodata.setVisibility(View.VISIBLE);
        else
            recent_nodata.setVisibility(View.GONE);

        if (older.size() == 0 || older.size() < 0)
            older_nodata.setVisibility(View.VISIBLE);
        else
            older_nodata.setVisibility(View.GONE);
    }

    private void allCountVisibility(int allCount) {
        if (allCount == 0 || allCount < 0) {
            no_patient_found_block.setVisibility(View.VISIBLE);
            main_block.setVisibility(View.GONE);
        } else {
            no_patient_found_block.setVisibility(View.GONE);
            main_block.setVisibility(View.VISIBLE);
        }
    }

    void handleBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(context, HomeScreenActivity_New.class);
                startActivity(intent);
            }
        });
    }
}
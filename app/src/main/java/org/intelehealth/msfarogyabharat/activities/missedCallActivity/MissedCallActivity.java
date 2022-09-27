package org.intelehealth.msfarogyabharat.activities.missedCallActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.msfarogyabharat.R;
import org.intelehealth.msfarogyabharat.activities.activePatientsActivity.ActivePatientActivity;
import org.intelehealth.msfarogyabharat.activities.searchPatientActivity.SearchPatientAdapter;
import org.intelehealth.msfarogyabharat.app.AppConstants;
import org.intelehealth.msfarogyabharat.models.MissedCallModel;
import org.intelehealth.msfarogyabharat.models.dto.PatientDTO;
import org.intelehealth.msfarogyabharat.utilities.Base64Utils;
import org.intelehealth.msfarogyabharat.utilities.NetworkConnection;
import org.intelehealth.msfarogyabharat.utilities.SessionManager;
import org.intelehealth.msfarogyabharat.utilities.StringUtils;
import org.intelehealth.msfarogyabharat.utilities.UrlModifiers;
import org.intelehealth.msfarogyabharat.utilities.exception.DAOException;
import org.intelehealth.msfarogyabharat.widget.materialprogressbar.CustomProgressDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MissedCallActivity extends AppCompatActivity {

    private static final String TAG = ActivePatientActivity.class.getSimpleName();
    private SQLiteDatabase db;
    SessionManager sessionManager = null;
    Toolbar mToolbar;
    RecyclerView recyclerView;
    MaterialAlertDialogBuilder dialogBuilder;
    List<String> requiredNumList = new ArrayList<>();
    List<PatientDTO> requiredPatientList = new ArrayList<>();
    private SearchPatientAdapter adapter;
    TextView errorTV;
    CustomProgressDialog customProgressDialog;
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    private boolean shouldAllowBack = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missed_call);
        setTitle(getString(R.string.missed_calls));
        customProgressDialog = new CustomProgressDialog(MissedCallActivity.this);

        //set toolbar
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        mToolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        errorTV = findViewById(R.id.textviewmessage);
        errorTV.setVisibility(View.GONE);
        recyclerView = findViewById(R.id.today_patient_recycler_view);

        fetchAllMissedNum();

//        populateList(allPatientList, requiredNumList);
    }

    private void populateList(List<PatientDTO> requiredPatientList) {
        if (requiredPatientList.size() > 0) {
            errorTV.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            LinearLayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(reLayoutManager);
            adapter = new SearchPatientAdapter(requiredPatientList, MissedCallActivity.this);
            recyclerView.setAdapter(adapter);
        }
    }

    private void fetchAllMissedNum() {
        shouldAllowBack = false;
        customProgressDialog.show();

        executorService.execute(() -> {

            if (!NetworkConnection.isOnline(this)) {
                runOnUiThread(() -> {
                    customProgressDialog.dismiss();
                    Toast.makeText(MissedCallActivity.this, R.string.no_network, Toast.LENGTH_LONG).show();
                    shouldAllowBack = true;
                });
                return;
            }

            List<String> missedCallNumList = new ArrayList<>();
            //String encoded = sessionManager.getEncoded();
            Base64Utils base64Utils = new Base64Utils();
            String encoded = base64Utils.encoded("nurse1", "Nurse123");
            UrlModifiers urlModifiers = new UrlModifiers();
            String url = urlModifiers.getMissedCallsUrl();
            Single<MissedCallModel> missedCallRequest = AppConstants.apiInterface.MISSED_CALL(url, "Basic "/*bnVyc2UxOk51cnNlMTIz"*/ + encoded);
            missedCallRequest.subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(new DisposableSingleObserver<MissedCallModel>() {
                        @Override
                        public void onSuccess(MissedCallModel missedCallModel) {
                            if (missedCallModel != null && missedCallModel.getData() != null) {
                                for (int i = 0; i < missedCallModel.getData().size(); i++) {
                                    missedCallNumList.add(missedCallModel.getData().get(i).getNoanswer());
                                }

                                if (missedCallNumList.size() > 0) {
                                    requiredPatientList = getAllPatientsFromDB(missedCallNumList);

                                    if (requiredPatientList.size() > 0) {
                                        runOnUiThread(() -> {
                                            customProgressDialog.dismiss();
                                            errorTV.setVisibility(View.GONE);
                                            recyclerView.setVisibility(View.VISIBLE);
                                            LinearLayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
                                            recyclerView.setLayoutManager(reLayoutManager);
                                            adapter = new SearchPatientAdapter(requiredPatientList, MissedCallActivity.this);
                                            recyclerView.setAdapter(adapter);
                                            shouldAllowBack = true;
                                        });
                                    } else {
                                        runOnUiThread(() -> {
                                            customProgressDialog.dismiss();
                                            shouldAllowBack = true;
                                            errorTV.setVisibility(View.VISIBLE);
                                        });
                                    }
                                } else {
                                    runOnUiThread(() -> {
                                        customProgressDialog.dismiss();
                                        shouldAllowBack = true;
                                        errorTV.setVisibility(View.VISIBLE);
                                    });
                                }
                            } else {
                                runOnUiThread(() -> {
                                    customProgressDialog.dismiss();
                                    shouldAllowBack = true;
                                    errorTV.setVisibility(View.VISIBLE);
                                });
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            runOnUiThread(() -> {
                                customProgressDialog.dismiss();
                                shouldAllowBack = true;
                                errorTV.setVisibility(View.VISIBLE);
                                System.out.println(e);
                            });
                        }
                    });
        });
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public List<PatientDTO> getAllPatientsFromDB(List<String> requiredNumList) {
        List<PatientDTO> modelList = new ArrayList<PatientDTO>();
        String query = "Select b.openmrs_id,b.first_name,b.last_name,b.middle_name,b.uuid,b.date_of_birth from tbl_patient_attribute a, tbl_patient b where a.value in " +
                "('" + StringUtils.convertUsingStringBuilder(requiredNumList) + "')  " + "AND a.patientuuid = b.uuid GROUP BY b.openmrs_id ORDER BY b.first_name";
        final Cursor searchCursor = db.rawQuery(query, null);
        try {
            if (searchCursor.moveToFirst()) {
                do {
                    PatientDTO model = new PatientDTO();
                    model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                    model.setFirstname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")));
                    model.setLastname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")));
                    model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                    model.setUuid(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")));
                    model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
                    model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))));
                    modelList.add(model);
                } while (searchCursor.moveToNext());
            }
            searchCursor.close();
        } catch (DAOException e) {
            e.printStackTrace();
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }

    @Override
    public void onBackPressed() {
        if (shouldAllowBack)
            super.onBackPressed();
    }
}
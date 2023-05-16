package org.intelehealth.ekalarogya.activities.setupActivity;

import static org.intelehealth.ekalarogya.utilities.StringUtils.getDistanceStrings;

import android.accounts.Account;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.parse.Parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.app.AppConstants;
import org.intelehealth.ekalarogya.app.IntelehealthApplication;
import org.intelehealth.ekalarogya.database.dao.NewLocationDao;
import org.intelehealth.ekalarogya.models.DownloadMindMapRes;
import org.intelehealth.ekalarogya.models.Location;
import org.intelehealth.ekalarogya.models.loginModel.LoginModel;
import org.intelehealth.ekalarogya.models.loginProviderModel.LoginProviderModel;
import org.intelehealth.ekalarogya.models.statewise_location.ChildLocation;
import org.intelehealth.ekalarogya.models.statewise_location.District_Sanch_Village;
import org.intelehealth.ekalarogya.models.statewise_location.Result;
import org.intelehealth.ekalarogya.models.statewise_location.Setup_LocationModel;
import org.intelehealth.ekalarogya.networkApiCalls.ApiClient;
import org.intelehealth.ekalarogya.networkApiCalls.ApiInterface;
import org.intelehealth.ekalarogya.utilities.AdminPassword;
import org.intelehealth.ekalarogya.utilities.authJWT_API.ApiCallUtils;
import org.intelehealth.ekalarogya.utilities.Base64Utils;
import org.intelehealth.ekalarogya.utilities.DialogUtils;
import org.intelehealth.ekalarogya.utilities.DownloadMindMaps;
import org.intelehealth.ekalarogya.utilities.Logger;
import org.intelehealth.ekalarogya.utilities.NetworkConnection;
import org.intelehealth.ekalarogya.utilities.SessionManager;
import org.intelehealth.ekalarogya.utilities.StringEncryption;
import org.intelehealth.ekalarogya.utilities.UrlModifiers;
import org.intelehealth.ekalarogya.utilities.exception.DAOException;
import org.intelehealth.ekalarogya.widget.materialprogressbar.CustomProgressDialog;

import org.intelehealth.ekalarogya.activities.homeActivity.HomeActivity;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class SetupActivity extends AppCompatActivity {

    private static final String TAG = SetupActivity.class.getSimpleName();
    private boolean isLocationFetched;
    String BASE_URL = "";
    private static final int PERMISSION_ALL = 1;
    private long createdRecordsCount = 0;
    ProgressDialog mProgressDialog;

    //    protected AccountManager manager;
    UrlModifiers urlModifiers = new UrlModifiers();
    Base64Utils base64Utils = new Base64Utils();
    String encoded = null;
    AlertDialog.Builder dialog;
    String key = null;
    String licenseUrl = null;
    SessionManager sessionManager = null;
    public File base_dir;
    public String[] FILES;
    //        private TestSetup mAuthTask = null;
    private List<Location> mLocations = new ArrayList<>();
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mAdminPasswordView;
    private EditText mUrlField;
    private Button mLoginButton;
    // private Spinner mDropdownLocation;
    private Spinner spinner_state, spinner_district,
            spinner_sanch, spinner_village;
    private TextView mAndroidIdTextView;
    private RadioButton r1;
    private RadioButton r2;
    final Handler mHandler = new Handler();
    boolean click_box = false;

    Context context;
    private String mindmapURL = "";
    private DownloadMindMaps mTask;
    CustomProgressDialog customProgressDialog;
    HashMap<String, String> hashMap1, hashMap2, hashMap3, hashMap4 = null;
    boolean value = false;
    String base_url;
    Map.Entry<String, String> village_name;
    int state_count = 0, district_count = 0, sanch_count = 0, village_count = 0;
    private String selectedState = "", selectedDistrict = "", selectedSanch = "", selectedVillage = "";
    NewLocationDao newLocationDao = null;

    private RadioGroup subCentreRadioGroup, primaryCentreRadioGroup, communityHealthCentreRadioGroup, districtHospitalRadioGroup,
            medicalStoreRadioGroup, pathologicalLabRadioGroup, privateClinicWithMbbsDoctorRadioGroup, privateClinicWithAlternateMedicalRadioGroup,
            jalJeevanYojanaSchemeRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        getSupportActionBar();
        sessionManager = new SessionManager(this);
        // Persistent login information
//        manager = AccountManager.get(SetupActivity.this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);

        String language = sessionManager.getAppLanguage();
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }

        setTitle(R.string.title_activity_setup);

        context = SetupActivity.this;
        customProgressDialog = new CustomProgressDialog(context);

        // Set up the login form.
        mEmailView = findViewById(R.id.email);
        // populateAutoComplete(); TODO: create our own autocomplete code

        mLoginButton = findViewById(R.id.setup_submit_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!areFieldsValid()) {
                    Toast.makeText(context, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                    return;
                }
                attemptLogin();
            }
        });

        r1 = findViewById(R.id.demoMindmap);
        r2 = findViewById(R.id.downloadMindmap);

        mPasswordView = findViewById(R.id.password);

        mAdminPasswordView = findViewById(R.id.admin_password);

        Button submitButton = findViewById(R.id.setup_submit_button);

        mUrlField = findViewById(R.id.editText_URL);
        //   mDropdownLocation = findViewById(R.id.spinner_location);
        spinner_state = findViewById(R.id.spinner_state);
        spinner_district = findViewById(R.id.spinner_district);
        spinner_sanch = findViewById(R.id.spinner_sanch);
        spinner_village = findViewById(R.id.spinner_village);

        // Set up for Radio Group.
        subCentreRadioGroup = findViewById(R.id.distance_to_sub_centre_radio_group);
        primaryCentreRadioGroup = findViewById(R.id.distance_to_nearest_primary_centre_radio_group);
        communityHealthCentreRadioGroup = findViewById(R.id.distance_to_nearest_community_health_centre_radio_group);
        districtHospitalRadioGroup = findViewById(R.id.distance_to_nearest_district_hospital_radio_group);
        medicalStoreRadioGroup = findViewById(R.id.distance_to_nearest_medical_store_radio_group);
        pathologicalLabRadioGroup = findViewById(R.id.distance_to_nearest_pathological_lab_radio_group);
        privateClinicWithMbbsDoctorRadioGroup = findViewById(R.id.distance_to_nearest_private_clinic_with_mbbs_doctor_radio_group);
        privateClinicWithAlternateMedicalRadioGroup = findViewById(R.id.distance_to_nearest_private_clinic_with_alternate_medical_radio_group);
        jalJeevanYojanaSchemeRadioGroup = findViewById(R.id.jal_jeevan_yojana_scheme_radio_group);

        spinner_state.setEnabled(false);
        spinner_district.setEnabled(false);
        spinner_sanch.setEnabled(false);
        spinner_village.setEnabled(false);

        mAdminPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.admin_password || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mAndroidIdTextView = findViewById(R.id.textView_Aid);
        String deviceID = getResources().getString(R.string.device_ID) + IntelehealthApplication.getAndroidId();
        mAndroidIdTextView.setText(deviceID);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!areFieldsValid()) {
                    Toast.makeText(context, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                    return;
                }
                attemptLogin();
                //progressBar.setVisibility(View.VISIBLE);
                //progressBar.setProgress(0);
            }
        });
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showOkDialog(this, getString(R.string.generic_warning), getString(R.string.setup_internet), getString(R.string.generic_ok));

        mUrlField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isLocationFetched = false;
                mEmailView.setError(null);
                state_count = 0;
                district_count = 0;
                sanch_count = 0;
                village_count = 0;
                empty_spinner("url");
            }

            @Override
            public void afterTextChanged(Editable s) {

                mHandler.removeCallbacksAndMessages(null);
                mHandler.postDelayed(userStoppedTyping, 1500); // 1.5 second


            }

            Runnable userStoppedTyping = new Runnable() {

                @Override
                public void run() {
                    //testing.intelehealth.org
                    // user didn't typed for 1.5 seconds, do whatever you want
                    if (!mUrlField.getText().toString().trim().isEmpty() && mUrlField.getText().toString().length() >= 12) {
                        if (Patterns.WEB_URL.matcher(mUrlField.getText().toString()).matches()) {
                            String BASE_URL = "https://" + mUrlField.getText().toString() + ":3004/api/openmrs/";
                            if (URLUtil.isValidUrl(BASE_URL) && !isLocationFetched && !BASE_URL.contains("?"))
                                value = getLocationFromServer(BASE_URL); //state wise locations...
                            else
                                Toast.makeText(SetupActivity.this, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
                        }

                        /*if (Patterns.WEB_URL.matcher(mUrlField.getText().toString()).matches()) {
                            String BASE_URL = "https://" + mUrlField.getText().toString() + "/openmrs/ws/rest/v1/";
                            base_url = "https://" + mUrlField.getText().toString() + "/openmrs/ws/rest/v1/";
                            if (URLUtil.isValidUrl(BASE_URL) && !isLocationFetched && !BASE_URL.contains("?"))
                                value = getLocationFromServer(BASE_URL); //state wise locations...
                            else
                                Toast.makeText(SetupActivity.this, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
                        }*/
                    }
                }
            };

        });

        spinner_state.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //district wise locations...
                if (position != 0) {
                    String state_uuid = "";
                    selectedState = spinner_state.getSelectedItem().toString();
                    List<String> district_locations = newLocationDao.getDistrictList(selectedState, context);
                    if (district_locations.size() > 1) {
                        LocationArrayAdapter locationArrayAdapter =
                                new LocationArrayAdapter(SetupActivity.this, district_locations);

                        spinner_district.setEnabled(true);
                        spinner_district.setAlpha(1);
                        spinner_district.setAdapter(locationArrayAdapter);
                        isLocationFetched = true;
                    } else {
                        empty_spinner("state");
                    }
                }

                /*String state_uuid = "";

                selectedState = spinner_state.getSelectedItem().toString();

                if (state_count == 0) {
                    if (value && parent.getSelectedItemPosition() > 0) {
                        for (Map.Entry<String, String> entry : hashMap1.entrySet()) {
                            String list = entry.getValue();
                            // Do things with the list
                            if (list.equalsIgnoreCase(parent.getItemAtPosition(position).toString())) {
                                state_uuid = entry.getKey();
                            }
                        }
                        value = getLocationFromServer_District(base_url, state_uuid, "state");
                        state_count = parent.getSelectedItemPosition();
                    }
                } else if (state_count == parent.getSelectedItemPosition()) {
                    if (value && parent.getSelectedItemPosition() > 0) {
                        for (Map.Entry<String, String> entry : hashMap1.entrySet()) {
                            String list = entry.getValue();
                            // Do things with the list
                            if (list.equalsIgnoreCase(parent.getItemAtPosition(position).toString())) {
                                state_uuid = entry.getKey();
                            }
                        }
                        value = getLocationFromServer_District(base_url, state_uuid, "state");
                    }
                } else {
                    // Toast.makeText(context, "Enter Url", Toast.LENGTH_SHORT).show();
                    //  mUrlField.getText().clear();
                    empty_spinner("state");
                    if (value && parent.getSelectedItemPosition() > 0) {
                        for (Map.Entry<String, String> entry : hashMap1.entrySet()) {
                            String list = entry.getValue();
                            // Do things with the list
                            if (list.equalsIgnoreCase(parent.getItemAtPosition(position).toString())) {
                                state_uuid = entry.getKey();
                            }
                        }
                        value = getLocationFromServer_District(base_url, state_uuid, "state");
                    }
                }*/

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //this will give Sanch...
        spinner_district.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //district wise locations...

                if (position != 0) {
                    String district_uuid = "";
                    selectedDistrict = spinner_district.getSelectedItem().toString();
                    List<String> sanch_locations = newLocationDao.getSanchList(selectedState, selectedDistrict, context);
                    if (sanch_locations.size() > 1) {
                        LocationArrayAdapter locationArrayAdapter =
                                new LocationArrayAdapter(SetupActivity.this, sanch_locations);

                        spinner_sanch.setEnabled(true);
                        spinner_sanch.setAlpha(1);
                        spinner_sanch.setAdapter(locationArrayAdapter);
                        isLocationFetched = true;
                    } else {
                        empty_spinner("district");
                    }
                } else {
                    empty_spinner("district");
                }
                /*String district_uuid = "";

                if (district_count == 0) {
                    if (value && parent.getSelectedItemPosition() > 0) {
                        for (Map.Entry<String, String> entry : hashMap2.entrySet()) {
                            String list = entry.getValue();
                            // Do things with the list
                            if (list.equalsIgnoreCase(parent.getItemAtPosition(position).toString())) {
                                district_uuid = entry.getKey();
                            }
                        }
                        value = getLocationFromServer_District(base_url, district_uuid, "district");
                        district_count = parent.getSelectedItemPosition();
                    }
                } else if (district_count == parent.getSelectedItemPosition()) {
                    if (value && parent.getSelectedItemPosition() > 0) {
                        for (Map.Entry<String, String> entry : hashMap2.entrySet()) {
                            String list = entry.getValue();
                            // Do things with the list
                            if (list.equalsIgnoreCase(parent.getItemAtPosition(position).toString())) {
                                district_uuid = entry.getKey();
                            }
                        }
                        value = getLocationFromServer_District(base_url, district_uuid, "district");
                    }
                } else {
//                    Toast.makeText(context, "Enter Url", Toast.LENGTH_SHORT).show();
//                    mUrlField.getText().clear();
                    empty_spinner("district");
                    if (value && parent.getSelectedItemPosition() > 0) {
                        for (Map.Entry<String, String> entry : hashMap2.entrySet()) {
                            String list = entry.getValue();
                            // Do things with the list
                            if (list.equalsIgnoreCase(parent.getItemAtPosition(position).toString())) {
                                district_uuid = entry.getKey();
                            }
                        }
                        value = getLocationFromServer_District(base_url, district_uuid, "district");
                    }
                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //this will give Villages...
        spinner_sanch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //sanch wise locations...

                if (position != 0) {
                    String sanch_uuid = "";
                    selectedSanch = spinner_sanch.getSelectedItem().toString();
                    List<String> village_locations = newLocationDao.getVillageList(selectedState, selectedDistrict, selectedSanch, context);
                    if (village_locations.size() > 1) {
                        LocationArrayAdapter locationArrayAdapter =
                                new LocationArrayAdapter(SetupActivity.this, village_locations);

                        spinner_village.setEnabled(true);
                        spinner_village.setAlpha(1);
                        spinner_village.setAdapter(locationArrayAdapter);
                        isLocationFetched = true;
                    } else {
                        empty_spinner("sanch");
                    }
                } else {
                    empty_spinner("sanch");
                }

                /*String sanch_uuid = "";

                if (sanch_count == 0) {
                    if (value && parent.getSelectedItemPosition() > 0) {
                        for (Map.Entry<String, String> entry : hashMap3.entrySet()) {
                            String list = entry.getValue();
                            // Do things with the list
                            if (list.equalsIgnoreCase(parent.getItemAtPosition(position).toString())) {
                                sanch_uuid = entry.getKey();
                            }
                        }
                        value = getLocationFromServer_District(base_url, sanch_uuid, "sanch");
                        sanch_count = parent.getSelectedItemPosition();
                    }
                } else if (sanch_count == parent.getSelectedItemPosition()) {
                    if (value && parent.getSelectedItemPosition() > 0) {
                        for (Map.Entry<String, String> entry : hashMap3.entrySet()) {
                            String list = entry.getValue();
                            // Do things with the list
                            if (list.equalsIgnoreCase(parent.getItemAtPosition(position).toString())) {
                                sanch_uuid = entry.getKey();
                            }
                        }
                        value = getLocationFromServer_District(base_url, sanch_uuid, "sanch");
                    }
                } else {
//                    Toast.makeText(context, "Enter Url", Toast.LENGTH_SHORT).show();
//                    mUrlField.getText().clear();
                    empty_spinner("sanch");
                    if (value && parent.getSelectedItemPosition() > 0) {
                        for (Map.Entry<String, String> entry : hashMap3.entrySet()) {
                            String list = entry.getValue();
                            // Do things with the list
                            if (list.equalsIgnoreCase(parent.getItemAtPosition(position).toString())) {
                                sanch_uuid = entry.getKey();
                            }
                        }
                        value = getLocationFromServer_District(base_url, sanch_uuid, "sanch");
                    }

                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //to fetch village and pass as locations to location-api
        spinner_village.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //village wise locations...
                try {
                    if (position != 0) {
                        String village_uuid = "";
                        selectedVillage = spinner_village.getSelectedItem().toString();
                        village_uuid = newLocationDao.getVillageUuid(selectedState, selectedDistrict, selectedSanch, selectedVillage);
                        hashMap4 = new HashMap<>();
                        hashMap4.put(village_uuid, selectedVillage);
                        for (Map.Entry<String, String> entry : hashMap4.entrySet()) {
                            village_name = entry;
                        }
                    }
                } catch (Exception E) {
                    E.printStackTrace();
                }

               /* if (village_count == 0) {
                    if (value && parent.getSelectedItemPosition() > 0) {
                        for (Map.Entry<String, String> entry : hashMap4.entrySet()) {
                            String list = entry.getValue();
                            // Do things with the list
                            if (list.equalsIgnoreCase(parent.getItemAtPosition(position).toString())) {
                                //send value to the login api...
                                village_name = entry;
                            }
                        }
                        // value = getLocationFromServer_District(base_url, village_name, "sanch");
                        village_count = parent.getSelectedItemPosition();
                    }
                } else if (village_count == parent.getSelectedItemPosition()) {
                    if (value && parent.getSelectedItemPosition() > 0) {
                        for (Map.Entry<String, String> entry : hashMap4.entrySet()) {
                            String list = entry.getValue();
                            // Do things with the list
                            if (list.equalsIgnoreCase(parent.getItemAtPosition(position).toString())) {
                                //send value to the login api...
                                village_name = entry;
                            }
                        }
                        // value = getLocationFromServer_District(base_url, village_name, "sanch");
                    }
                } else {
//                    Toast.makeText(context, "Enter Url", Toast.LENGTH_SHORT).show();
//                    mUrlField.getText().clear();
                    empty_spinner("village");
                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        showProgressbar();
    }

    private boolean areFieldsValid() {
        AtomicBoolean validations = new AtomicBoolean(true);

        // Validation for distance_to_sub_centre_radio_group
        if (subCentreRadioGroup.getCheckedRadioButtonId() == -1) {
            validations.set(false);
            return validations.get();
        }

        // Validation for distance_to_nearest_primary_centre_radio_group
        if (primaryCentreRadioGroup.getCheckedRadioButtonId() == -1) {
            validations.set(false);
            return validations.get();
        }

        // Validation for distance_to_nearest_community_health_centre_radio_group
        if (communityHealthCentreRadioGroup.getCheckedRadioButtonId() == -1) {
            validations.set(false);
            return validations.get();
        }

        // Validation for distance_to_nearest_district_hospital_radio_group
        if (districtHospitalRadioGroup.getCheckedRadioButtonId() == -1) {
            validations.set(false);
            return validations.get();
        }
        // Validation for distance_to_nearest_medical_store_radio_group
        if (medicalStoreRadioGroup.getCheckedRadioButtonId() == -1) {
            validations.set(false);
            return validations.get();
        }

        // Validation for distance_to_nearest_pathological_lab_radio_group
        if (pathologicalLabRadioGroup.getCheckedRadioButtonId() == -1) {
            validations.set(false);
            return validations.get();
        }

        // Validation for distance_to_nearest_private_clinic_with_mbbs_doctor_radio_group
        if (privateClinicWithMbbsDoctorRadioGroup.getCheckedRadioButtonId() == -1) {
            validations.set(false);
            return validations.get();
        }

        // Validation for distance_to_nearest_private_clinic_with_alternate_medical_radio_group
        if (privateClinicWithAlternateMedicalRadioGroup.getCheckedRadioButtonId() == -1) {
            validations.set(false);
            return validations.get();
        }

        // Validation for jal_jeevan_yojana_scheme_radio_group
        if (jalJeevanYojanaSchemeRadioGroup.getCheckedRadioButtonId() == -1) {
            validations.set(false);
            return validations.get();
        }

        return validations.get();
    }

    private void empty_spinner(String value) {

        if (value.equalsIgnoreCase("state")) {
            List<String> list_district = new ArrayList<>();
            list_district.add(getResources().getString(R.string.setup_select_district_str));
            spinner_district.setEnabled(false);
            spinner_district.setAlpha(0.4F);
            LocationArrayAdapter adapter_district = new LocationArrayAdapter(SetupActivity.this, list_district);
            spinner_district.setAdapter(adapter_district);

            List<String> list_sanch = new ArrayList<>();
            list_sanch.add(getResources().getString(R.string.setup_select_sanch_str));
            spinner_sanch.setEnabled(false);
            spinner_sanch.setAlpha(0.4F);
            LocationArrayAdapter adapter_sanch = new LocationArrayAdapter(SetupActivity.this, list_sanch);
            spinner_sanch.setAdapter(adapter_sanch);

            List<String> list_village = new ArrayList<>();
            list_village.add(getResources().getString(R.string.setup_select_village_str));
            spinner_village.setEnabled(false);
            spinner_village.setAlpha(0.4F);
            LocationArrayAdapter adapter_village = new LocationArrayAdapter(SetupActivity.this, list_village);
            spinner_village.setAdapter(adapter_village);
        } else if (value.equalsIgnoreCase("district")) {
            List<String> list_sanch = new ArrayList<>();
            list_sanch.add(getResources().getString(R.string.setup_select_sanch_str));
            spinner_sanch.setEnabled(false);
            spinner_sanch.setAlpha(0.4F);
            LocationArrayAdapter adapter_sanch = new LocationArrayAdapter(SetupActivity.this, list_sanch);
            spinner_sanch.setAdapter(adapter_sanch);

            List<String> list_village = new ArrayList<>();
            list_village.add(getResources().getString(R.string.setup_select_village_str));
            spinner_village.setEnabled(false);
            spinner_village.setAlpha(0.4F);
            LocationArrayAdapter adapter_village = new LocationArrayAdapter(SetupActivity.this, list_village);
            spinner_village.setAdapter(adapter_village);
        } else if (value.equalsIgnoreCase("sanch")) {
            List<String> list_village = new ArrayList<>();
            list_village.add(getResources().getString(R.string.setup_select_village_str));
            spinner_village.setEnabled(false);
            spinner_village.setAlpha(0.4F);
            LocationArrayAdapter adapter_village = new LocationArrayAdapter(SetupActivity.this, list_village);
            spinner_village.setAdapter(adapter_village);
        } else if (value.equalsIgnoreCase("village")) {
            //do nothing
        } else {
            List<String> list_state = new ArrayList<>();
            list_state.add(getResources().getString(R.string.setup_select_state_str));
            spinner_state.setEnabled(false);
            spinner_state.setAlpha(0.4F);
            LocationArrayAdapter adapter_state = new LocationArrayAdapter(SetupActivity.this, list_state);
            spinner_state.setAdapter(adapter_state);

            List<String> list_district = new ArrayList<>();
            list_district.add(getResources().getString(R.string.setup_select_district_str));
            spinner_district.setEnabled(false);
            spinner_district.setAlpha(0.4F);
            LocationArrayAdapter adapter_district = new LocationArrayAdapter(SetupActivity.this, list_district);
            spinner_district.setAdapter(adapter_district);

            List<String> list_sanch = new ArrayList<>();
            list_sanch.add(getResources().getString(R.string.setup_select_sanch_str));
            spinner_sanch.setEnabled(false);
            spinner_sanch.setAlpha(0.4F);
            LocationArrayAdapter adapter_sanch = new LocationArrayAdapter(SetupActivity.this, list_sanch);
            spinner_sanch.setAdapter(adapter_sanch);

            List<String> list_village = new ArrayList<>();
            list_village.add(getResources().getString(R.string.setup_select_village_str));
            spinner_village.setEnabled(false);
            spinner_village.setAlpha(0.4F);
            LocationArrayAdapter adapter_village = new LocationArrayAdapter(SetupActivity.this, list_village);
            spinner_village.setAdapter(adapter_village);
        }
    }

    /**
     * Check username and password validations.
     * Get user selected location.
     */
    private void attemptLogin() {

        // Reset errors.
        mUrlField.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mAdminPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String url = mUrlField.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String admin_password = mAdminPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(url)) {
            focusView = mUrlField;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (!TextUtils.isEmpty(admin_password) && !isPasswordValid(admin_password)) {
            mAdminPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mAdminPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
        }

        //spinner...
        if (spinner_state.getSelectedItemPosition() <= 0) {
            cancel = true;
            focusView = spinner_state;
            TextView t = (TextView) spinner_state.getSelectedView();
            t.setError(getResources().getString(R.string.setup_select_state_str));
            t.setTextColor(Color.RED);
            Toast.makeText(SetupActivity.this, getResources().getString(R.string.setup_select_dropdown_state_msg), Toast.LENGTH_LONG).show();
        } else if (spinner_district.getSelectedItemPosition() <= 0) {
            cancel = true;
            focusView = spinner_district;
            TextView t = (TextView) spinner_district.getSelectedView();
            t.setError(getResources().getString(R.string.setup_select_district_str));
            focusView.setEnabled(true);
            t.setTextColor(Color.RED);
            Toast.makeText(SetupActivity.this, getResources().getString(R.string.setup_select_dropdown_district_msg), Toast.LENGTH_LONG).show();
        } else if (spinner_sanch.getSelectedItemPosition() <= 0) {
            cancel = true;
            focusView = spinner_sanch;
            TextView t = (TextView) spinner_sanch.getSelectedView();
            t.setError(getResources().getString(R.string.setup_select_sanch_str));
            t.setTextColor(Color.RED);
            Toast.makeText(SetupActivity.this, getResources().getString(R.string.setup_select_dropdown_sanch_msg), Toast.LENGTH_LONG).show();
        } else if (spinner_village.getSelectedItemPosition() <= 0) {
            cancel = true;
            focusView = spinner_village;
            TextView t = (TextView) spinner_village.getSelectedView();
            t.setError(getResources().getString(R.string.setup_select_village_str));
            t.setTextColor(Color.RED);
            Toast.makeText(SetupActivity.this, getResources().getString(R.string.setup_select_dropdown_village_msg), Toast.LENGTH_LONG).show();
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            if (focusView != null) {
                if (TextUtils.isEmpty(url)) {
                    mUrlField.requestFocus();
                    mUrlField.setError(getResources().getString(R.string.enter_url_str));
                }
                focusView.requestFocus();
            }
        } else {

            /*if (spinner_village.getSelectedItemPosition() != 0) {
                String urlString = mUrlField.getText().toString();
                mLoginButton.setText(getString(R.string.please_wait_progress));
                mLoginButton.setEnabled(false);
                TestSetup(urlString, email, password, admin_password, spinner_village.getSelectedItem().toString());
                Log.d(TAG, "attempting setup");
            }*/

            if (village_name != null) {
                String urlString = mUrlField.getText().toString();
                mLoginButton.setText(getString(R.string.please_wait_progress));
                mLoginButton.setEnabled(false);
                TestSetup(urlString, email, password, admin_password, village_name);
                Log.d(TAG, "attempting setup");
            }
        }
    }

    private void showProgressbar() {
        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(SetupActivity.this);
        mProgressDialog.setMessage(getString(R.string.download_protocols));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private boolean getLocationFromServer_District(String url, String state_uuid, String location_wise) {
        customProgressDialog.show();
        value = false;
        String encoded = "";

        ApiClient.changeApiBaseUrl(url, context);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);

        encoded = base64Utils.encoded("sysnurse", "IHNurse#1");

        try {
            Observable<District_Sanch_Village> district_sanch_villageObservable =
                    apiService.DISTRICT_SANCH_VILLAGE_OBSERVABLE(state_uuid, "Basic " + encoded);
            district_sanch_villageObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<District_Sanch_Village>() {
                        @Override
                        public void onNext(@NonNull District_Sanch_Village district_sanch_village) {
                            if (!district_sanch_village.getChildLocations().isEmpty()) {


                                if (location_wise.equalsIgnoreCase("state")) {
                                    customProgressDialog.dismiss();
                                    List<String> district_locations = getLocation_district(district_sanch_village.getChildLocations(), "state");
                                    LocationArrayAdapter locationArrayAdapter =
                                            new LocationArrayAdapter(SetupActivity.this, district_locations);

                                    spinner_district.setEnabled(true);
                                    spinner_district.setAlpha(1);
                                    spinner_district.setAdapter(locationArrayAdapter);
                                    isLocationFetched = true;

                                    if (hashMap2 != null) {
                                        hashMap2.clear();
                                    } //to clear the previous data...
                                    else {
                                        hashMap2 = new HashMap<>();
                                    }

                                    for (int i = 0; i < district_sanch_village.getChildLocations().size(); i++) {
                                        hashMap2.put(district_sanch_village.getChildLocations().get(i).getUuid(),
                                                district_sanch_village.getChildLocations().get(i).getDisplay());
                                    }

                                } else if (location_wise.equalsIgnoreCase("district")) {
                                    customProgressDialog.dismiss();
                                    List<String> district_locations = getLocation_district(district_sanch_village.getChildLocations(), "district");
                                    LocationArrayAdapter locationArrayAdapter =
                                            new LocationArrayAdapter(SetupActivity.this, district_locations);

                                    spinner_sanch.setEnabled(true);
                                    spinner_sanch.setAlpha(1);
                                    spinner_sanch.setAdapter(locationArrayAdapter);
                                    isLocationFetched = true;

                                    if (hashMap3 != null) {
                                        hashMap3.clear();
                                    } //to clear the previous data...
                                    else {
                                        hashMap3 = new HashMap<>();
                                    }

                                    for (int i = 0; i < district_sanch_village.getChildLocations().size(); i++) {
                                        hashMap3.put(district_sanch_village.getChildLocations().get(i).getUuid(),
                                                district_sanch_village.getChildLocations().get(i).getDisplay());
                                    }
                                } else if (location_wise.equalsIgnoreCase("sanch")) {
                                    customProgressDialog.dismiss();
                                    List<String> district_locations = getLocation_district(district_sanch_village.getChildLocations(), "sanch");
                                    LocationArrayAdapter locationArrayAdapter =
                                            new LocationArrayAdapter(SetupActivity.this, district_locations);

                                    spinner_village.setEnabled(true);
                                    spinner_village.setAlpha(1);
                                    spinner_village.setAdapter(locationArrayAdapter);
                                    isLocationFetched = true;

                                    if (hashMap4 != null) {
                                        hashMap4.clear();
                                    } //to clear the previous data...
                                    else {
                                        hashMap4 = new HashMap<>();
                                    }

                                    for (int i = 0; i < district_sanch_village.getChildLocations().size(); i++) {
                                        hashMap4.put(district_sanch_village.getChildLocations().get(i).getUuid(),
                                                district_sanch_village.getChildLocations().get(i).getDisplay());
                                    }
                                }

                                value = true;
                            } else {
                                customProgressDialog.dismiss();
                                value = false;
                                isLocationFetched = false;

                                switch (location_wise) {
                                    case "state":
                                        Toast.makeText(SetupActivity.this, "No District found", Toast.LENGTH_SHORT).show();
                                        state_count = 0;
                                        spinner_district.setEnabled(false);
                                        spinner_district.setAlpha(0.4F);
                                        spinner_sanch.setEnabled(false);
                                        spinner_sanch.setAlpha(0.4F);
                                        spinner_village.setEnabled(false);
                                        spinner_village.setAlpha(0.4F);
                                        break;
                                    case "district":
                                        Toast.makeText(SetupActivity.this, "No Sanch found", Toast.LENGTH_SHORT).show();
                                        district_count = 0;
                                        spinner_sanch.setEnabled(false);
                                        spinner_sanch.setAlpha(0.4F);
                                        spinner_village.setEnabled(false);
                                        spinner_village.setAlpha(0.4F);
                                        break;
                                    case "sanch":
                                        Toast.makeText(SetupActivity.this, "No Village found", Toast.LENGTH_SHORT).show();
                                        sanch_count = 0;
                                        spinner_village.setEnabled(false);
                                        spinner_village.setAlpha(0.4F);
                                        break;
                                }
                                //Toast.makeText(SetupActivity.this, "Unable to fetch State", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            value = false;
                            if (e.getLocalizedMessage().contains("Unable to resolve host")) {
                                Toast.makeText(SetupActivity.this, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SetupActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                            customProgressDialog.dismiss();
                        }

                        @Override
                        public void onComplete() {
                            value = true;
                            customProgressDialog.dismiss();
                        }
                    });
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            mUrlField.setError(getString(R.string.url_invalid));
            customProgressDialog.dismiss();
        }

        return value;
    }


    /**
     * Parse locations fetched through api and provide the appropriate dropdown.
     *
     * @param url string of url.
     */
    private boolean getLocationFromServer(String url) {
        customProgressDialog.show();
        ApiClient.changeApiBaseUrl(url, context);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);

        try {
            Observable<Setup_LocationModel> locationObservable = apiService.SETUP_LOCATIONOBSERVABLE();
            locationObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<Setup_LocationModel>() {
                        @Override
                        public void onNext(@NonNull Setup_LocationModel location) {
                            if (location.getStates() != null) {
                                try {
                                    newLocationDao = new NewLocationDao();
                                    newLocationDao.insertSetupLocations(location);
                                    customProgressDialog.dismiss();

                                    List<String> state_locations = newLocationDao.getStateList(context);

                                    if (state_locations.size() != 0) {
                                        LocationArrayAdapter locationArrayAdapter =
                                                new LocationArrayAdapter(SetupActivity.this, state_locations);

                                        spinner_state.setEnabled(true);
                                        spinner_state.setAlpha(1);
                                        spinner_state.setAdapter(locationArrayAdapter);
                                        isLocationFetched = true;
                                    } else {
                                        empty_spinner("state");
                                    }

                                    /*hashMap1 = new HashMap<>();
                                    for (int i = 0; i < state.getResults().size(); i++) {
                                        hashMap1.put(state.getResults().get(i).getUuid(),
                                                state.getResults().get(i).getDisplay());
                                    }*/
                                    value = true;
                                } catch (DAOException e) {
                                    e.printStackTrace();
                                    customProgressDialog.dismiss();
                                    value = false;
                                }
                            } else {
                                customProgressDialog.dismiss();
                                value = false;
                                isLocationFetched = false;
                                Toast.makeText(SetupActivity.this, "Unable to fetch State", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            value = false;
                            if (e.getLocalizedMessage().contains("Unable to resolve host")) {
                                Toast.makeText(SetupActivity.this, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SetupActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                            customProgressDialog.dismiss();
                        }

                        @Override
                        public void onComplete() {
                            value = true;
                            customProgressDialog.dismiss();
                        }
                    });
            /*Observable<State> stateObservable = apiService.STATE_OBSERVABLE();
            stateObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<State>() {
                        @Override
                        public void onNext(@NonNull State state) {
                            if (state.getResults() != null) {
                                customProgressDialog.dismiss();
                                List<String> state_locations = getLocation(state.getResults());
                                LocationArrayAdapter locationArrayAdapter =
                                        new LocationArrayAdapter(SetupActivity.this, state_locations);

                                spinner_state.setEnabled(true);
                                spinner_state.setAlpha(1);
                                spinner_state.setAdapter(locationArrayAdapter);
                                isLocationFetched = true;

                                hashMap1 = new HashMap<>();
                                for (int i = 0; i < state.getResults().size(); i++) {
                                    hashMap1.put(state.getResults().get(i).getUuid(),
                                            state.getResults().get(i).getDisplay());
                                }

                                value = true;
                            } else {
                                customProgressDialog.dismiss();
                                value = false;
                                isLocationFetched = false;
                                Toast.makeText(SetupActivity.this, "Unable to fetch State", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            value = false;
                            Toast.makeText(SetupActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            customProgressDialog.dismiss();
                        }

                        @Override
                        public void onComplete() {
                            value = true;
                            customProgressDialog.dismiss();
                        }
                    });*/
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            mUrlField.setError(getString(R.string.url_invalid));
            customProgressDialog.dismiss();
        }

        return value;
    }


    /**
     * Returns list of locations.
     *
     * @param locationList a list of type {@link Location}.
     * @return list of type string.
     * @see Location
     */
    private List<String> getLocationStringList(List<Location> locationList) {
        List<String> list = new ArrayList<String>();
        list.add(getString(R.string.login_location_select));
        for (int i = 0; i < locationList.size(); i++) {
            list.add(locationList.get(i).getDisplay());
        }
        return list;
    }

    private List<String> getLocation(List<Result> resultList) {
        List<String> list = new ArrayList<>();
        list.add(getResources().getString(R.string.setup_select_state_str));
        for (int i = 0; i < resultList.size(); i++) {
            list.add(resultList.get(i).getDisplay());
        }

        return list;
    }

    private List<String> getLocation_district(List<ChildLocation> childLocationList, String location_wise) {
        List<String> list = new ArrayList<>();

        if (location_wise.equalsIgnoreCase("state")) {
            list.add(getResources().getString(R.string.setup_select_district_str));
        } else if (location_wise.equalsIgnoreCase("district")) {
            list.add(getResources().getString(R.string.setup_select_sanch_str));
        } else if (location_wise.equalsIgnoreCase("sanch")) {
            list.add(getResources().getString(R.string.setup_select_village_str));
        }

        for (int i = 0; i < childLocationList.size(); i++) {
            list.add(childLocationList.get(i).getDisplay());
        }

        return list;
    }

    public void onRadioClick(View v) {

        boolean checked = ((RadioButton) v).isChecked();
        switch (v.getId()) {
            case R.id.demoMindmap:
                if (checked) {
                    r2.setChecked(false);
                }
                break;

            case R.id.downloadMindmap:
                if (NetworkConnection.isOnline(this)) {
                    if (checked) {
                        r1.setChecked(false);
                        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
                        // AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
                        LayoutInflater li = LayoutInflater.from(this);
                        View promptsView = li.inflate(R.layout.dialog_mindmap_cred, null);


                        dialog.setTitle(getString(R.string.enter_license_key))
                                .setView(promptsView)

                                .setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Dialog d = (Dialog) dialog;

                                        EditText text = d.findViewById(R.id.licensekey);
                                        EditText url = d.findViewById(R.id.licenseurl);
                                        if (text.getText().toString().isEmpty() && text.getText() == null || url.getText().toString().isEmpty() && url.getText() == null) {
                                            text.setFocusable(true);
                                            text.setError(getResources().getString(R.string.enter_license_key));
                                        }

                                        if (sessionManager.getLicenseKey() != null && sessionManager.getLicenseKey().equalsIgnoreCase("https://mindmaps.intelehealth.io:4040")) {
                                            text.setText(sessionManager.getLicenseKey());
                                            url.setText(sessionManager.getMindMapServerUrl());
                                        }


                                        if (!url.getText().toString().trim().isEmpty()) {
                                            if (Patterns.WEB_URL.matcher(url.getText().toString().trim()).matches()) {
                                                String url_field = "https://" + url.getText().toString() + ":3004/";
                                                if (URLUtil.isValidUrl(url_field)) {
                                                    key = text.getText().toString().trim();
                                                    licenseUrl = url.getText().toString().trim();

                                                    if (licenseUrl.isEmpty()) {
                                                        url.setError(getResources().getString(R.string.enter_server_url));
                                                        url.requestFocus();
                                                        return;
                                                    }
                                                    if (licenseUrl.contains(":")) {
                                                        url.setError(getResources().getString(R.string.invalid_url));
                                                        url.requestFocus();
                                                        return;
                                                    }
                                                    if (key.isEmpty()) {
                                                        text.setError(getResources().getString(R.string.enter_license_key));
                                                        text.requestFocus();
                                                        return;
                                                    }

                                                    sessionManager.setMindMapServerUrl(licenseUrl);
                                                    //Toast.makeText(SetupActivity.this, "" + key, Toast.LENGTH_SHORT).show();
                                                    if (keyVerified(key)) {
                                                        // create a shared pref to store the key

                                                        // SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("pref",MODE_PRIVATE);

                                                        //DOWNLOAD MIND MAP FILE LIST
                                                        //upnew getJSONFile().execute(null, "AllFiles", "TRUE");

                                                        // UpdateProtocolsTask updateProtocolsTask = new UpdateProtocolsTask(SetupActivity.this);
                                                        // updateProtocolsTask.execute(null, "AllFiles", "TRUE");
//                                        DownloadProtocolsTask downloadProtocolsTask = new DownloadProtocolsTask(SetupActivity.this);
//                                        downloadProtocolsTask.execute(key);
                                                        getMindmapDownloadURL("https://" + licenseUrl + ":3004/");

                                                    }
                                                } else {
                                                    Toast.makeText(SetupActivity.this, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();

                                                }
                                            } else {
                                                //invalid url || invalid url and key.
                                                Toast.makeText(SetupActivity.this, R.string.enter_valid_license_url, Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(SetupActivity.this, R.string.please_enter_url_and_key, Toast.LENGTH_SHORT).show();
                                            r1.setChecked(true);
                                            r2.setChecked(false);
                                        }

                                    }
                                })

                                .setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        r2.setChecked(false);
                                        r1.setChecked(true);

                                    }
                                });
                        AlertDialog alertDialog = dialog.create();
                        alertDialog.setView(promptsView, 20, 0, 20, 0);
                        alertDialog.show();
                        // Get the alert dialog buttons reference
                        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                        // Change the alert dialog buttons text and background color
                        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                        // positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                        //negativeButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);


                    }
                } else {
                    ((RadioButton) v).setChecked(false);
                    Toast.makeText(context, getString(R.string.mindmap_internect_connection), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private boolean keyVerified(String key) {
        //TODO: Verify License Key
        return true;
    }

    /**
     * Attempts login to the OpenMRS server.
     * If successful cretes a new {@link Account}
     * If unsuccessful details are saved in SharedPreferences.
     */
    public void TestSetup(String CLEAN_URL, String USERNAME, String PASSWORD, String ADMIN_PASSWORD, Map.Entry<String, String> location) {

        ProgressDialog progress;
        progress = new ProgressDialog(SetupActivity.this, R.style.AlertDialogStyle);
        ;//SetupActivity.this);
        progress.setTitle(getString(R.string.please_wait_progress));
        progress.setMessage(getString(R.string.logging_in));
        progress.show();

        String urlString = urlModifiers.loginUrl(CLEAN_URL);
        Logger.logD(TAG, "usernaem and password" + USERNAME + PASSWORD);
        encoded = base64Utils.encoded(USERNAME, PASSWORD);
        sessionManager.setEncoded(encoded);
        getDataFromRadioButtons();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Observable<LoginModel> loginModelObservable = AppConstants.apiInterface.LOGIN_MODEL_OBSERVABLE(urlString, "Basic " + encoded);
        loginModelObservable.subscribe(new Observer<LoginModel>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(LoginModel loginModel) {
                Boolean authencated = loginModel.getAuthenticated();
                Gson gson = new Gson();
                sessionManager.setChwname(loginModel.getUser().getDisplay());
                sessionManager.setCreatorID(loginModel.getUser().getUuid());
                sessionManager.setSessionID(loginModel.getSessionId());
                sessionManager.setProviderID(loginModel.getUser().getPerson().getUuid());
                sessionManager.setHwID(loginModel.getUser().getPerson().getUuid());
                UrlModifiers urlModifiers = new UrlModifiers();
                String url = urlModifiers.loginUrlProvider(CLEAN_URL, loginModel.getUser().getUuid());

                if (authencated) {
//
//                    /**
//                     * Here, the entered login creds is Authenticated so we will now call JWT authentication api
//                     * by passing the login creds entered during setup screen of HW and rememberMe: True status.
//                     */
//                    // Start
//                    ApiCallUtils.auth_login_jwt_token(context, CLEAN_URL, USERNAME, PASSWORD);
//                    // End

                    Observable<LoginProviderModel> loginProviderModelObservable = AppConstants.apiInterface.LOGIN_PROVIDER_MODEL_OBSERVABLE(url, "Basic " + encoded);
                    loginProviderModelObservable
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new DisposableObserver<LoginProviderModel>() {
                                @Override
                                public void onNext(LoginProviderModel loginProviderModel) {
                                    if (loginProviderModel.getResults().size() != 0) {
                                        for (int i = 0; i < loginProviderModel.getResults().size(); i++) {
                                            Log.i(TAG, "doInBackground: " + loginProviderModel.getResults().get(i).getUuid());
                                            sessionManager.setProviderID(loginProviderModel.getResults().get(i).getUuid());
//                                                responsecode = 200;
                                          /*  final Account account = new Account(USERNAME, "io.intelehealth.openmrs");
                                            manager.addAccountExplicitly(account, PASSWORD, null);*/

                                            sessionManager.setLocationName(location.getValue());
                                            sessionManager.setLocationUuid(location.getKey());
                                            //sessionManager.setLocationDescription(location.getDescription());
                                            sessionManager.setServerUrl(CLEAN_URL);
                                            sessionManager.setServerUrlRest(BASE_URL);
                                            sessionManager.setServerUrlBase("https://" + CLEAN_URL + "/openmrs");
                                            sessionManager.setBaseUrl(BASE_URL);
                                            sessionManager.setSetupComplete(true);

                                            //Storing State Name
                                            sessionManager.setCountryName("India");
                                            sessionManager.setStateName(selectedState);
                                            sessionManager.setDistrictName(selectedDistrict);
                                            sessionManager.setSanchName(selectedSanch);
                                            sessionManager.setVillageName(selectedVillage);

                                            // OfflineLogin.getOfflineLogin().setUpOfflineLogin(USERNAME, PASSWORD);
                                            AdminPassword.getAdminPassword().setUp(ADMIN_PASSWORD);

                                            Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                                                    .applicationId(AppConstants.IMAGE_APP_ID)
                                                    .server("https://" + CLEAN_URL + ":1337/parse/")
                                                    .build()
                                            );

                                            SQLiteDatabase sqLiteDatabase = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
                                            //SQLiteDatabase read_db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();

                                            sqLiteDatabase.beginTransaction();
                                            //read_db.beginTransaction();
                                            ContentValues values = new ContentValues();

                                            //StringEncryption stringEncryption = new StringEncryption();
                                            String random_salt = getSalt_DATA();

                                            //String random_salt = stringEncryption.getRandomSaltString();
                                            Log.d("salt", "salt: " + random_salt);
                                            //Salt_Getter_Setter salt_getter_setter = new Salt_Getter_Setter();
                                            //salt_getter_setter.setSalt(random`_salt);


                                            String hash_password = null;
                                            try {
                                                //hash_email = StringEncryption.convertToSHA256(random_salt + mEmail);
                                                hash_password = StringEncryption.convertToSHA256(random_salt + PASSWORD);
                                            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                                                FirebaseCrashlytics.getInstance().recordException(e);
                                            }

                                            try {
                                                values.put("username", USERNAME);
                                                values.put("password", hash_password);
                                                values.put("creator_uuid_cred", loginModel.getUser().getUuid());
                                                values.put("chwname", loginModel.getUser().getDisplay());
                                                values.put("provider_uuid_cred", sessionManager.getProviderID());
                                                createdRecordsCount = sqLiteDatabase.insertWithOnConflict("tbl_user_credentials", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                                                sqLiteDatabase.setTransactionSuccessful();

                                                Logger.logD("values", "values" + values);
                                                Logger.logD("created user credentials", "create user records" + createdRecordsCount);
                                            } catch (SQLException e) {
                                                Log.d("SQL", "SQL user credentials: " + e);
                                            } finally {
                                                sqLiteDatabase.endTransaction();
                                            }

                                            Log.i(TAG, "onPostExecute: Parse init");
                                            Intent intent = new Intent(SetupActivity.this, HomeActivity.class);
                                            intent.putExtra("setup", true);
                                            if (r2.isChecked()) { // License protocol chosen
                                                if (!sessionManager.getLicenseKey().isEmpty()) {
                                                    sessionManager.setTriggerNoti("no");
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(SetupActivity.this, R.string.please_enter_valid_license_key, Toast.LENGTH_LONG).show();
                                                }
                                            } else { // demo protocol chosen
                                                sessionManager.setTriggerNoti("no");
                                                startActivity(intent);
                                                finish();
                                            }
                                            progress.dismiss();
                                        }
                                    }

                                }

                                @Override
                                public void onError(Throwable e) {
                                    Logger.logD(TAG, "handle provider error" + e.getMessage());
                                    progress.dismiss();
                                }

                                @Override
                                public void onComplete() {

                                }
                            });
                }
            }

            @Override
            public void onError(Throwable e) {
                Logger.logD(TAG, "Login Failure" + e.getMessage());
                progress.dismiss();
                DialogUtils dialogUtils = new DialogUtils();
                dialogUtils.showerrorDialog(SetupActivity.this, getResources().getString(R.string.error_login_str), getString(R.string.error_incorrect_password), "ok");
                mEmailView.requestFocus();
                mPasswordView.requestFocus();
                mLoginButton.setText(getString(R.string.action_sign_in));
                mLoginButton.setEnabled(true);
            }

            @Override
            public void onComplete() {
                Logger.logD(TAG, "completed");
            }
        });


    }

    public String getSalt_DATA() {
        BufferedReader reader = null;
        String salt = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("salt.env")));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                //process line
                salt = mLine;
                Log.d("SA", "SA " + salt);
            }
        } catch (Exception e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    //log the exception
                }
            }
        }
        return salt;

    }

    private void getMindmapDownloadURL(String url) {
        customProgressDialog.show();
        ApiClient.changeApiBaseUrl(url, context);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        try {
         //   Observable<DownloadMindMapRes> resultsObservable = apiService.DOWNLOAD_MIND_MAP_RES_OBSERVABLE(key, sessionManager.getJwtAuthToken());
            Observable<DownloadMindMapRes> resultsObservable = apiService.DOWNLOAD_MIND_MAP_RES_OBSERVABLE(key);
            resultsObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<DownloadMindMapRes>() {
                        @Override
                        public void onNext(DownloadMindMapRes res) {
                            customProgressDialog.dismiss();
                            if (res.getMessage() != null && res.getMessage().equalsIgnoreCase("Success")) {

                                Log.e("MindMapURL", "Successfully get MindMap URL");
                                mTask = new DownloadMindMaps(context, mProgressDialog);
                                mindmapURL = res.getMindmap().trim();
                                sessionManager.setLicenseKey(key);
                                checkExistingMindMaps();

                            } else {
//                                Toast.makeText(SetupActivity.this, res.getMessage(), Toast.LENGTH_LONG).show();
                                Toast.makeText(SetupActivity.this, getResources().getString(R.string.no_protocols_found), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            customProgressDialog.dismiss();
                            Log.e("MindMapURL", " " + e);
                            if (e.getLocalizedMessage().contains("Unable to resolve host")) {
                                Toast.makeText(SetupActivity.this, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SetupActivity.this, getResources().getString(R.string.unable_to_get_proper_response), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "changeApiBaseUrl: " + e.getMessage());
            Log.e(TAG, "changeApiBaseUrl: " + e.getStackTrace());
        }
    }

    private void checkExistingMindMaps() {

        //Check is there any existing mindmaps are present, if yes then delete.

        File engines = new File(context.getFilesDir().getAbsolutePath(), "/Engines");
        Log.e(TAG, "Engines folder=" + engines.exists());
        if (engines.exists()) {
            engines.delete();
        }
        File logo = new File(context.getFilesDir().getAbsolutePath(), "/logo");
        Log.e(TAG, "Logo folder=" + logo.exists());
        if (logo.exists()) {
            logo.delete();
        }
        File physicalExam = new File(context.getFilesDir().getAbsolutePath() + "/physExam.json");
        Log.e(TAG, "physExam.json=" + physicalExam.exists());
        if (physicalExam.exists()) {
            physicalExam.delete();
        }
        File familyHistory = new File(context.getFilesDir().getAbsolutePath() + "/famHist.json");
        Log.e(TAG, "famHist.json=" + familyHistory.exists());
        if (familyHistory.exists()) {
            familyHistory.delete();
        }
        File pastMedicalHistory = new File(context.getFilesDir().getAbsolutePath() + "/patHist.json");
        Log.e(TAG, "patHist.json=" + pastMedicalHistory.exists());
        if (pastMedicalHistory.exists()) {
            pastMedicalHistory.delete();
        }
        File config = new File(context.getFilesDir().getAbsolutePath() + "/config.json");
        Log.e(TAG, "config.json=" + config.exists());
        if (config.exists()) {
            config.delete();
        }

        //Start downloading mindmaps
        mTask.execute(mindmapURL, context.getFilesDir().getAbsolutePath() + "/mindmaps.zip");
        Log.e("DOWNLOAD", "isSTARTED");

    }

    private void getDataFromRadioButtons() {
        Configuration configuration = new Configuration(IntelehealthApplication.getAppContext().getResources().getConfiguration());
        configuration.setLocale(new Locale("en"));
        Context updatedContext = SetupActivity.this.createConfigurationContext(configuration);

        String subCenterDistance = getDistanceStrings(
                ((RadioButton) subCentreRadioGroup.findViewById(subCentreRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                context,
                updatedContext,
                sessionManager.getAppLanguage()
        );

        sessionManager.setSubCentreDistance(subCenterDistance);

        String primaryHealthCenterDistance = getDistanceStrings(
                ((RadioButton) primaryCentreRadioGroup.findViewById(primaryCentreRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                context,
                updatedContext,
                sessionManager.getAppLanguage()
        );

        sessionManager.setPrimaryHealthCentreDistance(primaryHealthCenterDistance);

        String communityHealthCenterDistance = getDistanceStrings(
                ((RadioButton) communityHealthCentreRadioGroup.findViewById(communityHealthCentreRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                context,
                updatedContext,
                sessionManager.getAppLanguage()
        );

        sessionManager.setCommunityHealthCentreDistance(communityHealthCenterDistance);

        String districtHospital = getDistanceStrings(
                ((RadioButton) districtHospitalRadioGroup.findViewById(districtHospitalRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                context,
                updatedContext,
                sessionManager.getAppLanguage()
        );

        sessionManager.setDistrictHospitalDistance(districtHospital);

        String medicalStore = getDistanceStrings(
                ((RadioButton) medicalStoreRadioGroup.findViewById(medicalStoreRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                context,
                updatedContext,
                sessionManager.getAppLanguage()
        );

        sessionManager.setMedicalStoreDistance(medicalStore);

        String pathologicalLab = getDistanceStrings(
                ((RadioButton) pathologicalLabRadioGroup.findViewById(pathologicalLabRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                context,
                updatedContext,
                sessionManager.getAppLanguage()
        );

        sessionManager.setPathologicalLabDistance(pathologicalLab);

        String privateClinicWithMbbsDoctorDistance = getDistanceStrings(
                ((RadioButton) privateClinicWithMbbsDoctorRadioGroup.findViewById(privateClinicWithMbbsDoctorRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                context,
                updatedContext,
                sessionManager.getAppLanguage()
        );

        sessionManager.setPrivateClinicWithMbbsDoctorDistance(privateClinicWithMbbsDoctorDistance);

        String privateClinicWithAlternateDoctorDistance = getDistanceStrings(
                ((RadioButton) privateClinicWithAlternateMedicalRadioGroup.findViewById(privateClinicWithAlternateMedicalRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                context,
                updatedContext,
                sessionManager.getAppLanguage()
        );

        sessionManager.setPrivateClinicWithAlternateDoctorDistance(privateClinicWithAlternateDoctorDistance);

        String jalJeevanYojana = getDistanceStrings(
                ((RadioButton) jalJeevanYojanaSchemeRadioGroup.findViewById(jalJeevanYojanaSchemeRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                context,
                updatedContext,
                sessionManager.getAppLanguage()
        );

        sessionManager.setJalJeevanYojanaScheme(jalJeevanYojana);
    }
}
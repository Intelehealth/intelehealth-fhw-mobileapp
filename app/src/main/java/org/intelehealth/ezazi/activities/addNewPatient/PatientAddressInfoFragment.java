package org.intelehealth.ezazi.activities.addNewPatient;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.database.dao.PatientsDAO;
import org.intelehealth.ezazi.models.dto.PatientAttributesDTO;
import org.intelehealth.ezazi.models.dto.PatientDTO;
import org.intelehealth.ezazi.utilities.FileUtils;
import org.intelehealth.ezazi.utilities.Logger;
import org.intelehealth.ezazi.utilities.NetworkConnection;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.StringUtils;
import org.intelehealth.ezazi.utilities.UuidGenerator;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PatientAddressInfoFragment extends Fragment {
    private static final String TAG = "PatientPersonalInfoFrag";
    View view;
    AutoCompleteTextView autotvCountry, autotvState, autotvCity;
    Context mContext;
    TextInputEditText etAddress1, etAddress2, etPostalCode;
    private PatientOtherInfoFragment fragment_thirdScreen;
    private PatientPersonalInfoFragment firstScreen;
    boolean fromThirdScreen = false, fromFirstScreen = false;
    boolean patient_detail = false;
    SessionManager sessionManager = null;
    private boolean hasLicense = false;
    private String country1, state;
    MaterialButton btnNext, btnBack;
    String city_village;
    String uuid = "";
    PatientDTO patientDTO = new PatientDTO();
    UuidGenerator uuidGenerator = new UuidGenerator();
    Calendar today = Calendar.getInstance();
    Calendar dob = Calendar.getInstance();
    ImageView ivPersonal, ivAddress, ivOther;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_patient_address_info, container, false);
        mContext = getActivity();
        sessionManager = new SessionManager(getActivity());
        initUI();
        return view;
    }

    private void initUI() {
        ivPersonal = getActivity().findViewById(R.id.iv_personal_info);
        ivAddress = getActivity().findViewById(R.id.iv_address_info);
        ivOther = getActivity().findViewById(R.id.iv_other_info);

        autotvCountry = view.findViewById(R.id.autotv_country);
        autotvState = view.findViewById(R.id.autotv_state);

        autotvCity = view.findViewById(R.id.autotv_city);
        etAddress1 = view.findViewById(R.id.et_address1);
        etAddress2 = view.findViewById(R.id.et_address2);
        etPostalCode = view.findViewById(R.id.et_postal_code);
        btnBack = view.findViewById(R.id.btn_back_address);
        btnNext = view.findViewById(R.id.btn_next_address);

        setCountriesAndStates();
        firstScreen = new PatientPersonalInfoFragment();
        fragment_thirdScreen = new PatientOtherInfoFragment();
        if (getArguments() != null) {
            Log.d(TAG, "initUI: getargs is not null");
            patientDTO = (PatientDTO) getArguments().getSerializable("patientDTO");
            fromThirdScreen = getArguments().getBoolean("fromThirdScreen");
            fromFirstScreen = getArguments().getBoolean("fromFirstScreen");
            patient_detail = getArguments().getBoolean("patient_detail");
            //   patientID_edit = getArguments().getString("patientUuid");

            //check new flow
            Log.d(TAG, "22initUI: firstname:  " + patientDTO.getFirstname());
            Log.d(TAG, "22initUI: lastname: " + patientDTO.getLastname());
            Log.d(TAG, "22initUI: middlename: " + patientDTO.getMiddlename());
            Log.d(TAG, "22initUI: dob: " + patientDTO.getDateofbirth());
            Log.d(TAG, "22initUI: phoneno: " + patientDTO.getPhonenumber());
            Log.d(TAG, "22initUI: patient_detail: " + patient_detail);
            Log.d(TAG, "22initUI: fromFirstScreen: " + fromFirstScreen);


           /* if (patientID_edit != null) {
                patientDTO.setUuid(patientID_edit);
            } else {
                // do nothing...
            }
*/
            if (patient_detail) {
                //   patientDTO.setUuid(patientID_edit);
            } else {
                // do nothing...
            }
        }


    }

    private void setCountriesAndStates() {
        //countries array
        List<String> countriesList = Arrays.asList(getResources().getStringArray(R.array.countries));
        CountryArrayAdapter adapter = new CountryArrayAdapter
                (getActivity(), countriesList);
        autotvCountry.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);
        autotvCountry.setAdapter(adapter);
        autotvCountry.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: spinner");

                if (position != 0) {
                    String country = parent.getItemAtPosition(position).toString();
//                    ArrayAdapter<CharSequence> stateAdapter = null;
                /*todo for All Language Changes Regarding...
                  if (country.matches(getResources().getString(R.string.str_check_India))) {

                        try {
                            String mStateLanguage = "states_india_" + sessionManager.getAppLanguage();
                            int state = res.getIdentifier(mStateLanguage, "array", getApplicationContext().getPackageName());

                            if (state != 0) {
                                stateAdapter = ArrayAdapter.createFromResource(mContext,
                                        state, R.layout.custom_spinner);
                            }
                            mState.setAdapter(stateAdapter);
                        } catch (Exception e) {

                            Logger.logE("Identification", "#648", e);
                        }

                        if (patientID_edit != null)
//                            mState.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));

                        mState.setSelection(stateAdapter.getPosition(StringUtils.getValue(StringUtils.mSwitch_hi_en_te_State_edit(patient1.getState_province(),sessionManager.getAppLanguage()))));

                        else
//                            mState.setSelection(0);
                            mState.setSelection(stateAdapter.getPosition(getResources().getString(R.string.str_check_Odisha)));

                    } else if (country.matches(getResources().getString(R.string.str_check_UnitedStates))) {
                        try {
                            String mStatesLanguage = "states_us_" + sessionManager.getAppLanguage();
                            int state = res.getIdentifier(mStatesLanguage, "array", getApplicationContext().getPackageName());
                            if (state != 0) {
                                stateAdapter = ArrayAdapter.createFromResource(mContext,
                                        state, R.layout.custom_spinner);
                            }
                            mState.setAdapter(stateAdapter);
                        } catch (Exception e) {

                            Logger.logE("Identification", "#648", e);
                        }
                        if (patientID_edit != null) {
                            mState.setSelection(stateAdapter.getPosition(StringUtils.getValue(StringUtils.mSwitch_hi_en_te_State_edit(patient1.getState_province(),sessionManager.getAppLanguage()))));

//                            mState.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
                        } else {
                            mState.setSelection(0);
                        }
                    } else if (country.matches(getResources().getString(R.string.str_check_Philippines))) {
                        try {
                            String mStatesLanguage = "states_philippines_" + sessionManager.getAppLanguage();
                            int state = res.getIdentifier(mStatesLanguage, "array", getApplicationContext().getPackageName());
                            if (state != 0) {
                                stateAdapter = ArrayAdapter.createFromResource(mContext,
                                        state, R.layout.custom_spinner);
                            }
                            mState.setAdapter(stateAdapter);
                        } catch (Exception e) {

                            Logger.logE("Identification", "#648", e);
                        }

                        if (patientID_edit != null) {
                            mState.setSelection(stateAdapter.getPosition(StringUtils.getValue(StringUtils.mSwitch_hi_en_te_State_edit(patient1.getState_province(),sessionManager.getAppLanguage()))));
                        } else {
                            mState.setSelection(stateAdapter.getPosition(getResources().getString(R.string.str_check_Bukidnon)));
                        }

                    } else {
                        stateAdapter = ArrayAdapter.createFromResource(mContext,
                                R.array.state_error, R.layout.custom_spinner);
                        // stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mState.setAdapter(stateAdapter);

                    }*/

                    if (country.matches("India")) {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(mContext,
                                R.array.states_india, R.layout.custom_spinner);
                        // stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        autotvState.setAdapter(stateAdapter);
                        // setting state according database when user clicks edit details

                   /*    temp  if (patientID_edit != null)
                            autotvState.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
                        else
*/
                        //for default state select state
                        autotvState.setSelection(0);
//                            mState.setSelection(stateAdapter.getPosition(state));


                    } else if (country.matches("United States")) {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(mContext,
                                R.array.states_us, R.layout.custom_spinner);
                        // stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        autotvState.setAdapter(stateAdapter);

                       /*
                       temp  if (patientID_edit != null) {

                            autotvState.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
                        }*/
                    } else if (country.matches("Philippines")) {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(mContext,
                                R.array.states_philippines, R.layout.custom_spinner);
                        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        autotvState.setAdapter(stateAdapter);

                    /*   temp  if (patientID_edit != null) {
                            autotvState.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
                        } else {*/
                        autotvState.setSelection(stateAdapter.getPosition("Bukidnon"));
                        // }

                    } else {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(mContext,
                                R.array.state_error, R.layout.custom_spinner);
                        // stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        autotvState.setAdapter(stateAdapter);
                    }
                }

            }
        });

        autotvState.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String state = parent.getItemAtPosition(position).toString();
                Log.d(TAG, "onItemSelected: state : " + state);
                if (state.matches(getResources().getString(R.string.str_check_Odisha))) {
                    Log.d(TAG, "onItemSelected: state : " + state);
                    //Creating the instance of ArrayAdapter containing list of fruit names
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,
                            R.array.odisha_villages, R.layout.custom_spinner);
                    autotvCity.setThreshold(1);//will start working from first character
                    autotvCity.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
                } else if (state.matches(getResources().getString(R.string.str_check_Bukidnon))) {
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,
                            R.array.bukidnon_villages, R.layout.custom_spinner);
                    autotvCity.setThreshold(1);//will start working from first character
                    autotvCity.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
                } else {
                    autotvCity.setAdapter(null);
                }


//                if (state.matches("Odisha")) {
//                    //Creating the instance of ArrayAdapter containing list of fruit names
//                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
//                            R.array.odisha_villages, R.layout.custom_spinner);
//                    mCity.setThreshold(1);//will start working from first character
//                    mCity.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
//                } else if (state.matches("Bukidnon")) {
//                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
//                            R.array.bukidnon_villages, R.layout.custom_spinner);
//                    mCity.setThreshold(1);//will start working from first character
//                    mCity.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
//                } else {
//                    mCity.setAdapter(null);
//                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ivPersonal.setImageDrawable(getResources().getDrawable(R.drawable.ic_personal_info_done));
        ivAddress.setImageDrawable(getResources().getDrawable(R.drawable.ic_address_active));
        ivOther.setImageDrawable(getResources().getDrawable(R.drawable.ic_other_unselected));

        if (!sessionManager.getLicenseKey().isEmpty())
            hasLicense = true;

        //Check for license key and load the correct config file
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse
                        (FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, mContext),
                                String.valueOf(FileUtils.encodeJSON(mContext, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(getActivity(), AppConstants.CONFIG_FILE_NAME)));
            }

            //Display the fields on the Add Patient screen as per the config file
            country1 = obj.getString("mCountry");
            state = obj.getString("mState");

//            if (obj.getBoolean("country_spinner")) {
//                mFirstName.setVisibility(View.VISIBLE);
//            } else {
//                mFirstName.setVisibility(View.GONE);
//            }
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
//            Issue #627
//            added the catch exception to check the config and throwing back to setup activity
            Toast.makeText(getActivity(), "JsonException" + e, Toast.LENGTH_LONG).show();
            //  showAlertDialogButtonClicked(e.toString());
        }

     /*
     added in intui method
     Resources res = getResources();

        // country
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.countries));
//        country_spinner.setSelection(countryAdapter.getPosition(country1));
        mCountryNameSpinner.setAdapter(countryAdapter); // keeping this is setting textcolor to white so comment this and add android:entries in xml

       *//* ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.states_india, android.R.layout.simple_spinner_dropdown_item);
        state_spinner.setSelection(stateAdapter.getPosition(state));
*//*

        //state
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.states_india));
        mStateNameSpinner.setAdapter(stateAdapter);

        //district
        districtAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.district));
        mDistrictNameSpinner.setAdapter(districtAdapter);

        //city
        cityAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.city));
        mCityNameSpinner.setAdapter(cityAdapter);
*/
        // Setting up the screen when user came from SEcond screen.
        if (fromThirdScreen || fromFirstScreen) {
            if (patientDTO.getPostalcode() != null && !patientDTO.getPostalcode().isEmpty())
                etPostalCode.setText(patientDTO.getPostalcode());
            if (patientDTO.getAddress1() != null && !patientDTO.getAddress1().isEmpty())
                etAddress1.setText(patientDTO.getAddress1());
            if (patientDTO.getAddress2() != null && !patientDTO.getAddress2().isEmpty())
                etAddress2.setText(patientDTO.getAddress2());
            //autotvCountry.setSelection(countryAdapter.getPosition(String.valueOf(patientDTO.getCountry())));
            // mStateNameSpinner.setSelection(stateAdapter.getPosition(String.valueOf(patientDTO.getStateprovince())));

            autotvCountry.setText(patientDTO.getCountry());
            autotvState.setText(patientDTO.getStateprovince());

            if (patientDTO.getCityvillage() != null) {
                String[] district_city = patientDTO.getCityvillage().trim().split(":");
                //  district = district_city[0];
               if(district_city.length>=2)
                city_village = district_city[1];
                //mDistrictNameSpinner.setSelection(districtAdapter.getPosition(district));
                // temp autotvCity.setSelection(cityAdapter.getPosition(city_village));
                Log.d(TAG, "onActivityCreated: city_village : " + city_village);
                autotvState.setText(city_village);


            }
        }

        // Back Button click event.
        btnBack.setOnClickListener(v -> {
            onBackInsertIntopatientDTO();
        });

        // Next Button click event.
        btnNext.setOnClickListener(v -> {
            onPatientCreateClicked();
        });

        autotvCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    String district = adapterView.getItemAtPosition(i).toString();
                 /*  temp  mCityNameErrorTextView.setVisibility(View.GONE);
                    mCityNameSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);*/
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
     /*
     temp
     // District based City - start
        mDistrictNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    String district = adapterView.getItemAtPosition(i).toString();
                    mDistrictNameErrorTextView.setVisibility(View.GONE);
                    mDistrictNameSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
                    if (!fromThirdScreen || fromFirstScreen) {
                        if (district.matches("Navi Mumbai")) {
                            ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(getActivity(),
                                    R.array.city, android.R.layout.simple_spinner_dropdown_item);
                            mCityNameSpinner.setAdapter(cityAdapter);

                            // setting state according database when user clicks edit details
                            if (fromThirdScreen || fromFirstScreen)
                                mCityNameSpinner.setSelection(cityAdapter.getPosition(String.valueOf(city_village)));
                            else
                                mCityNameSpinner.setSelection(cityAdapter.getPosition("Select"));

                        } else {
                            // show errro msg
                        }
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // District based city - end


        // district based  state - start
        mStateNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    String state = adapterView.getItemAtPosition(i).toString();
                    mStateNameErrorTextView.setVisibility(View.GONE);
                    mStateNameSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
                    if (state.matches("Maharashtra")) {
                        ArrayAdapter<CharSequence> districtAdapter = ArrayAdapter.createFromResource(getActivity(),
                                R.array.district, android.R.layout.simple_spinner_dropdown_item);
                        mDistrictNameSpinner.setAdapter(districtAdapter);

                        // setting state according database when user clicks edit details
                        if (fromThirdScreen || fromFirstScreen)
                            mDistrictNameSpinner.setSelection(districtAdapter.getPosition(String.valueOf(district)));
                        else
                            mDistrictNameSpinner.setSelection(districtAdapter.getPosition("Select"));

                    } else {

                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // State based district - end*/

      /*  // country - start
        autotvCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    String country = adapterView.getItemAtPosition(i).toString();
                    //mCountryNameErrorTextView.setVisibility(View.GONE);
                   // mCountryNameSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
                    if (country.matches("India")) {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(getActivity(),
                                R.array.states_india, android.R.layout.simple_spinner_dropdown_item);
                        autotvState.setAdapter(stateAdapter);

                        // setting state according database when user clicks edit details
                        if (fromThirdScreen || fromFirstScreen)
                            autotvState.setSelection(stateAdapter.getPosition(String.valueOf(patientDTO.getStateprovince())));
                        else
                            autotvState.setSelection(stateAdapter.getPosition("Select"));

                    } else if (country.matches("United States")) {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(getActivity(),
                                R.array.states_us, android.R.layout.simple_spinner_dropdown_item);
                        mStateNameSpinner.setAdapter(stateAdapter);

                        // setting state according database when user clicks edit details
                        if (fromThirdScreen || fromFirstScreen)
                            mStateNameSpinner.setSelection(stateAdapter.getPosition(String.valueOf(patientDTO.getStateprovince())));
                        else
                            mStateNameSpinner.setSelection(stateAdapter.getPosition("Select"));

                    } else if (country.matches("Philippines")) {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(getActivity(),
                                R.array.states_philippines, android.R.layout.simple_spinner_dropdown_item);
                        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mStateNameSpinner.setAdapter(stateAdapter);

                        // setting state according database when user clicks edit details
                        if (fromThirdScreen || fromFirstScreen)
                            mStateNameSpinner.setSelection(stateAdapter.getPosition(String.valueOf(patientDTO.getStateprovince())));
                        else
                            mStateNameSpinner.setSelection(stateAdapter.getPosition("Select"));

                    } else {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(getActivity(),
                                R.array.state_error, android.R.layout.simple_spinner_dropdown_item);
                        mStateNameSpinner.setAdapter(stateAdapter);
                    }
                }
                new SnackbarUtils().hideKeyboard(getActivity());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // country - end*/

    }

    private void onBackInsertIntopatientDTO() {
        patientDTO.setPostalcode(etPostalCode.getText().toString());
        patientDTO.setCountry(autotvCountry.getText().toString());
        patientDTO.setStateprovince(autotvState.getText().toString());
        patientDTO.setCityvillage(autotvCity.getText().toString());
        patientDTO.setAddress1(etAddress1.getText().toString());
        patientDTO.setAddress2(etAddress2.getText().toString());

     /*   Log.v("fragmemt_2", "values: " + autotvCountry.getSelectedItem().toString()
                + "\n" + autotvState.getSelectedItem().toString()
                + "\n" + autotvCity.getSelectedItem().toString()
                + "\n" + mAddress1EditText.getText().toString()
                + "\n" + mAddress2EditText.getText().toString());*/

        Bundle bundle = new Bundle();
        bundle.putSerializable("patientDTO", (Serializable) patientDTO);
        bundle.putBoolean("fromSecondScreen", true);
        bundle.putBoolean("patient_detail", patient_detail);
        firstScreen.setArguments(bundle); // passing data to Fragment
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_add_patient, firstScreen)
                .commit();
    }

    public void onPatientCreateClicked() {

        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
        List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();
        uuid = UUID.randomUUID().toString();

        patientDTO.setUuid(uuid);
        Gson gson = new Gson();

        boolean cancel = false;
        View focusView = null;


/*

temp
        if (dob.equals("") || dob.toString().equals("")) {
            if (dob.after(today)) {
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(mContext);
                alertDialogBuilder.setTitle(R.string.error);
                alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_dob);
                //alertDialogBuilder.setMessage(getString(R.string.identification_dialog_date_error));
                alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();

                mDOBPicker.show();
                alertDialog.show();

                Button postiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                postiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                // postiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);
                return;
            }
        }
*/


   /*     ArrayList<EditText> values = new ArrayList<>();
        values.add(mFirstName);
        values.add(mMiddleName);
        values.add(mLastName);
        values.add(mDOB);
        values.add(mPhoneNum);
        values.add(mAddress1);
        values.add(mAddress2);
        values.add(mCity);
        values.add(mPostal);
        values.add(mRelationship);
        values.add(mOccupation);*/

/*
        if (!mGenderF.isChecked() && !mGenderM.isChecked()) {
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
            alertDialogBuilder.setTitle(R.string.error);
            alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_gender);
            alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
            positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

            return;
        }
*/

        if (!autotvCountry.getText().toString().equals("") && !autotvState.getText().toString().equals("")
                && !autotvCity.getText().toString().equals("")) {

            Log.v(TAG, "Result");

        } else {
            if (autotvCountry.getText().toString().equals("")) {
                autotvCountry.setError(getString(R.string.error_field_required));
            }

            if (autotvState.getText().toString().equals("")) {
                autotvState.setError(getString(R.string.error_field_required));
            }

            if (autotvCity.getText().toString().equals("")) {
                autotvCity.setError(getString(R.string.error_field_required));
            }

/* commented as per new flow

            if (!mGenderF.isChecked() && !mGenderM.isChecked()) {
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
                alertDialogBuilder.setTitle(R.string.error);
                alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_gender);
                alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);

            }

*/


            Toast.makeText(mContext, R.string.identification_screen_required_fields, Toast.LENGTH_LONG).show();
            return;
        }

      /*
      validation pending
      if (autotvCountry.getListSelection() == 0) {
            countryText.setError(getString(R.string.error_field_required));
            focusView = countryText;
            cancel = true;
            return;
        } else {
            countryText.setError(null);
        }


        if (mState.getSelectedItemPosition() == 0) {
            stateText.setError(getString(R.string.error_field_required));
            focusView = stateText;
            cancel = true;
            return;
        } else {
            stateText.setError(null);
        }*/
        /*new*/

        if (cancel) {
            focusView.requestFocus();
        } else {

            patientDTO.setAddress1(StringUtils.getValue(etAddress1.getText().toString()));
            patientDTO.setAddress2(StringUtils.getValue(etAddress2.getText().toString()));
            patientDTO.setPostalcode(StringUtils.getValue(etPostalCode.getText().toString()));
            patientDTO.setCityvillage(StringUtils.getValue(autotvCity.getText().toString()));
            patientDTO.setStateprovince(StringUtils.getValue(autotvState.getText().toString()));
            patientDTO.setCountry(StringUtils.getValue(autotvCountry.getText().toString()));

            /*patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("caste"));
            patientAttributesDTO.setValue(StringUtils.getProvided(mCaste));
            patientAttributesDTOList.add(patientAttributesDTO);*/

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Telephone Number"));
            // patientAttributesDTO.setValue(StringUtils.getValue(mPhoneNum.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);

        /*    //Ezazi Registration Number
            int number = (int) (Math.random() * (99999999 - 100 + 1) + 100);
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Ezazi Registration Number"));
            patientAttributesDTO.setValue(patientDTO.getCountry().substring(0, 2) + "/" + patientDTO.getStateprovince().substring(0, 2) + "/" + patientDTO.getCityvillage().substring(0, 2) + "/" + String.valueOf(number));
            patientAttributesDTOList.add(patientAttributesDTO);*/

            // temp  patientAttributesDTOList.add(patientAttributesDTO);
            Logger.logD(TAG, "PatientAttribute list size" + patientAttributesDTOList.size());
            patientDTO.setPatientAttributesDTOList(patientAttributesDTOList);
            patientDTO.setSyncd(false);
            Logger.logD("patient json : ", "Json : " + gson.toJson(patientDTO, PatientDTO.class));


        }
        // Bundle data
        Bundle bundle = new Bundle();
        bundle.putSerializable("patientDTO", (Serializable) patientDTO);
        bundle.putBoolean("fromSecondScreen", true);
        //   bundle.putString("patientUuid", patientID_edit);
        bundle.putBoolean("patient_detail", patient_detail);
        fragment_thirdScreen.setArguments(bundle); // passing data to Fragment

        Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_add_patient, fragment_thirdScreen)
                .commit();

        if (NetworkConnection.isOnline(mContext)) {
//                patientApiCall();
//                frameJson();

//                AppConstants.notificationUtils.showNotifications(getString(R.string.patient_data_upload),
//                        getString(R.string.uploading) + patientDTO.getFirstname() + "" + patientDTO.getLastname() +
//                                "'s data", 2, getApplication());


//                if (push)
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientDTO.getFirstname() + "" + patientDTO.getLastname() + "'s data upload complete.", 2, getApplication());
//                else
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientDTO.getFirstname() + "" + patientDTO.getLastname() + "'s data not uploaded.", 2, getApplication());

//                if (pushImage)
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientDTO.getFirstname() + "" + patientDTO.getLastname() + "'s Image upload complete.", 4, getApplication());
//                else
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientDTO.getFirstname() + "" + patientDTO.getLastname() + "'s Image not complete.", 4, getApplication());


//

//            else {
//                AppConstants.notificationUtils.showNotifications(getString(R.string.patient_data_failed), getString(R.string.check_your_connectivity), 2, IdentificationActivity.this);
//            }

        }
    }

}

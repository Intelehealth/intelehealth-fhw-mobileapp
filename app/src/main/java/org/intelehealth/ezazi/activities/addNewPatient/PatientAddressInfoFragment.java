package org.intelehealth.ezazi.activities.addNewPatient;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.addNewPatient.model.DistData;
import org.intelehealth.ezazi.activities.addNewPatient.model.StateData;
import org.intelehealth.ezazi.activities.addNewPatient.model.StateDistMaster;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.database.dao.PatientsDAO;
import org.intelehealth.ezazi.models.dto.PatientAttributesDTO;
import org.intelehealth.ezazi.models.dto.PatientAttributesModel;
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

    public static PatientAddressInfoFragment getInstance() {
        return new PatientAddressInfoFragment();
    }

    View view;
    AutoCompleteTextView autotvCountry, autotvState, autotvCity, autotvDistrict;
    Context mContext;
    TextInputEditText etAddress1, etAddress2, etPostalCode, etDistrict, etCityVillage;
    private PatientOtherInfoFragment fragment_thirdScreen;
    private PatientPersonalInfoFragment firstScreen;
    boolean fromThirdScreen = false, fromFirstScreen = false;
    boolean patient_detail = false;
    boolean editDetails = false;
    boolean fromSummary = false;
    String patientUuidUpdate = "";
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
    //    ImageView ivPersonal, ivAddress, ivOther;
//    TextView tvPersonalInfo, tvAddressInfo, tvOtherInfo;
    String[] countryArr, stateArr;
    TextView tvErrorCountry, tvErrorState, tvErrorCityVillage, tvErrorPostalCode, tvDistrictError;
    MaterialCardView cardCountry, cardState, cardCityVillage, cardPostalCode, cardDistrict;
    String mAlternateNumberString;
    TextInputLayout etLayoutCityVillage;
    String[] cityVillagesArr;
    String[] districtsArr;
    boolean isLoadFirstTime;
    private boolean mIsIndiaSelected = true;
    private String mCountryName = "", mStateName = "", mDistName = "", mCityVillageName = "";
    private String mCountryNameEn = "", mStateNameEn = "", mDistNameEn = "", mCityVillageNameEn = "";
    private String[] mCountryList = null;
    private List<StateData> mLastSelectedStateList = new ArrayList<>();
    private List<DistData> mLastSelectedDistList = new ArrayList<>();
    private StateDistMaster mStateDistMaster;
    ArrayAdapter<String> districtAdapter, stateAdapter;
    String district;
    PatientAttributesModel patientAttributesModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_patient_address_info, container, false);
        mContext = getActivity();
        sessionManager = new SessionManager(getActivity());
        initUI();
        return view;
    }

    private void initUI() {
//        ivPersonal = requireActivity().findViewById(R.id.iv_personal_info);
//        ivAddress = getActivity().findViewById(R.id.iv_address_info);
//        ivOther = getActivity().findViewById(R.id.iv_other_info);
//        tvPersonalInfo = getActivity().findViewById(R.id.tv_personal_info);
//        tvAddressInfo = getActivity().findViewById(R.id.tv_address_info);
//        tvOtherInfo = getActivity().findViewById(R.id.tv_other_info);

        autotvCountry = view.findViewById(R.id.autotv_country);
        autotvState = view.findViewById(R.id.autotv_state);
        autotvDistrict = view.findViewById(R.id.autotv_district);

        autotvCity = view.findViewById(R.id.autotv_city);
        etAddress1 = view.findViewById(R.id.et_address1);
        etAddress2 = view.findViewById(R.id.et_address2);
        etPostalCode = view.findViewById(R.id.et_postal_code);
        btnBack = view.findViewById(R.id.btn_back_address);
        btnNext = view.findViewById(R.id.btn_next_address);
        tvErrorCountry = view.findViewById(R.id.tv_error_country);
        tvErrorState = view.findViewById(R.id.tv_error_state);
        tvErrorCityVillage = view.findViewById(R.id.tv_error_city_village);
        tvErrorPostalCode = view.findViewById(R.id.tv_error_postal_code);
        tvDistrictError = view.findViewById(R.id.tv_error_district);


        cardCountry = view.findViewById(R.id.card_country);
        cardState = view.findViewById(R.id.card_state);
        cardCityVillage = view.findViewById(R.id.card_city_village);
        etLayoutCityVillage = view.findViewById(R.id.et_layout_city_village);
        cardPostalCode = view.findViewById(R.id.card_postal_code);
        cardPostalCode = view.findViewById(R.id.card_postal_code);
        cardDistrict = view.findViewById(R.id.card_district);
        etDistrict = view.findViewById(R.id.et_district);
        etCityVillage = view.findViewById(R.id.et_city_village);

        autotvState.addTextChangedListener(new MyTextWatcher(autotvState));
        autotvDistrict.addTextChangedListener(new MyTextWatcher(autotvDistrict));
        etCityVillage.addTextChangedListener(new MyTextWatcher(etCityVillage));
        etPostalCode.addTextChangedListener(new MyTextWatcher(etPostalCode));


        firstScreen = new PatientPersonalInfoFragment();
        fragment_thirdScreen = new PatientOtherInfoFragment();
        if (getArguments() != null) {
            patientDTO = (PatientDTO) getArguments().getSerializable("patientDTO");
            fromThirdScreen = getArguments().getBoolean("fromThirdScreen");
            fromFirstScreen = getArguments().getBoolean("fromFirstScreen");
            patient_detail = getArguments().getBoolean("patient_detail");
            mAlternateNumberString = getArguments().getString("mAlternateNumberString");
            editDetails = getArguments().getBoolean("editDetails");
            fromSummary = getArguments().getBoolean("fromSummary");
            patientUuidUpdate = getArguments().getString("patientUuidUpdate");
            patientAttributesModel = (PatientAttributesModel) getArguments().getSerializable("patientAttributes");


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

        etCityVillage.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(requireActivity());
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

        mStateDistMaster = new Gson().fromJson(FileUtils.encodeJSON(mContext, "state_district_tehsil.json").toString(), StateDistMaster.class);
        sessionManager.setAppLanguage("en");
        mCountryName = sessionManager.getAppLanguage().equals("en") ? "India" : "भारत";

//        ivPersonal.setImageDrawable(getResources().getDrawable(R.drawable.ic_personal_info_done));
//        ivAddress.setImageDrawable(getResources().getDrawable(R.drawable.ic_address_active));
//        ivOther.setImageDrawable(getResources().getDrawable(R.drawable.ic_other_unselected));
//        tvPersonalInfo.setTextColor(getResources().getColor(R.color.colorPrimary));
//        tvAddressInfo.setTextColor(getResources().getColor(R.color.colorPrimary));
//        tvOtherInfo.setTextColor(getResources().getColor(R.color.darkGray));
        if (!sessionManager.getLicenseKey().isEmpty()) hasLicense = true;

        //Check for license key and load the correct config file
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse(FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, mContext), String.valueOf(FileUtils.encodeJSON(mContext, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
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
        // Setting up the screen when user came from SEcond screen.
        if (fromThirdScreen || fromFirstScreen) {
            if (patientDTO.getPostalcode() != null && !patientDTO.getPostalcode().isEmpty())
                etPostalCode.setText(patientDTO.getPostalcode());
            if (patientDTO.getAddress1() != null && !patientDTO.getAddress1().isEmpty())
                etAddress1.setText(patientDTO.getAddress1());
            if (patientDTO.getAddress2() != null && !patientDTO.getAddress2().isEmpty())
                etAddress2.setText(patientDTO.getAddress2());
            autotvState.setText(patientDTO.getStateprovince());
            mStateName = patientDTO.getStateprovince();

           /* int countryIndex = countryAdapter.getPosition(String.valueOf(patientDTO.getCountry()));
            if (countryIndex <= 0) {
                countryIndex = countryAdapter.getPosition(sessionManager.getAppLanguage().equals("en") ? "India" : "भारत");
                mCountryName = sessionManager.getAppLanguage().equals("en") ? "India" : "भारत";
            }*/
            if (patientDTO.getCityvillage() != null && !patientDTO.getCityvillage().isEmpty()) {
                String[] district_city = patientDTO.getCityvillage().trim().split(":");
                if (district_city.length == 2) {
                    district = mDistName = district_city[0];
                    city_village = mCityVillageName = district_city[1];
                    etCityVillage.setText(city_village);
                    autotvDistrict.setText(district, false);

                }
            }

            if (mCountryName.equalsIgnoreCase(sessionManager.getAppLanguage().equals("en") ? "India" : "भारत")) {
                mIsIndiaSelected = true;
                setStateAdapter(mCountryName);
                Log.v(TAG, "mStateName -" + mStateName + "??");
                if (mStateName != null && !mStateName.isEmpty()) {
                    // autotvState.setSelection(stateAdapter.getPosition(String.valueOf(patientDTO.getStateprovince())));
                    autotvState.setText(patientDTO.getStateprovince(), false);

                    setDistAdapter(mStateName);
                    Log.d(TAG, "onActivityCreated: mDistName : " + mDistName);
                    if (mDistName != null && mDistName.isEmpty())
                        // autotvDistrict.setSelection(districtAdapter.getPosition(district));
                        autotvDistrict.setText(district, false);
                }

            } else {
                mIsIndiaSelected = false;
                //mStateEditText.setVisibility(View.VISIBLE);
                autotvState.setVisibility(View.GONE);
                //mStateEditText.setText(patientDTO.getStateprovince() != null ? String.valueOf(patientDTO.getStateprovince()) : "");
                etDistrict.setVisibility(View.VISIBLE);
                autotvDistrict.setVisibility(View.GONE);
                etDistrict.setText(String.valueOf(district));
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

        // District based City - start

        autotvDistrict.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    String distName = parent.getItemAtPosition(position).toString();
                    // if (!distName.equalsIgnoreCase(mDistName)) etCityVillage.setText("");
                    mDistName = parent.getItemAtPosition(position).toString();
                    mDistNameEn = mLastSelectedDistList.get(position - 1).getName();
                    tvDistrictError.setVisibility(View.GONE);
                    //autotvDistrict.setBackgroundResource(R.drawable.ui2_spinner_background_new);
                    //etCityVillage.setBackgroundResource(R.drawable.bg_input_fieldnew);

                    //   if (!fromThirdScreen || fromFirstScreen) {
                    /*if (district.matches("Navi Mumbai")) {
                        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(getActivity(),
                                R.array.navi_mumbai_city, android.R.layout.simple_spinner_dropdown_item);
                        mCityNameSpinner.setAdapter(cityAdapter);

                        // setting state according database when user clicks edit details
                        if (fromThirdScreen || fromFirstScreen)
                            mCityNameSpinner.setSelection(cityAdapter.getPosition(String.valueOf(city_village)));
                        else
                            mCityNameSpinner.setSelection(cityAdapter.getPosition("Select"));

                    } else if (district.matches("Kurla")) {
                        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(getActivity(),
                                R.array.kurla_city, android.R.layout.simple_spinner_dropdown_item);
                        mCityNameSpinner.setAdapter(cityAdapter);

                        // setting state according database when user clicks edit details
                        if (fromThirdScreen || fromFirstScreen)
                            mCityNameSpinner.setSelection(cityAdapter.getPosition(String.valueOf(city_village)));
                        else
                            mCityNameSpinner.setSelection(cityAdapter.getPosition("Select"));

                    }*/
                    //      }
                }

            }
        });
        // District based city - end


        // district based  state - start
        autotvState.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    mStateName = parent.getItemAtPosition(position).toString();
                    mStateNameEn = mLastSelectedStateList.get(position - 1).getState();
                    tvErrorState.setVisibility(View.GONE);
                    autotvDistrict.setText("");

                    // autotvState.setBackgroundResource(R.drawable.ui2_spinner_background_new);

                    Log.d(TAG, "onItemSelected:mCountryName :  " + mCountryName);
                    Log.d(TAG, "onItemSelected:language :  " + sessionManager.getAppLanguage());

                    if (mCountryName.equalsIgnoreCase(sessionManager.getAppLanguage().equals("en") ? "India" : "भारत")) {
                        Log.d(TAG, "onItemSelected: in if");
                        etDistrict.setVisibility(View.GONE);
                        autotvDistrict.setVisibility(View.VISIBLE);
                        setDistAdapter(mStateName);

                        if (district != null && !district.isEmpty()) {
                            if (fromThirdScreen || fromFirstScreen)
                                //autotvDistrict.setSelection(districtAdapter.getPosition(String.valueOf(district)));
                                autotvDistrict.setText(district, false);

                            else
                                //  autotvDistrict.setSelection(districtAdapter.getPosition(getResources().getString(R.string.select_spinner)));
                                autotvDistrict.setText("");

                        } else {
                            autotvDistrict.setText("");

                        }


                    } else {

                        etDistrict.setVisibility(View.VISIBLE);
                        autotvDistrict.setVisibility(View.GONE);
                        if (fromThirdScreen || fromFirstScreen)
                            etDistrict.setText(String.valueOf(district));
                    }


                    /*if (state.matches("Maharashtra")) {
                        ArrayAdapter<CharSequence> districtAdapter = ArrayAdapter.createFromResource(getActivity(),
                                R.array.district, android.R.layout.simple_spinner_dropdown_item);
                        mDistrictNameSpinner.setAdapter(districtAdapter);
                        mDistrictNameSpinner.setVisibility(View.VISIBLE);
                        mCityNameSpinner.setVisibility(View.VISIBLE);
                        mDistrictET.setVisibility(View.GONE);
                        mCityVillageET.setVisibility(View.GONE);

                        // setting state according database when user clicks edit details
                        if (fromThirdScreen || fromFirstScreen)
                            mDistrictNameSpinner.setSelection(districtAdapter.getPosition(String.valueOf(district)));
                        else
                            mDistrictNameSpinner.setSelection(districtAdapter.getPosition("Select"));

                    } else {
                        ArrayAdapter<CharSequence> districtAdapter = ArrayAdapter.createFromResource(getActivity(),
                                R.array.select, android.R.layout.simple_spinner_dropdown_item);
                        mDistrictNameSpinner.setAdapter(districtAdapter);
                        mDistrictNameSpinner.setVisibility(View.GONE);
                        mDistrictET.setVisibility(View.VISIBLE);

                        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(getActivity(),
                                R.array.select, android.R.layout.simple_spinner_dropdown_item);
                        mCityNameSpinner.setAdapter(cityAdapter);
                        mCityNameSpinner.setVisibility(View.GONE);
                        mCityVillageET.setVisibility(View.VISIBLE);
                    }*/
                }

            }
        });
        // State based district - end


        autotvCountry.setText(getResources().getString(R.string.str_check_India));

    }

    private void onBackInsertIntopatientDTO() {
        patientDTO.setAddress1(etAddress1.getText().toString());
        patientDTO.setAddress2(etAddress2.getText().toString());

        mStateName = autotvState.getText().toString().trim();
        mDistName = etDistrict.getText().toString().trim();

        mCityVillageName = etCityVillage.getText().toString().trim();

        patientDTO.setPostalcode(etPostalCode.getText().toString());
        patientDTO.setCountry(autotvCountry.getText().toString());

        patientDTO.setStateprovince(StringUtils.getValue(mIsIndiaSelected ? autotvState.getText().toString() : mStateName));

        patientDTO.setCityvillage(StringUtils.getValue((mIsIndiaSelected ? autotvDistrict.getText().toString() : mDistName) + ":" + mCityVillageName));

        // patientDTO.setStateprovince(autotvState.getText().toString());
        //  patientDTO.setCityvillage(autotvCity.getText().toString());

        Bundle bundle = new Bundle();
        bundle.putSerializable("patientDTO", (Serializable) patientDTO);
        bundle.putBoolean("fromSecondScreen", true);
        bundle.putBoolean("patient_detail", patient_detail);
        bundle.putString("mAlternateNumberString", mAlternateNumberString);
        bundle.putBoolean("fromSummary", fromSummary);
        bundle.putString("patientUuidUpdate", patientUuidUpdate);
        bundle.putSerializable("patientAttributes", (Serializable) patientAttributesModel);

        firstScreen.setArguments(bundle); // passing data to Fragment
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_add_patient, firstScreen).commit();
        ((AddNewPatientActivity) requireActivity()).changeCurrentPage(AddNewPatientActivity.PAGE_PERSONAL);
    }

    public void onPatientCreateClicked() {
        //validations
        /*if (TextUtils.isEmpty(autotvCountry.getText().toString())) {
            autotvCountry.requestFocus();

            tvErrorCountry.setVisibility(View.VISIBLE);
            tvErrorCountry.setText(getString(R.string.select_country));
            cardCountry.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));

            return;
        } else {
            tvErrorCountry.setVisibility(View.GONE);
            cardCountry.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
        }*/

        if (!areValidFields()) {
            return;
        }
        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
        List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();
        uuid = UUID.randomUUID().toString();

        patientDTO.setUuid(uuid);
        Gson gson = new Gson();


        boolean cancel = false;
        View focusView = null;

        if (cancel) {
            focusView.requestFocus();
        } else {
            mStateName = autotvState.getText().toString().trim();
            mDistName = etDistrict.getText().toString().trim();

            mCityVillageName = etCityVillage.getText().toString().trim();
            patientDTO.setStateprovince(StringUtils.getValue(mIsIndiaSelected ? autotvState.getText().toString() : mStateName));
            patientDTO.setCityvillage(StringUtils.getValue((mIsIndiaSelected ? autotvDistrict.getText().toString() : mDistName) + ":" + mCityVillageName));

            if (!sessionManager.getAppLanguage().equals("en")) {
                patientDTO.setCountry(StringUtils.getValue(mCountryNameEn));
                patientDTO.setStateprovince(StringUtils.getValue(mIsIndiaSelected ? mStateNameEn : mStateName));

                patientDTO.setCityvillage(StringUtils.getValue((mIsIndiaSelected ? mDistNameEn : mDistName) + ":" + mCityVillageName));

            }

            patientDTO.setAddress1(StringUtils.getValue(etAddress1.getText().toString()));
            patientDTO.setAddress2(StringUtils.getValue(etAddress2.getText().toString()));
            patientDTO.setPostalcode(StringUtils.getValue(etPostalCode.getText().toString()));
            patientDTO.setCountry("India");

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
        bundle.putBoolean("editDetails", true);
        bundle.putString("mAlternateNumberString", mAlternateNumberString);
        bundle.putBoolean("fromSummary", fromSummary);
        bundle.putString("patientUuidUpdate", patientUuidUpdate);
        bundle.putBoolean("patient_detail", patient_detail);
        bundle.putSerializable("patientAttributes", (Serializable) patientAttributesModel);

        fragment_thirdScreen.setArguments(bundle); // passing data to Fragment
//
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_add_patient, fragment_thirdScreen).commit();
        ((AddNewPatientActivity) requireActivity()).changeCurrentPage(AddNewPatientActivity.PAGE_OTHER);

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

    private boolean areValidFields() {
        List<ErrorManagerModel> errorDetailsList = new ArrayList<>();

        String stateText = autotvState.getText().toString();
        boolean isStateInList = searchForState(stateText);
        if (TextUtils.isEmpty(stateText) || !isStateInList) {

            /*tvErrorState.setVisibility(View.VISIBLE);
            tvErrorState.setText(getString(R.string.select_state));
            cardState.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));*/
            errorDetailsList.add(new ErrorManagerModel(autotvState, tvErrorState, getString(R.string.select_state), cardState));
        } else {
            tvErrorState.setVisibility(View.GONE);
            cardState.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));

        }
        String isDistrictString = searchForDistrict(autotvDistrict.getText().toString());
        if (TextUtils.isEmpty(autotvDistrict.getText().toString()) || isDistrictString.equalsIgnoreCase("notInList")) {

            /*tvDistrictError.setVisibility(View.VISIBLE);
            tvDistrictError.setText(getString(R.string.select_district));*/
            cardDistrict.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
            errorDetailsList.add(new ErrorManagerModel(autotvDistrict, tvDistrictError, getString(R.string.select_district), cardDistrict));

        } else {
            tvDistrictError.setVisibility(View.GONE);
            cardDistrict.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
        }
        if (TextUtils.isEmpty(etCityVillage.getText().toString())) {

         /*   tvErrorCityVillage.setVisibility(View.VISIBLE);
            tvErrorCityVillage.setText(getString(R.string.select_city_village));
            cardCityVillage.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));*/
            errorDetailsList.add(new ErrorManagerModel(etCityVillage, tvErrorCityVillage, getString(R.string.select_city_village), cardCityVillage));

        } else {
            tvErrorCityVillage.setVisibility(View.GONE);
            cardCityVillage.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
        }
        String postalCode = etPostalCode.getText().toString();
        if (!postalCode.isEmpty() && postalCode.length() != 6) {

        /*    tvErrorPostalCode.setVisibility(View.VISIBLE);
            tvErrorPostalCode.setText(getString(R.string.enter_postal_limit));
            cardPostalCode.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));*/
            //errorDetailsList.add(new ErrorManagerModel(etPostalCode, tvErrorPostalCode, getString(R.string.enter_postal_limit), cardPostalCode));
            errorDetailsList.add(new ErrorManagerModel(etPostalCode, tvErrorPostalCode, getString(R.string.enter_postal_limit), cardPostalCode));

        } else {
            tvErrorPostalCode.setVisibility(View.GONE);
            cardPostalCode.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
        }

        if (autotvState.getText().toString().isEmpty() && autotvDistrict.getText().toString().isEmpty() && etCityVillage.getText().toString().isEmpty()) {
            Toast.makeText(mContext, getResources().getString(R.string.fill_required_fields), Toast.LENGTH_SHORT).show();
        }
        if (errorDetailsList.size() > 0) {
            for (int i = 0; i < errorDetailsList.size(); i++) {
                ErrorManagerModel errorModel = errorDetailsList.get(i);
                if (i == 0) {
                    errorModel.view.requestFocus();
                }
                errorModel.tvError.setVisibility(View.VISIBLE);
                errorModel.tvError.setText(errorModel.getErrorMessage());
                errorModel.cardView.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));

            }
            return false;
        } else {
            return true;
        }
    }


    class MyTextWatcher implements TextWatcher {
        EditText editText;

        MyTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            String val = editable.toString().trim();
        /*   if (this.editText.getId() == R.id.autotv_country) {
                if (val.isEmpty()) {
                    tvErrorCountry.setVisibility(View.VISIBLE);
                    tvErrorCountry.setText(getString(R.string.select_admission_date));
                    cardCountry.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
                    return;
                } else {
                    tvErrorCountry.setVisibility(View.GONE);
                    cardCountry.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));

                }
            } else*/

            if (val.length() > 0) {
                if (this.editText.getId() == R.id.autotv_state) {
                    boolean isStateInList = searchForState(val);
                    if (val.isEmpty() || !isStateInList) {

                        tvErrorState.setVisibility(View.VISIBLE);
                        tvErrorState.setText(getString(R.string.select_state));
                        cardState.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
                    } else {
                        tvErrorState.setVisibility(View.GONE);
                        cardState.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));

                    }
                } else if (this.editText.getId() == R.id.autotv_district) {
                    String districtString = searchForDistrict(val);
                    if (val.isEmpty() || districtString.equalsIgnoreCase("notInList")) {
                        tvDistrictError.setVisibility(View.VISIBLE);
                        tvDistrictError.setText(getString(R.string.select_district));
                        cardDistrict.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));

                    } else {
                        tvDistrictError.setVisibility(View.GONE);
                        cardDistrict.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
                    }
                } else if (this.editText.getId() == R.id.et_city_village) {
                    if (val.isEmpty()) {
                        tvErrorCityVillage.setVisibility(View.VISIBLE);
                        tvErrorCityVillage.setText(getString(R.string.select_city_village));
                        cardCityVillage.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));

                    } else {
                        tvErrorCityVillage.setVisibility(View.GONE);
                        cardCityVillage.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
                    }
                } else if (!val.isEmpty() && val.length() != 6) {

                    tvErrorPostalCode.setVisibility(View.VISIBLE);
                    tvErrorPostalCode.setText(getString(R.string.enter_postal_limit));
                    cardPostalCode.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));

                } else {
                    tvErrorPostalCode.setVisibility(View.GONE);
                    cardPostalCode.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
                }
            }

        }

        /*String isDistrictString = searchForDistrict(autotvDistrict.getText().toString());
        if (TextUtils.isEmpty(autotvDistrict.getText().toString()) || isDistrictString.equalsIgnoreCase("notInList")) {
            autotvDistrict.requestFocus();

            tvDistrictError.setVisibility(View.VISIBLE);
            tvDistrictError.setText(getString(R.string.select_city_village));
            cardDistrict.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));

            return;
        } else {
            tvDistrictError.setVisibility(View.GONE);
            cardDistrict.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
        }*/
    }

    private boolean searchForState(String state) {
        if (stateArr != null && stateArr.length > 0) {
            List<String> statesList = Arrays.asList(stateArr);
            return statesList.contains(state);

        } else {
            return false;
        }

    }

    private String searchForCityVillage(String cityVillage) {
        String result = "";
        if (cityVillagesArr != null) {
            List<String> cityVillageList = Arrays.asList(cityVillagesArr);
            boolean isInList = cityVillageList.contains(cityVillage);
            if (isInList) result = "inList";
            else {
                result = "notInList";

            }
        } else result = "none";
        return result;
    }

    private String searchForDistrict(String district) {
        String result = "";
        if (districtsArr != null && districtsArr.length > 0) {
            List<String> districtsList = Arrays.asList(districtsArr);
            boolean isInList = districtsList.contains(district);
            if (isInList) result = "inList";
            else {
                result = "notInList";

            }
        } else result = "none";
        return result;
    }

    private void setStateAdapter(String countryName) {
        mLastSelectedStateList = mStateDistMaster.getStateDataList();
        String[] stateList = new String[mStateDistMaster.getStateDataList().size() + 1];
        stateArr = new String[mStateDistMaster.getStateDataList().size() + 1];

        stateList[0] = getResources().getString(R.string.select_spinner);
        for (int i = 1; i <= mStateDistMaster.getStateDataList().size(); i++) {
            stateList[i] = sessionManager.getAppLanguage().equals("en") ? mStateDistMaster.getStateDataList().get(i - 1).getState() : mStateDistMaster.getStateDataList().get(i - 1).getStateHindi();
            stateArr[i] = sessionManager.getAppLanguage().equals("en") ? mStateDistMaster.getStateDataList().get(i - 1).getState() : mStateDistMaster.getStateDataList().get(i - 1).getStateHindi();
        }

        stateAdapter = new ArrayAdapter<String>(getActivity(), R.layout.custom_spinner, stateList);
        autotvState.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);
        autotvState.setThreshold(1);
        autotvState.setAdapter(stateAdapter);
    }

    private void setDistAdapter(String stateName) {
        Log.v(TAG, "setDistAdapter stateName - " + stateName);
        List<DistData> distDataList = new ArrayList<>();

        for (int i = 0; i < mStateDistMaster.getStateDataList().size(); i++) {
            String sName = sessionManager.getAppLanguage().equals("en") ? mStateDistMaster.getStateDataList().get(i).getState() : mStateDistMaster.getStateDataList().get(i).getStateHindi();
            if (sName.equalsIgnoreCase(stateName)) {
                distDataList = mStateDistMaster.getStateDataList().get(i).getDistDataList();
                break;
            }
        }
        mLastSelectedDistList = distDataList;

        String[] distList = new String[distDataList.size() + 1];
        districtsArr = new String[distDataList.size() + 1];

        distList[0] = getResources().getString(R.string.select_spinner);
        for (int i = 1; i <= distDataList.size(); i++) {
            distList[i] = sessionManager.getAppLanguage().equals("en") ? distDataList.get(i - 1).getName() : distDataList.get(i - 1).getNameHindi();
            Log.v(TAG, "distList[i] - " + distList[i]);
            districtsArr[i] = sessionManager.getAppLanguage().equals("en") ? distDataList.get(i - 1).getName() : distDataList.get(i - 1).getNameHindi();
        }

        districtAdapter = new ArrayAdapter<String>(getActivity(), R.layout.custom_spinner, distList);
        autotvDistrict.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);
        autotvDistrict.setThreshold(1);
        autotvDistrict.setAdapter(districtAdapter);
    }

    public void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}

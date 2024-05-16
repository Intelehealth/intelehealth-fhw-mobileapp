package org.intelehealth.app.activities.identificationActivity;

import static org.intelehealth.app.utilities.StringUtils.inputFilter_Name;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.identificationActivity.model.DistData;
import org.intelehealth.app.activities.identificationActivity.model.StateData;
import org.intelehealth.app.activities.identificationActivity.model.StateDistMaster;
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ImagesPushDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.SnackbarUtils;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.exception.DAOException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Created by Prajwal Waingankar on 13/10/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class Fragment_SecondScreen extends Fragment {
    private static final String TAG = Fragment_SecondScreen.class.getSimpleName();
    private View view;
    SessionManager sessionManager = null;
    private boolean hasLicense = false;
    private ImageView personal_icon, address_icon, other_icon;
    private Button frag2_btn_back, frag2_btn_next;
    private EditText mPostalCodeEditText, mAddress1EditText, mAddress2EditText;
    private Spinner mCountryNameSpinner, mStateNameSpinner, mDistrictNameSpinner/*, mCityNameSpinner*/; // now city always an input field not spinner
    Context context;
    private String country1, state;
    ArrayAdapter<String> districtAdapter, stateAdapter;
    ArrayAdapter<CharSequence> countryAdapter;
    EditText mDistrictET, mCityVillageET;
    private PatientDTO patientDTO;
    private Fragment_ThirdScreen fragment_thirdScreen;
    private Fragment_FirstScreen firstScreen;
    private TextView mPostalCodeErrorTextView, mCountryNameErrorTextView, mStateNameErrorTextView, mDistrictNameErrorTextView, mCityNameErrorTextView, mAddress1ErrorTextView, mAddress2ErrorTextView;
    boolean fromThirdScreen = false, fromFirstScreen = false;
    String district;
    String city_village;
    String patientID_edit;
    boolean patient_detail = false;
    private StateDistMaster mStateDistMaster;
    private EditText mStateEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_registration_secondscreen, container, false);
        setLocale(getContext());
        return view;
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getActivity();
        sessionManager = new SessionManager(getActivity());
        mStateDistMaster = new Gson().fromJson(FileUtils.encodeJSON(getActivity(), "state_district_tehsil.json").toString(), StateDistMaster.class);
        personal_icon = getActivity().findViewById(R.id.addpatient_icon);
        address_icon = getActivity().findViewById(R.id.addresslocation_icon);
        other_icon = getActivity().findViewById(R.id.other_icon);
        frag2_btn_back = getActivity().findViewById(R.id.frag2_btn_back);
        frag2_btn_next = getActivity().findViewById(R.id.frag2_btn_next);
        mPostalCodeEditText = view.findViewById(R.id.postalcode_edittext);
        mCountryNameSpinner = view.findViewById(R.id.country_spinner);
        mStateNameSpinner = view.findViewById(R.id.state_spinner);
        mStateEditText = view.findViewById(R.id.state_edittext);
        mStateEditText.setVisibility(View.GONE);
        mDistrictNameSpinner = view.findViewById(R.id.district_spinner);
        mDistrictET = view.findViewById(R.id.district_edittext);
        mCityVillageET = view.findViewById(R.id.city_village_edittext);
//        mCityVillageET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        mCityVillageET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50), inputFilter_Others}); //maxlength 50
        mAddress1EditText = view.findViewById(R.id.address1_edittext);
        mAddress1EditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50)}); //maxlength 50
        mAddress2EditText = view.findViewById(R.id.address2_edittext);
        mAddress2EditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50)}); //maxlength 50
        mPostalCodeErrorTextView = view.findViewById(R.id.postalcode_error);
        mCountryNameErrorTextView = view.findViewById(R.id.country_error);
        mStateNameErrorTextView = view.findViewById(R.id.state_error);
        mDistrictNameErrorTextView = view.findViewById(R.id.district_error);
        mCityNameErrorTextView = view.findViewById(R.id.city_error);
        mAddress1ErrorTextView = view.findViewById(R.id.address1_error);
        mAddress2ErrorTextView = view.findViewById(R.id.address2_error);
        mPostalCodeEditText.addTextChangedListener(new MyTextWatcher(mPostalCodeEditText));
        mAddress1EditText.addTextChangedListener(new MyTextWatcher(mAddress1EditText));
        mAddress2EditText.addTextChangedListener(new MyTextWatcher(mAddress2EditText));
        mStateEditText.addTextChangedListener(new MyTextWatcher(mStateEditText));
        mDistrictET.addTextChangedListener(new MyTextWatcher(mDistrictET));
        mCityVillageET.addTextChangedListener(new MyTextWatcher(mCityVillageET));
        firstScreen = new Fragment_FirstScreen();
        fragment_thirdScreen = new Fragment_ThirdScreen();
        if (getArguments() != null) {
            patientDTO = (PatientDTO) getArguments().getSerializable("patientDTO");
            fromThirdScreen = getArguments().getBoolean("fromThirdScreen");
            fromFirstScreen = getArguments().getBoolean("fromFirstScreen");
            patient_detail = getArguments().getBoolean("patient_detail");
            if (patient_detail) {
                frag2_btn_back.setVisibility(View.GONE);
                frag2_btn_next.setText(getString(R.string.save));
            } else {
                // do nothing...
            }
        }

        personal_icon.setActivated(true);
        address_icon.setSelected(true);
//        personal_icon.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.addpatient_icon_done));
//        address_icon.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.addresslocation_icon));
//        other_icon.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.other_icon_unselected));

        if (!sessionManager.getLicenseKey().isEmpty())
            hasLicense = true;

        //Check for license key and load the correct config file
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse
                        (FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, context),
                                String.valueOf(FileUtils.encodeJSON(context, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
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

        Resources res = getResources();
        // country
        try {
            mCountryList = getResources().getStringArray(R.array.countries_en);
            String countriesLanguage = "countries_" + sessionManager.getAppLanguage();
            int countries = res.getIdentifier(countriesLanguage, "array", getActivity().getApplicationContext().getPackageName());
            if (countries != 0) {
                countryAdapter = ArrayAdapter.createFromResource(getActivity(),
                        countries, R.layout.simple_spinner_item_1);
                countryAdapter.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);
            }
            mCountryNameSpinner.setAdapter(countryAdapter); // keeping this is setting textcolor to white so comment this and add android:entries in xml
            mCountryNameSpinner.setPopupBackgroundDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.popup_menu_background));
            mCountryNameSpinner.setSelection(countryAdapter.getPosition(sessionManager.getAppLanguage().equals("en") ? "India" : "भारत"));
        } catch (Exception e) {
            Logger.logE("Identification", "#648", e);
        }


        // Setting up the screen when user came from SEcond screen.
        if (fromThirdScreen || fromFirstScreen) {
            if (patientDTO.getPostalcode() != null && !patientDTO.getPostalcode().isEmpty())
                mPostalCodeEditText.setText(patientDTO.getPostalcode());
            if (patientDTO.getAddress1() != null && !patientDTO.getAddress1().isEmpty())
                mAddress1EditText.setText(patientDTO.getAddress1());
            if (patientDTO.getAddress2() != null && !patientDTO.getAddress2().isEmpty())
                mAddress2EditText.setText(patientDTO.getAddress2());
            mCountryName = String.valueOf(patientDTO.getCountry());
            int countryIndex = countryAdapter.getPosition(String.valueOf(patientDTO.getCountry()));
            if (countryIndex <= 0) {
                countryIndex = countryAdapter.getPosition(sessionManager.getAppLanguage().equals("en") ? "India" : "भारत");
                mCountryName = sessionManager.getAppLanguage().equals("en") ? "India" : "भारत";
            }
            mCountryNameSpinner.setSelection(countryIndex);
            if (patientDTO.getCityvillage() != null && !patientDTO.getCityvillage().isEmpty()) {
                String[] district_city = patientDTO.getCityvillage().trim().split(":");
                if (district_city.length == 2) {
                    district = mDistName = district_city[0];
                    city_village = mCityVillageName = district_city[1];
                    mCityVillageET.setText(city_village);
                }
            }

            if (mCountryName.equalsIgnoreCase(sessionManager.getAppLanguage().equals("en") ? "India" : "भारत")) {
                mIsIndiaSelected = true;
                Log.v(TAG, "setStateAdapter calling....344");
                //setStateAdapter(mCountryName);
                mStateNameEn = String.valueOf(patientDTO.getStateprovince());
                Log.v(TAG, "mStateName -" + mStateNameEn + "??");

            } else {
                mIsIndiaSelected = false;
                mStateEditText.setVisibility(View.VISIBLE);
                mStateNameSpinner.setVisibility(View.GONE);
                mStateEditText.setText(patientDTO.getStateprovince() != null ? String.valueOf(patientDTO.getStateprovince()) : "");
                mDistrictET.setVisibility(View.VISIBLE);
                mDistrictNameSpinner.setVisibility(View.GONE);
                mDistrictET.setText(String.valueOf(district));
            }

            /*if (patientDTO.getStateprovince() != null && patientDTO.getStateprovince().equalsIgnoreCase("Maharashtra")) {

            } else {
                if (patientDTO.getCityvillage() != null) {
                    mDistrictNameSpinner.setVisibility(View.GONE);
                    //mCityNameSpinner.setVisibility(View.GONE);
                    mDistrictET.setVisibility(View.VISIBLE);
                    mCityVillageET.setVisibility(View.VISIBLE);
                    String[] district_city = patientDTO.getCityvillage().trim().split(":");
                    district = district_city[0];
                    city_village = district_city[1];
                    mDistrictET.setText(district);
                    mCityVillageET.setText(city_village);
                }
            }*/
        }

        // Back Button click event.
        frag2_btn_back.setOnClickListener(v -> {
            onBackInsertIntoPatientDTO();
        });

        // Next Button click event.
        frag2_btn_next.setOnClickListener(v -> {
            onPatientCreateClicked();
        });

        /*mCityNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    String district = adapterView.getItemAtPosition(i).toString();
                    mCityNameErrorTextView.setVisibility(View.GONE);
                    mCityNameSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });*/
        // District based City - start
        mDistrictNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.v(TAG, "i - " + i);
                Log.v(TAG, "item - " + adapterView.getItemAtPosition(i));
                if (i != 0) {
                    String distName = adapterView.getItemAtPosition(i).toString();
//                    if (!distName.equalsIgnoreCase(mDistName))
//                        mCityVillageET.setText("");
                    mDistName = adapterView.getItemAtPosition(i).toString();
                    mDistNameEn = mLastSelectedDistList.get(i - 1).getName();
                    mDistrictNameErrorTextView.setVisibility(View.GONE);
                    mDistrictNameSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
                    mCityNameErrorTextView.setVisibility(View.GONE);
                    mCityVillageET.setBackgroundResource(R.drawable.bg_input_fieldnew);

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
                    Log.v(TAG, "onItemSelected - " + i);
                    mStateName = adapterView.getItemAtPosition(i).toString();
                    mStateNameEn = mLastSelectedStateList.get(i - 1).getState();
                    mStateNameErrorTextView.setVisibility(View.GONE);
                    mStateNameSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);

                    if (mCountryName.equalsIgnoreCase(sessionManager.getAppLanguage().equals("en") ? "India" : "भारत")) {

                        mDistrictET.setVisibility(View.GONE);
                        mDistrictNameSpinner.setVisibility(View.VISIBLE);
                        setDistAdapter(mStateNameEn);

                        if (fromThirdScreen || fromFirstScreen) {
                            int itemPosition = 0;
                            for (int k = 0; k < mLastSelectedDistList.size(); k++) {
                                if (mLastSelectedDistList.get(k).getName().equalsIgnoreCase(mDistName)) {
                                    itemPosition = k + 1;
                                    break;
                                }
                            }
                            //mDistrictNameSpinner.setSelection(districtAdapter.getPosition(String.valueOf(district)));
                            mDistrictNameSpinner.setSelection(itemPosition);
                        } else
                            mDistrictNameSpinner.setSelection(districtAdapter.getPosition(getResources().getString(R.string.select_spinner)));


                    } else {

                        mDistrictET.setVisibility(View.VISIBLE);
                        mDistrictNameSpinner.setVisibility(View.GONE);
                        if (fromThirdScreen || fromFirstScreen)
                            mDistrictET.setText(String.valueOf(district));
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

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // State based district - end

        // country - start
        mCountryNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    mCountryName = adapterView.getItemAtPosition(i).toString();
                    mCountryNameEn = mCountryList[i];
                    mCountryNameErrorTextView.setVisibility(View.GONE);
                    mCountryNameSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);

                    if (mCountryName.equalsIgnoreCase(sessionManager.getAppLanguage().equals("en") ? "India" : "भारत")) {
                        mIsIndiaSelected = true;
                        mStateEditText.setVisibility(View.GONE);
                        mStateNameSpinner.setVisibility(View.VISIBLE);
                        Log.v(TAG, "setStateAdapter calling....599");
                        setStateAdapter(mCountryName);

                        mDistrictET.setVisibility(View.GONE);
                        mDistrictNameSpinner.setVisibility(View.VISIBLE);

                        if (fromThirdScreen || fromFirstScreen) {
                            int itemPosition = 0;
                            for (int k = 0; k < mLastSelectedStateList.size(); k++) {

                                if (mLastSelectedStateList.get(k).getState().equalsIgnoreCase(String.valueOf(patientDTO.getStateprovince()))) {
                                    itemPosition = k + 1;
                                    break;
                                }
                            }
                            mStateNameSpinner.setSelection(itemPosition);
                        } else
                            mStateNameSpinner.setSelection(stateAdapter.getPosition(getResources().getString(R.string.select_spinner)));


                    } else {
                        mIsIndiaSelected = false;
                        mStateEditText.setVisibility(View.VISIBLE);
                        mStateNameSpinner.setVisibility(View.GONE);
                        if (fromThirdScreen || fromFirstScreen)
                            mStateEditText.setText(patientDTO.getStateprovince() != null ? String.valueOf(patientDTO.getStateprovince()) : "");

                        mDistrictET.setVisibility(View.VISIBLE);
                        mDistrictNameSpinner.setVisibility(View.GONE);
                    }

                }
                new SnackbarUtils().hideKeyboard(getActivity());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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
            if (this.editText.getId() == R.id.postalcode_edittext) {
                if (val.isEmpty()) {
                    mPostalCodeErrorTextView.setVisibility(View.GONE);
                    mPostalCodeEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            }
            if (this.editText.getId() == R.id.state_edittext) {
                if (val.isEmpty()) {
                    mStateNameErrorTextView.setVisibility(View.VISIBLE);
                    mStateNameErrorTextView.setText(getString(R.string.error_field_required));
                    editText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mStateNameErrorTextView.setVisibility(View.GONE);
                    editText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            } else if (this.editText.getId() == R.id.district_edittext) {
                if (val.isEmpty()) {
                    mDistrictNameErrorTextView.setVisibility(View.VISIBLE);
                    mDistrictNameErrorTextView.setText(getString(R.string.error_field_required));
                    editText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mDistrictNameErrorTextView.setVisibility(View.GONE);
                    editText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            } else if (this.editText.getId() == R.id.city_village_edittext) {
                if (val.isEmpty()) {
                    mCityNameErrorTextView.setVisibility(View.VISIBLE);
                    mCityNameErrorTextView.setText(getString(R.string.error_field_required));
                    editText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mCityNameErrorTextView.setVisibility(View.GONE);
                    editText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            }
        }
    }

    private boolean mIsIndiaSelected = true;
    private String mCountryName = "", mStateName = "", mDistName = "", mCityVillageName = "";
    private String mCountryNameEn = "", mStateNameEn = "", mDistNameEn = "", mCityVillageNameEn = "";
    private String[] mCountryList = null;
    private List<StateData> mLastSelectedStateList = new ArrayList<>();
    private List<DistData> mLastSelectedDistList = new ArrayList<>();

    private void setStateAdapter(String countryName) {
        Log.v(TAG, "setStateAdapter");
        mLastSelectedStateList = mStateDistMaster.getStateDataList();
        String[] stateList = new String[mStateDistMaster.getStateDataList().size() + 1];
        stateList[0] = getResources().getString(R.string.select_spinner);
        for (int i = 1; i <= mStateDistMaster.getStateDataList().size(); i++) {
            stateList[i] = sessionManager.getAppLanguage().equals("en") ? mStateDistMaster.getStateDataList().get(i - 1).getState() : mStateDistMaster.getStateDataList().get(i - 1).getStateHindi();
        }

        stateAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.simple_spinner_item_1, stateList);
        stateAdapter.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);

        mStateNameSpinner.setAdapter(stateAdapter);
        mStateNameSpinner.setPopupBackgroundDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.popup_menu_background));
        mStateNameSpinner.setSelection(1);
    }

    private void setDistAdapter(String stateName) {

        Log.v(TAG, "setDistAdapter - " + stateName);
        List<DistData> distDataList = new ArrayList<>();

        for (int i = 0; i < mStateDistMaster.getStateDataList().size(); i++) {
            //String sName = sessionManager.getAppLanguage().equals("en") ? mStateDistMaster.getStateDataList().get(i).getState() : mStateDistMaster.getStateDataList().get(i).getStateHindi();
            String sName = mStateDistMaster.getStateDataList().get(i).getState();
            if (sName.equalsIgnoreCase(stateName)) {
                distDataList = mStateDistMaster.getStateDataList().get(i).getDistDataList();
                break;
            }
        }
        mLastSelectedDistList = distDataList;

        String[] distList = new String[distDataList.size() + 1];
        distList[0] = getResources().getString(R.string.select_spinner);
        for (int i = 1; i <= distDataList.size(); i++) {
            distList[i] = sessionManager.getAppLanguage().equals("en") ? distDataList.get(i - 1).getName() : distDataList.get(i - 1).getNameHindi();
            //Log.v(TAG, "distList[i] - " + distList[i]);
        }

        districtAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.simple_spinner_item_1, distList);
        districtAdapter.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);

        mDistrictNameSpinner.setAdapter(districtAdapter);
        mDistrictNameSpinner.setPopupBackgroundDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.popup_menu_background));
    }

    private void onBackInsertIntoPatientDTO() {

        mStateName = mStateEditText.getText().toString().trim();
        mDistName = mDistrictET.getText().toString().trim();

        mCityVillageName = mCityVillageET.getText().toString().trim();

        patientDTO.setPostalcode(mPostalCodeEditText.getText().toString());
        patientDTO.setCountry(StringUtils.getValue(mCountryNameSpinner.getSelectedItem().toString()));
        patientDTO.setStateprovince(StringUtils.getValue(mIsIndiaSelected ? mStateNameSpinner.getSelectedItem().toString() : mStateName));
        if (mDistrictNameSpinner != null && mDistrictNameSpinner.getSelectedItem() != null)
            patientDTO.setCityvillage(StringUtils.getValue((mIsIndiaSelected ? mDistrictNameSpinner.getSelectedItem().toString() : mDistName) + ":" + mCityVillageName));
        if (!sessionManager.getAppLanguage().equals("en")) {
            patientDTO.setCountry(StringUtils.getValue(mCountryNameEn));
            patientDTO.setStateprovince(StringUtils.getValue(mIsIndiaSelected ? mStateNameEn : mStateName));

            patientDTO.setCityvillage(StringUtils.getValue((mIsIndiaSelected ? mDistNameEn : mDistName) + ":" + mCityVillageName));

        }
        patientDTO.setAddress1(mAddress1EditText.getText().toString());
        patientDTO.setAddress2(mAddress2EditText.getText().toString());

        Log.v("fragmemt_2", "values: " + new Gson().toJson(patientDTO));

        Bundle bundle = new Bundle();
        bundle.putSerializable("patientDTO", (Serializable) patientDTO);
        bundle.putBoolean("fromSecondScreen", true);
        bundle.putBoolean("patient_detail", patient_detail);
        firstScreen.setArguments(bundle); // passing data to Fragment
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_firstscreen, firstScreen)
                .commit();
    }

    private void onPatientCreateClicked() {
        Gson gson = new Gson();
        boolean cancel = false;
        View focusView = null;
        if (!mPostalCodeEditText.getText().toString().equals("")) {
            if (mCountryNameSpinner.getSelectedItem().toString().equalsIgnoreCase(sessionManager.getAppLanguage().equals("en") ? "India" : "भारत") && mPostalCodeEditText.getText().toString().trim().length() != 6) {
                mPostalCodeErrorTextView.setVisibility(View.VISIBLE);
                mPostalCodeErrorTextView.setText(getString(R.string.postal_code_6_dig_invalid_txt));
                mPostalCodeEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                mPostalCodeEditText.requestFocus();
                return;
            } else {
                mPostalCodeErrorTextView.setVisibility(View.GONE);
                mPostalCodeEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
            }
        } else {
            mPostalCodeErrorTextView.setVisibility(View.GONE);
            mPostalCodeEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }

        if (mCountryNameSpinner.getSelectedItemPosition() == 0) {
            mCountryNameErrorTextView.setVisibility(View.VISIBLE);
            mCountryNameErrorTextView.setText(getString(R.string.error_field_required));
            mCountryNameSpinner.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mCountryNameSpinner.requestFocus();
            return;
        } else {
            mCountryNameErrorTextView.setVisibility(View.GONE);
            mCountryNameSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
        }

        if (mStateNameSpinner.getSelectedItemPosition() == 0) {
            mStateNameErrorTextView.setVisibility(View.VISIBLE);
            mStateNameErrorTextView.setText(getString(R.string.error_field_required));
            mStateNameSpinner.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mStateNameSpinner.requestFocus();
            return;
        } else {
            mStateNameErrorTextView.setVisibility(View.GONE);
            mStateNameSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
        }

        if (mDistrictNameSpinner.getVisibility() == View.VISIBLE && mDistrictNameSpinner.getSelectedItemPosition() == 0) {
            mDistrictNameErrorTextView.setVisibility(View.VISIBLE);
            mDistrictNameErrorTextView.setText(getString(R.string.error_field_required));
            mDistrictNameSpinner.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mDistrictNameSpinner.requestFocus();
            return;
        } else {
            mDistrictNameErrorTextView.setVisibility(View.GONE);
            mDistrictNameSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
        }

        if (mDistrictET.getVisibility() == View.VISIBLE && mDistrictET.getText().toString().equals("")) {
            mDistrictNameErrorTextView.setVisibility(View.VISIBLE);
            mDistrictNameErrorTextView.setText(getString(R.string.error_field_required));
            mDistrictET.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mDistrictET.requestFocus();
            return;
        } else {
            mDistrictNameErrorTextView.setVisibility(View.GONE);
            mDistrictET.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }

        if (mCityVillageET.getText().toString().isEmpty()) {
            mCityNameErrorTextView.setVisibility(View.VISIBLE);
            mCityNameErrorTextView.setText(getString(R.string.error_field_required));
            mCityVillageET.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mCityVillageET.requestFocus();
            return;
        } else if (mCityVillageET.getText().toString().length() < 3) {
            mCityNameErrorTextView.setVisibility(View.VISIBLE);
            mCityNameErrorTextView.setText(getString(R.string.error_field_valid_village_required));
            mCityVillageET.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mCityVillageET.requestFocus();
            return;
        } else {
            mCityNameErrorTextView.setVisibility(View.GONE);
            mCityVillageET.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }

        /**
         *  entering value in dataset start
         */
        if (cancel) {
            focusView.requestFocus();
        } else {
            mStateName = mStateEditText.getText().toString().trim();
            mDistName = mDistrictET.getText().toString().trim();
            mCityVillageName = mCityVillageET.getText().toString().trim();

            patientDTO.setPostalcode(mPostalCodeEditText.getText().toString());
            patientDTO.setCountry(StringUtils.getValue(mCountryNameSpinner.getSelectedItem().toString()));
            patientDTO.setStateprovince(StringUtils.getValue(mIsIndiaSelected ? mStateNameSpinner.getSelectedItem().toString() : mStateName));
            if (mDistrictNameSpinner != null && mDistrictNameSpinner.getSelectedItem() != null)
                patientDTO.setCityvillage(StringUtils.getValue((mIsIndiaSelected ? mDistrictNameSpinner.getSelectedItem().toString() : mDistName) + ":" + mCityVillageName));

            if (!sessionManager.getAppLanguage().equals("en")) {
                patientDTO.setCountry(StringUtils.getValue(mCountryNameEn));
                patientDTO.setStateprovince(StringUtils.getValue(mIsIndiaSelected ? mStateNameEn : mStateName));
                patientDTO.setCityvillage(StringUtils.getValue((mIsIndiaSelected ? mDistNameEn : mDistName) + ":" + mCityVillageName));

            }
            patientDTO.setAddress1(mAddress1EditText.getText().toString());
            patientDTO.setAddress2(mAddress2EditText.getText().toString());
        }


        try {
            Logger.logD(TAG, "insertpatinet");
            boolean isPatientInserted = false;
            boolean isPatientImageInserted = false;
            PatientsDAO patientsDAO = new PatientsDAO();
            PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
            List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();
            ImagesDAO imagesDAO = new ImagesDAO();

            if (patient_detail) {
                isPatientInserted = patientsDAO.updatePatientToDB_PatientDTO(patientDTO, patientDTO.getUuid(), patientAttributesDTOList);
                isPatientImageInserted = imagesDAO.updatePatientProfileImages(patientDTO.getPatientPhoto(), patientDTO.getUuid());
            } else {
                // Bundle data
                Bundle bundle = new Bundle();
                bundle.putSerializable("patientDTO", (Serializable) patientDTO);
                bundle.putBoolean("fromSecondScreen", true);
                //   bundle.putString("patientUuid", patientID_edit);
                bundle.putBoolean("patient_detail", patient_detail);
                fragment_thirdScreen.setArguments(bundle); // passing data to Fragment

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_firstscreen, fragment_thirdScreen)
                        .commit();
            }

            if (NetworkConnection.isOnline(getActivity().getApplication())) { // todo: uncomment later jsut for testing added.
                SyncDAO syncDAO = new SyncDAO();
                ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
                boolean push = syncDAO.pushDataApi();
                boolean pushImage = imagesPushDAO.patientProfileImagesPush();
            }

            if (isPatientInserted && isPatientImageInserted) {
                Logger.logD(TAG, "inserted");
                Intent intent = new Intent(getActivity().getApplication(), PatientDetailActivity2.class);
                intent.putExtra("patientUuid", patientDTO.getUuid());
                intent.putExtra("patientName", patientDTO.getFirstname() + " " + patientDTO.getLastname());
                intent.putExtra("tag", "newPatient");
                intent.putExtra("hasPrescription", "false");
                Bundle args = new Bundle();
                args.putSerializable("patientDTO", (Serializable) patientDTO);
                intent.putExtra("BUNDLE", args);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    InputFilter lettersFilter = (source, start, end, dest, dStart, dEnd) -> {
        for (int i = start; i < end; i++) {
            if (!Character.isLetterOrDigit(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
                return ""; // Block non-letter and non-digit characters.
            }
        }
        return null; // Accept the original characters.
    };

    public static InputFilter inputFilter_Others = new InputFilter() { //filter input for all other fields
        @Override
        public CharSequence filter(CharSequence charSequence, int start, int end, Spanned spanned, int i2, int i3) {
            boolean keepOriginal = true;
            StringBuilder sb = new StringBuilder(end - start);
            for (int i = start; i < end; i++) {
                char c = charSequence.charAt(i);
                if (isCharAllowed(c)) // put your condition here
                    sb.append(c);
                else if (c=='.' || c=='&' || c=='(' || c==')')
                    sb.append(c);
                else
                    keepOriginal = false;
            }
            if (keepOriginal)
                return null;
            else {
                if (charSequence instanceof Spanned) {
                    SpannableString sp = new SpannableString(sb);
                    TextUtils.copySpansFrom((Spanned) charSequence, start, sb.length(), null, sp, 0);
                    return sp;
                } else {
                    return sb;
                }
            }
        }

        private boolean isCharAllowed(char c) {
            return Character.isLetterOrDigit(c) || Character.isSpaceChar(c);   // This allows only alphabets, digits and spaces.
        }
    };

}

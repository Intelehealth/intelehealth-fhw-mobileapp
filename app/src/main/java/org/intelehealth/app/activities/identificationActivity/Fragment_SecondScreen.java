package org.intelehealth.app.activities.identificationActivity;

import static org.intelehealth.app.utilities.StringUtils.inputFilter_Name;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
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
import androidx.fragment.app.Fragment;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.SnackbarUtils;
import org.intelehealth.app.utilities.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
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
    private Spinner mCountryNameSpinner, mStateNameSpinner, mDistrictNameSpinner, mCityNameSpinner;
    Context context;
    private String country1, state;
    ArrayAdapter<String> districtAdapter, cityAdapter;

    private PatientDTO patientDTO;
    private Fragment_ThirdScreen fragment_thirdScreen;
    private Fragment_FirstScreen firstScreen;
    private TextView mPostalCodeErrorTextView, mCountryNameErrorTextView, mStateNameErrorTextView, mDistrictNameErrorTextView, mCityNameErrorTextView, mAddress1ErrorTextView, mAddress2ErrorTextView;
    boolean fromThirdScreen = false, fromFirstScreen = false;
    String district;
    String city_village;
    String patientID_edit;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_registration_secondscreen, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getActivity();
        sessionManager = new SessionManager(getActivity());

        personal_icon = getActivity().findViewById(R.id.addpatient_icon);
        address_icon = getActivity().findViewById(R.id.addresslocation_icon);
        other_icon = getActivity().findViewById(R.id.other_icon);
        frag2_btn_back = getActivity().findViewById(R.id.frag2_btn_back);
        frag2_btn_next = getActivity().findViewById(R.id.frag2_btn_next);

        mPostalCodeEditText = view.findViewById(R.id.postalcode_edittext);
        mCountryNameSpinner = view.findViewById(R.id.country_spinner);
        mStateNameSpinner = view.findViewById(R.id.state_spinner);
        mDistrictNameSpinner = view.findViewById(R.id.district_spinner);
        mCityNameSpinner = view.findViewById(R.id.city_spinner);
        mAddress1EditText = view.findViewById(R.id.address1_edittext);
        mAddress1EditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50), inputFilter_Name}); //maxlength 50

        mAddress2EditText = view.findViewById(R.id.address2_edittext);
        mAddress2EditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50), inputFilter_Name}); //maxlength 50


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
                    mPostalCodeErrorTextView.setVisibility(View.VISIBLE);
                    mPostalCodeErrorTextView.setText(getString(R.string.error_field_required));
                    mPostalCodeEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mPostalCodeErrorTextView.setVisibility(View.GONE);
                    mPostalCodeEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            } else if (this.editText.getId() == R.id.address1_edittext) {
                if (val.isEmpty()) {
                    mAddress1ErrorTextView.setVisibility(View.VISIBLE);
                    mAddress1ErrorTextView.setText(getString(R.string.error_field_required));
                    mAddress1EditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mAddress1ErrorTextView.setVisibility(View.GONE);
                    mAddress1EditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            } /*else if (this.editText.getId() == R.id.address2_edittext) {
                if (val.isEmpty()) {
                    mAddress2ErrorTextView.setVisibility(View.VISIBLE);
                    mAddress2ErrorTextView.setText(getString(R.string.error_field_required));
                    mAddress2EditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mAddress2ErrorTextView.setVisibility(View.GONE);
                    mAddress2EditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            }*/
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        firstScreen = new Fragment_FirstScreen();
        fragment_thirdScreen = new Fragment_ThirdScreen();
        personal_icon.setImageDrawable(getResources().getDrawable(R.drawable.addpatient_icon_done));
        address_icon.setImageDrawable(getResources().getDrawable(R.drawable.addresslocation_icon));
        other_icon.setImageDrawable(getResources().getDrawable(R.drawable.other_icon_unselected));

        if (getArguments() != null) {
            patientDTO = (PatientDTO) getArguments().getSerializable("patientDTO");
            fromThirdScreen = getArguments().getBoolean("fromThirdScreen");
            fromFirstScreen = getArguments().getBoolean("fromFirstScreen");
            patientID_edit = getArguments().getString("patientUuid");

            if (patientID_edit != null) {
                patientDTO.setUuid(patientID_edit);
            } else {
               // do nothing...
            }
        }

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
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.countries));
//        country_spinner.setSelection(countryAdapter.getPosition(country1));
        mCountryNameSpinner.setAdapter(countryAdapter); // keeping this is setting textcolor to white so comment this and add android:entries in xml

       /* ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.states_india, android.R.layout.simple_spinner_dropdown_item);
        state_spinner.setSelection(stateAdapter.getPosition(state));
*/

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

        // Setting up the screen when user came from SEcond screen.
        if (fromThirdScreen || fromFirstScreen) {
            if (patientDTO.getPostalcode() != null && !patientDTO.getPostalcode().isEmpty())
                mPostalCodeEditText.setText(patientDTO.getPostalcode());
            if (patientDTO.getAddress1() != null && !patientDTO.getAddress1().isEmpty())
                mAddress1EditText.setText(patientDTO.getAddress1());
            if (patientDTO.getAddress2() != null && !patientDTO.getAddress2().isEmpty())
                mAddress2EditText.setText(patientDTO.getAddress2());

            mCountryNameSpinner.setSelection(countryAdapter.getPosition(String.valueOf(patientDTO.getCountry())));
            mStateNameSpinner.setSelection(stateAdapter.getPosition(String.valueOf(patientDTO.getStateprovince())));
            if (patientDTO.getCityvillage() != null) {
                String[] district_city = patientDTO.getCityvillage().trim().split(":");
                district = district_city[0];
                city_village = district_city[1];
                mDistrictNameSpinner.setSelection(districtAdapter.getPosition(district));
                mCityNameSpinner.setSelection(cityAdapter.getPosition(city_village));
            }
        }

        // Back Button click event.
        frag2_btn_back.setOnClickListener(v -> {
            onBackInsertIntoPatientDTO();
        });

        // Next Button click event.
        frag2_btn_next.setOnClickListener(v -> {
            onPatientCreateClicked();
        });

        mCityNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        });
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
        // State based district - end

        // country - start
        mCountryNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    String country = adapterView.getItemAtPosition(i).toString();
                    mCountryNameErrorTextView.setVisibility(View.GONE);
                    mCountryNameSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
                    if (country.matches("India")) {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(getActivity(),
                                R.array.states_india, android.R.layout.simple_spinner_dropdown_item);
                        mStateNameSpinner.setAdapter(stateAdapter);

                        // setting state according database when user clicks edit details
                        if (fromThirdScreen || fromFirstScreen)
                            mStateNameSpinner.setSelection(stateAdapter.getPosition(String.valueOf(patientDTO.getStateprovince())));
                        else
                            mStateNameSpinner.setSelection(stateAdapter.getPosition("Select"));

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

        // country - end

    }

    private void onBackInsertIntoPatientDTO() {
        patientDTO.setPostalcode(mPostalCodeEditText.getText().toString());
        patientDTO.setCountry(StringUtils.getValue(mCountryNameSpinner.getSelectedItem().toString()));
        patientDTO.setStateprovince(StringUtils.getValue(mStateNameSpinner.getSelectedItem().toString()));
        patientDTO.setCityvillage(StringUtils.getValue(mDistrictNameSpinner.getSelectedItem().toString() +
                ":" + mCityNameSpinner.getSelectedItem().toString()));
        patientDTO.setAddress1(mAddress1EditText.getText().toString());
        patientDTO.setAddress2(mAddress2EditText.getText().toString());

        Log.v("fragmemt_2", "values: " + mCountryNameSpinner.getSelectedItem().toString()
                + "\n" + mStateNameSpinner.getSelectedItem().toString()
                + "\n" + mDistrictNameSpinner.getSelectedItem().toString()
                + "\n" + mCityNameSpinner.getSelectedItem().toString()
                + "\n" + mAddress1EditText.getText().toString()
                + "\n" + mAddress2EditText.getText().toString());

        Bundle bundle = new Bundle();
        bundle.putSerializable("patientDTO", (Serializable) patientDTO);
        bundle.putBoolean("fromSecondScreen", true);
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

        // validation - start
        if (mPostalCodeEditText.getText().toString().equals("")) {
            mPostalCodeErrorTextView.setVisibility(View.VISIBLE);
            mPostalCodeErrorTextView.setText(getString(R.string.error_field_required));
            mPostalCodeEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mPostalCodeEditText.requestFocus();
            return;
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

        if (mDistrictNameSpinner.getSelectedItemPosition() == 0) {
            mDistrictNameErrorTextView.setVisibility(View.VISIBLE);
            mDistrictNameErrorTextView.setText(getString(R.string.error_field_required));
            mDistrictNameSpinner.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mDistrictNameSpinner.requestFocus();
            return;
        } else {
            mDistrictNameErrorTextView.setVisibility(View.GONE);
            mDistrictNameSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
        }

        if (mCityNameSpinner.getSelectedItemPosition() == 0) {
            mCityNameErrorTextView.setVisibility(View.VISIBLE);
            mCityNameErrorTextView.setText(getString(R.string.error_field_required));
            mCityNameSpinner.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mCityNameSpinner.requestFocus();
            return;
        } else {
            mCityNameErrorTextView.setVisibility(View.GONE);
            mCityNameSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
        }

        if (mAddress1EditText.getText().toString().equals("")) {
            mAddress1ErrorTextView.setVisibility(View.VISIBLE);
            mAddress1ErrorTextView.setText(getString(R.string.error_field_required));
            mAddress1EditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mAddress1EditText.requestFocus();
            return;
        } else {
            mAddress1ErrorTextView.setVisibility(View.GONE);
            mAddress1EditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }

        /*if (mAddress2EditText.getText().toString().equals("")) {
            mAddress2ErrorTextView.setVisibility(View.VISIBLE);
            mAddress2ErrorTextView.setText(getString(R.string.error_field_required));
            mAddress2EditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mAddress2EditText.requestFocus();
            return;
        } else {
            mAddress2ErrorTextView.setVisibility(View.GONE);
            mAddress2EditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }*/
        // validation - end

        /**
         *  entering value in dataset start
         */
        if (cancel) {
            focusView.requestFocus();
        } else {
            patientDTO.setPostalcode(mPostalCodeEditText.getText().toString());
            patientDTO.setCountry(StringUtils.getValue(mCountryNameSpinner.getSelectedItem().toString()));
            patientDTO.setStateprovince(StringUtils.getValue(mStateNameSpinner.getSelectedItem().toString()));
            patientDTO.setCityvillage(StringUtils.getValue(mDistrictNameSpinner.getSelectedItem().toString() +
                    ":" + mCityNameSpinner.getSelectedItem().toString()));
            patientDTO.setAddress1(mAddress1EditText.getText().toString());
            patientDTO.setAddress2(mAddress2EditText.getText().toString());

            Log.v("fragmemt_2", "values: " + mCountryNameSpinner.getSelectedItem().toString()
                    + "\n" + mStateNameSpinner.getSelectedItem().toString()
                    + "\n" + mDistrictNameSpinner.getSelectedItem().toString()
                    + "\n" + mCityNameSpinner.getSelectedItem().toString()
                    + "\n" + mAddress1EditText.getText().toString()
                    + "\n" + mAddress2EditText.getText().toString());
        }

        // Bundle data
        Bundle bundle = new Bundle();
        bundle.putSerializable("patientDTO", (Serializable) patientDTO);
        bundle.putBoolean("fromSecondScreen", true);
        bundle.putString("patientUuid", patientID_edit);
        fragment_thirdScreen.setArguments(bundle); // passing data to Fragment

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_firstscreen, fragment_thirdScreen)
                .commit();
    }
}

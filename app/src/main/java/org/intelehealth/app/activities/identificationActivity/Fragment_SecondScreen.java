package org.intelehealth.app.activities.identificationActivity;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
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
import org.intelehealth.app.utilities.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;
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
    private EditText postalcode_edittext, address1_edittext, address2_edittext;
    private Spinner country_spinner, state_spinner, district_spinner, city_spinner;
    Context context;
    private String country1, state;

    private PatientDTO patientDTO;
    private Fragment_ThirdScreen fragment_thirdScreen;
    private TextView postalcode_error, country_error, state_error, district_error, city_error, address1_error, address2_error;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_registration_secondscreen, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = Fragment_SecondScreen.this.context;
        sessionManager = new SessionManager(getActivity());

        if (getArguments() != null)
            patientDTO = (PatientDTO) getArguments().getSerializable("patientDTO");

        personal_icon = getActivity().findViewById(R.id.addpatient_icon);
        address_icon = getActivity().findViewById(R.id.addresslocation_icon);
        other_icon = getActivity().findViewById(R.id.other_icon);
        frag2_btn_back = getActivity().findViewById(R.id.frag2_btn_back);
        frag2_btn_next = getActivity().findViewById(R.id.frag2_btn_next);

        postalcode_edittext = view.findViewById(R.id.postalcode_edittext);
        country_spinner = view.findViewById(R.id.country_spinner);
        state_spinner = view.findViewById(R.id.state_spinner);
        district_spinner = view.findViewById(R.id.district_spinner);
        city_spinner = view.findViewById(R.id.city_spinner);
        address1_edittext = view.findViewById(R.id.address1_edittext);
        address2_edittext = view.findViewById(R.id.address2_edittext);

        postalcode_error = view.findViewById(R.id.postalcode_error);
        country_error = view.findViewById(R.id.country_error);
        state_error = view.findViewById(R.id.state_error);
        district_error = view.findViewById(R.id.district_error);
        city_error = view.findViewById(R.id.city_error);
        address1_error = view.findViewById(R.id.address1_error);
        address2_error = view.findViewById(R.id.address2_error);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fragment_thirdScreen = new Fragment_ThirdScreen();
        personal_icon.setImageDrawable(getResources().getDrawable(R.drawable.addpatient_icon_done));
        address_icon.setImageDrawable(getResources().getDrawable(R.drawable.addresslocation_icon));
        other_icon.setImageDrawable(getResources().getDrawable(R.drawable.other_icon_unselected));

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
        }
        catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
//            Issue #627
//            added the catch exception to check the config and throwing back to setup activity
            Toast.makeText(getActivity(), "JsonException" + e, Toast.LENGTH_LONG).show();
            //  showAlertDialogButtonClicked(e.toString());
        }

        Resources res = getResources();
        ArrayAdapter<CharSequence> countryAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.countries, R.layout.custom_spinner);
        country_spinner.setSelection(countryAdapter.getPosition(country1));
     //   country_spinner.setAdapter(countryAdapter); // keeping this is setting textcolor to white so comment this and add android:entries in xml

        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.states_india, R.layout.custom_spinner);
        state_spinner.setSelection(stateAdapter.getPosition(state));


        // Back Button click event.
        frag2_btn_back.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            //   bundle.putString("");
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_firstscreen, new Fragment_FirstScreen())
                    .commit();
        });

        // Next Button click event.
        frag2_btn_next.setOnClickListener(v -> {
            onPatientCreateClicked();
        });

        // District based City - start
        district_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    String district = adapterView.getItemAtPosition(i).toString();

                    if (district.matches("Navi Mumbai")) {
                        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(getActivity(),
                                R.array.city, R.layout.custom_spinner);
                        city_spinner.setAdapter(cityAdapter);
                        // setting state according database when user clicks edit details

                     /*   if (patientID_edit != null) // TODO: uncomment
                            state_spinner.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
                        else*/
                        city_spinner.setSelection(cityAdapter.getPosition(district));

//                    } else if (state.matches("United States")) {
//                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(getActivity(),
//                                R.array.states_us, R.layout.custom_spinner);
//                        // stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        state_spinner.setAdapter(stateAdapter);
//
///*                        if (patientID_edit != null) { // TODO: uncomment
//                            state_spinner.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
//                        }*/
//                    } else if (country.matches("Philippines")) {
//                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(getActivity(),
//                                R.array.states_philippines, R.layout.custom_spinner);
//                        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        state_spinner.setAdapter(stateAdapter);
//
//                        /*if (patientID_edit != null) { // TODO: uncomment
//                            state_spinner.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
//                        } else {*/
//                        state_spinner.setSelection(stateAdapter.getPosition("Bukidnon"));
//                        //  } // TODO: uncomment
//

                    } else {
//                        ArrayAdapter<CharSequence> districtAdapter = ArrayAdapter.createFromResource(getActivity(),
//                                R.array.district, R.layout.custom_spinner);
//                        // stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        state_spinner.setAdapter(stateAdapter);
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // District based city - end


        // district based  state - start
        state_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    String state = adapterView.getItemAtPosition(i).toString();

                    if (state.matches("Maharashtra")) {
                        ArrayAdapter<CharSequence> districtAdapter = ArrayAdapter.createFromResource(getActivity(),
                                R.array.district, R.layout.custom_spinner);
                        district_spinner.setAdapter(districtAdapter);
                        // setting state according database when user clicks edit details

                     /*   if (patientID_edit != null) // TODO: uncomment
                            state_spinner.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
                        else*/
                        district_spinner.setSelection(districtAdapter.getPosition(state));

//                    } else if (state.matches("United States")) {
//                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(getActivity(),
//                                R.array.states_us, R.layout.custom_spinner);
//                        // stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        state_spinner.setAdapter(stateAdapter);
//
///*                        if (patientID_edit != null) { // TODO: uncomment
//                            state_spinner.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
//                        }*/
//                    } else if (country.matches("Philippines")) {
//                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(getActivity(),
//                                R.array.states_philippines, R.layout.custom_spinner);
//                        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        state_spinner.setAdapter(stateAdapter);
//
//                        /*if (patientID_edit != null) { // TODO: uncomment
//                            state_spinner.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
//                        } else {*/
//                        state_spinner.setSelection(stateAdapter.getPosition("Bukidnon"));
//                        //  } // TODO: uncomment
//

                    } else {
//                        ArrayAdapter<CharSequence> districtAdapter = ArrayAdapter.createFromResource(getActivity(),
//                                R.array.district, R.layout.custom_spinner);
//                        // stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        state_spinner.setAdapter(stateAdapter);
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // State based district - end

        // country - start
        country_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    String country = adapterView.getItemAtPosition(i).toString();

                    if (country.matches("India")) {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(getActivity(),
                                R.array.states_india, R.layout.custom_spinner);
                        // stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        state_spinner.setAdapter(stateAdapter);
                        // setting state according database when user clicks edit details

                     /*   if (patientID_edit != null) // TODO: uncomment
                            state_spinner.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
                        else*/
                            state_spinner.setSelection(stateAdapter.getPosition(state));

                    } else if (country.matches("United States")) {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(getActivity(),
                                R.array.states_us, R.layout.custom_spinner);
                        // stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        state_spinner.setAdapter(stateAdapter);

/*                        if (patientID_edit != null) { // TODO: uncomment
                            state_spinner.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
                        }*/
                    } else if (country.matches("Philippines")) {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(getActivity(),
                                R.array.states_philippines, R.layout.custom_spinner);
                        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        state_spinner.setAdapter(stateAdapter);

                        /*if (patientID_edit != null) { // TODO: uncomment
                            state_spinner.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
                        } else {*/
                            state_spinner.setSelection(stateAdapter.getPosition("Bukidnon"));
                      //  } // TODO: uncomment

                    } else {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(getActivity(),
                                R.array.state_error, R.layout.custom_spinner);
                        // stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        state_spinner.setAdapter(stateAdapter);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // country - end

    }


    private void onPatientCreateClicked() {
        Gson gson = new Gson();
        boolean cancel = false;
        View focusView = null;

        // validation - start
        if (postalcode_edittext.getText().toString().equals("")) {
            postalcode_error.setVisibility(View.VISIBLE);
            return;
        }
        else {
            postalcode_error.setVisibility(View.GONE);
        }

        if (country_spinner.getSelectedItemPosition() == 0) {
            country_error.setVisibility(View.VISIBLE);
            return;
        }
        else {
            country_error.setVisibility(View.GONE);
        }

        if (state_spinner.getSelectedItemPosition() == 0) {
            state_error.setVisibility(View.VISIBLE);
            return;
        }
        else {
            state_error.setVisibility(View.GONE);
        }

        if (district_spinner.getSelectedItemPosition() == 0) {
            district_error.setVisibility(View.VISIBLE);
            return;
        }
        else {
            district_error.setVisibility(View.GONE);
        }

        if (city_spinner.getSelectedItemPosition() == 0) {
            city_error.setVisibility(View.VISIBLE);
            return;
        }
        else {
            city_error.setVisibility(View.GONE);
        }

        if (address1_edittext.getText().toString().equals("")) {
            address1_error.setVisibility(View.VISIBLE);
            return;
        }
        else {
            address1_error.setVisibility(View.GONE);
        }

        if (address2_edittext.getText().toString().equals("")) {
            address2_error.setVisibility(View.VISIBLE);
            return;
        }
        else {
            address2_error.setVisibility(View.GONE);
        }
        // validation - end

        /**
         *  entering value in dataset start
         */
        if (cancel) {
            focusView.requestFocus();
        } else {
            patientDTO.setPostalcode(postalcode_edittext.getText().toString());
            patientDTO.setCountry(StringUtils.getValue(country_spinner.getSelectedItem().toString()));
            patientDTO.setStateprovince(StringUtils.getValue(state_spinner.getSelectedItem().toString()));
            patientDTO.setCityvillage(StringUtils.getValue(district_spinner.getSelectedItem().toString() +
                    ":" + city_spinner.getSelectedItem().toString()));
            patientDTO.setAddress1(address1_edittext.getText().toString());
            patientDTO.setAddress2(address2_edittext.getText().toString());

            Log.v("fragmemt_2", "values: " + country_spinner.getSelectedItem().toString()
                    + "\n" + state_spinner.getSelectedItem().toString()
                    + "\n" + district_spinner.getSelectedItem().toString()
                    + "\n" + city_spinner.getSelectedItem().toString()
                    + "\n" + address1_edittext.getText().toString()
                    + "\n" + address2_edittext.getText().toString());
        }

        // Bundle data
        Bundle bundle = new Bundle();
        bundle.putSerializable("patientDTO", (Serializable) patientDTO);
        fragment_thirdScreen.setArguments(bundle); // passing data to Fragment

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_firstscreen, fragment_thirdScreen)
                .commit();
    }
}

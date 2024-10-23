package org.intelehealth.app.activities.identificationActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.ajalt.timberkt.Timber;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.identificationActivity.model.Block;
import org.intelehealth.app.activities.identificationActivity.model.DistData;
import org.intelehealth.app.activities.identificationActivity.model.GramPanchayat;
import org.intelehealth.app.activities.identificationActivity.model.StateData;
import org.intelehealth.app.activities.identificationActivity.model.StateDistMaster;
import org.intelehealth.app.activities.identificationActivity.model.Village;
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ImagesPushDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.ui.listener.DefaultOnItemSelectedListener;
import org.intelehealth.app.utilities.BundleKeys;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.PatientRegConfigKeys;
import org.intelehealth.app.utilities.PatientRegFieldsUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.SnackbarUtils;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.config.presenter.fields.data.RegFieldRepository;
import org.intelehealth.config.presenter.fields.factory.RegFieldViewModelFactory;
import org.intelehealth.config.presenter.fields.viewmodel.RegFieldViewModel;
import org.intelehealth.config.room.ConfigDatabase;
import org.intelehealth.config.room.entity.PatientRegistrationFields;
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
    private Spinner mCountryNameSpinner, mStateNameSpinner, mDistrictNameSpinner, mBlockSpinner, mGramPanchayatSpinner, mVillageSpinner/*, mCityNameSpinner*/; // now city always an input field not spinner
    Context context;
    private String country1, state;
    ArrayAdapter<String> districtAdapter, stateAdapter;
    ArrayAdapter<CharSequence> countryAdapter;
    EditText mDistrictET;
    //            mCityVillageET;
    private PatientDTO patientDTO;
    private Fragment_ThirdScreen fragment_thirdScreen;
    private Fragment_FirstScreen firstScreen;
    private TextView mPostalCodeErrorTextView, mCountryNameErrorTextView, mStateNameErrorTextView, mDistrictNameErrorTextView, mCityNameErrorTextView, mAddress1ErrorTextView, mAddress2ErrorTextView, postalCodeTv, countryTv, stateTv, districtTv, villTownCityTv, address1Tv, address2Tv, blockError, gramPanchatError;

    LinearLayout postalCodeLay, countryLay, stateLay, districtLay, villTownCityLay, address1Lay, address2Lay;
    boolean fromThirdScreen = false, fromFirstScreen = false;
    String district;
    String city_village;
    String patientID_edit;
    boolean patient_detail = false;
    private StateDistMaster mStateDistMaster;
    private EditText mStateEditText;

    RegFieldViewModel regFieldViewModel;

    List<PatientRegistrationFields> patientRegistrationFields;
    private boolean isEditMode = false;

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

        //config viewmodel initialization
        RegFieldRepository repository = new RegFieldRepository(ConfigDatabase.getInstance(getActivity()).patientRegFieldDao());
        RegFieldViewModelFactory factory = new RegFieldViewModelFactory(repository);
        regFieldViewModel = new ViewModelProvider(this, factory).get(RegFieldViewModel.class);

        initUi();

        fetchRegConfig();

        firstScreen = new Fragment_FirstScreen();
        fragment_thirdScreen = new Fragment_ThirdScreen();
        Bundle args = getArguments();
        if (getArguments() != null) {
            patientDTO = (PatientDTO) args.getSerializable("patientDTO");
            fromThirdScreen = args.getBoolean("fromThirdScreen");
            fromFirstScreen = args.getBoolean("fromFirstScreen");
            patient_detail = args.getBoolean("patient_detail");
            isEditMode = args.getBoolean(BundleKeys.FROM_EDIT, false);
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


        if (!sessionManager.getLicenseKey().isEmpty()) hasLicense = true;

        //Check for license key and load the correct config file
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse(FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, context), String.valueOf(FileUtils.encodeJSON(context, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
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
//            String countriesLanguage = "countries_" + sessionManager.getAppLanguage();
//            int countries = res.getIdentifier(countriesLanguage, "array", getActivity().getApplicationContext().getPackageName());
//            if (countries != 0) {
            countryAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.countries, R.layout.simple_spinner_item_1);
            countryAdapter.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);
//            }
            mCountryNameSpinner.setAdapter(countryAdapter); // keeping this is setting textcolor to white so comment this and add android:entries in xml
            mCountryNameSpinner.setPopupBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.popup_menu_background));
            mCountryNameSpinner.setOnItemSelectedListener(countryListener);
            mCountryNameSpinner.setSelection(countryAdapter.getPosition(getString(R.string.default_country)));
//            boolean enable = PatientRegFieldsUtils.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.COUNTRY);
            mCountryNameSpinner.setEnabled(false);
        } catch (Exception e) {
            Logger.logE("Identification", "#648", e);
        }

        //updateUiFromFirstAndSecondFrag();

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
//        mDistrictNameSpinner.setOnItemSelectedListener(new DefaultOnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                Log.v(TAG, "i - " + i);
//                Log.v(TAG, "item - " + adapterView.getItemAtPosition(i));
//                if (i != 0) {
//                    String distName = adapterView.getItemAtPosition(i).toString();
////                    if (!distName.equalsIgnoreCase(mDistName))
////                        mCityVillageET.setText("");
//                    mDistName = adapterView.getItemAtPosition(i).toString();
//                    mDistNameEn = mLastSelectedDistList.get(i - 1).getName();
//                    mDistrictNameErrorTextView.setVisibility(View.GONE);
//                    mDistrictNameSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
//                    mCityNameErrorTextView.setVisibility(View.GONE);
//                    if (mDistrictNameSpinner.getTag() != null) {
//                        List<DistData> districts = (List<DistData>) mDistrictNameSpinner.getTag();
//                        if (districts.get(i).getBlocks() != null && !districts.get(i).getBlocks().isEmpty())
//                            setBlockAdapter(districts.get(i).getBlocks());
//                    }
////                    mCityVillageET.setBackgroundResource(R.drawable.bg_input_fieldnew);
//
//                    //   if (!fromThirdScreen || fromFirstScreen) {
//                    /*if (district.matches("Navi Mumbai")) {
//                        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(getActivity(),
//                                R.array.navi_mumbai_city, android.R.layout.simple_spinner_dropdown_item);
//                        mCityNameSpinner.setAdapter(cityAdapter);
//
//                        // setting state according database when user clicks edit details
//                        if (fromThirdScreen || fromFirstScreen)
//                            mCityNameSpinner.setSelection(cityAdapter.getPosition(String.valueOf(city_village)));
//                        else
//                            mCityNameSpinner.setSelection(cityAdapter.getPosition("Select"));
//
//                    } else if (district.matches("Kurla")) {
//                        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(getActivity(),
//                                R.array.kurla_city, android.R.layout.simple_spinner_dropdown_item);
//                        mCityNameSpinner.setAdapter(cityAdapter);
//
//                        // setting state according database when user clicks edit details
//                        if (fromThirdScreen || fromFirstScreen)
//                            mCityNameSpinner.setSelection(cityAdapter.getPosition(String.valueOf(city_village)));
//                        else
//                            mCityNameSpinner.setSelection(cityAdapter.getPosition("Select"));
//
//                    }*/
//                    //      }
//                }
//
//            }
//        });
        // District based city - end


        // district based  state - start
        mStateNameSpinner.setOnItemSelectedListener(new DefaultOnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    Log.v(TAG, "onItemSelected - " + i);
                    mStateName = adapterView.getItemAtPosition(i).toString();
                    mStateNameEn = mLastSelectedStateList.get(i - 1).getState();
                    mStateNameErrorTextView.setVisibility(View.GONE);
                    mStateNameSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);

                    if (mCountryName.equalsIgnoreCase(getString(R.string.default_country))) {
                        mDistrictET.setVisibility(View.GONE);
                        mDistrictNameSpinner.setVisibility(View.VISIBLE);
                        setDistAdapter(mStateNameEn);
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
        });
        // State based district - end

        // country - start
    }

    private final DefaultOnItemSelectedListener blockSelectedListener = new DefaultOnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (i > 0 && mBlockSpinner.getTag() != null) {
                Block block = (Block) adapterView.getSelectedItem();
                Timber.tag(TAG).e("Block index =>%s", i);
                Timber.tag(TAG).e("Block onItemSelected =>%s", new Gson().toJson(block));
                setGramPanchayatAdapter(block.getGramPanchayats());
            }
        }
    };

    private final DefaultOnItemSelectedListener gpSelectedListener = new DefaultOnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (i != 0 && mGramPanchayatSpinner.getTag() != null) {
                GramPanchayat gramPanchayat = (GramPanchayat) adapterView.getSelectedItem();
                setVillageAdapter(gramPanchayat.getVillages());
            }
        }
    };

    private final DefaultOnItemSelectedListener villageSelectedListener = new DefaultOnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (i != 0 && mVillageSpinner.getTag() != null) {
                Village village = (Village) adapterView.getSelectedItem();
                Timber.tag(TAG).d("Village size => [%1s], %2s", i, new Gson().toJson(village));
                mCityVillageName = village.getName();
            }
        }
    };

    private void setBlockAdapter(List<Block> blocks) {
//        Log.v(TAG, "setBlockAdapter =>" + new Gson().toJson(blocks));
        // To avoid the duplication of list used temp array
        List<Block> temp = new ArrayList<>(blocks);
        Block defaultBlock = new Block(getResources().getString(R.string.select_spinner), null, null);
        temp.add(0, defaultBlock);
//        String[] blockList = new String[blocks.size() + 1];
//        blockList[0] = getResources().getString(R.string.select_spinner);
        for (int i = 0; i < temp.size(); i++) {
            Timber.tag(TAG).d("blocks =>%s", temp.get(i).getName());
        }


        ArrayAdapter<Block> blockAdapter = new ArrayAdapter<Block>(requireContext(), R.layout.simple_spinner_item_1, temp);
        blockAdapter.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);

        mBlockSpinner.setAdapter(blockAdapter);
        mBlockSpinner.setTag(temp);
        mBlockSpinner.setOnItemSelectedListener(blockSelectedListener);
        mBlockSpinner.setPopupBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.popup_menu_background));
        if (patientDTO.getAddress3() != null && !patientDTO.getAddress3().isEmpty()) {
            for (Block b : temp) {
                if (b.getName().equalsIgnoreCase(patientDTO.getAddress3())) {
                    mBlockSpinner.setSelection(blockAdapter.getPosition(b));
                }
            }
        }
    }

    private void setGramPanchayatAdapter(List<GramPanchayat> gramPanchayats) {
        Log.v(TAG, "setGramPanchayatAdapter =>" + new Gson().toJson(gramPanchayats));
        if (gramPanchayats.size() > 0 && !Objects.equals(gramPanchayats.get(0).getName(), getResources().getString(R.string.select_spinner))) {
            GramPanchayat defaultGP = new GramPanchayat(getResources().getString(R.string.select_spinner), null, null);
            gramPanchayats.add(0, defaultGP);
        }
//        String[] gpList = new String[gramPanchayats.size() + 1];
//        gpList[0] = getResources().getString(R.string.select_spinner);
//        for (int i = 1; i <= gramPanchayats.size(); i++) {
//            Timber.tag(TAG).d("GramPanchayat =>%s", gramPanchayats.get(i - 1).getName());
//            gpList[i] = gramPanchayats.get(i - 1).getName();
//        }


        ArrayAdapter<GramPanchayat> gpAdapter = new ArrayAdapter<>(requireActivity(), R.layout.simple_spinner_item_1, gramPanchayats);
        gpAdapter.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);

        mGramPanchayatSpinner.setEnabled(true);
        mGramPanchayatSpinner.setAdapter(gpAdapter);
        mGramPanchayatSpinner.setTag(gramPanchayats);
        mGramPanchayatSpinner.setOnItemSelectedListener(gpSelectedListener);
        mGramPanchayatSpinner.setPopupBackgroundDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.popup_menu_background));
        if (patientDTO.getAddress4() != null && !patientDTO.getAddress4().isEmpty()) {
            for (GramPanchayat gp : gramPanchayats) {
                if (gp.getName().equalsIgnoreCase(patientDTO.getAddress4())) {
                    mGramPanchayatSpinner.setSelection(gpAdapter.getPosition(gp));
                }
            }
        }
    }

    private void setVillageAdapter(List<Village> villages) {
        Log.v(TAG, "setVillageAdapter =>" + new Gson().toJson(villages));
        if (villages.size() > 0 && !Objects.equals(villages.get(0).getName(), getResources().getString(R.string.select_spinner)))
            villages.add(0, new Village(getResources().getString(R.string.select_spinner), null));
//        String[] gpList = new String[villages.size() + 1];
//        gpList[0] = getResources().getString(R.string.select_spinner);
//        for (int i = 1; i <= villages.size(); i++) {
//            Timber.tag(TAG).d("blocks =>%s", villages.get(i - 1).getName());
//            gpList[i] = villages.get(i - 1).getName();
//        }


        ArrayAdapter<Village> villageAdapter = new ArrayAdapter<Village>(getActivity(), R.layout.simple_spinner_item_1, villages);
        villageAdapter.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);

        mVillageSpinner.setEnabled(true);
        mVillageSpinner.setAdapter(villageAdapter);
        mVillageSpinner.setTag(villages);
        mVillageSpinner.setOnItemSelectedListener(villageSelectedListener);
        mVillageSpinner.setPopupBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.popup_menu_background));
        if (patientDTO.getAddress5() != null && !patientDTO.getAddress5().isEmpty()) {
            for (Village v : villages) {
                if (v.getName().equalsIgnoreCase(patientDTO.getAddress5())) {
                    mVillageSpinner.setSelection(villageAdapter.getPosition(v));
                }
            }
        }
    }

    private final DefaultOnItemSelectedListener distListener = new DefaultOnItemSelectedListener() {
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
                if (mDistrictNameSpinner.getTag() != null) {
                    List<DistData> districts = (List<DistData>) mDistrictNameSpinner.getTag();
                    Timber.tag(TAG).d("District => %s", districts.get(i - 1).getName());
                    List<Block> blocks = districts.get(i - 1).getBlocks();
                    if (blocks != null && !blocks.isEmpty()) setBlockAdapter(blocks);
                    else Timber.tag(TAG).d("Empty blocks");
                } else Timber.tag(TAG).d("Tag is null");
//                    mCityVillageET.setBackgroundResource(R.drawable.bg_input_fieldnew);

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
    };

    private List<Block> getRegionBlocks(List<DistData> districts, int i) {
        if (sessionManager.getAppLanguage().equals("en")) return districts.get(i - 1).getBlocks();
        else return districts.get(i - 1).getBlocksHindi();
    }

    private final AdapterView.OnItemSelectedListener countryListener = new DefaultOnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (i != 0) {
                mCountryName = adapterView.getItemAtPosition(i).toString();
                mCountryNameEn = mCountryList[i];
                mCountryNameErrorTextView.setVisibility(View.GONE);
                mCountryNameSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);

                if (mCountryName.equalsIgnoreCase(getString(R.string.default_country))) {
                    mIsIndiaSelected = true;
                    mStateEditText.setVisibility(View.GONE);
                    mStateNameSpinner.setVisibility(View.VISIBLE);
                    Log.v(TAG, "setStateAdapter calling....599");
                    setStateAdapter(mCountryName);

                    mDistrictET.setVisibility(View.GONE);
                    mDistrictNameSpinner.setVisibility(View.VISIBLE);

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
    };

    /**
     * fetching reg config from local db
     */
    private void fetchRegConfig() {
        regFieldViewModel.fetchAddressRegFields();
        regFieldViewModel.getAddressSectionFieldsLiveData().observe(getViewLifecycleOwner(), it -> {
            patientRegistrationFields = it;
            configAllFields();
            updateUiFromFirstAndSecondFrag();
        });
    }

    /**
     * update ui from first or third screen
     */
    private void updateUiFromFirstAndSecondFrag() {
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
                countryIndex = countryAdapter.getPosition(getString(R.string.default_country));
                mCountryName = getString(R.string.default_country);
            }
            mCountryNameSpinner.setSelection(countryIndex);
            if (patientDTO.getCityvillage() != null && !patientDTO.getCityvillage().isEmpty()) {
                String[] district_city = patientDTO.getCityvillage().trim().split(":");
                if (district_city.length == 2) {
                    district = mDistName = district_city[0];
                    city_village = mCityVillageName = district_city[1];
//                    mCityVillageET.setText(city_village);
                } else {
//                    mCityVillageET.setText(patientDTO.getCityvillage());
                }
            }

            if (mCountryName.equalsIgnoreCase(getString(R.string.default_country))) {
//                mIsIndiaSelected = true;
//                Log.v(TAG, "setStateAdapter calling....344");
//                //setStateAdapter(mCountryName);
//                mStateNameEn = String.valueOf(patientDTO.getStateprovince());
//                Log.v(TAG, "mStateName -" + mStateNameEn + "??");

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
    }


    /**
     * changing fields status based on config data
     */
    private void configAllFields() {
        for (PatientRegistrationFields fields : patientRegistrationFields) {
            switch (fields.getIdKey()) {
                case PatientRegConfigKeys.POSTAL_CODE ->
                        PatientRegFieldsUtils.configField(isEditMode, fields, postalCodeLay, mPostalCodeEditText, null, postalCodeTv);
                case PatientRegConfigKeys.COUNTRY -> {
                    PatientRegFieldsUtils.configField(isEditMode, fields, countryLay, mCountryNameSpinner, null, countryTv);
                    if (PatientRegFieldsUtils.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.POSTAL_CODE)) {
                        setMarginToLayout(countryLay);
                    }
                }
                case PatientRegConfigKeys.STATE ->
                        PatientRegFieldsUtils.configField(isEditMode, fields, stateLay, mStateNameSpinner, null, stateTv);
                case PatientRegConfigKeys.DISTRICT ->
                        PatientRegFieldsUtils.configField(isEditMode, fields, districtLay, mDistrictNameSpinner, null, districtTv);
//                case PatientRegConfigKeys.VILLAGE_TOWN_CITY -> {
//                    PatientRegFieldsUtils.configField(
//                            isEditMode,
//                            fields,
//                            villTownCityLay,
//                            mCityVillageET,
//                            null,
//                            villTownCityTv
//                    );
//                    if (PatientRegFieldsUtils.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.DISTRICT)) {
//                        setMarginToLayout(villTownCityLay);
//                    }
//                }
                case PatientRegConfigKeys.ADDRESS_1 ->
                        PatientRegFieldsUtils.configField(isEditMode, fields, address1Lay, mAddress1EditText, null, address1Tv);
                case PatientRegConfigKeys.ADDRESS_2 ->
                        PatientRegFieldsUtils.configField(isEditMode, fields, address2Lay, mAddress2EditText, null, address2Tv);
            }
        }
    }

    private void setMarginToLayout(LinearLayout layout) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) layout.getLayoutParams();
        lp.setMargins((int) getResources().getDimension(R.dimen.margin_16dp), 0, 0, 0);
        layout.setLayoutParams(lp);
    }

    private void initUi() {
        //all field title tv
        postalCodeTv = view.findViewById(R.id.postal_code_tv);
        districtTv = view.findViewById(R.id.district_tv);
        countryTv = view.findViewById(R.id.country_tv);
        stateTv = view.findViewById(R.id.state_tv);
        villTownCityTv = view.findViewById(R.id.vill_town_city_tv);
        address1Tv = view.findViewById(R.id.address1_tv);
        address2Tv = view.findViewById(R.id.address2_tv);

        //all field layout
        postalCodeLay = view.findViewById(R.id.postal_code_lay);
        districtLay = view.findViewById(R.id.district_lay);
        countryLay = view.findViewById(R.id.country_lay);
        stateLay = view.findViewById(R.id.linear_state);
        villTownCityLay = view.findViewById(R.id.vill_town_city_lay);
        address1Lay = view.findViewById(R.id.linear_address1);
        address2Lay = view.findViewById(R.id.address2_lay);


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
//        mCityVillageET = view.findViewById(R.id.city_village_edittext);
//        mCityVillageET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
//        mCityVillageET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50), inputFilter_Others}); //maxlength 50
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
//        mCityVillageET.addTextChangedListener(new MyTextWatcher(mCityVillageET));
        mBlockSpinner = view.findViewById(R.id.spBlock);
        mGramPanchayatSpinner = view.findViewById(R.id.gram_panchayat_spinner);
        mVillageSpinner = view.findViewById(R.id.spVillage);
        blockError = view.findViewById(R.id.block_error);
        gramPanchatError = view.findViewById(R.id.gram_panchayat_error);
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
                if (PatientRegFieldsUtils.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.POSTAL_CODE)) {
                    if (val.isEmpty() && PatientRegFieldsUtils.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.POSTAL_CODE)) {
                        if (mCountryNameSpinner.getSelectedItem().toString().equalsIgnoreCase(getString(R.string.default_country)) && mPostalCodeEditText.getText().toString().trim().length() != 6) {
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
                }

            }
            if (this.editText.getId() == R.id.state_edittext) {
                if (PatientRegFieldsUtils.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.STATE)) {
                    if (val.isEmpty() && PatientRegFieldsUtils.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.STATE)) {
                        mStateNameErrorTextView.setVisibility(View.VISIBLE);
                        mStateNameErrorTextView.setText(getString(R.string.error_field_required));
                        editText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    } else {
                        mStateNameErrorTextView.setVisibility(View.GONE);
                        editText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                    }
                }
            } else if (this.editText.getId() == R.id.district_edittext) {
                if (PatientRegFieldsUtils.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.DISTRICT)) {
                    if (val.isEmpty() && PatientRegFieldsUtils.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.DISTRICT)) {
                        mDistrictNameErrorTextView.setVisibility(View.VISIBLE);
                        mDistrictNameErrorTextView.setText(getString(R.string.error_field_required));
                        editText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    } else {
                        mDistrictNameErrorTextView.setVisibility(View.GONE);
                        editText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                    }
                }
            }
//            else if (this.editText.getId() == R.id.city_village_edittext) {
//                if (PatientRegFieldsUtils.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.VILLAGE_TOWN_CITY)) {
//                    if (val.isEmpty() && PatientRegFieldsUtils.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.VILLAGE_TOWN_CITY)) {
//                        mCityNameErrorTextView.setVisibility(View.VISIBLE);
//                        mCityNameErrorTextView.setText(getString(R.string.error_field_required));
//                        editText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
//                    } else {
//                        mCityNameErrorTextView.setVisibility(View.GONE);
//                        editText.setBackgroundResource(R.drawable.bg_input_fieldnew);
//                    }
//                }
//            }
            else if (this.editText.getId() == R.id.address1_edittext) {
                if (PatientRegFieldsUtils.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.VILLAGE_TOWN_CITY)) {
                    if (val.isEmpty() && PatientRegFieldsUtils.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.VILLAGE_TOWN_CITY)) {
                        mAddress1ErrorTextView.setVisibility(View.VISIBLE);
                        mAddress1ErrorTextView.setText(getString(R.string.error_field_required));
                        editText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    } else {
                        mAddress1ErrorTextView.setVisibility(View.GONE);
                        editText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                    }
                }
            } else if (this.editText.getId() == R.id.address2_edittext) {
                if (PatientRegFieldsUtils.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.ADDRESS_1)) {
                    if (val.isEmpty() && PatientRegFieldsUtils.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.ADDRESS_1)) {
                        mAddress2ErrorTextView.setVisibility(View.VISIBLE);
                        mAddress2ErrorTextView.setText(getString(R.string.error_field_required));
                        editText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    } else {
                        mAddress2ErrorTextView.setVisibility(View.GONE);
                        editText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                    }
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

        stateAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_spinner_item_1, stateList);
        stateAdapter.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);

        mStateNameSpinner.setAdapter(stateAdapter);
        mStateNameSpinner.setPopupBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.popup_menu_background));
        int index = stateAdapter.getPosition(getString(R.string.default_state));
        Timber.tag(TAG).d("Default State index=>%s", index);

        if (fromThirdScreen || fromFirstScreen) {
            int itemPosition = stateAdapter.getPosition(getString(R.string.default_state));
            for (int k = 0; k < mLastSelectedStateList.size(); k++) {

                if (mLastSelectedStateList.get(k).getState().equalsIgnoreCase(String.valueOf(patientDTO.getStateprovince()))) {
                    itemPosition = k + 1;
                    break;
                }
            }
            mStateNameSpinner.setSelection(itemPosition);
        }
//        boolean enable = PatientRegFieldsUtils.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.STATE);
        mStateNameSpinner.setEnabled(false);
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

        districtAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_spinner_item_1, distList);
        districtAdapter.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);

        mDistrictNameSpinner.setAdapter(districtAdapter);
        mDistrictNameSpinner.setTag(distDataList);
        mDistrictNameSpinner.setOnItemSelectedListener(distListener);
        mDistrictNameSpinner.setPopupBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.popup_menu_background));

        if (fromThirdScreen || fromFirstScreen) {
            int itemPosition = districtAdapter.getPosition(getString(R.string.default_district));
            for (int k = 0; k < mLastSelectedDistList.size(); k++) {
                if (mLastSelectedDistList.get(k).getName().equalsIgnoreCase(mDistName)) {
                    itemPosition = k + 1;
                    break;
                }
            }
            //mDistrictNameSpinner.setSelection(districtAdapter.getPosition(String.valueOf(district)));
            mDistrictNameSpinner.setSelection(itemPosition);
        }

//        boolean enable = PatientRegFieldsUtils.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.DISTRICT);
        mDistrictNameSpinner.setEnabled(false);
    }

    private void onBackInsertIntoPatientDTO() {

        mStateName = mStateEditText.getText().toString().trim();
        mDistName = mDistrictET.getText().toString().trim();

//        mCityVillageName = mCityVillageET.getText().toString().trim();

        patientDTO.setPostalcode(mPostalCodeEditText.getText().toString());
        patientDTO.setCountry(mCountryNameSpinner.getSelectedItem() == null ? "" : StringUtils.getValue(mCountryNameSpinner.getSelectedItem().toString()));
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
        patientDTO.setAddress3(mBlockSpinner.getSelectedItem().toString());
        patientDTO.setAddress4(mGramPanchayatSpinner.getSelectedItem().toString());
        patientDTO.setAddress5(mVillageSpinner.getSelectedItem().toString());

        Log.v("fragmemt_2", "values: " + new Gson().toJson(patientDTO));

        Bundle bundle = new Bundle();
        bundle.putSerializable("patientDTO", (Serializable) patientDTO);
        bundle.putBoolean("fromSecondScreen", true);
        bundle.putBoolean("patient_detail", patient_detail);
        firstScreen.setArguments(bundle); // passing data to Fragment
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_firstscreen, firstScreen).commit();
    }

    private void onPatientCreateClicked() {
        Gson gson = new Gson();
        boolean cancel = false;
        View focusView = null;

        if (PatientRegFieldsUtils.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.POSTAL_CODE)) {
            if (mPostalCodeEditText.getText().toString().equals("") && PatientRegFieldsUtils.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.POSTAL_CODE)) {
                if (mCountryNameSpinner.getSelectedItem().toString().equalsIgnoreCase(getString(R.string.default_country)) && mPostalCodeEditText.getText().toString().trim().length() != 6) {
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
        }

        if (PatientRegFieldsUtils.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.COUNTRY)) {
            if (mCountryNameSpinner.getSelectedItemPosition() == 0 && PatientRegFieldsUtils.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.COUNTRY)) {
                mCountryNameErrorTextView.setVisibility(View.VISIBLE);
                mCountryNameErrorTextView.setText(getString(R.string.error_field_required));
                mCountryNameSpinner.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                mCountryNameSpinner.requestFocus();
                return;
            } else {
                mCountryNameErrorTextView.setVisibility(View.GONE);
                mCountryNameSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
            }
        }

        if (PatientRegFieldsUtils.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.STATE)) {
            if (mStateNameSpinner.getSelectedItemPosition() == 0 && PatientRegFieldsUtils.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.STATE)) {
                mStateNameErrorTextView.setVisibility(View.VISIBLE);
                mStateNameErrorTextView.setText(getString(R.string.error_field_required));
                mStateNameSpinner.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                mStateNameSpinner.requestFocus();
                return;
            } else {
                mStateNameErrorTextView.setVisibility(View.GONE);
                mStateNameSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
            }
        }

        if (PatientRegFieldsUtils.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.DISTRICT)) {
            if (mDistrictNameSpinner.getVisibility() == View.VISIBLE && (mDistrictNameSpinner.getSelectedItemPosition() == 0 || mDistrictNameSpinner.getChildCount() == 0) && PatientRegFieldsUtils.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.DISTRICT)) {
                mDistrictNameErrorTextView.setVisibility(View.VISIBLE);
                mDistrictNameErrorTextView.setText(getString(R.string.error_field_required));
                mDistrictNameSpinner.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                mDistrictNameSpinner.requestFocus();
                return;
            } else {
                mDistrictNameErrorTextView.setVisibility(View.GONE);
                mDistrictNameSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
            }
        }

        if (PatientRegFieldsUtils.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.DISTRICT)) {
            if (mDistrictET.getVisibility() == View.VISIBLE && mDistrictET.getText().toString().equals("") && PatientRegFieldsUtils.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.DISTRICT)) {
                mDistrictNameErrorTextView.setVisibility(View.VISIBLE);
                mDistrictNameErrorTextView.setText(getString(R.string.error_field_required));
                mDistrictET.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                mDistrictET.requestFocus();
                return;
            } else {
                mDistrictNameErrorTextView.setVisibility(View.GONE);
                mDistrictET.setBackgroundResource(R.drawable.bg_input_fieldnew);
            }
        }

//        if (PatientRegFieldsUtils.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.DISTRICT)) {
        if (mBlockSpinner.getVisibility() == View.VISIBLE && (mBlockSpinner.getSelectedItemPosition() == 0 || mBlockSpinner.getChildCount() == 0)) {
            blockError.setVisibility(View.VISIBLE);
            blockError.setText(getString(R.string.error_field_required));
            mBlockSpinner.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mBlockSpinner.requestFocus();
            return;
        } else {
            blockError.setVisibility(View.GONE);
            mBlockSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
        }

        if (mGramPanchayatSpinner.getVisibility() == View.VISIBLE && (mGramPanchayatSpinner.getSelectedItemPosition() == 0 || mGramPanchayatSpinner.getChildCount() == 0)) {
            gramPanchatError.setVisibility(View.VISIBLE);
            gramPanchatError.setText(getString(R.string.error_field_required));
            mGramPanchayatSpinner.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mGramPanchayatSpinner.requestFocus();
            return;
        } else {
            gramPanchatError.setVisibility(View.GONE);
            mGramPanchayatSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
        }

        if (mVillageSpinner.getVisibility() == View.VISIBLE && (mVillageSpinner.getSelectedItemPosition() == 0 || mVillageSpinner.getChildCount() == 0)) {
            mCityNameErrorTextView.setVisibility(View.VISIBLE);
            mCityNameErrorTextView.setText(getString(R.string.error_field_required));
            mVillageSpinner.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mVillageSpinner.requestFocus();
            return;
        } else {
            gramPanchatError.setVisibility(View.GONE);
            mVillageSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
        }
//        }

//        if (PatientRegFieldsUtils.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.VILLAGE_TOWN_CITY)) {
//            if (mCityVillageET.getText().toString().isEmpty() &&
//                    PatientRegFieldsUtils.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.VILLAGE_TOWN_CITY)) {
//                mCityNameErrorTextView.setVisibility(View.VISIBLE);
//                mCityNameErrorTextView.setText(getString(R.string.error_field_required));
//                mCityVillageET.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
//                mCityVillageET.requestFocus();
//                return;
//            }
//            else if (mCityVillageET.getText().toString().length() < 3 &&
//                    PatientRegFieldsUtils.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.VILLAGE_TOWN_CITY)) {
//                mCityNameErrorTextView.setVisibility(View.VISIBLE);
//                mCityNameErrorTextView.setText(getString(R.string.error_field_valid_village_required));
//                mCityVillageET.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
//                mCityVillageET.requestFocus();
//                return;
//            }
//            else {
//                mCityNameErrorTextView.setVisibility(View.GONE);
//                mCityVillageET.setBackgroundResource(R.drawable.bg_input_fieldnew);
//            }
//        }

        //address 1
        if (PatientRegFieldsUtils.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.ADDRESS_1)) {
            if (mAddress1EditText.getText().toString().isEmpty() && PatientRegFieldsUtils.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.ADDRESS_1)) {
                mAddress1ErrorTextView.setVisibility(View.VISIBLE);
                mAddress1ErrorTextView.setText(getString(R.string.error_field_required));
                mAddress1EditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                mAddress1EditText.requestFocus();
                return;
            } else {
                mAddress1ErrorTextView.setVisibility(View.GONE);
                mAddress1EditText.setBackgroundResource(R.drawable.ui2_spinner_background_new);
            }
        }

        //address 2
        if (PatientRegFieldsUtils.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.ADDRESS_2)) {
            if (mAddress2EditText.getText().toString().isEmpty() && PatientRegFieldsUtils.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.ADDRESS_2)) {
                mAddress2ErrorTextView.setVisibility(View.VISIBLE);
                mAddress2ErrorTextView.setText(getString(R.string.error_field_required));
                mAddress2EditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                mAddress2EditText.requestFocus();
                return;
            } else {
                mAddress2ErrorTextView.setVisibility(View.GONE);
                mAddress2EditText.setBackgroundResource(R.drawable.ui2_spinner_background_new);
            }
        }


        /**
         *  entering value in dataset start
         */
        if (cancel) {
            focusView.requestFocus();
        } else {
            mStateName = mStateEditText.getText().toString().trim();
            mDistName = mDistrictET.getText().toString().trim();
//            mCityVillageName = mCityVillageET.getText().toString().trim();

            patientDTO.setPostalcode(mPostalCodeEditText.getText().toString());
            patientDTO.setCountry(mCountryNameSpinner.getSelectedItem() == null ? "" : StringUtils.getValue(mCountryNameSpinner.getSelectedItem().toString()));
            patientDTO.setStateprovince(StringUtils.getValue(mIsIndiaSelected ? mStateNameSpinner.getSelectedItem().toString() : mStateName));
            if (mDistrictNameSpinner != null && mDistrictNameSpinner.getSelectedItem() != null) {
                patientDTO.setCityvillage(StringUtils.getValue((mIsIndiaSelected ? mDistrictNameSpinner.getSelectedItem().toString() : mDistName) + ":" + mCityVillageName));
            } else {
                patientDTO.setCityvillage(mCityVillageName);
            }

            if (!sessionManager.getAppLanguage().equals("en")) {
                patientDTO.setCountry(StringUtils.getValue(mCountryNameEn));
                patientDTO.setStateprovince(StringUtils.getValue(mIsIndiaSelected ? mStateNameEn : mStateName));
                patientDTO.setCityvillage(StringUtils.getValue((mIsIndiaSelected ? mDistNameEn : mDistName) + ":" + mCityVillageName));

            }
            patientDTO.setAddress1(mAddress1EditText.getText().toString());
            patientDTO.setAddress2(mAddress2EditText.getText().toString());
            patientDTO.setAddress3(((Block) mBlockSpinner.getSelectedItem()).getName());
            patientDTO.setAddress4(((GramPanchayat) mGramPanchayatSpinner.getSelectedItem()).getName());
            patientDTO.setAddress5(((Village) mVillageSpinner.getSelectedItem()).getName());
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

                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_firstscreen, fragment_thirdScreen).commit();
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
                else if (c == '.' || c == '&' || c == '(' || c == ')') sb.append(c);
                else keepOriginal = false;
            }
            if (keepOriginal) return null;
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

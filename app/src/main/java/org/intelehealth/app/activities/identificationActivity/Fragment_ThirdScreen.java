package org.intelehealth.app.activities.identificationActivity;

import static org.intelehealth.app.abdm.activity.AadharMobileVerificationActivity.BEARER_AUTH;
import static org.intelehealth.app.activities.identificationActivity.IdentificationActivity_New.MOBILE_PAYLOAD;
import static org.intelehealth.app.activities.identificationActivity.IdentificationActivity_New.PAYLOAD;
import static org.intelehealth.app.utilities.StringUtils.inputFilter_Others;
import static org.intelehealth.app.utilities.StringUtils.switch_as_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_as_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_as_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_bn_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_bn_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_bn_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_gu_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_gu_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_gu_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_hi_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_hi_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_hi_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_kn_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_kn_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_kn_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ml_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ml_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ml_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_mr_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_mr_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_mr_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_or_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_or_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_or_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ru_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ru_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ru_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ta_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ta_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ta_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_te_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_te_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_te_education_edit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.ajalt.timberkt.Timber;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.abdm.activity.AbhaAddressSuggestionsActivity;
import org.intelehealth.app.abdm.activity.CreateAbhaAccountActivity;
import org.intelehealth.app.abdm.model.AbhaProfileResponse;
import org.intelehealth.app.abdm.model.EnrollSuggestionRequestBody;
import org.intelehealth.app.abdm.model.EnrollSuggestionResponse;
import org.intelehealth.app.abdm.model.OTPVerificationResponse;
import org.intelehealth.app.abdm.model.TokenResponse;
import org.intelehealth.app.abdm.utils.ABDMConstant;
import org.intelehealth.app.activities.patientDetailActivity.AbhaCardDownloadUtil;
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ImagesPushDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.app.widget.materialprogressbar.CustomProgressDialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class Fragment_ThirdScreen extends Fragment {
    private static final String TAG = Fragment_ThirdScreen.class.getSimpleName();
    private PatientDTO patientDTO;
    private View view;
    SessionManager sessionManager = null;
    private ArrayAdapter<CharSequence> educationAdapter;
    private ArrayAdapter<CharSequence> casteAdapter;
    private ArrayAdapter<CharSequence> economicStatusAdapter;
    private EditText mRelationNameEditText, mOccupationEditText, mNationalIDEditText,
            mAbhaNumberEditText, mAbhaAddressEditText;
    private Spinner mCasteSpinner, mEducationSpinner, mEconomicstatusSpinner;
    private ImageView personal_icon, address_icon, other_icon;
    private Button frag3_btn_back, frag3_btn_next;
    private TextView mRelationNameErrorTextView, mOccupationErrorTextView, mCasteErrorTextView, mEducationErrorTextView,
            mEconomicErrorTextView, mAbhaNumberErrorTextView, mAbhaAddressErrorTextView;
    ImagesDAO imagesDAO = new ImagesDAO();
    private Fragment_SecondScreen secondScreen;
    boolean fromThirdScreen = false, fromSecondScreen = false;
    Patient patient1 = new Patient();
    PatientsDAO patientsDAO = new PatientsDAO();
    String patientID_edit;
    boolean patient_detail = false;
    private OTPVerificationResponse otpVerificationResponse;
    private AbhaProfileResponse abhaProfileResponse;
    private String accessToken, xToken, txnId;
    private TextView tvCreateNewAbhaAddress;

    private LinearLayout linearAbhaNo, linearAbhaAddress;
    private String blockCharacterSet_ABHA_Address = "@";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_registration_thirdscreen, container, false);
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
        sessionManager = new SessionManager(getActivity());

        personal_icon = getActivity().findViewById(R.id.addpatient_icon);
        address_icon = getActivity().findViewById(R.id.addresslocation_icon);
        other_icon = getActivity().findViewById(R.id.other_icon);
        frag3_btn_back = getActivity().findViewById(R.id.frag3_btn_back);
        frag3_btn_next = getActivity().findViewById(R.id.frag3_btn_next);

        mRelationNameEditText = view.findViewById(R.id.relation_edittext);
        mNationalIDEditText = view.findViewById(R.id.national_ID_editText);
        mNationalIDEditText.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.LengthFilter(24)}); //all capital input

        mAbhaNumberEditText = view.findViewById(R.id.abhaNo_editText);
        mAbhaNumberErrorTextView = view.findViewById(R.id.abhaNo_error);

        mAbhaAddressEditText = view.findViewById(R.id.abhaAddress_editText);
        mAbhaAddressErrorTextView = view.findViewById(R.id.abhaAddress_error);
        linearAbhaNo = view.findViewById(R.id.linear_abhaNo);
        linearAbhaAddress = view.findViewById(R.id.linear_abhaAddress);

        if (!sessionManager.doCreateABHA()) {
            linearAbhaNo.setVisibility(View.GONE);
            linearAbhaAddress.setVisibility(View.GONE);
        }

        mOccupationEditText = view.findViewById(R.id.occupation_editText);
        mCasteSpinner = view.findViewById(R.id.caste_spinner);
        mEducationSpinner = view.findViewById(R.id.education_spinner);
        mEconomicstatusSpinner = view.findViewById(R.id.economicstatus_spinner);

        mRelationNameErrorTextView = view.findViewById(R.id.relation_error);
        mOccupationErrorTextView = view.findViewById(R.id.occupation_error);
        mCasteErrorTextView = view.findViewById(R.id.caste_error);
        mEducationErrorTextView = view.findViewById(R.id.education_error);
        mEconomicErrorTextView = view.findViewById(R.id.economic_error);


        mRelationNameEditText.addTextChangedListener(new MyTextWatcher(mRelationNameEditText));
        mRelationNameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Others}); //maxlength 25

        /*mNationalIDEditText.addTextChangedListener(new MyTextWatcher(mNationalIDEditText));
        mNationalIDEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(18), inputFilter_Others});*/ //maxlength 25

        mOccupationEditText.addTextChangedListener(new MyTextWatcher(mOccupationEditText));
        mOccupationEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Others}); //maxlength 25

        tvCreateNewAbhaAddress = view.findViewById(R.id.tv_create_new_abha_address);

        secondScreen = new Fragment_SecondScreen();
        if (getArguments() != null) {
            patientDTO = (PatientDTO) getArguments().getSerializable("patientDTO");
            fromSecondScreen = getArguments().getBoolean("fromSecondScreen");
            patient_detail = getArguments().getBoolean("patient_detail");
            if (patient_detail) {
                frag3_btn_back.setVisibility(View.GONE);
                frag3_btn_next.setText(getString(R.string.save));
            } else {
                // do nothing...
            }

            // abdm - start
            if (getArguments().containsKey(PAYLOAD)) {
                otpVerificationResponse = (OTPVerificationResponse) getArguments().getSerializable(PAYLOAD);
                if (otpVerificationResponse != null) {
                    setAutoFillValuesViaAadhar(otpVerificationResponse);
                }
            } else if (getArguments().containsKey(MOBILE_PAYLOAD)) {
                abhaProfileResponse = (AbhaProfileResponse) getArguments().getSerializable(MOBILE_PAYLOAD);
                if (abhaProfileResponse != null) {
                    setAutoFillValuesViaMobile(abhaProfileResponse);
                }
            }

            if (!patient_detail && otpVerificationResponse != null) {
                tvCreateNewAbhaAddress.setVisibility(View.VISIBLE);
            }

            if (getArguments().containsKey("accessToken"))
                accessToken = getArguments().getString("accessToken");
            if (getArguments().containsKey("xToken"))
                xToken = getArguments().getString("xToken");
            if (getArguments().containsKey("txnId"))
                txnId = getArguments().getString("txnId");
            if (getArguments().containsKey("firstRequestFulfilled")) {
                if (getArguments().getBoolean("firstRequestFulfilled"))
                    tvCreateNewAbhaAddress.setVisibility(View.GONE);
            }

            disableAbhaFlowFieldsOnEdit(patientDTO);
            // abdm - end
        }

        mCasteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    mCasteErrorTextView.setVisibility(View.GONE);
                    mCasteSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mEducationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    mEducationErrorTextView.setVisibility(View.GONE);
                    mEducationSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mEconomicstatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    mEconomicErrorTextView.setVisibility(View.GONE);
                    mEconomicstatusSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        tvCreateNewAbhaAddress.setOnClickListener(v -> {
            callFetchAbhaAddressSuggestionsApi(otpVerificationResponse, accessToken);
        });
    }

    private void callFetchAbhaAddressSuggestionsApi(OTPVerificationResponse otpVerificationResponse, String accessToken) {
        ArrayList<String> addressList = new ArrayList<>();
        String url = UrlModifiers.getEnrollABHASuggestionUrl();
        EnrollSuggestionRequestBody body = new EnrollSuggestionRequestBody();
        body.setTxnId(otpVerificationResponse.getTxnId());

        Single<EnrollSuggestionResponse> enrollSuggestionResponseSingle = AppConstants.apiInterface.PUSH_ENROLL_ABHA_ADDRESS_SUGGESTION(url, accessToken, body);
        enrollSuggestionResponseSingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<>() {
                    @Override
                    public void onSuccess(EnrollSuggestionResponse enrollSuggestionResponse) {
                        if (enrollSuggestionResponse.getAbhaAddressList() != null) {
                            addressList.addAll(otpVerificationResponse.getABHAProfile().getPhrAddress());
                            addressList.addAll(enrollSuggestionResponse.getAbhaAddressList());
                            addressList.remove(otpVerificationResponse.getABHAProfile().getPhrAddress().get(0));

                            if (!addressList.isEmpty()) {
                                Intent intent = new Intent(getActivity(), AbhaAddressSuggestionsActivity.class);
                                intent.putStringArrayListExtra("addressList", addressList);
                                intent.putExtra("payload", otpVerificationResponse);
                                intent.putExtra("accessToken", accessToken);
                                intent.putExtra("firstRequestFulfilled", true);
                                startActivity(intent);
                                if (getActivity() != null) {
                                    getActivity().finish();
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.tag(TAG).e("onError: suggestion%s", e.toString());
                    }
                });
    }

    private void disableAbhaFlowFieldsOnEdit(PatientDTO patientDTO) {
        if (patientDTO.getAbhaAddress() == null || patientDTO.getAbhaAddress().trim().isEmpty()) {
            return;
        }

        mAbhaNumberEditText.setEnabled(false);
        mAbhaAddressEditText.setEnabled(false);
    }

    private InputFilter filter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
            if (charSequence != null && blockCharacterSet_ABHA_Address.contains(("" + charSequence))) {
                return "";
            }
            return null;
        }
    };

    private void setAutoFillValuesViaMobile(AbhaProfileResponse abhaProfileResponse) {
        mAbhaNumberEditText.setText(abhaProfileResponse.getABHANumber());
        mAbhaNumberEditText.setEnabled(false);
        if (!abhaProfileResponse.getPreferredAbhaAddress().endsWith("@sbx") && !abhaProfileResponse.getPreferredAbhaAddress().isEmpty()) {
            mAbhaAddressEditText.setText(abhaProfileResponse.getPreferredAbhaAddress() + "@sbx");
            mAbhaAddressEditText.setEnabled(false);
        } else {
            mAbhaAddressEditText.setText(abhaProfileResponse.getPreferredAbhaAddress());
            mAbhaAddressEditText.setEnabled(false);
        }

    }

    private void setAutoFillValuesViaAadhar(OTPVerificationResponse otpVerificationResponse) {
        mAbhaNumberEditText.setText(otpVerificationResponse.getABHAProfile().getABHANumber());
        mAbhaNumberEditText.setEnabled(false);
        if (!otpVerificationResponse.getABHAProfile().getPhrAddress().get(0).endsWith("@sbx") && !otpVerificationResponse.getABHAProfile().getPhrAddress().isEmpty()) {
            mAbhaAddressEditText.setText(otpVerificationResponse.getABHAProfile().getPhrAddress().get(0) + "@sbx");
            mAbhaAddressEditText.setEnabled(false);
        } else {
            mAbhaAddressEditText.setText(otpVerificationResponse.getABHAProfile().getPhrAddress().get(0));
            mAbhaAddressEditText.setEnabled(false);
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
            /*if (this.editText.getId() == R.id.relation_edittext) {
                if (val.isEmpty()) {
                    mRelationNameErrorTextView.setVisibility(View.VISIBLE);
                    mRelationNameErrorTextView.setText(getString(R.string.error_field_required));
                    mRelationNameEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mRelationNameErrorTextView.setVisibility(View.GONE);
                    mRelationNameEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            } else if (this.editText.getId() == R.id.occupation_editText) {
                if (val.isEmpty()) {
                    mOccupationErrorTextView.setVisibility(View.VISIBLE);
                    mOccupationErrorTextView.setText(getString(R.string.error_field_required));
                    mOccupationEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mOccupationErrorTextView.setVisibility(View.GONE);
                    mOccupationEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            }*/
        }
    }

    public int countChar(String str, char c) {
        int count = 0;

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c)
                count++;
        }

        return count;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        personal_icon.setImageDrawable(getResources().getDrawable(R.drawable.addpatient_icon_done));
        address_icon.setImageDrawable(getResources().getDrawable(R.drawable.addresslocation_icon_done));
        other_icon.setImageDrawable(getResources().getDrawable(R.drawable.other_icon));

        frag3_btn_back.setOnClickListener(v -> {
            if (!mAbhaAddressEditText.getText().toString().isEmpty() && countChar(mAbhaAddressEditText.getText().toString(), '@') > 1) {
                mAbhaAddressErrorTextView.setText("Enter valid ABHA Address (e.g. address@sbx)");
                mAbhaAddressErrorTextView.setVisibility(View.VISIBLE);
                return;
            } else if (!mAbhaAddressEditText.getText().toString().isEmpty() && !mAbhaAddressEditText.getText().toString().endsWith("@sbx")) {
                mAbhaAddressErrorTextView.setText("Enter valid ABHA Address (e.g. address@sbx)");
                mAbhaAddressErrorTextView.setVisibility(View.VISIBLE);
                return;
            } else {
                mAbhaAddressErrorTextView.setVisibility(View.GONE);
                onBackInsertIntoPatientDTO();
            }
        });

        frag3_btn_next.setOnClickListener(v -> {
            if (!mAbhaAddressEditText.getText().toString().isEmpty() && countChar(mAbhaAddressEditText.getText().toString(), '@') > 1) {
                mAbhaAddressErrorTextView.setText("Enter valid ABHA Address (e.g. address@sbx)");
                mAbhaAddressErrorTextView.setVisibility(View.VISIBLE);
                return;

            } else if (!mAbhaAddressEditText.getText().toString().isEmpty() && !mAbhaAddressEditText.getText().toString().endsWith("@sbx")) {
                mAbhaAddressErrorTextView.setText("Enter valid ABHA Address (e.g. address@sbx)");
                mAbhaAddressErrorTextView.setVisibility(View.VISIBLE);
                return;
            } else {
                mAbhaAddressErrorTextView.setVisibility(View.GONE);
                onPatientCreateClicked();
                downloadAbhaCard();
            }
        });

        // caste spinner
        Resources res = getResources();
        try {
            String casteLanguage = "caste_" + sessionManager.getAppLanguage();
            int castes = res.getIdentifier(casteLanguage, "array", getActivity().getApplicationContext().getPackageName());
            if (castes != 0) {
                casteAdapter = ArrayAdapter.createFromResource(getActivity(),
                        castes, R.layout.simple_spinner_item_1);
                casteAdapter.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);
            }
            mCasteSpinner.setAdapter(casteAdapter);
            mCasteSpinner.setPopupBackgroundDrawable(getActivity().getDrawable(R.drawable.popup_menu_background));

        } catch (Exception e) {
//            Toast.makeText(this, R.string.education_values_missing, Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        // education spinner
        try {
            String educationLanguage = "education_" + sessionManager.getAppLanguage();
            int educations = res.getIdentifier(educationLanguage, "array", getActivity().getApplicationContext().getPackageName());
            if (educations != 0) {
                educationAdapter = ArrayAdapter.createFromResource(getActivity(),
                        educations, R.layout.simple_spinner_item_1);
                educationAdapter.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);
            }
            mEducationSpinner.setAdapter(educationAdapter);
            mEducationSpinner.setPopupBackgroundDrawable(getActivity().getDrawable(R.drawable.popup_menu_background));
        } catch (Exception e) {
//            Toast.makeText(this, R.string.education_values_missing, Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        // economic spinner
        try {
            String economicLanguage = "economic_" + sessionManager.getAppLanguage();
            int economics = res.getIdentifier(economicLanguage, "array", getActivity().getApplicationContext().getPackageName());
            if (economics != 0) {
                economicStatusAdapter = ArrayAdapter.createFromResource(getActivity(),
                        economics, R.layout.simple_spinner_item_1);
                economicStatusAdapter.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);
            }
            mEconomicstatusSpinner.setAdapter(economicStatusAdapter);
            mEconomicstatusSpinner.setPopupBackgroundDrawable(getActivity().getDrawable(R.drawable.popup_menu_background));
        } catch (Exception e) {
//            Toast.makeText(this, R.string.education_values_missing, Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        if (patientDTO.getSon_dau_wife() != null && !patientDTO.getSon_dau_wife().isEmpty())
            mRelationNameEditText.setText(patientDTO.getSon_dau_wife());
        Log.v(TAG, "relation: " + patientDTO.getSon_dau_wife());

        if (patientDTO.getOccupation() != null && !patientDTO.getOccupation().isEmpty())
            mOccupationEditText.setText(patientDTO.getOccupation());

        if (patientDTO.getNationalID() != null && !patientDTO.getNationalID().isEmpty())
            mNationalIDEditText.setText(patientDTO.getNationalID());

        if (patientDTO.getAbhaNumber() != null && !patientDTO.getAbhaNumber().isEmpty() && !patientDTO.getAbhaNumber().equals("NA")) {
            mAbhaNumberEditText.setText(patientDTO.getAbhaNumber());
            mAbhaNumberEditText.setEnabled(false);
        }

        if (patientDTO.getAbhaAddress() != null && !patientDTO.getAbhaAddress().isEmpty()) {
            mAbhaAddressEditText.setText(patientDTO.getAbhaAddress());
            mAbhaAddressEditText.setEnabled(false);
        }

        // setting screen in edit for spinners...
        if (fromThirdScreen || fromSecondScreen) {
            //caste
            if (patientDTO.getCaste() != null) {
                if (patientDTO.getCaste().equals(getResources().getString(R.string.not_provided)))
                    mCasteSpinner.setSelection(0);
//            else
//                caste_spinner.setSelection(casteAdapter.getPosition(patientDTO.getCaste()));

                else {
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                        String caste = switch_hi_caste_edit(patientDTO.getCaste());
                        mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                        String caste = switch_or_caste_edit(patientDTO.getCaste());
                        mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                        String caste = switch_te_caste_edit(patientDTO.getCaste());
                        mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                        String caste = switch_mr_caste_edit(patientDTO.getCaste());
                        mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                        String caste = switch_as_caste_edit(patientDTO.getCaste());
                        mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                        String caste = switch_ml_caste_edit(patientDTO.getCaste());
                        mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                        String caste = switch_kn_caste_edit(patientDTO.getCaste());
                        mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                        String caste = switch_ru_caste_edit(patientDTO.getCaste());
                        mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                        String caste = switch_gu_caste_edit(patientDTO.getCaste());
                        mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                        String caste = switch_bn_caste_edit(patientDTO.getCaste());
                        mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                        String caste = switch_ta_caste_edit(patientDTO.getCaste());
                        mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                    } else {
                        mCasteSpinner.setSelection(casteAdapter.getPosition(patientDTO.getCaste()));
                    }
                }
            }

            //education status
            if (patientDTO.getEducation() != null) {
                if (patientDTO.getEducation().equals(getResources().getString(R.string.not_provided)))
                    mEducationSpinner.setSelection(0);
//            else
//                education_spinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(patientDTO.getEducation()) : 0);

                else {
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                        String education = switch_hi_education_edit(patientDTO.getEducation());
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                        String education = switch_or_education_edit(patientDTO.getEducation());
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                        String education = switch_te_education_edit(patientDTO.getEducation());
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                        String education = switch_mr_education_edit(patientDTO.getEducation());
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                        String education = switch_as_education_edit(patientDTO.getEducation());
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                        String education = switch_gu_education_edit(patientDTO.getEducation());
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                        String education = switch_ta_education_edit(patientDTO.getEducation());
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                        String education = switch_bn_education_edit(patientDTO.getEducation());
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                        String education = switch_ml_education_edit(patientDTO.getEducation());
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                        String education = switch_kn_education_edit(patientDTO.getEducation());
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                        String education = switch_ru_education_edit(patientDTO.getEducation());
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else {
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(patientDTO.getEducation()) : 0);
                    }
                }
            }


            // economic statius
            if (patientDTO.getEconomic() != null) {
                if (patientDTO.getEconomic().equals(getResources().getString(R.string.not_provided)))
                    mEconomicstatusSpinner.setSelection(0);
//            else
//                economicstatus_spinner.setSelection(economicStatusAdapter.getPosition(patientDTO.getEconomic()));

                else {
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                        String economic = switch_hi_economic_edit(patientDTO.getEconomic());
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                        String economic = switch_or_economic_edit(patientDTO.getEconomic());
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                        String economic = switch_te_economic_edit(patientDTO.getEconomic());
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                        String economic = switch_mr_economic_edit(patientDTO.getEconomic());
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                        String economic = switch_as_economic_edit(patientDTO.getEconomic());
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                        String economic = switch_ml_economic_edit(patientDTO.getEconomic());
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                        String economic = switch_kn_economic_edit(patientDTO.getEconomic());
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                        String economic = switch_ru_economic_edit(patientDTO.getEconomic());
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                        String economic = switch_gu_economic_edit(patientDTO.getEconomic());
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                        String economic = switch_bn_economic_edit(patientDTO.getEconomic());
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                        String economic = switch_ta_economic_edit(patientDTO.getEconomic());
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else {
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(patientDTO.getEconomic()));
                    }
                }
            }


        }

    }

    private void downloadAbhaCard() {
        AbhaCardDownloadUtil util = new AbhaCardDownloadUtil(patientDTO);
        HashMap<String, String> tokenHashMap = getTokenHashmap();
        String token = tokenHashMap.get("token");
        String scope = tokenHashMap.get("scope");

        if (!util.isAbhaCardPresent() || sessionManager.getIsCommunicationNumberUsed()) {
            util.downloadAbhaCard(scope, token, accessToken, requireActivity());
        }
    }

    private HashMap<String, String> getTokenHashmap() {
        HashMap<String, String> responseHashMap = new HashMap<>();
        String token;
        String scope;

        if (otpVerificationResponse != null && otpVerificationResponse.getTokens() != null && otpVerificationResponse.getTokens().getToken() != null) {
            String responseToken = otpVerificationResponse.getTokens().getToken();
            token = responseToken.startsWith("Bearer") ? responseToken : "Bearer " + responseToken;
            scope = ABDMConstant.SCOPE_AADHAAR;
        } else if (abhaProfileResponse != null && abhaProfileResponse.getToken() != null) {
            token = abhaProfileResponse.getToken();
            scope = ABDMConstant.SCOPE_MOBILE;
        } else {
            token = xToken;
            scope = sessionManager.getTempScope();
        }

        responseHashMap.put("token", token);
        responseHashMap.put("scope", scope);
        return responseHashMap;
    }


    private void onBackInsertIntoPatientDTO() {
        patientDTO.setSon_dau_wife(mRelationNameEditText.getText().toString());
        patientDTO.setOccupation(mOccupationEditText.getText().toString());
        patientDTO.setNationalID(mNationalIDEditText.getText().toString());
        patientDTO.setAbhaNumber(mAbhaNumberEditText.getText().toString());
        if (!mAbhaAddressEditText.getText().toString().isEmpty() && !mAbhaAddressEditText.getText().toString().endsWith("@sbx"))
            patientDTO.setAbhaAddress(mAbhaAddressEditText.getText().toString() + "@sbx");
        else
            patientDTO.setAbhaAddress(mAbhaAddressEditText.getText().toString());
        patientDTO.setCaste(StringUtils.getValue(mCasteSpinner.getSelectedItem().toString()));
        patientDTO.setEducation(StringUtils.getValue(mEducationSpinner.getSelectedItem().toString()));
        patientDTO.setEconomic(StringUtils.getValue(mEconomicstatusSpinner.getSelectedItem().toString()));

        Bundle bundle = new Bundle();
        bundle.putSerializable("patientDTO", (Serializable) patientDTO);
        bundle.putBoolean("fromThirdScreen", true);
        bundle.putBoolean("patient_detail", patient_detail);

        // abha - start
        if (abhaProfileResponse != null)
            bundle.putSerializable(MOBILE_PAYLOAD, abhaProfileResponse);
        if (otpVerificationResponse != null)
            bundle.putSerializable(PAYLOAD, otpVerificationResponse);
        // abha - end

        bundle.putString("accessToken", accessToken);
        bundle.putString("xToken", xToken);
        bundle.putString("txnId", txnId);
        secondScreen.setArguments(bundle); // passing data to Fragment

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_firstscreen, secondScreen)
                .commit();
    }

    private void onPatientCreateClicked() {
        patientDTO.setSon_dau_wife(mRelationNameEditText.getText().toString());
        patientDTO.setOccupation(mOccupationEditText.getText().toString());
        patientDTO.setNationalID(mNationalIDEditText.getText().toString());
        patientDTO.setAbhaNumber(mAbhaNumberEditText.getText().toString());
        if (!mAbhaAddressEditText.getText().toString().isEmpty() && !mAbhaAddressEditText.getText().toString().endsWith("@sbx"))
            patientDTO.setAbhaAddress(mAbhaAddressEditText.getText().toString() + "@sbx");
        else
            patientDTO.setAbhaAddress(mAbhaAddressEditText.getText().toString());
        patientDTO.setCaste(StringUtils.getValue(mCasteSpinner.getSelectedItem().toString()));
        patientDTO.setEducation(StringUtils.getValue(mEducationSpinner.getSelectedItem().toString()));
        patientDTO.setEconomic(StringUtils.getValue(mEconomicstatusSpinner.getSelectedItem().toString()));

        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
        List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();

        Gson gson = new Gson();


        /**
         *  entering value in dataset start
         */

        String uuid = patientDTO.getUuid();

        if (patientDTO.getPhonenumber() != null && !patientDTO.getPhonenumber().isEmpty()) {
            // mobile no adding in patient attributes.
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Telephone Number"));
            patientAttributesDTO.setValue(StringUtils.getValue(patientDTO.getPhonenumber()));
            patientAttributesDTOList.add(patientAttributesDTO);
        }

        // son/daughter/wife of
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Son/wife/daughter"));
        patientAttributesDTO.setValue(StringUtils.getValue(mRelationNameEditText.getText().toString()));
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("NationalID"));
        patientAttributesDTO.setValue(StringUtils.getValue(mNationalIDEditText.getText().toString()));
        patientAttributesDTOList.add(patientAttributesDTO);

        // occupation
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("occupation"));
        patientAttributesDTO.setValue(StringUtils.getValue(mOccupationEditText.getText().toString()));
        patientAttributesDTOList.add(patientAttributesDTO);

        // caste
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("caste"));
        patientAttributesDTO.setValue(StringUtils.getProvided(mCasteSpinner));
        patientAttributesDTOList.add(patientAttributesDTO);
        Log.v(TAG, "values_caste: " + patientAttributesDTO.toString());

        // education
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Education Level"));
        patientAttributesDTO.setValue(StringUtils.getProvided(mEducationSpinner));
        patientAttributesDTOList.add(patientAttributesDTO);

        // economic status
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Economic Status"));
        patientAttributesDTO.setValue(StringUtils.getProvided(mEconomicstatusSpinner));
        patientAttributesDTOList.add(patientAttributesDTO);

        // createdDate
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("createdDate"));
        if (patientDTO.getCreatedDate() != null) {
            patientAttributesDTO.setValue(patientDTO.getCreatedDate());
        } else {
            patientAttributesDTO.setValue(DateAndTimeUtils.getTodaysDateInRequiredFormat("dd MMMM, yyyy"));
        }
        patientAttributesDTOList.add(patientAttributesDTO);

        //providerUUID
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("providerUUID"));
        if (patientDTO.getProviderUUID() != null) {
            patientAttributesDTO.setValue(patientDTO.getProviderUUID());
        } else {
            patientAttributesDTO.setValue(sessionManager.getProviderID());
        }
        patientAttributesDTOList.add(patientAttributesDTO);

        patientDTO.setPatientAttributesDTOList(patientAttributesDTOList);
        patientDTO.setSyncd(false);
        //  patientDTO.setSyncd(true);
        Logger.logD("patient json : ", "Json : " + gson.toJson(patientDTO, PatientDTO.class));


        // inserting data in db and uploading to server...
        try {
            Logger.logD(TAG, "insertpatinet ");
            boolean isPatientInserted = false;
            boolean isPatientImageInserted = false;

            if (patient_detail) {
                /*isPatientInserted = patientsDAO.insertPatientToDB(patientDTO, patientID_edit);
                isPatientImageInserted = imagesDAO.insertPatientProfileImages(patientDTO.getPatientPhoto(), patientID_edit);*/

                isPatientInserted = patientsDAO.updatePatientToDB_PatientDTO(patientDTO, patientDTO.getUuid(), patientAttributesDTOList);
                isPatientImageInserted = imagesDAO.updatePatientProfileImages(patientDTO.getPatientPhoto(), patientDTO.getUuid());
            } else {
                if (patientDTO.getIsExist() != null && patientDTO.getIsExist()) {
                    isPatientInserted = patientsDAO.updatePatientToDB_PatientDTO(patientDTO, patientDTO.getUuid(), patientAttributesDTOList);
                    isPatientImageInserted = imagesDAO.updatePatientProfileImages(patientDTO.getPatientPhoto(), patientDTO.getUuid());
                } else {
                    isPatientInserted = patientsDAO.insertPatientToDB(patientDTO, patientDTO.getUuid());
                    isPatientImageInserted = imagesDAO.insertPatientProfileImages(patientDTO.getPatientPhoto(), patientDTO.getUuid());
                }

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
                // abha - start
                if (abhaProfileResponse != null)
                    args.putSerializable(MOBILE_PAYLOAD, abhaProfileResponse);
                if (otpVerificationResponse != null)
                    args.putSerializable(PAYLOAD, otpVerificationResponse);
                // abha - end

                args.putString("accessToken", accessToken);
                args.putString("xToken", xToken);
                args.putString("txnId", txnId);
                intent.putExtra("BUNDLE", args);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }


    private void setscreen(String str) {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        Log.v(TAG, "relation: " + str);

        String patientSelection = "uuid=?";
        String[] patientArgs = {str};
        String[] patientColumns = {"uuid", "first_name", "middle_name", "last_name",
                "date_of_birth", "address1", "address2", "city_village", "state_province",
                "postal_code", "country", "phone_number", "gender", "sdw", "occupation", "patient_photo",
                "economic_status", "education_status", "caste", "abha_number", "abha_address"};
        Cursor idCursor = db.query("tbl_patient", patientColumns, patientSelection, patientArgs, null, null, null);
        if (idCursor.moveToFirst()) {
            do {
                patient1.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                patient1.setFirst_name(idCursor.getString(idCursor.getColumnIndexOrThrow("first_name")));
                patient1.setMiddle_name(idCursor.getString(idCursor.getColumnIndexOrThrow("middle_name")));
                patient1.setLast_name(idCursor.getString(idCursor.getColumnIndexOrThrow("last_name")));
                patient1.setDate_of_birth(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                patient1.setAddress1(idCursor.getString(idCursor.getColumnIndexOrThrow("address1")));
                patient1.setAddress2(idCursor.getString(idCursor.getColumnIndexOrThrow("address2")));
                patient1.setCity_village(idCursor.getString(idCursor.getColumnIndexOrThrow("city_village")));
                patient1.setState_province(idCursor.getString(idCursor.getColumnIndexOrThrow("state_province")));
                patient1.setPostal_code(idCursor.getString(idCursor.getColumnIndexOrThrow("postal_code")));
                patient1.setCountry(idCursor.getString(idCursor.getColumnIndexOrThrow("country")));
                patient1.setPhone_number(idCursor.getString(idCursor.getColumnIndexOrThrow("phone_number")));
                patient1.setGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                patient1.setSdw(idCursor.getString(idCursor.getColumnIndexOrThrow("sdw")));
                patient1.setOccupation(idCursor.getString(idCursor.getColumnIndexOrThrow("occupation")));
                patient1.setPatient_photo(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_photo")));
                patientDTO.setAbhaNumber(idCursor.getString(idCursor.getColumnIndexOrThrow("abha_number")));
                patientDTO.setAbhaAddress(idCursor.getString(idCursor.getColumnIndexOrThrow("abha_address")));

            } while (idCursor.moveToNext());
            idCursor.close();
        }
        String patientSelection1 = "patientuuid = ?";
        String[] patientArgs1 = {str};
        String[] patientColumns1 = {"value", "person_attribute_type_uuid"};
        final Cursor idCursor1 = db.query("tbl_patient_attribute", patientColumns1, patientSelection1, patientArgs1, null, null, null);
        String name = "";
        if (idCursor1.moveToFirst()) {
            do {
                try {
                    name = patientsDAO.getAttributesName(idCursor1.getString(idCursor1.getColumnIndexOrThrow("person_attribute_type_uuid")));
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

                if (name.equalsIgnoreCase("caste")) {
                    patient1.setCaste(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Telephone Number")) {
                    patient1.setPhone_number(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Education Level")) {
                    patient1.setEducation_level(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Economic Status")) {
                    patient1.setEconomic_status(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("occupation")) {
                    patient1.setOccupation(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Son/wife/daughter")) {
                    patient1.setSdw(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

            } while (idCursor1.moveToNext());
        }
        idCursor1.close();
    }
}

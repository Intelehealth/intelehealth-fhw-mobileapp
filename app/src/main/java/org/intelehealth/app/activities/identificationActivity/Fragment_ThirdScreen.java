package org.intelehealth.app.activities.identificationActivity;

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

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
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
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ImagesPushDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.exception.DAOException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Fragment_ThirdScreen extends Fragment {
    private static final String TAG = Fragment_ThirdScreen.class.getSimpleName();
    private PatientDTO patientDTO;
    private View view;
    SessionManager sessionManager = null;
    private ArrayAdapter<CharSequence> educationAdapter;
    private ArrayAdapter<CharSequence> casteAdapter;
    private ArrayAdapter<CharSequence> economicStatusAdapter;
    private EditText mRelationNameEditText, mOccupationEditText;
    private Spinner mCasteSpinner, mEducationSpinner, mEconomicstatusSpinner;
    private ImageView personal_icon, address_icon, other_icon;
    private Button frag3_btn_back, frag3_btn_next;
    private TextView mRelationNameErrorTextView, mOccupationErrorTextView, mCasteErrorTextView, mEducationErrorTextView, mEconomicErrorTextView;
    ImagesDAO imagesDAO = new ImagesDAO();
    private Fragment_SecondScreen secondScreen;
    boolean fromThirdScreen = false;
    Patient patient1 = new Patient();
    PatientsDAO patientsDAO = new PatientsDAO();
    String patientID_edit;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_registration_thirdscreen, container, false);
        return view;
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
        mOccupationEditText.addTextChangedListener(new MyTextWatcher(mOccupationEditText));

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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        secondScreen = new Fragment_SecondScreen();
        personal_icon.setImageDrawable(getResources().getDrawable(R.drawable.addpatient_icon_done));
        address_icon.setImageDrawable(getResources().getDrawable(R.drawable.addresslocation_icon_done));
        other_icon.setImageDrawable(getResources().getDrawable(R.drawable.other_icon));

        if (getArguments() != null) {
            patientDTO = (PatientDTO) getArguments().getSerializable("patientDTO");
            fromThirdScreen = getArguments().getBoolean("fromSecondScreen");
            patientID_edit = getArguments().getString("patientUuid");

            if (patientID_edit != null) {
                patient1.setUuid(patientID_edit);
                setscreen(patientID_edit);
            } else {
                patient1.setUuid(patientDTO.getUuid());
                setscreen(patientDTO.getUuid());
            }
        }


        frag3_btn_back.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("patientDTO", (Serializable) patientDTO);
            bundle.putBoolean("fromSecondScreen", true);
            secondScreen.setArguments(bundle); // passing data to Fragment

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_firstscreen, secondScreen)
                    .commit();
        });

        frag3_btn_next.setOnClickListener(v -> {
//                Intent intent = new Intent(getActivity(), PatientDetailActivity2.class);
//                startActivity(intent);
            onPatientCreateClicked();
        });

        // caste spinner
        Resources res = getResources();
        try {
            String casteLanguage = "caste_" + sessionManager.getAppLanguage();
            int castes = res.getIdentifier(casteLanguage, "array", getActivity().getApplicationContext().getPackageName());
            if (castes != 0) {
                casteAdapter = ArrayAdapter.createFromResource(getActivity(),
                        castes, android.R.layout.simple_spinner_dropdown_item);
            }
            mCasteSpinner.setAdapter(casteAdapter);
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
                        educations, android.R.layout.simple_spinner_dropdown_item);
            }
            mEducationSpinner.setAdapter(educationAdapter);
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
                        economics, android.R.layout.simple_spinner_dropdown_item);
            }
            mEconomicstatusSpinner.setAdapter(economicStatusAdapter);
        } catch (Exception e) {
//            Toast.makeText(this, R.string.education_values_missing, Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        if (patient1.getSdw() != null && !patient1.getSdw().isEmpty())
            mRelationNameEditText.setText(patient1.getSdw());
        Log.v(TAG, "relation: " + patient1.getSdw());
        if (patient1.getOccupation() != null && !patient1.getOccupation().isEmpty())
            mOccupationEditText.setText(patient1.getOccupation());

        // setting screen in edit for spinners...
        if (fromThirdScreen) {
            //caste
            if (patient1.getCaste() != null) {
                if (patient1.getCaste().equals(getResources().getString(R.string.not_provided)))
                    mCasteSpinner.setSelection(0);
//            else
//                caste_spinner.setSelection(casteAdapter.getPosition(patient1.getCaste()));

                else {
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                        String caste = switch_hi_caste_edit(patient1.getCaste());
                        mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                        String caste = switch_or_caste_edit(patient1.getCaste());
                        mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                        String caste = switch_te_caste_edit(patient1.getCaste());
                        mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                        String caste = switch_mr_caste_edit(patient1.getCaste());
                        mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                        String caste = switch_as_caste_edit(patient1.getCaste());
                        mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                        String caste = switch_ml_caste_edit(patient1.getCaste());
                        mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                        String caste = switch_kn_caste_edit(patient1.getCaste());
                        mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                        String caste = switch_ru_caste_edit(patient1.getCaste());
                        mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                        String caste = switch_gu_caste_edit(patient1.getCaste());
                        mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                        String caste = switch_bn_caste_edit(patient1.getCaste());
                        mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                        String caste = switch_ta_caste_edit(patient1.getCaste());
                        mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                    } else {
                        mCasteSpinner.setSelection(casteAdapter.getPosition(patient1.getCaste()));
                    }
                }
            }

            //education status
            if (patient1.getEducation_level() != null) {
                if (patient1.getEducation_level().equals(getResources().getString(R.string.not_provided)))
                    mEducationSpinner.setSelection(0);
//            else
//                education_spinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(patient1.getEducation_level()) : 0);

                else {
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                        String education = switch_hi_education_edit(patient1.getEducation_level());
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                        String education = switch_or_education_edit(patient1.getEducation_level());
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                        String education = switch_te_education_edit(patient1.getEducation_level());
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                        String education = switch_mr_education_edit(patient1.getEducation_level());
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                        String education = switch_as_education_edit(patient1.getEducation_level());
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                        String education = switch_gu_education_edit(patient1.getEducation_level());
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                        String education = switch_ta_education_edit(patient1.getEducation_level());
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                        String education = switch_bn_education_edit(patient1.getEducation_level());
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                        String education = switch_ml_education_edit(patient1.getEducation_level());
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                        String education = switch_kn_education_edit(patient1.getEducation_level());
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                        String education = switch_ru_education_edit(patient1.getEducation_level());
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else {
                        mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(patient1.getEducation_level()) : 0);
                    }
                }
            }


            // economic statius
            if (patient1.getEconomic_status() != null) {
                if (patient1.getEconomic_status().equals(getResources().getString(R.string.not_provided)))
                    mEconomicstatusSpinner.setSelection(0);
//            else
//                economicstatus_spinner.setSelection(economicStatusAdapter.getPosition(patient1.getEconomic_status()));

                else {
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                        String economic = switch_hi_economic_edit(patient1.getEconomic_status());
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                        String economic = switch_or_economic_edit(patient1.getEconomic_status());
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                        String economic = switch_te_economic_edit(patient1.getEconomic_status());
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                        String economic = switch_mr_economic_edit(patient1.getEconomic_status());
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                        String economic = switch_as_economic_edit(patient1.getEconomic_status());
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                        String economic = switch_ml_economic_edit(patient1.getEconomic_status());
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                        String economic = switch_kn_economic_edit(patient1.getEconomic_status());
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                        String economic = switch_ru_economic_edit(patient1.getEconomic_status());
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                        String economic = switch_gu_economic_edit(patient1.getEconomic_status());
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                        String economic = switch_bn_economic_edit(patient1.getEconomic_status());
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                        String economic = switch_ta_economic_edit(patient1.getEconomic_status());
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else {
                        mEconomicstatusSpinner.setSelection(economicStatusAdapter.getPosition(patient1.getEconomic_status()));
                    }
                }
            }


        }

    }

    private void onPatientCreateClicked() {
        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
        List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();

        Gson gson = new Gson();
        boolean cancel = false;
        View focusView = null;

        // validation - start
        /*if (mRelationNameEditText.getText().toString().equals("")) {
            mRelationNameErrorTextView.setVisibility(View.VISIBLE);
            mRelationNameErrorTextView.setText(getString(R.string.error_field_required));
            mRelationNameEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mRelationNameEditText.requestFocus();
            return;
        } else {
            mRelationNameErrorTextView.setVisibility(View.GONE);
            mRelationNameEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }

        if (mOccupationEditText.getText().toString().equals("")) {
            mOccupationErrorTextView.setVisibility(View.VISIBLE);
            mOccupationErrorTextView.setText(getString(R.string.error_field_required));
            mOccupationEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mOccupationEditText.requestFocus();
            return;
        } else {
            mOccupationErrorTextView.setVisibility(View.GONE);
            mOccupationEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }

        if (mCasteSpinner.getSelectedItemPosition() == 0) {
            mCasteErrorTextView.setVisibility(View.VISIBLE);
            mCasteErrorTextView.setText(getString(R.string.error_field_required));
            mCasteSpinner.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mCasteSpinner.requestFocus();
            return;
        } else {
            mCasteErrorTextView.setVisibility(View.GONE);
            mCasteSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
        }

        if (mEducationSpinner.getSelectedItemPosition() == 0) {
            mEducationErrorTextView.setVisibility(View.VISIBLE);
            mEducationErrorTextView.setText(getString(R.string.error_field_required));
            mEducationSpinner.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mEducationSpinner.requestFocus();
            return;
        } else {
            mEducationErrorTextView.setVisibility(View.GONE);
            mEducationSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
        }

        if (mEconomicstatusSpinner.getSelectedItemPosition() == 0) {
            mEconomicErrorTextView.setVisibility(View.VISIBLE);
            mEconomicErrorTextView.setText(getString(R.string.error_field_required));
            mEconomicstatusSpinner.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mEconomicstatusSpinner.requestFocus();
            return;
        } else {
            mEconomicErrorTextView.setVisibility(View.GONE);
            mEconomicstatusSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
        }*/
        // validation - end


        /**
         *  entering value in dataset start
         */

            String uuid = patientDTO.getUuid();

            // mobile no adding in patient attributes.
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Telephone Number"));
            patientAttributesDTO.setValue(StringUtils.getValue(patientDTO.getPhonenumber()));
            patientAttributesDTOList.add(patientAttributesDTO);

            // son/daughter/wife of
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Son/wife/daughter"));
            patientAttributesDTO.setValue(StringUtils.getValue(mRelationNameEditText.getText().toString()));
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

            patientDTO.setPatientAttributesDTOList(patientAttributesDTOList);
            patientDTO.setSyncd(false); // todo:uncomment later
            //  patientDTO.setSyncd(true); // todo: remove ...just for testing.
            Logger.logD("patient json : ", "Json : " + gson.toJson(patientDTO, PatientDTO.class));


        // inserting data in db and uploading to server...
        try {
            Logger.logD(TAG, "insertpatinet ");
            boolean isPatientInserted = false;
            boolean isPatientImageInserted = false;

            if (patientID_edit != null) {
                /*isPatientInserted = patientsDAO.insertPatientToDB(patientDTO, patientID_edit);
                isPatientImageInserted = imagesDAO.insertPatientProfileImages(patientDTO.getPatientPhoto(), patientID_edit);*/

                isPatientInserted = patientsDAO.updatePatientToDB_PatientDTO(patientDTO, patientID_edit, patientAttributesDTOList);
                isPatientImageInserted = imagesDAO.updatePatientProfileImages(patientDTO.getPatientPhoto(), patientID_edit);
            } else {
                isPatientInserted = patientsDAO.insertPatientToDB(patientDTO, patientDTO.getUuid());
                isPatientImageInserted = imagesDAO.insertPatientProfileImages(patientDTO.getPatientPhoto(), patientDTO.getUuid());
            }

            if (NetworkConnection.isOnline(getActivity().getApplication())) { // todo: uncomment later jsut for testing added.
                SyncDAO syncDAO = new SyncDAO();
                ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
                boolean push = syncDAO.pushDataApi("frag third");
                boolean pushImage = imagesPushDAO.patientProfileImagesPush();
            }

            if (isPatientInserted && isPatientImageInserted) {
                Logger.logD(TAG, "inserted");
                Intent intent = new Intent(getActivity().getApplication(), PatientDetailActivity2.class);
                intent.putExtra("patientUuid", patientDTO.getUuid());
                intent.putExtra("patientName", patientDTO.getFirstname() + " " + patientDTO.getLastname());
                intent.putExtra("tag", "newPatient");
                intent.putExtra("hasPrescription", "false");
                //   i.putExtra("privacy", privacy_value); // todo: uncomment later.
                //   Log.d(TAG, "Privacy Value on (Identification): " + privacy_value); //privacy value transferred to PatientDetail activity.
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                Bundle args = new Bundle();
                args.putSerializable("patientDTO", (Serializable) patientDTO);
                intent.putExtra("BUNDLE", args);
                intent.putExtra("patientUuid", patientID_edit);
                getActivity().startActivity(intent);
                startActivity(intent);
            } else {
                Toast.makeText(getActivity(), "Error of adding the data", Toast.LENGTH_SHORT).show();
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }


    private void setscreen(String str) {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        Log.v(TAG, "relation: " + str);

        String patientSelection = "uuid=?";
        String[] patientArgs = {str};
        String[] patientColumns = {"uuid", "first_name", "middle_name", "last_name",
                "date_of_birth", "address1", "address2", "city_village", "state_province",
                "postal_code", "country", "phone_number", "gender", "sdw", "occupation", "patient_photo",
                "economic_status", "education_status", "caste"};
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

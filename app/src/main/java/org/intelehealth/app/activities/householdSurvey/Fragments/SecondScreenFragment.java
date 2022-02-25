package org.intelehealth.app.activities.householdSurvey.Fragments;
/**
 * Created by Prajwal Maruti Waingankar on 14-02-2022
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

import static org.intelehealth.app.activities.householdSurvey.HouseholdSurveyActivity.patientAttributesDTOList;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.ImagesPushDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.databinding.FragmentSecondScreenBinding;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

public class SecondScreenFragment extends Fragment implements View.OnClickListener {
    EditText nameHOHEditText, noOfSmartPhoneEditText, noOfFeaturePhoneEditText, noOfEarningEditText;
    RadioButton maleHoHRadio, femaleHoHRadio;
    Spinner religionSpinner, casteSpinner;
    ArrayAdapter<CharSequence> religionAdapter, casteAdapter;
    MaterialCheckBox saleCerealCheckbox, saleAnimalsCheckbox, agriLaborCheckbox, salariedWorkerCheckbox, selfEmployedCheckbox, dailyLaborCheckbox,
            nregaCheckbox, seasonalLaborCheckbox, noPaidWorkCheckbox, pensionCheckbox, remittancesCheckbox, otherCheckbox;
    SessionManager sessionManager = null;
    String patientUuid;
    ImageButton next_button;
    private static final String TAG = SecondScreenFragment.class.getSimpleName();
    TextInputLayout otherTIL;
    TextInputEditText otherEditText;
    private FragmentSecondScreenBinding binding;
    private ArrayList<View> mandatoryFields = new ArrayList<>();
    PatientsDAO patientsDAO = new PatientsDAO();

    public SecondScreenFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
        }

        sessionManager = new SessionManager(getActivity());
        String language = sessionManager.getAppLanguage();
        Log.d("lang", "lang: " + language);
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getActivity().getBaseContext().getResources().updateConfiguration(config,
                    getActivity().getBaseContext().getResources().getDisplayMetrics());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        View rootView =  inflater.inflate(R.layout.fragment_second_screen, container, false);
        binding = FragmentSecondScreenBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();
        initUI(rootView);
        ClickListener();
        setData(patientUuid);
       /* ImageButton next_button = rootView.findViewById(R.id.next_button);
        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.framelayout_container, new ThirdScreenFragment())
                        .commit();
            }
        });*/
        return rootView;
    }

    private void initUI(View rootView) {
        // nameHOHEditText, noOfSmartPhoneEditText, noOfFeaturePhoneEditText, noOfEarningEditText;
        nameHOHEditText = rootView.findViewById(R.id.head_of_household_name_EditText);
        noOfSmartPhoneEditText = rootView.findViewById(R.id.editText_smartphones);
        noOfFeaturePhoneEditText = rootView.findViewById(R.id.editTextFeaturePhone);
        noOfEarningEditText = rootView.findViewById(R.id.editText_earningmember);
        next_button = rootView.findViewById(R.id.next_button);
        otherTIL = rootView.findViewById(R.id.other_sources_of_income_layout);
        otherEditText = rootView.findViewById(R.id.other_sources_of_income_edit_text);
        // TODO: Similarly init other views

        saleCerealCheckbox = rootView.findViewById(R.id.sale_of_cereal_production_checkbox);
        saleAnimalsCheckbox = rootView.findViewById(R.id.sale_of_animal_products_checkbox);
        agriLaborCheckbox = rootView.findViewById(R.id.agricultural_wage_labor_checkbox);
        salariedWorkerCheckbox = rootView.findViewById(R.id.salaried_worker_checkbox);
        selfEmployedCheckbox = rootView.findViewById(R.id.self_employed_checkbox);
        dailyLaborCheckbox = rootView.findViewById(R.id.daily_labor_checkbox);
        nregaCheckbox = rootView.findViewById(R.id.nrega_checkbox);
        seasonalLaborCheckbox = rootView.findViewById(R.id.seasonal_labor_checkbox);
        noPaidWorkCheckbox = rootView.findViewById(R.id.no_paid_work_checkbox);
        pensionCheckbox = rootView.findViewById(R.id.pension_checkbox);
        remittancesCheckbox = rootView.findViewById(R.id.remittance_checkbox);
        otherCheckbox = rootView.findViewById(R.id.other_income_checkbox);
        mandatoryFields.addAll(Arrays.asList(binding.headOfHouseholdNameEditText, binding.headOfHouseholdGenderRadioGroup, binding.religionDropDown, binding.casteDropDown, binding.editTextSmartphones, binding.editTextFeaturePhone, binding.editTextEarningmember));
    }

    private void ClickListener() {
        // TODO: InitViews for this below views and then uncomment.
        next_button.setOnClickListener(this);

       /* maleHoHRadio.setOnClickListener(this);
        femaleHoHRadio.setOnClickListener(this);

        religionSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        casteSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        saleCerealCheckbox.setOnClickListener(this);
        saleAnimalsCheckbox.setOnClickListener(this);
        agriLaborCheckbox.setOnClickListener(this);
        salariedWorkerCheckbox.setOnClickListener(this);
        selfEmployedCheckbox.setOnClickListener(this);
        dailyLaborCheckbox.setOnClickListener(this);
        nregaCheckbox.setOnClickListener(this);
        seasonalLaborCheckbox.setOnClickListener(this);
        noPaidWorkCheckbox.setOnClickListener(this);
        pensionCheckbox.setOnClickListener(this);
        remittancesCheckbox.setOnClickListener(this);
        otherCheckbox.setOnClickListener(this);*/

        binding.otherIncomeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.otherSourcesOfIncomeLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        binding.religionDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 6) {
                    binding.otherReligionLayout.setVisibility(View.VISIBLE);
                } else {
                    binding.otherReligionLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        // TODO: InitViews for this then uncomment.
        boolean checked = false;
        if (view.getId() != R.id.next_button)
            checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.next_button:
                try {
                    insertData();
                } catch (DAOException e) {
                    e.printStackTrace();
                }
        }
    }

    private void insertData() throws DAOException {
        if (!StringUtils.validateFields(mandatoryFields)) {
            Toast.makeText(getContext(), R.string.fill_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
        // List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();

        //householdHeadName
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdHeadName"));
        patientAttributesDTO.setValue(nameHOHEditText.getText().toString());
        patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...


        //householdHeadGender
        if (binding.headOfHouseholdGenderRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdHeadGender"));
            patientAttributesDTO.setValue(binding.headOfHouseholdGenderRadioGroup.getCheckedRadioButtonId() == binding.headOfHouseholdGenderMale.getId() ? binding.headOfHouseholdGenderMale.getText().toString() : binding.headOfHouseholdGenderFemale.getText().toString());
            patientAttributesDTOList.add(patientAttributesDTO);
        }

        //householdHeadReligion
        if (binding.religionDropDown.getSelectedItemPosition() != 0) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdHeadReligion"));
            patientAttributesDTO.setValue(binding.religionDropDown.getSelectedItem().toString());
            patientAttributesDTOList.add(patientAttributesDTO);
        }

        //householdHeadCaste
        if (binding.casteDropDown.getSelectedItemPosition() != 0) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdHeadCaste"));
            patientAttributesDTO.setValue(binding.casteDropDown.getSelectedItem().toString());
            patientAttributesDTOList.add(patientAttributesDTO);
        }

        //noOfSmartphones
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("noOfSmartphones"));
        patientAttributesDTO.setValue(binding.editTextSmartphones.getText().toString());
        patientAttributesDTOList.add(patientAttributesDTO);

        //noOfEarningMembers
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("noOfEarningMembers"));
        patientAttributesDTO.setValue(binding.editTextEarningmember.getText().toString());
        patientAttributesDTOList.add(patientAttributesDTO);

        //noOfFeaturePhones
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("noOfFeaturePhones"));
        patientAttributesDTO.setValue(binding.editTextFeaturePhone.getText().toString());
        patientAttributesDTOList.add(patientAttributesDTO);

        //primarySourceOfIncome
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("primarySourceOfIncome"));
        patientAttributesDTO.setValue(StringUtils.getSelectedCheckboxes(binding.checkboxLinearLayout));
        patientAttributesDTOList.add(patientAttributesDTO);


        Gson gson = new Gson();
        gson.toJson(patientAttributesDTOList);
        Log.v("screen", "secondscreen: \n" + gson.toJson(patientAttributesDTOList));

        // TODO: this logic just for testing purpose have added here. Once all screens is done than at the end of 7th screen
        //  by clicking on SUBMIT button add this code on that button clicklistener...
//        boolean isPatientUpdated = patientsDAO.SurveyupdatePatientToDB(patientUuid, patientAttributesDTOList);
//        if (NetworkConnection.isOnline(getActivity().getApplication())) {
//            SyncDAO syncDAO = new SyncDAO();
//            boolean ispush = syncDAO.pushDataApi();
//
//        }
//        // Upto here so that data is stored in localdb and pushed by clicking on FAB...
//
//        if (isPatientUpdated) {
//            getFragmentManager().beginTransaction()
//                    .replace(R.id.framelayout_container, new ThirdScreenFragment())
//                    .commit();
//        }

        getFragmentManager().beginTransaction()
                .replace(R.id.framelayout_container, new ThirdScreenFragment())
                .commit();

    }

    private void setData(String patientUuid) {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        String patientSelection1 = "patientuuid = ?";
        String[] patientArgs1 = {patientUuid};
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
                if (name.equalsIgnoreCase("householdHeadName")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null)
                        nameHOHEditText.setText(value1);
                }
                if (name.equalsIgnoreCase("householdHeadGender")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if(value1!=null)
                        defaultSelectRB(binding.headOfHouseholdGenderRadioGroup, value1);
                }
                if (name.equalsIgnoreCase("householdHeadReligion")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null)
                        binding.religionDropDown.setSelection(getIndex(binding.religionDropDown, value1));
                }
                if (name.equalsIgnoreCase("householdHeadCaste")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null)
                        binding.casteDropDown.setSelection(getIndex(binding.casteDropDown, value1));
                }
                if (name.equalsIgnoreCase("noOfSmartphones")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null)
                        noOfSmartPhoneEditText.setText(value1);
                }
                if (name.equalsIgnoreCase("noOfEarningMembers")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null)
                        noOfEarningEditText.setText(value1);
                }
                if (name.equalsIgnoreCase("noOfFeaturePhones")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null)
                        noOfFeaturePhoneEditText.setText(value1);
                }
                if (name.equalsIgnoreCase("primarySourceOfIncome")) {
                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(getString(R.string.sale_of_cereal_production)))
                        saleCerealCheckbox.setChecked(true);
                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(getString(R.string.sale_of_animals_or_animal_products)))
                        saleAnimalsCheckbox.setChecked(true);
                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(getString(R.string.agricultural_wage_labor_employed_for_farm_work)))
                        agriLaborCheckbox.setChecked(true);
                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(getString(R.string.salaried_worker_fixed_monthly_salary)))
                        salariedWorkerCheckbox.setChecked(true);
                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(getString(R.string.self_employed_non_agricultural_petty_business)))
                        selfEmployedCheckbox.setChecked(true);
                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(getString(R.string.daily_labor_unskilled_work_agricultural_non_agricultural)))
                        dailyLaborCheckbox.setChecked(true);
                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(getString(R.string.nrega)))
                        nregaCheckbox.setChecked(true);
                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(getString(R.string.seasonal_labor)))
                        seasonalLaborCheckbox.setChecked(true);
                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(getString(R.string.no_paid_work)))
                        noPaidWorkCheckbox.setChecked(true);
                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(getString(R.string.pension)))
                        pensionCheckbox.setChecked(true);
                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(getString(R.string.remittances_checkbox)))
                        remittancesCheckbox.setChecked(true);
                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(getString(R.string.other_please_specify)))
                        otherCheckbox.setChecked(true);
                }
            } while (idCursor1.moveToNext());
        }
        idCursor1.close();

    }

    private int getIndex(Spinner spinner, String s){
        for(int i=0;i<spinner.getCount();i++)
        {
            if(spinner.getItemAtPosition(i).toString().equalsIgnoreCase(s))
                return i;
        }
        return 0;
    }

    void defaultSelectRB(RadioGroup radioGroup, String s) {
        int childCount = radioGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            RadioButton rButton = (RadioButton) radioGroup.getChildAt(i);
            if (rButton.getText().toString().equalsIgnoreCase(s)) {
                rButton.setChecked(true);
                return;
            }

        }
    }
}
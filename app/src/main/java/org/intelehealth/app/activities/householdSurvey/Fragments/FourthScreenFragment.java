package org.intelehealth.app.activities.householdSurvey.Fragments;
/**
 * Created by Prajwal Maruti Waingankar on 14-02-2022
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

import android.content.Context;
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
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.databinding.FragmentFourthScreenBinding;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.intelehealth.app.activities.householdSurvey.HouseholdSurveyActivity.patientAttributesDTOList;
import static org.intelehealth.app.utilities.StringUtils.getCultivableLand;
import static org.intelehealth.app.utilities.StringUtils.getCultivableLandEdit;
import static org.intelehealth.app.utilities.StringUtils.getEnglishConfiguration;
import static org.intelehealth.app.utilities.StringUtils.getMarathiConfiguration;

public class FourthScreenFragment extends Fragment {

    private FragmentFourthScreenBinding binding;
    private String patientUuid;
    private SessionManager sessionManager;
    private List<View> mandatoryFields = new ArrayList<>();
    PatientsDAO patientsDAO = new PatientsDAO();

    public FourthScreenFragment() {
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
//        View rootView =  inflater.inflate(R.layout.fragment_fourth_screen, container, false);
        binding = FragmentFourthScreenBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();
        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    insertData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        binding.prevButton.setOnClickListener(view -> {
            getActivity().onBackPressed();
        });
        binding.cultivableLandRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            binding.cultivableLandLayout.setVisibility(View.VISIBLE);
        });

        binding.annualHealthExpenditureRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.greater_than_thirty_thousand_radio_button) {
                binding.moreThanThirtyThousandLayout.setVisibility(View.VISIBLE);
            } else {
                binding.moreThanThirtyThousandLayout.setVisibility(View.GONE);
            }
        });

        mandatoryFields.addAll(Arrays.asList(binding.cultivableLandRadioGroup, binding.averageAnnualHouseholdIncomeRadioGroup, binding.monthlyFoodExpenditureRadioGroup, binding.annualHealthExpenditureRadioGroup, binding.annualEducationExpenditureRadioGroup
                , binding.annualClothingExpenditureRadioGroup, binding.monthlyIntoxicantsExpenditureRadioGroup, binding.bplCardCouponRadioGroup, binding.antodayaCardCouponRadioGroup, binding.rsbyCardRadioGroup, binding.mgnregaCardRadioGroup));

        getPatientUuidsForHouseholdValue(patientUuid);
        // setData(patientUuid);
        return rootView;
    }

    public void getPatientUuidsForHouseholdValue(String patientUuid) {
        String houseHoldValue = "";
        try {
            houseHoldValue = patientsDAO.getHouseHoldValue(patientUuid);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        if (!houseHoldValue.equalsIgnoreCase("")) {
            //Fetch all patient UUID from houseHoldValue
            try {
                List<String> patientUUIDs = new ArrayList<>(patientsDAO.getPatientUUIDs(houseHoldValue));
                Log.e("patientUUIDss", "" + patientUUIDs);
                for (int i = 0; i < patientUUIDs.size(); i++) {
                    setData(patientUUIDs.get(i));
                }
            } catch (Exception e) {
            }
        }
    }


    private void insertData() throws Exception {
//        if (!StringUtils.validateFields(mandatoryFields)) {
//            Toast.makeText(getContext(), R.string.fill_required_fields, Toast.LENGTH_SHORT).show();
//            return;
//        }

        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
        // List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();

        //householdCultivableLand
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdCultivableLand"));
        String cultivableLand = "-";

        if (binding.cultivableLandRadioGroup.getCheckedRadioButtonId() != -1) {
            cultivableLand = StringUtils.getValue(binding.cultivableLandEditText.getText().toString()) + " " +
                    getCultivableLand(((RadioButton) binding.cultivableLandRadioGroup.findViewById(binding.cultivableLandRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                            requireContext(), sessionManager.getAppLanguage());
        }

        patientAttributesDTO.setValue(cultivableLand);
        patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...

        //averageAnnualHouseholdIncome
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("averageAnnualHouseholdIncome"));
        String averageAnnualHouseholdIncome = "-";

        if (binding.averageAnnualHouseholdIncomeRadioGroup.getCheckedRadioButtonId() != -1) {
            averageAnnualHouseholdIncome = ((RadioButton) binding.averageAnnualHouseholdIncomeRadioGroup.findViewById(binding.averageAnnualHouseholdIncomeRadioGroup.getCheckedRadioButtonId())).getText().toString();
        }

        patientAttributesDTO.setValue(averageAnnualHouseholdIncome);
        patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...


        //monthlyFoodExpenditure
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("monthlyFoodExpenditure"));
        String monthlyFoodExpenditure = "-";

        if (binding.monthlyFoodExpenditureRadioGroup.getCheckedRadioButtonId() != -1) {
            monthlyFoodExpenditure = ((RadioButton) binding.monthlyFoodExpenditureRadioGroup.findViewById(binding.monthlyFoodExpenditureRadioGroup.getCheckedRadioButtonId())).getText().toString();
        }

        patientAttributesDTO.setValue(monthlyFoodExpenditure);
        patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...

        //annualHealthExpenditure
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("annualHealthExpenditure"));
        String annualHealthExpenditure = "-";

        if (binding.annualHealthExpenditureRadioGroup.getCheckedRadioButtonId() != -1) {
            annualHealthExpenditure = ((RadioButton) binding.annualHealthExpenditureRadioGroup.findViewById(binding.annualHealthExpenditureRadioGroup.getCheckedRadioButtonId())).getText().toString();
        }

        patientAttributesDTO.setValue(annualHealthExpenditure);
        patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...

        //annualEducationExpenditure
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("annualEducationExpenditure"));
        String annualEducationExpenditure = "-";

        if (binding.annualEducationExpenditureRadioGroup.getCheckedRadioButtonId() != -1) {
            annualEducationExpenditure = ((RadioButton) binding.annualEducationExpenditureRadioGroup.findViewById(binding.annualEducationExpenditureRadioGroup.getCheckedRadioButtonId())).getText().toString();
        }

        patientAttributesDTO.setValue(annualEducationExpenditure);
        patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...

        //annualClothingExpenditure
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("annualClothingExpenditure"));
        String annualClothingExpenditure = "-";

        if (binding.annualClothingExpenditureRadioGroup.getCheckedRadioButtonId() != -1) {
            annualClothingExpenditure = ((RadioButton) binding.annualClothingExpenditureRadioGroup.findViewById(binding.annualClothingExpenditureRadioGroup.getCheckedRadioButtonId())).getText().toString();
        }

        patientAttributesDTO.setValue(annualClothingExpenditure);
        patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...


        //monthlyIntoxicantsExpenditure
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("monthlyIntoxicantsExpenditure"));
        String monthlyIntoxicantExpenditure = "-";

        if (binding.monthlyIntoxicantsExpenditureRadioGroup.getCheckedRadioButtonId() != -1) {
            monthlyIntoxicantExpenditure = ((RadioButton) binding.monthlyIntoxicantsExpenditureRadioGroup.findViewById(binding.monthlyIntoxicantsExpenditureRadioGroup.getCheckedRadioButtonId())).getText().toString();
        }

        patientAttributesDTO.setValue(monthlyIntoxicantExpenditure);
        patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...


        //householdBPLCardStatus
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdBPLCardStatus"));
        String householdBPLCardStatus = "-";

        if (binding.bplCardCouponRadioGroup.getCheckedRadioButtonId() != -1) {
            householdBPLCardStatus = StringUtils.getCardStatus(
                    ((RadioButton) binding.bplCardCouponRadioGroup.findViewById(binding.bplCardCouponRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                    sessionManager.getAppLanguage(),
                    requireContext()
            );
        }

        patientAttributesDTO.setValue(householdBPLCardStatus);
        patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...

        //householdAntodayaCardStatus
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdAntodayaCardStatus"));
        String householdAntodayaCardStatus = "-";

        if (binding.antodayaCardCouponRadioGroup.getCheckedRadioButtonId() != -1) {
            householdAntodayaCardStatus = StringUtils.getCardStatus(
                    ((RadioButton) binding.antodayaCardCouponRadioGroup.findViewById(binding.antodayaCardCouponRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                    sessionManager.getAppLanguage(),
                    requireContext()
            );
        }


        patientAttributesDTO.setValue(householdAntodayaCardStatus);
        patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...

        //householdRSBYCardStatus
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdRSBYCardStatus"));
        String householdRSBYCardStatus = "-";

        if (binding.rsbyCardRadioGroup.getCheckedRadioButtonId() != -1) {
            householdRSBYCardStatus = StringUtils.getCardStatus(
                    ((RadioButton) binding.rsbyCardRadioGroup.findViewById(binding.rsbyCardRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                    sessionManager.getAppLanguage(),
                    requireContext()
            );
        }

        patientAttributesDTO.setValue(householdRSBYCardStatus);
        patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...

        //householdMGNREGACardStatus
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdMGNREGACardStatus"));
        String householdMGNREGACardStatus = "-";

        if (binding.mgnregaCardRadioGroup.getCheckedRadioButtonId() != -1) {
            householdMGNREGACardStatus = StringUtils.getCardStatus(
                    ((RadioButton) binding.mgnregaCardRadioGroup.findViewById(binding.mgnregaCardRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                    sessionManager.getAppLanguage(),
                    requireContext()
            );
        }

        patientAttributesDTO.setValue(householdMGNREGACardStatus);
        patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...

        Gson gson = new Gson();
        gson.toJson(patientAttributesDTOList);
        Log.v("screen", "secondscreen: \n" + gson.toJson(patientAttributesDTOList));

        // TODO: this logic just for testing purpose have added here. Once all screens is done than at the end of 7th screen
        //  by clicking on SUBMIT button add this code on that button clicklistener...
        boolean isPatientUpdated = patientsDAO.SurveyupdatePatientToDB(patientUuid, patientAttributesDTOList);
//        if (NetworkConnection.isOnline(getActivity().getApplication())) {
//            SyncDAO syncDAO = new SyncDAO();
//            boolean ispush = syncDAO.pushDataApi();
//
//        }
//        // Upto here so that data is stored in localdb and pushed by clicking on FAB...
//
//        if (isPatientUpdated) {
//            getFragmentManager().beginTransaction()
//                    .replace(R.id.framelayout_container, new FifthScreenFragment())
//                    .commit();
//        }

        getFragmentManager().beginTransaction()
                .replace(R.id.framelayout_container, new FifthScreenFragment())
                .addToBackStack(null)
                .commit();

    }

    private void setData(String patientUuid) {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        Context updatedContext;
        if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
            Configuration configuration = getEnglishConfiguration();
            updatedContext = requireContext().createConfigurationContext(configuration);
        } else {
            updatedContext = requireContext();
        }

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
                if (name.equalsIgnoreCase("householdCultivableLand")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
//                    binding.cultivableLandEditText.setText(value1); // set value to the editText
                    if (value1 != null) {
                        String[] splitString = value1.split(" ");
                        splitString[1] = getCultivableLandEdit(splitString[1], requireContext(), sessionManager.getAppLanguage());

                        if (splitString[1].equalsIgnoreCase(getString(R.string.hectare))) {
                            binding.hectareRadioButton.setChecked(true);
                        }
                        if (splitString[1].equalsIgnoreCase(getString(R.string.acre))) {
                            binding.acreRadioButton.setChecked(true);
                        }
                        if (splitString[1].equalsIgnoreCase(getString(R.string.bigha))) {
                            binding.bighaRadioButton.setChecked(true);
                        }
                        if (splitString[1].equalsIgnoreCase(getString(R.string.gunta))) {
                            binding.guntaRadioButton.setChecked(true);
                        }

                        if (!splitString[0].equalsIgnoreCase("-"))
                            binding.cultivableLandEditText.setText(splitString[0]);
                    }
                }

                if (name.equalsIgnoreCase("averageAnnualHouseholdIncome")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null) {
                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.zero_thirty_thousand)))
                            binding.annualHouseholdIncome0.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.thirty_fifty_thousand)))
                            binding.annualHouseholdIncome1.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.fifty_thousand_one_lakh)))
                            binding.annualHouseholdIncome2.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.one_lakh_two_lakh_fifty_thousand)))
                            binding.annualHouseholdIncome4.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.more_than_two_lakh_fifty_thousand)))
                            binding.annualHouseholdIncome4.setChecked(true);
                    }
                }
                if (name.equalsIgnoreCase("monthlyFoodExpenditure")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null) {
                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.zero_fifteen_hundred)))
                            binding.monthlyFoodExpense0.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.fifteen_twenty_five_hundred)))
                            binding.monthlyFoodExpense1.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.twenty_five_hundred_five_thousand)))
                            binding.monthlyFoodExpense2.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.five_ten_thousand)))
                            binding.monthlyFoodExpense3.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.more_than_ten_thousand)))
                            binding.monthlyFoodExpense4.setChecked(true);
                    }
                }
                if (name.equalsIgnoreCase("annualHealthExpenditure")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null) {
                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.zero_five_thousand)))
                            binding.healthExpense0.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.five_thousand_one_ten_thousand)))
                            binding.healthExpense1.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.ten_thousand_one_twenty_thousand)))
                            binding.healthExpense2.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.twenty_thousand_one_thirty_thousand)))
                            binding.healthExpense3.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.more_than_thirty_thousand)))
                            binding.greaterThanThirtyThousandRadioButton.setChecked(true);
                    }
                }
                if (name.equalsIgnoreCase("annualEducationExpenditure")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null) {
                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.zero)))
                            binding.annualEducationExpense0.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.zero_ten_thousand)))
                            binding.annualEducationExpense1.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.ten_twenty_thousand)))
                            binding.annualEducationExpense2.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.twenty_forty_thousand)))
                            binding.annualEducationExpense3.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.forty_thousand_one_lakh)))
                            binding.annualEducationExpense4.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.more_than_one_lakh)))
                            binding.annualEducationExpense5.setChecked(true);
                    }
                }
                if (name.equalsIgnoreCase("annualClothingExpenditure")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null) {
                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.zero_ten_thousand)))
                            binding.annualClothingExpense0.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.ten_twenty_thousand)))
                            binding.annualClothingExpense1.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.twenty_forty_thousand)))
                            binding.annualClothingExpense2.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.forty_thousand_one_lakh)))
                            binding.annualClothingExpense3.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.more_than_one_lakh)))
                            binding.annualClothingExpense4.setChecked(true);
                    }
                }
                if (name.equalsIgnoreCase("monthlyIntoxicantsExpenditure")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null) {
                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.one_to_six_hundred)))
                            binding.intoxicExpense0.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.six_hundred_one_thousand)))
                            binding.intoxicExpense1.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.thousand_to_fifteen_hundred)))
                            binding.intoxicExpense2.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.fifteen_hundred_to_twenty_five_hundred)))
                            binding.intoxicExpense3.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.more_than_twenty_five_hundred)))
                            binding.intoxicExpense4.setChecked(true);
                    }
                }
                if (name.equalsIgnoreCase("householdBPLCardStatus")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null) {
                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.yes_card_seen)))
                            binding.bplYesCardSeen.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.yes_card_not_seen)))
                            binding.bplYesCardNotSeen.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.no_card)))
                            binding.bplNoCard.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.DO_NOT_KNOW)))
                            binding.bplDoNotKnow.setChecked(true);
                    }
                }
                if (name.equalsIgnoreCase("householdAntodayaCardStatus")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null) {
                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.yes_card_seen)))
                            binding.antodayaYesCardSeen.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.yes_card_not_seen)))
                            binding.antodayaYesCardNotSeen.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.no_card)))
                            binding.antodayaNoCard.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.DO_NOT_KNOW)))
                            binding.antodayaDoNotKnow.setChecked(true);
                    }
                }
                if (name.equalsIgnoreCase("householdRSBYCardStatus")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null) {
                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.yes_card_seen)))
                            binding.rsbyYesCardSeen.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.yes_card_not_seen)))
                            binding.rsbyYesCardNotSeen.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.no_card)))
                            binding.rsbyNoCard.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.DO_NOT_KNOW)))
                            binding.rsbyDoNotKnow.setChecked(true);
                    }
                }
                if (name.equalsIgnoreCase("householdMGNREGACardStatus")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null) {
                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.yes_card_seen)))
                            binding.mgnregaYesCardSeen.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.yes_card_not_seen)))
                            binding.mgnregaYesCardNotSeen.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.no_card)))
                            binding.mgnregaNoCard.setChecked(true);

                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.DO_NOT_KNOW)))
                            binding.mgnregaDoNotKnow.setChecked(true);
                    }
                }
            } while (idCursor1.moveToNext());
        }
        idCursor1.close();

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
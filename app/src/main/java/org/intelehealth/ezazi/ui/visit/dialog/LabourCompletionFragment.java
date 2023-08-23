package org.intelehealth.ezazi.ui.visit.dialog;

import static org.intelehealth.ezazi.ui.visit.activity.VisitLabourActivity.ARG_HAS_MOTHER_DECEASED;
import static org.intelehealth.ezazi.ui.visit.activity.VisitLabourActivity.ARG_VISIT_ID;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.database.dao.ObsDAO;
import org.intelehealth.ezazi.databinding.LabourCompleteAndMotherDeceasedDialogBinding;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.ui.validation.FirstLetterUpperCaseInputFilter;
import org.intelehealth.ezazi.ui.visit.activity.VisitLabourActivity;
import org.intelehealth.ezazi.ui.visit.model.LabourInfo;
import org.intelehealth.ezazi.utilities.Utils;
import org.intelehealth.ezazi.utilities.UuidDictionary;
import org.intelehealth.ezazi.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vaghela Mithun R. on 19-08-2023 - 11:25.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class LabourCompletionFragment extends Fragment {
    //    private static final String ARG_VISIT_ID = "visitId";
//    private static final String ARG_HAS_MOTHER_DECEASED = "has_mother_deceased";
    private LabourCompleteAndMotherDeceasedDialogBinding binding;
    private LabourInfo labourInfo;
    private String visitId;
    private boolean hasMotherDeceased;
    private VisitCompletionHelper helper;

//    public interface OnLabourCompleteListener {
//        void onLabourCompleted();
//    }

    public static LabourCompletionFragment getInstance(String visitId, boolean hasMotherDeceased) {
        LabourCompletionFragment fragment = new LabourCompletionFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_VISIT_ID, visitId);
        bundle.putBoolean(ARG_HAS_MOTHER_DECEASED, hasMotherDeceased);
        fragment.setArguments(bundle);
        return fragment;
    }

    public LabourCompletionFragment() {
        super(R.layout.labour_complete_and_mother_deceased_dialog);
    }

//    public void setListener(OnLabourCompleteListener listener) {
//        this.listener = listener;
//    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
        binding = LabourCompleteAndMotherDeceasedDialogBinding.bind(view);
//        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) binding.getRoot().getParent());
//        behavior.setPeekHeight(ScreenUtils.getInstance(requireContext()).getHeight());
        fetchArgument();
        initView();
        validatedInput();
    }

//    @NonNull
//    @Override
//    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        Dialog dialog = super.onCreateDialog(savedInstanceState);
////        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        dialog.setCancelable(false);
//        dialog.setCanceledOnTouchOutside(false);
//        return dialog;
//    }

    private void fetchArgument() {
        if (requireActivity().getIntent().hasExtra(ARG_VISIT_ID)) {
            visitId = requireActivity().getIntent().getStringExtra(ARG_VISIT_ID);
            hasMotherDeceased = requireActivity().getIntent().getBooleanExtra(ARG_HAS_MOTHER_DECEASED, false);
        }
    }

    private void initView() {
        helper = new VisitCompletionHelper(requireContext(), visitId);
        enableAndDisableAllFields(binding, false);
        setDropdownsData(binding);
        applyFirstLatterCapitalCase();
        binding.etLayoutDeceasedReason.setVisibility(hasMotherDeceased ? View.VISIBLE : View.GONE);
        binding.etLayoutDeceasedReason.setMultilineInputEndIconGravity();
        binding.etLayoutOtherComment.setMultilineInputEndIconGravity();
//        binding.bottomSheetAppBar.toolbar.setTitle(getString(R.string.complete_visit));
//        binding.bottomSheetAppBar.toolbar.setNavigationOnClickListener(v -> dismiss());
        binding.btnSubmit.setOnClickListener(v -> {
            saveVisitCompletionDetails(binding);
        });

        binding.etDeceasedReason.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) labourInfo.setMotherDeceasedReason(s.toString());
                else labourInfo.setMotherDeceasedReason(null);
                validatedInput();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void applyFirstLatterCapitalCase() {
        binding.etDeceasedReason.setFilters(new InputFilter[]{new FirstLetterUpperCaseInputFilter()});
        binding.etBabyStatus.setFilters(new InputFilter[]{new FirstLetterUpperCaseInputFilter()});
        binding.etMotherStatus.setFilters(new InputFilter[]{new FirstLetterUpperCaseInputFilter()});
        binding.etOtherComment.setFilters(new InputFilter[]{new FirstLetterUpperCaseInputFilter()});
    }

    private void enableAndDisableAllFields(LabourCompleteAndMotherDeceasedDialogBinding binding, boolean flag) {
        binding.etLayoutBirthWeight.setEnabled(flag);
        binding.etLayoutApgar1.setEnabled(flag);
        binding.etLayoutApgar5.setEnabled(flag);
        binding.etLayoutBabyGender.setEnabled(flag);
        binding.etLayoutBabyStatus.setEnabled(flag);
        binding.etLayoutMotherStatus.setEnabled(flag);
        binding.etLayoutOtherComment.setEnabled(flag);
    }

//    private void disableDropDownEditMode(){
//        binding.autotvLabourCompleted.setEnabled(false);
//        binding.etLayoutApgar1.setEnabled(flag);
//        binding.etLayoutApgar5.setEnabled(flag);
//        binding.etLayoutBabyGender.setEnabled(flag);
//    }

    private void setDropdownsData(LabourCompleteAndMotherDeceasedDialogBinding binding) {
        //for labour completed dropdown
        labourInfo = new LabourInfo();
        final String[] birthOutcomeList = getResources().getStringArray(R.array.labours);
        ArrayAdapter<String> labourCompletedAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_textview, birthOutcomeList);
        binding.autotvLabourCompleted.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);
        binding.autotvLabourCompleted.setAdapter(labourCompletedAdapter);
        binding.autotvLabourCompleted.setOnItemClickListener((parent, view, position, id) -> {
            Utils.hideKeyboard((AppCompatActivity) requireContext());
            labourInfo.setBirthOutcome(parent.getItemAtPosition(position).toString());
            validatedInput();
            String otherString = getString(R.string.other).toLowerCase();
            if (!labourInfo.getBirthOutcome().isEmpty() && labourInfo.getBirthOutcome().equalsIgnoreCase(otherString)) {
                binding.etOtherComment.setEnabled(true);
                binding.etLayoutBirthWeight.setEnabled(false);
                binding.etLayoutApgar1.setEnabled(false);
                binding.etLayoutApgar5.setEnabled(false);
                binding.etLayoutBabyGender.setEnabled(false);
                binding.etLayoutBabyStatus.setEnabled(false);
                binding.etLayoutMotherStatus.setEnabled(false);
            } else {
                binding.etOtherComment.setEnabled(true);
                binding.etLayoutBirthWeight.setEnabled(true);
                binding.etLayoutApgar1.setEnabled(true);
                binding.etLayoutApgar5.setEnabled(true);
                binding.etLayoutBabyGender.setEnabled(true);
                binding.etLayoutBabyStatus.setEnabled(true);
                binding.etLayoutMotherStatus.setEnabled(true);
            }
        });

        List<Integer> itemsList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            itemsList.add(i);
        }
        List<String> birthWeightList = new ArrayList<>();
        for (double i = 0.5; i <= 5; i += 0.5) {
            birthWeightList.add(i + " kg");
        }
        //for gender dropdown
        final String[] items = getResources().getStringArray(R.array.gender);
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_textview, items);
        binding.autotvBabyGender.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);
        binding.autotvBabyGender.setAdapter(genderAdapter);
        binding.autotvBabyGender.setOnItemClickListener((parent, view, position, id) -> {
            Utils.hideKeyboard((AppCompatActivity) requireContext());
            labourInfo.setGender(parent.getItemAtPosition(position).toString());
        });

        //for birth weight dropdown - in kg
        ArrayAdapter<String> weightAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_textview, birthWeightList);
        binding.autotvBirthWeight.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);
        binding.autotvBirthWeight.setAdapter(weightAdapter);
        binding.autotvBirthWeight.setOnItemClickListener((parent, view, position, id) -> {
            Utils.hideKeyboard((AppCompatActivity) requireContext());
            labourInfo.setBirthWeight(parent.getItemAtPosition(position).toString());
        });

        //for Apgar at 1min
        ArrayAdapter<Integer> apgar1 = new ArrayAdapter<>(requireContext(), R.layout.spinner_textview, itemsList);
        binding.autotvApgar1min.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);
        binding.autotvApgar1min.setAdapter(apgar1);
        binding.autotvApgar1min.setOnItemClickListener((parent, view, position, id) -> {
            Utils.hideKeyboard((AppCompatActivity) requireContext());
            labourInfo.setApgar1Min(parent.getItemAtPosition(position).toString());
        });

        //for Apgar at 5min
        ArrayAdapter<Integer> apgar2 = new ArrayAdapter<>(requireContext(), R.layout.spinner_textview, itemsList);
        binding.autotvApgar5min.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);
        binding.autotvApgar5min.setAdapter(apgar2);
        binding.autotvApgar5min.setOnItemClickListener((parent, view, position, id) -> {
            Utils.hideKeyboard((AppCompatActivity) requireContext());
            labourInfo.setApgar5Min(parent.getItemAtPosition(position).toString());
        });

    }

    private void saveVisitCompletionDetails(LabourCompleteAndMotherDeceasedDialogBinding binding) {
        labourInfo.setBabyStatus(binding.etBabyStatus.getText().toString());
        labourInfo.setMotherStatus(binding.etMotherStatus.getText().toString());
        labourInfo.setOtherComment(binding.etOtherComment.getText().toString());
        labourInfo.setMotherDeceasedReason(binding.etDeceasedReason.getText().toString());

        if (labourInfo.isInvalidData()) {
            Toast.makeText(requireContext(), getString(R.string.add_details_for_labour_completed), Toast.LENGTH_SHORT).show();
        } else {
            collectDataForLabourAndMotherBoth();
        }
    }

    @SuppressLint("SetTextI18n")
    private void collectDataForLabourAndMotherBoth() {
        try {
            ObsDAO obsDAO = new ObsDAO();
            boolean birthOutcomeStatus;
            boolean motherDeceasedStatus;

            String encounterId = helper.insertVisitCompleteEncounter();
            if (encounterId != null && encounterId.length() > 0) {
                if (labourInfo.getBirthOutcome().equalsIgnoreCase(getString(R.string.other))) {
                    birthOutcomeStatus = obsDAO.insert_Obs(encounterId, helper.sessionManager.getCreatorID(),
                            labourInfo.getBirthOutcome().toUpperCase(), UuidDictionary.BIRTH_OUTCOME);
                    obsDAO.insert_Obs(encounterId, helper.sessionManager.getCreatorID(),
                            labourInfo.getOtherComment(), UuidDictionary.LABOUR_OTHER);
                } else {
                    birthOutcomeStatus = insertStage2AdditionalData(encounterId);
                }

                motherDeceasedStatus = helper.addMotherDeceasedObs(encounterId, hasMotherDeceased, labourInfo.getMotherDeceasedReason());

//                motherDeceasedStatus = obsDAO.insertMotherDeceasedFlatObs(encounterId, helper.sessionManager.getCreatorID(), String.valueOf(hasMotherDeceased));
//
//                if (hasMotherDeceased) {
//                    obsDAO.insert_Obs(encounterId, helper.sessionManager.getCreatorID(),
//                            labourInfo.getMotherDeceasedReason(), UuidDictionary.MOTHER_DECEASED);
//                }

                if (birthOutcomeStatus && motherDeceasedStatus) {
                    binding.btnSubmit.setText("Visit Completing...Please wait");
                    binding.btnSubmit.setEnabled(false);
                    ((VisitLabourActivity) requireActivity()).checkInternetAndUploadVisitEncounter();
//                    dismiss();
                    Toast.makeText(requireContext(), getString(R.string.data_added_successfully), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    private boolean insertStage2AdditionalData(String encounterId) throws DAOException {
        boolean isInserted = false;
        ////
        // Now get this encounteruuid and create refer obs table.
        if (!encounterId.isEmpty()) {
            List<ObsDTO> obsDTOList = new ArrayList<>();

            // *. Birth Outcome
            obsDTOList.add(helper.createObs(encounterId, UuidDictionary.BIRTH_OUTCOME, labourInfo.getBirthOutcome()));

            // 1. Birth Weight
            if (labourInfo.getBirthWeight() != null && labourInfo.getBirthWeight().length() > 0) {
                obsDTOList.add(helper.createObs(encounterId, UuidDictionary.BIRTH_WEIGHT, labourInfo.getBirthWeight()));
            }

            // 2. Apgar 1 min
            if (labourInfo.getApgar1Min() != null && labourInfo.getApgar1Min().length() > 0) {
                obsDTOList.add(helper.createObs(encounterId, UuidDictionary.APGAR_1_MIN, labourInfo.getApgar1Min()));
            }

            // 3. Apgar 5min
            if (labourInfo.getApgar5Min() != null && !labourInfo.getApgar5Min().isEmpty()) {
                obsDTOList.add(helper.createObs(encounterId, UuidDictionary.APGAR_5_MIN, labourInfo.getApgar5Min()));
            }

            // 4. Sex
            if (labourInfo.getGender() != null && !labourInfo.getGender().isEmpty()) {
                obsDTOList.add(helper.createObs(encounterId, UuidDictionary.SEX, labourInfo.getGender()));
            }

            // 5. Baby Status
            if (labourInfo.getBabyStatus() != null && !labourInfo.getBabyStatus().isEmpty()) {
                obsDTOList.add(helper.createObs(encounterId, UuidDictionary.BABY_STATUS, labourInfo.getBabyStatus()));
            }

            // 6. Mother Status
            if (labourInfo.getMotherStatus() != null && !labourInfo.getMotherStatus().isEmpty()) {
                obsDTOList.add(helper.createObs(encounterId, UuidDictionary.MOTHER_STATUS, labourInfo.getMotherStatus()));
            }

            if (labourInfo.getOtherComment() != null && !labourInfo.getOtherComment().isEmpty()) {
                obsDTOList.add(helper.createObs(encounterId, UuidDictionary.LABOUR_OTHER, labourInfo.getOtherComment()));
            }

            isInserted = new ObsDAO().insertObsToDb(obsDTOList);
        }

        return isInserted;
    }

    private void validatedInput() {
        binding.btnSubmit.setEnabled(validBirthOutcome() && validMotherDeceased());
    }

    private boolean validBirthOutcome() {
        return labourInfo.getBirthOutcome() != null && labourInfo.getBirthOutcome().length() > 0;
    }

    private boolean validMotherDeceased() {
        if (hasMotherDeceased) {
            return labourInfo.getMotherDeceasedReason() != null && labourInfo.getMotherDeceasedReason().length() > 0;
        }
        return true;
    }
}

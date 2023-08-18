package org.intelehealth.ezazi.ui.visit.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.homeActivity.HomeActivity;
import org.intelehealth.ezazi.database.dao.ObsDAO;
import org.intelehealth.ezazi.databinding.LabourCompleteAndMotherDeceasedDialogBinding;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.ui.visit.model.LabourInfo;
import org.intelehealth.ezazi.utilities.Utils;
import org.intelehealth.ezazi.utilities.UuidDictionary;
import org.intelehealth.ezazi.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vaghela Mithun R. on 17-08-2023 - 21:03.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class LabourDialog extends VisitCompletionHelper {
    private final boolean hasMotherDeceased;
    private final OnLabourCompleteListener listener;
    private BottomSheetDialog bottomSheetDialogVisitComplete;
    private LabourInfo labourInfo;

    public interface OnLabourCompleteListener {
        void onLabourCompleted();
    }

    public LabourDialog(Context context, boolean hasMotherDeceased, String visitId, OnLabourCompleteListener listener) {
        super(context, visitId);
        this.hasMotherDeceased = hasMotherDeceased;
        this.listener = listener;
    }

    public void buildDialog() {
        bottomSheetDialogVisitComplete = new BottomSheetDialog(context);
        LabourCompleteAndMotherDeceasedDialogBinding binding = LabourCompleteAndMotherDeceasedDialogBinding.inflate(inflater, null, true);
        bottomSheetDialogVisitComplete.setContentView(binding.getRoot());
        bottomSheetDialogVisitComplete.setCancelable(false);
        bottomSheetDialogVisitComplete.getWindow().setWindowAnimations(R.style.DialogAnimationSlideIn);
        enableAndDisableAllFields(binding, false);
        setDropdownsData(binding);
        binding.etLayoutDeceasedReason.setVisibility(hasMotherDeceased ? View.VISIBLE : View.GONE);

        binding.toolbar.setTitle(context.getString(R.string.complete_visit));
        binding.toolbar.setNavigationOnClickListener(v -> bottomSheetDialogVisitComplete.dismiss());
        bottomSheetDialogVisitComplete.setOnDismissListener(DialogInterface::dismiss);
        binding.btnSubmit.setOnClickListener(v -> {
            saveVisitCompletionDetails(binding);
        });

        bottomSheetDialogVisitComplete.show();
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

    private void setDropdownsData(LabourCompleteAndMotherDeceasedDialogBinding binding) {
        //for labour completed dropdown
        labourInfo = new LabourInfo();
        final String[] birthOutcomeList = context.getResources().getStringArray(R.array.labours);
        ArrayAdapter<String> labourCompletedAdapter = new ArrayAdapter<>(context, R.layout.spinner_textview, birthOutcomeList);
        binding.autotvLabourCompleted.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);
        binding.autotvLabourCompleted.setAdapter(labourCompletedAdapter);
        binding.autotvLabourCompleted.setOnItemClickListener((parent, view, position, id) -> {
            Utils.hideKeyboard((AppCompatActivity) context);
            labourInfo.setBirthOutcome(parent.getItemAtPosition(position).toString());
            String otherString = context.getString(R.string.other).toLowerCase();
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
        List<Double> birthWeightList = new ArrayList<>();
        for (double i = 0.5; i <= 5; i += 0.5) {
            birthWeightList.add(i);
        }
        //for gender dropdown
        final String[] items = context.getResources().getStringArray(R.array.gender);
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(context, R.layout.spinner_textview, items);
        binding.autotvBabyGender.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);
        binding.autotvBabyGender.setAdapter(genderAdapter);
        binding.autotvBabyGender.setOnItemClickListener((parent, view, position, id) -> {
            Utils.hideKeyboard((AppCompatActivity) context);
            labourInfo.setGender(parent.getItemAtPosition(position).toString());
        });

        //for birth weight dropdown - in kg
        ArrayAdapter<Double> weightAdapter = new ArrayAdapter<>(context, R.layout.spinner_textview, birthWeightList);
        binding.autotvBirthWeight.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);
        binding.autotvBirthWeight.setAdapter(weightAdapter);
        binding.autotvBirthWeight.setOnItemClickListener((parent, view, position, id) -> {
            Utils.hideKeyboard((AppCompatActivity) context);
            labourInfo.setBirthWeight(parent.getItemAtPosition(position).toString());
        });

        //for Apgar at 1min
        ArrayAdapter<Integer> apgar1 = new ArrayAdapter<>(context, R.layout.spinner_textview, itemsList);
        binding.autotvApgar1min.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);
        binding.autotvApgar1min.setAdapter(apgar1);
        binding.autotvApgar1min.setOnItemClickListener((parent, view, position, id) -> {
            Utils.hideKeyboard((AppCompatActivity) context);
            labourInfo.setApgar1Min(parent.getItemAtPosition(position).toString());
        });

        //for Apgar at 5min
        ArrayAdapter<Integer> apgar2 = new ArrayAdapter<>(context, R.layout.spinner_textview, itemsList);
        binding.autotvApgar5min.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);
        binding.autotvApgar5min.setAdapter(apgar2);
        binding.autotvApgar5min.setOnItemClickListener((parent, view, position, id) -> {
            Utils.hideKeyboard((AppCompatActivity) context);
            labourInfo.setApgar5Min(parent.getItemAtPosition(position).toString());
        });

    }

    private void saveVisitCompletionDetails(LabourCompleteAndMotherDeceasedDialogBinding binding) {
        labourInfo.setBabyStatus(binding.etBabyStatus.getText().toString());
        labourInfo.setMotherStatus(binding.etMotherStatus.getText().toString());
        labourInfo.setOtherComment(binding.etOtherComment.getText().toString());
        labourInfo.setMotherDeceasedReason(binding.etDeceasedReason.getText().toString());

        if (labourInfo.isInvalidData()) {
            Toast.makeText(context, context.getString(R.string.add_details_for_labour_completed), Toast.LENGTH_SHORT).show();
        } else {
            collectDataForLabourAndMotherBoth();
        }
    }

    private void collectDataForLabourAndMotherBoth() {
        try {
            ObsDAO obsDAO = new ObsDAO();
            boolean birthOutcomeStatus;
            boolean motherDeceasedStatus;

            String encounterId = insertVisitCompleteEncounter();
            if (encounterId != null && encounterId.length() > 0) {
                if (labourInfo.getBirthOutcome().equalsIgnoreCase(context.getString(R.string.other))) {
                    birthOutcomeStatus = obsDAO.insert_Obs(encounterId, sessionManager.getCreatorID(),
                            labourInfo.getBirthOutcome(), UuidDictionary.BIRTH_OUTCOME);
                    obsDAO.insert_Obs(encounterId, sessionManager.getCreatorID(),
                            labourInfo.getOtherComment(), UuidDictionary.LABOUR_OTHER);
                } else {
                    birthOutcomeStatus = insertStage2AdditionalData(encounterId);
                }

                motherDeceasedStatus = obsDAO.insertMotherDeceasedFlatObs(encounterId, String.valueOf(hasMotherDeceased), UuidDictionary.MOTHER_DECEASED_FLAG);

                if (hasMotherDeceased) {
                    obsDAO.insert_Obs(encounterId, sessionManager.getCreatorID(),
                            labourInfo.getMotherDeceasedReason(), UuidDictionary.MOTHER_DECEASED);
                }

                if (birthOutcomeStatus && motherDeceasedStatus) {
                    listener.onLabourCompleted();
                    bottomSheetDialogVisitComplete.dismiss();
                    Toast.makeText(context, context.getString(R.string.data_added_successfully), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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
            obsDTOList.add(createObs(encounterId, UuidDictionary.BIRTH_OUTCOME, labourInfo.getBirthOutcome()));

            // 1. Birth Weight
            if (labourInfo.getBirthWeight() != null && labourInfo.getBirthWeight().length() > 0) {
                obsDTOList.add(createObs(encounterId, UuidDictionary.BIRTH_WEIGHT, labourInfo.getBirthWeight()));
            }

            // 2. Apgar 1 min
            if (labourInfo.getApgar1Min() != null && labourInfo.getApgar1Min().length() > 0) {
                obsDTOList.add(createObs(encounterId, UuidDictionary.APGAR_1_MIN, labourInfo.getApgar1Min()));
            }

            // 3. Apgar 5min
            if (labourInfo.getApgar5Min() != null && !labourInfo.getApgar5Min().isEmpty()) {
                obsDTOList.add(createObs(encounterId, UuidDictionary.APGAR_5_MIN, labourInfo.getApgar5Min()));
            }

            // 4. Sex
            if (labourInfo.getGender() != null && !labourInfo.getGender().isEmpty()) {
                obsDTOList.add(createObs(encounterId, UuidDictionary.SEX, labourInfo.getGender()));
            }

            // 5. Baby Status
            if (labourInfo.getBabyStatus() != null && !labourInfo.getBabyStatus().isEmpty()) {
                obsDTOList.add(createObs(encounterId, UuidDictionary.BABY_STATUS, labourInfo.getBabyStatus()));
            }

            // 6. Mother Status
            if (labourInfo.getMotherStatus() != null && !labourInfo.getMotherStatus().isEmpty()) {
                obsDTOList.add(createObs(encounterId, UuidDictionary.MOTHER_STATUS, labourInfo.getMotherStatus()));
            }

            isInserted = new ObsDAO().insertObsToDb(obsDTOList);
        }

        return isInserted;
    }
}

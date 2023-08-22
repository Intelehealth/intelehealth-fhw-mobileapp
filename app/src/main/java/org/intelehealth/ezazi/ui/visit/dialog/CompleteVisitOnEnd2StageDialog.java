package org.intelehealth.ezazi.ui.visit.dialog;

import android.content.Context;
import android.content.Intent;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.homeActivity.HomeActivity;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.database.dao.EncounterDAO;
import org.intelehealth.ezazi.database.dao.ObsDAO;
import org.intelehealth.ezazi.database.dao.VisitsDAO;
import org.intelehealth.ezazi.databinding.BirthOutcomeDialogBinding;
import org.intelehealth.ezazi.databinding.DialogReferHospitalEzaziBinding;
import org.intelehealth.ezazi.databinding.MotherDeceasedDialogBinding;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.ui.dialog.ConfirmationDialogFragment;
import org.intelehealth.ezazi.ui.dialog.CustomViewDialogFragment;
import org.intelehealth.ezazi.ui.validation.FirstLetterUpperCaseInputFilter;
import org.intelehealth.ezazi.ui.visit.model.CompletedVisitStatus;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.Utils;
import org.intelehealth.ezazi.utilities.UuidDictionary;
import org.intelehealth.ezazi.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by Vaghela Mithun R. on 17-08-2023 - 12:36.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class CompleteVisitOnEnd2StageDialog extends VisitCompletionHelper implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    private BirthOutcomeDialogBinding binding;
    private TextView selectedView;
    private final OnVisitCompleteListener listener;
    private static final String TAG = "CompleteVisitDialog";

    public interface OnVisitCompleteListener {
        void onVisitCompleted(boolean hasLabour, boolean hasMotherDeceased);
    }

    public CompleteVisitOnEnd2StageDialog(Context context, String visitUuid, OnVisitCompleteListener listener) {
        super(context, visitUuid);
        this.listener = listener;
    }

    public void buildDialog() {
        Log.e(TAG, "buildDialog: visitId =>" + visitId);
        binding = BirthOutcomeDialogBinding.inflate(inflater, null, true);
        binding.etOutOfTimeReasonLayout.setMultilineInputEndIconGravity();

        binding.tvReferToOtherHospital.setOnClickListener(this);
        binding.tvReferToOtherHospital.setTag(UuidDictionary.REFER_TYPE);
        binding.tvSelfDischarge.setOnClickListener(this);
        binding.tvSelfDischarge.setTag(UuidDictionary.REFER_TYPE);
        binding.tvShiftToSection.setOnClickListener(this);
        binding.tvShiftToSection.setTag(UuidDictionary.REFER_TYPE);
        binding.tvReferToICU.setOnClickListener(this);
        binding.tvReferToICU.setTag(UuidDictionary.REFER_TYPE);

        binding.cbLabourCompleted.setOnCheckedChangeListener(this);
        binding.cbLabourCompleted.setTag(UuidDictionary.BIRTH_OUTCOME);
        binding.cbMotherDeceased.setOnCheckedChangeListener(this);
        binding.cbMotherDeceased.setTag(UuidDictionary.MOTHER_DECEASED_FLAG);

        binding.etOtherCommentOutcomes.setTag(UuidDictionary.REFER_TYPE);
        binding.etOtherCommentOutcomes.setFilters(new InputFilter[]{new FirstLetterUpperCaseInputFilter()});
        binding.etOtherCommentOutcomes.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                clearSelection();
                Utils.showKeyboard(context, v);
                binding.etOtherCommentOutcomes.setCursorVisible(true);
                selectedView = null;
            }
        });

        showCustomViewDialog(R.string.additional_information, R.string.next,
                R.string.cancel, binding.getRoot(), this::manageBirthOutcomeSelection);
    }

    private void showCustomViewDialog(@StringRes int title,
                                      @StringRes int positiveLbl,
                                      @StringRes int negLbl,
                                      View view,
                                      CustomViewDialogFragment.OnConfirmationActionListener listener) {
        CustomViewDialogFragment dialog = new CustomViewDialogFragment.Builder(context)
                .title(title)
                .positiveButtonLabel(positiveLbl)
                .negativeButtonLabel(negLbl)
                .view(view)
                .build();

        dialog.setListener(listener);

        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), dialog.getClass().getCanonicalName());
    }

    @Override
    public void onClick(View v) {
        v.setSelected(true);
        clearSelection();
        clearUncheckableItemSelection();
        selectedView = (TextView) v;
    }

    private void clearSelection() {
        if (selectedView != null) selectedView.setSelected(false);
        binding.cbLabourCompleted.setChecked(false);
        binding.cbMotherDeceased.setChecked(false);
    }

    private void clearUncheckableItemSelection() {
        if (selectedView != null) selectedView.setSelected(false);
        binding.etOtherCommentOutcomes.clearFocus();
        binding.etOtherCommentOutcomes.setCursorVisible(false);
        binding.etOtherCommentOutcomes.setText("");
        Utils.hideKeyboard((AppCompatActivity) context);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) clearUncheckableItemSelection();
        selectedView = null;
    }

    private void manageBirthOutcomeSelection() {
        if (binding.cbMotherDeceased.isChecked() && binding.cbLabourCompleted.isChecked()) {
            listener.onVisitCompleted(true, binding.cbMotherDeceased.isChecked());
        } else if (binding.cbLabourCompleted.isChecked()) {
            listener.onVisitCompleted(true, binding.cbMotherDeceased.isChecked());
        } else if (binding.cbMotherDeceased.isChecked()) {
            showMotherDeceasedDialog();
        } else if (Objects.requireNonNull(binding.etOtherCommentOutcomes.getText()).length() > 0) {
            String value = binding.etOtherCommentOutcomes.getText().toString();
            String conceptId = (String) binding.etOtherCommentOutcomes.getTag();
            String content = context.getString(R.string.are_you_sure_want_to_complete_visit, value);
            showConfirmationDialog(content, () -> completeVisitWithOtherReferType(value, conceptId));
        } else if (selectedView != null) {
            completeVisitWithReferType();
        } else {
            Toast.makeText(context, context.getString(R.string.please_select_an_option), Toast.LENGTH_SHORT).show();
        }
//        isLabourCompletedChecked = cbLabourCompleted.isChecked();
//        isMotherDeceasedChecked = cbMotherDeceased.isChecked();
//
//        if (!isLabourCompletedChecked && !isMotherDeceasedChecked && selectedTextview == null && etOtherCommentOutcome.getText().toString().isEmpty()) {
//            Toast.makeText(context, context.getString(R.string.please_select_an_option), Toast.LENGTH_SHORT).show();
//        } else {
//            if (isLabourCompletedChecked && isMotherDeceasedChecked) {
//                // show ui for both labour completed and mother deceased
//                selectedBirthOutcome = LABOUR_AND_MOTHER;
//                showBottomSheetDialog(selectedBirthOutcome);
//
//            } else if (isLabourCompletedChecked) {
//
//                // show ui for labour completed only
//                selectedBirthOutcome = LABOUR_COMPLETED;
//                showBottomSheetDialog(selectedBirthOutcome);
//            } else if (isMotherDeceasedChecked) {
//                // show ui for mother deceased only
//                selectedBirthOutcome = MOTHER_DECEASED;
//                //  showBottomSheetDialog(selectedBirthOutcome);
//                showMotherDeceasedDialog();
//
//            } else if (selectedTextview.getId() == R.id.tvReferToOtherHospital) {
//                // refer other hospital // call visit complete enc.
//                referOtherHospitalDialog(value);
//
//            } else if (selectedTextview.getId() == R.id.tvSelfDischarge) {
//                // self discharge // call visit complete enc.
//                try {
//                    boolean isInserted = insertVisitCompleteObs(visitUuid, context.getString(R.string.self_discharge_medical_advice), UuidDictionary.REFER_TYPE);
//                    showToastAndUploadVisit(isInserted, getResources().getString(R.string.data_added_successfully));
//
//                } catch (DAOException e) {
//                    e.printStackTrace();
//                }
//            } else if (selectedTextview.getId() == R.id.tvShiftToSection) {
//                // Shift to C-Section // call visit complete enc.
//                try {
//                    boolean isInserted = insertVisitCompleteObs(visitUuid, context.getString(R.string.shift_to_c_section), UuidDictionary.REFER_TYPE);
//                    showToastAndUploadVisit(isInserted, getResources().getString(R.string.data_added_successfully));
//
//                } catch (DAOException e) {
//                    e.printStackTrace();
//                }
//            } else if (selectedTextview.getId() == R.id.tvReferToICU) {
//                //Refer to high dependency unit / ICU// call visit complete enc.
//                try {
//                    boolean isInserted = insertVisitCompleteObs(visitUuid, context.getString(R.string.refer_to_icu), UuidDictionary.REFER_TYPE);
//                    showToastAndUploadVisit(isInserted, getResources().getString(R.string.data_added_successfully));
//
//                } catch (DAOException e) {
//                    e.printStackTrace();
//                }
//            } else if (!etOtherCommentOutcome.getText().toString().isEmpty()) {
//                //for other comments - REFER_TYPE
//                try {
//                    boolean isInserted = insertVisitCompleteObs(visitUuid, etOtherCommentOutcome.getText().toString(), UuidDictionary.REFER_TYPE);
//                    showToastAndUploadVisit(isInserted, getResources().getString(R.string.data_added_successfully));
//
//                } catch (DAOException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }
    }

    private void completeVisitWithReferType() {
        if (selectedView == null) return;
        String value = selectedView.getText().toString();
        String conceptId = (String) selectedView.getTag();
        Log.e(TAG, "completeVisitWithReferType: value =>" + value);
        Log.e(TAG, "completeVisitWithReferType: conceptId =>" + conceptId);
        if (value.equals(context.getString(R.string.refer_to_other_hospital))) {
            showConfirmationDialog(R.string.are_you_sure_want_to_refer_other, this::referOtherHospitalDialog);
        } else if (value.equals(context.getString(R.string.self_discharge_medical_advice))) {
            showConfirmationDialog(R.string.are_you_sure_want_to_self_discharge, () -> completeVisitWithOtherReason(value, conceptId));
        } else if (value.equals(context.getString(R.string.shift_to_c_section))) {
            showConfirmationDialog(R.string.are_you_sure_want_to_shift_to_c_section, () -> completeVisitWithOtherReason(value, conceptId));
        } else if (value.equals(context.getString(R.string.refer_to_icu))) {
            showConfirmationDialog(R.string.are_you_sure_want_to_refer_to_icu, () -> completeVisitWithOtherReason(value, conceptId));
        }
    }

    private void completeVisitWithOtherReason(String value, String conceptId) {
        // Now get this encounteruuid and create BIRTH_OUTCOME in obs table.
        try {
            ObsDAO obsDAO = new ObsDAO();
            String encounterUuid = insertVisitCompleteEncounter();
            Log.e(TAG, "completeVisitWithOtherReason: encounterId =>" + encounterUuid);
            if (encounterUuid != null && encounterUuid.length() > 0) {
                boolean isInserted = obsDAO.insert_Obs(encounterUuid, sessionManager.getCreatorID(), value, conceptId);
                Log.e(TAG, "completeVisitWithOtherReason: isInserted => " + isInserted);
                if (isInserted) {
                    listener.onVisitCompleted(false, false);
                }
            }
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
    }

    private void completeVisitWithOtherReferType(String value, String conceptId) {
        // Now get this encounteruuid and create BIRTH_OUTCOME in obs table.
        try {
            ObsDAO obsDAO = new ObsDAO();
            String encounterUuid = insertVisitCompleteEncounter();
            Log.e(TAG, "completeVisitWithOtherReason: encounterId =>" + encounterUuid);
            if (encounterUuid != null && encounterUuid.length() > 0) {
                obsDAO.insert_Obs(encounterUuid, sessionManager.getCreatorID(), CompletedVisitStatus.ReferType.OTHER.value(), conceptId);
                boolean isInserted = obsDAO.insert_Obs(encounterUuid, sessionManager.getCreatorID(), value, UuidDictionary.END_2ND_STAGE_OTHER);
                Log.e(TAG, "completeVisitWithOtherReason: isInserted => " + isInserted);
                if (isInserted) {
                    listener.onVisitCompleted(false, false);
                }
            }
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showConfirmationDialog(@StringRes int content, ConfirmationDialogFragment.OnConfirmationActionListener listener) {
        showConfirmationDialog(context.getString(content), listener);
    }

    private void showConfirmationDialog(String content, ConfirmationDialogFragment.OnConfirmationActionListener listener) {
        ConfirmationDialogFragment dialog = new ConfirmationDialogFragment.Builder(context)
                .content(content)
                .positiveButtonLabel(R.string.yes)
                .build();

        dialog.setListener(listener);

        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), dialog.getClass().getCanonicalName());
    }

    private void referOtherHospitalDialog() {
        DialogReferHospitalEzaziBinding binding = DialogReferHospitalEzaziBinding.inflate(inflater, null, false);

        showCustomViewDialog(R.string.refer_section, R.string.yes, R.string.no, binding.getRoot(), () -> {
            boolean isInserted = false;
            String hospitalName = binding.referHospitalName.getText().toString();
            String doctorName = binding.referDoctorName.getText().toString();
            String note = binding.referNote.getText().toString();

            // call visitcompleteenc and add obs for refer type and referal values entered...
            try {
                isInserted = referToOtherHospital(hospitalName, doctorName, note);
                if (isInserted) listener.onVisitCompleted(false, false);
            } catch (DAOException e) {
                e.printStackTrace();
            }
        });
    }

    private boolean referToOtherHospital(String hospitalName, String doctorName, String note) throws DAOException {

        boolean isInserted = true;
        String encounterUuid = insertVisitCompleteEncounter();
        String conceptId = (String) selectedView.getTag();
        String value = selectedView.getText().toString();

        // Now get this encounteruuid and create refer obs table.
        if (!encounterUuid.isEmpty()) {
            ObsDAO obsDAO = new ObsDAO();
            ObsDTO obsDTO;
            List<ObsDTO> obsDTOList = new ArrayList<>();

            // 1. Refer Type
            obsDTOList.add(createObs(encounterUuid, conceptId, value));

            // 2. Refer Hospital Name
            if (hospitalName != null && hospitalName.length() > 0) {
                obsDTOList.add(createObs(encounterUuid, UuidDictionary.REFER_HOSPITAL, hospitalName));
            }

            // 3. Refer Doctor Name
            if (doctorName != null && doctorName.length() > 0) {
                obsDTOList.add(createObs(encounterUuid, UuidDictionary.REFER_DR_NAME, doctorName));
            }

            // 4. Refer Note
            if (note != null && note.length() > 0) {
                obsDTOList.add(createObs(encounterUuid, UuidDictionary.REFER_NOTE, note));
            }

            isInserted = obsDAO.insertObsToDb(obsDTOList);
        }

        return isInserted;
    }

    private void showMotherDeceasedDialog() {
        MotherDeceasedDialogBinding binding = MotherDeceasedDialogBinding.inflate(inflater, null, false);
        binding.etLayoutMotherDeceased.setMultilineInputEndIconGravity();

        CustomViewDialogFragment dialog = new CustomViewDialogFragment.Builder(context)
                .title(R.string.mother_deceased)
                .positiveButtonLabel(R.string.yes)
                .negativeButtonLabel(R.string.no)
                .view(binding.getRoot())
                .build();

        dialog.requireValidationBeforeDismiss(true);
        dialog.setListener(() -> {
            if (Objects.requireNonNull(binding.etMotherDeceasedReason.getText()).length() > 0) {
                String value = binding.etMotherDeceasedReason.getText().toString();
                boolean isInserted = addMotherDeceasedObs(true, value);
                if (isInserted) listener.onVisitCompleted(false, true);
                dialog.dismiss();
            } else {
                Toast.makeText(context, context.getString(R.string.please_enter_reason), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), dialog.getClass().getCanonicalName());

    }
}

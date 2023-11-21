package org.intelehealth.ezazi.ui.visit.dialog;

import static org.intelehealth.ezazi.app.AppConstants.INPUT_MAX_LENGTH;

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
public class CompleteVisitOnEnd2StageDialog extends ReferTypeHelper implements View.OnClickListener,
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
        binding.etOtherCommentOutcomes.setFilters(new InputFilter[]{new FirstLetterUpperCaseInputFilter(), new InputFilter.LengthFilter(INPUT_MAX_LENGTH)});
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
            showMotherDeceasedDialog(() -> listener.onVisitCompleted(false, true));
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
    }

    private void completeVisitWithReferType() {
        if (selectedView == null) return;
        String value = selectedView.getText().toString();
        String conceptId = (String) selectedView.getTag();
        completeVisitWithReferType(value, conceptId, () -> listener.onVisitCompleted(false, false));
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
}

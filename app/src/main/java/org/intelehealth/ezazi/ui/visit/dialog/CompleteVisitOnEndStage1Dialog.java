package org.intelehealth.ezazi.ui.visit.dialog;

import static org.intelehealth.ezazi.app.AppConstants.INPUT_MAX_LENGTH;

import android.content.Context;
import android.text.InputFilter;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.databinding.EndStage1OptionsDialogBinding;
import org.intelehealth.ezazi.databinding.MotherDeceasedDialogBinding;
import org.intelehealth.ezazi.ui.dialog.CustomViewDialogFragment;
import org.intelehealth.ezazi.ui.validation.FirstLetterUpperCaseInputFilter;
import org.intelehealth.ezazi.utilities.Utils;
import org.intelehealth.ezazi.utilities.UuidDictionary;

import java.util.Objects;

/**
 * Created by Kaveri Zaware on 13-09-2023
 * email - kaveri@intelehealth.org
 **/
public class CompleteVisitOnEndStage1Dialog extends ReferTypeHelper implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    private EndStage1OptionsDialogBinding binding;
    private TextView selectedView;
    private final OnVisitCompleteListener listener;
    private static final String TAG = "CompleteVisitDialog";

    public interface OnVisitCompleteListener {
        void onVisitCompleted(boolean isEndStage1);
    }

    public CompleteVisitOnEndStage1Dialog(Context context, String visitUuid, OnVisitCompleteListener listener) {
        super(context, visitUuid);
        this.listener = listener;
    }

    public void buildDialog() {
        binding = EndStage1OptionsDialogBinding.inflate(inflater, null, true);

        binding.tvReferToOtherHospital.setOnClickListener(this);
        binding.tvReferToOtherHospital.setTag(UuidDictionary.REFER_TYPE);
        binding.tvSelfDischarge.setOnClickListener(this);
        binding.tvSelfDischarge.setTag(UuidDictionary.REFER_TYPE);
        binding.tvShiftToSection.setOnClickListener(this);
        binding.tvShiftToSection.setTag(UuidDictionary.REFER_TYPE);
        binding.tvReferToICU.setOnClickListener(this);
        binding.tvReferToICU.setTag(UuidDictionary.REFER_TYPE);
        binding.tvMotherDeceased.setOnClickListener(this);
        binding.tvMoveToStage2.setOnClickListener(this);

        showCustomViewDialog(R.string.select_an_option, R.string.yes,
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
    }

    private void clearUncheckableItemSelection() {
        if (selectedView != null) selectedView.setSelected(false);
        Utils.hideKeyboard((AppCompatActivity) context);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) clearUncheckableItemSelection();
        selectedView = null;
    }

    private void manageBirthOutcomeSelection() {
        if (selectedView != null) {
            if (selectedView.getText().toString().equalsIgnoreCase(context.getString(R.string.mother_deceased))) {
                showMotherDeceasedDialog();
            } else  if (selectedView.getText().toString().equalsIgnoreCase(context.getString(R.string.move_to_stage2))) {
                listener.onVisitCompleted(true);
            } else {
                completeVisitWithReferType();
            }
        } else {
            Toast.makeText(context, context.getString(R.string.please_select_an_option), Toast.LENGTH_SHORT).show();
        }
    }

    private void completeVisitWithReferType() {
        if (selectedView == null) return;
        String value = selectedView.getText().toString();
        String conceptId = (String) selectedView.getTag();
        completeVisitWithReferType(value, conceptId, () -> listener.onVisitCompleted(false));
    }

    private void showMotherDeceasedDialog() {
        MotherDeceasedDialogBinding binding = MotherDeceasedDialogBinding.inflate(inflater, null, false);
        binding.etLayoutMotherDeceased.setMultilineInputEndIconGravity();
        binding.etMotherDeceasedReason.setFilters(new InputFilter[]{new FirstLetterUpperCaseInputFilter(), new InputFilter.LengthFilter(INPUT_MAX_LENGTH)});
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
                String encounterId = insertVisitCompleteEncounter();
                if (encounterId != null && encounterId.length() > 0) {
                    boolean isInserted = addMotherDeceasedObs(encounterId, true, value);
                    if (isInserted) listener.onVisitCompleted(false);
                    dialog.dismiss();
                }
            } else {
                Toast.makeText(context, context.getString(R.string.please_enter_reason), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), dialog.getClass().getCanonicalName());

    }
}

package org.intelehealth.ezazi.ui.visit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.visitSummaryActivity.TimelineVisitSummaryActivity;
import org.intelehealth.ezazi.databinding.BirthOutcomeDialogBinding;
import org.intelehealth.ezazi.ui.dialog.CustomViewDialogFragment;
import org.intelehealth.ezazi.utilities.Utils;

/**
 * Created by Vaghela Mithun R. on 17-08-2023 - 12:36.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class CompleteVisitOnEnd2StageDialog {
    private String visitId;
    private Context context;

    private BirthOutcomeDialogBinding binding;

    private LayoutInflater inflater;

    public CompleteVisitOnEnd2StageDialog(Context context, String visitUuid) {
        this.visitId = visitUuid;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void buildDialog() {
        binding = BirthOutcomeDialogBinding.inflate(inflater, null, true);
        tvReferToOtherHospital.setOnClickListener(this);
        tvSelfDischarge.setOnClickListener(this);
        tvShiftToCSection.setOnClickListener(this);
        tvReferToICU.setOnClickListener(this);

        cbLabourCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (selectedTextview != null) {
                    selectedTextview.clearFocus();
                    selectedTextview.setSelected(false);
                    etOtherCommentOutcome.clearFocus();
                    etOtherCommentOutcome.setCursorVisible(false);
                    Utils.hideKeyboard((AppCompatActivity) context);
                }
            }
        });
        cbMotherDeceased.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (selectedTextview != null) {
                    selectedTextview.clearFocus();
                    selectedTextview.setSelected(false);
                    etOtherCommentOutcome.clearFocus();
                    etOtherCommentOutcome.setCursorVisible(false);
                    Utils.hideKeyboard((AppCompatActivity) context);

                }
            }
        });

        etOtherCommentOutcome.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                birthOutcomeSelected = false;
                cbLabourCompleted.setChecked(false);
                cbMotherDeceased.setChecked(false);
                tvReferToOtherHospital.setSelected(false);
                tvSelfDischarge.setSelected(false);
                tvShiftToCSection.setSelected(false);
                tvReferToICU.setSelected(false);
                etOtherCommentOutcome.setCursorVisible(true);

                Utils.showKeyboard(context, binding.etOtherCommentOutcomes);
            }
        });

        showCustomViewDialog(R.string.additional_information, R.string.cancel,
                R.string.next, binding.getRoot(), this::manageBirthOutcomeSelection);
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

    private void showKeyboard(View editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }
}

package org.intelehealth.ekalarogya.activities.identificationActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.databinding.DialogMedicalHistoryBinding;

public class MedicalHistoryDialog extends DialogFragment {

    public static final String TAG = "MedicalHistoryDialog";
    private DialogMedicalHistoryBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        binding = DialogMedicalHistoryBinding.inflate(inflater);

        builder.setView(binding.getRoot())
                .setPositiveButton(R.string.ok, (dialog, which) -> {

                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.primary_text));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.primary_text));
        });

        setListeners();
        return dialog;
    }

    private void setListeners() {
        binding.anySurgeriesRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.surgeryYes.getId())
                binding.surgeryLinearLayout.setVisibility(View.VISIBLE);
            else
                binding.surgeryLinearLayout.setVisibility(View.GONE);
        });
    }
}

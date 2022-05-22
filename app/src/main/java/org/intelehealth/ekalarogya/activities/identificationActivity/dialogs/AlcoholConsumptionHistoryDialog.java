package org.intelehealth.ekalarogya.activities.identificationActivity.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.activities.identificationActivity.callback.AlcoholConsumptionCallback;
import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.AlcoholConsumptionHistory;
import org.intelehealth.ekalarogya.databinding.DialogAlcoholConsumptionHistoryBinding;

import java.util.concurrent.atomic.AtomicBoolean;

public class AlcoholConsumptionHistoryDialog extends DialogFragment {

    public static final String TAG = "SmokingHistoryDialog";
    private DialogAlcoholConsumptionHistoryBinding binding;
    private AlcoholConsumptionCallback callback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (AlcoholConsumptionCallback) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        binding = DialogAlcoholConsumptionHistoryBinding.inflate(inflater);

        builder.setView(binding.getRoot())
                .setPositiveButton(R.string.ok, (dialog, which) -> {

                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialog1 -> {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.primary_text));
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.primary_text));

            positiveButton.setOnClickListener(v -> {
                if (!isDataValid())
                    Toast.makeText(requireContext(), "All fields are mandatory here", Toast.LENGTH_SHORT).show();
                else {
                    AlcoholConsumptionHistory consumptionHistory = fetchData();
                    callback.saveAlcoholConsumptionData(consumptionHistory);
                    dialog1.dismiss();
                }
            });
        });

        setListeners();
        return dialog;
    }

    private void setListeners() {
        binding.alcoholConsumptionRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.smokerRadioButton.getId())
                binding.alcoholConsumptionLinearLayout.setVisibility(View.VISIBLE);
            else
                binding.alcoholConsumptionLinearLayout.setVisibility(View.GONE);
        });
    }

    private Boolean isDataValid() {
        AtomicBoolean validation = new AtomicBoolean(true);

        if (binding.alcoholConsumptionRadioGroup.getCheckedRadioButtonId() == -1) {
            validation.set(false);
            return validation.get();
        }

        if (binding.alcoholConsumptionLinearLayout.getVisibility() == View.VISIBLE) {
            // If the person answers the above question as Yes, only then this linearlayout will be visible
            // Only in this case, will we be validating these RadioGroups

            if (binding.alcoholConsumptionRateRadioGroup.getCheckedRadioButtonId() == -1) {
                validation.set(false);
                return validation.get();
            }

            if (binding.durationOfAlcoholConsumptionRadioGroup.getCheckedRadioButtonId() == -1) {
                validation.set(false);
                return validation.get();
            }
        }

        return validation.get();
    }

    private AlcoholConsumptionHistory fetchData() {
        AlcoholConsumptionHistory alcoholConsumptionHistory = new AlcoholConsumptionHistory();
        // History of alcohol consumption
        alcoholConsumptionHistory.setHistoryOfAlcoholConsumption(
                ((RadioButton) binding.alcoholConsumptionRadioGroup.findViewById(binding.alcoholConsumptionRadioGroup.getCheckedRadioButtonId())).getText().toString()
        );

        if (binding.alcoholConsumptionLinearLayout.getVisibility() == View.VISIBLE) {
            // Rate of alcohol consumption
            alcoholConsumptionHistory.setRateOfAlcoholConsumption(
                    ((RadioButton) binding.alcoholConsumptionRateRadioGroup.findViewById(binding.alcoholConsumptionRateRadioGroup.getCheckedRadioButtonId())).getText().toString()
            );
            // Duration of alcohol consumption
            alcoholConsumptionHistory.setDurationOfAlcoholConsumption(
                    ((RadioButton) binding.durationOfAlcoholConsumptionRadioGroup.findViewById(binding.durationOfAlcoholConsumptionRadioGroup.getCheckedRadioButtonId())).getText().toString()
            );
        }

        return alcoholConsumptionHistory;
    }
}
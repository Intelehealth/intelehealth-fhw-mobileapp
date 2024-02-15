package org.intelehealth.ekalarogya.activities.identificationActivity.dialogs;

import static org.intelehealth.ekalarogya.utilities.StringUtils.getAlcoholHistory;
import static org.intelehealth.ekalarogya.utilities.StringUtils.setSelectedCheckboxes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
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
import org.intelehealth.ekalarogya.activities.identificationActivity.IdentificationActivity;
import org.intelehealth.ekalarogya.activities.identificationActivity.callback.AlcoholConsumptionCallback;
import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.AlcoholConsumptionHistory;
import org.intelehealth.ekalarogya.databinding.DialogAlcoholConsumptionHistoryBinding;
import org.intelehealth.ekalarogya.utilities.SessionManager;

import java.util.concurrent.atomic.AtomicBoolean;

public class AlcoholConsumptionHistoryDialog extends DialogFragment {

    public static final String TAG = "SmokingHistoryDialog";
    private DialogAlcoholConsumptionHistoryBinding binding;
    private AlcoholConsumptionCallback callback;
    private Bundle bundle;
    private int position;
    private Resources updatedResources;
    private SessionManager sessionManager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (AlcoholConsumptionCallback) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = getArguments();
        updatedResources = ((IdentificationActivity) requireActivity()).getUpdatedResources();
        sessionManager = new SessionManager(requireContext());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        binding = DialogAlcoholConsumptionHistoryBinding.inflate(inflater);
        setListeners();

        if (bundle != null)
            setBundleData();

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
                    Toast.makeText(requireContext(), getString(R.string.all_fields_are_mandatory), Toast.LENGTH_SHORT).show();
                else {
                    AlcoholConsumptionHistory consumptionHistory = fetchData();

                    if (bundle != null)
                        callback.saveAlcoholConsumptionDataAtPosition(consumptionHistory, position);
                    else
                        callback.saveAlcoholConsumptionData(consumptionHistory);

                    dialog1.dismiss();

                }
            });
        });

        return dialog;
    }

    private void setListeners() {
        binding.alcoholConsumptionRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.yesRadioButton.getId())
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

            if (binding.frequencyOfConsumptionRadioGroup.getCheckedRadioButtonId() == -1) {
                validation.set(false);
                return validation.get();
            }
        }

        return validation.get();
    }

    private AlcoholConsumptionHistory fetchData() {
        AlcoholConsumptionHistory alcoholConsumptionHistory = new AlcoholConsumptionHistory();
        // History of alcohol consumption
        alcoholConsumptionHistory.setHistoryOfAlcoholConsumption(getAlcoholHistory(
                ((RadioButton) binding.alcoholConsumptionRadioGroup.findViewById(binding.alcoholConsumptionRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                requireContext().getResources(),
                updatedResources,
                sessionManager.getAppLanguage()
        ));

        if (binding.alcoholConsumptionLinearLayout.getVisibility() == View.VISIBLE) {
            // Rate of alcohol consumption
            alcoholConsumptionHistory.setRateOfAlcoholConsumption(getAlcoholHistory(
                    ((RadioButton) binding.alcoholConsumptionRateRadioGroup.findViewById(binding.alcoholConsumptionRateRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                    requireContext().getResources(),
                    updatedResources,
                    sessionManager.getAppLanguage()
            ));
            // Duration of alcohol consumption
            alcoholConsumptionHistory.setDurationOfAlcoholConsumption(getAlcoholHistory(
                    ((RadioButton) binding.durationOfAlcoholConsumptionRadioGroup.findViewById(binding.durationOfAlcoholConsumptionRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                    requireContext().getResources(),
                    updatedResources,
                    sessionManager.getAppLanguage()
            ));

            alcoholConsumptionHistory.setFrequencyOfAlcoholConsumption(getAlcoholHistory(
                    ((RadioButton) binding.frequencyOfConsumptionRadioGroup.findViewById(binding.frequencyOfConsumptionRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                    requireContext().getResources(),
                    updatedResources,
                    sessionManager.getAppLanguage()
            ));
        }

        return alcoholConsumptionHistory;
    }

    private void setBundleData() {
        position = bundle.getInt("position");

        String historyOfAlcoholConsumption = bundle.getString("historyOfAlcoholConsumption");
        setSelectedCheckboxes(binding.alcoholConsumptionRadioGroup, historyOfAlcoholConsumption, updatedResources, requireContext().getResources(), sessionManager.getAppLanguage());

        if (historyOfAlcoholConsumption.equalsIgnoreCase("Yes")) {
            String rateOfAlcoholConsumption = bundle.getString("rateOfAlcoholConsumption");
            setSelectedCheckboxes(binding.alcoholConsumptionRateRadioGroup, rateOfAlcoholConsumption, updatedResources, requireContext().getResources(), sessionManager.getAppLanguage());

            String durationOfAlcoholConsumption = bundle.getString("durationOfAlcoholConsumption");
            setSelectedCheckboxes(binding.durationOfAlcoholConsumptionRadioGroup, durationOfAlcoholConsumption, updatedResources, requireContext().getResources(), sessionManager.getAppLanguage());

            String frequencyOfAlcoholConsumption = bundle.getString("frequencyOfAlcoholConsumption");
            setSelectedCheckboxes(binding.frequencyOfConsumptionRadioGroup, frequencyOfAlcoholConsumption, updatedResources, requireContext().getResources(), sessionManager.getAppLanguage());
        }
    }
}
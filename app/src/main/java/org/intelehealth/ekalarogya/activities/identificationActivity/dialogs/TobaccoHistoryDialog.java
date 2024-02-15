package org.intelehealth.ekalarogya.activities.identificationActivity.dialogs;

import static org.intelehealth.ekalarogya.utilities.StringUtils.getTobaccoHistoryStrings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.activities.identificationActivity.IdentificationActivity;
import org.intelehealth.ekalarogya.activities.identificationActivity.callback.TobaccoHistoryCallback;
import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.TobaccoHistory;
import org.intelehealth.ekalarogya.databinding.DialogTobaccoHistoryBinding;
import org.intelehealth.ekalarogya.utilities.SessionManager;

import java.util.concurrent.atomic.AtomicBoolean;

public class TobaccoHistoryDialog extends DialogFragment {

    public static final String TAG = "TobaccoHistoryDialog";
    private DialogTobaccoHistoryBinding binding;
    private TobaccoHistoryCallback callback;
    private Bundle bundle;
    private int position;
    private Resources updatedResources;
    private SessionManager sessionManager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (TobaccoHistoryCallback) context;
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
        binding = DialogTobaccoHistoryBinding.inflate(inflater);
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
                    TobaccoHistory tobaccoHistory = fetchData();

                    if (bundle != null)
                        callback.saveTobaccoHistoryAtPosition(tobaccoHistory, position);
                    else
                        callback.saveTobaccoHistory(tobaccoHistory);

                    dialog1.dismiss();
                }
            });
        });

        return dialog;
    }

    private void setListeners() {
/*
        binding.tobaccoRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.smokerRadioButton.getId())
                binding.smokerLinearLayout.setVisibility(View.VISIBLE);
            else
                binding.smokerLinearLayout.setVisibility(View.GONE);
        });
*/
    }

    private Boolean isDataValid() {
        AtomicBoolean validation = new AtomicBoolean(true);

        if (binding.tobaccoRadioGroup.getCheckedRadioButtonId() == -1) {
            validation.set(false);
            return validation.get();
        }

/*
        if (binding.smokerLinearLayout.getVisibility() == View.VISIBLE) {
            // If the person answers the above question as Yes, only then this linearlayout will be visible
            // Only in this case, will we be validating these RadioGroups

            if (binding.rateOfConsumptionRadioGroup.getCheckedRadioButtonId() == -1) {
                validation.set(false);
                return validation.get();
            }

            if (binding.durationOfSmokingRadioGroup.getCheckedRadioButtonId() == -1) {
                validation.set(false);
                return validation.get();
            }

            if (binding.frequencyOfConsumptionRadioGroup.getCheckedRadioButtonId() == -1) {
                validation.set(false);
                return validation.get();
            }
        }
*/

        return validation.get();
    }

    private TobaccoHistory fetchData() {
        TobaccoHistory tobaccoHistory = new TobaccoHistory();
        // History of smoking
        tobaccoHistory.setChewTobaccoStatus(getTobaccoHistoryStrings(
                ((RadioButton) binding.tobaccoRadioGroup.findViewById(binding.tobaccoRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                requireContext().getResources(),
                updatedResources,
                sessionManager.getAppLanguage()
        ));

/*
        if (binding.smokerLinearLayout.getVisibility() == View.VISIBLE) {
            // Rate of smoking
            tobaccoHistory.setRateOfSmoking(getSmokingHistoryStrings(
                    ((RadioButton) binding.rateOfConsumptionRadioGroup.findViewById(binding.rateOfConsumptionRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                    requireContext(),
                    updatedContext,
                    sessionManager.getAppLanguage()
            ));

            // Duration of smoking
            tobaccoHistory.setDurationOfSmoking(getSmokingHistoryStrings(
                    ((RadioButton) binding.durationOfSmokingRadioGroup.findViewById(binding.durationOfSmokingRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                    requireContext(),
                    updatedContext,
                    sessionManager.getAppLanguage()
            ));

            // Frequency of Smoking
            tobaccoHistory.setFrequencyOfSmoking(getSmokingHistoryStrings(
                    ((RadioButton) binding.frequencyOfConsumptionRadioGroup.findViewById(binding.frequencyOfConsumptionRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                    requireContext(),
                    updatedContext,
                    sessionManager.getAppLanguage()
            ));
        }
*/

        return tobaccoHistory;
    }

    private void setBundleData() {
        position = bundle.getInt("position");

        String tobaccoStatus = bundle.getString("TobaccoStatus");
        if (tobaccoStatus.equalsIgnoreCase(updatedResources.getString(R.string.survey_yes)))
            binding.yesTobaccoRadioButton.setChecked(true);
        else
            binding.noTobaccoRadioButton.setChecked(true);

/*
        if (tobaccoStatus.equalsIgnoreCase(updatedContext.getString(R.string.smoker))) {

            String rateOfSmoking = bundle.getString("rateOfSmoking");
            setSelectedCheckboxes(binding.rateOfConsumptionRadioGroup, rateOfSmoking, updatedContext, requireContext(), sessionManager.getAppLanguage());

            String durationOfSmoking = bundle.getString("durationOfSmoking");
            setSelectedCheckboxes(binding.durationOfSmokingRadioGroup, durationOfSmoking, updatedContext, requireContext(), sessionManager.getAppLanguage());

            String frequencyOfSmoking = bundle.getString("frequencyOfSmoking");
            setSelectedCheckboxes(binding.frequencyOfConsumptionRadioGroup, frequencyOfSmoking, updatedContext, requireContext(), sessionManager.getAppLanguage());
        }
*/
    }
}

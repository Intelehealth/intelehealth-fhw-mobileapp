package org.intelehealth.ekalarogya.activities.identificationActivity.dialogs;

import static org.intelehealth.ekalarogya.utilities.StringUtils.getMedicalHistoryStrings;
import static org.intelehealth.ekalarogya.utilities.StringUtils.setSelectedCheckboxes;

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
import org.intelehealth.ekalarogya.activities.identificationActivity.IdentificationActivity;
import org.intelehealth.ekalarogya.activities.identificationActivity.callback.MedicalHistoryCallback;
import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.MedicalHistory;
import org.intelehealth.ekalarogya.databinding.DialogMedicalHistoryBinding;
import org.intelehealth.ekalarogya.utilities.SessionManager;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class MedicalHistoryDialog extends DialogFragment {

    public static final String TAG = "MedicalHistoryDialog";
    private DialogMedicalHistoryBinding binding;
    private MedicalHistoryCallback callback;
    private Context updatedContext;
    private SessionManager sessionManager;
    private Bundle bundle;
    private int position;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (MedicalHistoryCallback) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = getArguments();
        updatedContext = ((IdentificationActivity) requireActivity()).getUpdatedContext();
        sessionManager = new SessionManager(requireContext());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        binding = DialogMedicalHistoryBinding.inflate(inflater);
        setListeners();

        if (bundle != null)
            setBundleData();

        builder.setView(binding.getRoot())
                .setPositiveButton(R.string.ok, (dialog, which) -> {

                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.primary_text));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.primary_text));

            positiveButton.setOnClickListener(v -> {
                if (!isDataValid())
                    Toast.makeText(requireContext(), getString(R.string.all_fields_are_mandatory), Toast.LENGTH_SHORT).show();
                else {
                    MedicalHistory medicalHistory = getData();

                    if (bundle != null)
                        callback.saveMedicalHistoryDataAtPosition(medicalHistory, position);
                    else
                        callback.saveMedicalHistoryData(medicalHistory);

                    dialog1.dismiss();
                }
            });
        });

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

    private Boolean isDataValid() {
        AtomicBoolean validation = new AtomicBoolean(true);

        if (binding.hypertensionRadioGroup.getCheckedRadioButtonId() == -1) {
            validation.set(false);
            return validation.get();
        }

        if (binding.diabetesRadioGroup.getCheckedRadioButtonId() == -1) {
            validation.set(false);
            return validation.get();
        }

        if (binding.arthritisRadioGroup.getCheckedRadioButtonId() == -1) {
            validation.set(false);
            return validation.get();
        }

        if (binding.anaemiaRadioGroup.getCheckedRadioButtonId() == -1) {
            validation.set(false);
            return validation.get();
        }

        if (binding.anySurgeriesRadioGroup.getCheckedRadioButtonId() == -1) {
            validation.set(false);
            return validation.get();
        }

        if (binding.surgeryLinearLayout.getVisibility() == View.VISIBLE) {
            if (binding.reasonForSurgeryEditText.getText().toString().equalsIgnoreCase("") || (binding.reasonForSurgeryEditText.getText().toString().isEmpty())) {
                validation.set(false);
                return validation.get();
            }
        }

        return true;
    }

    private MedicalHistory getData() {
        MedicalHistory medicalHistory = new MedicalHistory();

        medicalHistory.setHypertension(getMedicalHistoryStrings(
                ((RadioButton) binding.hypertensionRadioGroup.findViewById(binding.hypertensionRadioGroup.getCheckedRadioButtonId())).getText().toString().trim(),
                requireContext(),
                updatedContext,
                sessionManager.getAppLanguage()
        ));

        medicalHistory.setDiabetes(getMedicalHistoryStrings(
                ((RadioButton) binding.diabetesRadioGroup.findViewById(binding.diabetesRadioGroup.getCheckedRadioButtonId())).getText().toString().trim(),
                requireContext(),
                updatedContext,
                sessionManager.getAppLanguage()
        ));

        medicalHistory.setArthritis(getMedicalHistoryStrings(
                ((RadioButton) binding.arthritisRadioGroup.findViewById(binding.arthritisRadioGroup.getCheckedRadioButtonId())).getText().toString().trim(),
                requireContext(),
                updatedContext,
                sessionManager.getAppLanguage()
        ));

        medicalHistory.setAnaemia(getMedicalHistoryStrings(
                ((RadioButton) binding.anaemiaRadioGroup.findViewById(binding.anaemiaRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                requireContext(),
                updatedContext,
                sessionManager.getAppLanguage()
        ));

        medicalHistory.setAnySurgeries(getMedicalHistoryStrings(
                ((RadioButton) binding.anySurgeriesRadioGroup.findViewById(binding.anySurgeriesRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                requireContext(),
                updatedContext,
                sessionManager.getAppLanguage()
        ));

        if (medicalHistory.getAnySurgeries().equalsIgnoreCase("Yes"))
            medicalHistory.setReasonForSurgery(Objects.requireNonNull(binding.reasonForSurgeryEditText.getText()).toString().trim());

        return medicalHistory;
    }

    private void setBundleData() {
        position = bundle.getInt("position");

        String hypertensionText = bundle.getString("hypertension");
        setSelectedCheckboxes(binding.hypertensionRadioGroup, hypertensionText, updatedContext, requireContext(), sessionManager.getAppLanguage());

        String diabetesText = bundle.getString("diabetes");
        setSelectedCheckboxes(binding.diabetesRadioGroup, diabetesText, updatedContext, requireContext(), sessionManager.getAppLanguage());

        String arthritisText = bundle.getString("arthritis");
        setSelectedCheckboxes(binding.arthritisRadioGroup, arthritisText, updatedContext, requireContext(), sessionManager.getAppLanguage());

        String anaemiaText = bundle.getString("anaemia");
        setSelectedCheckboxes(binding.anaemiaRadioGroup, anaemiaText, updatedContext, requireContext(), sessionManager.getAppLanguage());

        String anySurgeries = bundle.getString("anySurgeries");
        setSelectedCheckboxes(binding.anySurgeriesRadioGroup, anySurgeries, updatedContext, requireContext(), sessionManager.getAppLanguage());

        String reasonForSurgery = bundle.getString("reasonForSurgery");
        if (anySurgeries.equalsIgnoreCase("Yes")) {
            binding.reasonForSurgeryEditText.setText(reasonForSurgery);
            binding.surgeryLinearLayout.setVisibility(View.VISIBLE);
        }
    }
}
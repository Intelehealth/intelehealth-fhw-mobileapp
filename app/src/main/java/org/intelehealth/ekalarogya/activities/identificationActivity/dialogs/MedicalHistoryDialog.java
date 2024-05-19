package org.intelehealth.ekalarogya.activities.identificationActivity.dialogs;

import static org.intelehealth.ekalarogya.utilities.StringUtils.getMedicalHistoryStrings;
import static org.intelehealth.ekalarogya.utilities.StringUtils.setSelectedCheckboxes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
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
    private Resources updatedResources;
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
        updatedResources = ((IdentificationActivity) requireActivity()).getUpdatedResources();
        sessionManager = new SessionManager(requireContext());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        binding = DialogMedicalHistoryBinding.inflate(inflater);
        setListeners();

        if (bundle != null) setBundleData();

        builder.setView(binding.getRoot()).setPositiveButton(R.string.ok, (dialog, which) -> {

        }).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

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
                    else callback.saveMedicalHistoryData(medicalHistory);

                    dialog1.dismiss();
                }
            });
        });

        return dialog;
    }

    private void setListeners() {
        binding.anySurgeriesRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.surgeryYes.getId()) {
                binding.surgeryLinearLayout.setVisibility(View.VISIBLE);
                binding.reasonForSurgeryEditText.setFilters(new InputFilter[]{inputFilter_Name, emojiFilter});
            } else {
                binding.surgeryLinearLayout.setVisibility(View.GONE);
            }
        });

        setHypertensionListeners();
        setDiabetesListeners();
        setAnemiaListeners();
    }

    private void setHypertensionListeners() {
        binding.bpYes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.llBpMedication.setVisibility(View.VISIBLE);
            } else {
                binding.llBpMedication.setVisibility(View.GONE);
            }
        });

        binding.bpMedicationYes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.llBpHealthcareWorker.setVisibility(View.VISIBLE);
            } else {
                binding.llBpHealthcareWorker.setVisibility(View.GONE);
            }
        });

        binding.bpMedicationNo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.llBpNoMedicationReason.setVisibility(View.VISIBLE);
            } else {
                binding.llBpNoMedicationReason.setVisibility(View.GONE);
            }
        });
    }

    private void setDiabetesListeners() {
        binding.diabetesYes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.llDiabetesMedication.setVisibility(View.VISIBLE);
            } else {
                binding.llDiabetesMedication.setVisibility(View.GONE);
            }
        });

        binding.diabetesMedicationYes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.llDiabetesHealthcareWorker.setVisibility(View.VISIBLE);
            } else {
                binding.llDiabetesHealthcareWorker.setVisibility(View.GONE);
            }
        });

        binding.diabetesMedicationNo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.llDiabetesNoMedicationReason.setVisibility(View.VISIBLE);
            } else {
                binding.llDiabetesNoMedicationReason.setVisibility(View.GONE);
            }
        });
    }

    private void setAnemiaListeners() {
        binding.anemiaYes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.llAnemiaMedication.setVisibility(View.VISIBLE);
            } else {
                binding.llAnemiaMedication.setVisibility(View.GONE);
            }
        });

        binding.anemiaMedicationYes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.llAnemiaHealthcareWorker.setVisibility(View.VISIBLE);
            } else {
                binding.llAnemiaHealthcareWorker.setVisibility(View.GONE);
            }
        });

        binding.anemiaMedicationNo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.llAnemiaNoMedicationReason.setVisibility(View.VISIBLE);
            } else {
                binding.llAnemiaNoMedicationReason.setVisibility(View.GONE);
            }
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

        medicalHistory.setHypertension(getMedicalHistoryStrings(((RadioButton) binding.hypertensionRadioGroup.findViewById(binding.hypertensionRadioGroup.getCheckedRadioButtonId())).getText().toString().trim(), requireContext().getResources(), updatedResources, sessionManager.getAppLanguage()));

        medicalHistory.setDiabetes(getMedicalHistoryStrings(((RadioButton) binding.diabetesRadioGroup.findViewById(binding.diabetesRadioGroup.getCheckedRadioButtonId())).getText().toString().trim(), requireContext().getResources(), updatedResources, sessionManager.getAppLanguage()));

        medicalHistory.setArthritis(getMedicalHistoryStrings(((RadioButton) binding.arthritisRadioGroup.findViewById(binding.arthritisRadioGroup.getCheckedRadioButtonId())).getText().toString().trim(), requireContext().getResources(), updatedResources, sessionManager.getAppLanguage()));

        medicalHistory.setAnaemia(getMedicalHistoryStrings(((RadioButton) binding.anaemiaRadioGroup.findViewById(binding.anaemiaRadioGroup.getCheckedRadioButtonId())).getText().toString(), requireContext().getResources(), updatedResources, sessionManager.getAppLanguage()));

        medicalHistory.setAnySurgeries(getMedicalHistoryStrings(((RadioButton) binding.anySurgeriesRadioGroup.findViewById(binding.anySurgeriesRadioGroup.getCheckedRadioButtonId())).getText().toString(), requireContext().getResources(), updatedResources, sessionManager.getAppLanguage()));

        if (medicalHistory.getAnySurgeries().equalsIgnoreCase("Yes"))
            medicalHistory.setReasonForSurgery(Objects.requireNonNull(binding.reasonForSurgeryEditText.getText()).toString().trim());

        return medicalHistory;
    }

    private void setBundleData() {
        position = bundle.getInt("position");

        String hypertensionText = bundle.getString("hypertension");
        setSelectedCheckboxes(binding.hypertensionRadioGroup, hypertensionText, updatedResources, requireContext().getResources(), sessionManager.getAppLanguage());

        String diabetesText = bundle.getString("diabetes");
        setSelectedCheckboxes(binding.diabetesRadioGroup, diabetesText, updatedResources, requireContext().getResources(), sessionManager.getAppLanguage());

        String arthritisText = bundle.getString("arthritis");
        setSelectedCheckboxes(binding.arthritisRadioGroup, arthritisText, updatedResources, requireContext().getResources(), sessionManager.getAppLanguage());

        String anaemiaText = bundle.getString("anaemia");
        setSelectedCheckboxes(binding.anaemiaRadioGroup, anaemiaText, updatedResources, requireContext().getResources(), sessionManager.getAppLanguage());

        String anySurgeries = bundle.getString("anySurgeries");
        setSelectedCheckboxes(binding.anySurgeriesRadioGroup, anySurgeries, updatedResources, requireContext().getResources(), sessionManager.getAppLanguage());

        String reasonForSurgery = bundle.getString("reasonForSurgery");
        if (anySurgeries != null && anySurgeries.equalsIgnoreCase("Yes")) {
            binding.reasonForSurgeryEditText.setText(reasonForSurgery);
            binding.surgeryLinearLayout.setVisibility(View.VISIBLE);
        }
    }


    public static InputFilter inputFilter_Name = new InputFilter() { //filter input for all other fields
        @Override
        public CharSequence filter(CharSequence charSequence, int start, int end, Spanned spanned, int i2, int i3) {
            boolean keepOriginal = true;
            StringBuilder sb = new StringBuilder(end - start);
            for (int i = start; i < end; i++) {
                char c = charSequence.charAt(i);
                if (isCharAllowed(c)) // put your condition here
                    sb.append(c);
                else if (c == '.') sb.append(c);
                else keepOriginal = false;
            }
            if (keepOriginal) return null;
            else {
                if (charSequence instanceof Spanned) {
                    SpannableString sp = new SpannableString(sb);
                    TextUtils.copySpansFrom((Spanned) charSequence, start, sb.length(), null, sp, 0);
                    return sp;
                } else {
                    return sb;
                }
            }
        }

        private boolean isCharAllowed(char c) {
            return Character.isLetter(c) || Character.isSpaceChar(c) || Character.getType(c) == Character.NON_SPACING_MARK || Character.getType(c) == Character.COMBINING_SPACING_MARK;     // This allows only alphabets, digits and spaces.
        }
    };

    private final InputFilter emojiFilter = (source, start, end, dest, dstart, dend) -> {
        for (int index = start; index < end - 1; index++) {
            int type = Character.getType(source.charAt(index));
            if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                return "";
            }
        }
        return null;
    };
}
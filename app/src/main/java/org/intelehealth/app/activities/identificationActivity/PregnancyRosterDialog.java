package org.intelehealth.app.activities.identificationActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.intelehealth.app.R;
import org.intelehealth.app.databinding.DialogPregnancyRosterBinding;

public class PregnancyRosterDialog extends DialogFragment {

    public static final String TAG = "PregnancyRosterDialog";
    private DialogPregnancyRosterBinding binding;
    private Bundle bundle;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = getArguments();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        binding = DialogPregnancyRosterBinding.inflate(inflater);

        builder
                .setView(binding.getRoot())
                .setPositiveButton(getString(R.string.ok), ((dialog, which) -> {
                }))
                .setNegativeButton(getString(R.string.cancel), ((dialog, which) -> dialog.dismiss()));

        setAdapters();
        setListeners();

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {

            });
        });

        return dialog;
    }

    private void setAdapters() {

    }

    private void setListeners() {
        // Pregnant Past Two Years Listener
        binding.spinnerPregnantpasttwoyrs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1)
                    binding.pregnancyQuestionsLinearLayout.setVisibility(View.VISIBLE);
                else
                    binding.pregnancyQuestionsLinearLayout.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.spinnerOutcomepregnancy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    if (position == 1) {
                        binding.llChildAlive.setVisibility(View.VISIBLE);
                    } else {
                        binding.llChildAlive.setVisibility(View.GONE);
                        binding.edittextBabyagedied.setVisibility(View.GONE);
                        binding.spinnerChildalive.setSelection(0);
                    }

                    if (position == 5) {
                        binding.edittextMonthspregnancylast.setVisibility(View.GONE);
                    } else {
                        binding.edittextMonthspregnancylast.setVisibility(View.VISIBLE);
                    }

                    if (position == 5) {
                        binding.edittextMonthsbeingpregnant.setVisibility(View.VISIBLE);
                    } else {
                        binding.edittextMonthsbeingpregnant.setVisibility(View.GONE);
                    }

                    if (position == 4 || position == 5) {
                        binding.llDeliveryPlace.setVisibility(View.GONE);
                    } else {
                        binding.llDeliveryPlace.setVisibility(View.VISIBLE);
                    }

                    if (position == 3 || position == 4 || position == 5) {
                        binding.llFocalPoint.setVisibility(View.GONE);
                        binding.llSingleMultipleBirth.setVisibility(View.GONE);
                        binding.llBabyGender.setVisibility(View.GONE);
                        binding.llChildComplications.setVisibility(View.GONE);
                        //  binding.edittextBabyagedied.setVisibility(View.GONE);
                    } else {
                        binding.llSingleMultipleBirth.setVisibility(View.VISIBLE);
                        binding.llBabyGender.setVisibility(View.VISIBLE);
                        binding.llChildComplications.setVisibility(View.VISIBLE);
                        binding.llFocalPoint.setVisibility(View.VISIBLE);

                        //todo for place of deleivery is home so fockl is not shown at that time
                        if (binding.spinnerPlaceofdeliverypregnant.getSelectedItemPosition() == 1) {
                            binding.spinnerPlaceofdeliverypregnant.setSelection(0);

                        }
                        // binding.edittextBabyagedied.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.spinnerChildalive.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position == 2)
                    binding.edittextBabyagedied.setVisibility(View.VISIBLE);
                else
                    binding.edittextBabyagedied.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.spinnerPlaceofdeliverypregnant.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                if (pos == 1) {
                    binding.llFocalPoint.setVisibility(View.GONE);
                } else {
                    binding.llFocalPoint.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }
}

package org.intelehealth.app.activities.identificationActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.intelehealth.app.R;
import org.intelehealth.app.databinding.DialogPregnancyRosterBinding;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;

public class PregnancyRosterDialog extends DialogFragment {

    public static final String TAG = "PregnancyRosterDialog";
    private DialogPregnancyRosterBinding binding;
    private PregnancyOutcomeCallback callback;
    private SessionManager sessionManager;
    private PregnancyRosterData data;
    private Bundle bundle;

    // Adapters
    private ArrayAdapter<CharSequence> adapter_pregnantPastTwoYears, adapter_outcomepregnancy, adapter_childalive, adapter_placeofdeliverypregnant, adapter_pregnancyplanned,
            adapter_focalPointBlock, adapter_sexofbaby, adapter_pregnancyhighriskcase, adapter_pregnancycomplications, adapter_singlemultiplebirths;

    public final String SELECT = "Select";
    public final String SELECT_BLOCK = "Select Block";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (PregnancyOutcomeCallback) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(requireActivity());
        bundle = getArguments();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        binding = DialogPregnancyRosterBinding.inflate(inflater);

        setListeners();
        setAdapters();

        if (bundle != null) {
            extractBundleData(bundle);
            setBundleData();
        }

        builder
                .setView(binding.getRoot())
                .setPositiveButton(getString(R.string.ok), ((dialog, which) -> {
                }))
                .setNegativeButton(getString(R.string.cancel), ((dialog, which) -> dialog.dismiss()));

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                PregnancyRosterData data = fetchData();
                boolean areDetailsCorrect = validateData(data);
                if (areDetailsCorrect) {
                    if (bundle != null) {
                        callback.savePregnancyDataAtPosition(data, bundle.getInt("position"));
                    } else {
                        callback.savePregnancyData(data);
                    }
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), getString(R.string.fill_required_fields), Toast.LENGTH_SHORT).show();
                }
            });
        });

        return dialog;
    }

    private void setAdapters() {
        // Pregnant Past Two Years Adapter
        try {
            String pastTwoYearsLanguage = "pasttwoyrs_" + sessionManager.getAppLanguage();
            int pastTwoYearsId = getResources().getIdentifier(pastTwoYearsLanguage, "array", requireActivity().getApplicationContext().getPackageName());
            if (pastTwoYearsId != 0) {
                adapter_pregnantPastTwoYears = ArrayAdapter.createFromResource(requireContext(), pastTwoYearsId, android.R.layout.simple_spinner_dropdown_item);
            }
            binding.spinnerPregnantpasttwoyrs.setAdapter(adapter_pregnantPastTwoYears);
        } catch (Exception e) {
            Logger.logE("Identification", "#648", e);
        }

        // Pregnancy Outcome Adapter
        try {
            String outcomepregnancyLanguage = "outcomepregnancy_" + sessionManager.getAppLanguage();
            int outcomepregnancy_id = getResources().getIdentifier(outcomepregnancyLanguage, "array", requireActivity().getApplicationContext().getPackageName());
            if (outcomepregnancy_id != 0) {
                adapter_outcomepregnancy = ArrayAdapter.createFromResource(requireContext(), outcomepregnancy_id, android.R.layout.simple_spinner_dropdown_item);
            }
            binding.spinnerOutcomepregnancy.setAdapter(adapter_outcomepregnancy);
        } catch (Exception e) {
            Logger.logE("Identification", "#648", e);
        }

        // Child Alive Adapter
        try {
            String childaliveLanguage = "childalive_" + sessionManager.getAppLanguage();
            int childalive_id = getResources().getIdentifier(childaliveLanguage, "array", requireActivity().getApplicationContext().getPackageName());
            if (childalive_id != 0) {
                adapter_childalive = ArrayAdapter.createFromResource(getContext(), childalive_id, android.R.layout.simple_spinner_dropdown_item);
            }
            binding.spinnerChildalive.setAdapter(adapter_childalive);
        } catch (Exception e) {
            Logger.logE("Identification", "#648", e);
        }

        // Place Of Delivery Adapter
        try {
            String placedeliveryLanguage = "placedelivery_" + sessionManager.getAppLanguage();
            int placedelivery_id = getResources().getIdentifier(placedeliveryLanguage, "array", requireActivity().getApplicationContext().getPackageName());
            if (placedelivery_id != 0) {
                adapter_placeofdeliverypregnant = ArrayAdapter.createFromResource(getContext(), placedelivery_id, android.R.layout.simple_spinner_dropdown_item);
            }
            binding.spinnerPlaceofdeliverypregnant.setAdapter(adapter_placeofdeliverypregnant);
        } catch (Exception e) {
            Logger.logE("Identification", "#648", e);
        }

        // Focal Facility Adapter
        try {
            String focalBlockLanguage = "block_" + sessionManager.getAppLanguage();
            int focalBlock_id = getResources().getIdentifier(focalBlockLanguage, "array", requireActivity().getApplicationContext().getPackageName());
            if (focalBlock_id != 0) {
                adapter_focalPointBlock = ArrayAdapter.createFromResource(getContext(), focalBlock_id, android.R.layout.simple_spinner_dropdown_item);
            }
            binding.spinnerFocalBlock.setAdapter(adapter_focalPointBlock);
        } catch (Exception e) {
            Logger.logE("Identification", "#648", e);
        }

        // Single / Multiple Births Adapter
        try {
            String singlemultipleLanguage = "singlemultiplebirths_" + sessionManager.getAppLanguage();
            int singlemultiple_id = getResources().getIdentifier(singlemultipleLanguage, "array", requireActivity().getApplicationContext().getPackageName());
            if (singlemultiple_id != 0) {
                adapter_singlemultiplebirths = ArrayAdapter.createFromResource(getContext(), singlemultiple_id, android.R.layout.simple_spinner_dropdown_item);
            }
            binding.spinnerSinglemultiplebirths.setAdapter(adapter_singlemultiplebirths);

        } catch (Exception e) {
            Logger.logE("Identification", "#648", e);
        }


        // Sex of the Baby Adapter
        try {
            String sexbabyLanguage = "sexofbaby_" + sessionManager.getAppLanguage();
            int sexbaby_id = getResources().getIdentifier(sexbabyLanguage, "array", requireActivity().getApplicationContext().getPackageName());
            if (sexbaby_id != 0) {
                adapter_sexofbaby = ArrayAdapter.createFromResource(getContext(), sexbaby_id, android.R.layout.simple_spinner_dropdown_item);
            }
            binding.spinnerSexofbaby.setAdapter(adapter_sexofbaby);

        } catch (Exception e) {
            Logger.logE("Identification", "#648", e);
        }

        // Pregnancy Planned Adapter
        try {
            String pregnancyplannedLanguage = "pregnancyplanned_" + sessionManager.getAppLanguage();
            int pregnancyplanned_id = getResources().getIdentifier(pregnancyplannedLanguage, "array", requireActivity().getApplicationContext().getPackageName());
            if (pregnancyplanned_id != 0) {
                adapter_pregnancyplanned = ArrayAdapter.createFromResource(getContext(),
                        pregnancyplanned_id, android.R.layout.simple_spinner_dropdown_item);
            }
            binding.spinnerPregnancyplanned.setAdapter(adapter_pregnancyplanned);
        } catch (Exception e) {
            Logger.logE("Identification", "#648", e);
        }

        // High Risk Pregnancy Adapter
        try {
            String highriskpregnancyLanguage = "highriskpregnancy_" + sessionManager.getAppLanguage();
            int highriskpregnancy_id = getResources().getIdentifier(highriskpregnancyLanguage, "array", requireActivity().getApplicationContext().getPackageName());
            if (highriskpregnancy_id != 0) {
                adapter_pregnancyhighriskcase = ArrayAdapter.createFromResource(getContext(), highriskpregnancy_id, android.R.layout.simple_spinner_dropdown_item);
            }
            binding.spinnerPregnancyhighriskcase.setAdapter(adapter_pregnancyhighriskcase);
        } catch (Exception e) {
            Logger.logE("Identification", "#648", e);
        }

        // Pregnancy Complications Adapter
        try {
            String complicationsLanguage = "complications_" + sessionManager.getAppLanguage();
            int complications_id = getResources().getIdentifier(complicationsLanguage, "array", requireActivity().getApplicationContext().getPackageName());
            if (complications_id != 0) {
                adapter_pregnancycomplications = ArrayAdapter.createFromResource(getContext(), complications_id, android.R.layout.simple_spinner_dropdown_item);
            }
            binding.spinnerPregnancycomplications.setAdapter(adapter_pregnancycomplications);

        } catch (Exception e) {
            // Toast.makeText(this, "BankAccount values are missing", Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }
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
                if (position == 1) {
                    binding.llChildAlive.setVisibility(View.VISIBLE);
                } else {
                    binding.llChildAlive.setVisibility(View.GONE);
                    binding.edittextBabyagedied.setVisibility(View.GONE);
                    binding.spinnerChildalive.setSelection(0);
                }

                if (position == 5) {
                    binding.edittextMonthspregnancylast.setVisibility(View.GONE);
                    binding.edittextMonthsbeingpregnant.setVisibility(View.VISIBLE);
                } else {
                    binding.edittextMonthspregnancylast.setVisibility(View.VISIBLE);
                    binding.edittextMonthsbeingpregnant.setVisibility(View.GONE);
                }

                if (position == 4 || position == 5) {
                    binding.llDeliveryPlace.setVisibility(View.GONE);
                } else {
                    binding.llDeliveryPlace.setVisibility(View.VISIBLE);
                }

                if (position == 0 || position == 3 || position == 4 || position == 5) {
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
                if (pos == 0 || pos == 1) {
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

    private boolean validateData(PregnancyRosterData data) {
        boolean areDetailsCorrect = true;
        int pregnancyOutcomePosition = binding.spinnerOutcomepregnancy.getSelectedItemPosition();

        if (binding.edittextHowmanytimmespregnant.getText().toString().equalsIgnoreCase("") || binding.edittextHowmanytimmespregnant.getText().toString().isEmpty()) {
            setEditTextError(binding.edittextHowmanytimmespregnant);
            areDetailsCorrect = false;
        }

        if (binding.spinnerPregnantpasttwoyrs.getSelectedItemPosition() == 0) {
            setSpinnerError(binding.spinnerPregnantpasttwoyrs);
            areDetailsCorrect = false;
        }

        if (binding.spinnerPregnantpasttwoyrs.getSelectedItemPosition() == 1) {
            if (data.getPregnancyOutcome().equals(getString(R.string.select))) {
                setSpinnerError(binding.spinnerOutcomepregnancy);
                areDetailsCorrect = false;
            }

            if (pregnancyOutcomePosition == 1) {
                if (data.getIsChildAlive().equals(getString(R.string.select))) {
                    setSpinnerError(binding.spinnerChildalive);
                    areDetailsCorrect = false;
                }
            }

            if (binding.edittextYearofpregnancy.getText().toString().equalsIgnoreCase("") || binding.edittextYearofpregnancy.getText().toString().isEmpty()) {
                setEditTextError(binding.edittextYearofpregnancy);
                areDetailsCorrect = false;
            }

            if (pregnancyOutcomePosition != 5) {
                if (binding.edittextMonthspregnancylast.getText().toString().equalsIgnoreCase("") || binding.edittextMonthspregnancylast.getText().toString().isEmpty()) {
                    setEditTextError(binding.edittextMonthspregnancylast);
                    areDetailsCorrect = false;
                }
            }

            if (pregnancyOutcomePosition == 5 && data.getMonthsBeenPregnant() == null) {
                if (binding.edittextMonthsbeingpregnant.getText().toString().equalsIgnoreCase("") || binding.edittextMonthsbeingpregnant.getText().toString().isEmpty()) {
                    setEditTextError(binding.edittextMonthsbeingpregnant);
                    areDetailsCorrect = false;
                }
            }

            if (pregnancyOutcomePosition != 4 && pregnancyOutcomePosition != 5) {
                if (data.getPlaceOfDelivery().equals(getString(R.string.select))) {
                    setSpinnerError(binding.spinnerPlaceofdeliverypregnant);
                    areDetailsCorrect = false;
                }
            }

            if (binding.spinnerPlaceofdeliverypregnant.getSelectedItemPosition() != 1 &&
                    (pregnancyOutcomePosition != 3 && pregnancyOutcomePosition != 4 && pregnancyOutcomePosition != 5)) {
                if (data.getFocalFacilityForPregnancy().equals("Select Block")) {
                    setSpinnerError(binding.spinnerFocalBlock);
                    areDetailsCorrect = false;
                }
            }

            if (pregnancyOutcomePosition != 3 && pregnancyOutcomePosition != 4 && pregnancyOutcomePosition != 5) {
                if (data.getSingleMultipleBirths().equals(getString(R.string.select))) {
                    setSpinnerError(binding.spinnerSinglemultiplebirths);
                    areDetailsCorrect = false;
                }
                if (data.getSexOfBaby().equals(getString(R.string.select))) {
                    setSpinnerError(binding.spinnerSexofbaby);
                    areDetailsCorrect = false;
                }
                if (data.getPregnancyComplications().equals(getString(R.string.select))) {
                    setSpinnerError(binding.spinnerPregnancycomplications);
                    areDetailsCorrect = false;
                }
            }

            if (pregnancyOutcomePosition == 1 && binding.spinnerChildalive.getSelectedItemPosition() == 2) {
                if (data.getBabyAgeDied().equals(getString(R.string.select))) {
                    setEditTextError(binding.edittextBabyagedied);
                    areDetailsCorrect = false;
                }
            }

            if (data.getPregnancyPlanned().equals(getString(R.string.select))) {
                setSpinnerError(binding.spinnerPregnancyplanned);
                areDetailsCorrect = false;
            }
            if (data.getHighRiskPregnancy().equals(getString(R.string.select))) {
                setSpinnerError(binding.spinnerPregnancyhighriskcase);
                areDetailsCorrect = false;
            }
        }

        return areDetailsCorrect;
    }

    private PregnancyRosterData fetchData() {
        PregnancyRosterData data = new PregnancyRosterData();
        data.setNumberOfTimesPregnant(binding.edittextHowmanytimmespregnant.getText().toString());
        int pregnancyOutcomePosition = binding.spinnerOutcomepregnancy.getSelectedItemPosition();

        data.setAnyPregnancyOutcomesInThePastTwoYears(StringUtils.getPasttwoyrs(binding.spinnerPregnantpasttwoyrs.getSelectedItem().toString(), sessionManager.getAppLanguage()));

        if (binding.spinnerPregnantpasttwoyrs.getSelectedItemPosition() == 1) {
            data.setPregnancyOutcome(StringUtils.getOutcomePregnancy(binding.spinnerOutcomepregnancy.getSelectedItem().toString(), sessionManager.getAppLanguage()));

            if (pregnancyOutcomePosition == 1)
                data.setIsChildAlive(StringUtils.getChildAlive(binding.spinnerChildalive.getSelectedItem().toString(), sessionManager.getAppLanguage()));

            data.setYearOfPregnancyOutcome(binding.edittextYearofpregnancy.getText().toString());

            if (pregnancyOutcomePosition != 5)
                data.setMonthsOfPregnancy(binding.edittextMonthspregnancylast.getText().toString());

            if (pregnancyOutcomePosition == 5)
                data.setMonthsBeenPregnant(binding.edittextMonthsbeingpregnant.getText().toString());

            if (pregnancyOutcomePosition != 4 && pregnancyOutcomePosition != 5)
                data.setPlaceOfDelivery(StringUtils.getPlaceDelivery(binding.spinnerPlaceofdeliverypregnant.getSelectedItem().toString(), sessionManager.getAppLanguage()));

            if (binding.spinnerPlaceofdeliverypregnant.getSelectedItemPosition() != 1 ||
                    (pregnancyOutcomePosition != 3 && pregnancyOutcomePosition != 4 && pregnancyOutcomePosition != 5)) {
                data.setFocalFacilityForPregnancy(StringUtils.getFocalFacility_Block(binding.spinnerFocalBlock.getSelectedItem().toString(), sessionManager.getAppLanguage()));
            }

            if (pregnancyOutcomePosition != 3 && pregnancyOutcomePosition != 4 && pregnancyOutcomePosition != 5) {
                data.setSingleMultipleBirths(StringUtils.getSinglemultiplebirths(binding.spinnerSinglemultiplebirths.getSelectedItem().toString(), sessionManager.getAppLanguage()));
                data.setSexOfBaby(StringUtils.getSexOfBaby(binding.spinnerSexofbaby.getSelectedItem().toString(), sessionManager.getAppLanguage()));
                data.setPregnancyComplications(StringUtils.getComplications(binding.spinnerPregnancycomplications.getSelectedItem().toString(), sessionManager.getAppLanguage()));
            }

            if (pregnancyOutcomePosition == 1 && binding.spinnerChildalive.getSelectedItemPosition() == 2) {
                data.setBabyAgeDied(binding.edittextBabyagedied.getText().toString());
            }

            data.setPregnancyPlanned(StringUtils.getPregnancyPlanned(binding.spinnerPregnancyplanned.getSelectedItem().toString(), sessionManager.getAppLanguage()));
            data.setHighRiskPregnancy(StringUtils.getHighRiskPregnancy(binding.spinnerPregnancyhighriskcase.getSelectedItem().toString(), sessionManager.getAppLanguage()));
        }

        return data;
    }

    private void setSpinnerError(Spinner spinner) {
        TextView textview = (TextView) spinner.getSelectedView();
        textview.setError(getString(R.string.select));
        textview.setTextColor(Color.RED);
    }

    private void setEditTextError(EditText editText) {
        editText.setError(getString(R.string.error_field_required));
    }

    private void setBundleData() {
        binding.edittextHowmanytimmespregnant.setText(data.getNumberOfTimesPregnant());

        int spinnerPosition = adapter_pregnantPastTwoYears.getPosition(StringUtils.getPasttwoyrs_edit(data.getAnyPregnancyOutcomesInThePastTwoYears(), sessionManager.getAppLanguage()));
        binding.spinnerPregnantpasttwoyrs.setSelection(spinnerPosition);

        if (!checkIfEmpty(data.getPregnancyOutcome())) {
            spinnerPosition = adapter_outcomepregnancy.getPosition(StringUtils.getOutcomePregnancy_edit(data.getPregnancyOutcome(), sessionManager.getAppLanguage()));
            binding.spinnerOutcomepregnancy.setSelection(spinnerPosition);
        }

        if (!checkIfEmpty(data.getIsChildAlive())) {
            spinnerPosition = adapter_childalive.getPosition(StringUtils.getChildAlive_edit(data.getIsChildAlive(), sessionManager.getAppLanguage()));
            binding.spinnerChildalive.setSelection(spinnerPosition);
        }

        if (!checkIfEmpty(data.getYearOfPregnancyOutcome())) {
            binding.edittextYearofpregnancy.setText(data.getYearOfPregnancyOutcome());
        }

        if (!checkIfEmpty(data.getMonthsOfPregnancy())) {
            binding.edittextMonthspregnancylast.setText(data.getMonthsOfPregnancy());
        }

        if (!checkIfEmpty(data.getMonthsBeenPregnant())) {
            binding.edittextMonthsbeingpregnant.setText(data.getMonthsBeenPregnant());
        }

        if (!checkIfEmpty(data.getPlaceOfDelivery())) {
            spinnerPosition = adapter_placeofdeliverypregnant.getPosition(StringUtils.getPlaceDelivery_edit(data.getPlaceOfDelivery(), sessionManager.getAppLanguage()));
            binding.spinnerPlaceofdeliverypregnant.setSelection(spinnerPosition);
        }

        if (!checkIfEmpty(data.getFocalFacilityForPregnancy())) {
            spinnerPosition = adapter_focalPointBlock.getPosition(StringUtils.getFocalFacility_Block_edit(data.getFocalFacilityForPregnancy(), sessionManager.getAppLanguage()));
            binding.spinnerFocalBlock.setSelection(spinnerPosition);
        }

        if (!checkIfEmpty(data.getBabyAgeDied())) {
            binding.edittextBabyagedied.setText(data.getBabyAgeDied());
        }

        if (!checkIfEmpty(data.getSexOfBaby())) {
            spinnerPosition = adapter_sexofbaby.getPosition(StringUtils.getSexOfBaby_edit(data.getSexOfBaby(), sessionManager.getAppLanguage()));
            binding.spinnerSexofbaby.setSelection(spinnerPosition);
        }

        if (!checkIfEmpty(data.getPregnancyPlanned())) {
            spinnerPosition = adapter_pregnancyplanned.getPosition(StringUtils.getPregnancyPlanned_edit(data.getPregnancyPlanned(), sessionManager.getAppLanguage()));
            binding.spinnerPregnancyplanned.setSelection(spinnerPosition);
        }

        if (!checkIfEmpty(data.getHighRiskPregnancy())) {
            spinnerPosition = adapter_pregnancyhighriskcase.getPosition(StringUtils.getHighRiskPregnancy_edit(data.getHighRiskPregnancy(), sessionManager.getAppLanguage()));
            binding.spinnerPregnancyhighriskcase.setSelection(spinnerPosition);
        }

        if (!checkIfEmpty(data.getPregnancyComplications())) {
            spinnerPosition = adapter_pregnancycomplications.getPosition(StringUtils.getComplications_edit(data.getPregnancyComplications(), sessionManager.getAppLanguage()));
            binding.spinnerPregnancycomplications.setSelection(spinnerPosition);
        }
    }

    private void extractBundleData(Bundle bundle) {
        data = new PregnancyRosterData();
        data.setNumberOfTimesPregnant(bundle.getString("numberOfTimesPregnant"));
        data.setAnyPregnancyOutcomesInThePastTwoYears(bundle.getString("anyPregnancyOutcomesInThePastTwoYears"));
        data.setPregnancyOutcome(bundle.getString("pregnancyOutcome"));
        data.setIsChildAlive(bundle.getString("isChildAlive"));
        data.setYearOfPregnancyOutcome(bundle.getString("yearOfPregnancyOutcome"));
        data.setMonthsOfPregnancy(bundle.getString("monthsOfPregnancy"));
        data.setMonthsBeenPregnant(bundle.getString("monthsBeenPregnant"));
        data.setPlaceOfDelivery(bundle.getString("placeOfDelivery"));
        data.setFocalFacilityForPregnancy(bundle.getString("focalFacilityForPregnancy"));
        data.setBabyAgeDied(bundle.getString("babyAgeDied"));
        data.setSexOfBaby(bundle.getString("sexOfBaby"));
        data.setPregnancyPlanned(bundle.getString("pregnancyPlanned"));
        data.setHighRiskPregnancy(bundle.getString("highRiskPregnancy"));
        data.setPregnancyComplications(bundle.getString("pregnancyComplications"));
    }

    private Boolean checkIfEmpty(String text) {
        return text == null || text.equals(SELECT) || text.equals(SELECT_BLOCK);
    }
}

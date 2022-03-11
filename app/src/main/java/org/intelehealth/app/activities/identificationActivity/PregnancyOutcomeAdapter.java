package org.intelehealth.app.activities.identificationActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.databinding.PresentationPregnancyRosterBinding;

import java.util.List;

public class PregnancyOutcomeAdapter extends RecyclerView.Adapter<PregnancyOutcomeAdapter.PregnancyViewHolder> {

    private final List<PregnancyRosterData> pregnancyOutcomesList;

    public PregnancyOutcomeAdapter(List<PregnancyRosterData> pregnancyOutcomesList) {
        this.pregnancyOutcomesList = pregnancyOutcomesList;
    }

    @NonNull
    @Override
    public PregnancyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PresentationPregnancyRosterBinding binding = PresentationPregnancyRosterBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new PregnancyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PregnancyViewHolder holder, int position) {
        holder.initData(pregnancyOutcomesList.get(position));
    }

    @Override
    public int getItemCount() {
        if (pregnancyOutcomesList.isEmpty())
            return 0;
        else
            return pregnancyOutcomesList.size();
    }

    static class PregnancyViewHolder extends RecyclerView.ViewHolder {

        public PresentationPregnancyRosterBinding binding;
        public PregnancyRosterData data;
        public final String SELECT = "Select";
        public final String SELECT_BLOCK = "Select Block";

        public PregnancyViewHolder(@NonNull PresentationPregnancyRosterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void initData(PregnancyRosterData data) {
            this.data = data;
            binding.textviewHowmanytimmespregnant.setText(data.getNumberOfTimesPregnant());
            binding.textviewPregnantpasttwoyrs.setText(data.getAnyPregnancyOutcomesInThePastTwoYears());

            if (data.getAnyPregnancyOutcomesInThePastTwoYears().equals("No")) {
                binding.pregnancyQuestionsLinearLayout.setVisibility(View.GONE);
            } else {
                binding.pregnancyQuestionsLinearLayout.setVisibility(View.VISIBLE);

                if (!checkIfEmpty(data.getPregnancyOutcome())) {
                    binding.textviewOutcomepregnancy.setText(data.getPregnancyOutcome());
                    binding.llPregnancyOutcome.setVisibility(View.VISIBLE);
                } else {
                    binding.llPregnancyOutcome.setVisibility(View.GONE);
                }

                if (!checkIfEmpty(data.getIsChildAlive())) {
                    binding.textviewChildalive.setText(data.getIsChildAlive());
                    binding.llChildAlive.setVisibility(View.VISIBLE);
                } else {
                    binding.llChildAlive.setVisibility(View.GONE);
                }

                if (!checkIfEmpty(data.getYearOfPregnancyOutcome())) {
                    binding.textviewYearofpregnancy.setText(data.getYearOfPregnancyOutcome());
                    binding.llYearOfPregnancy.setVisibility(View.VISIBLE);
                } else {
                    binding.llYearOfPregnancy.setVisibility(View.GONE);
                }

                if (!checkIfEmpty(data.getMonthsOfPregnancy())) {
                    binding.textviewMonthspregnancylast.setText(data.getMonthsOfPregnancy());
                    binding.llMonthsPregnancyLast.setVisibility(View.VISIBLE);
                } else {
                    binding.llMonthsPregnancyLast.setVisibility(View.GONE);
                }

                if (!checkIfEmpty(data.getMonthsBeenPregnant())) {
                    binding.textviewMonthsbeingpregnant.setText(data.getMonthsBeenPregnant());
                    binding.llMonthsBeingPregnant.setVisibility(View.VISIBLE);
                } else {
                    binding.llMonthsBeingPregnant.setVisibility(View.GONE);
                }

                if (!checkIfEmpty(data.getPlaceOfDelivery())) {
                    binding.textviewPlaceofdeliverypregnant.setText(data.getPlaceOfDelivery());
                    binding.llDeliveryPlace.setVisibility(View.VISIBLE);
                } else {
                    binding.llDeliveryPlace.setVisibility(View.GONE);
                }

                if (!checkIfEmpty(data.getFocalFacilityForPregnancy())) {
                    binding.textviewFocalBlock.setText(data.getFocalFacilityForPregnancy());
                    binding.llFocalPoint.setVisibility(View.VISIBLE);
                } else {
                    binding.llFocalPoint.setVisibility(View.GONE);
                }

                if (!checkIfEmpty(data.getSingleMultipleBirths())) {
                    binding.textviewSinglemultiplebirths.setText(data.getSingleMultipleBirths());
                    binding.llSingleMultipleBirth.setVisibility(View.VISIBLE);
                } else {
                    binding.llSingleMultipleBirth.setVisibility(View.GONE);
                }

                if (!checkIfEmpty(data.getBabyAgeDied())) {
                    binding.textviewBabyagedied.setText(data.getBabyAgeDied());
                    binding.llBabyAgeDied.setVisibility(View.VISIBLE);
                } else {
                    binding.llBabyAgeDied.setVisibility(View.GONE);
                }

                if (!checkIfEmpty(data.getSexOfBaby())) {
                    binding.textviewSexofbaby.setText(data.getSexOfBaby());
                    binding.llBabyGender.setVisibility(View.VISIBLE);
                } else {
                    binding.llBabyGender.setVisibility(View.GONE);
                }

                if (!checkIfEmpty(data.getPregnancyPlanned())) {
                    binding.textviewPregnancyplanned.setText(data.getPregnancyPlanned());
                    binding.llPregnancyPlanned.setVisibility(View.VISIBLE);
                } else {
                    binding.llPregnancyPlanned.setVisibility(View.GONE);
                }

                if (!checkIfEmpty(data.getHighRiskPregnancy())) {
                    binding.textviewPregnancyhighriskcase.setText(data.getHighRiskPregnancy());
                    binding.llPregnancyHighRiskCase.setVisibility(View.VISIBLE);
                } else {
                    binding.llPregnancyHighRiskCase.setVisibility(View.GONE);
                }

                if (!checkIfEmpty(data.getPregnancyComplications())) {
                    binding.textviewPregnancycomplications.setText(data.getPregnancyComplications());
                    binding.llChildComplications.setVisibility(View.VISIBLE);
                } else {
                    binding.llChildComplications.setVisibility(View.GONE);
                }
            }
        }

        private Boolean checkIfEmpty(String text) {
            return text == null || text.equals(SELECT) || text.equals(SELECT_BLOCK);
        }
    }
}
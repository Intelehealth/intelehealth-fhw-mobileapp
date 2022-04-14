package org.intelehealth.app.activities.identificationActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.databinding.PresentationPregnancyRosterBinding;
import org.intelehealth.app.utilities.StringUtils;

import java.util.List;

public class PregnancyOutcomeAdapter extends RecyclerView.Adapter<PregnancyOutcomeAdapter.PregnancyViewHolder> {

    private final List<PregnancyRosterData> pregnancyOutcomesList;
    private final ViewPagerCallback callback;
    private String appLanguage;

    public PregnancyOutcomeAdapter(List<PregnancyRosterData> pregnancyOutcomesList, ViewPagerCallback callback, String appLanguage) {
        this.pregnancyOutcomesList = pregnancyOutcomesList;
        this.callback = callback;
        this.appLanguage = appLanguage;
    }

    @NonNull
    @Override
    public PregnancyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PresentationPregnancyRosterBinding binding = PresentationPregnancyRosterBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new PregnancyViewHolder(binding, callback, appLanguage);
    }

    @Override
    public void onBindViewHolder(@NonNull PregnancyViewHolder holder, int position) {
        holder.initData(pregnancyOutcomesList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return pregnancyOutcomesList.size();
    }

    static class PregnancyViewHolder extends RecyclerView.ViewHolder {
        public PresentationPregnancyRosterBinding binding;
        public PregnancyRosterData data;
        public ViewPagerCallback callback;
        public String appLanguage;
        public int position;

        public final String SELECT = "Select";
        public final String SELECT_BLOCK = "Select Block";

        public PregnancyViewHolder(@NonNull PresentationPregnancyRosterBinding binding, ViewPagerCallback callback, String appLanguage) {
            super(binding.getRoot());
            this.binding = binding;
            this.callback = callback;
            this.appLanguage = appLanguage;
            binding.mainLinearLayout.setOnClickListener(v -> callback.getPregnancyIssueClicked(data, position));
        }

        public void initData(PregnancyRosterData data, int position) {
            this.data = data;
            this.position = position;

//            binding.textviewHowmanytimmespregnant.setText(data.getNumberOfTimesPregnant());
//            binding.textviewNoofpregnancyoutcomepasttwoyrs.setText(data.getNumberOfPregnancyOutcomePastTwoYrs());
//            binding.textviewPregnantpasttwoyrs.setText(StringUtils.getPasttwoyrs_edit(data.getAnyPregnancyOutcomesInThePastTwoYears(), appLanguage));

//            if (data.getAnyPregnancyOutcomesInThePastTwoYears().equals("No")) { // TODO replace with identification value
//                binding.pregnancyQuestionsLinearLayout.setVisibility(View.GONE);
//            } else {
//                binding.pregnancyQuestionsLinearLayout.setVisibility(View.VISIBLE);

            if (!checkIfEmpty(data.getPregnancyOutcome())) {
                binding.textviewOutcomepregnancy.setText(StringUtils.getOutcomePregnancy_edit(data.getPregnancyOutcome(), appLanguage));
                binding.llPregnancyOutcome.setVisibility(View.VISIBLE);
            } else {
                binding.llPregnancyOutcome.setVisibility(View.GONE);
            }

            if (!checkIfEmpty(data.getIsChildAlive())) {
                binding.textviewChildalive.setText(StringUtils.getChildAlive_edit(data.getIsChildAlive(), appLanguage));
                binding.llChildAlive.setVisibility(View.VISIBLE);
            } else {
                binding.llChildAlive.setVisibility(View.GONE);
            }

            if (!checkIfEmpty(data.getIsPreTerm())) {
                binding.preTermTextView.setText(StringUtils.getPreTermEdit(data.getIsPreTerm(), appLanguage));
                binding.llPreTerm.setVisibility(View.VISIBLE);
            } else {
                binding.llPreTerm.setVisibility(View.GONE);
            }

            if (!checkIfEmpty(data.getYearOfPregnancyOutcome())) {
                String yearOfPregnancyOutcome = appLanguage.equalsIgnoreCase("mr") ? StringUtils.en__mr_dob(data.getYearOfPregnancyOutcome()) : data.getYearOfPregnancyOutcome();
                binding.textviewYearofpregnancy.setText(yearOfPregnancyOutcome);
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
                binding.textviewPlaceofdeliverypregnant.setText(StringUtils.getPlaceDelivery_edit(data.getPlaceOfDelivery(), appLanguage));
                binding.llDeliveryPlace.setVisibility(View.VISIBLE);
            } else {
                binding.llDeliveryPlace.setVisibility(View.GONE);
            }

            if (!checkIfEmpty(data.getTypeOfDelivery())) {
                binding.deliveryTypeTextView.setText(StringUtils.getDeliveryTypeEdit(data.getTypeOfDelivery(), appLanguage));
                binding.llDeliveryType.setVisibility(View.VISIBLE);
            } else {
                binding.llDeliveryType.setVisibility(View.GONE);
            }

            if (!checkIfEmpty(data.getFocalFacilityForPregnancy())) {
                binding.textviewFocalBlock.setText(StringUtils.getFocalFacility_Block_edit(data.getFocalFacilityForPregnancy(), appLanguage));
                binding.llFocalPoint.setVisibility(View.VISIBLE);
                binding.textviewFacilityName.setText(data.getFacilityName());
            } else {
                binding.llFocalPoint.setVisibility(View.GONE);
            }

            if (!checkIfEmpty(data.getSingleMultipleBirths())) {
                binding.textviewSinglemultiplebirths.setText(StringUtils.getSinglemultiplebirths_edit(data.getSingleMultipleBirths(), appLanguage));
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
                binding.textviewSexofbaby.setText(StringUtils.getSexOfBaby_edit(data.getSexOfBaby(), appLanguage));
                binding.llBabyGender.setVisibility(View.VISIBLE);
            } else {
                binding.llBabyGender.setVisibility(View.GONE);
            }

            if (!checkIfEmpty(data.getPregnancyPlanned())) {
                binding.textviewPregnancyplanned.setText(StringUtils.getPregnancyPlanned_edit(data.getPregnancyPlanned(), appLanguage));
                binding.llPregnancyPlanned.setVisibility(View.VISIBLE);
            } else {
                binding.llPregnancyPlanned.setVisibility(View.GONE);
            }

            if (!checkIfEmpty(data.getHighRiskPregnancy())) {
                binding.textviewPregnancyhighriskcase.setText(StringUtils.getHighRiskPregnancy_edit(data.getHighRiskPregnancy(), appLanguage));
                binding.llPregnancyHighRiskCase.setVisibility(View.VISIBLE);
            } else {
                binding.llPregnancyHighRiskCase.setVisibility(View.GONE);
            }

            if (!checkIfEmpty(data.getPregnancyComplications())) {
                binding.textviewPregnancycomplications.setText(StringUtils.getComplications_edit(data.getPregnancyComplications(), appLanguage));
                binding.llChildComplications.setVisibility(View.VISIBLE);
            } else {
                binding.llChildComplications.setVisibility(View.GONE);
            }
//            }
        }

        private Boolean checkIfEmpty(String text) {
            return text == null || text.equals(SELECT) || text.equals(SELECT_BLOCK) || text.equals("");
        }
    }
}
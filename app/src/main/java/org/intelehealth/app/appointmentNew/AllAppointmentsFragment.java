package org.intelehealth.app.appointmentNew;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;

import org.intelehealth.app.R;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AllAppointmentsFragment extends Fragment {
    private static final String TAG = "AllAppointmentsFragment";
    View view;
    LinearLayout cardUpcomingAppointments, cardCancelledAppointments, cardCompletedAppointments, layoutMainAppOptions;
    RecyclerView rvUpcomingApp, rvCancelledApp, rvCompletedApp;
    RelativeLayout layoutParent;
    FrameLayout frameLayoutFilter, frameLayoutDateFilter;
    ImageView ivFilterAllApp, ivDateFilter;
    List<FilterOptionsModel> filtersList;
    List<String> filtersListNew;
    RadioGroup rgFilterAppointments;
    String selectedAppointmentOption;
    RadioButton rbUpcoming, rbCancelled, rbCompleted;
    TextView tvResultsFor;
    HorizontalScrollView scrollChips;
    boolean isChipInit = false;
    LinearLayout layoutUpcoming, layoutCancelled, layoutCompleted, layoutParentAll;
    boolean isNewType = false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_all_appointments_ui2,
                container, false);
        initUI();
        clickListeners();


        return view;
    }

    private void setFiltersToTheGroup(FilterOptionsModel inputModel) {


        //fill the list
        boolean result = false;
        if (filtersList.size() > 0) {

            for (int i = 0; i < filtersList.size(); i++) {
                FilterOptionsModel optionModel1 = filtersList.get(i);

                if (optionModel1.getFilterType().equals(inputModel.getFilterType())) {
                    result = false;

                    filtersList.remove(optionModel1);
                    filtersList.add(inputModel);
                } else {
                    result = true;
                }
            }

            if (result) {
                filtersList.add(inputModel);
            }

        } else {
            filtersList.add(inputModel);
        }

        if (filtersList.size() > 0) {
            tvResultsFor.setVisibility(View.VISIBLE);
            scrollChips.setVisibility(View.VISIBLE);
        }
        final ChipGroup chipGroup = view.findViewById(R.id.chipgroup_filter);
        isChipInit = true;
        chipGroup.removeAllViews();


        for (int index = 0; index < filtersList.size(); index++) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_custom_ui2, chipGroup, false);
            // final Chip chip = new Chip(Objects.requireNonNull(getActivity()));

            FilterOptionsModel filterOptionsModel = filtersList.get(index);
            final String tagName = filterOptionsModel.getFilterValue();
            int paddingDp = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 10,
                    getResources().getDisplayMetrics()
            );
            //   chip.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);
            chip.setText(tagName);
            // chip.setCloseIconResource(R.drawable.ui2_ic_close_drawer);
            //   chip.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
            chip.setCloseIconEnabled(true);
            chip.setBackground(getResources().getDrawable(R.drawable.ui2_ic_selcted_chip_bg));

            chipGroup.addView(chip);

            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    filtersList.remove(filterOptionsModel);
                    chipGroup.removeView(chip);

                    if (tagName.contains("appointment")) {
                        manageUIAsPerChips("upcoming");

                    }
                    if (filtersList != null && filtersList.size() == 0) {
                        tvResultsFor.setVisibility(View.GONE);
                        scrollChips.setVisibility(View.GONE);

                    }

                }
            });

        }

    }

    private void manageUIAsPerChips(String tagName) {
        String textToMatch = tagName.toLowerCase();
        if (textToMatch.contains("upcoming")) {
            updateMAinLayoutAsPerOptionSelected("upcoming");

        } else if (tagName.contains("cancelled")) {
            updateMAinLayoutAsPerOptionSelected("cancelled");

        } else if (tagName.contains("completed")) {
            updateMAinLayoutAsPerOptionSelected("completed");

        }
    }


    private void initUI() {

        rvUpcomingApp = view.findViewById(R.id.rv_all_upcoming_appointments);
        rvCancelledApp = view.findViewById(R.id.rv_all_cancelled_appointments);
        rvCompletedApp = view.findViewById(R.id.rv_all_completed_appointments);
        cardUpcomingAppointments = view.findViewById(R.id.card_upcoming_appointments1);
        cardCancelledAppointments = view.findViewById(R.id.card_cancelled_appointments1);
        cardCompletedAppointments = view.findViewById(R.id.card_completed_appointments1);
        layoutMainAppOptions = view.findViewById(R.id.layout_main_app_options1);
        layoutParent = view.findViewById(R.id.layout_parent_all_appointments);

        frameLayoutFilter = view.findViewById(R.id.filter_frame_all_appointments);
        ivFilterAllApp = view.findViewById(R.id.iv_filter_all_app);
        frameLayoutDateFilter = view.findViewById(R.id.filter_frame_date_appointments);
        ivDateFilter = view.findViewById(R.id.iv_calendar_all_app);
        ivDateFilter = view.findViewById(R.id.iv_calendar_all_app);
        rgFilterAppointments = frameLayoutFilter.findViewById(R.id.rg_filter_appointments);
        rbUpcoming = frameLayoutFilter.findViewById(R.id.rb_upcoming_appointments);
        rbCancelled = frameLayoutFilter.findViewById(R.id.rb_cancelled_appointments);
        rbCompleted = frameLayoutFilter.findViewById(R.id.rb_completed_appointments);
        tvResultsFor = view.findViewById(R.id.tv_results_for);
        scrollChips = view.findViewById(R.id.scroll_chips);

        layoutUpcoming = view.findViewById(R.id.layout_upcoming1);
        layoutCancelled = view.findViewById(R.id.layout_cancelled1);
        layoutCompleted = view.findViewById(R.id.layout_completed1);
        layoutParentAll = view.findViewById(R.id.layout_parent_all1);


        if (isChipInit) {
            tvResultsFor.setVisibility(View.VISIBLE);
            scrollChips.setVisibility(View.VISIBLE);
        } else {
            tvResultsFor.setVisibility(View.GONE);
            scrollChips.setVisibility(View.GONE);
        }

        //recyclerview for upcoming appointments
        MyAllAppointmentsAdapter myAllAppointmentsAdapter = new MyAllAppointmentsAdapter(getActivity());
        rvUpcomingApp.setAdapter(myAllAppointmentsAdapter);

        //recyclerview for cancelled appointments
        MyAllAppointmentsAdapter myAllAppointmentsAdapter1 = new MyAllAppointmentsAdapter(getActivity());
        rvCancelledApp.setAdapter(myAllAppointmentsAdapter1);

        //recyclerview for completed appointments
        MyAllAppointmentsAdapter myAllAppointmentsAdapter2 = new MyAllAppointmentsAdapter(getActivity());
        rvCompletedApp.setAdapter(myAllAppointmentsAdapter2);


        filtersList = new ArrayList<>();
        filtersListNew = new ArrayList<>();


        updateCardBackgrounds("upcoming");


    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void clickListeners() {
        cardUpcomingAppointments.setOnClickListener(v -> {

            updateCardBackgrounds("upcoming");
            updateMAinLayoutAsPerOptionSelected("upcoming");
        });
        cardCancelledAppointments.setOnClickListener(v -> {

            updateCardBackgrounds("cancelled");
            updateMAinLayoutAsPerOptionSelected("cancelled");

        });
        cardCompletedAppointments.setOnClickListener(v -> {

            updateCardBackgrounds("completed");
            updateMAinLayoutAsPerOptionSelected("completed");

        });

        //dismiss filter options layouts
        layoutParent.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (frameLayoutFilter.isShown())
                    frameLayoutFilter.setVisibility(View.GONE);
                if (frameLayoutDateFilter.isShown())
                    frameLayoutDateFilter.setVisibility(View.GONE);

                return true;
            }
            return false;
        });
        layoutParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (frameLayoutFilter.isShown())
                    frameLayoutFilter.setVisibility(View.GONE);
                if (frameLayoutDateFilter.isShown())
                    frameLayoutDateFilter.setVisibility(View.GONE);

            }
        });

        //click listeners for filters
        ivFilterAllApp.setOnClickListener(v -> {

            // filter options
            if (frameLayoutDateFilter.isShown())
                frameLayoutDateFilter.setVisibility(View.GONE);
            if (frameLayoutFilter.getVisibility() == View.VISIBLE)
                frameLayoutFilter.setVisibility(View.GONE);
            else
                frameLayoutFilter.setVisibility(View.VISIBLE);
        });

        ivDateFilter.setOnClickListener(v -> {

            FilterOptionsModel filterOptionsModel = new FilterOptionsModel("date", "selected date");

            setFiltersToTheGroup(filterOptionsModel);
            // filter options
            if (frameLayoutFilter.isShown())
                frameLayoutFilter.setVisibility(View.GONE);
            if (frameLayoutDateFilter.getVisibility() == View.VISIBLE)
                frameLayoutDateFilter.setVisibility(View.GONE);
            else
                frameLayoutDateFilter.setVisibility(View.VISIBLE);


        });

        //filter all appointments
        rgFilterAppointments.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton checkedRadioButton = group.findViewById(checkedId);
            boolean isChecked = checkedRadioButton.isChecked();
            if (isChecked) {
                selectedAppointmentOption = checkedRadioButton.getText().toString();

                onRadioButtonClicked(checkedRadioButton, selectedAppointmentOption);


            }
        });


    }

    private void updateCardBackgrounds(String cardName) {
        if (cardName.equals("upcoming")) {
            cardCancelledAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
            cardCompletedAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
            layoutMainAppOptions.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
            cardUpcomingAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_bg_selcted_card));
        } else if (cardName.equals("cancelled")) {
            cardUpcomingAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
            cardCompletedAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));

            layoutMainAppOptions.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
            cardCancelledAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_bg_selcted_card));
        } else if (cardName.equals("completed")) {
            cardCancelledAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
            cardUpcomingAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));

            layoutMainAppOptions.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
            cardCompletedAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_bg_selcted_card));
        }

    }

    private void updateMAinLayoutAsPerOptionSelected(String cardName) {
        Log.d(TAG, "updateMAinLayoutAsPerOptionSelected: cardName : " + cardName);
        if (cardName.equals("upcoming")) {
            layoutUpcoming.setVisibility(View.VISIBLE);
            layoutCompleted.setVisibility(View.VISIBLE);
            layoutCancelled.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT);
            params.weight = 1.0f;
            params.gravity = Gravity.TOP;

            layoutUpcoming.setLayoutParams(params);
        } else if (cardName.equals("cancelled")) {
            layoutUpcoming.setVisibility(View.GONE);
            layoutCompleted.setVisibility(View.VISIBLE);
            layoutCancelled.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT);
            params.weight = 1.0f;
            params.gravity = Gravity.TOP;

            layoutCancelled.setLayoutParams(params);
        } else if (cardName.equals("completed")) {
            layoutCompleted.setVisibility(View.VISIBLE);
            layoutCancelled.setVisibility(View.GONE);
            layoutUpcoming.setVisibility(View.GONE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT);
            params.weight = 1.0f;
            params.gravity = Gravity.TOP;

            layoutCompleted.setLayoutParams(params);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onRadioButtonClicked(View view, String selectedAppointmentOption) {
        boolean checked = ((RadioButton) view).isChecked();
        RadioButton checkedRadioButton = ((RadioButton) view);
        FilterOptionsModel filterOptionsModel = new FilterOptionsModel("appointment", selectedAppointmentOption);

        setFiltersToTheGroup(filterOptionsModel);
        switch (view.getId()) {
            case R.id.rb_upcoming_appointments:
                if (checked) {
                    Log.d(TAG, "onRadioButtonClicked:upcoming " + selectedAppointmentOption);
                    rbUpcoming.setButtonDrawable(getActivity().getDrawable(R.drawable.ui2_ic_selected_green));
                    rbCancelled.setButtonDrawable(getActivity().getDrawable(R.drawable.ui2_ic_circle));
                    rbCompleted.setButtonDrawable(getActivity().getDrawable(R.drawable.ui2_ic_circle));
                    updateCardBackgrounds("upcoming");
                    updateMAinLayoutAsPerOptionSelected("upcoming");

                }

                break;
            case R.id.rb_cancelled_appointments:
                if (checked) {
                    rbUpcoming.setButtonDrawable(getActivity().getDrawable(R.drawable.ui2_ic_circle));
                    rbCancelled.setButtonDrawable(getActivity().getDrawable(R.drawable.ui2_ic_selected_green));
                    rbCompleted.setButtonDrawable(getActivity().getDrawable(R.drawable.ui2_ic_circle));
                    Log.d(TAG, "onRadioButtonClicked:cancelled " + selectedAppointmentOption);
                    updateCardBackgrounds("cancelled");
                    updateMAinLayoutAsPerOptionSelected("cancelled");

                }

                break;
            case R.id.rb_completed_appointments:
                if (checked) {
                    rbUpcoming.setButtonDrawable(getActivity().getDrawable(R.drawable.ui2_ic_circle));
                    rbCancelled.setButtonDrawable(getActivity().getDrawable(R.drawable.ui2_ic_circle));
                    rbCompleted.setButtonDrawable(getActivity().getDrawable(R.drawable.ui2_ic_selected_green));
                    updateCardBackgrounds("completed");

                    updateMAinLayoutAsPerOptionSelected("completed");

                }

                break;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                frameLayoutFilter.setVisibility(View.GONE);
            }
        }, 1000);
    }

    public class FilterOptionsModel {
        public FilterOptionsModel(String filterType, String filterValue) {
            this.filterType = filterType;
            this.filterValue = filterValue;
        }

        public String getFilterType() {
            return filterType;
        }

        public void setFilterType(String filterType) {
            this.filterType = filterType;
        }

        public String getFilterValue() {
            return filterValue;
        }

        public void setFilterValue(String filterValue) {
            this.filterValue = filterValue;
        }

        String filterType, filterValue;
    }

}

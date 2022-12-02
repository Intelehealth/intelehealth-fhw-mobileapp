package org.intelehealth.app.appointmentNew;

import static org.intelehealth.app.database.dao.EncounterDAO.getStartVisitNoteEncounterByVisitUUID;
import static org.intelehealth.app.database.dao.PatientsDAO.isVisitPresentForPatient_fetchVisitValues;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.appointment.api.ApiClientAppointment;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.AppointmentInfo;
import org.intelehealth.app.appointment.model.AppointmentListingResponse;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.models.dto.VisitDTO;
import org.intelehealth.app.ui2.calendarviewcustom.CustomCalendarViewUI2;
import org.intelehealth.app.ui2.calendarviewcustom.SendSelectedDateInterface;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.exception.DAOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class AllAppointmentsFragment extends Fragment implements SendSelectedDateInterface {
    private static final String TAG = "AllAppointmentsFragment";
    View view;
    LinearLayout cardUpcomingAppointments, cardCancelledAppointments, cardCompletedAppointments, layoutMainAppOptions;
    RecyclerView rvUpcomingApp, rvCancelledApp, rvCompletedApp;
    RelativeLayout layoutParent;
    FrameLayout frameLayoutFilter, frameLayoutDateFilter;
    ImageView ivFilterAllApp, ivDateFilter, ivClearText;
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
    TextView tvUpcomingAppsCount, tvCompletedAppsCount, tvUpcomingAppsCountTitle, tvCompletedAppsCountTitle;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
    private SQLiteDatabase db;
    String fromDate = "";
    String toDate = "";
    String whichAppointment = "";
    EditText autotvSearch;
    String searchPatientText = "";
    View noDataFoundForUpcoming, noDataFoundForCompleted;
    TextView tvFromDate, tvToDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private DatePickerDialog.OnDateSetListener mDateSetListener1;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onResume() {
        super.onResume();

    }

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
                        fromDate = "";
                        toDate = "";
                        whichAppointment = "";
                        getAppointments();

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
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

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
        tvFromDate = frameLayoutDateFilter.findViewById(R.id.tv_from_date_all_app);
        tvToDate = frameLayoutDateFilter.findViewById(R.id.tv_to_date_all_app);


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

        tvUpcomingAppsCount = view.findViewById(R.id.tv_upcoming_appointments_all);
        tvCompletedAppsCount = view.findViewById(R.id.tv_completed_appointments_all);
        tvUpcomingAppsCountTitle = view.findViewById(R.id.tv_upcoming_apps_title_all);
        tvCompletedAppsCountTitle = view.findViewById(R.id.tv_completed_apps_title_all);

        autotvSearch = view.findViewById(R.id.et_search_all);
        ivClearText = view.findViewById(R.id.iv_clear_all);
        ivClearText.setOnClickListener(v -> {
            autotvSearch.setText("");
            searchPatientText = "";
            getAppointments();
        });
        //no data found
        noDataFoundForUpcoming = view.findViewById(R.id.layout_no_data_found_upcoming);
        noDataFoundForCompleted = view.findViewById(R.id.layout_no_data_found_completed);


        if (isChipInit) {
            tvResultsFor.setVisibility(View.VISIBLE);
            scrollChips.setVisibility(View.VISIBLE);
        } else {
            tvResultsFor.setVisibility(View.GONE);
            scrollChips.setVisibility(View.GONE);
        }


     /*   //recyclerview for cancelled appointments
        MyAllAppointmentsAdapter myAllAppointmentsAdapter1 = new MyAllAppointmentsAdapter(getActivity());
        rvCancelledApp.setAdapter(myAllAppointmentsAdapter1);

        //recyclerview for completed appointments
        MyAllAppointmentsAdapter myAllAppointmentsAdapter2 = new MyAllAppointmentsAdapter(getActivity());
        rvCompletedApp.setAdapter(myAllAppointmentsAdapter2);

*/
        filtersList = new ArrayList<>();
        filtersListNew = new ArrayList<>();


        updateCardBackgrounds("upcoming");

        getAppointments();
        getSlots();
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

            //selectDateRange();
            // selectDateRangeNew();


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
        autotvSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (!autotvSearch.getText().toString().isEmpty()) {
                        searchPatientText = autotvSearch.getText().toString();
                        getUpcomingAppointments(fromDate, toDate, searchPatientText);
                        getCompletedAppointments(fromDate, toDate, searchPatientText);
                    } else {
                        searchPatientText = "";

                        Log.d(TAG, "afterTextChanged: in else");
                        getAppointments();
                    }
                    return true;
                }
                return false;
            }
        });

        autotvSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    ivClearText.setVisibility(View.VISIBLE);
                } else {
                    searchPatientText = "";
                    getAppointments();
                    ivClearText.setVisibility(View.GONE);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

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
                    whichAppointment = "upcoming";

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
                    whichAppointment = "cancelled";

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
                    whichAppointment = "completed";

                    rbUpcoming.setButtonDrawable(getActivity().getDrawable(R.drawable.ui2_ic_circle));
                    rbCancelled.setButtonDrawable(getActivity().getDrawable(R.drawable.ui2_ic_circle));
                    rbCompleted.setButtonDrawable(getActivity().getDrawable(R.drawable.ui2_ic_selected_green));
                    updateCardBackgrounds("completed");

                    updateMAinLayoutAsPerOptionSelected("completed");

                }

                break;

        }

        if (whichAppointment.isEmpty()) {
            //call both
            getUpcomingAppointments(fromDate, toDate, searchPatientText);
            getCompletedAppointments(fromDate, toDate, searchPatientText);

        } else if (whichAppointment.equals("upcoming")) {
            //upcoming
            getUpcomingAppointments(fromDate, toDate, searchPatientText);

        } else if (whichAppointment.equals("completed")) {
            getCompletedAppointments(fromDate, toDate, searchPatientText);

        }
        new Handler().postDelayed(() -> frameLayoutFilter.setVisibility(View.GONE), 1000);
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

    private void getAppointments() {
        //whichAppointment = "";
        getUpcomingAppointments(fromDate, toDate, searchPatientText);
        getCompletedAppointments(fromDate, toDate, searchPatientText);
    }

    private void getUpcomingAppointments(String fromDate, String toDate,
                                         String searchPatientText) {
        //recyclerview for upcoming appointments
        Log.d(TAG, "getUpcomingAppointments: fromDate : " + fromDate);
        Log.d(TAG, "getUpcomingAppointments: toDate : " + toDate);
        Log.d(TAG, "getUpcomingAppointments: searchPatientText : " + searchPatientText);
        tvUpcomingAppsCount.setText("0");
        tvUpcomingAppsCountTitle.setText("Completed (0)");
        List<AppointmentInfo> appointmentInfoList = new AppointmentDAO().getAppointmentsWithFilters(fromDate, toDate, searchPatientText);
        Log.d(TAG, "getUpcomingAppointments: appointmentInfoList size : " + appointmentInfoList.size());
        List<AppointmentInfo> upcomingAppointmentsList = new ArrayList<>();
        try {
            if (appointmentInfoList.size() > 0) {
                rvUpcomingApp.setVisibility(View.VISIBLE);
                noDataFoundForUpcoming.setVisibility(View.GONE);
                for (int i = 0; i < appointmentInfoList.size(); i++) {
                    AppointmentInfo appointmentInfo = appointmentInfoList.get(i);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
                    String currentDateTime = dateFormat.format(new Date());
                    String slottime = appointmentInfo.getSlotDate() + " " + appointmentInfo.getSlotTime();

                    long diff = dateFormat.parse(slottime).getTime() - dateFormat.parse(currentDateTime).getTime();

                    long second = diff / 1000;
                    long minutes = second / 60;
                    if (appointmentInfo.getStatus().equalsIgnoreCase("booked") && minutes >= 0) {
                        upcomingAppointmentsList.add(appointmentInfo);
                    }
                }

                //recyclerview for upcoming appointments

                AllAppointmentsAdapter allAppointmentsAdapter = new
                        AllAppointmentsAdapter(getActivity(), upcomingAppointmentsList, "upcoming");
                rvUpcomingApp.setAdapter(allAppointmentsAdapter);

            } else {

                rvUpcomingApp.setVisibility(View.GONE);
                noDataFoundForUpcoming.setVisibility(View.VISIBLE);
            }

            tvUpcomingAppsCount.setText(upcomingAppointmentsList.size() + "");
            tvUpcomingAppsCountTitle.setText("Upcoming (" + upcomingAppointmentsList.size() + ")");

        } catch (Exception e) {
            Log.d(TAG, "getUpcomingAppointments: e : " + e.getLocalizedMessage());
        }


    }

    private void getCompletedAppointments(String fromDate, String toDate, String searchPatientText) {
        Log.d(TAG, "55getCompletedAppointments: searchPatientText : " + searchPatientText);
        Log.d(TAG, "55getCompletedAppointments:fromDate :  " + fromDate);
        Log.d(TAG, "55getCompletedAppointments:toDate :  " + toDate);

        tvCompletedAppsCount.setText("0");
        tvCompletedAppsCountTitle.setText("Completed (0)");
        List<AppointmentInfo> appointmentInfoList = new AppointmentDAO().getAppointmentsWithFilters(fromDate, toDate, searchPatientText);
        List<AppointmentInfo> completedAppointmentsList = new ArrayList<>();

        try {
            if (appointmentInfoList.size() > 0) {
                for (int i = 0; i < appointmentInfoList.size(); i++) {
                    AppointmentInfo appointmentInfo = appointmentInfoList.get(i);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
                    String currentDateTime = dateFormat.format(new Date());
                    String slottime = appointmentInfo.getSlotDate() + " " + appointmentInfo.getSlotTime();

                    long diff = dateFormat.parse(slottime).getTime() - dateFormat.parse(currentDateTime).getTime();

                    long second = diff / 1000;
                    long minutes = second / 60;
                    //for appointment is completed/ appointment time has been passed
                    if (appointmentInfo.getStatus().equalsIgnoreCase("visit closed")
                            || ((appointmentInfo.getStatus().equals("booked") && minutes <= 0))) {
                        //check patient uuid in visit table
                        completedAppointmentsList.add(appointmentInfo);
                    }
                }
            }

            if (completedAppointmentsList.size() > 0) {
                rvCompletedApp.setVisibility(View.VISIBLE);
                noDataFoundForCompleted.setVisibility(View.GONE);

                tvCompletedAppsCount.setText(completedAppointmentsList.size() + "");
                tvCompletedAppsCountTitle.setText("Completed (" + completedAppointmentsList.size() + ")");
                getDataForCompletedAppointments(completedAppointmentsList);
            } else {
                //no data found
                rvCompletedApp.setVisibility(View.GONE);
                noDataFoundForCompleted.setVisibility(View.VISIBLE);

                tvCompletedAppsCount.setText(completedAppointmentsList.size() + "");
                tvCompletedAppsCountTitle.setText("Completed (" + completedAppointmentsList.size() + ")");
            }
        } catch (Exception e) {
            Log.d(TAG, "getCompletedAppointments: e : " + e.getLocalizedMessage());
        }

    }

    private void getSlots() {
        Log.d(TAG, "getSlots: date1 : " + DateAndTimeUtils.getCurrentDateInDDMMYYYYFormat());
        Log.d(TAG, "getSlots: date2 : " + DateAndTimeUtils.getOneMonthAheadDateInDDMMYYYYFormat());

        String baseurl = "https://" + new SessionManager(getActivity()).getServerUrl() + ":3004";
        Log.d(TAG, "getSlots: baseurl : " + baseurl);
        ApiClientAppointment.getInstance(baseurl).getApi()
                .getSlotsAll(DateAndTimeUtils.getCurrentDateInDDMMYYYYFormat(),
                        DateAndTimeUtils.getOneMonthAheadDateInDDMMYYYYFormat(),
                        new SessionManager(getActivity()).getLocationUuid())

                .enqueue(new Callback<AppointmentListingResponse>() {
                    @Override
                    public void onResponse(Call<AppointmentListingResponse> call, retrofit2.Response<AppointmentListingResponse> response) {
                        if (response.body() == null) return;
                        AppointmentListingResponse slotInfoResponse = response.body();
                        AppointmentDAO appointmentDAO = new AppointmentDAO();
                        appointmentDAO.deleteAllAppointments();
                        for (int i = 0; i < slotInfoResponse.getData().size(); i++) {

                            try {
                                appointmentDAO.insert(slotInfoResponse.getData().get(i));
                            } catch (DAOException e) {
                                e.printStackTrace();
                            }
                        }

                        getAppointments();
                    }

                    @Override
                    public void onFailure(Call<AppointmentListingResponse> call, Throwable t) {
                        Log.v("onFailure", t.getMessage());
                    }
                });

        Log.d(TAG, "getSlots: location : " + new SessionManager(getActivity()).getLocationUuid());

    }

    private void getDataForCompletedAppointments(List<AppointmentInfo> appointmentsDaoList) {
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        //check if visit is present or not

        for (int i = 0; i < appointmentsDaoList.size(); i++) {
            VisitDTO visitDTO = isVisitPresentForPatient_fetchVisitValues(appointmentsDaoList.get(i).getPatientId());

            //get values from visit
            if (visitDTO.getUuid() != null && visitDTO.getStartdate() != null) {

                String encounteruuid = getStartVisitNoteEncounterByVisitUUID(visitDTO.getUuid());
                if (!encounteruuid.isEmpty() && !encounteruuid.equalsIgnoreCase("")) {
                    appointmentsDaoList.get(i).setPrescription_exists(true);
                } else {
                    appointmentsDaoList.get(i).setPrescription_exists(false);
                }
                String patientProfilePath = getPatientProfile(appointmentsDaoList.get(i).getPatientId());
                // String patientProfilePath = getPatientProfile("984af313-83c7-479e-b8a7-8e72e7384346");
                appointmentsDaoList.get(i).setPatientProfilePhoto(patientProfilePath);

                try {
                    String encounterId = EncounterDAO.getEncounterIdForCompletedVisit(visitDTO.getUuid());
                    String prescReceivedTime = EncounterDAO.getPrescriptionReceivedTime(encounterId);
                    if (prescReceivedTime != null && !prescReceivedTime.isEmpty()) {
                        appointmentsDaoList.get(i).setPresc_received_time(prescReceivedTime);
                    }

                } catch (DAOException e) {
                    e.printStackTrace();
                }

                //recyclerview for completed appointments
                AllAppointmentsAdapter allAppointmentsFragment = new
                        AllAppointmentsAdapter(getActivity(), appointmentsDaoList, "completed");
                rvCompletedApp.setAdapter(allAppointmentsFragment);

            } else {
            }
        }
        Log.d(TAG, "getDataForCompletedAppointments: comp size : " + appointmentsDaoList.size());


    }

    private String getPatientProfile(String patientUuid) {
        Log.d(TAG, "getPatientProfile: patientUuid : " + patientUuid);
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        String imagePath = "";

        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_patient where uuid = ? ",
                new String[]{patientUuid});

        if (idCursor.moveToFirst()) {
            do {
                imagePath = idCursor.getString(idCursor.getColumnIndexOrThrow("patient_photo"));

            } while (idCursor.moveToNext());
            idCursor.close();
        }
        return imagePath;

    }

    private void selectDateRange() {
        tvFromDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    mDateSetListener,
                    year, month, day);
            datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

            datePickerDialog.show();
        });

        mDateSetListener = (datePicker, year, month, day) -> {
            month = month + 1;

            //String date = day + "-" + month + "-" + year;
            String sDay = "";
            if (day < 10) {
                sDay = "0" + String.valueOf(day);
            } else {
                sDay = String.valueOf(day);
            }

            String sMonth = "";
            if (month < 10) {
                sMonth = "0" + String.valueOf(month);
            } else {
                sMonth = String.valueOf(month);
            }
            String date = year + "-" + sMonth + "-" + sDay;

            //  String dateToshow = sDay + "-" + sMonth + "-" + year;


            fromDate = sDay + "/" + sMonth + "/" + year;
            String dateToshow1 = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(fromDate);
            tvFromDate.setText(dateToshow1 + ", " + year);

            dismissDateFilterDialog();

        };

        tvToDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    mDateSetListener1,
                    year, month, day);
            datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

            datePickerDialog.show();
        });

        mDateSetListener1 = (datePicker, year, month, day) -> {
            month = month + 1;

            //String date = day + "-" + month + "-" + year;
            String sDay = "";
            if (day < 10) {
                sDay = "0" + String.valueOf(day);
            } else {
                sDay = String.valueOf(day);
            }

            String sMonth = "";
            if (month < 10) {
                sMonth = "0" + String.valueOf(month);
            } else {
                sMonth = String.valueOf(month);
            }
            String date = year + "-" + sMonth + "-" + sDay;

            // String dateToshow = sDay + "-" + sMonth + "-" + year;
            toDate = sDay + "/" + sMonth + "/" + year;

            String dateToshow = sDay + "-" + sMonth + "-" + year;
            String dateToshow1 = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(toDate);
            tvToDate.setText(dateToshow1 + ", " + year);
            dismissDateFilterDialog();

        };


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void selectDateRangeNew() {

        tvFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: tvFromDate clicked");
                CustomCalendarViewUI2 customCalendarViewUI2 = new CustomCalendarViewUI2(getActivity());
                String selectedDate = customCalendarViewUI2.showDatePicker(getActivity(), "fromdate");
                Log.d(TAG, "selectDateRangeNew: selectedDate return from: " + selectedDate);


             /*
             old logic before cust calendarview
             fromDate = selectedDate;
                String dateToshow1 = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(fromDate);
                if (!fromDate.isEmpty()) {
                    String[] splitedDate = fromDate.split("/");
                    tvFromDate.setText(dateToshow1 + ", " + splitedDate[2]);
                } else {
                    Log.d(TAG, "onClick: date empty");
                }

                dismissDateFilterDialog();*/
            }
        });
        tvToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomCalendarViewUI2 customCalendarViewUI2 = new CustomCalendarViewUI2(getActivity());
                String selectedDate = customCalendarViewUI2.showDatePicker(getActivity(), "todate");
                Log.d(TAG, "selectDateRangeNew: selectedDate return to: " + selectedDate);

              /*  toDate = selectedDate;
                String dateToshow1 = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(toDate);
                if (!toDate.isEmpty()) {
                    String[] splitedDate = toDate.split("/");
                    tvToDate.setText(dateToshow1 + ", " + splitedDate[2]);
                } else {
                    Log.d(TAG, "onClick: date empty");
                }

                dismissDateFilterDialog();*/
            }
        });

    }

    private void filterAsPerSelectedOptions() {
        Log.d(TAG, "newselectDateRange: fromDate : " + fromDate);
        Log.d(TAG, "newselectDateRange: todate : " + toDate);
        Log.d(TAG, "newselectDateRange: whichAppointment : " + whichAppointment);

        if (whichAppointment.isEmpty() && fromDate.isEmpty() && toDate.isEmpty()) {
            //all data
            Log.d(TAG, "filterAsPerSelectedOptions: all data");
            getUpcomingAppointments(fromDate, toDate, searchPatientText);
            getCompletedAppointments(fromDate, toDate, searchPatientText);
        } else if (whichAppointment.isEmpty() && !fromDate.isEmpty() && !toDate.isEmpty()) {
            //all
            getUpcomingAppointments(fromDate, toDate, searchPatientText);
            getCompletedAppointments(fromDate, toDate, searchPatientText);
        } else if (whichAppointment.equals("upcoming") && !fromDate.isEmpty() && !toDate.isEmpty()) {
            //upcoming
            getUpcomingAppointments(fromDate, toDate, searchPatientText);

        } else if (whichAppointment.equals("completed") && !fromDate.isEmpty() && !toDate.isEmpty()) {
            getCompletedAppointments(fromDate, toDate, searchPatientText);

        }
    }


    private void dismissDateFilterDialog() {
        if (!fromDate.isEmpty() && !toDate.isEmpty()) {
            filterAsPerSelectedOptions();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String date = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(fromDate) + " - " + DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(toDate);
                    FilterOptionsModel filterOptionsModel = new FilterOptionsModel("date", date);
                    setFiltersToTheGroup(filterOptionsModel);
                    frameLayoutDateFilter.setVisibility(View.GONE);
                }
            }, 1000);
        }
    }

    @Override
    public void getSelectedDate(String selectedDate, String whichDate) {
        Log.d(TAG, "getSelectedDate: selectedDate from interface : " + selectedDate);
        frameLayoutDateFilter = view.findViewById(R.id.filter_frame_date_appointments);

        tvFromDate = frameLayoutDateFilter.findViewById(R.id.tv_from_date_all_app);
        tvToDate = frameLayoutDateFilter.findViewById(R.id.tv_to_date_all_app);

        if (!whichDate.isEmpty() && whichDate.equals("fromdate")) {
            fromDate = selectedDate;
            String dateToshow1 = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(fromDate);
            if (!fromDate.isEmpty()) {
                String[] splitedDate = fromDate.split("/");
                tvFromDate.setText(dateToshow1 + ", " + splitedDate[2]);
                Log.d(TAG, "getSelectedDate: splitedDate : " + dateToshow1 + ", " + splitedDate[2]);

            } else {
                Log.d(TAG, "onClick: date empty");
            }

            dismissDateFilterDialog();
        }
        if (!whichDate.isEmpty() && whichDate.equals("todate")) {

            toDate = selectedDate;
            String dateToshow1 = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(toDate);
            if (!toDate.isEmpty()) {
                String[] splitedDate = toDate.split("/");
                tvToDate.setText(dateToshow1 + ", " + splitedDate[2]);
            } else {
                Log.d(TAG, "onClick: date empty");
            }

            dismissDateFilterDialog();
        }
    }

}

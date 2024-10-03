package org.intelehealth.app.appointmentNew;

import static org.intelehealth.app.database.dao.EncounterDAO.getStartVisitNoteEncounterByVisitUUID;
import static org.intelehealth.app.database.dao.PatientsDAO.isVisitPresentForPatient_fetchVisitValues;
import static org.intelehealth.app.utilities.DateAndTimeUtils.D_FORMAT_dd_M_yyyy;
import static org.intelehealth.app.utilities.constatnt.BundleConstants.FROM_DATE;
import static org.intelehealth.app.utilities.constatnt.BundleConstants.SELECTED_DATE;
import static org.intelehealth.app.utilities.constatnt.BundleConstants.TO_DATE;
import static org.intelehealth.app.utilities.constatnt.BundleConstants.WHICH_DATE;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import org.intelehealth.app.utilities.CustomLog;
import android.util.TypedValue;
import android.view.Gravity;
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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.intelehealth.app.R;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.AppointmentInfo;
import org.intelehealth.app.appointmentNew.MyAppointmentNew.MyAppointmentActivityNew;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.models.dto.VisitDTO;
import org.intelehealth.app.ui2.calendarviewcustom.CustomCalendarViewUI2;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.exception.DAOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AllAppointmentsFragment extends Fragment {
    private static final String TAG = "AllAppointmentsFragment";
    View parentView;
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
    TextView tvUpcomingAppsCount, tvCompletedAppsCount, tvUpcomingAppsCountTitle,
            tvCompletedAppsCountTitle, tvCancelledAppsCount, tvCancelledAppsCountTitle;
    String fromDate = "";
    String toDate = "";
    String whichAppointment = "";
    EditText autotvSearch;
    String searchPatientText = "";
    View noDataFoundForUpcoming, noDataFoundForCompleted, noDataFoundForCancelled;
    TextView tvFromDate, tvToDate;
    int MY_REQUEST_CODE = 5555;
    private UpdateAppointmentsCount listener;
    int totalUpcomingApps = 0;
    int totalCancelled = 0;
    int totalCompleted = 0;
    SessionManager sessionManager;
    String currentDate = "";

    private NestedScrollView nsvToday;

    private final int upcomingLimit = 15;
    private final int completedLimit = 15;
    private final int cancelledLimit = 15;
    private int upcomingStart = 0, upcomingEnd = upcomingStart + upcomingLimit;
    private int completedStart = 0, completedEnd = completedStart + completedLimit;
    private int cancelledStart = 0, cancelledEnd = cancelledStart + cancelledLimit;

    private boolean isUpcomingFullyLoaded = false;
    private boolean isCompletedFullyLoaded = false;
    private boolean isCancelledFullyLoaded = false;

    private List<AppointmentInfo> upcomingAppointmentInfoList;
    private List<AppointmentInfo> completedAppointmentInfoList;
    private List<AppointmentInfo> cancelledAppointmentInfoList;

    private final List<AppointmentInfo> upcomingSearchList = new ArrayList<>();
    private final List<AppointmentInfo> completedSearchList = new ArrayList<>();
    private final List<AppointmentInfo> cancelledSearchList = new ArrayList<>();

    private AllAppointmentsAdapter upcomingAllAppointmentsAdapter;
    private AllAppointmentsAdapter completedAllAppointmentsAdapter;
    private AllAppointmentsAdapter cancelledAllAppointmentsAdapter;


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocale(getContext());
        ((MyAppointmentActivityNew) getActivity()).initUpdateFragmentOnEvent(1, new UpdateFragmentOnEvent() {
            @Override
            public void onStart(int eventFlag) {
                CustomLog.v(TAG, "onStart");
            }

            @Override
            public void onFinished(int eventFlag) {
                CustomLog.v(TAG, "onFinished");
                initLimits();
                getAppointments();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setLocale(getContext());
        parentView = inflater.inflate(R.layout.fragment_all_appointments_ui2, container, false);
        initUI();
        clickListeners();
        return parentView;
    }

    public Context setLocale(Context context) {
        SessionManager sessionManager1 = new SessionManager(context);
        String appLanguage = sessionManager1.getAppLanguage();
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        conf.setLocale(locale);
        context.createConfigurationContext(conf);
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
        return context;
    }

    private void setFiltersToTheGroup(FilterOptionsModel inputModel) {
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
        final ChipGroup chipGroup = parentView.findViewById(R.id.chipgroup_filter);
        isChipInit = true;
        chipGroup.removeAllViews();
        for (int index = 0; index < filtersList.size(); index++) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_custom_ui2, chipGroup, false);
            FilterOptionsModel filterOptionsModel = filtersList.get(index);
            final String tagName = filterOptionsModel.getFilterValue();
            String tagName1 = filterOptionsModel.getFilterValue();
            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                tagName1 = StringUtils.en_hi_dob_updated(tagName);
            int paddingDp = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 10,
                    getResources().getDisplayMetrics()
            );
            chip.setText(tagName1);
            chip.setCloseIconVisible(true);
            chip.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_selcted_chip_bg));
            chipGroup.addView(chip);
            chip.setOnCloseIconClickListener(v -> {
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
        sessionManager = new SessionManager(getContext());
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        currentDate = dateFormat1.format(new Date());

        rvUpcomingApp = parentView.findViewById(R.id.rv_all_upcoming_appointments);
        rvCancelledApp = parentView.findViewById(R.id.rv_all_cancelled_appointments);
        rvCompletedApp = parentView.findViewById(R.id.rv_all_completed_appointments);
        cardUpcomingAppointments = parentView.findViewById(R.id.card_upcoming_appointments1);
        cardCancelledAppointments = parentView.findViewById(R.id.card_cancelled_appointments1);
        cardCompletedAppointments = parentView.findViewById(R.id.card_completed_appointments1);
        layoutMainAppOptions = parentView.findViewById(R.id.layout_main_app_options1);
        layoutParent = parentView.findViewById(R.id.layout_parent_all_appointments);
        frameLayoutFilter = parentView.findViewById(R.id.filter_frame_all_appointments);
        ivFilterAllApp = parentView.findViewById(R.id.iv_filter_all_app);
        frameLayoutDateFilter = parentView.findViewById(R.id.filter_frame_date_appointments);
        tvFromDate = frameLayoutDateFilter.findViewById(R.id.tv_from_date_all_app);
        tvToDate = frameLayoutDateFilter.findViewById(R.id.tv_to_date_all_app);
        ivDateFilter = parentView.findViewById(R.id.iv_calendar_all_app);
        rgFilterAppointments = frameLayoutFilter.findViewById(R.id.rg_filter_appointments);
        rbUpcoming = frameLayoutFilter.findViewById(R.id.rb_upcoming_appointments);
        rbCancelled = frameLayoutFilter.findViewById(R.id.rb_cancelled_appointments);
        rbCompleted = frameLayoutFilter.findViewById(R.id.rb_completed_appointments);
        tvResultsFor = parentView.findViewById(R.id.tv_results_for);
        scrollChips = parentView.findViewById(R.id.scroll_chips);
        layoutUpcoming = parentView.findViewById(R.id.layout_upcoming1);
        layoutCancelled = parentView.findViewById(R.id.layout_cancelled1);
        layoutCompleted = parentView.findViewById(R.id.layout_completed1);
        layoutParentAll = parentView.findViewById(R.id.layout_parent_all1);
        tvUpcomingAppsCount = parentView.findViewById(R.id.tv_upcoming_appointments_all);
        tvCompletedAppsCount = parentView.findViewById(R.id.tv_completed_appointments_all);
        tvUpcomingAppsCountTitle = parentView.findViewById(R.id.tv_upcoming_apps_title_all);
        tvCompletedAppsCountTitle = parentView.findViewById(R.id.tv_completed_apps_title_all);
        tvCancelledAppsCount = parentView.findViewById(R.id.tv_cancelled_appointments_all);
        tvCancelledAppsCountTitle = parentView.findViewById(R.id.tv_cancelled_apps_title_all);
        autotvSearch = parentView.findViewById(R.id.et_search_all);
        ivClearText = parentView.findViewById(R.id.iv_clear_all);
        ivClearText.setOnClickListener(v -> {
            autotvSearch.setText("");
            searchPatientText = "";
            resetData();
        });
        noDataFoundForUpcoming = parentView.findViewById(R.id.layout_no_data_found_upcoming);
        noDataFoundForCompleted = parentView.findViewById(R.id.layout_no_data_found_completed);
        noDataFoundForCancelled = parentView.findViewById(R.id.layout_no_data_found_cancelled);
        if (isChipInit) {
            tvResultsFor.setVisibility(View.VISIBLE);
            scrollChips.setVisibility(View.VISIBLE);
        } else {
            tvResultsFor.setVisibility(View.GONE);
            scrollChips.setVisibility(View.GONE);
        }
        filtersList = new ArrayList<>();
        filtersListNew = new ArrayList<>();
        updateCardBackgrounds(cardUpcomingAppointments);

        nsvToday = parentView.findViewById(R.id.nsv_today);
        nsvToday.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (v.getChildAt(v.getChildCount() - 1) != null) {
                if (scrollY > oldScrollY) {

                    if (upcomingAppointmentInfoList != null && upcomingAppointmentInfoList.size() == 0) {
                        isUpcomingFullyLoaded = true;
                    }

                    if (!isUpcomingFullyLoaded) {
                        setMoreDataIntoUpcomingRecyclerView();
                    }

                    if (cancelledAppointmentInfoList != null && cancelledAppointmentInfoList.size() == 0) {
                        isCancelledFullyLoaded = true;
                    }

                    if (isUpcomingFullyLoaded && !isCancelledFullyLoaded) {
                        setMoreDataIntoCancelledRecyclerView();
                    }

                    if (scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) {
                        if (completedAppointmentInfoList != null && completedAppointmentInfoList.size() == 0) {
                            isCompletedFullyLoaded = true;
                            return;
                        }

                        if (!isCompletedFullyLoaded) {
                            setMoreDataIntoCompletedRecyclerView();
                        }
                    }
                }
            }
        });

        fragmentResultListener();
    }

    /**
     * listening result from calender dialog
     */
    private void fragmentResultListener() {
        getParentFragmentManager().setFragmentResultListener("requestKey", AllAppointmentsFragment.this, (requestKey, bundle) -> {
            String selectedDate = bundle.getString(SELECTED_DATE);
            if (selectedDate != null) {
                String whichDate = bundle.getString(WHICH_DATE);
                if (!whichDate.isEmpty() && whichDate.equals(FROM_DATE)) {
                    if (!toDate.isEmpty() && DateAndTimeUtils.isAfter(selectedDate, toDate, D_FORMAT_dd_M_yyyy)) {
                        Toast.makeText(requireContext(), R.string.the_from_date_cannot_be_greater_than_the_to_date, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    fromDate = selectedDate;
                    String dateToshow1 = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(fromDate);
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                        dateToshow1 = StringUtils.en_hi_dob_updated(DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(fromDate));
                    if (!fromDate.isEmpty()) {
                        String[] splitedDate = fromDate.split("/");
                        tvFromDate.setText(dateToshow1 + ", " + splitedDate[2]);
                    }
                    dismissDateFilterDialog();
                }

                if (!whichDate.isEmpty() && whichDate.equals(TO_DATE)) {
                    if (!fromDate.isEmpty() && DateAndTimeUtils.isBefore(selectedDate, fromDate, D_FORMAT_dd_M_yyyy)) {
                        Toast.makeText(requireContext(), R.string.the_to_date_cannot_be_less_than_the_from_date, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    toDate = selectedDate;
                    String dateToshow1 = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(toDate);
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                        dateToshow1 = StringUtils.en_hi_dob_updated(DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(toDate));
                    if (!toDate.isEmpty()) {
                        String[] splitedDate = toDate.split("/");
                        tvToDate.setText(dateToshow1 + ", " + splitedDate[2]);
                    }
                    dismissDateFilterDialog();
                }
            }
        });
    }

    private void setMoreDataIntoUpcomingRecyclerView() {
        if (upcomingSearchList.size() > 0 || cancelledSearchList.size() > 0 || completedSearchList.size() > 0) {
            return;
        }

        if (upcomingAppointmentInfoList != null && upcomingAppointmentInfoList.size() == 0) {
            isUpcomingFullyLoaded = true;
            return;
        }

        List<AppointmentInfo> tempList = new AppointmentDAO().getUpcomingAppointmentsWithFilters(fromDate, toDate, upcomingLimit, upcomingStart, currentDate);
        if (tempList.size() > 0) {
            upcomingAppointmentInfoList.addAll(tempList);
            upcomingAllAppointmentsAdapter.notifyDataSetChanged();
            upcomingStart = upcomingEnd;
            upcomingEnd += upcomingLimit;
            tvUpcomingAppsCount.setText(upcomingAppointmentInfoList.size() + "");
            tvUpcomingAppsCountTitle.setText(getResources().getString(R.string.upcoming) + " (" + upcomingAppointmentInfoList.size() + ")");
        }
    }

    private void setMoreDataIntoCancelledRecyclerView() {
        if (upcomingSearchList.size() > 0 || cancelledSearchList.size() > 0 || completedSearchList.size() > 0) {
            return;
        }

        if (cancelledAppointmentInfoList != null && cancelledAppointmentInfoList.size() == 0) {
            isCancelledFullyLoaded = true;
            return;
        }

        List<AppointmentInfo> tempList = new AppointmentDAO().getCancelledAppointmentsWithFilters(fromDate, toDate, cancelledLimit, cancelledStart, currentDate);
        if (tempList.size() > 0) {
            cancelledAppointmentInfoList.addAll(tempList);
            cancelledAllAppointmentsAdapter.notifyDataSetChanged();
            cancelledStart = cancelledEnd;
            cancelledEnd += cancelledLimit;
            tvCancelledAppsCount.setText(cancelledAppointmentInfoList.size() + "");
            tvCancelledAppsCountTitle.setText(getResources().getString(R.string.cancelled) + " (" + cancelledAppointmentInfoList.size() + ")");
        }
    }

    private void setMoreDataIntoCompletedRecyclerView() {
        if (upcomingSearchList.size() > 0 || cancelledSearchList.size() > 0 || completedSearchList.size() > 0) {
            return;
        }

        if (completedAppointmentInfoList != null && completedAppointmentInfoList.size() == 0) {
            isCompletedFullyLoaded = true;
            return;
        }

        List<AppointmentInfo> tempList = new AppointmentDAO().getCompletedAppointmentsWithFilters(fromDate, toDate, completedLimit, completedStart, currentDate);
        if (tempList.size() > 0) {
            getDataForCompletedAppointments(tempList);
            completedAppointmentInfoList.addAll(tempList);
            completedAllAppointmentsAdapter.notifyDataSetChanged();
            upcomingStart = upcomingEnd;
            upcomingEnd += upcomingLimit;
            tvCompletedAppsCount.setText(completedAppointmentInfoList.size() + "");
            tvCompletedAppsCountTitle.setText(getResources().getString(R.string.completed) + " (" + completedAppointmentInfoList.size() + ")");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void clickListeners() {
        cardUpcomingAppointments.setOnClickListener(v -> {
            updateCardBackgrounds(cardUpcomingAppointments);
            updateMAinLayoutAsPerOptionSelected("upcoming");
        });
        cardCancelledAppointments.setOnClickListener(v -> {
            updateCardBackgrounds(cardCancelledAppointments);
            updateMAinLayoutAsPerOptionSelected("cancelled");
        });
        cardCompletedAppointments.setOnClickListener(v -> {
            updateCardBackgrounds(cardCompletedAppointments);
            updateMAinLayoutAsPerOptionSelected("completed");
        });

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

        layoutParent.setOnClickListener(v -> {
            if (frameLayoutFilter.isShown())
                frameLayoutFilter.setVisibility(View.GONE);
            if (frameLayoutDateFilter.isShown())
                frameLayoutDateFilter.setVisibility(View.GONE);

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                selectDateRangeNew();
            }


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

        autotvSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String searchText = autotvSearch.getText().toString();
                if (!searchText.isEmpty()) {
                    searchPatientText = autotvSearch.getText().toString();
                    searchOperation(searchPatientText);
                } else {
                    getAppointments();
                }
                return true;
            }
            return false;
        });

        autotvSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 0) {
                    ivClearText.setVisibility(View.VISIBLE);
                } else {
                    searchPatientText = "";
                    getAppointments();
                    ivClearText.setVisibility(View.GONE);

                }
            }
        });

    }

    private void searchOperation(String query) {
        query = query.toLowerCase().trim();
        query = query.replaceAll(" {2}", " ");
        String finalQuery = query;

        new Thread(() -> {
            List<AppointmentInfo> allUpcomingList = new AppointmentDAO().getAllUpcomingAppointmentsWithFilters(fromDate, toDate);
            List<AppointmentInfo> allCancelledList = new AppointmentDAO().getAllCancelledAppointmentsWithFilters(fromDate, toDate);
            List<AppointmentInfo> allCompletedList = new AppointmentDAO().getAllCompletedAppointmentsWithFilters(fromDate, toDate);

            if (!finalQuery.isEmpty()) {
                upcomingSearchList.clear();
                cancelledSearchList.clear();
                completedSearchList.clear();

                if (allUpcomingList.size() > 0) {
                    for (AppointmentInfo info : allUpcomingList) {
                        String patientName = info.getPatientName().toLowerCase();
                        if (patientName.contains(finalQuery) || patientName.equalsIgnoreCase(finalQuery)) {
                            upcomingSearchList.add(info);
                        }
                    }
                }

                if (allCancelledList.size() > 0) {
                    for (AppointmentInfo info : allCancelledList) {
                        String patientName = info.getPatientName().toLowerCase();
                        if (patientName.contains(finalQuery) || patientName.equalsIgnoreCase(finalQuery)) {
                            cancelledSearchList.add(info);
                        }
                    }
                }

                if (allCompletedList.size() > 0) {
                    for (AppointmentInfo info : allCompletedList) {
                        String patientName = info.getPatientName().toLowerCase();
                        if (patientName.contains(finalQuery) || patientName.equalsIgnoreCase(finalQuery)) {
                            completedSearchList.add(info);
                        }
                    }
                }

                requireActivity().runOnUiThread(() -> {
                    upcomingAllAppointmentsAdapter = new AllAppointmentsAdapter(getActivity(), completedAppointmentInfoList, "upcoming");
                    rvUpcomingApp.setNestedScrollingEnabled(true);
                    rvUpcomingApp.setAdapter(upcomingAllAppointmentsAdapter);

                    cancelledAllAppointmentsAdapter = new AllAppointmentsAdapter(getActivity(), cancelledAppointmentInfoList, "cancelled");
                    rvCancelledApp.setNestedScrollingEnabled(true);
                    rvCancelledApp.setAdapter(cancelledAllAppointmentsAdapter);

                    completedAllAppointmentsAdapter = new AllAppointmentsAdapter(getActivity(), completedAppointmentInfoList, "completed");
                    rvCompletedApp.setNestedScrollingEnabled(true);
                    rvCompletedApp.setAdapter(completedAllAppointmentsAdapter);
                });
            }


        }).start();
    }


    private void updateCardBackgrounds(View view) {
        cardCancelledAppointments.setSelected(false);
        cardCompletedAppointments.setSelected(false);
        cardUpcomingAppointments.setSelected(false);
        view.setSelected(true);
        // update all 3 cards background as per selection
//        if (cardName.equals("upcoming")) {
//            cardCancelledAppointments.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_bg_options_appointment));
//            cardCompletedAppointments.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_bg_options_appointment));
//            layoutMainAppOptions.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_bg_options_appointment));
//            cardUpcomingAppointments.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_bg_selcted_card));
//        } else if (cardName.equals("cancelled")) {
//            cardUpcomingAppointments.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_bg_options_appointment));
//            cardCompletedAppointments.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_bg_options_appointment));
//
//            layoutMainAppOptions.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_bg_options_appointment));
//            cardCancelledAppointments.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_bg_selcted_card));
//        } else if (cardName.equals("completed")) {
//            cardCancelledAppointments.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_bg_options_appointment));
//            cardUpcomingAppointments.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_bg_options_appointment));
//
//            layoutMainAppOptions.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_bg_options_appointment));
//            cardCompletedAppointments.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_bg_selcted_card));
//        }

    }

    private void updateMAinLayoutAsPerOptionSelected(String cardName) {
        //adjust main layout as per option selected like figma prototype
        if (cardName.equals("upcoming")) {
            layoutUpcoming.setVisibility(View.VISIBLE);
            layoutCompleted.setVisibility(View.VISIBLE);
            layoutCancelled.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1.0f;
            params.gravity = Gravity.TOP;

            layoutUpcoming.setLayoutParams(params);
        } else if (cardName.equals("cancelled")) {
            layoutUpcoming.setVisibility(View.GONE);
            layoutCompleted.setVisibility(View.VISIBLE);
            layoutCancelled.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1.0f;
            params.gravity = Gravity.TOP;

            layoutCancelled.setLayoutParams(params);
        } else if (cardName.equals("completed")) {
            layoutCompleted.setVisibility(View.VISIBLE);
            layoutCancelled.setVisibility(View.GONE);
            layoutUpcoming.setVisibility(View.GONE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1.0f;
            params.gravity = Gravity.TOP;

            layoutCompleted.setLayoutParams(params);
        }

    }

    public void onRadioButtonClicked(View view, String selectedAppointmentOption) {
        boolean checked = ((RadioButton) view).isChecked();
        RadioButton checkedRadioButton = ((RadioButton) view);
        FilterOptionsModel filterOptionsModel = new FilterOptionsModel("appointment", selectedAppointmentOption);

        setFiltersToTheGroup(filterOptionsModel);
        switch (view.getId()) {
            case R.id.rb_upcoming_appointments:
                if (checked) {
                    whichAppointment = "upcoming";

                    rbUpcoming.setButtonDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_selected_green));
                    rbCancelled.setButtonDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_circle));
                    rbCompleted.setButtonDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_circle));
                    updateCardBackgrounds(cardUpcomingAppointments);
                    updateMAinLayoutAsPerOptionSelected("upcoming");
                }

                break;
            case R.id.rb_cancelled_appointments:
                if (checked) {
                    whichAppointment = "cancelled";

                    rbUpcoming.setButtonDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_circle));
                    rbCancelled.setButtonDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_selected_green));
                    rbCompleted.setButtonDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_circle));
                    updateCardBackgrounds(cardCancelledAppointments);
                    updateMAinLayoutAsPerOptionSelected("cancelled");

                }

                break;
            case R.id.rb_completed_appointments:
                if (checked) {
                    whichAppointment = "completed";

                    rbUpcoming.setButtonDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_circle));
                    rbCancelled.setButtonDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_circle));
                    rbCompleted.setButtonDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_selected_green));
                    updateCardBackgrounds(cardCompletedAppointments);

                    updateMAinLayoutAsPerOptionSelected("completed");

                }

                break;

        }

        if (whichAppointment.isEmpty()) {
            //call both
            getUpcomingAppointments();
            getCompletedAppointments();
            getCancelledAppointments();


        } else if (whichAppointment.equals("upcoming")) {
            //upcoming
            getUpcomingAppointments();

        } else if (whichAppointment.equals("completed")) {
            getCompletedAppointments();

        } else if (whichAppointment.equals("cancelled")) {
            getCancelledAppointments();

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
        getUpcomingAppointments();
        getCompletedAppointments();
        getCancelledAppointments();

    }

    private void getUpcomingAppointments() {
        //recyclerview for upcoming appointments
        tvUpcomingAppsCount.setText("0");
        tvUpcomingAppsCountTitle.setText(getResources().getString(R.string.completed_0));
        upcomingAppointmentInfoList = new AppointmentDAO().getUpcomingAppointmentsWithFilters(fromDate, toDate, upcomingLimit, upcomingStart, currentDate);

        if (upcomingAppointmentInfoList.size() > 0) {
            rvUpcomingApp.setVisibility(View.VISIBLE);
            noDataFoundForUpcoming.setVisibility(View.GONE);

            for (AppointmentInfo info : upcomingAppointmentInfoList) {
                String patientProfilePath = getPatientProfile(info.getPatientId());
                info.setPatientProfilePhoto(patientProfilePath);
            }

            upcomingAllAppointmentsAdapter = new AllAppointmentsAdapter(getActivity(), upcomingAppointmentInfoList, "upcoming");
            rvUpcomingApp.setAdapter(upcomingAllAppointmentsAdapter);
            upcomingStart = upcomingEnd;
            upcomingEnd += upcomingLimit;
        } else {
            rvUpcomingApp.setVisibility(View.GONE);
            noDataFoundForUpcoming.setVisibility(View.VISIBLE);
        }

        tvUpcomingAppsCount.setText(upcomingAppointmentInfoList.size() + "");
        tvUpcomingAppsCountTitle.setText(getResources().getString(R.string.upcoming) + " (" + upcomingAppointmentInfoList.size() + ")");
    }

    private void getCancelledAppointments() {
        //recyclerview for getCancelledAppointments appointments
        tvCancelledAppsCount.setText("0");
        tvCancelledAppsCountTitle.setText(getResources().getString(R.string.cancelled_0));
        cancelledAppointmentInfoList = new AppointmentDAO().getCancelledAppointmentsWithFilters(fromDate, toDate, cancelledLimit, cancelledStart, currentDate);

        if (cancelledAppointmentInfoList.size() > 0) {
            rvCancelledApp.setVisibility(View.VISIBLE);
            noDataFoundForCancelled.setVisibility(View.GONE);

            for (AppointmentInfo info : cancelledAppointmentInfoList) {
                String patientProfilePath = getPatientProfile(info.getPatientId());
                info.setPatientProfilePhoto(patientProfilePath);
            }

            cancelledAllAppointmentsAdapter = new AllAppointmentsAdapter(getActivity(), cancelledAppointmentInfoList, "cancelled");
            rvCancelledApp.setAdapter(cancelledAllAppointmentsAdapter);
            cancelledStart = cancelledEnd;
            cancelledEnd += cancelledLimit;
        } else {
            rvCancelledApp.setVisibility(View.GONE);
            noDataFoundForCancelled.setVisibility(View.VISIBLE);
        }

        tvCancelledAppsCount.setText(cancelledAppointmentInfoList.size() + "");
        tvCancelledAppsCountTitle.setText(getResources().getString(R.string.cancelled) + " (" + cancelledAppointmentInfoList.size() + ")");
    }


    private void getCompletedAppointments() {
        tvCompletedAppsCount.setText("0");
        tvCompletedAppsCountTitle.setText(getResources().getString(R.string.completed_0));
        completedAppointmentInfoList = new AppointmentDAO().getCompletedAppointmentsWithFilters(fromDate, toDate, completedLimit, completedStart, currentDate);

        if (completedAppointmentInfoList.size() > 0) {
            rvCompletedApp.setVisibility(View.VISIBLE);
            noDataFoundForCompleted.setVisibility(View.GONE);
            tvCompletedAppsCount.setText(completedAppointmentInfoList.size() + "");
            tvCompletedAppsCountTitle.setText(getResources().getString(R.string.completed) + " (" + completedAppointmentInfoList.size() + ")");
            getDataForCompletedAppointments(completedAppointmentInfoList);
            completedStart = completedEnd;
            completedEnd += completedLimit;
        } else {
            //no data found
            rvCompletedApp.setVisibility(View.GONE);
            noDataFoundForCompleted.setVisibility(View.VISIBLE);
            tvCompletedAppsCount.setText(completedAppointmentInfoList.size() + "");
            tvCompletedAppsCountTitle.setText(getResources().getString(R.string.completed) + " (" + completedAppointmentInfoList.size() + ")");
        }
    }

    private void getDataForCompletedAppointments(List<AppointmentInfo> appointmentsDaoList) {
        for (int i = 0; i < appointmentsDaoList.size(); i++) {
            VisitDTO visitDTO = isVisitPresentForPatient_fetchVisitValues(appointmentsDaoList.get(i).getPatientId());
            if (visitDTO.getUuid() != null && visitDTO.getStartdate() != null) {
                String encounteruuid = getStartVisitNoteEncounterByVisitUUID(visitDTO.getUuid());
                if (!encounteruuid.isEmpty() && !encounteruuid.equalsIgnoreCase("")) {
                    appointmentsDaoList.get(i).setPrescription_exists(true);
                } else {
                    appointmentsDaoList.get(i).setPrescription_exists(false);
                }
                String patientProfilePath = getPatientProfile(appointmentsDaoList.get(i).getPatientId());
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
            }
        }
        AllAppointmentsAdapter allAppointmentsFragment = new
                AllAppointmentsAdapter(getActivity(), appointmentsDaoList, "completed");
        rvCompletedApp.setAdapter(allAppointmentsFragment);
    }

    private String getPatientProfile(String patientUuid) {
        CustomLog.d(TAG, "getPatientProfile: patientUuid : " + patientUuid);
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
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

    private void selectDateRangeNew() {
        tvFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("whichDate", "fromdate");
                CustomCalendarViewUI2 dialog = new CustomCalendarViewUI2(getActivity());
                dialog.setArguments(args);
                getParentFragmentManager().setFragmentResult("requestKey", args);

                if (getActivity().getSupportFragmentManager() != null) {
                    dialog.show(getActivity().getSupportFragmentManager(), "AllAppointmentsFragment");
                }
            }
        });
        tvToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("whichDate", "todate");
                CustomCalendarViewUI2 dialog = new CustomCalendarViewUI2(getActivity());
                dialog.setArguments(args);
                getParentFragmentManager().setFragmentResult("requestKey", args);

                if (getActivity().getSupportFragmentManager() != null) {
                    dialog.show(getActivity().getSupportFragmentManager(), "tag");
                }
            }
        });

    }

    private void filterAsPerSelectedOptions() {
        initLimits();
        if (whichAppointment.isEmpty() && fromDate.isEmpty() && toDate.isEmpty()) {
            //all data
            getUpcomingAppointments();
            getCompletedAppointments();
            getCancelledAppointments();

        } else if (whichAppointment.isEmpty() && !fromDate.isEmpty() && !toDate.isEmpty()) {
            //all
            getUpcomingAppointments();
            getCompletedAppointments();
            getCancelledAppointments();

        } else if (whichAppointment.equals("upcoming") && !fromDate.isEmpty() && !toDate.isEmpty()) {
            //upcoming
            getUpcomingAppointments();

        } else if (whichAppointment.equals("completed") && !fromDate.isEmpty() && !toDate.isEmpty()) {
            getCompletedAppointments();

        } else if (whichAppointment.equals("cancelled") && !fromDate.isEmpty() && !toDate.isEmpty()) {
            getCancelledAppointments();
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
            }, 2000);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Bundle bundle = data.getExtras();
            String selectedDate = bundle.getString(SELECTED_DATE);
            String whichDate = bundle.getString(WHICH_DATE);
            if (!whichDate.isEmpty() && whichDate.equals(FROM_DATE)) {
                if (!toDate.isEmpty() && DateAndTimeUtils.isAfter(selectedDate, toDate, D_FORMAT_dd_M_yyyy)) {
                    Toast.makeText(requireContext(), "The 'from' date cannot be greater than the 'to' date", Toast.LENGTH_SHORT).show();
                    return;
                }
                fromDate = selectedDate;
                String dateToshow1 = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(fromDate);
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                    dateToshow1 = StringUtils.en_hi_dob_updated(DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(fromDate));
                if (!fromDate.isEmpty()) {
                    String[] splitedDate = fromDate.split("/");
                    tvFromDate.setText(dateToshow1 + ", " + splitedDate[2]);
                }
                dismissDateFilterDialog();
            }

            if (!whichDate.isEmpty() && whichDate.equals(TO_DATE)) {
                if (!fromDate.isEmpty() && DateAndTimeUtils.isBefore(selectedDate, fromDate, D_FORMAT_dd_M_yyyy)) {
                    Toast.makeText(requireContext(), "The 'to' date cannot be less than the 'from' date", Toast.LENGTH_SHORT).show();
                    return;
                }
                toDate = selectedDate;
                String dateToshow1 = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(toDate);
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                    dateToshow1 = StringUtils.en_hi_dob_updated(DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(toDate));
                if (!toDate.isEmpty()) {
                    String[] splitedDate = toDate.split("/");
                    tvToDate.setText(dateToshow1 + ", " + splitedDate[2]);
                }
                dismissDateFilterDialog();
            }
        }
    }

    public static TodaysMyAppointmentsFragment newInstance() {
        return new TodaysMyAppointmentsFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof UpdateAppointmentsCount) {
            listener = (UpdateAppointmentsCount) context;
            int totalAllApps = totalUpcomingApps + totalCancelled + totalCompleted;
            listener.updateCount("all", 2000);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentCommunicationListener");
        }
    }

    private void initLimits() {
        upcomingStart = 0;
        cancelledStart = 0;
        completedStart = 0;

        upcomingEnd = upcomingStart + upcomingLimit;
        cancelledEnd = cancelledStart + cancelledLimit;
        completedEnd = completedEnd + completedLimit;
    }

    private void resetData() {
        completedSearchList.clear();
        cancelledSearchList.clear();
        upcomingSearchList.clear();

        initLimits();
        getAppointments();
    }

}
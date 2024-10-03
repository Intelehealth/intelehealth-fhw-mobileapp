package org.intelehealth.app.appointmentNew;

import static org.intelehealth.app.database.dao.EncounterDAO.getStartVisitNoteEncounterByVisitUUID;
import static org.intelehealth.app.database.dao.PatientsDAO.isVisitPresentForPatient_fetchVisitValues;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import org.intelehealth.app.utilities.CustomLog;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.appointment.api.ApiClientAppointment;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.AppointmentInfo;
import org.intelehealth.app.appointment.model.AppointmentListingResponse;
import org.intelehealth.app.appointmentNew.MyAppointmentNew.MyAppointmentActivityNew;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.models.dto.VisitDTO;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.NavigationUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.exception.DAOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class TodaysMyAppointmentsFragment extends Fragment {
    private static final String TAG = "TodaysMyAppointmentsFra";
    View view;
    LinearLayout cardUpcomingAppointments, cardCancelledAppointments, cardCompletedAppointments, layoutMainAppOptions, layoutUpcoming, layoutCancelled, layoutCompleted;
    RecyclerView rvUpcomingApp, rvCancelledApp, rvCompletedApp;
    LinearLayout layoutParentAll;
    TextView tvUpcomingAppointments, tvUpcomingAppointmentsTitle, tvCompletedAppointments, tvCompletedAppointmentsTitle, tvCancelledAppsCount, tvCancelledAppsCountTitle;
    SessionManager sessionManager = null;
    private SQLiteDatabase db;
    ImageView ivRefresh, ivClearText;
    View noDataFoundForUpcoming, noDataFoundForCompleted, noDataFoundForCancelled;
    EditText autotvSearch;
    String searchPatientText = "";
    String currentDate = "";
    int totalUpcomingApps = 0;
    int totalCancelled = 0;
    int totalCompleted = 0;
    private UpdateAppointmentsCount listener;
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

    private TodaysMyAppointmentsAdapter todaysMyAppointmentsAdapter;
    private TodaysMyAppointmentsAdapter completedMyAppointmentsAdapter;
    private TodaysMyAppointmentsAdapter cancelledMyAppointmentsAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocale(getContext());
        ((MyAppointmentActivityNew) getActivity()).initUpdateFragmentOnEvent(0, new UpdateFragmentOnEvent() {
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
        view = inflater.inflate(R.layout.fragment_todays_appointments_ui2, container, false);
        initUI();
        clickListeners();

        return view;
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

    @Override
    public void onResume() {
        super.onResume();
        //getAppointments();
    }

    private void initUI() {
        sessionManager = new SessionManager(getActivity());
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        currentDate = dateFormat1.format(new Date());
        String language = sessionManager.getAppLanguage();

        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            requireActivity().getResources().updateConfiguration(config, getActivity().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());
        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();

        //initialize all the views
        rvUpcomingApp = view.findViewById(R.id.rv_upcoming_appointments);
        rvCancelledApp = view.findViewById(R.id.rv_cancelled_appointments);
        rvCompletedApp = view.findViewById(R.id.rv_completed_appointments);
        cardUpcomingAppointments = view.findViewById(R.id.card_upcoming_appointments);
        cardCancelledAppointments = view.findViewById(R.id.card_cancelled_appointments);
        cardCompletedAppointments = view.findViewById(R.id.card_completed_appointments);
        layoutMainAppOptions = view.findViewById(R.id.layout_main_app_options);
        layoutUpcoming = view.findViewById(R.id.layout_upcoming);
        layoutCancelled = view.findViewById(R.id.layout_cancelled);
        layoutCompleted = view.findViewById(R.id.layout_completed);
        layoutParentAll = view.findViewById(R.id.layout_parent_all);
        ivRefresh = requireActivity().findViewById(R.id.imageview_is_internet_common);

        tvUpcomingAppointments = view.findViewById(R.id.tv_upcoming_appointments_todays);
        tvUpcomingAppointmentsTitle = view.findViewById(R.id.tv_upcoming_apps_count_todays);
        tvCompletedAppointments = view.findViewById(R.id.tv_completed_appointments_todays);
        tvCompletedAppointmentsTitle = view.findViewById(R.id.tv_completed_apps_count_todays);
        tvCancelledAppsCount = view.findViewById(R.id.tv_cancelled_appointments_todays);
        tvCancelledAppsCountTitle = view.findViewById(R.id.tv_cancelled_apps_count_todays);

        //no data found
        noDataFoundForUpcoming = view.findViewById(R.id.layout_no_data_found_upcoming);
        noDataFoundForCompleted = view.findViewById(R.id.layout_no_data_found_completed);
        noDataFoundForCancelled = view.findViewById(R.id.layout_no_data_found_cancelled);

        autotvSearch = view.findViewById(R.id.et_search_today);
        ivClearText = view.findViewById(R.id.iv_clear_today);
        ivClearText.setOnClickListener(v -> {
            autotvSearch.setText("");
            searchPatientText = "";
            resetData();
        });

//        cardCancelledAppointments.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_bg_options_appointment));
//        cardCompletedAppointments.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_bg_options_appointment));
//        layoutMainAppOptions.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_bg_options_appointment));
//        cardUpcomingAppointments.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_bg_selcted_card));
        cardUpcomingAppointments.setSelected(true);
        layoutUpcoming.setVisibility(View.VISIBLE);
        layoutCompleted.setVisibility(View.VISIBLE);
        layoutCancelled.setVisibility(View.VISIBLE);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 1.0f;
        params.gravity = Gravity.TOP;

        layoutUpcoming.setLayoutParams(params);

        nsvToday = view.findViewById(R.id.nsv_today);
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

        searchPatient();
        //getSlots();


    }

    private void resetData() {
        completedSearchList.clear();
        cancelledSearchList.clear();
        upcomingSearchList.clear();

        initLimits();
        getAppointments();
    }

    private void initLimits() {
        upcomingStart = 0;
        cancelledStart = 0;
        completedStart = 0;

        upcomingEnd = upcomingStart + upcomingLimit;
        cancelledEnd = cancelledStart + cancelledLimit;
        completedEnd = completedEnd + completedLimit;
    }

    private void setMoreDataIntoUpcomingRecyclerView() {
        if (upcomingSearchList.size() > 0 || cancelledSearchList.size() > 0 || completedSearchList.size() > 0) {
            return;
        }

        if (upcomingAppointmentInfoList != null && upcomingAppointmentInfoList.size() == 0) {
            isUpcomingFullyLoaded = true;
            return;
        }

        List<AppointmentInfo> tempList = new AppointmentDAO().getUpcomingAppointmentsForToday(currentDate, upcomingLimit, upcomingStart);
        if (tempList.size() > 0) {
            upcomingAppointmentInfoList.addAll(tempList);
            todaysMyAppointmentsAdapter.notifyDataSetChanged();
            upcomingStart = upcomingEnd;
            upcomingEnd += upcomingLimit;
            tvUpcomingAppointments.setText(upcomingAppointmentInfoList.size() + "");
            tvUpcomingAppointmentsTitle.setText(getResources().getString(R.string.upcoming) + " (" + upcomingAppointmentInfoList.size() + ")");
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

        List<AppointmentInfo> tempList = new AppointmentDAO().getCancelledAppointmentsForToday(currentDate, cancelledLimit, cancelledStart);
        if (tempList.size() > 0) {
            cancelledAppointmentInfoList.addAll(tempList);
            cancelledMyAppointmentsAdapter.notifyDataSetChanged();
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

        List<AppointmentInfo> tempList = new AppointmentDAO().getCompletedAppointmentsForToday(currentDate, completedLimit, completedStart);
        if (tempList.size() > 0) {
            getDataForCompletedAppointments(tempList);
            completedAppointmentInfoList.addAll(tempList);
            completedMyAppointmentsAdapter.appointmentInfoList.addAll(tempList);
            completedMyAppointmentsAdapter.notifyDataSetChanged();
            upcomingStart = upcomingEnd;
            upcomingEnd += upcomingLimit;
            tvCompletedAppointments.setText(completedAppointmentInfoList.size() + "");
            tvCompletedAppointmentsTitle.setText(getResources().getString(R.string.completed) + " (" + completedAppointmentInfoList.size() + ")");
        }
    }

    private void searchPatient() {
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

        autotvSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String searchText = autotvSearch.getText().toString();
                    if (!searchText.isEmpty()) {
                        searchOperation(searchText);
                    } else {
                        getAppointments();
                    }
                    return true;
                }
                return false;
            }
        });

    }

    private void clickListeners() {
        cardUpcomingAppointments.setOnClickListener(v -> {
            cardCancelledAppointments.setSelected(false);
            cardCompletedAppointments.setSelected(false);
            cardUpcomingAppointments.setSelected(true);

            layoutUpcoming.setVisibility(View.VISIBLE);
            layoutCompleted.setVisibility(View.VISIBLE);
            layoutCancelled.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1.0f;
            params.gravity = Gravity.TOP;

            layoutUpcoming.setLayoutParams(params);
        });
        cardCancelledAppointments.setOnClickListener(v -> {

//            cardUpcomingAppointments.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_bg_options_appointment));
//            cardCompletedAppointments.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_bg_options_appointment));
//
//            layoutMainAppOptions.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_bg_options_appointment));
//            cardCancelledAppointments.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_bg_selcted_card));

            cardCancelledAppointments.setSelected(true);
            cardCompletedAppointments.setSelected(false);
            cardUpcomingAppointments.setSelected(false);

            layoutUpcoming.setVisibility(View.GONE);
            layoutCompleted.setVisibility(View.VISIBLE);
            layoutCancelled.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1.0f;
            params.gravity = Gravity.TOP;

            layoutCancelled.setLayoutParams(params);

        });
        cardCompletedAppointments.setOnClickListener(v -> {
//            cardCancelledAppointments.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_bg_options_appointment));
//            cardUpcomingAppointments.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_bg_options_appointment));
//
//            layoutMainAppOptions.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_bg_options_appointment));
//            cardCompletedAppointments.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_bg_selcted_card));

            cardCancelledAppointments.setSelected(false);
            cardCompletedAppointments.setSelected(true);
            cardUpcomingAppointments.setSelected(false);

            layoutCompleted.setVisibility(View.VISIBLE);
            layoutCancelled.setVisibility(View.GONE);
            layoutUpcoming.setVisibility(View.GONE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1.0f;
            params.gravity = Gravity.TOP;

            layoutCompleted.setLayoutParams(params);

        });

    }

    private void getAppointments() {
        getUpcomingAppointments();
        getCompletedAppointments();
        getCancelledAppointments();
    }

    private void getUpcomingAppointments() {
        //recyclerview for upcoming appointments
        tvUpcomingAppointments.setText("0");
        tvUpcomingAppointmentsTitle.setText(getResources().getString(R.string.upcoming_0));
        upcomingAppointmentInfoList = new AppointmentDAO().getUpcomingAppointmentsForToday(currentDate, upcomingLimit, upcomingStart);

        if (upcomingAppointmentInfoList.size() > 0) {
            rvUpcomingApp.setVisibility(View.VISIBLE);
            noDataFoundForUpcoming.setVisibility(View.GONE);
            totalUpcomingApps = upcomingAppointmentInfoList.size();

            upcomingAppointmentInfoList.forEach((appointmentInfo) -> {
                String patientProfilePath = getPatientProfile(appointmentInfo.getPatientId());
                appointmentInfo.setPatientProfilePhoto(patientProfilePath);
            });

            todaysMyAppointmentsAdapter = new TodaysMyAppointmentsAdapter(getActivity(), upcomingAppointmentInfoList, "upcoming");
            rvUpcomingApp.setAdapter(todaysMyAppointmentsAdapter);
            upcomingStart = upcomingEnd;
            upcomingEnd += upcomingLimit;
        } else {
            rvUpcomingApp.setVisibility(View.GONE);
            noDataFoundForUpcoming.setVisibility(View.VISIBLE);
        }

        tvUpcomingAppointments.setText(upcomingAppointmentInfoList.size() + "");
        tvUpcomingAppointmentsTitle.setText(getResources().getString(R.string.upcoming) + " (" + upcomingAppointmentInfoList.size() + ")");
    }

    private void getCompletedAppointments() {
        //recyclerview for completed appointments
        tvCompletedAppointments.setText("0");
        tvCompletedAppointmentsTitle.setText(getResources().getString(R.string.completed_0));
        completedAppointmentInfoList = new AppointmentDAO().getCompletedAppointmentsForToday(currentDate, completedLimit, completedStart);

        if (completedAppointmentInfoList.size() > 0) {
            rvCompletedApp.setVisibility(View.VISIBLE);
            noDataFoundForCompleted.setVisibility(View.GONE);
            getDataForCompletedAppointments(completedAppointmentInfoList);
            completedStart = completedEnd;
            completedEnd += completedLimit;
        } else {
            rvCompletedApp.setVisibility(View.GONE);
            noDataFoundForCompleted.setVisibility(View.VISIBLE);
        }
    }

    private void getDataForCompletedAppointments(List<AppointmentInfo> completedAppointmentInfoList) {
        rvCompletedApp.setVisibility(View.VISIBLE);
        noDataFoundForCompleted.setVisibility(View.GONE);
        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();

        //check if visit is present or not
        for (int i = 0; i < completedAppointmentInfoList.size(); i++) {
            VisitDTO visitDTO = isVisitPresentForPatient_fetchVisitValues(completedAppointmentInfoList.get(i).getPatientId());

            //get values from visit
            if (visitDTO.getUuid() != null && visitDTO.getStartdate() != null) {

                String encounteruuid = getStartVisitNoteEncounterByVisitUUID(visitDTO.getUuid());
                completedAppointmentInfoList.get(i).setPrescription_exists(!encounteruuid.isEmpty() && !encounteruuid.equalsIgnoreCase(""));
                String patientProfilePath = getPatientProfile(completedAppointmentInfoList.get(i).getPatientId());
                // String patientProfilePath = getPatientProfile("984af313-83c7-479e-b8a7-8e72e7384346");
                completedAppointmentInfoList.get(i).setPatientProfilePhoto(patientProfilePath);
                try {
                    String encounterId = EncounterDAO.getEncounterIdForCompletedVisit(visitDTO.getUuid());
                    String prescReceivedTime = EncounterDAO.getPrescriptionReceivedTime(encounterId);
                    CustomLog.d(TAG, "getDataForCompletedAppointments:  receivedtime : " + prescReceivedTime);

                    if (prescReceivedTime != null && !prescReceivedTime.isEmpty()) {
                        completedAppointmentInfoList.get(i).setPresc_received_time(prescReceivedTime);
                    }

                } catch (DAOException e) {
                    e.printStackTrace();
                }
            } else {

            }
        }
        //recyclerview for completed appointments
        totalCompleted = completedAppointmentInfoList.size();

        completedMyAppointmentsAdapter = new TodaysMyAppointmentsAdapter(getActivity(), completedAppointmentInfoList, "completed");
        rvCompletedApp.setAdapter(completedMyAppointmentsAdapter);
        tvCompletedAppointments.setText(completedAppointmentInfoList.size() + "");
        tvCompletedAppointmentsTitle.setText(getResources().getString(R.string.completed) + " (" + completedAppointmentInfoList.size() + ")");


    }

    private String getPatientProfile(String patientUuid) {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();

        String imagePath = "";

        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_patient where uuid = ? ", new String[]{patientUuid});

        if (idCursor.moveToFirst()) {
            do {
                imagePath = idCursor.getString(idCursor.getColumnIndexOrThrow("patient_photo"));
            } while (idCursor.moveToNext());
            idCursor.close();
        }
        return imagePath;

    }

    private void getCancelledAppointments() {
        //recyclerview for getCancelledAppointments appointments
        tvCancelledAppsCount.setText("0");
        tvCancelledAppsCountTitle.setText(getResources().getString(R.string.cancelled_0));
        cancelledAppointmentInfoList = new AppointmentDAO().getCancelledAppointmentsForToday(currentDate, cancelledLimit, cancelledStart);

        if (cancelledAppointmentInfoList.size() > 0) {
            rvCancelledApp.setVisibility(View.VISIBLE);
            noDataFoundForCancelled.setVisibility(View.GONE);
            totalCancelled = cancelledAppointmentInfoList.size();

            cancelledAppointmentInfoList.forEach(appointmentInfo -> {
                String patientProfilePath = getPatientProfile(appointmentInfo.getPatientId());
                appointmentInfo.setPatientProfilePhoto(patientProfilePath);
            });

            cancelledMyAppointmentsAdapter = new TodaysMyAppointmentsAdapter(getActivity(), cancelledAppointmentInfoList, "cancelled");
            rvCancelledApp.setAdapter(cancelledMyAppointmentsAdapter);
            cancelledStart = cancelledEnd;
            cancelledEnd += cancelledLimit;
        } else {
            rvCancelledApp.setVisibility(View.GONE);
            noDataFoundForCancelled.setVisibility(View.VISIBLE);
        }

        tvCancelledAppsCount.setText(cancelledAppointmentInfoList.size() + "");
        tvCancelledAppsCountTitle.setText(getResources().getString(R.string.cancelled) + " (" + cancelledAppointmentInfoList.size() + ")");

    }

    public static TodaysMyAppointmentsFragment newInstance() {
        return new TodaysMyAppointmentsFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof UpdateAppointmentsCount) {
            listener = (UpdateAppointmentsCount) context;
            int totalTodayApps = totalUpcomingApps + totalCancelled + totalCompleted;
            listener.updateCount("today", 1000);
        } else {
            throw new RuntimeException(context + " must implement OnFragmentCommunicationListener");
        }
    }

    private void getSlots() {
        String serverUrl = BuildConfig.SERVER_URL + ":3004";

        ApiClientAppointment.getInstance(serverUrl).getApi().getSlotsAll(DateAndTimeUtils.getCurrentDateInDDMMYYYYFormat(), DateAndTimeUtils.getCurrentDateInDDMMYYYYFormat(), new SessionManager(getActivity()).getLocationUuid()).enqueue(new Callback<AppointmentListingResponse>() {
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
                        /*if (slotInfoResponse.getCancelledAppointments() != null) {
                            if (slotInfoResponse != null && slotInfoResponse.getCancelledAppointments().size() > 0) {
                                for (int i = 0; i < slotInfoResponse.getCancelledAppointments().size(); i++) {
                                    try {
                                        appointmentDAO.insert(slotInfoResponse.getCancelledAppointments().get(i));

                                    } catch (DAOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else {
                        }*/


                getAppointments();
            }

            @Override
            public void onFailure(Call<AppointmentListingResponse> call, Throwable t) {
                CustomLog.v("onFailure", t.getMessage());
                //log out operation if response code is 401
                new NavigationUtils().logoutOperation(getActivity(), t);
            }
        });
    }

    private void searchOperation(String query) {
        query = query.toLowerCase().trim();
        query = query.replaceAll(" {2}", " ");
        String finalQuery = query;

        new Thread(() -> {
            List<AppointmentInfo> allUpcomingList = new AppointmentDAO().getAllUpcomingAppointmentsForToday(currentDate);
            List<AppointmentInfo> allCancelledList = new AppointmentDAO().getAllCancelledAppointmentsForToday(currentDate);
            List<AppointmentInfo> allCompletedList = new AppointmentDAO().getAllCompletedAppointmentsForToday(currentDate);

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
                    todaysMyAppointmentsAdapter = new TodaysMyAppointmentsAdapter(getActivity(), upcomingSearchList, "upcoming");
                    rvUpcomingApp.setNestedScrollingEnabled(true);
                    rvUpcomingApp.setAdapter(todaysMyAppointmentsAdapter);

                    cancelledMyAppointmentsAdapter = new TodaysMyAppointmentsAdapter(getActivity(), cancelledSearchList, "cancelled");
                    rvCancelledApp.setNestedScrollingEnabled(true);
                    rvCancelledApp.setAdapter(cancelledMyAppointmentsAdapter);

                    completedMyAppointmentsAdapter = new TodaysMyAppointmentsAdapter(getActivity(), completedSearchList, "completed");
                    rvCompletedApp.setNestedScrollingEnabled(true);
                    rvCompletedApp.setAdapter(completedMyAppointmentsAdapter);
                });
            }

        }).start();
    }
}

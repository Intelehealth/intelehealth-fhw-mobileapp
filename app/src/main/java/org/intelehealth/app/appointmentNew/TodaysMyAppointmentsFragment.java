package org.intelehealth.app.appointmentNew;

import static org.intelehealth.app.database.dao.EncounterDAO.getStartVisitNoteEncounterByVisitUUID;
import static org.intelehealth.app.database.dao.PatientsDAO.isVisitPresentForPatient_fetchVisitValues;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.appointment.api.ApiClientAppointment;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.AppointmentInfo;
import org.intelehealth.app.appointment.model.AppointmentListingResponse;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.models.dto.VisitDTO;
import org.intelehealth.app.utilities.DateAndTimeUtils;
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
    LinearLayout cardUpcomingAppointments, cardCancelledAppointments, cardCompletedAppointments, layoutMainAppOptions,
            layoutUpcoming, layoutCancelled, layoutCompleted;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MyAppointmentActivity) getActivity()).initUpdateFragmentOnEvent(0,new UpdateFragmentOnEvent() {
            @Override
            public void onStart(int eventFlag) {
                Log.v(TAG, "onStart");
            }

            @Override
            public void onFinished(int eventFlag) {
                Log.v(TAG, "onFinished");
                getAppointments();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_todays_appointments_ui2, container, false);
        initUI();
        clickListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getAppointments();
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
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

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
        ivRefresh = requireActivity().findViewById(R.id.ivRefreshCommonToolbar);

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
            getAppointments();
        });

        cardCancelledAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
        cardCompletedAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
        layoutMainAppOptions.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
        cardUpcomingAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_bg_selcted_card));

        layoutUpcoming.setVisibility(View.VISIBLE);
        layoutCompleted.setVisibility(View.VISIBLE);
        layoutCancelled.setVisibility(View.VISIBLE);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT);
        params.weight = 1.0f;
        params.gravity = Gravity.TOP;

        layoutUpcoming.setLayoutParams(params);

        searchPatient();
        //getSlots();


    }

    private void searchPatient() {
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

        autotvSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (!autotvSearch.getText().toString().isEmpty()) {
                        searchPatientText = autotvSearch.getText().toString();
                        getUpcomingAppointments();
                        getCompletedAppointments();
                        getCancelledAppointments();

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

    }

    private void clickListeners() {
        cardUpcomingAppointments.setOnClickListener(v -> {
            cardCancelledAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
            cardCompletedAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
            layoutMainAppOptions.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
            cardUpcomingAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_bg_selcted_card));

            layoutUpcoming.setVisibility(View.VISIBLE);
            layoutCompleted.setVisibility(View.VISIBLE);
            layoutCancelled.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT);
            params.weight = 1.0f;
            params.gravity = Gravity.TOP;

            layoutUpcoming.setLayoutParams(params);
        });
        cardCancelledAppointments.setOnClickListener(v -> {

            cardUpcomingAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
            cardCompletedAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));

            layoutMainAppOptions.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
            cardCancelledAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_bg_selcted_card));

            layoutUpcoming.setVisibility(View.GONE);
            layoutCompleted.setVisibility(View.VISIBLE);
            layoutCancelled.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT);
            params.weight = 1.0f;
            params.gravity = Gravity.TOP;

            layoutCancelled.setLayoutParams(params);

        });
        cardCompletedAppointments.setOnClickListener(v -> {
            cardCancelledAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
            cardUpcomingAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));

            layoutMainAppOptions.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
            cardCompletedAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_bg_selcted_card));

            layoutCompleted.setVisibility(View.VISIBLE);
            layoutCancelled.setVisibility(View.GONE);
            layoutUpcoming.setVisibility(View.GONE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT);
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
        List<AppointmentInfo> appointmentInfoList = new AppointmentDAO().getAppointmentsWithFiltersForToday(searchPatientText, currentDate);
        List<AppointmentInfo> upcomingAppointmentsList = new ArrayList<>();

        try {
            Log.v(TAG, "appointmentInfoList - " + appointmentInfoList.size());
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
                    String patientProfilePath = getPatientProfile(appointmentInfo.getPatientId());
                    appointmentInfo.setPatientProfilePhoto(patientProfilePath);
                    Log.v(TAG, new Gson().toJson(appointmentInfo));
                    if (appointmentInfo.getStatus()!=null && appointmentInfo.getStatus().equalsIgnoreCase("booked") && minutes >= 0) {
                        upcomingAppointmentsList.add(appointmentInfo);
                    }
                }
                totalUpcomingApps = upcomingAppointmentsList.size();

                TodaysMyAppointmentsAdapter todaysUpcomingAppointmentsAdapter = new
                        TodaysMyAppointmentsAdapter(getActivity(), upcomingAppointmentsList, "upcoming");
                rvUpcomingApp.setAdapter(todaysUpcomingAppointmentsAdapter);
            } else {
                rvUpcomingApp.setVisibility(View.GONE);
                noDataFoundForUpcoming.setVisibility(View.VISIBLE);
            }
            Log.d(TAG, "getUpcomingAppointments:upcomingAppointmentsList size :  " + upcomingAppointmentsList.size());
            tvUpcomingAppointments.setText(upcomingAppointmentsList.size() + "");
            tvUpcomingAppointmentsTitle.setText(getResources().getString(R.string.upcoming) + " (" + upcomingAppointmentsList.size() + ")");

        } catch (
                Exception e) {
            e.printStackTrace();
        }


    }

    private void getCompletedAppointments() {
        tvCompletedAppointments.setText("0");
        tvCompletedAppointmentsTitle.setText(getResources().getString(R.string.completed_0));

        //recyclerview for completed appointments
        List<AppointmentInfo> appointmentInfoList = new AppointmentDAO().getAppointmentsWithFiltersForToday(searchPatientText, currentDate);
        List<AppointmentInfo> completedAppointmentsList = new ArrayList<>();
        try {
            if (appointmentInfoList.size() > 0) {
                rvCompletedApp.setVisibility(View.VISIBLE);
                noDataFoundForCompleted.setVisibility(View.GONE);
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
                        completedAppointmentsList.add(appointmentInfo);
                    }
                }
            }
        } catch (Exception e) {

        }
        if (completedAppointmentsList.size() > 0) {

            getDataForCompletedAppointments(completedAppointmentsList);
        } else {
            rvCompletedApp.setVisibility(View.GONE);
            noDataFoundForCompleted.setVisibility(View.VISIBLE);
        }

    }

    private void getDataForCompletedAppointments(List<AppointmentInfo> appointmentsDaoList) {
        rvCompletedApp.setVisibility(View.VISIBLE);
        noDataFoundForCompleted.setVisibility(View.GONE);
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
                    Log.d(TAG, "getDataForCompletedAppointments:  receivedtime : " + prescReceivedTime);

                    if (prescReceivedTime != null && !prescReceivedTime.isEmpty()) {
                        appointmentsDaoList.get(i).setPresc_received_time(prescReceivedTime);
                    }

                } catch (DAOException e) {
                    e.printStackTrace();
                }
            } else {

            }
        }
        //recyclerview for completed appointments
        totalCompleted = appointmentsDaoList.size();

        TodaysMyAppointmentsAdapter todaysMyAppointmentsAdapter1 = new
                TodaysMyAppointmentsAdapter(getActivity(), appointmentsDaoList, "completed");
        rvCompletedApp.setAdapter(todaysMyAppointmentsAdapter1);
        tvCompletedAppointments.setText(appointmentsDaoList.size() + "");
        tvCompletedAppointmentsTitle.setText(getResources().getString(R.string.completed) + " (" + appointmentsDaoList.size() + ")");


    }

    private String getPatientProfile(String patientUuid) {
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

    private void getCancelledAppointments() {
        //recyclerview for getCancelledAppointments appointments
        tvCancelledAppsCount.setText("0");
        tvCancelledAppsCountTitle.setText(getResources().getString(R.string.cancelled_0));
        List<AppointmentInfo> appointmentInfoList = new AppointmentDAO().getCancelledAppointmentsWithFiltersForToday(searchPatientText, currentDate);
        List<AppointmentInfo> cancelledAppointmentsList = new ArrayList<>();
        try {
            if (appointmentInfoList.size() > 0) {
                rvCancelledApp.setVisibility(View.VISIBLE);
                noDataFoundForCancelled.setVisibility(View.GONE);
                for (int i = 0; i < appointmentInfoList.size(); i++) {
                    AppointmentInfo appointmentInfo = appointmentInfoList.get(i);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
                    String currentDateTime = dateFormat.format(new Date());
                    String slottime = appointmentInfo.getSlotDate() + " " + appointmentInfo.getSlotTime();

                    long diff = dateFormat.parse(slottime).getTime() - dateFormat.parse(currentDateTime).getTime();

                    long second = diff / 1000;
                    long minutes = second / 60;
                    cancelledAppointmentsList.add(appointmentInfo);
                    String patientProfilePath = getPatientProfile(appointmentInfo.getPatientId());
                    appointmentInfo.setPatientProfilePhoto(patientProfilePath);

                   /* if (minutes >= 0) {
                        cancelledAppointmentsList.add(appointmentInfo);
                    }*/
                }

                //recyclerview for cancelled appointments

                totalCancelled = cancelledAppointmentsList.size();
                TodaysMyAppointmentsAdapter todaysMyAppointmentsAdapter = new
                        TodaysMyAppointmentsAdapter(getActivity(), cancelledAppointmentsList, "cancelled");
                rvCancelledApp.setAdapter(todaysMyAppointmentsAdapter);

            } else {

                rvCancelledApp.setVisibility(View.GONE);
                noDataFoundForCancelled.setVisibility(View.VISIBLE);
            }

            tvCancelledAppsCount.setText(cancelledAppointmentsList.size() + "");
            tvCancelledAppsCountTitle.setText(getResources().getString(R.string.cancelled) + " (" + cancelledAppointmentsList.size() + ")");

        } catch (Exception e) {
            Log.d(TAG, "getCancelledAppointments: e : " + e.getLocalizedMessage());
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
            int totalTodayApps = totalUpcomingApps + totalCancelled + totalCompleted;
            listener.updateCount("today", 1000);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentCommunicationListener");
        }
    }

    private void getSlots() {
        String serverUrl = "https://" + sessionManager.getServerUrl() + ":3004";

        ApiClientAppointment.getInstance(serverUrl).getApi()
                .getSlotsAll(DateAndTimeUtils.getCurrentDateInDDMMYYYYFormat(), DateAndTimeUtils.getCurrentDateInDDMMYYYYFormat(), new SessionManager(getActivity()).getLocationUuid())
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
                        Log.v("onFailure", t.getMessage());
                    }
                });
    }

}

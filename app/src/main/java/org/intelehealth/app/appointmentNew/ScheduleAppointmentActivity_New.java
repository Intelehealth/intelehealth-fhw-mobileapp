package org.intelehealth.app.appointmentNew;

import static com.google.common.base.Preconditions.checkArgument;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.appointment.ScheduleListingActivity;
import org.intelehealth.app.appointment.adapter.SlotListingAdapter;
import org.intelehealth.app.appointment.api.ApiClientAppointment;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.AppointmentDetailsResponse;
import org.intelehealth.app.appointment.model.AppointmentListingResponse;
import org.intelehealth.app.appointment.model.BookAppointmentRequest;
import org.intelehealth.app.appointment.model.SlotInfo;
import org.intelehealth.app.appointment.model.SlotInfoResponse;
import org.intelehealth.app.horizontalcalendar.CalendarModel;
import org.intelehealth.app.horizontalcalendar.HorizontalCalendarViewAdapter;
import org.intelehealth.app.ui2.utils.CheckInternetAvailability;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.exception.DAOException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Spliterator;

import retrofit2.Call;
import retrofit2.Callback;

public class ScheduleAppointmentActivity_New extends AppCompatActivity {
    private static final String TAG = "ScheduleAppointmentActi";
    RecyclerView rvMorningSlots, rvAfternoonSlots, rvEveningSlots;
    RecyclerView rvHorizontalCal;
    int currentMonth;
    int currentYear;
    // Calendar calendar;
    ImageView ivPrevMonth, ivNextMonth;
    int monthNumber;
    String monthNAmeFromNo;
    TextView tvSelectedMonthYear;
    Calendar calendarInstance;
    String yearToCompare = "";
    String monthToCompare = "";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
    private String mSelectedStartDate = "";
    private String mSelectedEndDate = "";
    List<SlotInfo> slotInfoMorningList, slotInfoAfternoonList, slotInfoEveningList;
    Button btnBookAppointment;
    String selectedDateTime = "";
    SlotInfo slotInfoForBookApp;
    int appointmentId = 0;
    String visitUuid;
    String patientUuid;
    String patientName;
    String speciality;
    String openMrsId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_appointment_new);


        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }
        mSelectedStartDate = simpleDateFormat.format(new Date());
        mSelectedEndDate = simpleDateFormat.format(new Date());
        Log.d(TAG, "onCreate: mSelectedStartDate : " + mSelectedStartDate);
        Log.d(TAG, "onCreate: mSelectedEndDate : " + mSelectedEndDate);

        View toolbar = findViewById(R.id.toolbar_schedule_appointments);
        TextView tvTitle = toolbar.findViewById(R.id.tv_screen_title_common);
        ImageView ivIsInternet = toolbar.findViewById(R.id.imageview_is_internet_common);

        tvTitle.setText("Schedule appointment");
        if (CheckInternetAvailability.isNetworkAvailable(this)) {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));
        } else {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));

        }

   /*
  // intent params as per old flow
   appointmentId = getIntent().getIntExtra("appointmentId", 0);
        visitUuid = getIntent().getStringExtra("visitUuid");
        patientUuid = getIntent().getStringExtra("patientUuid");
        patientName = getIntent().getStringExtra("patientName");
        speciality = getIntent().getStringExtra("speciality");
        openMrsId = getIntent().getStringExtra("openMrsId");
*/

        //temporary hardcode parameters for temporary use
        visitUuid = "e57040f6-6746-4ab2-949c-0a3a343dbac2";
        patientUuid = "68617ab0-f826-4668-92dd-ab411ad6ab60";
        patientName = "Test User2";
        speciality = "General Physician";
        openMrsId = "13TR2-8";

        initUI();
    }

    private void initUI() {

        slotInfoMorningList = new ArrayList<>();
        slotInfoAfternoonList = new ArrayList<>();
        slotInfoEveningList = new ArrayList<>();
        rvMorningSlots = findViewById(R.id.rv_morning_time_slots);
        rvAfternoonSlots = findViewById(R.id.rv_afternoon_time_slots);
        rvEveningSlots = findViewById(R.id.rv_evening_time_slots);
        btnBookAppointment = findViewById(R.id.btn_book_appointment);
        btnBookAppointment.setOnClickListener(v -> {
            Log.d(TAG, "initUI: selectedDateTime : " + selectedDateTime);
            if (!selectedDateTime.isEmpty())
                bookAppointmentDialog(ScheduleAppointmentActivity_New.this, selectedDateTime);
            else
                Toast.makeText(this, "Please select time slot", Toast.LENGTH_SHORT).show();
        });

        rvMorningSlots.setHasFixedSize(true);
        rvMorningSlots.setLayoutManager(new GridLayoutManager(this, 3));

        rvAfternoonSlots.setHasFixedSize(true);
        rvAfternoonSlots.setLayoutManager(new GridLayoutManager(this, 3));

        rvEveningSlots.setHasFixedSize(true);
        rvEveningSlots.setLayoutManager(new GridLayoutManager(this, 3));

    /*    PickUpTimeSlotsAdapter pickUpTimeSlotsAdapter = new PickUpTimeSlotsAdapter(this);
        rvMorningSlots.setAdapter(pickUpTimeSlotsAdapter);
        rvAfternoonSlots.setAdapter(pickUpTimeSlotsAdapter);
        rvEveningSlots.setAdapter(pickUpTimeSlotsAdapter);*/
        rvHorizontalCal = findViewById(R.id.rv_horizontal_cal);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        rvHorizontalCal.setLayoutManager(linearLayoutManager);
        ivPrevMonth = findViewById(R.id.iv_prev_month1);
        ivNextMonth = findViewById(R.id.iv_next_month1);
        tvSelectedMonthYear = findViewById(R.id.tv_selected_month_year);

        calendarInstance = Calendar.getInstance();
        currentMonth = calendarInstance.getActualMaximum(Calendar.MONTH);
        currentYear = calendarInstance.get(Calendar.YEAR);
        monthToCompare = String.valueOf(currentMonth);
        yearToCompare = String.valueOf(currentYear);

        if (monthToCompare.equals(String.valueOf(currentMonth)) && yearToCompare.equals(String.valueOf(currentYear))) {
            enableDisablePreviousButton(false);

        } else {
            enableDisablePreviousButton(true);

        }
        getAllDatesOfSelectedMonth(calendarInstance, true, String.valueOf(currentMonth), String.valueOf(currentYear), String.valueOf(currentMonth));

        ivNextMonth.setOnClickListener(v -> {
            getNextMonthDates();
        });
        ivPrevMonth.setOnClickListener(v -> {
            getPreviousMonthDates();
        });
        getSlots();
    }


    private void getSlots() {
        Log.d(TAG, "getSlots: mSelectedStartDate : " + mSelectedStartDate);
        Log.d(TAG, "getSlots: mSelectedEndDate : " + mSelectedEndDate);

        String baseurl = "https://" + new SessionManager(this).getServerUrl() + ":3004";
        ApiClientAppointment.getInstance(baseurl).getApi()
                .getSlots(mSelectedStartDate, mSelectedEndDate, "General Physician")
                .enqueue(new Callback<SlotInfoResponse>() {
                    @Override
                    public void onResponse(Call<SlotInfoResponse> call, retrofit2.Response<SlotInfoResponse> response) {
                        SlotInfoResponse slotInfoResponse = response.body();
                        List<SlotInfo> slotInfoList = new ArrayList<>();


                        slotInfoList.addAll(slotInfoResponse.getDates());

                        for (int i = 0; i < slotInfoList.size(); i++) {
                            SlotInfo slotInfo = slotInfoList.get(i);
                            if (!slotInfo.getSlotTime().isEmpty() && slotInfo.getSlotTime().contains(" ")) {
                                String[] splitedTime = slotInfo.getSlotTime().split(" ");
                                Log.d(TAG, "onResponse:splitedTime :  " + splitedTime);
                                Log.d(TAG, "onResponse: in 1st if");
                                if (splitedTime[1].trim().equals("AM")) {
                                    Log.d(TAG, "onResponse: in am if");

                                    slotInfoMorningList.add(slotInfo);
                                }

                            }
                        }

                        for (int i = 0; i < slotInfoList.size(); i++) {
                            SlotInfo slotInfo = slotInfoList.get(i);
                            if (!slotInfo.getSlotTime().isEmpty() && slotInfo.getSlotTime().contains(" ")) {
                                String[] splitedTime = slotInfo.getSlotTime().split(" ");
                                double appointmentTime;
                                if (splitedTime[1].trim().equals("PM")) {
                                    if (splitedTime[0].contains(":")) {
                                        String time = splitedTime[0].replace(":", ".");
                                        appointmentTime = Double.parseDouble(time);

                                    } else {
                                        appointmentTime = Double.parseDouble(splitedTime[0]);
                                    }
                                    Log.d(TAG, "onResponse: appointmentTime : " + appointmentTime);
                                    if ((appointmentTime >= 1 && appointmentTime <= 6) || appointmentTime >= 12) {
                                        slotInfoAfternoonList.add(slotInfo);

                                    } else {
                                        slotInfoEveningList.add(slotInfo);

                                    }

                                }

                            }

                        }
                        Log.d(TAG, "onResponse: slotInfoMorningList : " + slotInfoMorningList.size());
                        setDataForMorningAppointments(slotInfoMorningList);
                        setDataForAfternoonAppointments(slotInfoAfternoonList);
                        setDataForEveningAppointments(slotInfoEveningList);

                    }

                    @Override
                    public void onFailure(Call<SlotInfoResponse> call, Throwable t) {
                        Log.v("onFailure", t.getMessage());
                    }
                });

    }

    private void setDataForAfternoonAppointments(List<SlotInfo> slotInfoList) {
        PickUpTimeSlotsAdapter slotListingAdapter = new PickUpTimeSlotsAdapter(
                ScheduleAppointmentActivity_New.this,
                slotInfoList, "afternoon", new PickUpTimeSlotsAdapter.OnItemSelection() {
            @Override
            public void onSelect(SlotInfo slotInfo) {
                String result = getDayOfMonthSuffix(slotInfo.getSlotDate());
                selectedDateTime = result + " at " + slotInfo.getSlotTime();

                slotInfoForBookApp = slotInfo;
                setDataForMorningAppointments(slotInfoMorningList);
                setDataForEveningAppointments(slotInfoEveningList);

                Toast.makeText(ScheduleAppointmentActivity_New.this, "Selected position afternoon : " + slotInfo.getSlotTime(), Toast.LENGTH_SHORT).show();
                //------before reschedule need to cancel appointment----
                AppointmentDAO appointmentDAO = new AppointmentDAO();
                //    appointmentDAO.deleteAppointmentByVisitId(visitUuid);
                             /*   if (appointmentId != 0) {
                                    askReason(slotInfo);
                                } else {
                                    bookAppointment(slotInfo, null);
                                }*/

            }
        });
        rvAfternoonSlots.setAdapter(slotListingAdapter);
                       /* if (slotListingAdapter.getItemCount() == 0) {
                            findViewById(R.id.llEmptyView).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.llEmptyView).setVisibility(View.GONE);
                        }*/
    }

    private void setDataForEveningAppointments(List<SlotInfo> slotInfoList) {
        PickUpTimeSlotsAdapter slotListingAdapter = new PickUpTimeSlotsAdapter(
                ScheduleAppointmentActivity_New.this,
                slotInfoList, "evening", new PickUpTimeSlotsAdapter.OnItemSelection() {
            @Override
            public void onSelect(SlotInfo slotInfo) {
                String result = getDayOfMonthSuffix(slotInfo.getSlotDate());
                selectedDateTime = result + " at " + slotInfo.getSlotTime();

                Toast.makeText(ScheduleAppointmentActivity_New.this, "Selected position  evening : " + slotInfo.getSlotTime(), Toast.LENGTH_SHORT).show();
                slotInfoForBookApp = slotInfo;

                setDataForAfternoonAppointments(slotInfoAfternoonList);
                setDataForMorningAppointments(slotInfoMorningList);
                //------before reschedule need to cancel appointment----
                AppointmentDAO appointmentDAO = new AppointmentDAO();
                //    appointmentDAO.deleteAppointmentByVisitId(visitUuid);
                             /*   if (appointmentId != 0) {
                                    askReason(slotInfo);
                                } else {
                                    bookAppointment(slotInfo, null);
                                }*/

            }
        });
        rvEveningSlots.setAdapter(slotListingAdapter);
                       /* if (slotListingAdapter.getItemCount() == 0) {
                            findViewById(R.id.llEmptyView).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.llEmptyView).setVisibility(View.GONE);
                        }*/
    }

    private void setDataForMorningAppointments(List<SlotInfo> slotInfoList) {
        PickUpTimeSlotsAdapter slotListingAdapter = new PickUpTimeSlotsAdapter(
                ScheduleAppointmentActivity_New.this,
                slotInfoList, "morning", new PickUpTimeSlotsAdapter.OnItemSelection() {
            @Override
            public void onSelect(SlotInfo slotInfo) {
                slotInfoForBookApp = slotInfo;

                Toast.makeText(ScheduleAppointmentActivity_New.this, "Selected position morning : " + slotInfo.getSlotTime(), Toast.LENGTH_SHORT).show();
                String result = getDayOfMonthSuffix(slotInfo.getSlotDate());
                selectedDateTime = result + " at " + slotInfo.getSlotTime();

                setDataForAfternoonAppointments(slotInfoAfternoonList);
                setDataForEveningAppointments(slotInfoEveningList);

                //------before reschedule need to cancel appointment----
                AppointmentDAO appointmentDAO = new AppointmentDAO();
                //    appointmentDAO.deleteAppointmentByVisitId(visitUuid);
                             /*   if (appointmentId != 0) {
                                    askReason(slotInfo);
                                } else {
                                    bookAppointment(slotInfo, null);
                                }*/

            }
        });
        rvMorningSlots.setAdapter(slotListingAdapter);
                       /* if (slotListingAdapter.getItemCount() == 0) {
                            findViewById(R.id.llEmptyView).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.llEmptyView).setVisibility(View.GONE);
                        }*/

    }

    private void getAllDatesOfSelectedMonth(Calendar calendar,
                                            boolean isCurrentMonth,
                                            String selectedMonth, String selectedYear, String selectedMonthForDays) {
        Log.d(TAG, "getAllDatesOfSelectedMonth: selectedMonth : " + selectedMonth);
        Log.d(TAG, "getAllDatesOfSelectedMonth: selectedYear : " + selectedYear);

        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int currentDay;
        if (isCurrentMonth) {
            currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            currentDay = 1;
        }
        int daysLeft = lastDay - currentDay;

        CalendarModel calendarModel;
        SimpleDateFormat inFormat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat outFormat = new SimpleDateFormat("EEEE");

        List<CalendarModel> listOfDates = new ArrayList<>();
        for (int i = currentDay; i <= lastDay; i++) {

            try {
                String inputDate = i + "-" + selectedMonthForDays + "-" + selectedYear;
                Date date = inFormat.parse(inputDate);
                if (date != null) {
                    String dayForDate = outFormat.format(date);
                    String dayForDateFinal = dayForDate.substring(0, 3);

                    if (i == currentDay) {
                        calendarModel = new CalendarModel(dayForDateFinal, i, currentDay, true, selectedMonth, selectedYear, false, selectedMonthForDays);

                    } else {
                        calendarModel = new CalendarModel(dayForDateFinal, i, currentDay, false, selectedMonth, selectedYear, false, selectedMonthForDays);

                    }

                    listOfDates.add(calendarModel);

                } else {
                }

            } catch (ParseException e) {
                Log.d(TAG, "getAllDatesOfSelectedMonth: e : " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        //  HorizontalCalendarViewAdapter horizontalCalendarViewAdapter = new HorizontalCalendarViewAdapter(this, listOfDates,this);

        rvHorizontalCal.setAdapter(new HorizontalCalendarViewAdapter(this, listOfDates, calendarModel1 -> {
            int date = calendarModel1.getDate();
            String month = calendarModel1.getSelectedMonthForDays();
            String year = calendarModel1.getSelectedYear();
            mSelectedStartDate = date + "/" + month + "/" + year;
            mSelectedEndDate = date + "/" + month + "/" + year;
            Toast.makeText(this, "Selected date : " + mSelectedStartDate, Toast.LENGTH_SHORT).show();
            getSlots();

        }));

    }

    private void getPreviousMonthDates() {
        calendarInstance.add(Calendar.MONTH, -1);
        Date monthNameNEw = calendarInstance.getTime();
        Date date = null;
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy");
        try {
            date = formatter.parse(monthNameNEw.toString());
            String formateDate = new SimpleDateFormat("dd/MM/yyyy").format(date);

            String[] dateSplit = formateDate.split("/");
            yearToCompare = dateSplit[2];
            monthToCompare = dateSplit[1];
            String[] monthYear = DateAndTimeUtils.getMonthAndYearFromGivenDate(formateDate);

            if (monthYear.length > 0) {
                String selectedPrevMonth = monthYear[0];
                String selectedPrevMonthYear = monthYear[1];
                tvSelectedMonthYear.setText(selectedPrevMonth + ", " + selectedPrevMonthYear);
                if (monthToCompare.equals(String.valueOf(currentMonth)) && yearToCompare.equals(String.valueOf(currentYear))) {
                    enableDisablePreviousButton(false);

                    getAllDatesOfSelectedMonth(calendarInstance, true, monthToCompare, selectedPrevMonthYear, monthToCompare);

                } else {
                    enableDisablePreviousButton(true);

                    getAllDatesOfSelectedMonth(calendarInstance, false, monthToCompare, selectedPrevMonthYear, monthToCompare);

                }

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void getNextMonthDates() {
        enableDisablePreviousButton(true);

        calendarInstance.add(Calendar.MONTH, 1);
        Date monthNameNEw = calendarInstance.getTime();
        Date date = null;
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy");
        try {
            date = formatter.parse(monthNameNEw.toString());
            String formateDate = new SimpleDateFormat("dd/MM/yyyy").format(date);

            String[] monthYear = DateAndTimeUtils.getMonthAndYearFromGivenDate(formateDate);
            String selectedNextMonth;
            String selectedMonthYear;

            if (monthYear.length > 0) {
                selectedNextMonth = monthYear[0];
                selectedMonthYear = monthYear[1];
                String[] dateSplit = formateDate.split("/");

                tvSelectedMonthYear.setText(selectedNextMonth + ", " + selectedMonthYear);
                if (selectedNextMonth.equals(String.valueOf(currentMonth)) && selectedMonthYear.equals(String.valueOf(currentYear))) {
                    getAllDatesOfSelectedMonth(calendarInstance, true, selectedNextMonth, selectedMonthYear, dateSplit[1]);
                } else {
                    getAllDatesOfSelectedMonth(calendarInstance, false, selectedNextMonth, selectedMonthYear, dateSplit[1]);

                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void enableDisablePreviousButton(boolean wantToEnable) {
        if (wantToEnable) {
            ivPrevMonth.setEnabled(true);
            ivPrevMonth.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);

        } else {
            ivPrevMonth.setEnabled(false);
            ivPrevMonth.setColorFilter(ContextCompat.getColor(this, R.color.font_black_3), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }


    public interface OnItemSelection {
        public void onSelect(SlotInfo slotInfo);
    }

    public void bookAppointmentDialog(Context context, String info) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_book_appointment_dialog_ui2, null);
        alertdialogBuilder.setView(convertView);
        TextView tvInfo = convertView.findViewById(R.id.tv_info_dialog_app);
        Button noButton = convertView.findViewById(R.id.button_no_appointment);
        Button yesButton = convertView.findViewById(R.id.btn_yes_appointment);

        String infoText = "Are you sure, patient want to \nbook the appointment on " + "<b>" + selectedDateTime + "?</b>";
        tvInfo.setText(Html.fromHtml(infoText));
       /* SpannableString ss = new SpannableString(text);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        ss.setSpan(boldSpan, 21, 29,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(ss);*/

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg);
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        yesButton.setOnClickListener(v -> {
          /*  Intent i_back = new Intent(context.getApplicationContext(), MyAppointmentActivity.class);
            context.startActivity(i_back);*/
            bookAppointment();
        });

        noButton.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    private void bookAppointment() {
        if (slotInfoForBookApp != null) {
            //reason - as per old flow
            BookAppointmentRequest request = new BookAppointmentRequest();
            if (appointmentId != 0) {
                request.setAppointmentId(appointmentId);
                request.setReason("reason");
            }

            request.setSlotDay(slotInfoForBookApp.getSlotDay());
            request.setSlotDate(slotInfoForBookApp.getSlotDate());
            request.setSlotDuration(slotInfoForBookApp.getSlotDuration());
            request.setSlotDurationUnit(slotInfoForBookApp.getSlotDurationUnit());
            request.setSlotTime(slotInfoForBookApp.getSlotTime());

            request.setSpeciality(slotInfoForBookApp.getSpeciality());

            request.setUserUuid(slotInfoForBookApp.getUserUuid());
            request.setDrName(slotInfoForBookApp.getDrName());
            request.setVisitUuid(visitUuid);
            request.setPatientName(patientName);
            request.setPatientId(patientUuid);
            request.setOpenMrsId(openMrsId);
            request.setLocationUuid(new SessionManager(ScheduleAppointmentActivity_New.this).getLocationUuid());
            request.setHwUUID(new SessionManager(ScheduleAppointmentActivity_New.this).getProviderID()); // user id / healthworker id

            String baseurl = "https://" + new SessionManager(this).getServerUrl() + ":3004";
            String url = baseurl + (appointmentId == 0 ? "/api/appointment/bookAppointment" : "/api/appointment/rescheduleAppointment");
            ApiClientAppointment.getInstance(baseurl).getApi()
                    .bookAppointment(url, request)
                    .enqueue(new Callback<AppointmentDetailsResponse>() {
                        @Override
                        public void onResponse(Call<AppointmentDetailsResponse> call, retrofit2.Response<AppointmentDetailsResponse> response) {
                            AppointmentDetailsResponse appointmentDetailsResponse = response.body();

                            if (appointmentDetailsResponse == null || !appointmentDetailsResponse.isStatus()) {
                                Log.d(TAG, "onResponse:Appointment book failed ");

                                Toast.makeText(ScheduleAppointmentActivity_New.this, getString(R.string.appointment_booked_failed), Toast.LENGTH_SHORT).show();
                                getSlots();
                            } else {
                                Log.d(TAG, "onResponse:Appointment booked successfully ");
                                Toast.makeText(ScheduleAppointmentActivity_New.this, getString(R.string.appointment_booked_successfully), Toast.LENGTH_SHORT).show();
                                /*setResult(RESULT_OK);
                                finish();*/
                                Intent intent = new Intent(ScheduleAppointmentActivity_New.this, MyAppointmentActivity.class);
                                startActivity(intent);
                                finish();
                            }

                        }

                        @Override
                        public void onFailure(Call<AppointmentDetailsResponse> call, Throwable t) {
                            Log.v("onFailure", t.getMessage());
                            Toast.makeText(ScheduleAppointmentActivity_New.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();

                        }
                    });

        } else {
            Log.d(TAG, "bookAppointment: model is null");
        }

    }

    String getDayOfMonthSuffix(String date) {
        String result = "";
        String[] splitedDate = new String[0];
        if (!date.isEmpty()) {
            splitedDate = date.split("/");
            int n = Integer.parseInt(splitedDate[0]);
            checkArgument(n >= 1 && n <= 31, "illegal day of month: " + n);
            if (n >= 11 && n <= 13) {
                result = "th";
            }
            switch (n % 10) {
                case 1:
                    result = "st";
                case 2:
                    result = "nd";
                case 3:
                    result = "rd";
                default:
                    result = "th";
            }
        }
        String[] resultMonth = DateAndTimeUtils.getMonthAndYearFromGivenDate(date);
        String finalDate = splitedDate[0] + result + " " + resultMonth[0];
        return finalDate;
    }

}
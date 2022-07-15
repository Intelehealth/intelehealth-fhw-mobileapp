package org.intelehealth.msfarogyabharat.activities.dailyPerformance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.intelehealth.msfarogyabharat.R;
import org.intelehealth.msfarogyabharat.app.AppConstants;
import org.intelehealth.msfarogyabharat.models.DailyPerformanceModel;
import org.intelehealth.msfarogyabharat.utilities.SessionManager;
import org.intelehealth.msfarogyabharat.utilities.UrlModifiers;
import org.intelehealth.msfarogyabharat.widget.materialprogressbar.CustomProgressDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class DailyPerformanceActivity extends AppCompatActivity {

    TextView totalCallTV, ableReachTV, unableReachTV, rescheduleCallTV, notPickUpTV, notReachTV, notValidNoTV, patientDeniedTV, patientCounselledTV, adviceProvidedTV;
    SessionManager sessionManager;
    String providerPhoneNum;
    String encoded = "", url = "", todayDate_string="";
    UrlModifiers urlModifiers = new UrlModifiers();
    int count_able_reach,count_unable_reach, count_reschedule_call, count_not_valid, count_not_pickUp, count_not_reachable, count_counselled, count_denied_counselled, count_adviced = 0;
    Context context;
    Toolbar mToolbar;
    CustomProgressDialog customProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_performance);
        setTitle(getString(R.string.daily_performance));
        mToolbar = findViewById(R.id.dailyPerformanceToolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        mToolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        initViews();
        apiCall(todayDate_string);
    }

    private void apiCall(String todayDate_string) {
        customProgressDialog.show();
        Single<DailyPerformanceModel> dailyPerformanceRequest = AppConstants.apiInterface.DAILY_PERFORMANCE(url + "/" + providerPhoneNum, "Basic " + encoded);
        dailyPerformanceRequest.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<DailyPerformanceModel>() {
                    @Override
                    public void onSuccess(DailyPerformanceModel dailyPerformanceModel) {
                        count_able_reach = 0; count_unable_reach = 0; count_reschedule_call = 0;
                        count_counselled = 0; count_denied_counselled = 0; count_adviced = 0;
                        count_not_valid = 0; count_not_pickUp = 0; count_not_reachable = 0;
                        if(dailyPerformanceModel!=null && dailyPerformanceModel.getData()!=null) {
                            for(int i=0;i<dailyPerformanceModel.getData().size();i++)
                            {
                                if(dailyPerformanceModel.getData().get(i).getDateOfCalls().equals(todayDate_string)) {
                                    if (dailyPerformanceModel.getData().get(i).getActionIfCompleted() != null && !dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals("")) {
                                        if (dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals("Reschedule the call"))
                                            count_reschedule_call = dailyPerformanceModel.getData().get(i).getCount();
                                        else if (dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals("Patient Counselled"))
                                            count_counselled = dailyPerformanceModel.getData().get(i).getCount();
                                        else if (dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals("Patient denied counselling"))
                                            count_denied_counselled = dailyPerformanceModel.getData().get(i).getCount();
                                        else if (dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals("Medical advice provided"))
                                            count_adviced = dailyPerformanceModel.getData().get(i).getCount();
                                        else if (dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals("Not a valid number"))
                                            count_not_valid = dailyPerformanceModel.getData().get(i).getCount();
                                        else if (dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals("Patient Did Not Pick Up"))
                                            count_not_pickUp = dailyPerformanceModel.getData().get(i).getCount();
                                        else if (dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals("Not reachable"))
                                            count_not_reachable = dailyPerformanceModel.getData().get(i).getCount();
                                    }
                                }
                            }

                            populateFields(count_counselled, count_denied_counselled, count_adviced, count_reschedule_call,count_not_valid,count_not_pickUp,count_not_reachable);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        customProgressDialog.dismiss();
                        System.out.println(e);
                    }
                });
    }

    private void initViews() {
        totalCallTV = findViewById(R.id.calls_made_TV);
        ableReachTV = findViewById(R.id.able_to_reach_TV);
        unableReachTV = findViewById(R.id.unable_to_reach_TV);
        rescheduleCallTV = findViewById(R.id.rescheduleCallTV);
        patientDeniedTV = findViewById(R.id.deniedCounsellingTV);
        patientCounselledTV = findViewById(R.id.patientCounselledTV);
        adviceProvidedTV = findViewById(R.id.adviceProvidedTV);
        notPickUpTV = findViewById(R.id.not_pickup_TV);
        notReachTV = findViewById(R.id.not_reach_TV);
        notValidNoTV = findViewById(R.id.not_valid_number_TV);
        context = DailyPerformanceActivity.this;
        sessionManager = new SessionManager(context);
        customProgressDialog = new CustomProgressDialog(context);
        encoded = sessionManager.getEncoded();
        url = urlModifiers.getDailyPerformanceUrl();
        if(sessionManager.getProviderPhoneno()!= null)
            providerPhoneNum = sessionManager.getProviderPhoneno();
        else
            providerPhoneNum = "9999999999";
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        Date todayDate = new Date();
        todayDate_string = currentDate.format(todayDate) + " 00:00";
        String language = sessionManager.getAppLanguage();

        //In case of crash still the app should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

    }



    private void populateFields(int count_counselled, int count_denied_counselled, int count_adviced, int count_reschedule_call,int count_not_valid,int count_not_pickUp,int count_not_reachable){
        count_able_reach = count_counselled + count_adviced + count_denied_counselled + count_reschedule_call;
        count_unable_reach = count_not_reachable + count_not_pickUp + count_not_valid;
        totalCallTV.setText(getString(R.string.total_call_made) + " " + String.valueOf(count_able_reach + count_unable_reach));
        ableReachTV.setText(getString(R.string.able_to_reach) + " " + String.valueOf(count_able_reach));
        unableReachTV.setText(getString(R.string.unable_to_reach) + " " + String.valueOf(count_unable_reach));
        notPickUpTV.setText(getString(R.string.not_picked_up) + ": " + String.valueOf(count_not_pickUp));
        rescheduleCallTV.setText(getString(R.string.reschedule_call) + ": " + String.valueOf(count_reschedule_call));
        patientCounselledTV.setText(getString(R.string.patient_counselled) + ": " + String.valueOf(count_counselled));
        patientDeniedTV.setText(getString(R.string.patient_denied_counselling) + ": " + String.valueOf(count_denied_counselled));
        adviceProvidedTV.setText(getString(R.string.medical_advice_provided) + ": " + String.valueOf(count_adviced));
        notValidNoTV.setText(getString(R.string.not_valid_number) + ": " + String.valueOf(count_not_valid));
        notReachTV.setText(getString(R.string.not_reachable) + ": " + String.valueOf(count_not_reachable));
        updateChart(count_able_reach + count_unable_reach, count_able_reach);
        customProgressDialog.dismiss();
    }

    private void updateChart(int total_calls, int calls_reachable){
        ProgressBar pieChart = findViewById(R.id.stats_progressbar);
        ProgressBar pieChart1 = findViewById(R.id.background_progressbar);
        pieChart.setVisibility(View.VISIBLE);
        pieChart1.setVisibility(View.VISIBLE);
        if(total_calls==0)
        {
            pieChart.setVisibility(View.GONE);
            pieChart1.setVisibility(View.GONE);
            return;
        }
        double d = (double) calls_reachable / (double) total_calls;
        int progress = (int) (d * 100);
        pieChart.setProgress(progress);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_sync, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_sync:
                apiCall(todayDate_string);
                return true;

            case R.id.action_calendar:
                getCalendarPicker();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getCalendarPicker() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String date_string = "";
                SimpleDateFormat todaydateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                date_string = todaydateFormat.format(calendar.getTime()) + " 00:00";
                apiCall(date_string);
            }
        }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.show();

        Button positiveButton = datePickerDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = datePickerDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));
    }

}
package org.intelehealth.ekalhelpline.activities.recordings;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.ekalhelpline.R;
import org.intelehealth.ekalhelpline.app.AppConstants;
import org.intelehealth.ekalhelpline.networkApiCalls.ApiInterface;
import org.intelehealth.ekalhelpline.utilities.NetworkConnection;
import org.intelehealth.ekalhelpline.utilities.SessionManager;
import org.intelehealth.ekalhelpline.utilities.UrlModifiers;
import org.intelehealth.ekalhelpline.widget.materialprogressbar.CustomProgressDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecordingsActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    Context context;
    SessionManager sessionManager = null;
    CustomProgressDialog customProgressDialog;
    TextView msg;
    private String TAG = RecordingsActivity.class.getSimpleName();
    String todayDate_string;

    public static void start(Context context) {
        Intent starter = new Intent(context, RecordingsActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordings);
        context = RecordingsActivity.this;
        customProgressDialog = new CustomProgressDialog(context);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        // Get the intent, verify the action and get the query
        sessionManager = new SessionManager(this);
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


        msg = findViewById(R.id.textviewmessage);
        recyclerView = findViewById(R.id.recycle);
        LinearLayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(reLayoutManager);

        SimpleDateFormat todaydateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        Calendar today = Calendar.getInstance();
        Date todayDate = today.getTime();
        todayDate_string = todaydateFormat.format(todayDate);
        getRecordings(sessionManager.getProviderPhoneno(), todayDate_string);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recyclerView.clearOnScrollListeners();
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
                getRecordings(sessionManager.getProviderPhoneno(), todayDate_string);
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
                date_string = todaydateFormat.format(calendar.getTime());
                getRecordings(sessionManager.getProviderPhoneno(), date_string);
            }
        }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.show();

        Button positiveButton = datePickerDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = datePickerDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));
    }

    private void getRecordings(String providerPhoneno, String date_string) {

        if (!NetworkConnection.isOnline(this)) {
            customProgressDialog.dismiss();
            Toast.makeText(context, R.string.no_network, Toast.LENGTH_LONG).show();
            return;
        }

        customProgressDialog.show();
        UrlModifiers urlModifiers = new UrlModifiers();
        ApiInterface apiInterface = AppConstants.apiInterface;
        String encoded = "Basic bnVyc2UxOk51cnNlMTIz";
        apiInterface.getRecordings(urlModifiers.getRecordingListUrl(providerPhoneno), encoded).enqueue(new Callback<RecordingResponse>() {
            @Override
            public void onResponse(Call<RecordingResponse> call, Response<RecordingResponse> response) {
                if (response.body() != null && response.body().data != null && response.body().data.size() > 0) {
                    List<Recording> recordingList = new ArrayList<>();
                    for (Recording recording : response.body().data) {
                        if (!TextUtils.isEmpty(recording.RecordingURL)) {
                            String recording_date = recording.time.substring(0,10);
                            if(!TextUtils.isEmpty(recording.time) && recording_date.equals(date_string))
                                recordingList.add(recording);
                        }
                    }
                    if (recordingList.size() > 0) {
                        customProgressDialog.dismiss();
                        msg.setVisibility(View.GONE);
                    } else {
                        customProgressDialog.dismiss();
                        msg.setVisibility(View.VISIBLE);
                        msg.setText(R.string.no_records_found);
                    }
                    recyclerView.setAdapter(new RecordingsAdapter(recordingList, RecordingsActivity.this));
                } else {
                    customProgressDialog.dismiss();
                    msg.setVisibility(View.VISIBLE);
                    msg.setText(R.string.no_records_found);
                }
            }

            @Override
            public void onFailure(Call<RecordingResponse> call, Throwable t) {
                System.out.println(t);
            }
        });

    }


}





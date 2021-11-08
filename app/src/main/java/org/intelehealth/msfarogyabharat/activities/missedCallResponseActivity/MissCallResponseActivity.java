package org.intelehealth.msfarogyabharat.activities.missedCallResponseActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.msfarogyabharat.R;
import org.intelehealth.msfarogyabharat.app.AppConstants;
import org.intelehealth.msfarogyabharat.app.IntelehealthApplication;
import org.intelehealth.msfarogyabharat.networkApiCalls.ApiInterface;
import org.intelehealth.msfarogyabharat.utilities.Logger;
import org.intelehealth.msfarogyabharat.utilities.NetworkConnection;
import org.intelehealth.msfarogyabharat.utilities.SessionManager;
import org.intelehealth.msfarogyabharat.utilities.UrlModifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MissCallResponseActivity extends AppCompatActivity {
        RecyclerView recyclerView;
        SessionManager sessionManager = null;
        TextView msg;
        private String TAG = MissCallResponseActivity.class.getSimpleName();

        public static void start(Context context) {
            Intent starter = new Intent(context, MissCallResponseActivity.class);
            context.startActivity(starter);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_recordings);
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

            UrlModifiers urlModifiers = new UrlModifiers();
            ApiInterface apiInterface = AppConstants.apiInterface;

            String encoded = "Basic bnVyc2UxOk51cnNlMTIz";
            apiInterface.getRecordings(urlModifiers.getRecordingListUrl()).enqueue(new Callback<RecordingResponse>() {
                @Override
                public void onResponse(Call<RecordingResponse> call, Response<RecordingResponse> response) {
                    if (response.body() != null && response.body().data != null && response.body().data.size() > 0) {
                        List<Recording> recordingList = new ArrayList<>();
                        for (Recording recording : response.body().data) {
                            if (!TextUtils.isEmpty(recording.RecordingURL)) {
                                recordingList.add(recording);

                            }
                        }
                        if (recordingList.size() > 0) {
                            msg.setVisibility(View.GONE);
                        } else {
                            //All followups done
                            msg.setText(R.string.no_records_found);
                        }

                        recyclerView.setAdapter(new RecordingsAdapter(recordingList, new RecordingsAdapter.OnClickingItemListner() {
                            @Override
                            public void mCallAgain(int pos) {
//todo
                                updatetheCaller(recordingList.get(pos).Caller);
                            }
                        },MissCallResponseActivity.this));
                    } else {
                        msg.setText(R.string.no_records_found);
                    }
                }

                @Override
                public void onFailure(Call<RecordingResponse> call, Throwable t) {
                    System.out.println(t);
                }
            });
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            recyclerView.clearOnScrollListeners();
        }
    private void updatetheCaller(String phoneNumber) {

        UpdateRecordingCallerBodyModel   updateRecordingCallerBody = new UpdateRecordingCallerBodyModel();
        updateRecordingCallerBody.setCallid(phoneNumber);

        if (!NetworkConnection.isOnline(this)) {
            Toast.makeText(this, R.string.no_network, Toast.LENGTH_SHORT).show();
            return;
        }
        UrlModifiers urlModifiers = new UrlModifiers();
        String url = urlModifiers.getUpdateRecording();

        Call<UpdatedCallerResponce> UpdateCallerCall = AppConstants.apiInterface.getUpdateRecording(url,updateRecordingCallerBody);

        UpdateCallerCall.enqueue(new Callback<UpdatedCallerResponce>() {
            @Override
            public void onResponse(Call<UpdatedCallerResponce> call, Response<UpdatedCallerResponce> response) {
//                Log.v("main", "hash: "+hash);
                showAlert(R.string.calling_patient);
            }
            @Override
            public void onFailure(Call<UpdatedCallerResponce> call, Throwable t) {
                t.printStackTrace();
                Log.v("main", "failure: " + t.getLocalizedMessage());
            }
        });
    }

    void showAlert(int messageRes) {
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        alertDialogBuilder.setMessage(messageRes);
        alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

//    public void callPatientViaIVR(String caller) {
//        if (!NetworkConnection.isOnline(this)) {
//            Toast.makeText(this, R.string.no_network, Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        String receiver = caller;
//        if (TextUtils.isEmpty(receiver))
//            return;
//        UrlModifiers urlModifiers = new UrlModifiers();
//        String caller = sessionManager.getProviderPhoneno(); //fetches the provider mobile no who has logged in the app...
//        String url = urlModifiers.getIvrCallUrl(caller, receiver);
//        Logger.logD(TAG, "ivr call url" + url);
//        Single<String> patientIvrCall = AppConstants.ivrApiInterface.CALL_PATIENT_IVR(url);
//        patientIvrCall.subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new DisposableSingleObserver<String>() {
//                    @Override
//                    public void onSuccess(@NonNull String s) {
//                       showAlert (R.string.calling_patient);
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        showAlert(R.string.error_calling_patient);
//                    }
//                });
//    }

}
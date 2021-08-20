package org.intelehealth.ekalhelpline.activities.ivrCallResponseActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.intelehealth.ekalhelpline.R;
import org.intelehealth.ekalhelpline.models.IVR_Call_Models.CallTo_Status_GetterSetter;
import org.intelehealth.ekalhelpline.models.IVR_Call_Models.Call_Details_Response;
import org.intelehealth.ekalhelpline.networkApiCalls.ApiClient;
import org.intelehealth.ekalhelpline.networkApiCalls.ApiInterface;
import org.intelehealth.ekalhelpline.utilities.Logger;
import org.intelehealth.ekalhelpline.utilities.NetworkConnection;
import org.intelehealth.ekalhelpline.utilities.SessionManager;
import org.intelehealth.ekalhelpline.utilities.UrlModifiers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class IVRCallResponseActivity extends AppCompatActivity {
    Context context;
    SessionManager sessionManager;
    RecyclerView recyclerView;
    IVRCallResponse_Adapter adapter;
    List<CallTo_Status_GetterSetter> call_list;
    Call_Details_Response response;
    TextView total_count_textview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ivrcall_response);
        context = IVRCallResponseActivity.this;
        sessionManager = new SessionManager(context);
        call_list = new ArrayList<>();

        Log.v("main", "provider_no: "+ sessionManager.getProviderPhoneno());

        recyclerView = findViewById(R.id.ivr_response_recyclerview);
        total_count_textview = findViewById(R.id.total_count_textview);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));

        getIVR_Call_Response(sessionManager.getProviderPhoneno());
    }

    private void getIVR_Call_Response(String providerNo) {
        if (!NetworkConnection.isOnline(this)) {
            Toast.makeText(context, R.string.no_network, Toast.LENGTH_SHORT).show();
            return;
        }

        response = new Call_Details_Response();
        ApiClient.changeApiBaseUrl("https://api-voice.kaleyra.com");
        UrlModifiers urlModifiers = new UrlModifiers();

        SimpleDateFormat todaydateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        Calendar today = Calendar.getInstance();
        Date todayDate = today.getTime();
        String todayDate_string = todaydateFormat.format(todayDate);

        String url = urlModifiers.getIvrCall_ResponseUrl(providerNo, "2021/08/19");
        Logger.logD("main", "ivr call response url" + url);
        Observable<Call_Details_Response> patientIvrCall_response =
                ApiClient.createService(ApiInterface.class).IVR_CALL_RESPONSE(url);
        patientIvrCall_response
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Call_Details_Response>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Call_Details_Response call_details_response) {
                        response = call_details_response;
                        total_count_textview.setText("Total Calls: " + call_details_response.getData().size());
                        adapter = new IVRCallResponse_Adapter(context, response);
                        if(response.getData() != null) {
                            recyclerView.setAdapter(adapter);
                        }
                        else {
                            Toast.makeText(context, "Something Went Wrong. Refresh again", Toast.LENGTH_SHORT).show();
                        }

                        Log.v("main", "call_ivr_response: "+ call_details_response);

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v("main", "call_ivr_response_error: "+ e.getLocalizedMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.v("main", "call_ivr_response_onComplete(): ");
                    }
                });

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
            case R.id.action_sync:
                getIVR_Call_Response(sessionManager.getProviderPhoneno());

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
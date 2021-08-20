package org.intelehealth.ekalhelpline.activities.ivrCallResponseActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.intelehealth.ekalhelpline.R;
import org.intelehealth.ekalhelpline.models.IVR_Call_Models.Call_Details_Response;
import org.intelehealth.ekalhelpline.networkApiCalls.ApiClient;
import org.intelehealth.ekalhelpline.networkApiCalls.ApiInterface;
import org.intelehealth.ekalhelpline.utilities.Logger;
import org.intelehealth.ekalhelpline.utilities.NetworkConnection;
import org.intelehealth.ekalhelpline.utilities.SessionManager;
import org.intelehealth.ekalhelpline.utilities.UrlModifiers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class IVRCallResponseActivity extends AppCompatActivity {
    Context context;
    SessionManager sessionManager;
    String provider_no;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ivrcall_response);
        context = IVRCallResponseActivity.this;
        sessionManager = new SessionManager(context);

        provider_no = sessionManager.getProviderPhoneno();
        getIVR_Call_Response(provider_no);

    }

    private void getIVR_Call_Response(String providerNo) {
        if (!NetworkConnection.isOnline(this)) {
            Toast.makeText(context, R.string.no_network, Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient.changeApiBaseUrl("https://api-voice.kaleyra.com");
        UrlModifiers urlModifiers = new UrlModifiers();

        SimpleDateFormat todaydateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        Calendar today = Calendar.getInstance();
        Date todayDate = today.getTime();
        String todayDate_string = todaydateFormat.format(todayDate);

        String url = urlModifiers.getIvrCall_ResponseUrl(providerNo, todayDate_string);
        Logger.logD("main", "ivr call response url" + url);
        Observable<Call_Details_Response> patientIvrCall_response = ApiClient.createService(ApiInterface.class).IVR_CALL_RESPONSE(url);
        patientIvrCall_response
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Call_Details_Response>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Call_Details_Response call_details_response) {
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
                getIVR_Call_Response(provider_no);

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
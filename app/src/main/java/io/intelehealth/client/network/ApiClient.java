package io.intelehealth.client.network;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import io.intelehealth.client.activities.setting_activity.SettingsActivity;
import io.intelehealth.client.application.IntelehealthApplication;
import io.intelehealth.client.utilities.SessionManager;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit = null;
    private static OkHttpClient.Builder client = new OkHttpClient.Builder();
    static  String Baseurl=null;

    public static Retrofit getApiClient() {
        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(IntelehealthApplication.getAppContext());
//        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
//        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//        client.addInterceptor(loggingInterceptor);


//        issue #676 checking and adding base url from the shared prefernces
        if (sessionManager.getBaseUrl() != null) {
            Baseurl=sessionManager.getBaseUrl();
        }else{
            Baseurl = sharedPref.getString(SettingsActivity.KEY_PREF_SERVER_URL_REST, "");
        }

        if (retrofit == null) {
//convert the static to dynamic code
            retrofit = new Retrofit.Builder()
                    .baseUrl(Baseurl)
                    .client(client.build())
                    //AndroidSchedulers.mainThread()
                    .addConverterFactory(GsonConverterFactory.create())
//                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

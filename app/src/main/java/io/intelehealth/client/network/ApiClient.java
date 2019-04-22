package io.intelehealth.client.network;


import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.utilities.SessionManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    static OkHttpClient.Builder client = new OkHttpClient.Builder();
    private static Retrofit retrofit = null;

    public static Retrofit getApiClient() {
        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client.addInterceptor(loggingInterceptor);
        if (retrofit == null) {

            retrofit = new Retrofit.Builder()
                    .baseUrl(sessionManager.getBaseUrl())
                    .client(client.build())
                    .addConverterFactory(GsonConverterFactory.create())
//                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return retrofit;
    }

}

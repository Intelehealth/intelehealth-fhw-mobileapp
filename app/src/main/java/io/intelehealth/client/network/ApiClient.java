package io.intelehealth.client.network;


import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient {
    //    SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
    static String apiBaseUrl = "http://openmrs.intelehealth.io";

    private static OkHttpClient.Builder client = new OkHttpClient.Builder();
    private static Retrofit retrofit = null;

    public static Retrofit getApiClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client.addInterceptor(loggingInterceptor);
        if (retrofit == null) {

            retrofit = new Retrofit.Builder()
                    .baseUrl(apiBaseUrl)
                    .client(client.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static void changeApiBaseUrl(String newApiBaseUrl) {
        apiBaseUrl = newApiBaseUrl;
    }

}

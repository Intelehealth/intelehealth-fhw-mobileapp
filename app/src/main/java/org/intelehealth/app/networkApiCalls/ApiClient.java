package org.intelehealth.app.networkApiCalls;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.utilities.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient {
    //
    private static final OkHttpClient.Builder client = new OkHttpClient.Builder();
    private static String apiBaseUrl = SessionManager.getInstance(IntelehealthApplication.getAppContext()).getServerUrl();    //testing server
    private static final Gson gson = new GsonBuilder()
            .setLenient()
            .create();


    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(normalizeBaseUrl(apiBaseUrl))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create());


    public static void changeApiBaseUrl(String newApiBaseUrl) {
        apiBaseUrl = newApiBaseUrl;
        builder = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(normalizeBaseUrl(apiBaseUrl));

    }

    public static <S> S createService(Class<S> serviceClass) {

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client.addInterceptor(loggingInterceptor);
        client.connectTimeout(60, TimeUnit.SECONDS);
        client.readTimeout(60, TimeUnit.SECONDS);
        client.writeTimeout(60, TimeUnit.SECONDS);
        Retrofit retrofit = builder.client(client.build()).build();
        return retrofit.create(serviceClass);
    }


    /**
     * added this to handle crashes in case of invalid url
     * AEAT-2097
     * @param url
     * @return
     */
    private static String normalizeBaseUrl(String url) {
        if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }
}
package org.intelehealth.app.networkApiCalls;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.networkApiCalls.interceptors.TokenSetupInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient {
    private static String apiBaseUrl = BuildConfig.SERVER_URL;    //testing server
    private static final Gson gson = new GsonBuilder()
            .setLenient()
            .create();

    /**
     * Built once and shared by every service.
     *
     * The builder used to be a static field that createService added two interceptors to on each call,
     * so the chain grew by two for every service ever created and each request was logged — and passed
     * through the token interceptor — once per accumulated copy. Building here also keeps the app to a
     * single connection pool and dispatcher rather than one per service, which is what a local builder
     * would have produced instead.
     *
     * TEMPORARY (2026-08-03): body logging to inspect whether the pull response carries
     * abha_number / abha_address in patientlist. Bodies contain patient PII — revert to NONE once
     * confirmed. Gated on DEBUG so a release build can never emit it.
     */
    private static final OkHttpClient httpClient = buildHttpClient();

    private static OkHttpClient buildHttpClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(
                BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(new TokenSetupInterceptor())
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }



    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(apiBaseUrl)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create());


    public static void changeApiBaseUrl(String newApiBaseUrl) {
        apiBaseUrl = newApiBaseUrl;
        builder = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(apiBaseUrl);

    }

    public static <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit = builder.client(httpClient).build();
        return retrofit.create(serviceClass);
    }

}
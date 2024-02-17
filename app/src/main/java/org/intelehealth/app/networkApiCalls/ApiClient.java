package org.intelehealth.app.networkApiCalls;


import org.intelehealth.app.networkApiCalls.interceptors.LogoutInterceptor;
import org.intelehealth.app.networkApiCalls.interceptors.TokenSetupInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient {
    //
    private static OkHttpClient.Builder client = new OkHttpClient.Builder();
    private static String apiBaseUrl = "https://intelehealth.org";    //testing server
    private static Retrofit retrofit;
    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(apiBaseUrl)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create());


    public static void changeApiBaseUrl(String newApiBaseUrl) {
        apiBaseUrl = newApiBaseUrl;
        builder = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(apiBaseUrl);

    }

    public static <S> S createService(Class<S> serviceClass) {

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client.addInterceptor(loggingInterceptor);
        client.addInterceptor(new TokenSetupInterceptor());
        client.addInterceptor(new LogoutInterceptor());
        client.connectTimeout(60, TimeUnit.SECONDS);
        client.readTimeout(60, TimeUnit.SECONDS);
        client.writeTimeout(60, TimeUnit.SECONDS);
        retrofit = builder.client(client.build()).build();
        return retrofit.create(serviceClass);
    }

}
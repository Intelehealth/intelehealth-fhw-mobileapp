package org.intelehealth.ekalarogya.networkApiCalls;


import android.content.Context;
import android.widget.Toast;

import org.intelehealth.ekalarogya.BuildConfig;
import org.intelehealth.ekalarogya.R;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import java.net.UnknownHostException;


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


    public static void changeApiBaseUrl(String newApiBaseUrl, Context context){
        try {
            apiBaseUrl = newApiBaseUrl;
            builder = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .baseUrl(apiBaseUrl);
        }catch (IllegalArgumentException e){
            Toast.makeText(context, context.getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
        }

    }

    public static <S> S createService(Class<S> serviceClass) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        client.addInterceptor(loggingInterceptor);
        client.connectTimeout(600, TimeUnit.SECONDS);
        client.readTimeout(600, TimeUnit.SECONDS);
        client.writeTimeout(600, TimeUnit.SECONDS);
        retrofit = builder.client(client.build()).build();
        return retrofit.create(serviceClass);
    }

}
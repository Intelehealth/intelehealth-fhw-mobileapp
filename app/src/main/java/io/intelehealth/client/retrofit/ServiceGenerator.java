package io.intelehealth.client.retrofit;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import io.intelehealth.client.BuildConfig;
import io.intelehealth.client.IntelehealthApplication;
import io.intelehealth.client.SettingsActivity;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by dexterbarretto on 06/05/16.
 */
public class ServiceGenerator {

    public static String apiBaseUrl = "http://openmrs.intelehealth.io";
    private static Retrofit retrofit;

    private static HttpLoggingInterceptor logging = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
    static Gson gson = new GsonBuilder()
            .setDateFormat("MM-dd-yyyy'T'HH:mm:ss")
            .create();


    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(apiBaseUrl);

    private static OkHttpClient.Builder httpClient;

    static {



        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(logging);
        } else {
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS);
        }

    }


    // No need to instantiate this class.
    private ServiceGenerator() {
    }

    public static void changeApiBaseUrl(String newApiBaseUrl) {
        apiBaseUrl = newApiBaseUrl;

        builder = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(apiBaseUrl);
    }

    public static <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit = builder.client(httpClient.build()).build();
        return retrofit.create(serviceClass);
    }



}

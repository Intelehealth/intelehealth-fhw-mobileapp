package app.intelehealth.client.networkApiCalls;


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
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        client.addInterceptor(loggingInterceptor);
        client.connectTimeout(70, TimeUnit.SECONDS);
        client.readTimeout(70, TimeUnit.SECONDS);
        client.writeTimeout(70, TimeUnit.SECONDS);
        retrofit = builder.client(client.build()).build();
        return retrofit.create(serviceClass);
    }

}
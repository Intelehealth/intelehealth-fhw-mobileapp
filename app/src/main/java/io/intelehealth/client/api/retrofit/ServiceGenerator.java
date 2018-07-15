package io.intelehealth.client.api.retrofit;

import java.util.concurrent.TimeUnit;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Sets up api call requests.
 *
 * @see <a href="http://square.github.io/retrofit/">http://square.github.io/retrofit</a>
 * <p>
 * Created by dexterbarretto on 06/05/16.
 */
public class ServiceGenerator {

    public static String apiBaseUrl = "http://openmrs.intelehealth.io";
    private static Retrofit retrofit;


    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(apiBaseUrl);

    private static OkHttpClient.Builder httpClient;

    static {


        /**
         * If debug flavour {@link HttpLoggingInterceptor} is used.
         */
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(2, TimeUnit.MINUTES);
        } else {
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(2, TimeUnit.MINUTES);
        }

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(1);
        dispatcher.setMaxRequests(4);

        httpClient = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS);

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

package io.intelehealth.client.api.retrofit;

import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.intelehealth.client.BuildConfig;
import io.intelehealth.client.R;
import io.intelehealth.client.activities.setup_activity.LocationArrayAdapter;
import io.intelehealth.client.activities.setup_activity.SetupActivity;
import io.intelehealth.client.models.Location;
import io.intelehealth.client.models.Results;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS);
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

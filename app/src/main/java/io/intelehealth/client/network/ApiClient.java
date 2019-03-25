package io.intelehealth.client.network;


import io.intelehealth.client.application.IntelehealthApplication;
import io.intelehealth.client.utilities.SessionManager;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit = null;
    private static OkHttpClient.Builder client = new OkHttpClient.Builder();

    public static Retrofit getApiClient() {
        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
//        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
//        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//        client.addInterceptor(loggingInterceptor);
        if (sessionManager.getBaseUrl() != null) {
//           Issue #682.
//           retrofit2.Utils.checkNotNull
            if (retrofit == null) {
//convert the static to dynamic code
                retrofit = new Retrofit.Builder()
                        .baseUrl(sessionManager.getBaseUrl())
                        .client(client.build())
                        //AndroidSchedulers.mainThread()
                        .addConverterFactory(GsonConverterFactory.create())
//                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .build();
            }

        }
        return retrofit;
    }
}

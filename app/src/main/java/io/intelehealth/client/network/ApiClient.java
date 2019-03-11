package io.intelehealth.client.network;


import io.intelehealth.client.application.IntelehealthApplication;
import io.intelehealth.client.utilities.SessionManager;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit = null;
    static OkHttpClient.Builder client = new OkHttpClient.Builder();
static SessionManager sessionManager;
    public static Retrofit getApiClient() {
        sessionManager=new SessionManager(IntelehealthApplication.getAppContext());
//        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
//        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//        client.addInterceptor(loggingInterceptor);
        if (retrofit == null) {
//convert the static to dynamic code
            retrofit = new Retrofit.Builder()
                    .baseUrl(sessionManager.getBaseUrl())
                    .client(client.build())
                    .addConverterFactory(GsonConverterFactory.create())
//                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

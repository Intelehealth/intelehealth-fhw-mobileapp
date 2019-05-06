package io.intelehealth.client.network;


import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.utilities.SessionManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient {
    //
    private static OkHttpClient.Builder client = new OkHttpClient.Builder();
    private static String apiBaseUrl = "http://openmrs.intelehealth.io";
    private static Retrofit retrofit;
    SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(apiBaseUrl)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create());

//    public static Retrofit getApiClient() {
//
//        if (retrofit == null) {
//
//            retrofit = new Retrofit.Builder()
//                    .baseUrl(apiBaseUrl)
//                    .client(client.build())
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                    .build();
//        }
//        return retrofit;
//    }

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
        retrofit = builder.client(client.build()).build();
        return retrofit.create(serviceClass);
    }

}
//public static String apiBaseUrl = "http://openmrs.intelehealth.io";
//    private static Retrofit retrofit;
//
//
//    private static Retrofit.Builder builder =
//            new Retrofit.Builder()
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                    .baseUrl(apiBaseUrl);
//
//    private static OkHttpClient.Builder httpClient;
//
//    static {
//
//
//        /**
//         * If debug flavour {@link HttpLoggingInterceptor} is used.
//         */
//        //Increase the timed out
//        if (BuildConfig.BUILD_TYPE.equals("debug")) {
//            httpClient = new OkHttpClient.Builder()
//                    .connectTimeout(2, TimeUnit.MINUTES)
//                    .readTimeout(3, TimeUnit.MINUTES);
//        } else {
//            httpClient = new OkHttpClient.Builder()
//                    .connectTimeout(2, TimeUnit.MINUTES)
//                    .readTimeout(3, TimeUnit.MINUTES);
//        }
//
//        Dispatcher dispatcher = new Dispatcher();
//        dispatcher.setMaxRequestsPerHost(1);
//        dispatcher.setMaxRequests(4);
//
//        httpClient = new OkHttpClient.Builder()
//                .dispatcher(dispatcher)
//                .connectTimeout(1, TimeUnit.MINUTES)
//                .readTimeout(2, TimeUnit.MINUTES);
//
//    }
//
//
//    // No need to instantiate this class.
//    private ApiClient() {
//    }
//
//    public static void changeApiBaseUrl(String newApiBaseUrl) {
//        apiBaseUrl = newApiBaseUrl;
//
//        builder = new Retrofit.Builder()
//                .addConverterFactory(GsonConverterFactory.create())
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .baseUrl(apiBaseUrl);
//    }
//
//
//    public static <S> S createService(Class<S> serviceClass) {
//        Retrofit retrofit = builder.client(httpClient.build()).build();
//        return retrofit.create(serviceClass);
//    }
//
//
//}


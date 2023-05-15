package org.intelehealth.unicef.appointment.api;


import org.intelehealth.unicef.BuildConfig;
import org.intelehealth.unicef.networkApiCalls.ApiClient;

import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClientAppointment {
    private static ApiClientAppointment instance = null;
    private static OkHttpClient.Builder client = new OkHttpClient.Builder();
    private Api mApi;

    private ApiClientAppointment(String baseUrl) {
        getUnsafeOkHttpClient();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        client.addInterceptor(loggingInterceptor);
        client.connectTimeout(45, TimeUnit.SECONDS);
        client.readTimeout(45, TimeUnit.SECONDS);
        client.writeTimeout(45, TimeUnit.SECONDS);

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.client(client.build()).build();
        mApi = retrofit.create(Api.class);
    }

    public static synchronized ApiClientAppointment getInstance(String baseUrl) {
        if (instance == null) {
            instance = new ApiClientAppointment(baseUrl);
        }
        return instance;
    }


    public Api getApi() {
        return mApi;
    }

    public static OkHttpClient.Builder getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            client.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            client.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            return client;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

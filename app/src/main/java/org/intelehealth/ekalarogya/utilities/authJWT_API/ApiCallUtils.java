package org.intelehealth.ekalarogya.utilities.authJWT_API;

import android.content.Context;
import android.util.Log;

import org.intelehealth.ekalarogya.app.AppConstants;
import org.intelehealth.ekalarogya.utilities.SessionManager;
import org.intelehealth.ekalarogya.utilities.UrlModifiers;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class ApiCallUtils {
    public static final String TAG = ApiCallUtils.class.getName();

    public static void auth_login_jwt_token(Context context, String BASE_URL, String username, String password) {

        UrlModifiers urlModifiers = new UrlModifiers();
        String url = urlModifiers.auth_jwt_url(BASE_URL);

        AuthJWTBody authJWTBody = new AuthJWTBody();
        authJWTBody.setUsername(username);
        authJWTBody.setPassword(password);
        authJWTBody.setRememberme(true);

        Observable<AuthJWTResponse> observable = AppConstants.apiInterface
                .AUTH_LOGIN_JWT_API(url, authJWTBody);

        observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<AuthJWTResponse>() {
                    @Override
                    public void onNext(AuthJWTResponse authJWTResponse) {
                        // store the token provided in response in sessionmanager as a string...
                        Log.v(TAG, "onNext: " + authJWTResponse.toString());
                        String token = authJWTResponse.getToken();
                        SessionManager sessionManager = new SessionManager(context);
                        sessionManager.setJwtAuthToken(token);
                        Log.v(TAG, "token: " + sessionManager.getJwtAuthToken());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v(TAG, "erroor: " + e.toString());

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}

package io.intelehealth.client.firebase_cloud_messaging;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Dexter Barretto on 5/25/17.
 * Github : @dbarretto
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService{

    private static final String TAG = MyFirebaseInstanceIdService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {

        //Getting registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        //Displaying token on logcat
        Log.d(TAG, "Refreshed token: " + refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        //TODO: Implement this method to store the token on your server

    }
}

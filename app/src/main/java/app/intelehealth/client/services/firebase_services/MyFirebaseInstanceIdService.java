package app.intelehealth.client.services.firebase_services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import androidx.annotation.NonNull;


/**
 * Created by Dexter Barretto on 5/25/17.
 * Github : @dbarretto
 */

public class MyFirebaseInstanceIdService implements InstanceIdResult {

    private static final String TAG = MyFirebaseInstanceIdService.class.getSimpleName();

    @NonNull
    @Override
    public String getId() {
        return null;
    }

    @NonNull
    @Override
    public String getToken() {
        //Getting registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        //Displaying token on logcat
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        return null;
    }
}
package io.intelehealth.client.services;

import io.intelehealth.client.objects.OpenMRSAccountAuthenticator;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by tusharjois on 9/5/16.
 */
public class OpenMRSAccountAuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return new OpenMRSAccountAuthenticator(this).getIBinder();
    }
}

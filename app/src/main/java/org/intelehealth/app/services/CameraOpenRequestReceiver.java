package org.intelehealth.app.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.intelehealth.app.utilities.CustomLog;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.ihutils.ui.CameraActivity;

import java.util.UUID;

public class CameraOpenRequestReceiver extends BroadcastReceiver {
    private static final String TAG = CameraOpenRequestReceiver.class.getSimpleName();

    public void onReceive(Context context, Intent intent) {
        CustomLog.v(TAG, "onReceive");
        Intent cameraIntent = new Intent(context, CameraActivity.class);
        String imageName = UUID.randomUUID().toString();
        cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
        cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, AppConstants.IMAGE_PATH);
        cameraIntent.putExtra(CameraActivity.SEND_BROADCAST_AFTER_CAPTURE, true);
        context.startActivity(cameraIntent);
    }
}
package org.intelehealth.app.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class PermissionManager {

    /**
     * # Include Permissions #
     * 1.Manifest.permission.BLUETOOTH.
     * 2.Manifest.permission.BLUETOOTH_ADMIN.
     * 3.Manifest.permission.ACCESS_FINE_LOCATION
     */
    public static final int REQUEST_CODE_ALL_PERMISSIONS = 311;

    public static final int requestCode_location = 411;

    public static final int REQUEST_CODE_GET_BLUETOOTH_LIST = 412;

    public static final int REQUEST_CODE_CAMERA = 511;

    public static final String PERMISSION_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

    public static boolean isObtain(Activity activity, String permission, int requestCode) {
        if (isAboveAndroidOS6_0()) {
            int checkPermission = ActivityCompat.checkSelfPermission(activity, permission);
            if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public static boolean isObtain(Fragment fragment, String permission, int requestCode) {
        if (isAboveAndroidOS6_0()) {
            final Context context = fragment.getContext();
            if (context == null) return false;
            int checkPermission = ActivityCompat.checkSelfPermission(context, permission);
            if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                fragment.requestPermissions(new String[]{permission}, requestCode);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public static boolean isObtain(Activity activity, String[] permissions, int requestCode) {
        if (isAboveAndroidOS6_0()) {
            ArrayList<String> permissionsList = new ArrayList<>();
            for (String permission : permissions) {
                int checkPermission = ActivityCompat.checkSelfPermission(activity, permission);
                if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                    permissionsList.add(permission);
                }
            }
            int newPermissionListSize = permissionsList.size();
            if (newPermissionListSize == 0) {
                return true;
            } else {
                if (newPermissionListSize == permissions.length) {
                    ActivityCompat.requestPermissions(activity, permissions, requestCode);
                } else {
                    String[] newPermissions = permissionsList.toArray(new String[newPermissionListSize]);
                    ActivityCompat.requestPermissions(activity, newPermissions, requestCode);
                }
                return false;
            }
        }
        return true;
    }

    public static boolean isPermissionGranted(@NonNull final Activity activity, @NonNull final String permission, final String dialogMsg, final int requestCode) {
        if (isAboveAndroidOS6_0()) {
            int hasWritePermission = ActivityCompat.checkSelfPermission(activity, permission);
            if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    showMessageOKCancel(activity, dialogMsg, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", activity.getApplication().getPackageName(), null);
                                intent.setData(uri);
                                activity.startActivityForResult(intent, 101);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    return false;
                }
                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
                return false;
            }
        }
        return true;
    }

    private static void showMessageOKCancel(Activity activity, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton("手动授予", okListener)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }

    public static boolean isPermissionGranted(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    public static String[] getUseBluetoothPermissions() {
        return new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
    }

    public static String[] getAllPermissions() {
        return new String[]{
//                //By location (for bluetooth scan)
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                //By camera(QR code scan)
                Manifest.permission.CAMERA,
                //By phone
                Manifest.permission.READ_PHONE_STATE,
                //By read contact
                Manifest.permission.READ_CONTACTS,
                //By SD-Card
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                //By microphone
                Manifest.permission.RECORD_AUDIO

        };
    }

    public static boolean canScanBluetoothDevice(Context context) {
        //系统SDK 大于 23  需要系统定位开关才能搜索蓝牙
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
            return manager != null && (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                            ? manager.isLocationEnabled()
                            : manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            );
        }
        return true;
    }


    public static void openGPS(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            activity.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            // The Android SDK doc says that the location settings activity
            // may not be found. In that case show the general settings.
            // General settings activity
            intent.setAction(Settings.ACTION_SETTINGS);
            try {
                activity.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(activity, "Can not find the GPS setting page.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static boolean isAboveAndroidOS6_0() {
//        if (targetSdkVersion == 0)
//            targetSdkVersion = context.getApplicationInfo().targetSdkVersion;
//        return targetSdkVersion >= 23;
        return Build.VERSION.SDK_INT >= 23;
    }

    public static String[] getBluetoothPermissions() {
        return new String[]{
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
        };
    }
}

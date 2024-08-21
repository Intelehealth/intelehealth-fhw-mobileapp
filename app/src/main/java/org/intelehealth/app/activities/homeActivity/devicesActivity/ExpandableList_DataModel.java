package org.intelehealth.app.activities.homeActivity.devicesActivity;

import android.content.Context;

import com.smartcaredoc.app.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Prajwal Waingankar
 * on February 2023.
 * Github: prajwalmw
 */
public class ExpandableList_DataModel {
    private static Context context;

    public static HashMap<String, List<String>> getData(DevicesActivity devicesActivity) {
        context = devicesActivity;
        HashMap<String, List<String>> hashMap = new HashMap<String, List<String>>();

        List<String> healthcubeDevice = new ArrayList<String>();
        healthcubeDevice.add(context.getString(R.string.rhemos_device_info));
        healthcubeDevice.add(context.getString(R.string.rhemos_bloog_glucose_calibration));
        hashMap.put(context.getString(R.string.rhemos_device), healthcubeDevice);

//        List<String> thermalPrinterDevice = new ArrayList<String>();
//        thermalPrinterDevice.add(context.getString(R.string.thermalprinter_device_info));
//        hashMap.put(context.getString(R.string.thermal_printer), thermalPrinterDevice);

        return hashMap;
    }
}

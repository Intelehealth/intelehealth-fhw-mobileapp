package org.intelehealth.app.activities.homeActivity.devicesActivity;

import android.content.Context;

import org.intelehealth.app.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableList_DataModel {
    private static Context context;

    public static HashMap<String, List<String>> getData(DevicesActivity devicesActivity) {
        context = devicesActivity;
        HashMap<String, List<String>> hashMap = new HashMap<String, List<String>>();

        List<String> healthcubeDevice = new ArrayList<String>();
        healthcubeDevice.add("Rhemos Device Info");
        healthcubeDevice.add("Blood Glucose Calibration");
        hashMap.put("Rhemos", healthcubeDevice);

//        List<String> thermalPrinterDevice = new ArrayList<String>();
//        thermalPrinterDevice.add(context.getString(R.string.thermalprinter_device_info));
//        hashMap.put(context.getString(R.string.thermal_printer), thermalPrinterDevice);

        return hashMap;
    }
}

package org.intelehealth.app.activities.devicesActivity;

import android.content.Context;

import org.intelehealth.app.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
* Created by Prajwal Maruti Waingankar on 24-06-2022, 13:01
* Copyright (c) 2021 . All rights reserved.
* Email: prajwalwaingankar@gmail.com
* Github: prajwalmw
*/

public class ExpandableList_DataModel {
    private static Context context;

    public static HashMap<String, List<String>> getData(DevicesActivity devicesActivity) {
        context = devicesActivity;
        HashMap<String, List<String>> hashMap = new HashMap<String, List<String>>();

        List<String> healthcubeDevice = new ArrayList<String>();
        healthcubeDevice.add(context.getString(R.string.healthcube_device_info));
        healthcubeDevice.add(context.getString(R.string.blood_glucose_calibration));
        healthcubeDevice.add(context.getString(R.string.hemoglobin_calibration));
        healthcubeDevice.add(context.getString(R.string.uric_acid_calibration));
        healthcubeDevice.add(context.getString(R.string.total_cholesterol_calibration));
        hashMap.put(context.getString(R.string.healthcube), healthcubeDevice);

        List<String> thermalPrinterDevice = new ArrayList<String>();
        thermalPrinterDevice.add(context.getString(R.string.thermalprinter_device_info));
        hashMap.put(context.getString(R.string.thermal_printer), thermalPrinterDevice);

        return hashMap;
    }
}

package org.intelehealth.app.activities.devicesActivity;

import static com.healthcubed.ezdxlib.model.TestName.BLOOD_GLUCOSE;
import static com.healthcubed.ezdxlib.model.TestName.BLOOD_GLUCOSE_CALIBRATION;
import static com.healthcubed.ezdxlib.model.TestName.BLOOD_PRESSURE;
import static com.healthcubed.ezdxlib.model.TestName.CHOLESTEROL_CALIBRATION;
import static com.healthcubed.ezdxlib.model.TestName.HEMOGLOBIN;
import static com.healthcubed.ezdxlib.model.TestName.HEMOGLOBIN_CALIBRATION;
import static com.healthcubed.ezdxlib.model.TestName.PULSE_OXIMETER;
import static com.healthcubed.ezdxlib.model.TestName.URIC_ACID_CALIBRATION;

import static org.intelehealth.app.app.AppConstants.key;
import static org.intelehealth.app.utilities.DialogUtils.showInfoDialog;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.healthcubed.ezdxlib.bluetoothHandler.BluetoothService;
import com.healthcubed.ezdxlib.bluetoothHandler.BluetoothStatus;
import com.healthcubed.ezdxlib.bluetoothHandler.EzdxBT;
import com.healthcubed.ezdxlib.model.EzdxData;
import com.healthcubed.ezdxlib.model.HCDeviceData;
import com.healthcubed.ezdxlib.model.Status;
import com.healthcubed.ezdxlib.model.TestName;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.vitalActivity.VitalsActivity;
import org.intelehealth.app.app.IntelehealthApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Prajwal Maruti Waingankar on 24-06-2022, 12:36
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

public class DevicesActivity extends AppCompatActivity implements BluetoothService.OnBluetoothEventCallback {
    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    HashMap<String, List<String>> expandableListDetail;
    AppCompatImageView imageView;
    TextView textView;
    AlertDialog alertDialog;
    Context context;
    BluetoothService bluetoothService;
    HCDeviceData hcDeviceData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        setTitle(getString(R.string.devices));
        context = DevicesActivity.this;
        EzdxBT.authenticate(key); // Authenticate Key before starting the test.
        bluetoothService = BluetoothService.getDefaultInstance();
        bluetoothService.setOnEventCallback(this);
        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        expandableListDetail = ExpandableList_DataModel.getData(this);
        expandableListTitle = new ArrayList<String>(expandableListDetail.keySet());
        expandableListAdapter = new CustomExpandableListAdapter
                (this, expandableListTitle, expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);


        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView expandableListView,
                                        View view, int groupPosition, int childPosition, long id) {

                // Health cube info
                if (expandableListDetail.get(expandableListTitle.get(groupPosition)).get(childPosition)
                        .equalsIgnoreCase(getString(R.string.healthcube_device_info))) {

                    if (hcDeviceData != null) {
                        try {
                            showInfoDialog(DevicesActivity.this, hcDeviceData.toString(), getString(R.string.healthcube_device_info));
                        } catch (Exception e) {
                        }
                    }
                }

                // Blood Glucose Calibration
                if (expandableListDetail.get(expandableListTitle.get(groupPosition)).get(childPosition)
                        .equalsIgnoreCase(getString(R.string.blood_glucose_calibration))) {
                    Status status = EzdxBT.startBloodGlucoseCalibration();
                    Log.v("Details", "Deta: " + status.toString());
                    if (status.equals(Status.CALIBRATION_NOT_REQUIRED)) {
                        Toast.makeText(context, getString(R.string.calibration_not_required), Toast.LENGTH_SHORT).show();
                    } else if (status.equals(Status.BLUETOOTH_NOT_CONNECTED)) {
                        Toast.makeText(context, getString(R.string.connect_bluetooth), Toast.LENGTH_SHORT).show();
                    } else {
                        showTestDialog();
                    }

                }

                // Blood Glucose Calibration
                if (expandableListDetail.get(expandableListTitle.get(groupPosition)).get(childPosition)
                        .equalsIgnoreCase(getString(R.string.hemoglobin_calibration))) {
                    Status status = EzdxBT.startHemoglobinCalibration();
                    if (status.equals(Status.BLUETOOTH_NOT_CONNECTED)) {
                        Toast.makeText(context, getString(R.string.connect_bluetooth), Toast.LENGTH_SHORT).show();
                    } else {
                        showTestDialog();
                    }
                }

                // Uric Acid Calibration
                if (expandableListDetail.get(expandableListTitle.get(groupPosition)).get(childPosition)
                        .equalsIgnoreCase(getString(R.string.uric_acid_calibration))) {
                    Status status = EzdxBT.startUricAcidCalibration();
                    if (status.equals(Status.BLUETOOTH_NOT_CONNECTED)) {
                        Toast.makeText(context, getString(R.string.connect_bluetooth), Toast.LENGTH_SHORT).show();
                    } else {
                        showTestDialog();
                    }
                }

                // Total Cholesterol Calibration
                if (expandableListDetail.get(expandableListTitle.get(groupPosition)).get(childPosition)
                        .equalsIgnoreCase(getString(R.string.total_cholesterol_calibration))) {
                    Status status = EzdxBT.startCholestrolCalibration();
                    if (status.equals(Status.BLUETOOTH_NOT_CONNECTED)) {
                        Toast.makeText(context, getString(R.string.connect_bluetooth), Toast.LENGTH_SHORT).show();
                    } else {
                        showTestDialog();
                    }
                }

                return false;
            }
        });
    }

    private void showTestDialog() {
        // show dialog
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(context);
        View layoutInflater = LayoutInflater.from(context)
                .inflate(R.layout.welcome_slide1, null);
        imageView = layoutInflater.findViewById(R.id.instructionImage);
        imageView.setImageDrawable(getDrawable(R.drawable.hemoglobin_calibration));
        textView = layoutInflater.findViewById(R.id.tv_intro_one);
        textView.setTextColor(getColor(R.color.colorPrimaryDark));
        dialog.setView(layoutInflater);

        dialog.setNegativeButton(R.string.STOP, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                EzdxBT.stopCurrentTest(); // stopping the test is necessary...
                Toast.makeText(context, getString(R.string.test_stopped), Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog = dialog.create();
        alertDialog.show();

        Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        pb.setTextColor(getResources().getColor((R.color.colorPrimary)));
        pb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

    private void fetchStatusOfTest(EzdxData ezdxData, TestName testName) {
        if (testName.equals(BLOOD_GLUCOSE_CALIBRATION))
            imageView.setImageDrawable(getDrawable(R.drawable.glucose_calibration));

        else if (testName.equals(HEMOGLOBIN_CALIBRATION) ||
                testName.equals(URIC_ACID_CALIBRATION) ||
                testName.equals(CHOLESTEROL_CALIBRATION)) {
            imageView.setImageDrawable(getDrawable(R.drawable.hemoglobin_calibration));
        }

        // Status reading...
        if (ezdxData.getStatus().equals(Status.STARTED)) {
            if (alertDialog != null) {
                textView.setText(R.string.calibration_started_successfully);
            }
        }
        if (ezdxData.getStatus().equals(Status.INITIALIZING)) {
            if (alertDialog != null) {
                textView.setText(R.string.initializing);
            }
        }
        if (ezdxData.getStatus().equals(Status.INSERT_CALIBRATION_STRIP)) {
            if (alertDialog != null) {
                textView.setText(R.string.insert_calibration_strip);
                textView.setTextColor(getColor(R.color.red3));
            }
        }
        if (ezdxData.getStatus().equals(Status.STOPPED)) {
            Toast.makeText(this, R.string.calibration_stopped_successfully, Toast.LENGTH_SHORT).show();
            textView.setTextColor(getColor(R.color.red3));
        }

        if (ezdxData.getStatus().equals(Status.CALIBRATION_COMPLETED)) {
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
            Toast.makeText(this, R.string.calibration_completed_successfully, Toast.LENGTH_SHORT).show();
            EzdxBT.stopCurrentTest();
                    /*Once the test gives the ‘TEST_COMPLETED’ state, fetch the result from the object and call the
                     ‘stopCurrentTest()’ method to stop the test.
                     Otherwise callback will be called every second until stop is called.*/
        }
    }


    @Override
    public void onEzdxData(EzdxData ezdxData) {
        switch (ezdxData.getTestName()) {
            case BLOOD_GLUCOSE_CALIBRATION: { // <a href="https://www.flaticon.com/free-icons/calibration" title="calibration icons">Calibration icons created by Muhammad Ali - Flaticon</a>
                fetchStatusOfTest(ezdxData, BLOOD_GLUCOSE_CALIBRATION);
                break;
            }
            case HEMOGLOBIN_CALIBRATION: { // <a href="https://www.flaticon.com/free-icons/calibration" title="calibration icons">Calibration icons created by Iconic Panda - Flaticon</a>
                fetchStatusOfTest(ezdxData, HEMOGLOBIN_CALIBRATION); // Diabetes
                break;
            }
            default:

        }
    }

    @Override
    public void onHCDeviceInfo(HCDeviceData hcDeviceData) {
        if (hcDeviceData != null) {
            this.hcDeviceData = hcDeviceData;
        }
    }

    @Override
    public void onStatusChange(BluetoothStatus bluetoothStatus) {

    }
}
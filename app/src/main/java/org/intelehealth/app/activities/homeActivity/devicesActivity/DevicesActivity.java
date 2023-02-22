package org.intelehealth.app.activities.homeActivity.devicesActivity;


import static org.intelehealth.app.utilities.DialogUtils.showInfoDialog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import org.intelehealth.app.R;
import org.intelehealth.app.models.rhemos_device.DeviceInfoModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DevicesActivity extends AppCompatActivity {
    Context context;
    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    HashMap<String, List<String>> expandableListDetail;
    private DeviceInfoModel infoModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        setTitle(getString(R.string.devices));
        context = DevicesActivity.this;

        Intent intent = getIntent();
        infoModel = (DeviceInfoModel) intent.getSerializableExtra("device_info");

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
                        .equalsIgnoreCase(getString(R.string.rhemos_device_info))) {

                    try {
                        showInfoDialog(DevicesActivity.this, infoModel.toString(), getString(R.string.rhemos_device_info));
                    } catch (Exception e) {
                    }

                   /* if (hcDeviceData != null) {
                        try {
                            showInfoDialog(DevicesActivity.this, hcDeviceData.toString(), getString(R.string.healthcube_device_info));
                        } catch (Exception e) {
                        }
                    }*/
                }

                // Blood Glucose Calibration
                if (expandableListDetail.get(expandableListTitle.get(groupPosition)).get(childPosition)
                        .equalsIgnoreCase(getString(R.string.rhemos_bloog_glucose_calibration))) {
                   /* Status status = EzdxBT.startBloodGlucoseCalibration();
                    Log.v("Details", "Deta: " + status.toString());
                    if (status.equals(Status.CALIBRATION_NOT_REQUIRED)) {
                        Toast.makeText(context, getString(R.string.calibration_not_required), Toast.LENGTH_SHORT).show();
                    } else if (status.equals(Status.BLUETOOTH_NOT_CONNECTED)) {
                        Toast.makeText(context, getString(R.string.connect_bluetooth), Toast.LENGTH_SHORT).show();
                    } else {
                        showTestDialog();
                    }*/

                }

                return false;
            }
        });
    }

}
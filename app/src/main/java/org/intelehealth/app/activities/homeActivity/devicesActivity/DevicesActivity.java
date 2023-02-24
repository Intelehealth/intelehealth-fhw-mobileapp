package org.intelehealth.app.activities.homeActivity.devicesActivity;


import static org.intelehealth.app.utilities.DialogUtils.showInfoDialog;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.linktop.MonitorDataTransmissionManager;
import com.linktop.constant.TestPaper;
import com.linktop.whealthService.MeasureType;

import org.intelehealth.app.R;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.rhemos_device.DeviceInfoModel;
import org.intelehealth.app.utilities.SessionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DevicesActivity extends AppCompatActivity {
    Context context;
    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    HashMap<String, List<String>> expandableListDetail;
    private DeviceInfoModel infoModel;
    private AppCompatSpinner spin_test_paper_manufacturer, spin_test_paper_code;
    private String mManufacturer;
    private String mPaperCode;
    private String[] mTestPaperCodes;
    private SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        setTitle(getString(R.string.devices));
        context = DevicesActivity.this;
        sessionManager = new SessionManager(context);

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
                }

                // Blood Glucose Calibration
                if (expandableListDetail.get(expandableListTitle.get(groupPosition)).get(childPosition)
                        .equalsIgnoreCase(getString(R.string.rhemos_bloog_glucose_calibration))) {

                    showCalibrationDialog();

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

    private void showCalibrationDialog() {
        // show dialog
        Dialog dialog = new Dialog(context);
        View layoutInflater = LayoutInflater.from(context).inflate(R.layout.bg_calibration_layout, null);
        spin_test_paper_manufacturer = layoutInflater.findViewById(R.id.spin_test_paper_manufacturer);
        spin_test_paper_code = layoutInflater.findViewById(R.id.spin_test_paper_code);
        TextView cancel_txt = layoutInflater.findViewById(R.id.cancel_txt);
        TextView submit_txt = layoutInflater.findViewById(R.id.submit_txt);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(layoutInflater);

        String[] manufacturers = TestPaper.Manufacturer.values();
        ArrayAdapter<String> adapterManufacturer = new ArrayAdapter<>(context
                , android.R.layout.simple_spinner_dropdown_item
                , getManufacturerList(manufacturers));
        spin_test_paper_manufacturer.setAdapter(adapterManufacturer);
        spin_test_paper_manufacturer.setSelection(TestPaper.Manufacturer.indexOf(TestPaper.Manufacturer.YI_CHENG));
        spin_test_paper_manufacturer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mManufacturer = manufacturers[position];
                mTestPaperCodes = TestPaper.Code.values(mManufacturer, getTestPaperMeasureType());
                ArrayAdapter<String> adapterTestPaper = new ArrayAdapter<>(context
                        , android.R.layout.simple_spinner_dropdown_item
                        , Arrays.asList(mTestPaperCodes));
                spin_test_paper_code.setAdapter(adapterTestPaper);

                if (TestPaper.Manufacturer.YI_CHENG.equals(mManufacturer)) {
                    //Default value select TestPaperCode.C20.
                    spin_test_paper_code.setSelection(TestPaper.Code.indexOf(mTestPaperCodes
                            , TestPaper.Code.C21));
                }
              //  oldUnit.set(TestPaper.Manufacturer.IQUEGO.equals(mManufacturer));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spin_test_paper_code.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPaperCode = mTestPaperCodes[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        dialog.setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                /** On clicking submit, store these spinner values in SessionManager to be fetched in Vitals screen.
//                 * In case of no value selection at inital app setup than in Vitals add that logic if empty than set default
//                 * strip value which you have.
//                 */
//                sessionManager.setTestManufacturer(mManufacturer);
//                sessionManager.setTestPaperCode(mPaperCode);
//                Log.v("BG_Calibrate", "BG_Calibrate: " + sessionManager.getTestManufacturer() + " : " + sessionManager.getTestPaperCode());
//            }
//        });

      /*  dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
//                EzdxBT.stopCurrentTest(); // stopping the test is necessary...
//                Toast.makeText(context, getString(R.string.test_stopped), Toast.LENGTH_SHORT).show();
            }
        });*/


      //  alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        cancel_txt.setOnClickListener(v -> {
            dialog.dismiss();
        });

        submit_txt.setOnClickListener(v -> {
            /** On clicking submit, store these spinner values in SessionManager to be fetched in Vitals screen.
             * In case of no value selection at inital app setup than in Vitals add that logic if empty than set default
             * strip value which you have.
             */
            sessionManager.setTestManufacturer(mManufacturer);
            sessionManager.setTestPaperCode(mPaperCode);
            Log.v("BG_Calibrate", "BG_Calibrate: " + sessionManager.getTestManufacturer() + " : " + sessionManager.getTestPaperCode());
            dialog.dismiss();
            Toast.makeText(context, "Blood Glucose calibration successful!", Toast.LENGTH_SHORT).show();
        });

     /*   Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        pb.setTextColor(getResources().getColor((R.color.colorPrimary)));
        pb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        Button nb = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        nb.setTextColor(getResources().getColor((R.color.colorPrimary)));
        nb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);*/

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
      //  IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

    private List<String> getManufacturerList(String[] array) {
        List<String> list = new ArrayList<>();
        for (String name : array) {
            switch (name) {
                case TestPaper.Manufacturer.YI_CHENG:
                    list.add(getString(R.string.manufacturer_yi_cheng));
                    break;
                case TestPaper.Manufacturer.IQUEGO:
                    list.add(getString(R.string.manufacturer_iquego));
                    break;
                case TestPaper.Manufacturer.BENE_CHECK:
                    list.add(getString(R.string.manufacturer_bene_check));
                    break;
            }
        }
        return list;
    }

    protected int getTestPaperMeasureType() {
        return MeasureType.BG;
    }

}
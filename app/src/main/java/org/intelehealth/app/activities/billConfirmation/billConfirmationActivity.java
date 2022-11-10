package org.intelehealth.app.activities.billConfirmation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.checkerframework.checker.units.qual.A;
import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeActivity;
import org.intelehealth.app.activities.identificationActivity.IdentificationActivity;
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.ConceptAttributeListDAO;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class billConfirmationActivity extends AppCompatActivity implements PaymentResultListener {

    Toolbar toolbar;
    String patientName, patientVillage, patientOpenID, patientHideVisitID, patientPhoneNum, visitType, patientVisitID, billType;
    ArrayList<String> selectedTests = new ArrayList<>();
    TextView patientDetailsTV, paymentStatusTV;
    String patientDetails;
    String receiptNum = "XXXXX";
    String billDateString = "DD MM YYYY";
    CardView consultCV, followUPCV, glucoseFCV, glucoseRCV, glucoseNFCV, glucosePPNCV, haemoglobinCV, cholesterolCV, bpCV, uricAcidCV, totalAmountCV, confirmBillCV,
            printCV, downloadCV, shareCV, finalBillCV, makePaymentCV;
    TextView consultChargeTV, followUpChargeTV, glucoseFChargeTV, glucoseRChargeTV, glucoseNFChargeTV, glucosePPNChargeTV, haemoglobinChargeTV, cholesterolChargeTV, bpChargeTV, uricAcidChargeTV, totalAmountTV, payingBillTV;
    String paymentStatus = "";
    int total_amount = 0;
    RadioButton yes, no;
    String not_paying_reason = "";
    EditText not_paying_reasonET;
    TextInputLayout not_paying_reasonTIL;
    RadioGroup radioGroup;
    SessionManager sessionManager;
    private boolean hasLicense = false;
    JSONObject obj = null;
    private Bitmap bitmap;
    String finalBillPath = "";
    SyncUtils syncUtils = new SyncUtils();
    ConceptAttributeListDAO conceptAttributeListDAO = new ConceptAttributeListDAO();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_confirmation);
        initViews();
        setToolbar();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        sessionManager = new SessionManager(this);

        //editText
        not_paying_reasonTIL = findViewById(R.id.reasonTIL);
        not_paying_reasonET = findViewById(R.id.reasonET);

        //radioButton
        yes = findViewById(R.id.yes_pay_bill);
        no = findViewById(R.id.no_pay_bill);
        radioGroup = findViewById(R.id.radioGrp);

        //textViews
        patientDetailsTV = findViewById(R.id.patientDetailsTV);
        consultChargeTV = findViewById(R.id.consultation_chargesTV);
        followUpChargeTV = findViewById(R.id.follow_up_chargesTV);
        glucoseFChargeTV = findViewById(R.id.glucose_f_chargesTV);
        glucoseRChargeTV = findViewById(R.id.glucose_ran_chargesTV);
        glucoseNFChargeTV = findViewById(R.id.glucose_nf_chargesTV);
        glucosePPNChargeTV = findViewById(R.id.glucose_ppn_chargesTV);
        haemoglobinChargeTV = findViewById(R.id.haemeo_chargesTV);
        cholesterolChargeTV = findViewById(R.id.cholestrol_chargesTV);
        bpChargeTV = findViewById(R.id.bp_chargesTV);
        uricAcidChargeTV = findViewById(R.id.uric_acid_chargesTV);
        totalAmountTV = findViewById(R.id.total_chargesTV);
        payingBillTV = findViewById(R.id.paying_billTV);

        //cardViews
        consultCV = findViewById(R.id.consultation_chargesCV);
        followUPCV = findViewById(R.id.follow_up_chargesCV);
        glucoseFCV = findViewById(R.id.glucose_f_chargesCV);
        glucoseRCV = findViewById(R.id.glucose_ran_chargesCV);
        glucoseNFCV = findViewById(R.id.glucose_nf_chargesCV);
        glucosePPNCV = findViewById(R.id.glucose_ppn_chargesCV);
        haemoglobinCV = findViewById(R.id.haemeo_chargesCV);
        cholesterolCV = findViewById(R.id.cholestrol_chargesCV);
        bpCV = findViewById(R.id.bp_chargesCV);
        uricAcidCV = findViewById(R.id.uric_acid_chargesCV);
        totalAmountCV = findViewById(R.id.total_chargesCV);
        confirmBillCV = findViewById(R.id.button_confirm_bill);
        makePaymentCV = findViewById(R.id.button_make_payment);
        printCV = findViewById(R.id.button_print);
        downloadCV = findViewById(R.id.button_download);
        shareCV = findViewById(R.id.button_share);
        finalBillCV = findViewById(R.id.finalBillCV);
        paymentStatusTV = findViewById(R.id.paymentStatus);


        Intent intent = getIntent();
        if (intent != null) {
            patientName = intent.getStringExtra("patientName");
            patientPhoneNum = intent.getStringExtra("patientPhoneNum");
            patientVillage = intent.getStringExtra("patientVillage");
            patientOpenID = intent.getStringExtra("patientOpenMRSID");
            patientHideVisitID = intent.getStringExtra("patientHideVisitID");
            visitType = intent.getStringExtra("visitType");
            patientVisitID = intent.getStringExtra("patientVisitID");
            receiptNum = intent.getStringExtra("receiptNum");
            billDateString = intent.getStringExtra("receiptDate");
            billType = intent.getStringExtra("billType");
            selectedTests = (ArrayList<String>) intent.getSerializableExtra("testsList");
        }

        patientDetails = "Receipt Number : " + receiptNum + "\nClient Name : " +
                patientName + "\nClient ID : " + patientOpenID + "\nVisit ID : " +
                patientHideVisitID + "\nContact Number : " + patientPhoneNum
                + "\nClient Village Name : " + patientVillage + "\n\n" +
                "Date : " + billDateString;

        patientDetailsTV.setText(patientDetails);
        manageCardView(selectedTests);
        if(!billType.equals("NA"))
        {
            if(billType.equals("Paid"))
            {
                paymentStatusTV.setVisibility(View.VISIBLE);
                paymentStatusTV.setText("Paid");
                paymentStatusTV.setBackgroundColor(Color.GREEN);
            }
            else if(billType.contains("Unpaid"))
            {
                paymentStatusTV.setVisibility(View.VISIBLE);
                paymentStatusTV.setText("Unpaid");
                paymentStatusTV.setBackgroundColor(Color.RED);
            }
            payingBillTV.setVisibility(View.GONE);
            radioGroup.setVisibility(View.GONE);
            confirmBillCV.setVisibility(View.GONE);
            makePaymentCV.setVisibility(View.VISIBLE);
            printCV.setVisibility(View.VISIBLE);
            downloadCV.setVisibility(View.VISIBLE);
            shareCV.setVisibility(View.VISIBLE);
        }
        setPrices();
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        confirmBillCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if(!yes.isChecked() && !no.isChecked())
                {
                    MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(billConfirmationActivity.this);
                    alertDialogBuilder.setTitle(R.string.error);
                    alertDialogBuilder.setMessage(R.string.select_payment_information);
                    alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                    Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                    IntelehealthApplication.setAlertDialogCustomTheme(billConfirmationActivity.this, alertDialog);
                    return;
                }
                if(no.isChecked() && not_paying_reasonTIL.getVisibility() == View.VISIBLE)
                {
                    if(not_paying_reasonET.getText().toString().isEmpty()) {
                        not_paying_reasonET.setError(getResources().getString(R.string.error_field_required));
                        Toast.makeText(billConfirmationActivity.this, getString(R.string.enter_reason_toast), Toast.LENGTH_LONG).show();
                        return;
                    }
                    else {
                        not_paying_reason = not_paying_reasonET.getText().toString();
                        paymentStatus = "Unpaid - " + not_paying_reason;
                    }
                }*/
                boolean billSuccess = syncBillToServer();
                if(billSuccess) {
                    if(paymentStatus.equals("Paid"))
                    {
                        paymentStatusTV.setVisibility(View.VISIBLE);
                        paymentStatusTV.setText("Paid");
                        paymentStatusTV.setBackgroundColor(Color.GREEN);
                    }
                    else if(paymentStatus.contains("Unpaid"))
                    {
                        paymentStatusTV.setVisibility(View.VISIBLE);
                        paymentStatusTV.setText("Unpaid");
                        paymentStatusTV.setBackgroundColor(Color.RED);
                    }
                    Toast.makeText(billConfirmationActivity.this, getString(R.string.bill_generated_success), Toast.LENGTH_LONG).show();
                    payingBillTV.setVisibility(View.GONE);
                    radioGroup.setVisibility(View.GONE);
                    confirmBillCV.setVisibility(View.GONE);
                    if (not_paying_reasonTIL.getVisibility() == View.VISIBLE)
                        not_paying_reasonTIL.setVisibility(View.GONE);
                    makePaymentCV.setVisibility(View.VISIBLE);
                    printCV.setVisibility(View.VISIBLE);
                    downloadCV.setVisibility(View.VISIBLE);
                    shareCV.setVisibility(View.VISIBLE);
                }
            }
        });

        makePaymentCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makePayment();
            }
        });

        downloadCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("size", "" + finalBillCV.getWidth() + " " + finalBillCV.getWidth());
                bitmap = LoadBitmap(finalBillCV, finalBillCV.getWidth(), finalBillCV.getHeight());
                createPdf();
            }
        });

        shareCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareFile();
            }
        });
    }

    private boolean syncBillToServer() {
        boolean success = false;
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
        Date todayDate = new Date();
        String thisDate = currentDate.format(todayDate);
        String encounter_uuid = UUID.randomUUID().toString();
        success = createEncounter(encounter_uuid, thisDate);
        if(success) {
            success = createObs(encounter_uuid);
        }
        if(success)
            success = syncUtils.syncForeground("bill");
        return success;
    }

    private boolean createObs(String encounter_uuid) {
        boolean success = false;
        ObsDTO obsDTO = new ObsDTO();
        ObsDAO obsDAO = new ObsDAO();
        obsDTO.setConceptuuid(UuidDictionary.BILL_DATE);
        obsDTO.setEncounteruuid(encounter_uuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(billDateString);
        obsDTO.setUuid(AppConstants.NEW_UUID);

        try {
            success = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        obsDTO.setConceptuuid(UuidDictionary.BILL_VISIT_TYPE);
        obsDTO.setEncounteruuid(encounter_uuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(visitType);
        obsDTO.setUuid(AppConstants.NEW_UUID);

        try {
            success = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        obsDTO.setConceptuuid(UuidDictionary.BILL_PAYMENT_STATUS);
        obsDTO.setEncounteruuid(encounter_uuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(paymentStatus);
        obsDTO.setUuid(AppConstants.NEW_UUID);

        try {
            success = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        obsDTO.setConceptuuid(UuidDictionary.BILL_NUM);
        obsDTO.setEncounteruuid(encounter_uuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(receiptNum);
        obsDTO.setUuid(AppConstants.NEW_UUID);

        try {
            success = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }


        if (selectedTests.contains(getString(R.string.blood_glucose_non_fasting)))
        {
            obsDTO.setConceptuuid(UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_ID);
            obsDTO.setEncounteruuid(encounter_uuid);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue("15");
            obsDTO.setUuid(AppConstants.NEW_UUID);

            try {
                success = obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
        if (selectedTests.contains(getString(R.string.blood_glucose_fasting)))
        {
            obsDTO.setConceptuuid(UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_FASTING_ID);
            obsDTO.setEncounteruuid(encounter_uuid);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue("15");
            obsDTO.setUuid(AppConstants.NEW_UUID);

            try {
                success = obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
        if (selectedTests.contains(getString(R.string.blood_glucose_post_prandial)))
        {
            obsDTO.setConceptuuid(UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_POST_PRANDIAL_ID);
            obsDTO.setEncounteruuid(encounter_uuid);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue("15");
            obsDTO.setUuid(AppConstants.NEW_UUID);

            try {
                success = obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
        if (selectedTests.contains(getString(R.string.blood_glucose_random)))
        {
            obsDTO.setConceptuuid(UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_RANDOM_ID);
            obsDTO.setEncounteruuid(encounter_uuid);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue("15");
            obsDTO.setUuid(AppConstants.NEW_UUID);

            try {
                success = obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
        if (selectedTests.contains(getString(R.string.uric_acid)))
        {
            obsDTO.setConceptuuid(UuidDictionary.BILL_PRICE_URIC_ACID_ID);
            obsDTO.setEncounteruuid(encounter_uuid);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue("30");
            obsDTO.setUuid(AppConstants.NEW_UUID);

            try {
                success = obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
        if (selectedTests.contains(getString(R.string.total_cholestrol)))
        {
            obsDTO.setConceptuuid(UuidDictionary.BILL_PRICE_TOTAL_CHOLESTEROL_ID);
            obsDTO.setEncounteruuid(encounter_uuid);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue("80");
            obsDTO.setUuid(AppConstants.NEW_UUID);

            try {
                success = obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
        if (selectedTests.contains(getString(R.string.haemoglobin)))
        {
            obsDTO.setConceptuuid(UuidDictionary.BILL_PRICE_HEMOGLOBIN_ID);
            obsDTO.setEncounteruuid(encounter_uuid);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue("20");
            obsDTO.setUuid(AppConstants.NEW_UUID);

            try {
                success = obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
        if (selectedTests.contains(getString(R.string.visit_summary_bp)))
        {
            obsDTO.setConceptuuid(UuidDictionary.BILL_PRICE_BP_ID);
            obsDTO.setEncounteruuid(encounter_uuid);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue("5");
            obsDTO.setUuid(AppConstants.NEW_UUID);

            try {
                success = obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }

        return success;
    }

    private boolean createEncounter(String encounter_uuid, String thisDate) {
        boolean success = false;
        EncounterDTO encounterDTO = new EncounterDTO();
        EncounterDAO encounterDAO = new EncounterDAO();
        encounterDTO = new EncounterDTO();
        encounterDTO.setUuid(encounter_uuid);
        encounterDTO.setEncounterTypeUuid(encounterDAO.getEncounterTypeUuid("Visit Billing Details"));
        encounterDTO.setEncounterTime(thisDate);
        encounterDTO.setVisituuid(patientVisitID);
        encounterDTO.setSyncd(false);
        encounterDTO.setProvideruuid(sessionManager.getProviderID());
        Log.d("DTO", "DTO:detail " + encounterDTO.getProvideruuid());
        encounterDTO.setVoided(0);
        encounterDTO.setPrivacynotice_value("Accept");//privacy value added.

        try {
            success = encounterDAO.createEncountersToDB(encounterDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        return success;
    }

    private void shareFile() {
        File path = this.getExternalFilesDir("Bill");
        String fName = patientName + "_" + patientOpenID + "_" + billDateString + ".pdf";
        String finalPath = path + fName;
        finalBillPath = finalPath;
        if (finalBillPath.equals("")) {
            Toast.makeText(billConfirmationActivity.this, getString(R.string.download_bill), Toast.LENGTH_LONG).show();
            return;
        }
        File file = new File(path, fName);
        if (!file.exists()) {
            Toast.makeText(billConfirmationActivity.this, getString(R.string.download_bill), Toast.LENGTH_LONG).show();
            return;
        }
        Uri uri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Share the file...."));
    }


    private void setPrices() {
            if (consultCV.getVisibility() == View.VISIBLE) {
                String price = conceptAttributeListDAO.getConceptPrice("Billing Visit Type Consultation");
                price = getPrice(price, price.indexOf('.'));
                consultChargeTV.setText("₹" + price + "/-");
                total_amount += Integer.parseInt(price);
            }
            if (followUPCV.getVisibility() == View.VISIBLE) {
                String price = conceptAttributeListDAO.getConceptPrice("Billing Visit Type Followup");
                price = getPrice(price, price.indexOf('.'));
                followUpChargeTV.setText("₹" + price + "/-");
                total_amount += Integer.parseInt(price);
            }
            if (glucoseRCV.getVisibility() == View.VISIBLE) {
                String price = conceptAttributeListDAO.getConceptPrice("Blood Sugar (Random)");
                price = getPrice(price, price.indexOf('.'));
                glucoseRChargeTV.setText("₹" + price + "/-");
                total_amount += Integer.parseInt(price);
            }
            if (glucoseFCV.getVisibility() == View.VISIBLE) {
                String price = conceptAttributeListDAO.getConceptPrice("Blood Glucose (Fasting)");
                price = getPrice(price, price.indexOf('.'));
                glucoseFChargeTV.setText("₹" + price + "/-");
                total_amount += Integer.parseInt(price);
            }
            if (glucosePPNCV.getVisibility() == View.VISIBLE) {
                String price = conceptAttributeListDAO.getConceptPrice("Blood Sugar ( Post-prandial)");
                price = getPrice(price, price.indexOf('.'));
                glucosePPNChargeTV.setText("₹" + price + "/-");
                total_amount += Integer.parseInt(price);
            }
            if (glucoseNFCV.getVisibility() == View.VISIBLE) {
                String price = conceptAttributeListDAO.getConceptPrice("Blood Sugar (Non-Fasting)");
                price = getPrice(price, price.indexOf('.'));
                glucoseNFChargeTV.setText("₹" + price + "/-");
                total_amount += Integer.parseInt(price);
            }
            if (uricAcidCV.getVisibility() == View.VISIBLE) {
                String price = conceptAttributeListDAO.getConceptPrice("SERUM URIC ACID");
                price = getPrice(price, price.indexOf('.'));
                uricAcidChargeTV.setText("₹" + price + "/-");
                total_amount += Integer.parseInt(price);
            }
            if (haemoglobinCV.getVisibility() == View.VISIBLE) {
                String price = conceptAttributeListDAO.getConceptPrice("Haemoglobin Test");
                price = getPrice(price, price.indexOf('.'));
                haemoglobinChargeTV.setText("₹" + price + "/-");
                total_amount += Integer.parseInt(price);
            }
            if (cholesterolCV.getVisibility() == View.VISIBLE) {
                String price = conceptAttributeListDAO.getConceptPrice("TOTAL CHOLESTEROL");
                price = getPrice(price, price.indexOf('.'));
                cholesterolChargeTV.setText("₹" + price + "/-");
                total_amount += Integer.parseInt(price);
            }
            if (bpCV.getVisibility() == View.VISIBLE) {
                String price = conceptAttributeListDAO.getConceptPrice("BP Test");
                price = getPrice(price, price.indexOf('.'));
                bpChargeTV.setText("₹" + price + "/-");
                total_amount += Integer.parseInt(price);
            }

        totalAmountTV.setText("₹" + String.valueOf(total_amount) + "/-");
    }

    private String getPrice(String price, int indexOf) {
        return price.substring(0, indexOf);
    }

    private void manageCardView(ArrayList<String> selectedTests) {
        if (visitType.equalsIgnoreCase("Consultation")) {
            consultCV.setVisibility(View.VISIBLE);
            followUPCV.setVisibility(View.GONE);
        }
        if (visitType.equalsIgnoreCase("Follow-Up")) {
            followUPCV.setVisibility(View.VISIBLE);
            consultCV.setVisibility(View.GONE);
        }
        if (selectedTests.contains(getString(R.string.blood_glucose_non_fasting)))
            glucoseNFCV.setVisibility(View.VISIBLE);
        if (selectedTests.contains(getString(R.string.blood_glucose_fasting)))
            glucoseFCV.setVisibility(View.VISIBLE);
        if (selectedTests.contains(getString(R.string.blood_glucose_post_prandial)))
            glucosePPNCV.setVisibility(View.VISIBLE);
        if (selectedTests.contains(getString(R.string.blood_glucose_random)))
            glucoseRCV.setVisibility(View.VISIBLE);
        if (selectedTests.contains(getString(R.string.uric_acid)))
            uricAcidCV.setVisibility(View.VISIBLE);
        if (selectedTests.contains(getString(R.string.total_cholestrol)))
            cholesterolCV.setVisibility(View.VISIBLE);
        if (selectedTests.contains(getString(R.string.haemoglobin)))
            haemoglobinCV.setVisibility(View.VISIBLE);
        if (selectedTests.contains(getString(R.string.visit_summary_bp)))
            bpCV.setVisibility(View.VISIBLE);

    }

    private void setToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        setTitle(patientName + " : " + receiptNum);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.yes_pay_bill:
                if (checked) {
                    paymentStatus = "Paid";
                    not_paying_reasonTIL.setVisibility(View.GONE);
                    not_paying_reasonET.setText("");
                }
                break;
            case R.id.no_pay_bill:
                if (checked) {
                    paymentStatus = "Unpaid";
                    not_paying_reasonTIL.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        //do nothing
        //Use the buttons on the screen to navigate
    }

    private Bitmap LoadBitmap(View v, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }

    private void createPdf() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        //  Display display = wm.getDefaultDisplay();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        float height = displaymetrics.heightPixels;
        float width = displaymetrics.widthPixels;

        int convertHeight = (int) height, convertWidth = (int) width;

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(convertWidth, convertHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        canvas.drawPaint(paint);

        bitmap = Bitmap.createScaledBitmap(bitmap, convertWidth, convertHeight, true);

        paint.setColor(Color.BLUE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        document.finishPage(page);

        // write the document content
        File path = this.getExternalFilesDir("Bill");
        String fName = patientName + "_" + patientOpenID + "_" + billDateString + ".pdf";
        File filePath = new File(path, fName);
        if (!filePath.exists())
            filePath.mkdirs();

        String finalPath = path + fName;
        finalBillPath = finalPath;
        File file = new File(path, fName);
        if (file.exists()) file.delete();

        try {
            file.createNewFile();
            document.writeTo(new FileOutputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
            return;
        }

        document.close();
        Toast.makeText(this, "successfully pdf created", Toast.LENGTH_SHORT).show();

//        openPdf(finalPath);

    }

    private void openPdf(String path) {
        File file = new File(path);
        if (file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No Application for pdf view", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.detail_home:
                Intent intent = new Intent(billConfirmationActivity.this, HomeActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*This function implements the Razorpay functionality
    By: Nishita Goyal
    Ticket: SCD-13*/
    private void makePayment() {
        String sAmount = "1";
        // rounding off the amount.
        int amount = Math.round(Float.parseFloat(sAmount) * 100);
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_lsxV2Ylin7dw1Y");
        checkout.setImage(R.drawable.scd_logo);
        JSONObject object = new JSONObject();
        try {
            object.put("name", "Smart Care Doc");
            object.put("description", "Test payment");
            object.put("theme.color", "#2E1E91");
            object.put("currency", "INR");
            object.put("amount", amount);
            object.put("prefill.contact", "9958392968");
            object.put("prefill.email", "nishita@intelehealth.org");
            checkout.open(billConfirmationActivity.this, object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPaymentSuccess(String s) {
        // this method is called on payment success.
        Toast.makeText(this, "Payment is successful : " + s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPaymentError(int i, String s) {
        // on payment failed.
        Toast.makeText(this, "Payment Failed due to error : " + s, Toast.LENGTH_SHORT).show();
    }

}
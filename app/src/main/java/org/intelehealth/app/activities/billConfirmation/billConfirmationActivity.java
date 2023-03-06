package org.intelehealth.app.activities.billConfirmation;

import static com.rt.printerlibrary.enumerate.CommonEnum.ALIGN_MIDDLE;
import static org.intelehealth.app.activities.textprintactivity.TextPrintESCActivity.curPrinterInterface;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.rt.printerlibrary.bean.BluetoothEdrConfigBean;
import com.rt.printerlibrary.bean.Position;
import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.EscFactory;
import com.rt.printerlibrary.connect.PrinterInterface;
import com.rt.printerlibrary.enumerate.BmpPrintMode;
import com.rt.printerlibrary.enumerate.CommonEnum;
import com.rt.printerlibrary.enumerate.ConnectStateEnum;
import com.rt.printerlibrary.enumerate.ESCFontTypeEnum;
import com.rt.printerlibrary.exception.SdkException;
import com.rt.printerlibrary.factory.cmd.CmdFactory;
import com.rt.printerlibrary.factory.connect.BluetoothFactory;
import com.rt.printerlibrary.factory.connect.PIFactory;
import com.rt.printerlibrary.factory.printer.PrinterFactory;
import com.rt.printerlibrary.factory.printer.ThermalPrinterFactory;
import com.rt.printerlibrary.observer.PrinterObserver;
import com.rt.printerlibrary.observer.PrinterObserverManager;
import com.rt.printerlibrary.printer.RTPrinter;
import com.rt.printerlibrary.setting.BitmapSetting;
import com.rt.printerlibrary.setting.CommonSetting;
import com.rt.printerlibrary.setting.TextSetting;
import com.rt.printerlibrary.utils.FuncUtils;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.ConceptAttributeListDAO;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.dialog.BluetoothDeviceChooseDialog;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.BaseEnum;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.TimeRecordUtils;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class billConfirmationActivity extends AppCompatActivity implements PaymentResultListener, PrinterObserver {

    Toolbar toolbar;
    String patientName, patientVillage, patientOpenID, patientHideVisitID, patientPhoneNum, visitType, patientVisitID, billType;
    ArrayList<String> selectedTests = new ArrayList<>();
    TextView patientDetailsTV;
    String patientDetails;
    String receiptNum = "XXXXX";
    String billDateString = "DD MM YYYY";
    LinearLayout consultCV, followUPCV, glucoseFCV, glucoseRCV, glucoseNFCV, glucosePPNCV, haemoglobinCV, cholesterolCV, bpCV, uricAcidCV, totalAmountCV, padd;
    CardView confirmBillCV, printCV, downloadCV, shareCV, finalBillCV, makePaymentCV;
    TextView consultChargeTV, followUpChargeTV, glucoseFChargeTV, glucoseRChargeTV, glucoseNFChargeTV, glucosePPNChargeTV, haemoglobinChargeTV, cholesterolChargeTV, bpChargeTV, uricAcidChargeTV, totalAmountTV, payingBillTV, tv_device_selected;
    String paymentStatus = "Paid";
    Button btn_disConnect, btn_connect;
    private ProgressBar pb_connect;
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
    private ArrayList<PrinterInterface> printerInterfaceArrayList = new ArrayList<>();
    private RTPrinter rtPrinter = null;
    private PrinterFactory printerFactory;
    private Bitmap mBitmap = null;
    private int bmpPrintWidth = 50;
    private String printStr;
    private TextSetting textSetting;
    private String mChartsetName = "UTF-8";
    private ESCFontTypeEnum curESCFontType = null;
    private Object configObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_confirmation);
        initViews();
        setToolbar();
        initBluetoothDevice();

    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        sessionManager = new SessionManager(billConfirmationActivity.this);
        String language = sessionManager.getAppLanguage();
        Log.v("Bill", "Bill: " + language);
        //In case of crash still the app should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

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
        tv_device_selected = findViewById(R.id.tv_device_selected);
        btn_connect = findViewById(R.id.btn_connect);
        btn_disConnect = findViewById(R.id.btn_disConnect);
        pb_connect = findViewById(R.id.pb_connect);
        padd = findViewById(R.id.padd);


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

        patientDetails = getString(R.string.receipt_no) + receiptNum + "\n" + getString(R.string.client_name) +
                patientName + "\n" + getString(R.string.client_id) + patientOpenID + "\n" + getString(R.string.visit_id) +
                patientHideVisitID + "\n" + getString(R.string.contact_no) + patientPhoneNum
                + "\n" + getString(R.string.client_village_name) + patientVillage + "\n" +
                getString(R.string.date) + billDateString;

        patientDetailsTV.setText(patientDetails);
        manageCardView(selectedTests);
        if (!billType.equals("NA")) {
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

        tv_device_selected.setOnClickListener(v -> {
            showBluetoothDeviceChooseDialog(); // Here on click, will open the Dialog that will show all the nearby Bluetooth devices...
        });

        btn_connect.setOnClickListener(v -> {
            doConnect(); //Here on clicking will connect with the selected Bluetooth device...
        });

        btn_disConnect.setOnClickListener(v -> {
            doDisConnect();
        });

        printCV.setOnClickListener(v -> {
            try {
                textPrint();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });

        confirmBillCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean billSuccess = syncBillToServer();
                if (billSuccess) {
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

        Checkout.preload(getApplicationContext());

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

    private void textPrint() throws UnsupportedEncodingException {
        switch (IntelehealthApplication.getInstance().getCurrentCmdType()) {
            case BaseEnum.CMD_ESC:
                escPrint();
                break;
            default:
                break;
        }
    }

    private boolean syncBillToServer() {
        boolean success = false;
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
        Date todayDate = new Date();
        String thisDate = currentDate.format(todayDate);
        String encounter_uuid = UUID.randomUUID().toString();
        success = createEncounter(encounter_uuid, thisDate);
        if (success) {
            success = createObs(encounter_uuid);
        }
        if (success)
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


        if (selectedTests.contains(getString(R.string.blood_glucose_non_fasting))) {
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
        if (selectedTests.contains(getString(R.string.blood_glucose_fasting))) {
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
        if (selectedTests.contains(getString(R.string.blood_glucose_post_prandial))) {
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
        if (selectedTests.contains(getString(R.string.blood_glucose_random))) {
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
        if (selectedTests.contains(getString(R.string.uric_acid))) {
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
        if (selectedTests.contains(getString(R.string.total_cholestrol))) {
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
        if (selectedTests.contains(getString(R.string.haemoglobin))) {
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
        if (selectedTests.contains(getString(R.string.visit_summary_bp))) {
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
        encounterDTO.setPrivacynotice_value("Accept"); //privacy value added.
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
        Toast.makeText(this, getResources().getString(R.string.successfully_pdf_created), Toast.LENGTH_SHORT).show();
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
        switch (item.getItemId()) {
            case R.id.detail_home:
                Intent intent = new Intent(billConfirmationActivity.this, HomeActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This function implements the Razorpay functionality
     * By: Nishita Goyal
     * Ticket: SCD-13
     */
    private void makePayment() {
        String sAmount = String.valueOf(total_amount);
        // rounding off the amount.
        int amount = Math.round(Float.parseFloat(sAmount) * 100);
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_lsxV2Ylin7dw1Y");
        checkout.setImage(R.drawable.scd_logo);

        JSONObject object = new JSONObject();
        try {
//            object.put("config.display.language", "mar");
            object.put("name", "MySmartCareDoc");
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

    //This will open a Dialog that will show all the Bluetooth devices...
    private void showBluetoothDeviceChooseDialog() {
        BluetoothDeviceChooseDialog bluetoothDeviceChooseDialog = new BluetoothDeviceChooseDialog();
        bluetoothDeviceChooseDialog.setOnDeviceItemClickListener(
                new BluetoothDeviceChooseDialog.onDeviceItemClickListener() {
                    @Override
                    public void onDeviceItemClick(BluetoothDevice device) {
                        if (TextUtils.isEmpty(device.getName())) {
                            tv_device_selected.setText(device.getAddress());
                        } else {
                            tv_device_selected.setText(device.getName() + " [" + device.getAddress() + "]");
                        }
                        configObj = new BluetoothEdrConfigBean(device);
                        tv_device_selected.setTag(BaseEnum.HAS_DEVICE);
                        isConfigPrintEnable(configObj);

                        btn_disConnect.setBackgroundResource(R.drawable.bg_button_disable);
                        btn_connect.setBackgroundResource(R.drawable.bg_visit_details);
                    }
                });
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth.
            Toast.makeText(billConfirmationActivity.this,
                    getResources().getString(R.string.bluetooth_notsupported_device), Toast.LENGTH_SHORT).show();
            return;
        } else if (!mBluetoothAdapter.isEnabled()) {
            // Bluetooth is Turned OFF.
            Toast.makeText(billConfirmationActivity.this,
                    getResources().getString(R.string.turn_on_bluetooth), Toast.LENGTH_SHORT).show();
            return;
        } else {
            // Bluetooth is Turned ON.
            bluetoothDeviceChooseDialog.show(billConfirmationActivity.this.getSupportFragmentManager(), null);
        }
    }

    private void initBluetoothDevice() {
        IntelehealthApplication.getInstance().setCurrentCmdType(BaseEnum.CMD_ESC);
        printerFactory = new ThermalPrinterFactory();
        rtPrinter = printerFactory.create();
        PrinterObserverManager.getInstance().add(this);
        if (curPrinterInterface != null) {
            // to maintain the bluetooth pairing throughout the app.
            rtPrinter.setPrinterInterface(curPrinterInterface);
            tv_device_selected.setText(curPrinterInterface.getConfigObject().toString());
            tv_device_selected.setTag(BaseEnum.HAS_DEVICE);
            printerInterfaceArrayList.add(curPrinterInterface);
            rtPrinter.setPrinterInterface(curPrinterInterface);
            setPrintEnable(true);
            btn_disConnect.setBackgroundResource(R.drawable.bg_end_visit);
            btn_connect.setBackgroundResource(R.drawable.bg_button_disable);
        }
        IntelehealthApplication.getInstance().setRtPrinter(rtPrinter);
        rtPrinter = IntelehealthApplication.getInstance().getRtPrinter();
        textSetting = new TextSetting();
        finalBillCV.setBackgroundColor(getResources().getColor(R.color.white));
        finalBillCV.setDrawingCacheEnabled(true);
        finalBillCV.buildDrawingCache();
        finalBillCV.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        finalBillCV.layout(0, 0, finalBillCV.getMeasuredWidth(), finalBillCV.getMeasuredHeight());
        mBitmap = finalBillCV.getDrawingCache(); // converting cardview to bitmap
    }

    @Override
    public void printerObserverCallback(final PrinterInterface printerInterface, final int state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pb_connect.setVisibility(View.GONE);
                // disconnect and connect button color change.
                if (state == CommonEnum.CONNECT_STATE_SUCCESS) {
                    Toast.makeText(billConfirmationActivity.this, printerInterface.getConfigObject().toString()
                            + getString(R.string._main_connected), Toast.LENGTH_SHORT).show();
                } else if (state == CommonEnum.CONNECT_STATE_INTERRUPTED) {
                    if (printerInterface != null && printerInterface.getConfigObject() != null) {
                        Toast.makeText(billConfirmationActivity.this, printerInterface.getConfigObject().toString()
                                        + getString(R.string._main_disconnect),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(billConfirmationActivity.this, getString(R.string._main_disconnect),
                                Toast.LENGTH_SHORT).show();
                    }
                }
                switch (state) {
                    case CommonEnum.CONNECT_STATE_SUCCESS:
                        TimeRecordUtils.record("RT连接end：", System.currentTimeMillis());
                        tv_device_selected.setTag(BaseEnum.HAS_DEVICE);
                        curPrinterInterface = printerInterface; // set current Printer Interface
                        printerInterfaceArrayList.add(printerInterface);
                        rtPrinter.setPrinterInterface(printerInterface);
                        setPrintEnable(true);
                        btn_disConnect.setBackgroundResource(R.drawable.bg_end_visit);
                        btn_connect.setBackgroundResource(R.drawable.bg_button_disable);
                        break;
                    case CommonEnum.CONNECT_STATE_INTERRUPTED:
                        if (printerInterface != null && printerInterface.getConfigObject() != null) {
                        } else {
                        }
                        TimeRecordUtils.record("Time：", System.currentTimeMillis());
                        tv_device_selected.setText(R.string.please_connect);
                        tv_device_selected.setTag(BaseEnum.NO_DEVICE);
                        curPrinterInterface = null;
                        printerInterfaceArrayList.remove(printerInterface);
                        setPrintEnable(false);
                        btn_disConnect.setBackgroundResource(R.drawable.bg_button_disable);
                        btn_connect.setBackgroundResource(R.drawable.bg_button_disable);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void printerReadMsgCallback(PrinterInterface printerInterface, byte[] bytes) {

    }

    private void setPrintEnable(boolean isEnable) {
        btn_connect.setEnabled(!isEnable);
        btn_disConnect.setEnabled(isEnable);
    }

    private void isConfigPrintEnable(Object configObj) {
        if (isInConnectList(configObj)) {
            setPrintEnable(true);
        } else {
            setPrintEnable(false);
        }
    }

    private boolean isInConnectList(Object configObj) {
        boolean isInList = false;
        for (int i = 0; i < printerInterfaceArrayList.size(); i++) {
            PrinterInterface printerInterface = printerInterfaceArrayList.get(i);
            if (configObj.toString().equals(printerInterface.getConfigObject().toString())) {
                if (printerInterface.getConnectState() == ConnectStateEnum.Connected) {
                    isInList = true;
                    break;
                }
            }
        }
        return isInList;
    }

    private void doConnect() {
        if (Integer.parseInt(tv_device_selected.getTag().toString()) == BaseEnum.NO_DEVICE) { // No device is selected.
            showAlertDialog(getString(R.string.main_pls_choose_device));
            return;
        }
        pb_connect.setVisibility(View.VISIBLE);
        TimeRecordUtils.record("Start：", System.currentTimeMillis());
        BluetoothEdrConfigBean bluetoothEdrConfigBean = (BluetoothEdrConfigBean) configObj;
        connectBluetooth(bluetoothEdrConfigBean);
    }

    private void doDisConnect() {
        if (Integer.parseInt(tv_device_selected.getTag().toString()) == BaseEnum.NO_DEVICE) {
            return;
        }
        if (rtPrinter != null && rtPrinter.getPrinterInterface() != null) {
            rtPrinter.disConnect();
        }
        // disconnect and connect button color change.
        btn_disConnect.setBackgroundResource(R.drawable.bg_button_disable);
        btn_connect.setBackgroundResource(R.drawable.bg_button_disable);
        tv_device_selected.setText(getString(R.string.please_connect));
        tv_device_selected.setTag(BaseEnum.NO_DEVICE);
        setPrintEnable(false);
    }

    public void showAlertDialog(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                android.app.AlertDialog.Builder dialog =
                        new android.app.AlertDialog.Builder(billConfirmationActivity.this);
                dialog.setTitle("Please connect device");
                dialog.setMessage(msg);
                dialog.setNegativeButton(R.string.cancel, null);
                dialog.show();
            }
        });
    }

    private void connectBluetooth(BluetoothEdrConfigBean bluetoothEdrConfigBean) {
        PIFactory piFactory = new BluetoothFactory();
        PrinterInterface printerInterface = piFactory.create();
        printerInterface.setConfigObject(bluetoothEdrConfigBean);
        rtPrinter.setPrinterInterface(printerInterface);
        try {
            rtPrinter.connect(bluetoothEdrConfigBean);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //do nothing...
        }
    }

    private void escPrint() throws UnsupportedEncodingException {
        printCV.setEnabled(false); // to avoid multiple prints at same time if user clicks by mistake
        printCV.setClickable(false);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (rtPrinter != null) {
                    CmdFactory escFac = new EscFactory();
                    Cmd escCmd = escFac.create();

                    escCmd.append(escCmd.getHeaderCmd());// Initial //btnCmds = 2......
                    escCmd.setChartsetName(mChartsetName);
                    CommonSetting commonSetting = new CommonSetting();
                    commonSetting.setAlign(ALIGN_MIDDLE);

                    BitmapSetting bitmapSetting = new BitmapSetting();
                    bitmapSetting.setBmpPrintMode(BmpPrintMode.MODE_SINGLE_COLOR);
                    bitmapSetting.setBimtapLimitWidth(bmpPrintWidth * 8);

                    Position txtposition = new Position(0, 0);
                    textSetting.setTxtPrintPosition(txtposition);
                    escCmd.append(escCmd.getCommonSettingCmd(commonSetting));
                    escCmd.append(escCmd.getLFCRCmd());

                    // here printing the image...
                    try {
                        if (mBitmap != null)
                            escCmd.append(escCmd.getBitmapCmd(bitmapSetting, mBitmap));
                    } catch (SdkException e) {
                        e.printStackTrace();
                    }

                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());
                    Log.i("bill", FuncUtils.ByteArrToHex(escCmd.getAppendCmds()));
                    if (rtPrinter.getPrinterInterface() != null) {
                        // If without selecting Bluetooth user click Print button crash happens so added this condition.
                        rtPrinter.writeMsgAsync(escCmd.getAppendCmds());
                        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(billConfirmationActivity.this);
                        alertdialogBuilder.setMessage(R.string.printing);
                        alertdialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                printCV.setEnabled(true);
                                printCV.setClickable(true);
                                finish();
                            }
                        });
                        androidx.appcompat.app.AlertDialog alertDialog = alertdialogBuilder.create();
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.setCancelable(false);
                        alertDialog.show();
                        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
                        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                        IntelehealthApplication.setAlertDialogCustomTheme(billConfirmationActivity.this, alertDialog);
                    } else {
                        printCV.setEnabled(true);
                        printCV.setClickable(true);
                        Toast.makeText(billConfirmationActivity.this, getResources().getString
                                (R.string.tip_have_no_paired_device), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


}
package org.intelehealth.app.activities.prescription.thermalprinter;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rt.printerlibrary.bean.BluetoothEdrConfigBean;
import com.rt.printerlibrary.bean.Position;
import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.EscFactory;
import com.rt.printerlibrary.connect.PrinterInterface;
import com.rt.printerlibrary.enumerate.BmpPrintMode;
import com.rt.printerlibrary.enumerate.CommonEnum;
import com.rt.printerlibrary.enumerate.ConnectStateEnum;
import com.rt.printerlibrary.enumerate.ESCFontTypeEnum;
import com.rt.printerlibrary.enumerate.SettingEnum;
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
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.databinding.ActivityTextPrintEscactivityBinding;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.SessionManager;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class TextPrintESCActivity extends BaseActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener, PrinterObserver {
    private static final String TAG = TextPrintESCActivity.class.getSimpleName();
    private TextView presTextview, drDetailsTextview;
    private Button btnTextPrint;
    private String printStr;
    private TextSetting textSetting;
    private String mChartsetName = "UTF-8";
    private ESCFontTypeEnum curESCFontType = null;
    private Intent intent;
    private String prescData, doctorDetails, fontFamily, drSignText;
    private Bitmap mBitmap = null;
    private int bmpPrintWidth = 40;
    private TextView tvDeviceSelected;
    private Button btnConnect, btnDisConnect;
    private Object configObj;
    private ArrayList<PrinterInterface> printerInterfaceArrayList = new ArrayList<>();
    private ProgressBar pbConnect;
    private RTPrinter rtPrinter = null;
    private PrinterFactory printerFactory;
    public static PrinterInterface curPrinterInterface = null;
    private SessionManager sessionManager;
    private CheckBox ckSmallFont, ckAntiWhite, ckDoubleWidth,
            ckDoubleHeight, ckBold, ckUnderline;
    private Spinner spinEscFontType;
    private TextView drSignTextview;
    private String base64String;
    private ImageView imgDrSign;
    private ActivityTextPrintEscactivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTextPrintEscactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = new SessionManager(getBaseContext());
        initView();
        addListener();
        init();
    }

    @SuppressLint("WrongViewCast")
    public void initView() {
        ImageView ivBack = binding.toolbarCommon.ivBackArrowCommon;
        ImageView ivIsInternet = binding.toolbarCommon.imageviewIsInternetCommon;
        binding.toolbarCommon.tvScreenTitleCommon.setText(getString(R.string.view_print));
        ivBack.setVisibility(View.VISIBLE);
        ivBack.setOnClickListener(v -> onBackPressed());
        ivIsInternet.setVisibility(View.GONE);

        presTextview =binding.presTextview;
        drSignTextview = binding.drSignTextview;
        drDetailsTextview = binding.drDetailsTextview;
        btnTextPrint = binding.btnTxtprint;
        tvDeviceSelected = binding.tvDeviceSelected;
        btnConnect = binding.btnConnect;
        btnDisConnect = binding.btnDisConnect;
        pbConnect = binding.pbConnect;
        imgDrSign = binding.imageviewDrSign;
    }

    public void addListener() {
        btnTextPrint.setOnClickListener(this);
        tvDeviceSelected.setOnClickListener(this);
        btnConnect.setOnClickListener(this);
        btnDisConnect.setOnClickListener(this);
    }

    private String removeNull(String inputString) {
        // Remove all occurrences of "&null" (case-insensitive) and "null" (case-insensitive)


        return inputString
                .replaceAll("(?i)& ?\\bnull\\b", "") // Remove "&null" or "null" with optional "&"
                .trim();
    }

    public void init() {
        IntelehealthApplication.getInstance().setCurrentCmdType(BaseEnum.CMD_ESC);
        // printerFactory = new UniversalPrinterFactory();
        printerFactory = new ThermalPrinterFactory();
        rtPrinter = printerFactory.create();
        PrinterObserverManager.getInstance().add(this);

        if (curPrinterInterface != null) {
            // to maintain the bluetooth pairing throughout the app.
            rtPrinter.setPrinterInterface(curPrinterInterface);
            tvDeviceSelected.setText(curPrinterInterface.getConfigObject().toString());
            tvDeviceSelected.setTag(BaseEnum.HAS_DEVICE);
            printerInterfaceArrayList.add(curPrinterInterface);
            rtPrinter.setPrinterInterface(curPrinterInterface);
            setPrintEnable(true);
        }

        IntelehealthApplication.getInstance().setRtPrinter(rtPrinter);
        rtPrinter = IntelehealthApplication.getInstance().getRtPrinter();
        textSetting = new TextSetting();

        intent = this.getIntent();
        if (intent != null) {
            //   prescData = Html.fromHtml(intent.getStringExtra("sms_prescripton")).toString();
            prescData = intent.getStringExtra("sms_prescripton");
            Log.d(TAG, "init: prescData : "+prescData);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                presTextview.setText(Html.fromHtml(removeNull(prescData), Html.FROM_HTML_MODE_COMPACT));
            } else {
                presTextview.setText(Html.fromHtml(removeNull(prescData)));
            }

           /* prescData = "    - Not Provided\n" +
                    "    ";*/
            doctorDetails = Html.fromHtml(intent.getStringExtra("doctorDetails")).toString();

            if (intent.getStringExtra("font-family") != null)
                fontFamily = Html.fromHtml(intent.getStringExtra("font-family")).toString();

            if (intent.getStringExtra("drSign-text") != null)
                drSignText = Html.fromHtml(intent.getStringExtra("drSign-text")).toString();

            if (intent.getStringExtra("signature") != null)
                base64String = Html.fromHtml(intent.getStringExtra("signature")).toString();
        }
        Log.e("pres:", "prescFinall:" + intent.getStringExtra("sms_prescripton") + intent.getStringExtra("doctorDetails"));

        String fontFamilyFile = "";
        if (fontFamily != null) {
            if (fontFamily.toLowerCase().equalsIgnoreCase("youthness")) {
                fontFamilyFile = "fonts/Youthness.ttf";
            } else if (fontFamily.toLowerCase().equalsIgnoreCase("asem")) {
                fontFamilyFile = "fonts/Asem.otf";
            } else if (fontFamily.toLowerCase().equalsIgnoreCase("arty")) {
                fontFamilyFile = "fonts/Arty.otf";
            } else if (fontFamily.toLowerCase().equalsIgnoreCase("almondita")) {
                fontFamilyFile = "fonts/Almondita.ttf";
            }
        }
        Bitmap bitmap = setBase64ToImageView();
        if(bitmap!=null){
            imgDrSign.setImageBitmap(bitmap);
        }
        /*if (fontFamily != null) {
            Typeface face = Typeface.createFromAsset(getAssets(), fontFamilyFile);
            drSignTextview.setTypeface(face);
        }

        drSignTextview.setTextSize(60f);
        drSignTextview.setIncludeFontPadding(false);
        drSignTextview.setTextColor(getResources().getColor(R.color.ink_pen));
        drSignTextview.setBackgroundColor(getResources().getColor(R.color.white));
        drSignTextview.setText(drSignText);
        drSignTextview.setDrawingCacheEnabled(true);
        drSignTextview.buildDrawingCache();
        drSignTextview.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        drSignTextview.layout(0, 0, drSignTextview.getMeasuredWidth(), drSignTextview.getMeasuredHeight());

        mBitmap = drSignTextview.getDrawingCache(); // converting Textview to Bitmap Image.*/

        //  pres_textview.setText(prescData);
        drDetailsTextview.setText(doctorDetails);
       /* Log.e("pres:", "prescFinal:" + presTextview.getText().toString() + drSignTextview.getText().toString() +
                drDetailsTextview.getText().toString());*/

//        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.doctor_sign);
//        showImage(uri);
    }

    private void textPrint() throws UnsupportedEncodingException {
        printStr = presTextview.getText().toString();

        if (TextUtils.isEmpty(printStr)) {
            printStr = "Hello Printer";
        }

        switch (IntelehealthApplication.getInstance().getCurrentCmdType()) {
            case BaseEnum.CMD_ESC:
                escPrint();
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_txtprint) {
            try {
                textPrint();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
                /*case R.id.btn_select_chartsetname:
                showSelectChartsetnameDialog();
                break;*/
        } else if (id == R.id.tv_device_selected) {
            showBluetoothDeviceChooseDialog(); // Here on click, will open the Dialog that will show all the nearby Bluetooth devices...
        } else if (id == R.id.btn_connect) {
            doConnect(); //Here on clicking will connect with the selected Bluetooth device...
        } else if (id == R.id.btn_disConnect) {
            doDisConnect();
        }
    }

    private void doDisConnect() {

        if (Integer.parseInt(tvDeviceSelected.getTag().toString()) == BaseEnum.NO_DEVICE) {
            return;
        }
        if (rtPrinter != null && rtPrinter.getPrinterInterface() != null) {
            rtPrinter.disConnect();
        }

        tvDeviceSelected.setText(getString(R.string.please_connect));
        tvDeviceSelected.setTag(BaseEnum.NO_DEVICE);
        //setPrintEnable(false);
        btnConnect.setEnabled(false);
        btnDisConnect.setEnabled(false);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isEnable) {
        if (compoundButton == ckSmallFont) {
            if (isEnable) {
                textSetting.setIsEscSmallCharactor(SettingEnum.Enable);
                spinEscFontType.setSelection(0);
            } else {
                textSetting.setIsEscSmallCharactor(SettingEnum.Disable);
            }
        }
        if (compoundButton == ckAntiWhite) {
            if (isEnable) {
                textSetting.setIsAntiWhite(SettingEnum.Enable);
            } else {
                textSetting.setIsAntiWhite(SettingEnum.Disable);
            }
        }
        if (compoundButton == ckDoubleWidth) {
            if (isEnable) {
                textSetting.setDoubleWidth(SettingEnum.Enable);
            } else {
                textSetting.setDoubleWidth(SettingEnum.Disable);
            }
        }
        if (compoundButton == ckDoubleHeight) {
            if (isEnable) {
                textSetting.setDoubleHeight(SettingEnum.Enable);
            } else {
                textSetting.setDoubleHeight(SettingEnum.Disable);
            }
        }
        if (compoundButton == ckBold) {
            if (isEnable) {
                textSetting.setBold(SettingEnum.Enable);
            } else {
                textSetting.setBold(SettingEnum.Disable);
            }
        }
        if (compoundButton == ckUnderline) {
            if (isEnable) {
                textSetting.setUnderline(SettingEnum.Enable);
            } else {
                textSetting.setUnderline(SettingEnum.Disable);
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
    }

    //This will open a Dialog that will show all the Bluetooth devices...
    private void showBluetoothDeviceChooseDialog() {
        BluetoothDeviceChooseDialog bluetoothDeviceChooseDialog = new BluetoothDeviceChooseDialog();
        bluetoothDeviceChooseDialog.setOnDeviceItemClickListener(
                new BluetoothDeviceChooseDialog.OnDeviceItemClickListener() {
                    @Override
                    public void onDeviceItemClick(BluetoothDevice device) {
                        if (TextUtils.isEmpty(device.getName())) {
                            tvDeviceSelected.setText(device.getAddress());
                        } else {
                            tvDeviceSelected.setText(device.getName() + " [" + device.getAddress() + "]");
                        }
                        configObj = new BluetoothEdrConfigBean(device);
                        tvDeviceSelected.setTag(BaseEnum.HAS_DEVICE);
                        isConfigPrintEnable(configObj);
                    }
                });

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth.
            Toast.makeText(TextPrintESCActivity.this,
                    getResources().getString(R.string.bluetooth_notsupported_device), Toast.LENGTH_SHORT).show();
            return;
        } else if (!mBluetoothAdapter.isEnabled()) {
            // Bluetooth is Turned OFF.
            Toast.makeText(TextPrintESCActivity.this,
                    getResources().getString(R.string.turn_on_bluetooth), Toast.LENGTH_SHORT).show();
            return;
        } else {
            // Bluetooth is Turned ON.
            bluetoothDeviceChooseDialog.show(TextPrintESCActivity.this.getSupportFragmentManager(), null);
        }

    }

    private void isConfigPrintEnable(Object configObj) {
        if (isInConnectList(configObj)) {
            setPrintEnable(true);
        } else {
            setPrintEnable(false);
        }
    }

    private void setPrintEnable(boolean isEnable) {
        // btn_txtprint.setEnabled(isEnable);
        btnConnect.setEnabled(!isEnable);
        btnDisConnect.setEnabled(isEnable);
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

        if (Integer.parseInt(tvDeviceSelected.getTag().toString()) == BaseEnum.NO_DEVICE) { // No device is selected.
            Toast.makeText(this, "Please select device", Toast.LENGTH_SHORT).show();
            //  showAlertDialog(getString(R.string.main_pls_choose_device));
            return;
        }

        pbConnect.setVisibility(View.VISIBLE);
        TimeRecordUtils.record("Start：", System.currentTimeMillis());
        BluetoothEdrConfigBean bluetoothEdrConfigBean = (BluetoothEdrConfigBean) configObj;
        connectBluetooth(bluetoothEdrConfigBean);
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


    public void showAlertDialog(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                android.app.AlertDialog.Builder dialog =
                        new android.app.AlertDialog.Builder(TextPrintESCActivity.this);
                dialog.setTitle("Please connect device");
                dialog.setMessage(msg);
                dialog.setNegativeButton(R.string.cancel, null);
                dialog.show();
            }
        });
    }

    @Override
    public void printerObserverCallback(final PrinterInterface printerInterface, final int state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pbConnect.setVisibility(View.GONE);
                // disconnect and connect button color change.
                if (state == CommonEnum.CONNECT_STATE_SUCCESS) {
                    Toast.makeText(TextPrintESCActivity.this, printerInterface.getConfigObject().toString()
                            + getString(R.string._main_connected), Toast.LENGTH_SHORT).show();
                } else if (state == CommonEnum.CONNECT_STATE_INTERRUPTED) {
                    if (printerInterface != null && printerInterface.getConfigObject() != null) {
                        Toast.makeText(TextPrintESCActivity.this, printerInterface.getConfigObject().toString()
                                        + getString(R.string._main_disconnect),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TextPrintESCActivity.this, getString(R.string._main_disconnect),
                                Toast.LENGTH_SHORT).show();
                    }
                }


                switch (state) {
                    case CommonEnum.CONNECT_STATE_SUCCESS:
                        TimeRecordUtils.record("RT连接end：", System.currentTimeMillis());
//                        Toast.makeText(TextPrintESCActivity.this, printerInterface.getConfigObject().toString()
//                                + getString(R.string._main_connected), Toast.LENGTH_SHORT).show();
//                        tvDeviceSelected.setText(printerInterface.getConfigObject().toString());
                        tvDeviceSelected.setTag(BaseEnum.HAS_DEVICE);
                        curPrinterInterface = printerInterface; // set current Printer Interface
                        printerInterfaceArrayList.add(printerInterface);
                        rtPrinter.setPrinterInterface(printerInterface);
                        setPrintEnable(true);
                        btnDisConnect.setEnabled(true);
                        break;
                    case CommonEnum.CONNECT_STATE_INTERRUPTED:
                        if (printerInterface != null && printerInterface.getConfigObject() != null) {
//                            Toast.makeText(TextPrintESCActivity.this, printerInterface.getConfigObject().toString()
//                                            + getString(R.string._main_disconnect),
//                                    Toast.LENGTH_SHORT).show();
                        } else {
//                            Toast.makeText(TextPrintESCActivity.this, getString(R.string._main_disconnect),
//                                    Toast.LENGTH_SHORT).show();
                        }
                        TimeRecordUtils.record("Time：", System.currentTimeMillis());
                        tvDeviceSelected.setText(R.string.please_connect);
                        tvDeviceSelected.setTag(BaseEnum.NO_DEVICE);
                        curPrinterInterface = null;
                        printerInterfaceArrayList.remove(printerInterface);
                        setPrintEnable(false);
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

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void escPrint() {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (rtPrinter != null && rtPrinter.getPrinterInterface() != null) {
                try {
                    CmdFactory escFac = new EscFactory();
                    Cmd escCmd = escFac.create();

                    escCmd.append(escCmd.getHeaderCmd());
                    escCmd.setChartsetName("UTF-8");

                    CommonSetting commonSetting = new CommonSetting();
                    escCmd.append(escCmd.getCommonSettingCmd(commonSetting));
                    String finalText = presTextview.getText().toString();
                    Bitmap bitmap = generateWrappedBitmap(finalText, drSignText, doctorDetails, fontFamily);

                    if (bitmap != null) {
                        BitmapSetting bitmapSetting = new BitmapSetting();
                        bitmapSetting.setBmpPrintMode(BmpPrintMode.MODE_SINGLE_COLOR);
                        bitmapSetting.setBimtapLimitWidth(576);  // 72mm paper = 576 dots
                        escCmd.append(escCmd.getBitmapCmd(bitmapSetting, bitmap));
                    }
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(new byte[]{0x1D, 0x56, 0x41, 0x10}); // Partial Cut

                    rtPrinter.writeMsgAsync(escCmd.getAppendCmds());

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(TextPrintESCActivity.this, "Print failed", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(TextPrintESCActivity.this, getResources().getString(R.string.tip_have_no_paired_device), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFontFilePath(String fontFamily) {
        if (fontFamily == null) return "";

        String fontFamilyFile = "";
        if (fontFamily.toLowerCase().equalsIgnoreCase("youthness")) {
            fontFamilyFile = "fonts/Youthness.ttf";
        } else if (fontFamily.toLowerCase().equalsIgnoreCase("asem")) {
            fontFamilyFile = "fonts/Asem.otf";
        } else if (fontFamily.toLowerCase().equalsIgnoreCase("arty")) {
            fontFamilyFile = "fonts/Arty.otf";
        } else if (fontFamily.toLowerCase().equalsIgnoreCase("almondita")) {
            fontFamilyFile = "fonts/Almondita.ttf";
        }
        return fontFamilyFile;
    }

    private Bitmap generateWrappedBitmap(String text, String drSignText, String doctorDetails, String fontFamily) {
        int width = 600;
        int height = 1400;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(22f);
        textPaint.setAntiAlias(true);
        StaticLayout staticLayout = new StaticLayout(
                text,
                textPaint,
                width - 40, // Width with margin
                Layout.Alignment.ALIGN_NORMAL,
                1.2f, // Line spacing multiplier for better readability
                0f,
                false
        );
        float currentYPosition = 20;
        canvas.translate(20, currentYPosition);
        staticLayout.draw(canvas);

        currentYPosition += staticLayout.getHeight() + 10; // Add space after the wrapped text

        try {
            // Decode Base64 and convert to Bitmap
            Bitmap signBitmap = setBase64ToImageView(); // Assuming this already returns the decoded Bitmap

            if (signBitmap != null) {
                Log.d(TAG, "Original Signature Size: " + signBitmap.getWidth() + "x" + signBitmap.getHeight());

                int desiredWidth = 120;  // or 300 based on your requirement
                float scaleRatio = (float) desiredWidth / signBitmap.getWidth();
                int desiredHeight = (int) (signBitmap.getHeight() * scaleRatio);

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(signBitmap, desiredWidth, desiredHeight, true);
                canvas.drawBitmap(scaledBitmap, 20, currentYPosition, null);

                // Update Y position
                currentYPosition += scaledBitmap.getHeight() + 10;

            } else {
                Log.e(TAG, "Signature bitmap is null.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

      /*  if (drSignText != null && !drSignText.isEmpty()) {
            try {
                Typeface customTypeface = Typeface.createFromAsset(getAssets(), "fonts/Almondita.ttf");
                Paint textPaint1 = new Paint();
                textPaint1.setTypeface(customTypeface);
                textPaint1.setTextSize(50f);
                textPaint1.setColor(Color.BLACK);
                textPaint1.setAntiAlias(true);
                textPaint1.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(drSignText, 20, currentYPosition, textPaint1);
                currentYPosition += textPaint1.descent() - textPaint1.ascent() + 10; // Decreased margin

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("SignatureError", "Error while drawing signature: " + e.getMessage());
            }
        }*/
        Log.d(TAG, "generateWrappedBitmap: doctorDetails : " + doctorDetails);

        if (doctorDetails != null && !doctorDetails.isEmpty()) {
            try {
                Paint textPaint2 = new Paint();
                textPaint2.setTextSize(22f);
                textPaint2.setColor(Color.BLACK);
                textPaint2.setAntiAlias(true);
                textPaint2.setTextAlign(Paint.Align.LEFT);
                String[] detailsLines = doctorDetails.split("\n");
                for (String line : detailsLines) {
                    canvas.drawText(line, 20, currentYPosition, textPaint2);
                    currentYPosition += textPaint2.descent() - textPaint2.ascent() + 5;  // Reduced gap between lines
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("DoctorDetailsError", "Error while drawing doctor's details: " + e.getMessage());
            }
        }
        return bitmap;
    }

    public Bitmap setBase64ToImageView() {
        try {
            // Remove "data:image..." prefix if it exists
            String base64Cleaned = base64String.contains("base64,")
                    ? base64String.substring(base64String.indexOf("base64,") + 7)
                    : base64String;
            byte[] decodedBytes = Base64.decode(base64Cleaned, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}


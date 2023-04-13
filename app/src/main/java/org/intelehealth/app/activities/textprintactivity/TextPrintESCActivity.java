package org.intelehealth.app.activities.textprintactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.rt.printerlibrary.utils.BitmapConvertUtil;
import com.rt.printerlibrary.utils.FuncUtils;
import com.rt.printerlibrary.utils.PrintListener;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeActivity;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.dialog.BluetoothDeviceChooseDialog;
import org.intelehealth.app.utilities.BaseEnum;
import org.intelehealth.app.utilities.TimeRecordUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class TextPrintESCActivity extends AppCompatActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener, PrinterObserver {

    private static final String TAG = TextPrintESCActivity.class.getSimpleName();
    private TextView pres_textview, drSign_textview, drDetails_textview;
    private Button btn_txtprint, btn_select_chartsetname;
    private CheckBox ck_smallfont, ck_anti_white, ck_double_width,
            ck_double_height, ck_bold, ck_underline;
    private RadioGroup rg_align_group;
    private Spinner spin_esc_font_type;
    private EditText et_linespacing;

    private String printStr;
    private TextSetting textSetting;
    private String mChartsetName = "UTF-8";
    private ESCFontTypeEnum curESCFontType = null;
    Intent intent;
    String prescData, doctorDetails, font_family, drSign_Text;
    // IntelehealthApplication application;
    private Bitmap mBitmap = null;
    private int bmpPrintWidth = 40;
    TextView tv_device_selected;
    Button btn_connect, btn_disConnect;
    private Object configObj;
    private ArrayList<PrinterInterface> printerInterfaceArrayList = new ArrayList<>();
    private ProgressBar pb_connect;
    private RTPrinter rtPrinter = null;
    private PrinterFactory printerFactory;
    public static PrinterInterface curPrinterInterface = null;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_print_escactivity);
        initView();
        addListener();
        init();
    }

    @SuppressLint("WrongViewCast")
    public void initView() {
        setTitle(getString(R.string.view_print));
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        mToolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        pres_textview = findViewById(R.id.pres_textview);
        drSign_textview = findViewById(R.id.drSign_textview);
        drDetails_textview = findViewById(R.id.drDetails_textview);
        btn_txtprint = findViewById(R.id.btn_txtprint);
//        ck_smallfont = findViewById(R.id.ck_smallfont);
//        ck_anti_white = findViewById(R.id.ck_anti_white);
//        ck_double_width = findViewById(R.id.ck_double_width);
//        ck_double_height = findViewById(R.id.ck_double_height);
//        ck_bold = findViewById(R.id.ck_bold);
//        ck_underline = findViewById(R.id.ck_underline);
//        rg_align_group = findViewById(R.id.rg_align_group);
//        btn_select_chartsetname = findViewById(R.id.btn_select_chartsetname);
//        spin_esc_font_type = findViewById(R.id.spin_esc_font_type);
//        et_linespacing = findViewById(R.id.et_linespacing);
        tv_device_selected = findViewById(R.id.tv_device_selected);
        btn_connect = findViewById(R.id.btn_connect);
        btn_disConnect = findViewById(R.id.btn_disConnect);
        pb_connect = findViewById(R.id.pb_connect);

        btn_disConnect.setBackgroundResource(R.drawable.bg_button_disable);
        btn_connect.setBackgroundResource(R.drawable.bg_button_disable);
    }

    public void addListener() {
        btn_txtprint.setOnClickListener(this);
        tv_device_selected.setOnClickListener(this);
        btn_connect.setOnClickListener(this);
        btn_disConnect.setOnClickListener(this);

//        btn_select_chartsetname.setOnClickListener(this);
//        ck_smallfont.setOnCheckedChangeListener(this);
//        ck_anti_white.setOnCheckedChangeListener(this);
//        ck_double_width.setOnCheckedChangeListener(this);
//        ck_double_height.setOnCheckedChangeListener(this);
//        ck_bold.setOnCheckedChangeListener(this);
//        ck_underline.setOnCheckedChangeListener(this);
//        rg_align_group.setOnCheckedChangeListener(this);


/*
        spin_esc_font_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                switch (pos) {
                    case 0:
                        curESCFontType = null;
                        ck_smallfont.setEnabled(true);
                        break;
                    case 1:
                        curESCFontType = ESCFontTypeEnum.FONT_A_12x24;
                        ck_smallfont.setChecked(false);
                        ck_smallfont.setEnabled(false);
                        break;
                    case 2:
                        curESCFontType = ESCFontTypeEnum.FONT_B_9x24;
                        ck_smallfont.setChecked(false);
                        ck_smallfont.setEnabled(false);
                        break;
                    case 3:
                        curESCFontType = ESCFontTypeEnum.FONT_C_9x17;
                        ck_smallfont.setChecked(false);
                        ck_smallfont.setEnabled(false);
                        break;
                    case 4:
                        curESCFontType = ESCFontTypeEnum.FONT_D_8x16;
                        ck_smallfont.setChecked(false);
                        ck_smallfont.setEnabled(false);
                        break;
                    default:
                        curESCFontType = null;
                        ck_smallfont.setEnabled(true);
                        break;
                }
                textSetting.setEscFontType(curESCFontType);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
*/
    }

    public void init() {
        IntelehealthApplication.getInstance().setCurrentCmdType(BaseEnum.CMD_ESC);
        // printerFactory = new UniversalPrinterFactory();
        printerFactory = new ThermalPrinterFactory();
        rtPrinter = printerFactory.create();
        PrinterObserverManager.getInstance().add(this);

        if(curPrinterInterface != null) {
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

        intent = this.getIntent();
        if (intent != null) {
            prescData = Html.fromHtml(intent.getStringExtra("sms_prescripton")).toString();
           /* prescData = "    - Not Provided\n" +
                    "    ";*/
            doctorDetails = Html.fromHtml(intent.getStringExtra("doctorDetails")).toString();
            font_family = Html.fromHtml(intent.getStringExtra("font-family")).toString();
            drSign_Text = Html.fromHtml(intent.getStringExtra("drSign-text")).toString();
        }
        Log.e("pres:", "prescFinall:" + intent.getStringExtra("sms_prescripton") + intent.getStringExtra("doctorDetails"));

        String fontFamilyFile = "";
        if (font_family != null && font_family != null) {
            if (font_family.toLowerCase().equalsIgnoreCase("youthness")) {
                fontFamilyFile = "fonts/Youthness.ttf";
            } else if (font_family.toLowerCase().equalsIgnoreCase("asem")) {
                fontFamilyFile = "fonts/Asem.otf";
            } else if (font_family.toLowerCase().equalsIgnoreCase("arty")) {
                fontFamilyFile = "fonts/Arty.otf";
            } else if (font_family.toLowerCase().equalsIgnoreCase("almondita")) {
                fontFamilyFile = "fonts/Almondita.ttf";
            }
        }

        Typeface face = Typeface.createFromAsset(getAssets(), fontFamilyFile);
        drSign_textview.setTypeface(face);
        drSign_textview.setTextSize(60f);
        drSign_textview.setIncludeFontPadding(false);
        drSign_textview.setTextColor(getResources().getColor(R.color.ink_pen));
        drSign_textview.setBackgroundColor(getResources().getColor(R.color.white));
        drSign_textview.setText(drSign_Text);
        drSign_textview.setDrawingCacheEnabled(true);
        drSign_textview.buildDrawingCache();
        drSign_textview.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        drSign_textview.layout(0, 0, drSign_textview.getMeasuredWidth(), drSign_textview.getMeasuredHeight());
        mBitmap = drSign_textview.getDrawingCache();  // converting Textview to Bitmap Image.

        pres_textview.setText(prescData);
        drDetails_textview.setText(doctorDetails);
        Log.e("pres:", "prescFinal:" + pres_textview.getText().toString() + drSign_textview.getText().toString() +
                drDetails_textview.getText().toString());

//        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.doctor_sign);
//        showImage(uri);
    }

    private void textPrint() throws UnsupportedEncodingException {
        printStr = pres_textview.getText().toString();

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

    /**
     * line spacing setting
     */
/*
    private int getInputLineSpacing() {
        String strLineSpacing = et_linespacing.getText().toString();
        if (TextUtils.isEmpty(strLineSpacing)) {
            strLineSpacing = "30";
            et_linespacing.setText(strLineSpacing);
        }
        int n = Integer.parseInt(strLineSpacing);
        if (n > 255) {
            n = 255;
        }
        return n;
    }
*/
    public static String stringToUnicode(String string) {
        StringBuffer unicode = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            // 取出每一个字符
            char c = string.charAt(i);
            // 转换为unicode
            //"\\u只是代号，请根据具体所需添加相应的符号"
            unicode.append("0x" + Integer.toHexString(c));
        }
        return unicode.toString();
    }


    private void escPrint() throws UnsupportedEncodingException {

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (rtPrinter != null) {
                    CmdFactory escFac = new EscFactory();
                    Cmd escCmd = escFac.create();

                    escCmd.append(escCmd.getHeaderCmd());// Initial //btnCmds = 2......
                    escCmd.setChartsetName(mChartsetName);
                    CommonSetting commonSetting = new CommonSetting();

                    BitmapSetting bitmapSetting = new BitmapSetting();
                    bitmapSetting.setBmpPrintMode(BmpPrintMode.MODE_SINGLE_COLOR);
                    bitmapSetting.setBimtapLimitWidth(bmpPrintWidth * 8);

                    Position txtposition = new Position(0, 0);
                    textSetting.setTxtPrintPosition(txtposition);
                    // textSetting.setAlign(CommonEnum.ALIGN_RIGHT);
                    // commonSetting.setEscLineSpacing(getInputLineSpacing());
                    escCmd.append(escCmd.getCommonSettingCmd(commonSetting));
                    try {
                        escCmd.append(escCmd.getTextCmd(textSetting, prescData));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());

                    try {
                        escCmd.append(escCmd.getBitmapCmd(bitmapSetting, mBitmap));
                    } catch (SdkException e) {
                        e.printStackTrace();
                    }
                    escCmd.append(escCmd.getLFCRCmd());

                    //here it prints 2nd time taking the position of the cursor where the priting ended above.
                    txtposition.x = 20;
                    textSetting.setTxtPrintPosition(txtposition);
                    try {
                        escCmd.append(escCmd.getTextCmd(textSetting, doctorDetails));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getHeaderCmd());
                    escCmd.append(escCmd.getLFCRCmd());

                    Log.i(TAG, FuncUtils.ByteArrToHex(escCmd.getAppendCmds()));
                    if(rtPrinter.getPrinterInterface() != null) {
                        // If without selecting Bluetooth user click Print button crash happens so added this condition.
                        rtPrinter.writeMsgAsync(escCmd.getAppendCmds());

                        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(TextPrintESCActivity.this);
                        alertdialogBuilder.setMessage(R.string.printing);
                        alertdialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });

                        androidx.appcompat.app.AlertDialog alertDialog = alertdialogBuilder.create();
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.setCancelable(false);
                        alertDialog.show();

                        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
                        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                        IntelehealthApplication.setAlertDialogCustomTheme(TextPrintESCActivity.this, alertDialog);
                    }
                    else {
                        Toast.makeText(TextPrintESCActivity.this, getResources().getString
                                (R.string.tip_have_no_paired_device), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

/*
    private void showSelectChartsetnameDialog() {
        final String[] chartsetNameArray = new String[]{"UTF-8", "GBK", "BIG5", "UCS2"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.dialog_title_chartset_setting);
        dialog.setItems(chartsetNameArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int pos) {
                mChartsetName = chartsetNameArray[pos];
                btn_select_chartsetname.setText(mChartsetName);
                if (pos == 3) {
                    mChartsetName = "UnicodeBigUnmarked";//UTF-16BE
                }
            }
        });
        dialog.setNegativeButton(R.string.dialog_cancel, null);
        dialog.show();
    }
*/


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_txtprint:
                try {
                    textPrint();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            /*case R.id.btn_select_chartsetname:
                showSelectChartsetnameDialog();
                break;*/
            case R.id.tv_device_selected:
                showBluetoothDeviceChooseDialog(); // Here on click, will open the Dialog that will show all the nearby Bluetooth devices...
                break;
            case R.id.btn_connect:
                doConnect(); //Here on clicking will connect with the selected Bluetooth device...
                break;
            case R.id.btn_disConnect:
                doDisConnect();
                break;
            default:
                break;
        }
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

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isEnable) {
        if (compoundButton == ck_smallfont) {
            if (isEnable) {
                textSetting.setIsEscSmallCharactor(SettingEnum.Enable);
                spin_esc_font_type.setSelection(0);
            } else {
                textSetting.setIsEscSmallCharactor(SettingEnum.Disable);
            }
        }
        if (compoundButton == ck_anti_white) {
            if (isEnable) {
                textSetting.setIsAntiWhite(SettingEnum.Enable);
            } else {
                textSetting.setIsAntiWhite(SettingEnum.Disable);
            }
        }
        if (compoundButton == ck_double_width) {
            if (isEnable) {
                textSetting.setDoubleWidth(SettingEnum.Enable);
            } else {
                textSetting.setDoubleWidth(SettingEnum.Disable);
            }
        }
        if (compoundButton == ck_double_height) {
            if (isEnable) {
                textSetting.setDoubleHeight(SettingEnum.Enable);
            } else {
                textSetting.setDoubleHeight(SettingEnum.Disable);
            }
        }
        if (compoundButton == ck_bold) {
            if (isEnable) {
                textSetting.setBold(SettingEnum.Enable);
            } else {
                textSetting.setBold(SettingEnum.Disable);
            }
        }
        if (compoundButton == ck_underline) {
            if (isEnable) {
                textSetting.setUnderline(SettingEnum.Enable);
            } else {
                textSetting.setUnderline(SettingEnum.Disable);
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
//        switch (i) {
//            case R.id.rb_align_left:
//                textSetting.setAlign(CommonEnum.ALIGN_LEFT);
//                break;
//            case R.id.rb_align_middle:
//                textSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
//                break;
//            case R.id.rb_align_right:
//                textSetting.setAlign(CommonEnum.ALIGN_RIGHT);
//                break;
//            default:
//                break;
//        }
    }

    private void showImage(Uri uri) {

        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
            System.gc();
        }
        try {
            mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (IntelehealthApplication.getInstance().getCurrentCmdType() == BaseEnum.CMD_ESC) {
            if (mBitmap.getWidth() > 48 * 8) {
                mBitmap = BitmapConvertUtil.decodeSampledBitmapFromUri(TextPrintESCActivity.this, uri, 48 * 8, 4000);
            }
        } else if (IntelehealthApplication.getInstance().getCurrentCmdType() == BaseEnum.CMD_PIN) {
            if (mBitmap.getWidth() > 210 * 8) {
                mBitmap = BitmapConvertUtil.decodeSampledBitmapFromUri(TextPrintESCActivity.this, uri, 210 * 8, 4000);
            }
        } else {
            if (mBitmap.getWidth() > 72 * 8) {
                mBitmap = BitmapConvertUtil.decodeSampledBitmapFromUri(TextPrintESCActivity.this, uri, 72 * 8, 4000);
            }
        }

//        Log.d(TAG, "mBitmap getWidth = " + mBitmap.getWidth());
//        Log.d(TAG, "mBitmap getHeight = " + mBitmap.getHeight());
        // ivImage.setImageBitmap(mBitmap);
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
        btn_connect.setEnabled(!isEnable);
        btn_disConnect.setEnabled(isEnable);

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
                pb_connect.setVisibility(View.GONE);
                // disconnect and connect button color change.
                if(state == CommonEnum.CONNECT_STATE_SUCCESS) {
                    Toast.makeText(TextPrintESCActivity.this, printerInterface.getConfigObject().toString()
                            + getString(R.string._main_connected), Toast.LENGTH_SHORT).show();
                }
                else if(state == CommonEnum.CONNECT_STATE_INTERRUPTED){
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
//                        tv_device_selected.setText(printerInterface.getConfigObject().toString());
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
//                            Toast.makeText(TextPrintESCActivity.this, printerInterface.getConfigObject().toString()
//                                            + getString(R.string._main_disconnect),
//                                    Toast.LENGTH_SHORT).show();
                        } else {
//                            Toast.makeText(TextPrintESCActivity.this, getString(R.string._main_disconnect),
//                                    Toast.LENGTH_SHORT).show();
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

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
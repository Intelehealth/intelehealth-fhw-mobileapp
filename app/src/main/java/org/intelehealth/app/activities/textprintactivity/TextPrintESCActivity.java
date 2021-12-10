package org.intelehealth.app.activities.textprintactivity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.rt.printerlibrary.bean.Position;
import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.EscFactory;
import com.rt.printerlibrary.enumerate.CommonEnum;
import com.rt.printerlibrary.enumerate.ESCFontTypeEnum;
import com.rt.printerlibrary.enumerate.SettingEnum;
import com.rt.printerlibrary.factory.cmd.CmdFactory;
import com.rt.printerlibrary.printer.RTPrinter;
import com.rt.printerlibrary.setting.CommonSetting;
import com.rt.printerlibrary.setting.TextSetting;
import com.rt.printerlibrary.utils.FuncUtils;

import org.intelehealth.app.R;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.utilities.BaseEnum;

import java.io.UnsupportedEncodingException;

public class TextPrintESCActivity extends AppCompatActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener{
    private static final String TAG = TextPrintESCActivity.class.getSimpleName();
    private ScrollEditText et_text;
    private Button btn_txtprint, btn_select_chartsetname;
    private CheckBox ck_smallfont, ck_anti_white, ck_double_width,
            ck_double_height, ck_bold, ck_underline;
    private RadioGroup rg_align_group;
    private Spinner spin_esc_font_type;
    private EditText et_linespacing;

    private RTPrinter rtPrinter;
    private String printStr;
    private TextSetting textSetting;
    private String mChartsetName = "UTF-8";
    private ESCFontTypeEnum curESCFontType = null;

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
        et_text = findViewById(R.id.et_text);
        btn_txtprint = findViewById(R.id.btn_txtprint);
        ck_smallfont = findViewById(R.id.ck_smallfont);
        ck_anti_white = findViewById(R.id.ck_anti_white);
        ck_double_width = findViewById(R.id.ck_double_width);
        ck_double_height = findViewById(R.id.ck_double_height);
        ck_bold = findViewById(R.id.ck_bold);
        ck_underline = findViewById(R.id.ck_underline);
        rg_align_group = findViewById(R.id.rg_align_group);
        btn_select_chartsetname = findViewById(R.id.btn_select_chartsetname);
        spin_esc_font_type = findViewById(R.id.spin_esc_font_type);
        et_linespacing = findViewById(R.id.et_linespacing);
    }

    public void addListener() {
        btn_txtprint.setOnClickListener(this);
        btn_select_chartsetname.setOnClickListener(this);

        ck_smallfont.setOnCheckedChangeListener(this);
        ck_anti_white.setOnCheckedChangeListener(this);
        ck_double_width.setOnCheckedChangeListener(this);
        ck_double_height.setOnCheckedChangeListener(this);
        ck_bold.setOnCheckedChangeListener(this);
        ck_underline.setOnCheckedChangeListener(this);
        rg_align_group.setOnCheckedChangeListener(this);

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
    }

    public void init() {
        rtPrinter = IntelehealthApplication.getRtPrinter();
        textSetting = new TextSetting();
    }

    private void textPrint() throws UnsupportedEncodingException {
        printStr = et_text.getText().toString();

        if (TextUtils.isEmpty(printStr)) {
            printStr = "Hello Printer";
        }

        switch (IntelehealthApplication.getCurrentCmdType()) {
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
        if (rtPrinter != null) {
            CmdFactory escFac = new EscFactory();
            Cmd escCmd = escFac.create();
            escCmd.append(escCmd.getHeaderCmd());//初始化, Initial
            //    escCmd.append(((EscCmd)escCmd).getPageMode(true));//设置为页模式
            //    escCmd.append(((EscCmd)escCmd).getPageArea(10,10,200,200));//设置为页模式的打印区域
            //      escCmd.append(((EscCmd)escCmd).getSetLeftStartSpacing(10*8));//左边留白 Leave white on the left(Unit: Point 1mm=8 points)
//            escCmd.append(((EscCmd)escCmd).getSetAreaWidth(76*8));//设置打印区域Set the print area (Unit: Point 1mm=8 points)
//            escCmd.append(((EscCmd)escCmd).getSetAreaWidth(104*8));//TODO  by FZP 由于下位机对于新版本RP410c的打印区域算法会溢出，所以针对此机型先屏蔽处理
            escCmd.setChartsetName(mChartsetName);

            CommonSetting commonSetting = new CommonSetting();
            Position txtposition = new  Position(0,0);
            textSetting.setTxtPrintPosition(txtposition);//如果没设置X值的偏移，就不要调用了

            commonSetting.setEscLineSpacing(getInputLineSpacing());
            escCmd.append(escCmd.getCommonSettingCmd(commonSetting));
            escCmd.append(escCmd.getTextCmd(textSetting, printStr));
            escCmd.append(escCmd.getLFCRCmd());
            //以下为设置X轴的函数的测试
            //注意如果txtposition.x>0,会和居中，对齐，冲突，不能共用
            txtposition.x = 160;//往右偏移160*0.125=20mm,txtposition.y目前没有用
            textSetting.setTxtPrintPosition(txtposition); //也可以用getSetXPosition来代替 escCmd.append(((EscCmd)escCmd).getSetXPosition(160));
            escCmd.append(escCmd.getTextCmd(textSetting, printStr));
            escCmd.append(escCmd.getLFCRCmd());
            //   escCmd.append(((EscCmd)escCmd).getPageEnd(true));
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getHeaderCmd());//初始化, Initial
            escCmd.append(escCmd.getLFCRCmd());
            Log.i(TAG, FuncUtils.ByteArrToHex(escCmd.getAppendCmds()));
            rtPrinter.writeMsgAsync(escCmd.getAppendCmds());
        }
    }

    private void showSelectChartsetnameDialog() {
        final String[] chartsetNameArray = new String[]{"UTF-8", "GBK", "BIG5","UCS2"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.dialog_title_chartset_setting);
        dialog.setItems(chartsetNameArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int pos) {
                mChartsetName = chartsetNameArray[pos];
                btn_select_chartsetname.setText(mChartsetName);
                if (pos==3){
                    mChartsetName ="UnicodeBigUnmarked";//UTF-16BE
                }
            }
        });
        dialog.setNegativeButton(R.string.dialog_cancel, null);
        dialog.show();
    }


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
            case R.id.btn_select_chartsetname:
                showSelectChartsetnameDialog();
                break;
            default:
                break;
        }
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
        switch (i) {
            case R.id.rb_align_left:
                textSetting.setAlign(CommonEnum.ALIGN_LEFT);
                break;
            case R.id.rb_align_middle:
                textSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
                break;
            case R.id.rb_align_right:
                textSetting.setAlign(CommonEnum.ALIGN_RIGHT);
                break;
            default:
                break;
        }
    }
}
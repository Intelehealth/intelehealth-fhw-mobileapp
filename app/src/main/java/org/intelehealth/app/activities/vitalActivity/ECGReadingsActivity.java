package org.intelehealth.app.activities.vitalActivity;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.ObservableBoolean;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.linktop.MonitorDataTransmissionManager;
import com.linktop.constant.Constants;
import com.linktop.constant.IUserProfile;
import com.linktop.infs.OnEcgResultListener;
import com.linktop.whealthService.MeasureType;
import com.linktop.whealthService.task.EcgTask;

import org.intelehealth.app.R;
import org.intelehealth.app.models.rhemos_device.ECG;
import org.intelehealth.app.models.rhemos_device.ECG_JsonModel;
import org.intelehealth.app.models.rhemos_device.UserProfile;
import org.intelehealth.app.services.HcService;
import org.intelehealth.app.widget.materialprogressbar.rhemos_widget.CountDownTextView;
import org.intelehealth.app.widget.materialprogressbar.rhemos_widget.WaveSurfaceView;
import org.intelehealth.app.widget.materialprogressbar.rhemos_widget.wave.ECGDrawWave;

import java.util.Locale;

public class ECGReadingsActivity extends AppCompatActivity implements OnEcgResultListener {
    private ECGDrawWave ecgDrawWave;
    protected ECG model;
    private final StringBuilder ecgWaveBuilder = new StringBuilder();
    private final ObservableBoolean toggleCountDown = new ObservableBoolean(false);
    private WaveSurfaceView wave_view;
    private CountDownTextView countDown_txt;
    private EcgTask mEcgTask;
    public HcService mHcService;
    private TextView finger_detection_txt, signal_quality_txt, r_r_interval_txt, heart_rate_txt, hrv_txt, respiratory_rate_txt,
            mood_txt, heart_age_txt, heart_beat_txt, robust_heart_rate_txt, stress_level_txt, click_to_see;
    private Button btn_measure, paper_speed_btn, gain_btn, btn_submit;
    private static final long timeout = 30L;
    private String patientName, patientBirthday, patientGender, patientHeight, patientWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecgreadings);

        initUI();
        fetchIntent();

        model = new ECG();
        ecgDrawWave = new ECGDrawWave();
        ecgDrawWave.setPaperSpeed(ECGDrawWave.PaperSpeed.VAL_25MM_PER_SEC);
        ecgDrawWave.setGain(ECGDrawWave.Gain.VAL_10MM_PER_MV);
        paper_speed_btn.setText(ecgDrawWave.getPaperSpeed().getDesc());
        gain_btn.setText(ecgDrawWave.getGain().getDesc());

        wave_view.setDrawWave(ecgDrawWave);
        wave_view.pause();


        click_to_see.setOnClickListener(v -> { openECGLarge();});
        countDown_txt.setOnCountDownFinishCallback(() -> {
            toggleCountDown.set(false);
            onEcgDuration(timeout);
        });

        btn_measure.setOnClickListener(v -> {
          //  showTestDialog(R.drawable.pulse_oximeter);
            clickMeasure();
        });

        btn_submit.setOnClickListener(v -> {
          //  showTestDialog(R.drawable.pulse_oximeter);
            captureAllValues();
        });

    }

    private void fetchIntent() {
        Intent intent = getIntent();
        patientName = intent.getStringExtra("patientName");
        patientBirthday = intent.getStringExtra("patientBirthday");
        patientGender = intent.getStringExtra("patientGender");
        patientHeight = intent.getStringExtra("patientHeight");
        patientWeight = intent.getStringExtra("patientWeight");

        if (patientHeight.equalsIgnoreCase(""))
            patientHeight = "0";

        if (patientWeight.equalsIgnoreCase(""))
            patientWeight = "0";
    }

    public static void toggleCountDown_(@NonNull CountDownTextView textView, boolean toggleCountDown) {
        if (toggleCountDown) {
            textView.start();
        } else {
            textView.cancel();
        }
    }
    private void captureAllValues() {
        @SuppressLint("WrongConstant")
        ECG_JsonModel jsonModel = new ECG_JsonModel(
                String.valueOf(model.getR2r()),
                String.valueOf(model.getHr()),
                String.valueOf(model.getHrv()),
                String.valueOf(model.getRr()),
                fetchMood(model.getMood()),
                String.valueOf(model.getHa()),
                String.valueOf(model.getHb()),
                String.valueOf(model.getRhr()),
                fetchStressLevel(model.getStress()));

        Gson gson = new Gson();
        String json = gson.toJson(jsonModel, ECG_JsonModel.class);
        Log.v("ECG", "ECG: " + json.toString());

        Intent returnIntent = new Intent();
        returnIntent.putExtra("result",json);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    private void initUI() {
        wave_view = findViewById(R.id.wave_view);
        click_to_see = findViewById(R.id.click_to_see);
        countDown_txt = findViewById(R.id.countDown_txt);
        paper_speed_btn = findViewById(R.id.paper_speed_btn);
        gain_btn = findViewById(R.id.gain_btn);
        finger_detection_txt = findViewById(R.id.finger_detection_txt);
        signal_quality_txt = findViewById(R.id.signal_quality_txt);
        r_r_interval_txt = findViewById(R.id.r_r_interval_txt);
        heart_rate_txt = findViewById(R.id.heart_rate_txt);
        hrv_txt = findViewById(R.id.hrv_txt);
        respiratory_rate_txt = findViewById(R.id.respiratory_rate_txt);
        mood_txt = findViewById(R.id.mood_txt);
        heart_age_txt = findViewById(R.id.heart_age_txt);
        heart_beat_txt = findViewById(R.id.heart_beat_txt);
        robust_heart_rate_txt = findViewById(R.id.robust_heart_rate_txt);
        stress_level_txt = findViewById(R.id.stress_level_txt);
        btn_measure = findViewById(R.id.btn_measure);
        btn_submit = findViewById(R.id.btn_submit);

        resetting_values();
    }

    private void resetting_values() {
        r_r_interval_txt.setText(getString(R.string.rr_interval_value, String.valueOf(0)));
        heart_rate_txt.setText(getString(R.string.hr_value, String.valueOf(0)));
        hrv_txt.setText(getString(R.string.hrv_value, String.valueOf(0)));
        respiratory_rate_txt.setText(getString(R.string.rr_value, String.valueOf(0)));
        mood_txt.setText(getString(R.string.mood_value, String.valueOf(0)));
        heart_age_txt.setText(getString(R.string.heart_age, String.valueOf(0)));
        heart_beat_txt.setText(getString(R.string.heart_beat, String.valueOf(0)));
        robust_heart_rate_txt.setText(getString(R.string.robust_heart_rate, String.valueOf(0)));
        stress_level_txt.setText(getString(R.string.stress_level, String.valueOf(0)));
    }

    private void onEcgDuration(long duration) {
        stopMeasure();
        model.setDuration(duration);
        model.setTs(System.currentTimeMillis() / 1000L);

        String ecgWave = ecgWaveBuilder.toString();
        ecgWave = ecgWave.substring(0, ecgWave.length() - 1);
        model.setWave(ecgWave);

      //  event.put(EVENT_FINGER_DETECT, "");
      //  resetState();
    }

    public void clickMeasure() {
        final MonitorDataTransmissionManager manager = MonitorDataTransmissionManager.getInstance();

        //判断手机是否和设备实现连接
        if (!manager.isConnected()) {
            Toast.makeText(ECGReadingsActivity.this, getString(R.string.please_connect_to_device), Toast.LENGTH_SHORT).show();
            //  toast(R.string.device_disconnect);
            return;
        }
        //判断设备是否在充电，充电时不可测量
        if (manager.isCharging()) {
            //  toast(R.string.charging);
            Toast.makeText(ECGReadingsActivity.this, getString(R.string.is_charging_please_wait), Toast.LENGTH_SHORT).show();
            return;
        }
        //判断是否测量中...
        if (manager.isMeasuring()) {
//            if (mPosition != 2) {//体温没有停止方法，当点击停止的是非体温时才执行停止
            //停止测量
            //  stopMeasure(testType);
            stopMeasure();
            Toast.makeText(ECGReadingsActivity.this, getString(R.string.start_measuring), Toast.LENGTH_SHORT).show();
            //设置ViewPager可滑动
            //  btnMeasure.setText(getString(R.string.start_measuring));
//            }
        } else {
            //   reset();
            //开始测量
            if (startMeasure()) {
                /*
                 * 请注意了：为了代码逻辑不会混乱，每一单项在测量过程中请确保用户无法通过任何途径
                 * (当然，如果用户强制关闭页面就不管了)切换至其他测量单项的界面，直到本项一次测量结束。
                 */
                //设置ViewPager不可滑动
                //  btnMeasure.setText(R.string.measuring);
                Toast.makeText(ECGReadingsActivity.this, R.string.measuring, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean startMeasure() {
     //   event.put(EVENT_FINGER_DETECT, "");
      //  event.put(EVENT_SQ, "");

        btn_measure.setText("Measuring...");

        countDown_txt.setCountDownParams(timeout);
        toggleCountDown_(countDown_txt, true);

        String[] dob_array = patientBirthday.split("-");
        int year = Integer.parseInt(dob_array[0]);
        int month = Integer.parseInt(dob_array[1]);
        int day = Integer.parseInt(dob_array[2]);
        Log.v("ECG_Dob", "ECG_DOB: " + year + " " + month + " " + day);

        int gender;
        if (patientGender.equalsIgnoreCase("M"))
            gender = UserProfile.MALE;
        else if (patientGender.equalsIgnoreCase("F"))
            gender = UserProfile.FEMALE;
        else
            gender = UserProfile.MALE;

        //   IUserProfile userProfileDefault = new UserProfile("ccl", UserProfile.MALE, 633715200, 170, 60);  // todo: pass actual user values here...
        IUserProfile userProfileDefault = new UserProfile();
        ((UserProfile) userProfileDefault).setBirthday(year, month, day);
        ((UserProfile) userProfileDefault).setUsername(patientName);
        ((UserProfile) userProfileDefault).setGender(gender);
        ((UserProfile) userProfileDefault).setHeight(Integer.parseInt(patientHeight));
        ((UserProfile) userProfileDefault).setWeight(Integer.parseInt(patientWeight));
        Log.v("ECG", "ECG User: " + userProfileDefault.toString());

        // ECG init...
        if (mHcService != null) {
            if (null == mHcService.getBleDevManager().getUserProfile()) {
                mHcService.getBleDevManager().setUserProfile(userProfileDefault);
            }
            mEcgTask = mHcService.getBleDevManager().getEcgTask();
            mEcgTask.setOnEcgResultListener(this);
            //Import user profile makes the result more accurate.
        } else {
            if (MonitorDataTransmissionManager.getInstance().getUserProfile() == null) {
                MonitorDataTransmissionManager.getInstance().setUserProfile(userProfileDefault);
            }
            MonitorDataTransmissionManager.getInstance().setOnEcgResultListener(this);
        }

        // ECG task perform...
        if (mEcgTask != null) {
            if (mEcgTask.isModuleExist()) {
                wave_view.reply();
                mEcgTask.start();
                return true;
            } else {
               // toast("This Device's ECG module is not exist.");
                Toast.makeText(this, "This Device's ECG module is not exist.", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            if (MonitorDataTransmissionManager.getInstance().isEcgModuleExist()) {
                wave_view.reply();
                MonitorDataTransmissionManager.getInstance().startMeasure(MeasureType.ECG
                        //if you want "onDrawWave(Object data)" output raw data,config this:
//                        , Pair.create(Constants.CONFIG_ECG_OUTPUT_RAW_DATA, true)
                        // if you want "onDrawWave(Object data)" output array data,config this:
//                        , Pair.create(Constants.CONFIG_ECG_OUTPUT_ARRAY_DATA, true)
                );
                return true;
            } else {
             //   toast("This Device's ECG module is not exist.");
                Toast.makeText(this, "This Device's ECG module is not exist.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
    }
    public void stopMeasure() {
        wave_view.pause();
        if (mEcgTask != null) {
            mEcgTask.stop();
        } else {
            MonitorDataTransmissionManager.getInstance().stopMeasure();
        }
        btn_measure.setText("Start Measuring");
    }

    public void openECGLarge() {
        Intent intent = new Intent(ECGReadingsActivity.this, ECGLargeActivity.class);
        intent.putExtra("paperSpeed", ecgDrawWave.getPaperSpeed());
        intent.putExtra("gain", ecgDrawWave.getGain());
        intent.putExtra("model", model);
        startActivity(intent);
    }

    @Override
    public void onDrawWave(Object data) {
        //将数据点在心电图控件里描绘出来
        if (data instanceof int[]) {
//            Log.e("CCL", "onDrawWave -> array");
            int[] rawDataArray = (int[]) data;
            for (int d : rawDataArray) {
                //将数据点在心电图控件里描绘出来
                ecgDrawWave.addData(d);
                //将数据点存入容器，查看大图使用
                ecgWaveBuilder.append(data).append(",");
            }
        } else if (data instanceof Integer) {
//            Log.e("CCL", "onDrawWave -> int");
            int d = (Integer) data;
            ecgDrawWave.addData(d);
            //将数据点存入容器，查看大图使用
            ecgWaveBuilder.append(data).append(",");
        }
    }

    @Override
    public void onSignalQuality(@Constants.ECGSignalQuality int quality) {
        String signal = formatSignalQuality(quality);
        signal_quality_txt.setText(getString(R.string.ecg_sq, signal));
    }

    private static String formatSignalQuality(@Constants.ECGSignalQuality int quality) {
        switch (quality) {
            case Constants.ECG_SQ_POOR:
                return "Poor";
            case Constants.ECG_SQ_MEDIUM:
                return "Medium";
            case Constants.ECG_SQ_GOOD:
                return "Good";
            case Constants.ECG_SQ_NOT_DETECTED:
                return "Not detected.";
            default:
                return "Unknown, code = " + quality;
        }
    }

    @Override
    public void onECGValues(@Constants.ECGKey int key, int value) {
        switch (key) {
            case Constants.ECG_KEY_HEART_RATE:
                model.setHr(value);
                heart_rate_txt.setText(getString(R.string.hr_value, String.valueOf(model.getHr())));
                break;
            case Constants.ECG_KEY_HRV:
                model.setHrv(value);
                hrv_txt.setText(getString(R.string.hrv_value, String.valueOf(model.getHrv())));
                break;
            case Constants.ECG_KEY_MOOD:
                model.setMood(value);
                formatMood(mood_txt, model.getMood());
                break;
            case Constants.ECG_KEY_RESPIRATORY_RATE://Respiratory rate.
                model.setRr(value);
                respiratory_rate_txt.setText(getString(R.string.rr_value, String.valueOf(model.getRr())));
                break;
            case Constants.ECG_KEY_R2R:
                model.setR2r(value);
                r_r_interval_txt.setText(getString(R.string.rr_interval_value, String.valueOf(model.getR2r())));
                break;
            case Constants.ECG_KEY_HEART_AGE:
                model.setHa(value);
                heart_age_txt.setText(getString(R.string.heart_age, String.valueOf(model.getHa())));
                break;
            case Constants.ECG_KEY_HEART_BEAT:
                model.setHb(value);
                heart_beat_txt.setText(getString(R.string.heart_beat, String.valueOf(model.getHb())));
                break;
            case Constants.ECG_KEY_ROBUST_HR:
                model.setRhr(value);
                robust_heart_rate_txt.setText(getString(R.string.robust_heart_rate, String.valueOf(model.getRhr())));
                break;
            case Constants.ECG_KEY_STRESS:
                model.setStress(value);
                formatStressLevel(stress_level_txt, value);
              //  stress_level_txt.setText(model.getStress());
                break;
            default:
                break;
        }
    }

    @Override
    public void onFingerDetection(boolean fingerDetected) {
        String value = fingerDetected ? getString(R.string.finger_detection_ok)
                : getString(R.string.finger_detection_fail);
        finger_detection_txt.setText(getString(R.string.finger_detection, value));

        if (fingerDetected) {
            if (!toggleCountDown.get()) {
                ecgDrawWave.clear();
                model.reset();
            }
            toggleCountDown.set(true);
        } else {
            toggleCountDown.set(false);
            stopMeasure();
            countDown_txt.cancel();
            resetting_values();
        }
    }

    /*
     * 点击设置时间基准(走纸速度)
     * 该值反应心电图x轴的幅度，设置的值这里没做保存，请自行保存，以便下次启动该页面时自动设置已保存的值
     * */
    public void clickSetPagerSpeed(View v) {
        int checkedItem = 0;
        final ECGDrawWave.PaperSpeed[] paperSpeeds = ECGDrawWave.PaperSpeed.values();
        for (int i = 0; i < paperSpeeds.length; i++) {
            if (paperSpeeds[i].equals(ecgDrawWave.getPaperSpeed())) {
                checkedItem = i;
                break;
            }
        }
        onShowSingleChoiceDialog(R.string.paper_speed, paperSpeeds, checkedItem
                , (dialog, which) -> {
                    ECGDrawWave.PaperSpeed paperSpeed = paperSpeeds[which];
                    ecgDrawWave.setPaperSpeed(paperSpeed);
                  //  event.put(EVENT_PAPER_SPEED, paperSpeed.getDesc());
                    paper_speed_btn.setText(paperSpeed.getDesc());
                    dialog.dismiss();
                });
    }

    /*
     * 点击设置增益
     * 该值反应心电图y轴的幅度，设置的值这里没做保存，请自行保存，以便下次启动该页面时自动设置已保存的值
     * */
    public void clickSetGain(View v) {
        int checkedItem = 0;
        final ECGDrawWave.Gain[] gains = ECGDrawWave.Gain.values();
        for (int i = 0; i < gains.length; i++) {
            if (gains[i].equals(ecgDrawWave.getGain())) {
                checkedItem = i;
                break;
            }
        }
        onShowSingleChoiceDialog(R.string.gain, gains, checkedItem
                , (dialog, which) -> {
                    ECGDrawWave.Gain gain = gains[which];
                    ecgDrawWave.setGain(gain);
                  //  event.put(EVENT_GAIN, gain.getDesc());
                    gain_btn.setText(gain.getDesc());
                    dialog.dismiss();
                });
    }

    private void onShowSingleChoiceDialog(@StringRes int titleResId, ECGDrawWave.ECGValImp[] arrays
            , int checkedItem, DialogInterface.OnClickListener listener) {
        int length = arrays.length;
        CharSequence[] items = new CharSequence[length];
        for (int i = 0; i < length; i++) {
            items[i] = arrays[i].getDesc();
        }
        new AlertDialog.Builder(ECGReadingsActivity.this)
                .setTitle(titleResId)
                .setSingleChoiceItems(items, checkedItem, listener)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }

    public static void formatMood(TextView view, int mood) {
        Resources res = view.getResources();
        String moodDesc;
        if (mood > 0 && mood <= 20) {
            moodDesc = res.getString(R.string.mood_desc_calm);
        } else if (mood > 20 && mood <= 40) {
            moodDesc = res.getString(R.string.mood_desc_relaxed);
        } else if (mood > 40 && mood <= 60) {
            moodDesc = res.getString(R.string.mood_desc_balanced);
        } else if (mood > 60 && mood <= 80) {
            moodDesc = res.getString(R.string.mood_desc_motivated);
        } else if (mood > 80 && mood <= 100) {
            moodDesc = res.getString(R.string.mood_desc_agitated);
        } else {
            moodDesc = "-";
        }
        view.setText(res.getString(R.string.mood_value, moodDesc));
    }
    public String fetchMood(int mood) {
        String moodDesc;
        if (mood > 0 && mood <= 20) {
            moodDesc = getString(R.string.mood_desc_calm);
        } else if (mood > 20 && mood <= 40) {
            moodDesc = getString(R.string.mood_desc_relaxed);
        } else if (mood > 40 && mood <= 60) {
            moodDesc = getString(R.string.mood_desc_balanced);
        } else if (mood > 60 && mood <= 80) {
            moodDesc = getString(R.string.mood_desc_motivated);
        } else if (mood > 80 && mood <= 100) {
            moodDesc = getString(R.string.mood_desc_agitated);
        } else {
            moodDesc = "-";
        }

        return moodDesc;
    }

    public static void formatStressLevel(@NonNull TextView textView, @Constants.ECGStressLevel int level) {
        String stress;
        switch (level) {
            case Constants.ECG_STRESS_LEVEL_INVALID:
                stress = "Invalid";
                break;
            case Constants.ECG_STRESS_LEVEL_NO:
                stress = "No";
                break;
            case Constants.ECG_STRESS_LEVEL_LOW:
                stress = "Low";
                break;
            case Constants.ECG_STRESS_LEVEL_MEDIUM:
                stress = "Medium";
                break;
            case Constants.ECG_STRESS_LEVEL_HIGH:
                stress = "High";
                break;
            case Constants.ECG_STRESS_LEVEL_VERY_HIGH:
                stress = "Very high";
                break;
            default:
                stress = "-";
                break;
        }
        textView.setText(String.format(Locale.getDefault(), "Stress level: %s", stress));
    }

    public String fetchStressLevel(@Constants.ECGStressLevel int level) {
        String stress;
        switch (level) {
            case Constants.ECG_STRESS_LEVEL_INVALID:
                stress = "Invalid";
                break;
            case Constants.ECG_STRESS_LEVEL_NO:
                stress = "No";
                break;
            case Constants.ECG_STRESS_LEVEL_LOW:
                stress = "Low";
                break;
            case Constants.ECG_STRESS_LEVEL_MEDIUM:
                stress = "Medium";
                break;
            case Constants.ECG_STRESS_LEVEL_HIGH:
                stress = "High";
                break;
            case Constants.ECG_STRESS_LEVEL_VERY_HIGH:
                stress = "Very high";
                break;
            default:
                stress = "-";
                break;
        }
        return stress;
    }

}
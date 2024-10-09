package org.intelehealth.app.activities.identificationActivity;

import static org.intelehealth.app.utilities.DialogUtils.patientRegistrationDialog;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.intelehealth.app.R;
import org.intelehealth.app.abdm.model.AbhaProfileResponse;
import org.intelehealth.app.abdm.model.OTPVerificationResponse;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.SessionManager;

import java.io.Serializable;
import java.util.Locale;

/**
 * Created by Prajwal Waingankar on 29/09/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class IdentificationActivity_New extends BaseActivity implements NetworkUtils.InternetCheckUpdateInterface {



    SessionManager sessionManager = null;
    private static final String TAG = IdentificationActivity_New.class.getSimpleName();
    PatientDTO patientdto = new PatientDTO();


    Context context;
    private String BlockCharacterSet_Others = "0123456789\\@$!=><&^*+€¥£`~";
    private String BlockCharacterSet_Name = "\\@$!=><&^*+\"\'€¥£`~";
    Intent i_privacy;
    String privacy_value;
    Patient patient1 = new Patient();
    String patientID_edit, screenName;
    TextView label;
    private Fragment_FirstScreen firstScreen;
    private Fragment_SecondScreen secondScreen;
    private Fragment_ThirdScreen thirdScreen;
    private ImageButton refresh;
    private NetworkUtils networkUtils;
    Intent intentRx;
    public static final String PAYLOAD = "payload";
    public static final String MOBILE_PAYLOAD = "mobile_payload";
    private OTPVerificationResponse otpVerificationResponse;
    private AbhaProfileResponse abhaProfileResponse;
    private String accessToken, xToken, txnId;
    private ObjectAnimator syncAnimator;
    private BroadcastReceiver syncBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (syncAnimator != null && syncAnimator.getCurrentPlayTime() > 200) {
                syncAnimator.cancel();
                syncAnimator.end();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        binding = ActivityIdentificationNewBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
        sessionManager = new SessionManager(this);
        firstScreen = new Fragment_FirstScreen();
        secondScreen = new Fragment_SecondScreen();
        thirdScreen = new Fragment_ThirdScreen();

        String language = sessionManager.getAppLanguage();
        Log.d("lang", "lang: " + language);
        //In case of crash still the org should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }

        setContentView(R.layout.activity_identification_new);

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        initUI();
        networkUtils = new NetworkUtils(this, this);

        intentRx = this.getIntent(); // The intent was passed to the activity
        if (intentRx != null) {
            accessToken = intentRx.getStringExtra("accessToken");
            xToken = intentRx.getStringExtra("xToken");
            txnId = intentRx.getStringExtra("txnId");

            if (intentRx.hasExtra("patientUuid")) {
                label.setText(R.string.update_patient_identification);
                patientID_edit = intentRx.getStringExtra("patientUuid");
                screenName = intentRx.getStringExtra("ScreenEdit");
                patient1.setUuid(patientID_edit);

                Bundle args = intentRx.getBundleExtra("BUNDLE");
                patientdto = (PatientDTO) args.getSerializable("patientDTO");

                // abdm - start
                if (args.containsKey(PAYLOAD)) {
                    otpVerificationResponse = (OTPVerificationResponse) args.getSerializable(PAYLOAD);
                } else if (args.containsKey(MOBILE_PAYLOAD)) {
                    abhaProfileResponse = (AbhaProfileResponse) args.getSerializable(MOBILE_PAYLOAD);
                }
                // abdm - end

                if (screenName.equalsIgnoreCase("personal_edit")) {
                    setscreen(firstScreen);
                } else if (screenName.equalsIgnoreCase("address_edit")) {
                    setscreen(secondScreen);
                } else if (screenName.equalsIgnoreCase("others_edit")) {
                    setscreen(thirdScreen);
                }

            }
            else {
                abdmAutoFillScreensWithValues(firstScreen, intentRx);
            }

            /*if (intentRx.hasExtra("payload")) {
                abdmAutoFillScreensWithValues(firstScreen, intentRx);
            }*/


        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(setLocale(newBase));
    }

    public Context setLocale(Context context) {
        SessionManager sessionManager1 = new SessionManager(context);
        String appLanguage = sessionManager1.getAppLanguage();
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        conf.setLocale(locale);
        context.createConfigurationContext(conf);
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
        return context;
    }

    private void abdmAutoFillScreensWithValues(Fragment fragment, Intent intent) {
        Bundle bundle = new Bundle();
        bundle.putString("accessToken", accessToken);
        bundle.putString("xToken", xToken);
        bundle.putString("txnId", txnId);

        if (intent.hasExtra(PAYLOAD)) {
            otpVerificationResponse = (OTPVerificationResponse) intent.getSerializableExtra(PAYLOAD);
            Log.d("abdmAutoFillScreensWithValues", "payload: " + otpVerificationResponse.toString());
            bundle.putSerializable(PAYLOAD, otpVerificationResponse);
        }
        else if (intent.hasExtra(MOBILE_PAYLOAD)) {
            abhaProfileResponse = (AbhaProfileResponse) intent.getSerializableExtra(MOBILE_PAYLOAD);
            Log.d("abdmAutoFillScreensWithValues", "payload_mobile: " + abhaProfileResponse.toString());
            bundle.putSerializable(MOBILE_PAYLOAD, abhaProfileResponse);
        }

        fragment.setArguments(bundle); // passing data to Fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_firstscreen, fragment).commit();
    }

    private void setscreen(Fragment fragment) {
        // Bundle data
        Bundle bundle = new Bundle();
        bundle.putSerializable("patientDTO", (Serializable) patientdto);
        Log.v(TAG, "reltion: " + patientID_edit);
        if (patientID_edit != null) {
            bundle.putString("patientUuid", patientID_edit);
        } else {
            bundle.putString("patientUuid", patientdto.getUuid());
        }
        bundle.putBoolean("fromFirstScreen", true);
        bundle.putBoolean("fromSecondScreen", true);
        bundle.putBoolean("fromThirdScreen", true);
        bundle.putBoolean("patient_detail", true);
        bundle.putString("accessToken", accessToken);
        bundle.putString("xToken", xToken);
        bundle.putString("txnId", txnId);

        if (otpVerificationResponse != null) {
            bundle.putSerializable(PAYLOAD, otpVerificationResponse);
        }
        else if (abhaProfileResponse != null) {
            bundle.putSerializable(MOBILE_PAYLOAD, abhaProfileResponse);
        }

        fragment.setArguments(bundle); // passing data to Fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_firstscreen, fragment).commit();
    }

    private void initUI() {
        i_privacy = getIntent();
        context = IdentificationActivity_New.this;
        label = findViewById(R.id.label);
        refresh = findViewById(R.id.refresh);
        privacy_value = i_privacy.getStringExtra("privacy"); //privacy_accept value retrieved from previous act.

        syncAnimator = ObjectAnimator.ofFloat(refresh, View.ROTATION, 0f, 359f).setDuration(1200);
        syncAnimator.setRepeatCount(ValueAnimator.INFINITE);
        syncAnimator.setInterpolator(new LinearInterpolator());

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_firstscreen, new Fragment_FirstScreen()).commit();

    }

    @Override
    public void onBackPressed() {
        cancelRegistration(null);
    }


    public void cancelRegistration(View view) {
        if(!intentRx.hasExtra("patientUuid")) {
            patientRegistrationDialog(context, getResources().getDrawable(R.drawable.close_patient_svg),
                    getResources().getString(R.string.close_patient_registration),
                    getResources().getString(R.string.sure_you_want_close_registration),
                    getResources().getString(R.string.yes), getResources().getString(R.string.no),
                    new DialogUtils.CustomDialogListener() {
                @Override
                public void onDialogActionDone(int action) {
                    if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
                        Intent intent = new Intent(context, HomeScreenActivity_New.class);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        }
        else
        {
            Intent intent = new Intent(this, PatientDetailActivity2.class);
            intent.putExtra("patientUuid", patientID_edit);
            intent.putExtra("tag", "searchPatient");
            intent.putExtra("privacy", "false");
            startActivity(intent);
            finish();
        }

    }

    public void syncNow(View view) {
        if (NetworkConnection.isOnline(this)) {
            new SyncUtils().syncBackground();
        }
        refresh.clearAnimation();
        syncAnimator.start();
    }

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        Log.d("TAG", "updateUIForInternetAvailability: ");
        if (isInternetAvailable) {
            refresh.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));
        } else {
            refresh.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //register receiver for internet check
        IntentFilter filter = new IntentFilter(AppConstants.SYNC_INTENT_ACTION);
        ContextCompat.registerReceiver(this, syncBroadcastReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        networkUtils.callBroadcastReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            unregisterReceiver(syncBroadcastReceiver);
            //unregister receiver for internet check
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
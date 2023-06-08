package org.intelehealth.unicef.activities.identificationActivity;

import static org.intelehealth.unicef.utilities.DialogUtils.patientRegistrationDialog;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.database.dao.ImagesDAO;
import org.intelehealth.unicef.database.dao.PatientsDAO;
import org.intelehealth.unicef.models.Patient;
import org.intelehealth.unicef.models.dto.PatientDTO;
import org.intelehealth.unicef.syncModule.SyncUtils;
import org.intelehealth.unicef.utilities.DialogUtils;
import org.intelehealth.unicef.utilities.NetworkConnection;
import org.intelehealth.unicef.utilities.NetworkUtils;
import org.intelehealth.unicef.utilities.SessionManager;

import java.io.Serializable;
import java.util.Locale;

/**
 * Created by Prajwal Waingankar on 29/09/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class IdentificationActivity_New extends AppCompatActivity implements NetworkUtils.InternetCheckUpdateInterface {
    // ActivityIdentificationNewBinding binding;
    Button nxt_btn_main, btn_back_firstscreen, btn_nxt_firstscreen;
    RelativeLayout relativeLayout;
    LinearLayout linearLayout;
    SessionManager sessionManager = null;
    private static final String TAG = IdentificationActivity_New.class.getSimpleName();
    PatientDTO patientdto = new PatientDTO();
    ImagesDAO imagesDAO = new ImagesDAO();
    PatientsDAO patientsDAO = new PatientsDAO();
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
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }

        setContentView(R.layout.activity_identification_new);

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        initUI();
        networkUtils = new NetworkUtils(this, this);

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            if (intent.hasExtra("patientUuid")) {
                label.setText(R.string.update_patient_identification);
                patientID_edit = intent.getStringExtra("patientUuid");
                screenName = intent.getStringExtra("ScreenEdit");
                patient1.setUuid(patientID_edit);

                Bundle args = intent.getBundleExtra("BUNDLE");
                patientdto = (PatientDTO) args.getSerializable("patientDTO");

                if (screenName.equalsIgnoreCase("personal_edit")) {
                    setscreen(firstScreen);
                } else if (screenName.equalsIgnoreCase("address_edit")) {
                    setscreen(secondScreen);
                } else if (screenName.equalsIgnoreCase("others_edit")) {
                    setscreen(thirdScreen);
                }

            }
        }

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
        fragment.setArguments(bundle); // passing data to Fragment

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_firstscreen, fragment)
                .commit();
    }

    private void initUI() {
        i_privacy = getIntent();
        context = IdentificationActivity_New.this;
        label = findViewById(R.id.label);
        refresh = findViewById(R.id.refresh);
        privacy_value = i_privacy.getStringExtra("privacy"); //privacy_accept value retrieved from previous act.

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_firstscreen, new Fragment_FirstScreen())
                .commit();

    }

    @Override
    public void onBackPressed() {
        cancelRegistration(null);
    }


    public void cancelRegistration(View view) {
        patientRegistrationDialog(context,
                getResources().getDrawable(R.drawable.close_patient_svg),
                getResources().getString(R.string.close_patient_registration),
                getResources().getString(R.string.sure_you_want_close_registration),
                getResources().getString(R.string.yes),
                getResources().getString(R.string.no), new DialogUtils.CustomDialogListener() {
                    @Override
                    public void onDialogActionDone(int action) {
                        if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK)
                            finish();
                    }
                });
    }

    public void syncNow(View view) {
        if (NetworkConnection.isOnline(this)) {
            new SyncUtils().syncBackground();
//            Toast.makeText(this, getString(R.string.sync_strated), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        Log.d("TAG", "updateUIForInternetAvailability: ");
        if (isInternetAvailable) {
            refresh.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));
        }
        else {
            refresh.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //register receiver for internet check
        networkUtils.callBroadcastReceiver();
    }
    @Override
    public void onStop() {
        super.onStop();
        try {
            //unregister receiver for internet check
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
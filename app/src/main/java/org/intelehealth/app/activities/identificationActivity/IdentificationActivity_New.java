package org.intelehealth.app.activities.identificationActivity;

import static org.intelehealth.app.utilities.DialogUtils.patientRegistrationDialog;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.SessionManager;

import java.io.Serializable;
import java.util.Locale;

/**
 * Created by Prajwal Waingankar on 29/09/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class IdentificationActivity_New extends AppCompatActivity {
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
    String patientID_edit, patient_detail;
    TextView label;
    private Fragment_FirstScreen firstScreen;
    private Fragment_SecondScreen secondScreen;
    private Fragment_ThirdScreen thirdScreen;

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

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            if (intent.hasExtra("patientUuid")) {
                label.setText(R.string.update_patient_identification);
                patientID_edit = intent.getStringExtra("patientUuid");
                patient_detail = intent.getStringExtra("patient_detail");
                patient1.setUuid(patientID_edit);

                Bundle args = intent.getBundleExtra("BUNDLE");
                patientdto = (PatientDTO) args.getSerializable("patientDTO");

                if (patient_detail.equalsIgnoreCase("personal_edit")) {
                    setscreen(firstScreen);
                }
                else if (patient_detail.equalsIgnoreCase("address_edit")) {
                    setscreen(secondScreen);
                }
                else if (patient_detail.equalsIgnoreCase("others_edit")) {
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
        }
        else {
            bundle.putString("patientUuid", patientdto.getUuid());
        }
        bundle.putBoolean("fromSecondScreen", true);
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
        privacy_value = i_privacy.getStringExtra("privacy"); //privacy_accept value retrieved from previous act.

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_firstscreen, new Fragment_FirstScreen())
                .commit();

    }

    @Override
    public void onBackPressed() {
        patientRegistrationDialog(context,
                getResources().getDrawable(R.drawable.close_patient_svg),
                "Close patient registration?",
                "Are you sure you want to close the patient registration?",
                "No",
                "Yes", new DialogUtils.CustomDialogListener() {
                    @Override
                    public void onDialogActionDone(int action) {

                    }
                });
    }

}
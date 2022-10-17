package org.intelehealth.app.activities.identificationActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeFragment_New;
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.CustomEditText;
import org.intelehealth.app.utilities.SessionManager;

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
    String patientID_edit;
    EditText firstname_edittext, middlename_edittext, lastname_edittext, postalcode_edittext;
    private Fragment_FirstScreen fragment_1;
    private Fragment_SecondScreen fragment_2;
    private Fragment_ThirdScreen fragment_3;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        binding = ActivityIdentificationNewBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
        sessionManager = new SessionManager(this);
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

      /*  getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_firstscreen, new HomeFragment_New())
                .commit();*/
        // TODO: update later correctly. create fragment_regs_firstscren class extends fragment

        initUI();

//        nxt_btn_main.setOnClickListener(v -> {
//            fragment_1 = new Fragment_FirstScreen();
//            Bundle bundle = new Bundle();
//         //   bundle.putString("");
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.frame_firstscreen, new Fragment_SecondScreen())
//                    .commit();
//
//            relativeLayout.setVisibility(View.GONE);
//            linearLayout.setVisibility(View.VISIBLE);
//        });
//
//        btn_back_firstscreen.setOnClickListener(v -> {
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.frame_firstscreen, new Fragment_FirstScreen())
//                    .commit();
//
//            relativeLayout.setVisibility(View.VISIBLE);
//            linearLayout.setVisibility(View.GONE);
//        });
//
//        btn_nxt_firstscreen.setOnClickListener(v -> {
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.frame_firstscreen, new Fragment_ThirdScreen())
//                    .commit();
//
//        });


    }

    private void initUI() {
        i_privacy = getIntent();
        context = IdentificationActivity_New.this;
        privacy_value = i_privacy.getStringExtra("privacy"); //privacy_accept value retrieved from previous act.

//        nxt_btn_main = findViewById(R.id.nxt_btn_main);
//        btn_back_firstscreen = findViewById(R.id.btn_back_firstscreen);
//        btn_nxt_firstscreen = findViewById(R.id.btn_nxt_firstscreen);
//        relativeLayout = findViewById(R.id.relative_firstscreen_nxt_btn);
//        linearLayout = findViewById(R.id.linear_secondscreen_nxt_btn);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_firstscreen, new Fragment_FirstScreen())
                .commit();

        firstname_edittext = findViewById(R.id.firstname_edittext);
        middlename_edittext = findViewById(R.id.middlename_edittext);
        lastname_edittext = findViewById(R.id.lastname_edittext);
        postalcode_edittext = findViewById(R.id.postalcode_edittext);

    }
}
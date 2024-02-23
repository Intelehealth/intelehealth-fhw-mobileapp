package org.intelehealth.app.abdm.activity;

import static org.intelehealth.app.utilities.DialogUtils.patientRegistrationDialog;
import static org.intelehealth.app.utilities.DialogUtils.showOKDialog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.identificationActivity.IdentificationActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.WindowsUtils;

import java.util.Locale;

public class ConsentActivity extends AppCompatActivity {
    private Button btn_accept_privacy;
    private Context context = ConsentActivity.this;
    public static final String ABHA_CONSENT = "ABHA_CONSENT";
    public static final String hasABHA = "hasABHA";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent);
        WindowsUtils.setStatusBarColor(ConsentActivity.this);   // changing status bar color

        btn_accept_privacy = findViewById(R.id.btn_accept_privacy); // ACCEPT BTN
        ImageView ivBack = findViewById(R.id.iv_back_arrow_terms);

        // check internet - start
        if (!NetworkConnection.isOnline(ConsentActivity.this)) {    // no internet.
            showOKDialog(context, getDrawable(R.drawable.ui2_ic_warning_internet),
                    getString(R.string.error_network), getString(R.string.you_need_an_active_internet_connection_to_use_this_feature),
                    getString(R.string.ok), new DialogUtils.CustomDialogListener() {
                        @Override
                        public void onDialogActionDone(int action) {
                            if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
                                // take user to Identification activity.
                                declinePP(null);
                            }
                        }
                    });
        }
        // check internet - end

        ivBack.setOnClickListener(v -> {
            finish();
        });

        btn_accept_privacy.setOnClickListener(v -> {
            Intent intent = new Intent(context, AadharMobileVerificationActivity.class);
            intent.putExtra(hasABHA, false);
            startActivity(intent);
        });

    }

    public void declinePP(View view) {  // DECLINE BTN
      //  setResult(AppConstants.CONSENT_DECLINE);
        Intent intent = new Intent(this, IdentificationActivity_New.class); // ie. normal flow.
        intent.putExtra(ABHA_CONSENT, false);
        startActivity(intent);
        finish();
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

}
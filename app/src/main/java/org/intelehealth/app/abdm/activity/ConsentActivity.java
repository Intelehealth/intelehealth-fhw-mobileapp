package org.intelehealth.app.abdm.activity;

import static org.intelehealth.app.utilities.DialogUtils.patientRegistrationDialog;
import static org.intelehealth.app.utilities.DialogUtils.showOKDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.checkbox.MaterialCheckBox;

import org.intelehealth.app.R;
import org.intelehealth.app.abdm.adapter.CheckboxAdapter;
import org.intelehealth.app.abdm.model.CheckBoxRecyclerModel;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.activities.identificationActivity.IdentificationActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.ProviderDAO;
import org.intelehealth.app.models.dto.ProviderDTO;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.WindowsUtils;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConsentActivity extends AppCompatActivity {
    private Button btn_accept_privacy;
    private Context context = ConsentActivity.this;
    public static final String ABHA_CONSENT = "ABHA_CONSENT";
    public static final String hasABHA = "hasABHA";
    private NestedScrollView nsvAbhaConsent;
    private RecyclerView rvAbhaConsent;
    private CheckboxAdapter checkboxAdapter;
    private List<CheckBoxRecyclerModel> modelList;
    public static final String NEW_LINE = "<br>";
    private SessionManager sessionManager;
    private boolean allCheckboxesChecked = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent);
        WindowsUtils.setStatusBarColor(ConsentActivity.this);   // changing status bar color

        btn_accept_privacy = findViewById(R.id.btn_accept_privacy); // ACCEPT BTN
        ImageView ivBack = findViewById(R.id.iv_back_arrow_terms);
        nsvAbhaConsent = findViewById(R.id.nsvAbhaConsent);
        rvAbhaConsent = findViewById(R.id.rvAbhaConsent);
        sessionManager = new SessionManager(context);

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

        modelList = new ArrayList<>();
        modelList.add(new CheckBoxRecyclerModel(getString(R.string.abha_consent_line1) + NEW_LINE, false));
        modelList.add(new CheckBoxRecyclerModel(getString(R.string.abha_consent_line2) + NEW_LINE, false));
        modelList.add(new CheckBoxRecyclerModel(getString(R.string.abha_consent_line3) + NEW_LINE, false));
        modelList.add(new CheckBoxRecyclerModel(getString(R.string.abha_consent_line4) + NEW_LINE, false));
        modelList.add(new CheckBoxRecyclerModel(getString(R.string.abha_consent_line5) + NEW_LINE, false));
        modelList.add(new CheckBoxRecyclerModel(String.format(getString(R.string.abha_consent_line6), fetchHwFullName()) + NEW_LINE, false));
        modelList.add(new CheckBoxRecyclerModel(getString(R.string.abha_consent_line7) + NEW_LINE, false));
        checkboxAdapter = new CheckboxAdapter(context, modelList, new CheckboxAdapter.OnCheckboxChecked() {
            @Override
            public void onOptionChecked(CheckBoxRecyclerModel model) {
                if (checkboxAdapter != null) {
                    boolean allChecked = ((CheckboxAdapter) rvAbhaConsent.getAdapter()).areAllItemsChecked();
                    if (allChecked) {
                        btn_accept_privacy.setEnabled(true);
                        btn_accept_privacy.setBackground(getDrawable(R.drawable.ui2_common_primary_bg));
                    }
                    else {
                        btn_accept_privacy.setEnabled(false);
                        btn_accept_privacy.setBackground(getDrawable(R.drawable.ui2_bg_disabled_time_slot));
                    }
                }
            }
        });
        rvAbhaConsent.setLayoutManager(new LinearLayoutManager(context));
        rvAbhaConsent.setAdapter(checkboxAdapter);

        ivBack.setOnClickListener(v -> {
            finish();
        });

        btn_accept_privacy.setOnClickListener(v -> {
            Intent intent = new Intent(context, AadharMobileVerificationActivity.class);
            intent.putExtra(hasABHA, false);
            startActivity(intent);
        });

    }

    public boolean isValidField(String fieldName) {
        if (fieldName != null && !fieldName.isEmpty() && !fieldName.equals("null")) {
            return true;
        } else {
            return false;
        }
    }
    private String fetchHwFullName() {
        try {
            ProviderDAO providerDAO = new ProviderDAO();
            ProviderDTO providerDTO = providerDAO.getLoginUserDetails(sessionManager.getProviderID());
            if (providerDTO != null) {
                boolean firstname = isValidField(providerDTO.getFamilyName());
                boolean lastname = isValidField(providerDTO.getGivenName());
                String userFullName = "";
                if (firstname && lastname) {
                    userFullName = providerDTO.getGivenName() + " " + providerDTO.getFamilyName();
                } else if (firstname) {
                    userFullName = providerDTO.getGivenName();
                } else if (lastname) {
                    userFullName = providerDTO.getFamilyName();
                }
                return userFullName;
                }
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return "(health worker)";
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
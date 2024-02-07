package org.intelehealth.app.abdm.activity;

import static org.intelehealth.app.utilities.DialogUtils.patientRegistrationDialog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.identificationActivity.IdentificationActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.WindowsUtils;

public class ConsentActivity extends AppCompatActivity {
    private Button btn_accept_privacy;
    private Context context = ConsentActivity.this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent);

        // changing status bar color
        WindowsUtils.setStatusBarColor(ConsentActivity.this);

        btn_accept_privacy = findViewById(R.id.btn_accept_privacy); // ACCEPT BTN
        ImageView ivBack = findViewById(R.id.iv_back_arrow_terms);

        ivBack.setOnClickListener(v -> {
            finish();
        });

        btn_accept_privacy.setOnClickListener(v -> {
            patientRegistrationDialog(context, getDrawable(R.drawable.dialog_icon_complete),
                    getString(R.string.abha_number), getString(R.string.do_you_have_your_abha_number),
                    getResources().getString(R.string.yes), getResources().getString(R.string.no),
                    new DialogUtils.CustomDialogListener() {
                        @Override
                        public void onDialogActionDone(int action) {
                          //  Intent intent = new Intent(context, AbhaAddressSuggestionsActivity.class); // todo: testing purpose -> comment later.
                          //  Intent intent = new Intent(context, AccountSelectionLoginActivity.class); // todo: testing purpose -> comment later.
                            Intent intent = new Intent(context, AadharMobileVerificationActivity.class);  // todo: uncomment later.
                            if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK)
                                intent.putExtra("hasABHA", true);   // ie. Aadhar OR Mobile api to call. // here either Aadhar or Mobile apis be used.
                            else
                                intent.putExtra("hasABHA", false);  // ie. Aadhar AND Mobile api to call. // here Aadhar api is used.
                            // todo: testing purpose -> uncomment later this block.

                            startActivity(intent);
                            finish();
                        }
                    });

        });

    }

    public void declinePP(View view) {  // DECLINE BTN
        setResult(AppConstants.CONSENT_DECLINE);
        Intent intent = new Intent(this, IdentificationActivity_New.class); // ie. normal flow.
        startActivity(intent);
        finish();
    }

}
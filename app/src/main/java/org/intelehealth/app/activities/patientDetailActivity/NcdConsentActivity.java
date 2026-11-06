package org.intelehealth.app.activities.patientDetailActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.onboarding.GenericWebViewClient;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.model.CommonVisitData;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.WebViewStatus;

/**
 * NCD screening consent, shown after the Teleconsultation consent for patients aged 30+.
 * Accept or Decline both start the visit; Decline just skips the NCD Eligibility step.
 */
public class NcdConsentActivity extends BaseActivity implements WebViewStatus {

    private AlertDialog loadingDialog;
    private CommonVisitData mCommonVisitData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teleconsultation_consent);
        mCommonVisitData = getIntent().getExtras().getParcelable("CommonVisitData");
        ((TextView) findViewById(R.id.tv_consent_title)).setText(R.string.ncd_screening);

        loadingDialog = new DialogUtils().showCommonLoadingDialog(this, getString(R.string.loading), getString(R.string.please_wait));
        String text = "<html><body style='color:black;font-size: 0.8em;' >"
                + "<p>" + getString(R.string.ncd_consent_placeholder_text) + "</p>"
                + "<p>" + getString(R.string.ncd_consent_question) + "</p>"
                + "</body></html>";
        WebView consentWebView = findViewById(R.id.content_tv);
        consentWebView.setWebViewClient(new GenericWebViewClient(this));
        consentWebView.loadDataWithBaseURL(null, text, "text/html", "utf-8", null);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(Color.WHITE);
        findViewById(R.id.iv_back_arrow_terms).setOnClickListener(v -> finish());
    }

    public void declineConsent(View view) {
        startVisit(false);
    }

    public void acceptConsent(View view) {
        startVisit(true);
    }

    private void startVisit(boolean ncdConsentAccepted) {
        Intent intent = new Intent(this, VisitCreationActivity.class);
        mCommonVisitData.setIntentTag("new");
        intent.putExtra("CommonVisitData", mCommonVisitData);
        intent.putExtra(VisitCreationActivity.EXTRA_NCD_CONSENT_ACCEPTED, ncdConsentAccepted);
        startActivity(intent);
        finish();
    }

    @Override
    public void onPageStarted() {
        loadingDialog.show();
    }

    @Override
    public void onPageFinish() {
        loadingDialog.dismiss();
    }

    @Override
    public void onPageError(@NonNull String error) {
        loadingDialog.dismiss();
    }
}

package org.intelehealth.ezazi.activities.epartogramActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;


import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.ui.BaseActionBarActivity;
import org.intelehealth.ezazi.ui.dialog.ConfirmationDialogFragment;
import org.intelehealth.ezazi.widget.materialprogressbar.CustomProgressDialog;

public class EpartogramViewActivity extends BaseActionBarActivity {
    private WebView webView;
    private String patientUuid, visitUuid;
    private static final String URL = "https://ezazi.intelehealth.org/intelehealth/index.html#/epartogram/";
    //    https://ezazi.intelehealth.org/intelehealth/index.html#/dashboard/visit-summary/af35030a-cbf0-426c-9c61-4b9677ccb3b2
    // "df07db0d-d9b9-4597-a9e5-d62d3cff3d45/705397d4-0c62-4f26-bd53-2dd8523d5d1b";
    private SwipeRefreshLayout mySwipeRefreshLayout;
    private ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;
    private CustomProgressDialog customProgressDialog;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_epartogram_ezazi);
        super.onCreate(savedInstanceState);

        Intent intent = this.getIntent();
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientuuid");
            visitUuid = intent.getStringExtra("visituuid");
        }
        Log.v("epartog", "epratog: " + "puid: " + patientUuid + "--" + " vuid: " + visitUuid);

        webView = findViewById(R.id.webview_epartogram);
        mySwipeRefreshLayout = (SwipeRefreshLayout) this.findViewById(R.id.swipeContainer);
        customProgressDialog = new CustomProgressDialog(EpartogramViewActivity.this);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setDomStorageEnabled(true);

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
        webView.setVisibility(View.VISIBLE);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                mySwipeRefreshLayout.setRefreshing(false);
                if (customProgressDialog.isShowing()) {
                    customProgressDialog.dismiss();
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (customProgressDialog.isShowing()) {
                    customProgressDialog.dismiss();
                    webView.setVisibility(View.GONE);
                    showPageLoadingErrorDialog();
                }
            }
        });

        customProgressDialog.show();
        webView.loadUrl(URL + visitUuid);
        Log.v("epartog", "webviewUrl: " + URL + visitUuid);
        mySwipeRefreshLayout.setOnRefreshListener(() -> webView.reload());

    }

    private void showPageLoadingErrorDialog() {
        ConfirmationDialogFragment dialogFragment = new ConfirmationDialogFragment.Builder(this)
                .content(getString(R.string.content_webview_page_loading_issue))
                .positiveButtonLabel(R.string.retry_again)
                .negativeButtonLabel(R.string.action_exit)
                .build();

        dialogFragment.setListener(new ConfirmationDialogFragment.OnConfirmationActionListener() {
            @Override
            public void onAccept() {
                webView.reload();
            }

            @Override
            public void onDecline() {
                ConfirmationDialogFragment.OnConfirmationActionListener.super.onDecline();
                finish();
            }
        });

        dialogFragment.show(getSupportFragmentManager(), dialogFragment.getClass().getCanonicalName());
    }

    @Override
    protected int getScreenTitle() {
        return 0;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mySwipeRefreshLayout.getViewTreeObserver().addOnScrollChangedListener(mOnScrollChangedListener =
                () -> {
                    if (webView.getScrollY() == 0)
                        mySwipeRefreshLayout.setEnabled(true);
                    else
                        mySwipeRefreshLayout.setEnabled(false);

                });
    }


    @Override
    protected void onStop() {
        super.onStop();
        mySwipeRefreshLayout.getViewTreeObserver().removeOnScrollChangedListener(mOnScrollChangedListener);
    }
}
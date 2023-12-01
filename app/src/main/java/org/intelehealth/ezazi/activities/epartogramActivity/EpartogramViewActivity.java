package org.intelehealth.ezazi.activities.epartogramActivity;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


import com.github.ajalt.timberkt.Timber;

import org.intelehealth.ezazi.BuildConfig;
import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.ui.elcg.HtmlJSInterface;
import org.intelehealth.ezazi.ui.shared.BaseActionBarActivity;
import org.intelehealth.ezazi.ui.dialog.ConfirmationDialogFragment;
import org.intelehealth.ezazi.utilities.NetworkConnection;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.widget.materialprogressbar.CustomProgressDialog;

import java.io.File;

import io.socket.utf8.UTF8;

public class EpartogramViewActivity extends BaseActionBarActivity {
    private WebView webView;
    private static final String TAG = "EpartogramViewActivity";

    private String patientUuid, visitUuid;
    private static final String URL = BuildConfig.SERVER_URL + "/intelehealth/index.html#/epartogram/";
    //    https://ezazi.intelehealth.org/intelehealth/index.html#/dashboard/visit-summary/af35030a-cbf0-426c-9c61-4b9677ccb3b2
    // "df07db0d-d9b9-4597-a9e5-d62d3cff3d45/705397d4-0c62-4f26-bd53-2dd8523d5d1b";
    private SwipeRefreshLayout mySwipeRefreshLayout;
    private ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;

    private String webArchiveFileDir;
    private SessionManager sessionManager;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_epartogram_ezazi);
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        webArchiveFileDir = getBaseContext().getCacheDir().getAbsolutePath() + File.separator;
        Timber.tag(TAG).d("webArchive =>%s", webArchiveFileDir);
        Intent intent = this.getIntent();
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientuuid");
            visitUuid = intent.getStringExtra("visituuid");
        }
        Log.v("epartog", "epratog: " + "puid: " + patientUuid + "--" + " vuid: " + visitUuid);

        webView = findViewById(R.id.webview_epartogram);
        mySwipeRefreshLayout = (SwipeRefreshLayout) this.findViewById(R.id.swipeContainer);

        webView.setWebViewClient(webViewClient);
        webView.setSaveEnabled(true);
        HtmlJSInterface htmlJSInterface = new HtmlJSInterface();
        webView.addJavascriptInterface(htmlJSInterface, HtmlJSInterface.EXECUTOR_PAGE_SAVER);

        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.getSettings().setUserAgentString("Android");

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");

        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setDomStorageEnabled(true);

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
        webView.setVisibility(View.VISIBLE);


        if (NetworkConnection.isOnline(this)) webView.loadUrl(URL + visitUuid);
        else if (!sessionManager.getLCGContentFile(visitUuid).isEmpty()) {
            String webArchiveFile = webArchiveFileDir + sessionManager.getLCGContentFile(visitUuid);
            Timber.tag(TAG).d("Offline => %s", webArchiveFile);
//            webView.loadUrl(webArchiveFile);
//            webView.loadData(htmlJSInterface.getHtml(), "text/html", null);
            webView.loadDataWithBaseURL(null, htmlJSInterface.getHtml(), "text/html", "UTF-8", null);
        } else {
            webView.setVisibility(View.GONE);
            Toast.makeText(this, getString(R.string.please_connect_to_internet), Toast.LENGTH_LONG).show();
        }

        Log.v("epartog", "webviewUrl: " + URL + visitUuid);
        mySwipeRefreshLayout.setOnRefreshListener(() -> webView.reload());
    }

    private final WebViewClient webViewClient = new WebViewClient() {

        @Override
        public void onPageFinished(WebView view, String url) {
            mySwipeRefreshLayout.setRefreshing(false);
            if (NetworkConnection.isOnline(EpartogramViewActivity.this)) {
                String fileName = visitUuid + ".mht";
                sessionManager.setLCGContentFile(fileName, visitUuid);
                view.saveWebArchive(webArchiveFileDir + fileName);
                view.loadUrl(HtmlJSInterface.jsFunction());
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.i("WEB_VIEW_TEST", "error code:" + errorCode);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.i("WEB_VIEW_TEST", "error code:" + error.getErrorCode());
            }
            super.onReceivedError(view, request, error);
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.i("WEB_VIEW_TEST", "error code:" + errorResponse.getStatusCode());
            }
            super.onReceivedHttpError(view, request, errorResponse);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
        }
    };

    private void handleError() {
        webView.setVisibility(View.GONE);
        showPageLoadingErrorDialog();
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
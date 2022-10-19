package org.intelehealth.ezazi.activities.epartogramActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;



import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.widget.materialprogressbar.CustomProgressDialog;

public class EpartogramViewActivity extends AppCompatActivity {
    WebView webView;
    String patientUuid, visitUuid;
    Intent intent;
    public static final String URL = "https://ezazi.intelehealth.org/intelehealth/index.html#/epartogram/";
    // "df07db0d-d9b9-4597-a9e5-d62d3cff3d45/705397d4-0c62-4f26-bd53-2dd8523d5d1b";
    private SwipeRefreshLayout mySwipeRefreshLayout;
    private ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;
    CustomProgressDialog customProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epartogram);

        intent = this.getIntent();
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

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);


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
                }
            }
        });

        customProgressDialog.show();
        webView.loadUrl(URL + patientUuid + "/" + visitUuid);
        Log.v("epartog", "webviewUrl: " + URL + patientUuid + "/" + visitUuid);
        
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        webView.reload();
                    }
                });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mySwipeRefreshLayout.getViewTreeObserver().addOnScrollChangedListener(mOnScrollChangedListener =
                new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        if (webView.getScrollY() == 0)
                            mySwipeRefreshLayout.setEnabled(true);
                        else
                            mySwipeRefreshLayout.setEnabled(false);

                    }
                });
    }


    @Override
    protected void onStop() {
        super.onStop();
        mySwipeRefreshLayout.getViewTreeObserver().removeOnScrollChangedListener(mOnScrollChangedListener);
    }
}
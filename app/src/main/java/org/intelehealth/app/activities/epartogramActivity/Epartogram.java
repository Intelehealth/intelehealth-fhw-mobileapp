package org.intelehealth.app.activities.epartogramActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.intelehealth.app.R;

public class Epartogram extends AppCompatActivity {
    WebView webView;
    String patientUuid, visitUuid;
    Intent intent;
    public static final String  URL = "https://ezazi.intelehealth.org/intelehealth/index.html#/epartogram/";
           // "df07db0d-d9b9-4597-a9e5-d62d3cff3d45/705397d4-0c62-4f26-bd53-2dd8523d5d1b";
           private SwipeRefreshLayout mySwipeRefreshLayout;
    private ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epartogram);

        intent = this.getIntent();
        if(intent != null) {
            patientUuid = intent.getStringExtra("patientuuid");
            visitUuid = intent.getStringExtra("visituuid");
        }

        webView = findViewById(R.id.webview_epartogram);
        mySwipeRefreshLayout = (SwipeRefreshLayout)this.findViewById(R.id.swipeContainer);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(true);
//        webView.setInitialScale(1);
//        webView.getSettings().setLoadWithOverviewMode(true);
//        webView.getSettings().setUseWideViewPort(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                mySwipeRefreshLayout.setRefreshing(false);
            }
        });
        webView.loadUrl(URL + patientUuid + "/" + visitUuid);


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
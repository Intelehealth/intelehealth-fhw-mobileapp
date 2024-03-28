package org.intelehealth.app.activities.onboarding

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.LocaleList
import android.util.Log
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import org.intelehealth.app.R
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.WebViewStatus
import java.util.Locale

class CommonWebViewActivity : AppCompatActivity(), WebViewStatus{
    private var webView: WebView? = null
    private var ivBack: ImageView? = null
    private var titleTv: TextView? = null
    private var errorTv: TextView? = null
    private var loadingDialog: AlertDialog? = null
    private var title = ""
    private var type = ""
    private var url = ""
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_webview_activity)
        WindowCompat.getInsetsController(window,window.decorView).isAppearanceLightStatusBars = true
        window.statusBarColor = Color.WHITE

        val intentData = intent.extras
        intentData?.let {
            title = it.getString(GenericWebViewClient.TITLE,"")
            type = it.getString(GenericWebViewClient.TYPE,"")
            url = it.getString(GenericWebViewClient.URL,"")
        }

        ivBack = findViewById(R.id.iv_back_arrow)
        titleTv = findViewById(R.id.title_tv)
        errorTv = findViewById(R.id.error_tv)
        webView = findViewById(R.id.webview)

        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.javaScriptCanOpenWindowsAutomatically = true
        webView?.webViewClient = CommonWebViewClient(this)

        titleTv?.text = title

        loadingDialog = DialogUtils().showCommonLoadingDialog(
            this,
            getString(R.string.loading),
            getString(R.string.please_wait)
        )

        webView?.loadUrl(url)

        ivBack?.setOnClickListener { v: View? -> finish() }
    }

    fun declineCon(view: View?) {
        setResult(AppConstants.PERSONAL_CONSENT_DECLINE)
        finish()
    }

    fun acceptCon(view: View?) {
        setResult(AppConstants.PERSONAL_CONSENT_ACCEPT)
        finish()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(setLocale(newBase))
    }

    fun setLocale(context: Context): Context? {
        val sessionManager1 = SessionManager(context)
        val appLanguage = sessionManager1.appLanguage
        val res = context.resources
        val conf = res.configuration
        val locale = Locale(appLanguage)
        Locale.setDefault(locale)
        conf.setLocale(locale)
        context.createConfigurationContext(conf)
        val dm = res.displayMetrics
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(LocaleList(locale))
        } else {
            conf.setLocale(locale)
        }
        res.displayMetrics.setTo(dm)
        res.configuration.setTo(conf)
        return context
    }

    override fun onPageStarted() {
        webView?.visibility = View.VISIBLE
        errorTv?.visibility = View.GONE
        loadingDialog?.show()
    }

    override fun onPageFinish() {
        loadingDialog?.dismiss()
    }

    override fun onPageError(error: String) {
        webView?.visibility = View.GONE
        errorTv?.visibility = View.VISIBLE
    }
}

class CommonWebViewClient(private var webViewStatus: WebViewStatus) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return false;
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        webViewStatus.onPageStarted()
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        webViewStatus.onPageFinish()
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        webViewStatus.onPageError(error.toString())
    }

}
package org.intelehealth.app.activities.onboarding

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import org.intelehealth.app.R
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.FileUtils
import org.intelehealth.app.utilities.NetworkConnection
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.WebViewStatus
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.Locale
import java.util.Objects
/**
 * Created by Tanvir Hasan on 28-03-2024 : 12-48.
 * Email: mhasan@intelehealth.org
 *
 * this is the common webview to load in app web link
 * need to pass some intent value to it like
 * title, url, sessionManagerKey
 */
class CommonWebViewActivity : AppCompatActivity(), WebViewStatus{
    private var webView: WebView? = null
    private var ivBack: ImageView? = null
    private var titleTv: TextView? = null
    private var errorTv: TextView? = null
    private var loadingDialog: AlertDialog? = null
    private var title = ""
    private var url = ""
    private var sessionManagerKey = ""
    lateinit var sessionManager: SessionManager
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_webview_activity)
        WindowCompat.getInsetsController(window,window.decorView).isAppearanceLightStatusBars = true
        window.statusBarColor = Color.WHITE

        val intentData = intent.extras
        intentData?.let {
            title = it.getString(GenericWebViewClient.TITLE,"")
            url = it.getString(GenericWebViewClient.URL,"")
            sessionManagerKey = it.getString(GenericWebViewClient.KEY,"")
        }

        sessionManager = SessionManager(this)

        ivBack = findViewById(R.id.iv_back_arrow)
        titleTv = findViewById(R.id.title_tv)
        errorTv = findViewById(R.id.error_tv)
        webView = findViewById(R.id.webview)

        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.javaScriptCanOpenWindowsAutomatically = true
        webView?.webViewClient = CommonWebViewClient(this)

        titleTv?.text = title

        backPress()

        loadingDialog = DialogUtils().showCommonLoadingDialog(
            this,
            getString(R.string.loading),
            getString(R.string.please_wait)
        )

        //if network available then loading data directly
        if(NetworkConnection.isCapableNetwork(this)){
            webView?.loadUrl(url)
            saveHtmlToSessionManager()
        } else{ //if network not available then getting data from session manager
            loadFromLocal()
        }

        ivBack?.setOnClickListener { v: View? -> handleBackPress() }
    }

    private fun loadFromLocal() {
        val localHtml = sessionManager.getHtml(sessionManagerKey)
        if(localHtml.isNotEmpty()){
            webView?.loadData(localHtml, "text/html", "utf-8")
        }else{//if data not available on session manager get data from asset
            loadAssetHtml(sessionManagerKey)
        }
    }

    private fun backPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        })
    }

    private fun handleBackPress() {
        if (webView?.canGoBack() == true) {
            webView?.goBack();
        } else {
            finish()
        }
    }

    /**
     * saving html to session manager if network is available
     */
    private fun saveHtmlToSessionManager() {
        val htmlResponseObservable =
            AppConstants.apiInterface.GET_HTML(url)

        htmlResponseObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ResponseBody> {
                override fun onSubscribe(d: Disposable) {}
                override fun onNext(responseBody: ResponseBody) {
                    val doc: Document = Jsoup.parse(responseBody.string())
                    val element: Element? = doc.getElementById("primary")
                    element?.let {
                        sessionManager.setHtml(sessionManagerKey,it.html().replace("#",""))
                    }
                }

                override fun onError(e: Throwable) {
                }

                override fun onComplete() {}
            })
    }

    /**
     * loading html from local
     * if its not exist on session manager
     */
    private fun loadAssetHtml(sessionManagerKey: String) {
        var assetHtml: String
        Thread {
            try {
                val obj = JSONObject(
                    Objects.requireNonNullElse(
                        FileUtils.readFileRoot(
                            AppConstants.CONFIG_FILE_NAME,
                            this
                        ),
                        FileUtils.encodeJSON(
                            this,
                            AppConstants.CONFIG_FILE_NAME
                        ).toString()
                    )
                )
                assetHtml =  obj.getString(sessionManagerKey.lowercase()).replace("#","")
            } catch (e: JSONException) {
                throw RuntimeException(e)
            }
            runOnUiThread {
                webView?.loadData( assetHtml , "text/html", "utf-8")
                //webView?.loadDataWithBaseURL(null, assetHtml , "text/html", "utf-8", null);
            }
        }.start()
    }

    fun okBtClick(view: View?) {
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

        (request?.url?.toString() ?: "").also {
            if (it.lowercase().contains("mailto")) {
                (webViewStatus as Context).startActivity(Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(it)
                })
            }else{
                return if(!NetworkConnection.isCapableNetwork(webViewStatus as  Context)){
                    Toast.makeText(webViewStatus as Context,(webViewStatus as Context).getString(R.string.no_network),Toast.LENGTH_SHORT).show()
                    true
                }else{
                    false
                }
            }
        }
        return true
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
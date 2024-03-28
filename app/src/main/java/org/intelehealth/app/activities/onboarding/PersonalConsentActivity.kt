package org.intelehealth.app.activities.onboarding

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import org.intelehealth.app.R
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.FileUtils
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.WebViewStatus
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale
import java.util.Objects


class PersonalConsentActivity : AppCompatActivity(), WebViewStatus {
    var obj: JSONObject? = null
    private var personal_consent_string = ""
    private var webView: WebView? = null
    var ivBack: ImageView? = null
    private val context: Context = this
    private var sessionManager: SessionManager? = null
    private var loadingDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_personal_consent)
        //  mIntentFrom = getIntent().getIntExtra("IntentFrom", 0);
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            true

        window.statusBarColor = Color.WHITE

        ivBack = findViewById(R.id.iv_back_arrow_terms)
        webView = findViewById(R.id.consent_webview)
        sessionManager = SessionManager(context)

        webView?.webViewClient = GenericWebViewClient(this)

        loadingDialog = DialogUtils().showCommonLoadingDialog(
            this,
            getString(R.string.loading),
            getString(R.string.please_wait)
        )

        ivBack?.setOnClickListener { v: View? -> finish() }

        if (personal_consent_string.isEmpty()) {
            Thread {

                // bg task
                try {
                    obj = JSONObject(
                        Objects.requireNonNullElse(
                            FileUtils.readFileRoot(
                                AppConstants.CONFIG_FILE_NAME,
                                context
                            ),
                            FileUtils.encodeJSON(
                                context,
                                AppConstants.CONFIG_FILE_NAME
                            ).toString()
                        )
                    ) //Load the config file
                    personal_consent_string = if (sessionManager!!.appLanguage
                            .equals("hi", ignoreCase = true)
                    )
                    //currently english is defaut
                        obj!!.getString("personalDataConsentText_Hindi")
                    else obj!!.getString("personalDataConsentText_English")
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
                runOnUiThread {
                    webView?.loadData(personal_consent_string, "text/html", "utf-8")
                }
            }.start()
        } else {
            webView?.loadData(personal_consent_string, "text/html", "utf-8")
        }
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
        loadingDialog?.show()
    }

    override fun onPageFinish() {
        loadingDialog?.dismiss()
    }

    override fun onPageError(error: String) {
        TODO("Not yet implemented")
    }
}
package org.intelehealth.app.activities.onboarding

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.text.util.Linkify
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import org.intelehealth.app.R
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.FileUtils
import org.intelehealth.app.utilities.SessionManager
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale
import java.util.Objects

class PersonalConsentActivity : AppCompatActivity() {
    var obj: JSONObject? = null
    var privacy_string = ""
    var tvText: TextView? = null
    var ivBack: ImageView? = null
    private val context: Context = this
    private var sessionManager: SessionManager? = null
    private var loadingDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_personal_consent)
        //  mIntentFrom = getIntent().getIntExtra("IntentFrom", 0);
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.WHITE
        }

        ivBack = findViewById(R.id.iv_back_arrow_terms)
        tvText = findViewById(R.id.tv_term_condition)
        sessionManager = SessionManager(context)

        loadingDialog = DialogUtils().showCommonLoadingDialog(
            this,
            getString(R.string.loading),
            getString(R.string.please_wait)
        )
        loadingDialog?.show()

        ivBack?.setOnClickListener { v: View? -> finish() }

        if (privacy_string.isEmpty()) {
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
                    privacy_string = if (sessionManager!!.appLanguage
                            .equals("hi", ignoreCase = true)
                    )
                        //currently english is defaut
                        obj!!.getString("personalDataConsentText_English") else obj!!.getString("personalDataConsentText_English")
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
                runOnUiThread {

                    // ui task
                    tvText?.autoLinkMask = Linkify.ALL
                    tvText?.text = HtmlCompat.fromHtml(
                        privacy_string,
                        HtmlCompat.FROM_HTML_MODE_COMPACT
                    )
                    loadingDialog?.dismiss()
                }
            }.start()
        } else {
            tvText?.text = HtmlCompat.fromHtml(privacy_string, HtmlCompat.FROM_HTML_MODE_COMPACT)
            loadingDialog?.dismiss()
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
            conf.locale = locale
        }
        res.updateConfiguration(conf, dm)
        return context
    }
}
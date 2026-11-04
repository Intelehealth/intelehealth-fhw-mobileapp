package org.intelehealth.app.activities.onboarding

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import org.intelehealth.app.R
import org.intelehealth.app.utilities.NetworkConnection
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.WebViewStatus


/**
 * Created by Tanvir Hasan on 28-03-2024 : 12-48.
 * Email: mhasan@intelehealth.org
 *
 * generic webview client to handle webview behaviour
 */
class GenericWebViewClient(private var context: Context) : WebViewClient() {
    companion object {
        val TITLE: String = "TITLE"
        val TYPE: String = "TYPE"
        val URL: String = "URL"
        val KEY: String = "KEY"
        val PRIVACY_POLICY: String = "Privacy Policy"
        val TERMS_OF_USE: String = "Terms Of Use"
        var PERSONAL_DATA_PROCESSING_POLICY = "Personal Data Processing  Policy"
    }

    /**
     * handling all url click here
     */
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        (request?.url?.toString() ?: "").also {
            val intent = Intent(context, CommonWebViewActivity::class.java)
            if (it.contains("intelehealth.org/privacy-policy")) {
                intent.apply {
                    putExtra(TITLE, PRIVACY_POLICY)
                    putExtra(KEY, SessionManager.PRIVACY_POLICY)
                    putExtra(URL, it)
                }
                context.startActivity(intent)
            } else if (it.contains("intelehealth.org/terms-of-use")) {
                intent.apply {
                    putExtra(TITLE, TERMS_OF_USE)
                    putExtra(KEY, SessionManager.TERMS_OF_USE)
                    putExtra(URL, it)
                }
                context.startActivity(intent)
            } else if (it.contains("intelehealth.org/personal-data-processing-policy")) {
                intent.apply {
                    putExtra(TITLE, PERSONAL_DATA_PROCESSING_POLICY)
                    putExtra(KEY, SessionManager.PERSONAL_DATA_PROCESSING_POLICY)
                    putExtra(URL, it)
                }
                context.startActivity(intent)
            }
            //if clicking link is pdf then opening the link on browser
            else if (it.lowercase().contains(".pdf") ||
                it.lowercase().contains("mailto")
            ) {
                context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(it)
                })
            } else {
                //if url type is other then loading the url on same webview if network is available
                return if (!NetworkConnection.isCapableNetwork(context)) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.no_network),
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                } else {
                    false
                }
            }
            return true
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        (context as WebViewStatus).onPageStarted()
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        (context as WebViewStatus).onPageFinish()
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        (context as WebViewStatus).onPageError(error.toString())
    }
}
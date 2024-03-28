package org.intelehealth.app.activities.onboarding

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import org.intelehealth.app.utilities.WebViewStatus

/**
 * Created by Tanvir Hasan on 28-03-2024 : 12-48.
 * Email: mhasan@intelehealth.org
 */
class GenericWebViewClient(private var context: Context) : WebViewClient() {
    companion object {
        val TITLE: String = "TITLE"
        val TYPE: String = "TYPE"
        val URL: String = "URL"
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        (request?.url?.toString() ?: "").also {
            val intent = Intent(context, CommonWebViewActivity::class.java)
            if (it.contains("https://www.intelehealth.org/privacy-policy")) {
                intent.apply {
                    putExtra(TITLE, "Privacy Policy")
                    putExtra(TYPE, "online")
                    putExtra(URL, it)
                }
            } else if (it.contains("https://intelehealth.org/terms-of-use")) {
                intent.apply {
                    putExtra(TITLE, "Terms Of Use")
                    putExtra(TYPE, "online")
                    putExtra(URL, it)
                }
            }
            context.startActivity(intent)
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
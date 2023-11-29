package org.intelehealth.ezazi.ui.elcg;

import android.webkit.JavascriptInterface;

import com.github.ajalt.timberkt.Timber;

import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.utilities.FileUtils;
import org.intelehealth.ezazi.utilities.SessionManager;

/**
 * Created by Vaghela Mithun R. on 22-11-2023 - 17:22.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class HtmlJSInterface {
    private static final String TAG = "HtmlJSInterface";
    public static final String EXECUTOR_PAGE_SAVER = "PageSaver";

    private int counter = 1;
    private String html;

    private final SessionManager sessionManager;

    public HtmlJSInterface() {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
    }

    /**
     * @return The most recent HTML received by the interface
     */
    public String getHtml() {
        return sessionManager.getLCGContent();
    }

    /**
     * Sets most recent HTML and notifies observers.
     *
     * @param html The full HTML of a page
     */
    @JavascriptInterface
    public void setHtml(String html) {
        Timber.tag(TAG).d("Counter => %s", counter);
        sessionManager.setLCGContent(html);
        counter++;
    }

    public static String jsFunction() {
        return "javascript:(function() { "
                + "window." + EXECUTOR_PAGE_SAVER + ".setHtml('<html>'+"
                + "document.getElementsByTagName('html')[0].innerHTML+'</html>');})();";
    }
}

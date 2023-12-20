package org.intelehealth.unicef.activities.base;

import android.content.res.Configuration;

import org.intelehealth.unicef.webrtc.activity.BaseActivity;

import java.util.Locale;

public class LocalConfigActivity extends BaseActivity {
    public void setLocale(String appLanguage) {
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }
}
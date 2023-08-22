package org.intelehealth.unicef.activities.base;

import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class BaseActivity extends AppCompatActivity {
    public void setLocale(String appLanguage) {
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }
}
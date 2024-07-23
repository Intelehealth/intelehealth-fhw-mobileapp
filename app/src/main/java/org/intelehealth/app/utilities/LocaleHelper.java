package org.intelehealth.app.utilities;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.util.Log;

import org.intelehealth.klivekit.utils.DateTimeResource;

import java.util.Locale;

/**
 * Created by Prajwal Maruti Waingankar on 20-01-2022, 17:02
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

public class LocaleHelper extends ContextWrapper {

    public LocaleHelper(Context base) {
        super(base);
    }

    public static ContextWrapper updateLocale(Context context, String lang) {
        Locale localeToSwitchTo = new Locale(lang);
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration(); // 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localeList = new LocaleList(localeToSwitchTo); // 2
            LocaleList.setDefault(localeList); // 3
            configuration.setLocales(localeList); // 4
        } else {
            configuration.locale = localeToSwitchTo; // 5
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            context = context.createConfigurationContext(configuration); // 6
        } else {
            resources.updateConfiguration(configuration, resources.getDisplayMetrics()); // 7
        }

        DateTimeResource.clearInstance();
        DateTimeResource.build(context);
        return new LocaleHelper(context);
    }

    public static Context setLocale(Context context) {
        SessionManager sessionManager1 = new SessionManager(context);
        String appLanguage = sessionManager1.getAppLanguage();
        if (!appLanguage.isEmpty()) {
            Resources res = context.getResources();
            Configuration conf = res.getConfiguration();
            Locale locale = new Locale(appLanguage);
            Locale.setDefault(locale);
            conf.setLocale(locale);
            context.createConfigurationContext(conf);
            DisplayMetrics dm = res.getDisplayMetrics();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                conf.setLocales(new LocaleList(locale));
            }
            res.updateConfiguration(conf, dm);
        }
        DateTimeResource.clearInstance();
        DateTimeResource.build(context);
        return context;
    }

    public static boolean isArabic(Context context) {
        SessionManager sessionManager1 = new SessionManager(context);
        String appLanguage = sessionManager1.getAppLanguage();
        return appLanguage.equalsIgnoreCase("ar");
    }

}

package org.intelehealth.app.utilities;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

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

        return new LocaleHelper(context);
    }

}

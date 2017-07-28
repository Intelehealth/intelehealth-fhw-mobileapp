package io.intelehealth.client.application;

import android.app.Application;
import android.content.Context;


import com.parse.Parse;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import io.intelehealth.client.R;
import io.intelehealth.client.utilities.HelperMethods;

/**
 * Created by tusharjois on 9/20/16.
 */
@ReportsCrashes(
        formUri = "https://intelehealth.cloudant.com/acra-intelehealth/_design/acra-storage/_update/report",
        reportType = HttpSender.Type.JSON,
        httpMethod = HttpSender.Method.POST,
        formUriBasicAuthLogin = "thisheyetheentmornevessh",
        formUriBasicAuthPassword = "2bf554e018d200e27788367cd2b8ebc259cb80a7",
        //formKey = "", // This is required for backward compatibility but not used
        customReportContent = {
                ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PACKAGE_NAME,
                ReportField.REPORT_ID,
                ReportField.BUILD,
                ReportField.STACK_TRACE
        },
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.toast_crash
)

public class IntelehealthApplication extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        // The following line triggers the initialization of ACRA
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(HelperMethods.IMAGE_APP_ID)
                .server(HelperMethods.IMAGE_SERVER_URL)
                .build()
        );
        this.mContext = getApplicationContext();
        ACRA.init(this);
    }

    public static Context getAppContext() {
        return mContext;
    }
}

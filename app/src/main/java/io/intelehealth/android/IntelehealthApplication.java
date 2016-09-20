package io.intelehealth.android;

import android.app.Application;
import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

/**
 * Created by tusharjois on 9/20/16.
 */
@ReportsCrashes(
        formUri = "https://intelehealth.cloudant.com/acra-intelehealth/_design/acra-storage/_update/report",
        reportType = HttpSender.Type.JSON,
        httpMethod = HttpSender.Method.POST,
        formUriBasicAuthLogin = "thisheyetheentmornevessh",
        formUriBasicAuthPassword = "2bf554e018d200e27788367cd2b8ebc259cb80a7",
        formKey = "", // This is required for backward compatibility but not used
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

    @Override
    public void onCreate() {
        super.onCreate();
        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}

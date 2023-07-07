package org.intelehealth.ezazi.ui.rtc.call;

import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.core.data.BaseDataSource;
import org.intelehealth.ezazi.networkApiCalls.ApiClient;
import org.intelehealth.ezazi.networkApiCalls.ApiInterface;
import org.intelehealth.ezazi.ui.password.listener.OnAPISuccessListener;
import org.intelehealth.ezazi.ui.rtc.data.RtcTokenDataSource;
import org.intelehealth.ezazi.ui.rtc.model.UserToken;
import org.intelehealth.klivekit.model.RtcArgs;

/**
 * Created by Vaghela Mithun R. on 06-07-2023 - 14:19.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class CallInitializer {
    public interface OnCallInitializedListener {
        void onInitialized(RtcArgs args);
    }

    private final RtcArgs args;

    public CallInitializer(RtcArgs args) {
        this.args = args;
    }

    public void initiateVideoCall(OnCallInitializedListener listener) {
        String BASE_URL = "https://" + AppConstants.APP_URL + ":3000";
        ApiClient.changeApiBaseUrl(BASE_URL);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        new RtcTokenDataSource(apiService).getRtcToken(result -> {
            args.setToken(result.getToken());
            args.setAppToken(result.getAppToken());
            listener.onInitialized(args);
        }, args);
    }
}

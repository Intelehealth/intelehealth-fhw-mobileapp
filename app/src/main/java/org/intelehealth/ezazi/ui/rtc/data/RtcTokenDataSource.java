package org.intelehealth.ezazi.ui.rtc.data;

import org.intelehealth.ezazi.core.ApiResponse;
import org.intelehealth.ezazi.core.data.BaseDataSource;
import org.intelehealth.ezazi.networkApiCalls.ApiInterface;
import org.intelehealth.ezazi.ui.password.listener.APIExecuteListener;
import org.intelehealth.ezazi.ui.password.listener.OnAPISuccessListener;
import org.intelehealth.ezazi.ui.password.model.PasswordResponseModel;
import org.intelehealth.ezazi.ui.rtc.model.UserToken;
import org.intelehealth.klivekit.model.RtcArgs;

import retrofit2.Call;

/**
 * Created by Vaghela Mithun R. on 07-07-2023 - 09:19.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class RtcTokenDataSource extends BaseDataSource implements APIExecuteListener<UserToken> {
    private OnAPISuccessListener<UserToken> successListener;

    public RtcTokenDataSource(ApiInterface apiInterface) {
        super(apiInterface);
    }

    public void getRtcToken(OnAPISuccessListener<UserToken> listener, RtcArgs args) {
        this.successListener = listener;
        executeDirectCall(this, apiInterface.getVideoAppToken(
                args.getDoctorUuid(),
                args.getPatientPersonUuid(),
                args.getNurseId()
        ));
    }

    @Override
    public void onSuccess(UserToken result) {
        if (!result.isSuccess()) onFail("Failed to generate token");
        else if (successListener != null) successListener.onSuccess(result);
    }

    @Override
    public void onLoading(boolean isLoading) {

    }

    @Override
    public void onFail(String message) {

    }

    @Override
    public void onError(Throwable throwable) {

    }
}

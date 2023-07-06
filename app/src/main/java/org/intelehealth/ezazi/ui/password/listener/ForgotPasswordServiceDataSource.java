package org.intelehealth.ezazi.ui.password.listener;

import org.intelehealth.ezazi.core.ApiResponse;
import org.intelehealth.ezazi.core.data.BaseDataSource;
import org.intelehealth.ezazi.networkApiCalls.ApiInterface;
import org.intelehealth.ezazi.ui.password.model.ChangePasswordRequestModel;
import org.intelehealth.ezazi.ui.password.model.PasswordResponseModel;
import org.intelehealth.ezazi.ui.password.model.RequestOTPModel;
import org.intelehealth.ezazi.ui.password.model.VerifyOtpRequestModel;

import retrofit2.Call;

/**
 * Created by Kaveri Zaware on 04-07-2023
 * email - kaveri@intelehealth.org
 **/
public class ForgotPasswordServiceDataSource extends BaseDataSource {
    private static final String TAG = "ForgotPasswordServiceDa";

    public ForgotPasswordServiceDataSource(ApiInterface apiInterface) {
        super(apiInterface);
    }

    public void requestOTP(APIExecuteListener<PasswordResponseModel> executeListener, RequestOTPModel requestOTPModel) {
        Call<ApiResponse<PasswordResponseModel>> call = apiInterface.requestOTP(requestOTPModel);
        executeCall(executeListener, call);
    }
    public void verifyOtp(APIExecuteListener<PasswordResponseModel> executeListener, VerifyOtpRequestModel verifyOtpRequestModel) {
        Call<ApiResponse<PasswordResponseModel>> call = apiInterface.verifyOtp(verifyOtpRequestModel);
        executeCall(executeListener, call);
    }

    public void resetPassword(APIExecuteListener<PasswordResponseModel> executeListener, String userUuid, ChangePasswordRequestModel changePasswordRequestModel) {
        Call<ApiResponse<PasswordResponseModel>> call = apiInterface.resetPassword(userUuid, changePasswordRequestModel);
        executeCall(executeListener, call);
    }

}

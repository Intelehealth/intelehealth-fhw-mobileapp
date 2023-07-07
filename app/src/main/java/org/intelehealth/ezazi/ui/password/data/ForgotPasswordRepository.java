package org.intelehealth.ezazi.ui.password.data;

import org.intelehealth.ezazi.ui.password.listener.APIExecuteListener;
import org.intelehealth.ezazi.ui.password.data.ForgotPasswordServiceDataSource;
import org.intelehealth.ezazi.ui.password.model.ChangePasswordRequestModel;
import org.intelehealth.ezazi.ui.password.model.PasswordResponseModel;
import org.intelehealth.ezazi.ui.password.model.RequestOTPModel;
import org.intelehealth.ezazi.ui.password.model.VerifyOtpRequestModel;

/**
 * Created by Kaveri Zaware on 03-07-2023
 * email - kaveri@intelehealth.org
 **/
public class ForgotPasswordRepository  {
    private final ForgotPasswordServiceDataSource serviceDataSource;

    public ForgotPasswordRepository(ForgotPasswordServiceDataSource forgotPasswordServiceDataSource) {
        this.serviceDataSource = forgotPasswordServiceDataSource;
    }

    public void requestOTP(APIExecuteListener<PasswordResponseModel> executeListener, RequestOTPModel requestOTPModel) {
        serviceDataSource.requestOTP(executeListener, requestOTPModel);
    }

    public void verifyOtp(APIExecuteListener<PasswordResponseModel> executeListener, VerifyOtpRequestModel verifyOtpRequestModel) {
        serviceDataSource.verifyOtp(executeListener, verifyOtpRequestModel);
    }

    public void resetPassword(APIExecuteListener<PasswordResponseModel> executeListener, String userUuid, ChangePasswordRequestModel changePasswordRequestModel) {
        serviceDataSource.resetPassword(executeListener, userUuid, changePasswordRequestModel);
    }
}

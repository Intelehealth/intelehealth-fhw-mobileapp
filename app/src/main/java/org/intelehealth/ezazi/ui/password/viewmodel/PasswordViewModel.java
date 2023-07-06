package org.intelehealth.ezazi.ui.password.viewmodel;

import android.util.Log;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.core.ApiResponse;
import org.intelehealth.ezazi.core.BaseViewModel;
import org.intelehealth.ezazi.networkApiCalls.ApiClient;
import org.intelehealth.ezazi.networkApiCalls.ApiInterface;
import org.intelehealth.ezazi.ui.password.listener.APIExecuteListener;
import org.intelehealth.ezazi.ui.password.listener.ForgotPasswordServiceDataSource;
import org.intelehealth.ezazi.ui.password.model.ChangePasswordRequestModel;
import org.intelehealth.ezazi.ui.password.model.PasswordResponseModel;
import org.intelehealth.ezazi.ui.password.model.RequestOTPModel;
import org.intelehealth.ezazi.ui.password.model.VerifyOtpRequestModel;
import org.intelehealth.ezazi.ui.password.repository.ForgotPasswordRepository;

/**
 * Created by Kaveri Zaware on 03-07-2023
 * email - kaveri@intelehealth.org
 **/
public class PasswordViewModel extends ViewModel {
    private static final String TAG = "ForgotPasswordViewModel";
    private final ForgotPasswordRepository repository;

    private final MutableLiveData<PasswordResponseModel> requestOTPResponse = new MutableLiveData<PasswordResponseModel>();
    public LiveData<PasswordResponseModel> requestOTPResponseData = requestOTPResponse;

    public final MutableLiveData<RequestOTPModel> requestOtpModel = new MutableLiveData<RequestOTPModel>();
    private final MutableLiveData<PasswordResponseModel> verifyOtpResultData = new MutableLiveData<PasswordResponseModel>();
    public LiveData<PasswordResponseModel> verifyOtpData = verifyOtpResultData;

    private final MutableLiveData<PasswordResponseModel> changePasswordResultData = new MutableLiveData<PasswordResponseModel>();
    public LiveData<PasswordResponseModel> changePasswordResponse = changePasswordResultData;

    private final MutableLiveData<Boolean> loadingData = new MutableLiveData<>();
    public LiveData<Boolean> loading = loadingData;

    private final MutableLiveData<String> otpFailureResultData = new MutableLiveData<String>();
    public LiveData<String> otpFailureResult = otpFailureResultData;

    private final MutableLiveData<String> otpVerifyFailureResultData = new MutableLiveData<String>();
    public LiveData<String> otpVerifyFailureResult = otpVerifyFailureResultData;
    private final MutableLiveData<String> resetPasswordFailureResultData = new MutableLiveData<String>();
    public LiveData<String> resetPasswordFailureResult = resetPasswordFailureResultData;




    public PasswordViewModel(ForgotPasswordRepository repository) {
        this.repository = repository;
    }


    public static final ViewModelInitializer<PasswordViewModel> initializer = new ViewModelInitializer<>(
            PasswordViewModel.class,
            creationExtras -> {
                String BASE_URL = "https://" + AppConstants.APP_URL + ":" + AppConstants.PORT_NUMBER;
                ApiClient.changeApiBaseUrl(BASE_URL);
                ApiInterface apiService = ApiClient.createService(ApiInterface.class);
                ForgotPasswordServiceDataSource dataSource = new ForgotPasswordServiceDataSource(apiService);
                ForgotPasswordRepository requestOtpRepository = new ForgotPasswordRepository(dataSource);
                return new PasswordViewModel(requestOtpRepository);
            }
    );

    public void requestOtp(RequestOTPModel requestOTPModel) {
        repository.requestOTP(new APIExecuteListener<PasswordResponseModel>() {
            @Override
            public void onSuccess(PasswordResponseModel result) {
                requestOTPResponse.postValue(result);
                requestOtpModel.postValue(requestOTPModel);
            }

            @Override
            public void onLoading(boolean isLoading) {
                loadingData.postValue(isLoading);
            }

            @Override
            public void onFail(String message) {
                otpFailureResultData.setValue(message);
            }

            @Override
            public void onError(Throwable throwable) {

            }


        }, requestOTPModel);
    }

    public void verifyOtp(VerifyOtpRequestModel verifyOtpRequestModel) {
        repository.verifyOtp(new APIExecuteListener<PasswordResponseModel>() {
            @Override
            public void onSuccess(PasswordResponseModel result) {
                verifyOtpResultData.postValue(result);

            }

            @Override
            public void onLoading(boolean isLoading) {
                loadingData.postValue(isLoading);
            }

            @Override
            public void onFail(String message) {
                otpVerifyFailureResultData.setValue(message);

            }

            @Override
            public void onError(Throwable throwable) {

            }


        }, verifyOtpRequestModel);
    }

    public void resetPassword(String userUuid, ChangePasswordRequestModel changePasswordRequestModel) {
        repository.resetPassword(new APIExecuteListener<PasswordResponseModel>() {
            @Override
            public void onSuccess(PasswordResponseModel result) {
                changePasswordResultData.postValue(result);

            }

            @Override
            public void onLoading(boolean isLoading) {
                loadingData.postValue(isLoading);
            }

            @Override
            public void onFail(String message) {
                resetPasswordFailureResultData.setValue(message);

            }

            @Override
            public void onError(Throwable throwable) {

            }


        }, userUuid, changePasswordRequestModel);
    }

}

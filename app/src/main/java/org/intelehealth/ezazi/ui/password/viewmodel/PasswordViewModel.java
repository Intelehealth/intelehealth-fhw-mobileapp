package org.intelehealth.ezazi.ui.password.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import org.intelehealth.ezazi.BuildConfig;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.core.BaseViewModel;
import org.intelehealth.ezazi.networkApiCalls.ApiClient;
import org.intelehealth.ezazi.networkApiCalls.ApiInterface;
import org.intelehealth.ezazi.ui.password.data.ForgotPasswordRepository;
import org.intelehealth.ezazi.ui.password.data.ForgotPasswordServiceDataSource;
import org.intelehealth.ezazi.ui.password.model.ChangePasswordRequestModel;
import org.intelehealth.ezazi.ui.password.model.PasswordResponseModel;
import org.intelehealth.ezazi.ui.password.model.RequestOTPModel;
import org.intelehealth.ezazi.ui.password.model.VerifyOtpRequestModel;

/**
 * Created by Kaveri Zaware on 03-07-2023
 * email - kaveri@intelehealth.org
 **/
public class PasswordViewModel extends BaseViewModel {
    private static final String TAG = "ForgotPasswordViewModel";
    private final ForgotPasswordRepository repository;

    private final MutableLiveData<PasswordResponseModel> requestOTPResponse = new MutableLiveData<PasswordResponseModel>();
    public LiveData<PasswordResponseModel> requestOTPResponseData = requestOTPResponse;

    public final MutableLiveData<RequestOTPModel> requestOtpModel = new MutableLiveData<RequestOTPModel>();
    private final MutableLiveData<PasswordResponseModel> verifyOtpResultData = new MutableLiveData<PasswordResponseModel>();
    public LiveData<PasswordResponseModel> verifyOtpData = verifyOtpResultData;
    private final MutableLiveData<PasswordResponseModel> changePasswordResultData = new MutableLiveData<PasswordResponseModel>();
    public LiveData<PasswordResponseModel> changePasswordResponse = changePasswordResultData;


    public PasswordViewModel(ForgotPasswordRepository repository) {
        this.repository = repository;
    }


    public static final ViewModelInitializer<PasswordViewModel> initializer = new ViewModelInitializer<>(
            PasswordViewModel.class,
            creationExtras -> {
                String BASE_URL = BuildConfig.SERVER_URL + ":" + AppConstants.PORT_NUMBER;
                ApiClient.changeApiBaseUrl(BASE_URL);
                ApiInterface apiService = ApiClient.createService(ApiInterface.class);
                ForgotPasswordServiceDataSource dataSource = new ForgotPasswordServiceDataSource(apiService);
                ForgotPasswordRepository requestOtpRepository = new ForgotPasswordRepository(dataSource);
                return new PasswordViewModel(requestOtpRepository);
            }
    );

    public void requestOtp(RequestOTPModel requestOTPModel) {
        repository.requestOTP(new ExecutionListener<PasswordResponseModel>() {
            @Override
            public void onSuccess(PasswordResponseModel result) {
                requestOTPResponse.postValue(result);
                requestOtpModel.postValue(requestOTPModel);
            }
        }, requestOTPModel);
    }

    public void verifyOtp(VerifyOtpRequestModel verifyOtpRequestModel) {
        repository.verifyOtp(new ExecutionListener<PasswordResponseModel>() {
            @Override
            public void onSuccess(PasswordResponseModel result) {
                verifyOtpResultData.postValue(result);
            }
        }, verifyOtpRequestModel);
    }

    public void resetPassword(String userUuid, ChangePasswordRequestModel changePasswordRequestModel) {
        repository.resetPassword(new ExecutionListener<PasswordResponseModel>() {
            @Override
            public void onSuccess(PasswordResponseModel result) {
                changePasswordResultData.postValue(result);
            }
        }, userUuid, changePasswordRequestModel);
    }

    public void clearPreviousResult() {
        requestOTPResponse.postValue(null);
        verifyOtpResultData.postValue(null);
        changePasswordResultData.postValue(null);
    }
}

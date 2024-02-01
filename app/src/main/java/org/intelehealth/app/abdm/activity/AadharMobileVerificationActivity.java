package org.intelehealth.app.abdm.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.checkerframework.checker.units.qual.A;
import org.intelehealth.app.R;
import org.intelehealth.app.abdm.model.AadharApiBody;
import org.intelehealth.app.abdm.model.TokenResponse;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.databinding.ActivityAadharMobileVerificationBinding;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.WindowsUtils;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class AadharMobileVerificationActivity extends AppCompatActivity {
    private Context context = AadharMobileVerificationActivity.this;
    public static final String TAG = AadharMobileVerificationActivity.class.getSimpleName();
    ActivityAadharMobileVerificationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAadharMobileVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowsUtils.setStatusBarColor(AadharMobileVerificationActivity.this);  // changing status bar color

        binding.sendOtpBtn.setOnClickListener(v -> {
            checkValidation();
            if (binding.otpBox.getVisibility() != View.VISIBLE)
                binding.otpBox.setVisibility(View.VISIBLE);

            callGenerateTokenApi();
        });
    }

    private void callGenerateTokenApi() {
        Single<TokenResponse> tokenResponse = AppConstants.apiInterface.GET_TOKEN(UrlModifiers.getABDM_TokenUrl());
        tokenResponse.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<TokenResponse>() {
                    @Override
                    public void onSuccess(TokenResponse tokenResponse) {
                        String token = tokenResponse.getAccessToken();
                        Log.d(TAG, "onSuccess: TokenResponse: " + tokenResponse.toString());

                        callAadharMobileVerificationApi(token);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    private void callAadharMobileVerificationApi(String accessToken) {
        if (accessToken.isEmpty()) {    // if token empty
            Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            return;
        }

        AadharApiBody aadharApiBody = new AadharApiBody();
        aadharApiBody.setAadhar(binding.aadharNoBox.getText().toString());

        String url = UrlModifiers.getAadharOTPVerificationUrl();
        Single<ResponseBody> responseBodySingle = AppConstants.apiInterface.GET_OTP_FOR_AADHAR(url, accessToken, aadharApiBody);
        responseBodySingle.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<ResponseBody>() {
                    @Override
                    public void onSuccess(ResponseBody responseBody) {
                        Log.d(TAG, "onSuccess: AadharResponse: " + responseBody);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: AadharResponse: " + e.getMessage());
                    }
                });

    }

    private void checkValidation() {
        if (binding.aadharNoBox.getText().toString().isEmpty()) {
            binding.aadharNoBox.setError(getString(R.string.error_field_required));
            return;
        }
        if (binding.mobileNoBox.getText().toString().isEmpty()) {
            binding.mobileNoBox.setError(getString(R.string.error_field_required));
            return;
        }
        if (binding.aadharNoBox.getText().toString().length() < 12) {
            binding.aadharNoBox.setError("Invalid");
            return;
        }
        if (binding.mobileNoBox.getText().toString().length() < 10) {
            binding.mobileNoBox.setError("Invalid");
            return;
        }
    }
}
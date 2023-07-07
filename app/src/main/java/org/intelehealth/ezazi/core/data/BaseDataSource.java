package org.intelehealth.ezazi.core.data;

import androidx.annotation.NonNull;

import org.intelehealth.ezazi.core.ApiResponse;
import org.intelehealth.ezazi.networkApiCalls.ApiInterface;
import org.intelehealth.ezazi.ui.password.listener.APIExecuteListener;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Kaveri Zaware on 04-07-2023
 * email - kaveri@intelehealth.org
 **/
public class BaseDataSource {
    protected final ApiInterface apiInterface;
    private static final String TAG = "ForgotPasswordServiceDa";

    public BaseDataSource(ApiInterface apiInterface) {
        this.apiInterface = apiInterface;
    }

    public <T> void executeCall(APIExecuteListener<T> executeListener, Call<ApiResponse<T>> call) {
        executeListener.onLoading(true);
        call.enqueue(new Callback<ApiResponse<T>>() {
            @Override
            public void onResponse(Call<ApiResponse<T>> call, Response<ApiResponse<T>> response) {
                executeListener.onLoading(false);
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().isSuccess()) {
                        executeListener.onSuccess(response.body().getData());
                    } else executeListener.onFail(response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<T>> call, Throwable t) {
                executeListener.onLoading(false);
                executeListener.onError(t);
            }
        });
    }

    public <T> void executeDirectCall(APIExecuteListener<T> executeListener, Call<T> call) {
        executeListener.onLoading(true);
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                executeListener.onLoading(false);
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        executeListener.onSuccess(response.body());
                    } else executeListener.onFail("No data found");
                }
            }

            @Override
            public void onFailure(@NonNull Call<T> call, Throwable t) {
                executeListener.onLoading(false);
                executeListener.onError(t);
            }
        });
    }

}

package org.intelehealth.ezazi.core;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.intelehealth.ezazi.ui.password.listener.APIExecuteListener;


/**
 * Created by Kaveri Zaware on 06-07-2023
 * email - kaveri@intelehealth.org
 **/
public class BaseViewModel extends ViewModel {
    private static final String TAG = "BaseViewModel";
    private final MutableLiveData<Boolean> loadingData = new MutableLiveData<>();
    public LiveData<Boolean> loading = loadingData;

    private final MutableLiveData<String> failResult = new MutableLiveData<String>();
    public LiveData<String> failDataResult = failResult;

    private final MutableLiveData<Throwable> errorResult = new MutableLiveData<Throwable>();
    public LiveData<Throwable> errorDataResult = errorResult;

    public abstract class ExecutionListener<T> implements APIExecuteListener<T> {
        @Override
        public void onLoading(boolean isLoading) {
            loadingData.postValue(isLoading);
        }

        @Override
        public void onFail(String message) {
            failResult.postValue(message);
            Log.d(TAG, "onFail: message : "+message);
        }

        @Override
        public void onError(Throwable throwable) {
            Log.d(TAG, "onFail: throwable : "+throwable.getLocalizedMessage());

            errorResult.postValue(throwable);
        }
    }
}

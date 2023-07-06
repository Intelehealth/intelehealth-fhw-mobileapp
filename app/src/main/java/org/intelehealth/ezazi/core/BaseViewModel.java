package org.intelehealth.ezazi.core;

import android.util.Log;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.core.data.BaseDataSource;
import org.intelehealth.ezazi.networkApiCalls.ApiClient;
import org.intelehealth.ezazi.networkApiCalls.ApiInterface;

/**
 * Created by Kaveri Zaware on 06-07-2023
 * email - kaveri@intelehealth.org
 **/
public class BaseViewModel<T> extends ViewModel {
    private static final String TAG = "BaseViewModel";
    public Class<T> tClass;

    public BaseViewModel(Class<T> tClass) {
        this.tClass = tClass;
    }

    public static final ViewModelInitializer<BaseViewModel> initializer = new ViewModelInitializer<>(
            BaseViewModel.class,
            creationExtras -> {
                String BASE_URL =AppConstants.PROTOCOL + AppConstants.APP_URL + ":" + AppConstants.PORT_NUMBER;
                Log.d(TAG, "baseurl for password: " + BASE_URL);
                ApiClient.changeApiBaseUrl(BASE_URL);
                ApiInterface apiService = ApiClient.createService(ApiInterface.class);
                BaseDataSource dataSource = new BaseDataSource(apiService);
                BaseRepository baseRepository = new BaseRepository(dataSource);
                return new BaseViewModel(baseRepository.getClass());
            }
    );

}

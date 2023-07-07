package org.intelehealth.ezazi.ui.password.listener;

/**
 * Created by Kaveri Zaware on 03-07-2023
 * email - kaveri@intelehealth.org
 **/
public interface APIExecuteListener<T> {
    void onSuccess(T result);

    void onLoading(boolean isLoading);

    void onFail(String message);

    void onError(Throwable throwable);
}

package org.intelehealth.app.activities.visit.download_doc;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.networkApiCalls.ApiInterface;

import java.io.File;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class DownloadDoctorDoc {

    private final DownloadDoctorDocCallback callback;
    private final File destinationFile;

    public DownloadDoctorDoc(DownloadDoctorDocCallback callback, File destinationFile) {
        this.callback = callback;
        this.destinationFile = destinationFile;
    }

    public void downloadDoctorDoc(String url, String auth) {
        ApiInterface apiInterface = AppConstants.apiInterface;
        apiInterface.DOWNLOAD_DOCTOR_ADDITIONAL_DOCUMENT(url, auth)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new SingleObserver<>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                callback.onDownloadStarted();
                            }

                            @Override
                            public void onSuccess(Response<ResponseBody> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    SaveDoctorDocument document = new SaveDoctorDocument();
                                    boolean isFileSaved = document.saveFile(response.body(), destinationFile);
                                    if (isFileSaved) {
                                        callback.onDownloadComplete(destinationFile);
                                    }
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                callback.onDownloadFailed();
                            }
                        }
                );
    }
}

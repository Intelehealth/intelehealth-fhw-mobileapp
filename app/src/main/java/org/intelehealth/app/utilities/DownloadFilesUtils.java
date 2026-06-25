package org.intelehealth.app.utilities;

import android.content.Context;
import android.os.Environment;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.utilities.exception.DAOException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Pair;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class DownloadFilesUtils {

    String TAG = DownloadFilesUtils.class.getSimpleName();

    public void saveToDisk(ResponseBody body, String filename) {
        try {

            File destinationFile = new File(AppConstants.IMAGE_PATH, filename + ".jpg");

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(destinationFile);
                byte[] data = new byte[4096];
                int count;
                int progress = 0;
                long fileSize = body.contentLength();
                CustomLog.d(TAG, "File Size=" + fileSize);
                while ((count = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, count);
                    progress += count;
                    Pair<Integer, Long> pairs = new Pair<>(progress, fileSize);
                    CustomLog.d(TAG, "Progress: " + progress + "/" + fileSize + " >>>> " + (float) progress / fileSize);
                }

                outputStream.flush();

                CustomLog.d(TAG, destinationFile.getParent());
                Pair<Integer, Long> pairs = new Pair<>(100, 100L);
                return;
            } catch (IOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                Pair<Integer, Long> pairs = new Pair<>(-1, Long.valueOf(-1));
                CustomLog.d(TAG, "Failed to save the file!");
                return;
            } finally {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            }
        } catch (IOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            CustomLog.d(TAG, "Failed to save the file!");
            return;
        }
    }

    public static void bindProfileImage(Context context, ImageView imageView, String photoPath) {
        if (photoPath == null || photoPath.isEmpty()) {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avatar1));
            return;
        }
        File file = new File(photoPath);
        Glide.with(context)
                .load(file)
                .signature(new ObjectKey(file.exists() ? file.lastModified() : 0L))
                .centerCrop()
                .error(R.drawable.avatar1)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imageView);
    }

    public static String resolvePatientPhotoPath(Context context, String patientId, String storedPath) {
        if (storedPath != null && !storedPath.isEmpty()) {
            File stored = new File(storedPath);
            if (stored.exists()) {
                return storedPath;
            }
        }
        if (patientId == null || patientId.isEmpty()) {
            return storedPath;
        }
        File defaultFile = new File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                patientId + ".jpg"
        );
        if (defaultFile.exists()) {
            return defaultFile.getAbsolutePath();
        }
        return storedPath;
    }

    public static void ensurePatientProfileImage(
            Context context,
            ImageView imageView,
            String patientId,
            String storedPath
    ) {
        String resolvedPath = resolvePatientPhotoPath(context, patientId, storedPath);
        if (resolvedPath != null && !resolvedPath.isEmpty() && new File(resolvedPath).exists()) {
            bindProfileImage(context, imageView, resolvedPath);
            return;
        }
        if (patientId == null || patientId.isEmpty()) {
            bindProfileImage(context, imageView, storedPath);
            return;
        }
        try {
            if (new ImagesDAO().hasUnsyncedPatientProfileImage(patientId)) {
                bindProfileImage(context, imageView, resolvedPath);
                return;
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        if (!NetworkConnection.isOnline(context)) {
            bindProfileImage(context, imageView, null);
            return;
        }
        String url = new UrlModifiers().patientProfileImageUrl(patientId);
        String auth = "Basic " + new SessionManager(context).getEncoded();
        AppConstants.apiInterface.PERSON_PROFILE_PIC_DOWNLOAD(url, auth)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody file) {
                        new DownloadFilesUtils().saveToDisk(file, patientId);
                    }

                    @Override
                    public void onError(Throwable e) {
                        bindProfileImage(context, imageView, resolvedPath);
                    }

                    @Override
                    public void onComplete() {
                        String photoPath = AppConstants.IMAGE_PATH + patientId + ".jpg";
                        try {
                            new PatientsDAO().updatePatientPhoto(patientId, photoPath);
                            new ImagesDAO().insertPatientProfileImages(photoPath, patientId);
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                        bindProfileImage(context, imageView, photoPath);
                    }
                });
    }

}

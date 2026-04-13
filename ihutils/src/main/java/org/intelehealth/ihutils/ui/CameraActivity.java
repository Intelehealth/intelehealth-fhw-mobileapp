package org.intelehealth.ihutils.ui;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.AspectRatioStrategy;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.ihutils.R;
import org.intelehealth.ihutils.utils.BitmapUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class CameraActivity extends AppCompatActivity {


    public static final int TAKE_IMAGE = 205;
    /**
     * Bundle key used for the {@link String} setting custom Image Name
     * for the file generated
     */
    public static final String SET_IMAGE_NAME = "IMG_NAME";
    /**
     * Bundle key used for the {@link String} setting custom FilePath for
     * storing the file generated
     */
    public static final String SET_IMAGE_PATH = "IMG_PATH";
    /**
     * Bundle key used for the {@link String} showing custom dialog
     * message before starting the camera.
     */
    public static final String SHOW_DIALOG_MESSAGE = "DEFAULT_DLG";
    public static final String SEND_BROADCAST_AFTER_CAPTURE = "SEND_BROADCAST_AFTER_CAPTURE";

    private static final int[] FLASH_OPTIONS = {
            ImageCapture.FLASH_MODE_AUTO,
            ImageCapture.FLASH_MODE_OFF,
            ImageCapture.FLASH_MODE_ON
    };

    private static final int[] FLASH_ICONS = {
            R.drawable.utils_ic_flash_auto,
            R.drawable.utils_ic_flash_off,
            R.drawable.ic_flash_on
    };

    private static final int[] FLASH_TITLES = {
            R.string.util_flash_auto,
            R.string.util_flash_off,
            R.string.util_flash_on
    };

    private final String TAG = CameraActivity.class.getSimpleName();

    private PreviewView previewView;
    private CameraSelector cameraSelector;
    private Preview preview;
    private ProcessCameraProvider provider;
    private ImageCapture imageCapture;

    private int mCurrentFlash = 0;
    private Handler mBackgroundHandler;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;

    //Pass Custom File Name Using intent.putExtra(CameraActivity.SET_IMAGE_NAME, "Image Name");
    private String mImageName = null;
    //Pass Dialog Message Using intent.putExtra(CameraActivity.SET_IMAGE_NAME, "Dialog Message");
    private String mDialogMessage = null;
    //Pass Custom File Path Using intent.putExtra(CameraActivity.SET_IMAGE_PATH, "Image Path");
    private String mFilePath = null;

    ResolutionSelector resolutionSelector;

    void compressImageAndSave(Bitmap bitmap) {
        getBackgroundHandler().post(() -> {
            if (mImageName == null) {
                mImageName = "IMG";
            }


            String filePath = mImagePathRoot + mImageName + ".jpg";

            File file;
            if (mFilePath == null) {
                file = new File(mImagePathRoot + mImageName + ".jpg");
            } else {
                file = new File(mImagePathRoot + mImageName + ".jpg");
            }
            OutputStream os = null;
            try {
                os = new FileOutputStream(file);
                //Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                //  Bitmap bitmap = Bitmap.createScaledBitmap(bmp, 600, 800, false);
                //  bitmap.recycle();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                os.flush();
                os.close();
                bitmap.recycle();


                Bitmap scaledBitmap = null;

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

                int actualHeight = options.outHeight;
                int actualWidth = options.outWidth;
                float maxHeight = 816.0f;
                float maxWidth = 612.0f;
                float imgRatio = actualWidth / actualHeight;
                float maxRatio = maxWidth / maxHeight;

                if (actualHeight > maxHeight || actualWidth > maxWidth) {
                    if (imgRatio < maxRatio) {
                        imgRatio = maxHeight / actualHeight;
                        actualWidth = (int) (imgRatio * actualWidth);
                        actualHeight = (int) maxHeight;
                    } else if (imgRatio > maxRatio) {
                        imgRatio = maxWidth / actualWidth;
                        actualHeight = (int) (imgRatio * actualHeight);
                        actualWidth = (int) maxWidth;
                    } else {
                        actualHeight = (int) maxHeight;
                        actualWidth = (int) maxWidth;
                    }
                }

                options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inTempStorage = new byte[16 * 1024];

                try {
                    bmp = BitmapFactory.decodeFile(filePath, options);
                } catch (OutOfMemoryError exception) {
                    exception.printStackTrace();

                }
                try {
                    scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
                } catch (OutOfMemoryError exception) {
                    exception.printStackTrace();
                }

                float ratioX = actualWidth / (float) options.outWidth;
                float ratioY = actualHeight / (float) options.outHeight;
                float middleX = actualWidth / 3.0f;
                float middleY = actualHeight / 3.0f;

                Matrix scaleMatrix = new Matrix();
                scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

                if (scaledBitmap != null) {
                    Canvas canvas = new Canvas(scaledBitmap);
                    canvas.setMatrix(scaleMatrix);
                    canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 3, middleY - bmp.getHeight() / 3, new Paint(Paint.FILTER_BITMAP_FLAG));
                }
                ExifInterface exif;
                try {
                    exif = new ExifInterface(filePath);

                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                    Log.e("EXIF", "Exif: " + orientation);
                    Matrix matrix = new Matrix();
                    if (orientation == 6) {
                        matrix.postRotate(90);
                        Log.e("EXIF", "Exif: " + orientation);
                    } else if (orientation == 3) {
                        matrix.postRotate(180);
                        Log.e("EXIF", "Exif: " + orientation);
                    } else if (orientation == 8) {
                        matrix.postRotate(270);
                        Log.e("EXIF", "Exif: " + orientation);
                    }

                    if (scaledBitmap != null) {
                        scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                FileOutputStream out = null;
                try {
                    if (scaledBitmap != null) {
                        out = new FileOutputStream(file);
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (bmp != null) {
                        bmp.recycle();
                        bmp = null;
                    }
                    if (scaledBitmap != null) {
                        scaledBitmap.recycle();
                    }
                }
                Intent intent = new Intent();
                intent.putExtra("RESULT", file.getAbsolutePath());
                runOnUiThread(() -> {
                    setResult(RESULT_OK, intent);
                    finish();
                });
            } catch (IOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                Log.w(TAG, "Cannot write to " + file, e);
                setResult(RESULT_CANCELED, new Intent());
                finish();
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        //FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }
            }

        });
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    public String mImagePathRoot = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImagePathRoot = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(SET_IMAGE_NAME))
                mImageName = extras.getString(SET_IMAGE_NAME);
            if (extras.containsKey(SHOW_DIALOG_MESSAGE))
                mDialogMessage = extras.getString(SHOW_DIALOG_MESSAGE);
            if (extras.containsKey(SET_IMAGE_PATH))
                mFilePath = extras.getString(SET_IMAGE_PATH);
        }

        setContentView(R.layout.utils_activity_camera);
        previewView = findViewById(R.id.utils_camera_surface_preview_view);

        handleBackPress();

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        resolutionSelector = buildResolutionSelector();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CameraActivityPermissionsDispatcher.startCameraWithPermissionCheck(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CameraActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    void startCamera() {
        if (provider != null) {
            bindPreview(provider);
            return;
        }

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                provider = cameraProviderFuture.get();
                bindPreview(provider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Failed to get camera provider", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }


    void bindPreview(@NonNull ProcessCameraProvider provider) {
        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();
        preview = new Preview.Builder()
                .setResolutionSelector(resolutionSelector)
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        imageCapture = new ImageCapture.Builder()
                .setResolutionSelector(resolutionSelector)
                .setFlashMode(FLASH_OPTIONS[mCurrentFlash])
                .build();
        try {
            provider.unbindAll();
            provider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
        } catch (Exception ignored) {
            // this point should not be reached
        }
    }

    ResolutionSelector buildResolutionSelector() {
        return new ResolutionSelector
                .Builder()
                .setAspectRatioStrategy(new AspectRatioStrategy(AspectRatio.RATIO_4_3, AspectRatioStrategy.FALLBACK_RULE_AUTO))
                .setResolutionStrategy(new ResolutionStrategy(new Size(1280, 960), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER))
                .build();

    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    void showDeniedForCamera() {
        Toast.makeText(this, getString(R.string.util_permission_camera_denied), Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    void showNeverAskForCamera() {
        Toast.makeText(this, getString(R.string.util_permission_camera_never_askagain), Toast.LENGTH_SHORT).show();
    }

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    /**
     * removed onBackPressed function due to deprecation
     * and added this one to handle onBackPressed
     */
    private void handleBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    public void endCameraSession(View view) {
        finish();
    }

    public void flipCamera(View view) {
        lensFacing = (lensFacing == CameraSelector.LENS_FACING_FRONT)
                ? CameraSelector.LENS_FACING_BACK
                : CameraSelector.LENS_FACING_FRONT;

        if (provider != null) {
            bindPreview(provider);
        }
    }


    public void takeImage(View view) {
        if (imageCapture == null) return;

        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@org.jspecify.annotations.NonNull ImageProxy image) {
                super.onCaptureSuccess(image);
                compressImageAndSave(BitmapUtils.imageProxyToBitmap(image));
                image.close();
            }

            @Override
            public void onError(@org.jspecify.annotations.NonNull ImageCaptureException exception) {
                super.onError(exception);
            }
        });
    }

    public void switchFlash(View view) {
        mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
        Toast.makeText(this, FLASH_TITLES[mCurrentFlash], Toast.LENGTH_SHORT).show();
        ((ImageView) view).setImageResource(FLASH_ICONS[mCurrentFlash]);
        imageCapture.setFlashMode(FLASH_OPTIONS[mCurrentFlash]);
    }
}

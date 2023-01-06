package org.intelehealth.msfarogyabharat.activities.cameraActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.TorchState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import org.intelehealth.msfarogyabharat.R;
import org.intelehealth.msfarogyabharat.app.AppConstants;
import org.intelehealth.msfarogyabharat.app.IntelehealthApplication;
import org.intelehealth.msfarogyabharat.databinding.ActivityCameraBinding;
import org.intelehealth.msfarogyabharat.utilities.BitmapUtils;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
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

    private final String TAG = CameraActivity.class.getSimpleName();
    //private CameraView mCameraView;
    private FloatingActionButton mFab;

    private static final int[] FLASH_OPTIONS = {ImageCapture.FLASH_MODE_AUTO, ImageCapture.FLASH_MODE_ON, ImageCapture.FLASH_MODE_OFF};
    private static final int[] FLASH_ICONS = {R.drawable.ic_flash_auto, R.drawable.ic_flash_on, R.drawable.ic_flash_off};
    private static final int[] FLASH_TITLES = {R.string.flash_auto, R.string.flash_off, R.string.flash_on,};
    private int mCurrentFlash = 0;

    private Handler mBackgroundHandler;

    //Pass Custom File Name Using intent.putExtra(CameraActivity.SET_IMAGE_NAME, "Image Name");
    private String mImageName = null;
    //Pass Dialog Message Using intent.putExtra(CameraActivity.SET_IMAGE_NAME, "Dialog Message");
    private String mDialogMessage = null;
    //Pass Custom File Path Using intent.putExtra(CameraActivity.SET_IMAGE_PATH, "Image Path");
    private String mFilePath = null;

    private ActivityCameraBinding binding;
    private ImageCapture imageCapture = null;
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
    private ProcessCameraProvider cameraProvider;

    void compressImageAndSave(final String filePath) {
        getBackgroundHandler().post(new Runnable() {
            @Override
            public void run() {
                File file = new File(filePath);
                BitmapUtils.fileCompressed(filePath);
                //OutputStream os = null;


                    /*os = new FileOutputStream(file);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    //  Bitmap bitmap = Bitmap.createScaledBitmap(bmp, 600, 800, false);
                    //  bitmap.recycle();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                    os.flush();
                    os.close();
                    bitmap.recycle();*/


              /*  Bitmap scaledBitmap = null;

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
                options.inPurgeable = true;
                options.inInputShareable = true;
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
                float middleX = actualWidth / 2.0f;
                float middleY = actualHeight / 2.0f;

                Matrix scaleMatrix = new Matrix();
                scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

                Canvas canvas = new Canvas(scaledBitmap);
                canvas.setMatrix(scaleMatrix);
                canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(
                        Paint.FILTER_BITMAP_FLAG));

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
                    scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(),
                            matrix, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                FileOutputStream out = null;
                String filename = filePath;
                try {
                    out = new FileOutputStream(file);
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
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
                }*/
                Intent intent = new Intent();
                intent.putExtra("RESULT", file.getAbsolutePath());
                setResult(RESULT_OK, intent);
                Log.i(TAG, file.getAbsolutePath());
                finish();


            }
        });
    }

    /*CameraX*/
    private PreviewView mPreviewView;
    private Executor executor = Executors.newSingleThreadExecutor();
    private Camera mCamera;

    /*END*/
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCameraBinding.inflate(getLayoutInflater());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(SET_IMAGE_NAME)) mImageName = extras.getString(SET_IMAGE_NAME);
            if (extras.containsKey(SHOW_DIALOG_MESSAGE))
                mDialogMessage = extras.getString(SHOW_DIALOG_MESSAGE);
            if (extras.containsKey(SET_IMAGE_PATH)) mFilePath = extras.getString(SET_IMAGE_PATH);
        }
        setContentView(binding.getRoot());
        mPreviewView = findViewById(R.id.previewView);
        mFab = findViewById(R.id.take_picture);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);

        }

        if (isCameraPermissionGranted()) {
            startCamera();
            binding.takePicture.setOnClickListener(view -> takePhoto());
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, AppConstants.CAMERA_PERMISSIONS);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.switch_flash) {
            mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
            item.setTitle(FLASH_TITLES[mCurrentFlash]);
            item.setIcon(FLASH_ICONS[mCurrentFlash]);
            imageCapture.setFlashMode(FLASH_OPTIONS[mCurrentFlash]);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AppConstants.CAMERA_PERMISSIONS) {
            if (isCameraPermissionGranted()) {
                startCamera();
            } else {
                Toast.makeText(CameraActivity.this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isCameraPermissionGranted() {
        return ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    void startCamera() {
        if (mDialogMessage != null) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this).setMessage(mDialogMessage).setNeutralButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.show();
            IntelehealthApplication.setAlertDialogCustomTheme(this, dialog);
        }

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        imageCapture = new ImageCapture
                .Builder()
                .build();

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
        } catch (Exception ignored) {

        }
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    void showRationaleForCamera(final PermissionRequest request) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this).setMessage(getString(R.string.permission_camera_rationale)).setPositiveButton(getString(R.string.button_allow), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                request.proceed();
            }
        }).setNegativeButton(getString(R.string.button_deny), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                request.cancel();
            }
        });
        AlertDialog dialog = builder.show();
        IntelehealthApplication.setAlertDialogCustomTheme(this, dialog);
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    void showDeniedForCamera() {
        Toast.makeText(this, getString(R.string.permission_camera_denied), Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    void showNeverAskForCamera() {
        Toast.makeText(this, getString(R.string.permission_camera_never_askagain), Toast.LENGTH_SHORT).show();
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        //file....
        if (mImageName == null) {
            mImageName = "IMG";
        }

        final String filePath = (mFilePath == null ? AppConstants.IMAGE_PATH : mFilePath) + File.separator + mImageName + ".jpg";

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(new File(filePath)).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                if (outputFileResults.getSavedUri() != null) {
                    compressImageAndSave(outputFileResults.getSavedUri().getPath());
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {

            }
        });
    }

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    @Override
    public void onBackPressed() {
        //do nothing
        finish();

    }
}

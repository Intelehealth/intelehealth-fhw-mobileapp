package org.intelehealth.vikalphelpline.activities.cameraActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.TorchState;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.intelehealth.vikalphelpline.R;
import org.intelehealth.vikalphelpline.app.AppConstants;
import org.intelehealth.vikalphelpline.app.IntelehealthApplication;
import org.intelehealth.vikalphelpline.utilities.BitmapUtils;
import org.intelehealth.vikalphelpline.utilities.StringUtils;

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

    private Handler mBackgroundHandler;

    //Pass Custom File Name Using intent.putExtra(CameraActivity.SET_IMAGE_NAME, "Image Name");
    private String mImageName = null;
    //Pass Dialog Message Using intent.putExtra(CameraActivity.SET_IMAGE_NAME, "Dialog Message");
    private String mDialogMessage = null;
    //Pass Custom File Path Using intent.putExtra(CameraActivity.SET_IMAGE_PATH, "Image Path");
    private String mFilePath = null;

    void compressImageAndSave(final String filePath, String mfilename) {
        getBackgroundHandler().post(new Runnable() {
            @Override
            public void run() {
                File file = new File(filePath);
                BitmapUtils.fileCompressed(filePath);

                Intent intent = new Intent();
                intent.putExtra("RESULT", file.getAbsolutePath());
                intent.putExtra("FILENAME", mfilename);
                setResult(RESULT_OK, intent);
                Log.i(TAG, file.getAbsolutePath());
                finish();

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

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(SET_IMAGE_NAME))
                mImageName = extras.getString(SET_IMAGE_NAME);
            if (extras.containsKey(SHOW_DIALOG_MESSAGE))
                mDialogMessage = extras.getString(SHOW_DIALOG_MESSAGE);
            if (extras.containsKey(SET_IMAGE_PATH))
                mFilePath = extras.getString(SET_IMAGE_PATH);
        }
        setContentView(R.layout.activity_camera);
        mPreviewView = findViewById(R.id.previewView);
        mFab = findViewById(R.id.take_picture);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CameraActivityPermissionsDispatcher.startCameraWithPermissionCheck(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.switch_flash) {
            if (mCamera.getCameraInfo().hasFlashUnit()) {
                mCamera.getCameraControl().enableTorch(!isTorchOn());
                if (!isTorchOn()) {
                    item.setTitle(getString(R.string.flash_off));
                    item.setIcon(R.drawable.ic_flash_off);
                } else {
                    item.setTitle(getString(R.string.flash_on));
                    item.setIcon(R.drawable.ic_flash_on);
                }
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isTorchOn() {
        if (mCamera == null) {
            return false;
        }
        return mCamera.getCameraInfo().getTorchState().getValue() == TorchState.ON;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CameraActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    void startCamera() {
        if (mDialogMessage != null) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                    .setMessage(mDialogMessage)
                    .setNeutralButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog = builder.show();
            IntelehealthApplication.setAlertDialogCustomTheme(this, dialog);
        }
        /*if (mCameraView != null)
            mCameraView.start();*/
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {

                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Log.d(TAG, "bindPreview ");
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        ImageCapture.Builder builder = new ImageCapture.Builder();

        //Vendor-Extensions (The CameraX extensions dependency in build.gradle)
        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);

        // Query if extension is available (optional).
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            // Enable the extension if available.
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }

        final ImageCapture imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                .build();

        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider());
        cameraProvider.unbindAll();
        mCamera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis, imageCapture);


        if (mFab != null) {
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //file....
                    if (mImageName == null) {
                        mImageName = "IMG";
                    }

                    final String filePath = (mFilePath == null ? AppConstants.IMAGE_PATH : mFilePath) + mImageName + ".jpg";
                    Log.v("main", "file: "+ filePath);

                    File file = new File(filePath);

                    ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
                    imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {

                                    File from = new File(IntelehealthApplication.getAppContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                                            + File.separator,mImageName +".jpg");

                                    EditText editText = new EditText(CameraActivity.this);
                                    editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(CameraActivity.this)
                                            .setTitle(R.string.dialog_title_enter_file_name)
                                            .setView(editText);
                                    AlertDialog alertDialog = builder1.create();
                                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.button_save), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String img = editText.getText().toString();
                                            File to = new File(IntelehealthApplication.getAppContext()
                                                    .getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator,img + ".jpg");
                                            Log.v("main", "file_new: "+ from + "\n" + to + "\n" + to.getAbsolutePath());

                                            if(from.exists())
                                                from.renameTo(to);

                                            compressImageAndSave(to.getAbsolutePath(), img);

                                            Toast.makeText(CameraActivity.this, getResources().getString(R.string.image_saved),
                                                    Toast.LENGTH_SHORT).show();

                                            /*Intent intent = new Intent();
                                            intent.putExtra("FILENAME", img);
                                            setResult(RESULT_OK, intent);
                                            finish();*/

                                        }
                                    });
                                    alertDialog.show();

                                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View v) {
                                            String img = editText.getText().toString();

                                            if (!StringUtils.isValidFileName(img)) {
                                                Toast.makeText(CameraActivity.this, R.string.invalid_filename, Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            alertDialog.dismiss();

                                            File to = new File(IntelehealthApplication.getAppContext()
                                                    .getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator,img + ".jpg");
                                            Log.v("main", "file_new: "+ from + "\n" + to + "\n" + to.getAbsolutePath());

                                            if(from.exists())
                                                from.renameTo(to);

                                            compressImageAndSave(to.getAbsolutePath(), img);

                                            Toast.makeText(CameraActivity.this, getResources().getString(R.string.image_saved),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });



                                }
                            });
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException error) {
                            error.printStackTrace();
                        }
                    });



                }
            });
        }

    }

    @OnShowRationale(Manifest.permission.CAMERA)
    void showRationaleForCamera(final PermissionRequest request) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setMessage(getString(R.string.permission_camera_rationale))
                .setPositiveButton(getString(R.string.button_allow), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(getString(R.string.button_deny), new DialogInterface.OnClickListener() {
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

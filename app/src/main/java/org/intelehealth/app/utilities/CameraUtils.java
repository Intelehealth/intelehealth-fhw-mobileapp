package org.intelehealth.app.utilities;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static org.intelehealth.ihutils.ui.CameraActivity.calculateInSampleSize;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by - Prajwal W. on 08/02/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
public class CameraUtils {

    private Handler mBackgroundHandler;
    private String mImageName = null;
    public String mImagePathRoot = "";
    private Activity context;
    private String mFilePath = null;
    String finalFilePath = null;

    public CameraUtils(Activity context, String mImageName, String mFilePath) {
        this.context = context;
        this.mImageName = mImageName;
        this.mFilePath = mFilePath;
        mImagePathRoot = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator;
    }
    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    public String compressImageAndSave(Bitmap bitmap) {
//        getBackgroundHandler().post(new Runnable() {
//            @Override
//            public void run() {
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
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                    os.flush();
                    os.close();

/*
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                        bitmap = null;
                    }
*/

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
                    float middleX = actualWidth / 3.0f;
                    float middleY = actualHeight / 3.0f;

                    Matrix scaleMatrix = new Matrix();
                    scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

                    Canvas canvas = new Canvas(scaledBitmap);
                    canvas.setMatrix(scaleMatrix);
                    canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 3, middleY - bmp.getHeight() / 3, new Paint(
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
                        if (bmp != null && !bmp.isRecycled()) {
                            bmp.recycle();
                            bmp = null;
                        }
                        if (scaledBitmap != null && !scaledBitmap.isRecycled()) {
                            scaledBitmap.recycle();
                        }
                    }
                   /* Intent intent = new Intent();
                    intent.putExtra("RESULT", file.getAbsolutePath());
                    context.setResult(RESULT_OK, intent);
                    Log.i("TAG", file.getAbsolutePath());
                    context.finish();*/

                    finalFilePath = file.getAbsolutePath();

                } catch (IOException e) {
                    Log.w("TAG", "Cannot write to " + file, e);
                    finalFilePath = null;
                    /*context.setResult(RESULT_CANCELED, new Intent());
                    context.finish();*/
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            //FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                }


          //  }
     //   });


        return finalFilePath;
    }

}

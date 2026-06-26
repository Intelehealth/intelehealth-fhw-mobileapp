package org.intelehealth.ihutils.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BitmapUtils {
    private static final String TAG = "BitmapUtils";
    /**
     * Rotate an image if required.
     *
     * @param data The image byte data
     * @return The resulted Bitmap after manipulation
     */
    public static Bitmap rotateImageIfRequired(byte[] data) throws IOException {
        Bitmap img = BitmapFactory.decodeByteArray(data, 0, data.length);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            ExifInterface ei = new ExifInterface(new ByteArrayInputStream(data));
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(img, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(img, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(img, 270);
                default:
                    return img;
            }
        } else {
            return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

/**
 * Function is used for Copy the Image
 * @param inputPath image uri path from media storage.
 * @param outputPath  to Copy the Path at intelehealth Directory.
 *
 * */
    public static void copyFile(String inputPath, String outputPath) {

        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(inputPath);
            out = new FileOutputStream(outputPath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

            Log.d("AdditionalDocuments", outputPath);

        } catch (FileNotFoundException fnfe1) {
            Log.e("AdditionalDocuments", fnfe1.getMessage());
        } catch (Exception e) {
            Log.v("AdditionalDocuments", e.getMessage());
        }
    }

    /**
     * Compress the file into bitmap
     *
     * @param filePath path of file to be compressed
     * */

    public static boolean fileCompressed(String filePath) {
        File file = new File(filePath);

        Log.d(TAG, "fileCompressed: filePath : " + filePath);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        if (options.outWidth <= 0 || options.outHeight <= 0) {
            return false;
        }

        int orientation = ExifInterface.ORIENTATION_NORMAL;
        try {
            ExifInterface exif = new ExifInterface(filePath);
            orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
            );
        } catch (IOException e) {
            Log.e(TAG, "Failed to read EXIF", e);
            return false;
        }

        int srcWidth = options.outWidth;
        int srcHeight = options.outHeight;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90
                || orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            int temp = srcWidth;
            srcWidth = srcHeight;
            srcHeight = temp;
        }

        float maxHeight = 816f;
        float maxWidth = 612f;
        int targetWidth = srcWidth;
        int targetHeight = srcHeight;

        if (targetHeight > maxHeight || targetWidth > maxWidth) {
            float imgRatio = (float) targetWidth / targetHeight;
            float maxRatio = maxWidth / maxHeight;
            if (imgRatio < maxRatio) {
                targetWidth = Math.round(maxHeight * imgRatio);
                targetHeight = Math.round(maxHeight);
            } else if (imgRatio > maxRatio) {
                targetHeight = Math.round(maxWidth / imgRatio);
                targetWidth = Math.round(maxWidth);
            } else {
                targetHeight = Math.round(maxHeight);
                targetWidth = Math.round(maxWidth);
            }
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);

        Bitmap decoded = BitmapFactory.decodeFile(filePath, options);
        if (decoded == null) {
            return false;
        }

        Bitmap oriented = applyExifOrientation(decoded, orientation);
        if (oriented != decoded) {
            decoded.recycle();
        }

        Bitmap scaled = Bitmap.createScaledBitmap(oriented, targetWidth, targetHeight, true);
        if (scaled != oriented) {
            oriented.recycle();
        }

        try (FileOutputStream out = new FileOutputStream(file)) {
            if (!scaled.compress(Bitmap.CompressFormat.JPEG, 95, out)) {
                scaled.recycle();
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to save compressed image", e);
            scaled.recycle();
            return false;
        }
        scaled.recycle();

        try {
            ExifInterface outputExif = new ExifInterface(filePath);
            outputExif.setAttribute(
                    ExifInterface.TAG_ORIENTATION,
                    String.valueOf(ExifInterface.ORIENTATION_NORMAL)
            );
            outputExif.saveAttributes();
        } catch (IOException e) {
            Log.e(TAG, "Failed to reset EXIF orientation", e);
        }

        return true;
    }

    private static Bitmap applyExifOrientation(Bitmap bitmap, int orientation) {
        int degrees;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degrees = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degrees = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degrees = 270;
                break;
            default:
                return bitmap;
        }
        return rotateImage(bitmap, degrees);
    }



    /**
     * @param  options object option
     * @param reqWidth  Width of bitmap
     * @param reqHeight  Height of bitmap
     * @return inSampleSize =integer value of image size
     *
     * */

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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

}
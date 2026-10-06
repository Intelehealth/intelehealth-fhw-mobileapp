package org.intelehealth.app.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.util.Log;
import org.intelehealth.app.utilities.CustomLog;

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

    /*
     * Upload policy for obs images (physical exam and additional documents). These images are sent
     * to POST /openmrs/ws/rest/v1/obs as multipart "file" parts during sync.
     *
     * - nginx in front of OpenMRS rejects request bodies above its configured limit with HTTP 413
     *   (observed: multi-MB uploads rejected, ~0.5 MB accepted; exact limit not yet confirmed).
     * - 1280 px on the longest side at JPEG quality 85 gives ~100-350 KB for typical phone photos
     *   and screenshots: well inside the accepted size, yet sharper than the camera flow (612x816).
     *
     * Revisit these values together with the backend upload limit, not in isolation.
     */
    public static final int UPLOAD_IMAGE_MAX_SIDE_PX = 1280;
    public static final int UPLOAD_IMAGE_JPEG_QUALITY = 85;

    /**
     * Opens a new stream to a source image each time it is called.
     * <p>
     * {@link #saveAsJpeg} reads the source three times (dimensions, pixels, EXIF) and an InputStream
     * can only be consumed once, so callers pass a factory instead of a stream, e.g.
     * {@code () -> getContentResolver().openInputStream(uri)} or {@code () -> new FileInputStream(file)}.
     */
    public interface StreamSource {
        InputStream open() throws IOException;
    }

    /**
     * Returns the real format of an image file by reading its first bytes ("magic numbers"):
     * "JPEG", "PNG", "WEBP", "HEIF", "UNKNOWN(&lt;hex&gt;)", or "MISSING"/"UNREADABLE".
     * <p>
     * The file extension cannot be trusted: every obs image is stored as {@code <obsUuid>.jpg},
     * but older builds copied gallery files byte-for-byte, so a ".jpg" may actually hold PNG data.
     */
    public static String detectImageFormat(File file) {
        if (file == null || !file.exists()) return "MISSING";
        byte[] header = new byte[12];
        int read;
        try (InputStream in = new FileInputStream(file)) {
            read = in.read(header);
        } catch (IOException e) {
            return "UNREADABLE";
        }
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < Math.max(read, 0); i++) hex.append(String.format("%02x", header[i]));
        String h = hex.toString();
        if (h.startsWith("ffd8ff")) return "JPEG";     // FF D8 FF
        if (h.startsWith("89504e47")) return "PNG";    // 89 'P' 'N' 'G'
        if (h.startsWith("52494646")) return "WEBP";   // "RIFF" container
        if (h.length() >= 16 && h.substring(8, 16).equals("66747970")) return "HEIF"; // "ftyp" at byte 4
        return "UNKNOWN(" + h + ")";
    }

    /**
     * True if the file can be uploaded as-is: it is a real JPEG and its longest side is within
     * {@link #UPLOAD_IMAGE_MAX_SIDE_PX}. Reads only the header (inJustDecodeBounds), so it is cheap.
     * <p>
     * The obs image sync uses this to spot files saved by older builds (PNG renamed to .jpg, or
     * multi-MB originals) that the server would reject, and re-encodes them before uploading.
     */
    public static boolean isUploadReadyJpeg(File file) {
        if (!"JPEG".equals(detectImageFormat(file))) return false;
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), bounds);
        return Math.max(bounds.outWidth, bounds.outHeight) <= UPLOAD_IMAGE_MAX_SIDE_PX;
    }

    /**
     * Decodes any image Android can read (JPEG, PNG, WEBP, HEIF...) and writes it to {@code dest}
     * as a JPEG the server accepts. Used for gallery picks and to repair already-saved images.
     * <p>
     * Why each step exists:
     * <ul>
     *   <li><b>JPEG output</b>: OpenMRS (ImageHandler) saves the upload in the format implied by the
     *       ".jpg" file name. Its JPEG writer fails on images with an alpha channel (e.g. PNG
     *       screenshots) and the API returns HTTP 500 "Trying to write complex obs to the file system".</li>
     *   <li><b>White background</b>: JPEG has no transparency. Drawing onto opaque white removes the
     *       alpha channel and keeps transparent areas from turning black.</li>
     *   <li><b>Downscale to maxSide</b>: keeps the request under the nginx body limit (HTTP 413).
     *       Smaller images are never upscaled.</li>
     *   <li><b>EXIF rotation</b>: phone photos often store orientation only as EXIF metadata, which is
     *       dropped when re-encoding (and ignored by the server), so the rotation is applied to the pixels.</li>
     *   <li><b>Temp file + rename</b>: a failure never leaves a half-written file, and {@code dest} may
     *       be the very file the source reads from (in-place repair during sync).</li>
     * </ul>
     * Does disk and CPU work; call it from a background thread.
     *
     * @param source  opens a new stream to the original image on each call
     * @param dest    output file; may be the same file the source reads from
     * @param maxSide maximum width/height of the output in pixels
     * @param quality JPEG quality, 0-100
     * @return true if {@code dest} now holds the converted JPEG; false if the image could not be
     *         decoded or written, in which case {@code dest} is left unchanged
     */
    public static boolean saveAsJpeg(StreamSource source, File dest, int maxSide, int quality) {
        Bitmap decoded = null;
        Bitmap output = null;
        File temp = new File(dest.getAbsolutePath() + ".tmp");
        try {
            // 1. Read only the dimensions; no pixel memory is allocated.
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream in = source.open()) {
                BitmapFactory.decodeStream(in, null, bounds);
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return false; // not a decodable image

            // 2. Decode at reduced size so a large photo never needs full-resolution memory
            //    (a 50 MP image would be ~200 MB as a bitmap). inSampleSize must be a power of two;
            //    use the largest one that still keeps the long side >= maxSide, so the exact
            //    resize in step 3 loses no detail.
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            int longSide = Math.max(bounds.outWidth, bounds.outHeight);
            while (longSide / (options.inSampleSize * 2) >= maxSide) options.inSampleSize *= 2;
            try (InputStream in = source.open()) {
                decoded = BitmapFactory.decodeStream(in, null, options);
            }
            if (decoded == null) return false;

            // 3. One transform: exact scale down to maxSide, then EXIF rotation. Rotating around the
            //    origin moves the image into negative coordinates, so shift it back to (0,0).
            float scale = Math.min(1f, (float) maxSide / Math.max(decoded.getWidth(), decoded.getHeight()));
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            matrix.postRotate(readExifRotation(source));
            RectF outBounds = new RectF(0, 0, decoded.getWidth(), decoded.getHeight());
            matrix.mapRect(outBounds);
            matrix.postTranslate(-outBounds.left, -outBounds.top);

            // 4. Draw onto an opaque white canvas to drop any alpha channel.
            output = Bitmap.createBitmap(Math.round(outBounds.width()), Math.round(outBounds.height()), Bitmap.Config.ARGB_8888);
            output.eraseColor(Color.WHITE);
            new Canvas(output).drawBitmap(decoded, matrix, new Paint(Paint.FILTER_BITMAP_FLAG));

            // 5. Write to a temp file, then rename over dest (replaces dest in one step).
            try (OutputStream out = new FileOutputStream(temp)) {
                if (!output.compress(Bitmap.CompressFormat.JPEG, quality, out)) return false;
            }
            return temp.renameTo(dest);
        } catch (IOException | RuntimeException | OutOfMemoryError e) {
            // OutOfMemoryError is caught on purpose: an oversized image should fail this one
            // conversion, not crash the app.
            Log.e(TAG, "saveAsJpeg failed for " + dest.getName(), e);
            return false;
        } finally {
            if (decoded != null) decoded.recycle();
            if (output != null) output.recycle();
            if (temp.exists()) temp.delete(); // only left behind if something failed
        }
    }

    /**
     * Returns the clockwise rotation (0, 90, 180 or 270) stored in the image's EXIF orientation tag.
     * Images without EXIF (most PNG/WEBP) or with unreadable metadata return 0.
     */
    private static int readExifRotation(StreamSource source) {
        try (InputStream in = source.open()) {
            int orientation = new ExifInterface(in).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90: return 90;
                case ExifInterface.ORIENTATION_ROTATE_180: return 180;
                case ExifInterface.ORIENTATION_ROTATE_270: return 270;
                default: return 0;
            }
        } catch (IOException | RuntimeException e) {
            // No EXIF block (typical for PNG/WEBP) or unsupported format: treat as upright.
            return 0;
        }
    }
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

            CustomLog.d("AdditionalDocuments", outputPath);

        } catch (FileNotFoundException fnfe1) {
            CustomLog.e("AdditionalDocuments", fnfe1.getMessage());
        } catch (Exception e) {
            CustomLog.v("AdditionalDocuments", e.getMessage());
        }
    }

    /**
     * Compress the file into bitmap
     *
     * @param filePath path of file to be compressed
     * */

    public static boolean fileCompressed(String filePath) {
        File file = new File(filePath);

        CustomLog.d(TAG, "fileCompressed: filePath : "+filePath);
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
        CustomLog.d(TAG, "fileCompressed: actualWidth : "+actualWidth);
        CustomLog.d(TAG, "fileCompressed: actualHeight : "+actualHeight);

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
            CustomLog.e("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                CustomLog.e("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                CustomLog.e("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                CustomLog.e("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(),
                    matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        FileOutputStream out = null;
        String filename = filePath;
        try {
            out = new FileOutputStream(file);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (bmp != null) {
                bmp.recycle();
                bmp = null;
            }
            if (scaledBitmap != null) {
                scaledBitmap.recycle();
            }
        }
        return true;
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
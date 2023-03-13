package org.intelehealth.ihutils.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;



import org.intelehealth.ihutils.data.UtilsConstant;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class Base64Utils {
    private String TAG = Base64Utils.class.getSimpleName();

    public String encoded(String USERNAME, String PASSWORD) {
        String encoded = null;
        encoded = Base64.encodeToString((USERNAME + ":" + PASSWORD).getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
        return encoded;
    }

    public String getBase64FromFileWithConversion(String path) {
        Bitmap bmp = null;
        ByteArrayOutputStream baos = null;
        byte[] baat = null;
        String encodeString = "";
        try {
            bmp = BitmapFactory.decodeFile(path);
            baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, UtilsConstant.IMAGE_JPG_QUALITY, baos);
            baat = baos.toByteArray();
            encodeString = Base64.encodeToString(baat, Base64.DEFAULT);
        } catch (Exception e) {
            //FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }

        return encodeString;
    }
}

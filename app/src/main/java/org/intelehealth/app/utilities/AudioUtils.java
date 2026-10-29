package org.intelehealth.app.utilities;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioUtils {
    public static File saveWavFile(Context context, byte[] audioData) throws IOException {

        File file = new File(context.getExternalFilesDir(null),
                "stetho_" + System.currentTimeMillis() + ".wav");

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(audioData);
        fos.close();

        return file;
    }
}

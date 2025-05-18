package org.intelehealth.app.activities.visit.download_doc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;

public class SaveDoctorDocument {
    public boolean saveFile(ResponseBody body, File destinationFile) {
        try {
            InputStream inputStream = body.byteStream();
            OutputStream outputStream = new FileOutputStream(destinationFile);

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            return true;
        } catch (IOException exception) {
            exception.printStackTrace();
            return false;
        }
    }
}

package org.intelehealth.app.activities.visit.download_doc;

import android.os.Environment;

import org.intelehealth.app.app.AppConstants;

import java.io.File;

public class DownloadDoctorDocUtils {

    public static String getDocumentFileName(String visitID) {
        return "Doctor-Additional-Doc-" + visitID + ".pdf";
    }

    public static String getDoctorsAdditionalDocumentUrl(String obsUuid) {
        String baseUrl = AppConstants.DOCTOR_DOCUMENT_BASE_URL;
        String stringToReplace = AppConstants.DOCTORS_URL_STRING_TO_REPLACE;
        return baseUrl.replace(stringToReplace, obsUuid);
    }

    public static File getDocumentFile(String fileName) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return new File(downloadsDir, fileName);
    }

}
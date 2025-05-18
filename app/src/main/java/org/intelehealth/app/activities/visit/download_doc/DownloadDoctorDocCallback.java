package org.intelehealth.app.activities.visit.download_doc;

import java.io.File;

public interface DownloadDoctorDocCallback {
    void onDownloadStarted();

    void onDownloadComplete(File downloadedFile);

    void onDownloadFailed();
}

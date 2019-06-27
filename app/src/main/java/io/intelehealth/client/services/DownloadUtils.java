package io.intelehealth.client.services;

public class DownloadUtils {

    protected String getStringByteSize(int size) {
        if (size > 1024 * 1024)  //mega
        {
            return String.format("%.1f MB", size / (float) (1024 * 1024));
        } else if (size > 1024)  //kilo
        {
            return String.format("%.1f KB", size / 1024.0f);
        } else {
            return String.format("%d B");
        }
    }
}

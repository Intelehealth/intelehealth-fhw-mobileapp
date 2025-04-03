package org.intelehealth.klivekit.utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {
    private Context context;
    private Properties properties;

    public PropertyReader(Context context) {
        this.context = context;
        properties = new Properties();
    }

    public Properties getMyProperties(String file) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(file);
            properties.load(inputStream);

        } catch (Exception e) {
            System.out.print(e.getMessage());
        }

        return properties;
    }

    public String getAwsAccessID() {
        if (properties != null) {
            return properties.getProperty("S3_ACCESS_ID");
        }
        return "";
    }

    public String getAwsSecretKey() {
        if (properties != null) {
            return properties.getProperty("S3_SECRET_KEY");
        }
        return "";
    }

    public String getAwsS3BucketName() {
        if (properties != null) {
            return properties.getProperty("S3_BUCKET_NAME");
        }
        return "";
    }

    public String getAwsS3BucketPrefixUrl() {
        if (properties != null) {
            return properties.getProperty("S3_BUCKET_PREFIX_URL");
        }
        return "";
    }
}
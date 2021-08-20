package org.intelehealth.ekalhelpline.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BucketResponse {
    @SerializedName("data")
    @Expose
    public List<Bucket> data;
    @SerializedName("status")
    @Expose
    public String status;

    public static class Bucket {
        @SerializedName("bucketId")
        @Expose
        public int bucketId;
        @SerializedName("bucketName")
        @Expose
        public String bucketName;

        @Override
        public String toString() {
            return bucketName;
        }
    }
}

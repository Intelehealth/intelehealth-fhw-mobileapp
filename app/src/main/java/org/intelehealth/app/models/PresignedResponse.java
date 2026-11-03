package org.intelehealth.app.models;

public class PresignedResponse {
    public String tracker;
    public String presigned_url;
    public String expires_in_seconds;

    public String getTracker() {
        return tracker;
    }

    public void setTracker(String tracker) {
        this.tracker = tracker;
    }

    public String getPresigned_url() {
        return presigned_url;
    }

    public void setPresigned_url(String presigned_url) {
        this.presigned_url = presigned_url;
    }

    public String getExpires_in_seconds() {
        return expires_in_seconds;
    }

    public void setExpires_in_seconds(String expires_in_seconds) {
        this.expires_in_seconds = expires_in_seconds;
    }
}

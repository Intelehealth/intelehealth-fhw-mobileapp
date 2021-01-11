package app.intelehealth.client.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CheckAppUpdateRes {

    @SerializedName("latestVersion")
    @Expose
    public String latestVersion;
    @SerializedName("latestVersionCode")
    @Expose
    public String latestVersionCode;
    @SerializedName("url")
    @Expose
    public String url;
    @SerializedName("releaseNotes")
    @Expose
    public List<String> releaseNotes = null;

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public String getLatestVersionCode() {
        return latestVersionCode;
    }

    public void setLatestVersionCode(String latestVersionCode) {
        this.latestVersionCode = latestVersionCode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(List<String> releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

}

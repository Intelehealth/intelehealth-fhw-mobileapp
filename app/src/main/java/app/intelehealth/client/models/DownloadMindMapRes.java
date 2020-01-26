package app.intelehealth.client.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sagar Shimpi
 * Github - Sagars23
 */
public class DownloadMindMapRes {

    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("mindmap")
    @Expose
    public String mindmap;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMindmap() {
        return mindmap;
    }

    public void setMindmap(String mindmap) {
        this.mindmap = mindmap;
    }
}

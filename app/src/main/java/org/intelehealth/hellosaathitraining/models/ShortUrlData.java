package org.intelehealth.hellosaathitraining.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ShortUrlData {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("hash")
    @Expose
    private String hash;
    @SerializedName("link")
    @Expose
    private String link;
    @SerializedName("createdAt")
    @Expose
    private String createdAt;
    @SerializedName("updatedAt")
    @Expose
    private String updatedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}

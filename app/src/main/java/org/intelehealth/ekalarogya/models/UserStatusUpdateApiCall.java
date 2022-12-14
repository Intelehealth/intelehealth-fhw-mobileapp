package org.intelehealth.ekalarogya.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.intelehealth.ekalarogya.models.pushResponseApiCall.Data;

public class UserStatusUpdateApiCall {
    @SerializedName("userUuid")
    @Expose
    private String userUuid;

    @SerializedName("currentTimestamp")
    @Expose
    private long currentTimestamp;

    @SerializedName("version")
    @Expose
    private String version;

    @SerializedName("androidVersion")
    @Expose
    private String androidVersion;

    @SerializedName("device")
    @Expose
    private String device;

    @SerializedName("deviceModel")
    @Expose
    private String deviceModel;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("lastSyncTimestamp")
    @Expose
    private Long lastSyncTimestamp;

    @SerializedName("lastActivity")
    @Expose
    private String lastActivity;

    @SerializedName("status")
    @Expose
    private String status;

    @SerializedName("village")
    @Expose
    private String village;

    @SerializedName("sanch")
    @Expose
    private String sanch;

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public Long getCurrentTimestamp() {
        return currentTimestamp;
    }

    public void setCurrentTimestamp(Long currentTimestamp) {
        this.currentTimestamp = currentTimestamp;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getName() {
        return name;
    }

    public void setName(String userName) {
        this.name = userName;
    }

    public long getLastSyncTimestamp() {
        return lastSyncTimestamp;
    }

    public void setLastSyncTimestamp(long lastSyncTimestamp) {
        this.lastSyncTimestamp = lastSyncTimestamp;
    }

    public String getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(String lastActivity) {
        this.lastActivity = lastActivity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getSanch() {
        return sanch;
    }

    public void setSanch(String sanch) {
        this.sanch = sanch;
    }
}

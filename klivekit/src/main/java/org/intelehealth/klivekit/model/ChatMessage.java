
package org.intelehealth.klivekit.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class ChatMessage {

    @SerializedName("createdAt")
    private String mCreatedAt;
    @SerializedName("fromUser")
    private String mFromUser;
    @SerializedName("id")
    private int mId;
    @SerializedName("isRead")
    private boolean mIsRead;
    @SerializedName("message")
    private String mMessage;
    @SerializedName("patientId")
    private String mPatientId;

    @SerializedName("patientName")
    private String patientName;

    private String hwName;

    private String hwPic;

    private String patientPic;

    @SerializedName("toUser")
    private String mToUser;

    private String visitId;

    @SerializedName("updatedAt")
    private String mUpdatedAt;

    @SerializedName("type")
    private String type;

    private int layoutType;

    private boolean loading;

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(String createdAt) {
        mCreatedAt = createdAt;
    }

    public String getFromUser() {
        return mFromUser;
    }

    public void setFromUser(String fromUser) {
        mFromUser = fromUser;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public boolean getIsRead() {
        return mIsRead;
    }

    public void setIsRead(boolean isRead) {
        mIsRead = isRead;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getPatientId() {
        return mPatientId;
    }

    public void setPatientId(String patientId) {
        mPatientId = patientId;
    }

    public String getToUser() {
        return mToUser;
    }

    public void setToUser(String toUser) {
        mToUser = toUser;
    }

    public String getUpdatedAt() {
        return mUpdatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        mUpdatedAt = updatedAt;
    }

    public void setLayoutType(int layoutType) {
        this.layoutType = layoutType;
    }

    public int getLayoutType() {
        return layoutType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHwName() {
        return hwName;
    }

    public String getHwPic() {
        return hwPic;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getPatientPic() {
        return patientPic;
    }

    public void setHwName(String hwName) {
        this.hwName = hwName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public String getVisitId() {
        return visitId;
    }

    public boolean isAttachment() {
        if (type == null) return false;
        return type.equals("attachment");
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}

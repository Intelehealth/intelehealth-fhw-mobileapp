package org.intelehealth.ekalarogya.knowledgeEngine.ncd;

import android.os.Parcel;
import android.os.Parcelable;

public class ActionResult implements Parcelable {
    private String target;
    private String targetData;
    private String popupMessage;
    private boolean isInsideNodeDataUpdate;

    protected ActionResult(Parcel in) {
        target = in.readString();
        targetData = in.readString();
        popupMessage = in.readString();
        isInsideNodeDataUpdate = in.readByte() != 0;
    }

    public ActionResult() {

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(target);
        dest.writeString(targetData);
        dest.writeString(popupMessage);
        dest.writeByte((byte) (isInsideNodeDataUpdate ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ActionResult> CREATOR = new Creator<ActionResult>() {
        @Override
        public ActionResult createFromParcel(Parcel in) {
            return new ActionResult(in);
        }

        @Override
        public ActionResult[] newArray(int size) {
            return new ActionResult[size];
        }
    };

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTargetData() {
        return targetData;
    }

    public void setTargetData(String targetData) {
        this.targetData = targetData;
    }

    public boolean isInsideNodeDataUpdate() {
        return isInsideNodeDataUpdate;
    }

    public void setInsideNodeDataUpdate(boolean insideNodeDataUpdate) {
        isInsideNodeDataUpdate = insideNodeDataUpdate;
    }


    public String getPopupMessage() {
        return popupMessage;
    }

    public void setPopupMessage(String popupMessage) {
        this.popupMessage = popupMessage;
    }
}

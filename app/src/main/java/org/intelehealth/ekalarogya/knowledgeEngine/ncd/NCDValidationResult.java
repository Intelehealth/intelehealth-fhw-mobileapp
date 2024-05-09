package org.intelehealth.ekalarogya.knowledgeEngine.ncd;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.intelehealth.ekalarogya.knowledgeEngine.Node;
/**
 * Represents the result of Node validation.
 * This class implements Parcelable to support passing instances between components in Android.
 * Created by Lincon Pradhan on 9th May, 2024
 * Contact me: lincon@intelehealth.org
 */
public class NCDValidationResult implements Parcelable {
    private String targetNodeID;
    private boolean isReadyToEndTheScreening;
    private Node updatedNode;

    protected NCDValidationResult(Parcel in) {
        targetNodeID = in.readString();
        isReadyToEndTheScreening = in.readByte() != 0;
    }

    public static final Creator<NCDValidationResult> CREATOR = new Creator<NCDValidationResult>() {
        @Override
        public NCDValidationResult createFromParcel(Parcel in) {
            return new NCDValidationResult(in);
        }

        @Override
        public NCDValidationResult[] newArray(int size) {
            return new NCDValidationResult[size];
        }
    };

    public NCDValidationResult() {

    }

    public String getTargetNodeID() {
        return targetNodeID;
    }

    public void setTargetNodeID(String targetNodeID) {
        this.targetNodeID = targetNodeID;
    }

    public boolean isReadyToEndTheScreening() {
        return isReadyToEndTheScreening;
    }

    public void setReadyToEndTheScreening(boolean readyToEndTheScreening) {
        isReadyToEndTheScreening = readyToEndTheScreening;
    }

    public Node getUpdatedNode() {
        return updatedNode;
    }

    public void setUpdatedNode(Node updatedNode) {
        this.updatedNode = updatedNode;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(targetNodeID);
        parcel.writeByte((byte) (isReadyToEndTheScreening ? 1 : 0));
    }
}

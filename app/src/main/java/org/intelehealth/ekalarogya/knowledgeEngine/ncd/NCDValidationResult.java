package org.intelehealth.ekalarogya.knowledgeEngine.ncd;

import android.os.Parcel;
import android.os.Parcelable;

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
    private boolean moveToNextQuestion;
    private Node updatedNode;
    private ActionResult actionResult;



    public NCDValidationResult() {

    }

    protected NCDValidationResult(Parcel in) {
        targetNodeID = in.readString();
        isReadyToEndTheScreening = in.readByte() != 0;
        moveToNextQuestion = in.readByte() != 0;
        actionResult = in.readParcelable(ActionResult.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(targetNodeID);
        dest.writeByte((byte) (isReadyToEndTheScreening ? 1 : 0));
        dest.writeByte((byte) (moveToNextQuestion ? 1 : 0));
        dest.writeParcelable(actionResult, flags);
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


    public ActionResult getActionResult() {
        return actionResult;
    }

    public void setActionResult(ActionResult actionResult) {
        this.actionResult = actionResult;
    }

    public boolean isMoveToNextQuestion() {
        return moveToNextQuestion;
    }

    public void setMoveToNextQuestion(boolean moveToNextQuestion) {
        this.moveToNextQuestion = moveToNextQuestion;
    }
}

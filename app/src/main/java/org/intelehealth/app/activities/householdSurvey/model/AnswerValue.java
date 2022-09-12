package org.intelehealth.app.activities.householdSurvey.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class AnswerValue implements Parcelable {

    @SerializedName("en")
    private String enValue;

    @SerializedName("ar")
    private String arValue;

    public AnswerValue() {

    }

    protected AnswerValue(Parcel in) {
        enValue = in.readString();
        arValue = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(enValue);
        dest.writeString(arValue);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AnswerValue> CREATOR = new Creator<AnswerValue>() {
        @Override
        public AnswerValue createFromParcel(Parcel in) {
            return new AnswerValue(in);
        }

        @Override
        public AnswerValue[] newArray(int size) {
            return new AnswerValue[size];
        }
    };

    public String getArValue() {
        return arValue;
    }

    public void setArValue(String arValue) {
        this.arValue = arValue;
    }

    public String getEnValue() {
        return enValue;
    }

    public void setEnValue(String enValue) {
        this.enValue = enValue;
    }
}

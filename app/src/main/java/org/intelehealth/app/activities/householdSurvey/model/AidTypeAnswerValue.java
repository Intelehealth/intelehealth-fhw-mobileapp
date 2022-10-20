package org.intelehealth.app.activities.householdSurvey.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AidTypeAnswerValue implements Parcelable {

    @SerializedName("en")
    private List<String>  enValues;

    @SerializedName("ar")
    private List<String> arValues;

    public AidTypeAnswerValue() {

    }

    protected AidTypeAnswerValue(Parcel in) {
        enValues = in.createStringArrayList();
        arValues = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(enValues);
        dest.writeStringList(arValues);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AidTypeAnswerValue> CREATOR = new Creator<AidTypeAnswerValue>() {
        @Override
        public AidTypeAnswerValue createFromParcel(Parcel in) {
            return new AidTypeAnswerValue(in);
        }

        @Override
        public AidTypeAnswerValue[] newArray(int size) {
            return new AidTypeAnswerValue[size];
        }
    };

    public List<String> getEnValues() {
        return enValues;
    }

    public void setEnValues(List<String> enValues) {
        this.enValues = enValues;
    }

    public List<String> getArValues() {
        return arValues;
    }

    public void setArValues(List<String> arValues) {
        this.arValues = arValues;
    }
}

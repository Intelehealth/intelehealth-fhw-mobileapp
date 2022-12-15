package org.intelehealth.app.ayu.visit.model;

import android.os.Parcel;
import android.os.Parcelable;

public class BMIStatus implements Parcelable {
    private int color;
    private String status;

    public BMIStatus(){

    }
    protected BMIStatus(Parcel in) {
        color = in.readInt();
        status = in.readString();
    }

    public static final Creator<BMIStatus> CREATOR = new Creator<BMIStatus>() {
        @Override
        public BMIStatus createFromParcel(Parcel in) {
            return new BMIStatus(in);
        }

        @Override
        public BMIStatus[] newArray(int size) {
            return new BMIStatus[size];
        }
    };

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(color);
        parcel.writeString(status);
    }
}

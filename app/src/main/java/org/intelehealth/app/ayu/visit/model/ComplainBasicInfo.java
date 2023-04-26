package org.intelehealth.app.ayu.visit.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ComplainBasicInfo implements Parcelable {
    private int optionSize;
    private String complainName;
    private boolean isAssociateSymptom;
    private boolean isPhysicalExam;
    private boolean isPatientHistory;

    public ComplainBasicInfo() {
    }

    protected ComplainBasicInfo(Parcel in) {
        optionSize = in.readInt();
        complainName = in.readString();
        isAssociateSymptom = in.readByte() != 0;
        isPhysicalExam = in.readByte() != 0;
        isPatientHistory = in.readByte() != 0;
        isFamilyHistory = in.readByte() != 0;
    }

    public static final Creator<ComplainBasicInfo> CREATOR = new Creator<ComplainBasicInfo>() {
        @Override
        public ComplainBasicInfo createFromParcel(Parcel in) {
            return new ComplainBasicInfo(in);
        }

        @Override
        public ComplainBasicInfo[] newArray(int size) {
            return new ComplainBasicInfo[size];
        }
    };

    public int getOptionSize() {
        return optionSize;
    }

    public void setOptionSize(int optionSize) {
        this.optionSize = optionSize;
    }

    public String getComplainName() {
        return complainName;
    }

    public void setComplainName(String complainName) {
        this.complainName = complainName;
    }

    public boolean isAssociateSymptom() {
        return isAssociateSymptom;
    }

    public void setAssociateSymptom(boolean associateSymptom) {
        isAssociateSymptom = associateSymptom;
    }

    public boolean isPhysicalExam() {
        return isPhysicalExam;
    }

    public void setPhysicalExam(boolean physicalExam) {
        isPhysicalExam = physicalExam;
    }

    public boolean isPatientHistory() {
        return isPatientHistory;
    }

    public void setPatientHistory(boolean patientHistory) {
        isPatientHistory = patientHistory;
    }

    public boolean isFamilyHistory() {
        return isFamilyHistory;
    }

    public void setFamilyHistory(boolean familyHistory) {
        isFamilyHistory = familyHistory;
    }

    private boolean isFamilyHistory;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(optionSize);
        parcel.writeString(complainName);
        parcel.writeByte((byte) (isAssociateSymptom ? 1 : 0));
        parcel.writeByte((byte) (isPhysicalExam ? 1 : 0));
        parcel.writeByte((byte) (isPatientHistory ? 1 : 0));
        parcel.writeByte((byte) (isFamilyHistory ? 1 : 0));
    }
}

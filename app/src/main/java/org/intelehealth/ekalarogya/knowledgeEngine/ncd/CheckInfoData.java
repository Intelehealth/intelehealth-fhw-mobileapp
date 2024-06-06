package org.intelehealth.ekalarogya.knowledgeEngine.ncd;

import android.os.Parcel;
import android.os.Parcelable;

public class CheckInfoData implements Parcelable {
    private String dataName;
    private String condition;
    private boolean havingAssociateCondition;
    private String associateOperator;

    private String checkSectionName;


    public CheckInfoData() {

    }

    protected CheckInfoData(Parcel in) {
        dataName = in.readString();
        condition = in.readString();
        havingAssociateCondition = in.readByte() != 0;
        associateOperator = in.readString();
        checkSectionName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(dataName);
        dest.writeString(condition);
        dest.writeByte((byte) (havingAssociateCondition ? 1 : 0));
        dest.writeString(associateOperator);
        dest.writeString(checkSectionName);
    }

    public static final Creator<CheckInfoData> CREATOR = new Creator<CheckInfoData>() {
        @Override
        public CheckInfoData createFromParcel(Parcel in) {
            return new CheckInfoData(in);
        }

        @Override
        public CheckInfoData[] newArray(int size) {
            return new CheckInfoData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }


    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }


    public String getAssociateOperator() {
        return associateOperator;
    }

    public void setAssociateOperator(String associateOperator) {
        this.associateOperator = associateOperator;
    }

    public boolean isHavingAssociateCondition() {
        return havingAssociateCondition;
    }

    public void setHavingAssociateCondition(boolean havingAssociateCondition) {
        this.havingAssociateCondition = havingAssociateCondition;
    }

    public String getCheckSectionName() {
        return checkSectionName;
    }

    public void setCheckSectionName(String checkSectionName) {
        this.checkSectionName = checkSectionName;
    }
}

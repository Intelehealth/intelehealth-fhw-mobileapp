package org.intelehealth.ekalarogya.knowledgeEngine.ncd;

import android.os.Parcel;
import android.os.Parcelable;

public class SourceData implements Parcelable {
    private String dataName;
    private String dataType;
    private String value;

    protected SourceData(Parcel in) {
        dataName = in.readString();
        dataType = in.readString();
        value = in.readString();
    }

    public SourceData() {

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(dataName);
        dest.writeString(dataType);
        dest.writeString(value);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SourceData> CREATOR = new Creator<SourceData>() {
        @Override
        public SourceData createFromParcel(Parcel in) {
            return new SourceData(in);
        }

        @Override
        public SourceData[] newArray(int size) {
            return new SourceData[size];
        }
    };

    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

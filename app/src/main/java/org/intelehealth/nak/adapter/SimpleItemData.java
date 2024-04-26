package org.intelehealth.nak.adapter;

import android.os.Parcel;
import android.os.Parcelable;

public class SimpleItemData implements Parcelable {
    private boolean isSelected;
    private String title;
    private String subTitle;
    private Object object;

    public SimpleItemData(){

    }

    protected SimpleItemData(Parcel in) {
        isSelected = in.readByte() != 0;
        title = in.readString();
        subTitle = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isSelected ? 1 : 0));
        dest.writeString(title);
        dest.writeString(subTitle);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SimpleItemData> CREATOR = new Creator<SimpleItemData>() {
        @Override
        public SimpleItemData createFromParcel(Parcel in) {
            return new SimpleItemData(in);
        }

        @Override
        public SimpleItemData[] newArray(int size) {
            return new SimpleItemData[size];
        }
    };

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}

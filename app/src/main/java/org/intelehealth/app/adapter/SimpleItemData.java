package org.intelehealth.app.adapter;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class SimpleItemData implements Parcelable {
    private boolean isSelected;
    private String title;
    private String subTitle;

    protected SimpleItemData(Parcel in) {
        isSelected = in.readByte() != 0;
        title = in.readString();
        subTitle = in.readString();
        titleLocal = in.readString();
        subTitleLocal = in.readString();
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

    public String getTitleLocal() {
        return titleLocal;
    }

    public void setTitleLocal(String titleLocal) {
        this.titleLocal = titleLocal;
    }

    public String getSubTitleLocal() {
        return subTitleLocal;
    }

    public void setSubTitleLocal(String subTitleLocal) {
        this.subTitleLocal = subTitleLocal;
    }

    private String titleLocal;
    private String subTitleLocal;
    private Object object;

    public SimpleItemData(){

    }


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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeByte((byte) (isSelected ? 1 : 0));
        parcel.writeString(title);
        parcel.writeString(subTitle);
        parcel.writeString(titleLocal);
        parcel.writeString(subTitleLocal);
    }
}

package org.intelehealth.app.activities.householdSurvey.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Survey implements Parcelable {

    @SerializedName("title")
    private String title;

    @SerializedName("title_ar")
    private String titleAr;

    @SerializedName("questions")
    private List<Questions> questions;

    protected Survey(Parcel in) {
        title = in.readString();
        titleAr = in.readString();
        questions = in.createTypedArrayList(Questions.CREATOR);
    }

    public static final Creator<Survey> CREATOR = new Creator<Survey>() {
        @Override
        public Survey createFromParcel(Parcel in) {
            return new Survey(in);
        }

        @Override
        public Survey[] newArray(int size) {
            return new Survey[size];
        }
    };

    public List<Questions> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Questions> questions) {
        this.questions = questions;
    }

    public String getTitleAr() {
        return titleAr;
    }

    public void setTitleAr(String titleAr) {
        this.titleAr = titleAr;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(titleAr);
        parcel.writeTypedList(questions);
    }
}

package org.intelehealth.unicef.ayu.visit.model;

import android.os.Parcel;
import android.os.Parcelable;

public class VisitSummaryData implements Parcelable {
    private String question;
    private String displayValue;

    public VisitSummaryData() {

    }

    protected VisitSummaryData(Parcel in) {
        question = in.readString();
        displayValue = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(question);
        dest.writeString(displayValue);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VisitSummaryData> CREATOR = new Creator<VisitSummaryData>() {
        @Override
        public VisitSummaryData createFromParcel(Parcel in) {
            return new VisitSummaryData(in);
        }

        @Override
        public VisitSummaryData[] newArray(int size) {
            return new VisitSummaryData[size];
        }
    };

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }
}

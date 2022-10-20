package org.intelehealth.app.activities.householdSurvey.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SurveyData implements Parcelable {

    @SerializedName("survey_questions")
    private List<Survey> surveyQuestions;

    public SurveyData() {

    }

    protected SurveyData(Parcel in) {
        surveyQuestions = in.createTypedArrayList(Survey.CREATOR);
    }

    public static final Creator<SurveyData> CREATOR = new Creator<SurveyData>() {
        @Override
        public SurveyData createFromParcel(Parcel in) {
            return new SurveyData(in);
        }

        @Override
        public SurveyData[] newArray(int size) {
            return new SurveyData[size];
        }
    };

    public List<Survey> getSurveyQuestions() {
        return surveyQuestions;
    }

    public void setSurveyQuestions(List<Survey> surveyQuestions) {
        this.surveyQuestions = surveyQuestions;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(surveyQuestions);
    }
}

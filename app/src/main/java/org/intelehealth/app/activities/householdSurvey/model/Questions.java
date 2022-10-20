package org.intelehealth.app.activities.householdSurvey.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Questions implements Parcelable {

    @SerializedName("attr_name")
    private String attrName;

    @SerializedName("question")
    private String question;

    @SerializedName("question_ar")
    private String questionAr;

    @SerializedName("data_type")
    private String dataType;

    @SerializedName("AID")
    private List<String> aids;

    @SerializedName("options")
    private List<Options> options;

    @SerializedName("answer")
    private AnswerValue answerValue;


    protected Questions(Parcel in) {
        attrName = in.readString();
        question = in.readString();
        questionAr = in.readString();
        dataType = in.readString();
        aids = in.createStringArrayList();
        options = in.createTypedArrayList(Options.CREATOR);
        answerValue = in.readParcelable(AnswerValue.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(attrName);
        dest.writeString(question);
        dest.writeString(questionAr);
        dest.writeString(dataType);
        dest.writeStringList(aids);
        dest.writeTypedList(options);
        dest.writeParcelable(answerValue, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Questions> CREATOR = new Creator<Questions>() {
        @Override
        public Questions createFromParcel(Parcel in) {
            return new Questions(in);
        }

        @Override
        public Questions[] newArray(int size) {
            return new Questions[size];
        }
    };

    public List<Options> getOptions() {
        return options;
    }

    public void setOptions(List<Options> options) {
        this.options = options;
    }

    public List<String> getAids() {
        return aids;
    }

    public void setAids(List<String> aids) {
        this.aids = aids;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getQuestionAr() {
        return questionAr;
    }

    public void setQuestionAr(String questionAr) {
        this.questionAr = questionAr;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }


    public AnswerValue getAnswerValue() {
        return answerValue;
    }

    public void setAnswerValue(AnswerValue answerValue) {
        this.answerValue = answerValue;
    }
}

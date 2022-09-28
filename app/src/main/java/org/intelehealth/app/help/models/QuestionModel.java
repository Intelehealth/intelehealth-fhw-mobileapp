package org.intelehealth.app.help.models;

public class QuestionModel {
    String question;
    String description;
    boolean isExpanded;


    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public String getDescription() {
        return description;
    }

    public QuestionModel() {
    }

    public QuestionModel(String question, String description) {
        this.question = question;
        this.description = description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}

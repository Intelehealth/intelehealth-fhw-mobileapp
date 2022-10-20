package org.intelehealth.app.activities.identificationActivity;

public class HealthIssuesValidationState {
    private boolean areDetailsCorrect = false;
    private String errorMessage = "";

    public boolean getAreDetailsCorrect() {
        return areDetailsCorrect;
    }

    public void setCorrect(boolean correct) {
        areDetailsCorrect = correct;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
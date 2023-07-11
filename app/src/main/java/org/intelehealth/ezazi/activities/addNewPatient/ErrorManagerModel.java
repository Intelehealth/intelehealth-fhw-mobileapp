package org.intelehealth.ezazi.activities.addNewPatient;

import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import org.intelehealth.ezazi.R;

/**
 * Created by Kaveri Zaware on 10-07-2023
 * email - kaveri@intelehealth.org
 **/
public class ErrorManagerModel {
    public TextInputEditText getTextInputEditText() {
        return inputFieldName;
    }

    public void setTextInputEditText(TextInputEditText textInputEditText) {
        this.inputFieldName = textInputEditText;
    }

    public TextView getTvError() {
        return tvError;
    }

    public void setTvError(TextView tvError) {
        this.tvError = tvError;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public MaterialCardView getCardView() {
        return cardView;
    }

    public void setCardView(MaterialCardView cardView) {
        this.cardView = cardView;
    }

    TextInputEditText inputFieldName;
    TextView tvError;
    String errorMessage;

    public ErrorManagerModel(TextInputEditText inputFieldName, TextView tvError, String errorMessage, MaterialCardView cardView) {
        this.inputFieldName = inputFieldName;
        this.tvError = tvError;
        this.errorMessage = errorMessage;
        this.cardView = cardView;
    }

    MaterialCardView cardView;


}

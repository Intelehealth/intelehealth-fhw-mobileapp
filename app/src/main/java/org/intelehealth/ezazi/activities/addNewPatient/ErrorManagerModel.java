package org.intelehealth.ezazi.activities.addNewPatient;

import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import org.intelehealth.ezazi.R;
import org.w3c.dom.Text;

/**
 * Created by Kaveri Zaware on 10-07-2023
 * email - kaveri@intelehealth.org
 **/
public class ErrorManagerModel {
    public View view;
    TextView tvError;
    String errorMessage;
    MaterialCardView cardView;

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public ErrorManagerModel(View view, TextView tvError, String errorMessage, MaterialCardView cardView) {
        this.view = view;
        this.tvError = tvError;
        this.errorMessage = errorMessage;
        this.cardView = cardView;
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

}

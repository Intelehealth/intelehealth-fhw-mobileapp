package org.intelehealth.ezazi.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

/**
 * Created by Vaghela Mithun R. on 14-06-2023 - 11:54.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class InputChangeValidationListener implements TextWatcher {
    public interface InputValidator {
        boolean validate(String text);

        default void onValidatted(boolean isValid) {

        }
    }

    private final TextInputLayout textInputLayout;
    private final InputValidator validator;
    private EditText editText;
    private String message;

    public InputChangeValidationListener(TextInputLayout textInputLayout, InputValidator validator) {
        this.textInputLayout = textInputLayout;
        this.validator = validator;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!validator.validate(s.toString())) {
            textInputLayout.setError(message);
            editText.requestFocus();
            validator.onValidatted(false);
        } else {
            textInputLayout.setError(null);
            validator.onValidatted(true);

        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public void validate(String message) {
        this.message = message;
        editText = textInputLayout.getEditText();
        if (editText != null) editText.addTextChangedListener(this);
    }
}

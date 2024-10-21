package org.intelehealth.app.ui.custom;

import android.content.Context;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import org.intelehealth.app.utilities.CustomLog;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.internal.CheckableImageButton;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Created by Vaghela Mithun R. on 01-06-2023 - 15:13.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class RightAlignErrorTextInputLayout extends TextInputLayout {
    private static final String TAG = "TextInputLayout";

    public RightAlignErrorTextInputLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public RightAlignErrorTextInputLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RightAlignErrorTextInputLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
    }

    @Override
    public void setErrorEnabled(boolean enabled) {
        super.setErrorEnabled(enabled);
        TextView errorView = findViewById(R.id.textinput_error);
        if (errorView != null) errorView.setTextAlignment(TextView.TEXT_ALIGNMENT_VIEW_END);
        if (enabled) activeErrorClickIfInputIsPassword();
    }

    @Override
    public void setError(@Nullable CharSequence errorText) {
        super.setError(errorText);
        TextView errorView = findViewById(R.id.textinput_error);
        View errorIcon = findViewById(R.id.text_input_error_icon);
        if (errorView != null) errorView.setTextAlignment(TextView.TEXT_ALIGNMENT_VIEW_END);
        if (errorIcon != null && getEditText() != null) {
            errorIcon.setActivated(!(getEditText().getTransformationMethod() instanceof PasswordTransformationMethod));
        }
        activeErrorClickIfInputIsPassword();

    }

    private void activeErrorClickIfInputIsPassword() {
        if (getEndIconMode() == END_ICON_PASSWORD_TOGGLE) {
            setErrorIconOnClickListener(v -> setupPasswordToggleViewMethod(v));
        }
    }

    private void setupPasswordToggleViewMethod(View view) {
        if (getEditText() != null) {
            TransformationMethod transformationMethod = getEditText().getTransformationMethod();
            if (transformationMethod instanceof PasswordTransformationMethod) {
                view.setActivated(true);
                getEditText().setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                view.setActivated(false);
                getEditText().setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            getEditText().setSelection(getEditText().getText().length());
        }
    }

    public void setMultilineInputEndIconGravity() {
        CheckableImageButton imageButton = findViewById(R.id.text_input_end_icon);
        if (imageButton != null && getEditText() != null) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageButton.getLayoutParams();
            params.gravity = Gravity.BOTTOM;
            imageButton.setLayoutParams(params);
        } else if (imageButton == null) {
            CustomLog.e(TAG, "setMultilineInputEndIconGravity: no end icon found");
        } else if (getEditText() == null) {
            CustomLog.e(TAG, "setMultilineInputEndIconGravity: no edit text attached");
        }
    }
}

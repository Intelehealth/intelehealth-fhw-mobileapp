package org.intelehealth.ezazi.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;

import java.lang.reflect.Field;

/**
 * Created by Vaghela Mithun R. on 01-06-2023 - 15:13.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class RightAlignErrorTextInputLayout extends TextInputLayout {
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

    private void init(){
        setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
    }

    @Override
    public void setErrorEnabled(boolean enabled) {
        super.setErrorEnabled(enabled);
        this.findViewById(R.id.textinput_error).setTextAlignment(TextView.TEXT_ALIGNMENT_VIEW_END);
//        if (!enabled) {
//            return;
//        }
//
//        try {
//            Field errorViewField = TextInputLayout.class.getDeclaredField("mErrorView");
//            errorViewField.setAccessible(true);
//            TextView errorView = (TextView) errorViewField.get(this);
//            if (errorView != null) {
//                errorView.setGravity(Gravity.END);
//                errorView.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
//                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                params.gravity = Gravity.END;
//                errorView.setLayoutParams(params);
//            }
//        }
//        catch (Exception e) {
//            // At least log what went wrong
//            e.printStackTrace();
//        }
    }

    @Override
    public void setError(@Nullable CharSequence errorText) {
        super.setError(errorText);
        this.findViewById(R.id.textinput_error).setTextAlignment(TextView.TEXT_ALIGNMENT_VIEW_END);
    }
}

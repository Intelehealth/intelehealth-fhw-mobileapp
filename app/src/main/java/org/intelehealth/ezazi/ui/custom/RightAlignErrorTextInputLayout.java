package org.intelehealth.ezazi.ui.custom;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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
        TextView errorView = findViewById(R.id.textinput_error);
        if (errorView != null) errorView.setTextAlignment(TextView.TEXT_ALIGNMENT_VIEW_END);

//        for (int i = 0; i < getChildCount(); i++) {
//            Log.e(TAG, "getChild " + +(i + 1));
//            Log.e(TAG, "Id " + getChildAt(i).getId());
//            Log.e(TAG, "tag " + getChildAt(i).getTag());
////            parent.getChildAt(i).setVisibility(View.GONE);
//            int resId = getChildAt(i).getId();
//            if (resId > -1)
//                Log.e(TAG, "ResName=>" + Resources.getSystem().getResourceEntryName(resId));
//
//            if (getChildAt(i) instanceof ViewGroup) {
//                findChild((ViewGroup) getChildAt(i));
//            }
//        }
    }

//    private void findChild(ViewGroup group) {
//        for (int i = 0; i < group.getChildCount(); i++) {
//            Log.e(TAG, "ViewGroup getChild " + (i + 1));
//            Log.e(TAG, "ViewGroup Id " + group.getChildAt(i).getId());
//            Log.e(TAG, "ViewGroup tag " + group.getChildAt(i).getTag());
////            parent.getChildAt(i).setVisibility(View.GONE);
//            int resId = group.getChildAt(i).getId();
//            if (resId > -1)
//                Log.e(TAG, "ViewGroup ResName=>" + Resources.getSystem().getResourceEntryName(resId));
//        }
//    }
}

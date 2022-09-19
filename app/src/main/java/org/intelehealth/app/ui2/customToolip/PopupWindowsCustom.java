package org.intelehealth.app.ui2.customToolip;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

public class PopupWindowsCustom {


    PopupWindow mWindow;
    private View mRootView;
    private Context mContext;

    PopupWindowsCustom(Context context) {
        mContext = context;
        mWindow = new PopupWindow(context);
        mWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    mWindow.dismiss();
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_BUTTON_PRESS) {
                    view.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    Context getContext() {
        return mContext;
    }

    void preShow() {
        if (mRootView == null)
            throw new IllegalStateException("setContentView was not called with a view to display.");

        mWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        mWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mWindow.setTouchable(true);
        mWindow.setFocusable(true);
        mWindow.setOutsideTouchable(true);
        mWindow.setContentView(mRootView);
        setShadows();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP) private void setShadows() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mWindow.setElevation(10);
    }

    void setContentView(View root) {
        mRootView = root;
        mWindow.setContentView(root);
    }

    void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        mWindow.setOnDismissListener(listener);
    }

    public void dismiss() {
        mWindow.dismiss();
    }
}


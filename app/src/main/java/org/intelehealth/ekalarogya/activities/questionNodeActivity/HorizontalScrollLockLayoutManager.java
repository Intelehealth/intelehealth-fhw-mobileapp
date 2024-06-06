package org.intelehealth.ekalarogya.activities.questionNodeActivity;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;

public class HorizontalScrollLockLayoutManager extends LinearLayoutManager {

    private boolean isHorizontalScrollEnabled = true;

    public HorizontalScrollLockLayoutManager(Context context) {
        super(context, LinearLayoutManager.HORIZONTAL, false);
    }

    public void setHorizontalScrollEnabled(boolean horizontalScrollEnabled) {
        isHorizontalScrollEnabled = horizontalScrollEnabled;
    }

    @Override
    public boolean canScrollHorizontally() {
        return isHorizontalScrollEnabled && super.canScrollHorizontally();
    }
}

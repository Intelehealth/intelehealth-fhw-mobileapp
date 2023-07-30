package org.intelehealth.ezazi.ui.patient.fragment;

import android.graphics.Point;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.fragment.app.Fragment;

/**
 * Created by Vaghela Mithun R. on 28-07-2023 - 21:10.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public abstract class PatientBaseFragment extends Fragment {
    public PatientBaseFragment(@LayoutRes int layout) {
        super(layout);
    }

    protected void setScrollToFocusedItem() {
        if (requireView().findFocus() != null) {
            Point point = getLocationOnScreen(requireView().findFocus());
            onFocusedViewChanged(point.y);
        }
    }

    protected Point getLocationOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new Point(location[0], location[1]);
    }

    public abstract void onFocusedViewChanged(int y);
}

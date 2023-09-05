package org.intelehealth.ezazi.ui.validation;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Created by Vaghela Mithun R. on 05-09-2023 - 13:19.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class RangeInputFilter implements InputFilter {

    private final int min;
    private final int max;
    private final OnInvalidRangeListener listener;

    public interface OnInvalidRangeListener {
        void onInvalidRange(int min, int max);
    }

    public RangeInputFilter(int min, int max, OnInvalidRangeListener listener) {
        this.min = min;
        this.max = max;
        this.listener = listener;
    }

    public RangeInputFilter(String min, String max, OnInvalidRangeListener listener) {
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
        this.listener = listener;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            int input = Integer.parseInt(source.toString());
            if (isInRange(min, max, input))
                return null;
            else {
                listener.onInvalidRange(min, max);
                return "";
            }
        } catch (NumberFormatException ignored) {
        }
        return "";
    }

    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}

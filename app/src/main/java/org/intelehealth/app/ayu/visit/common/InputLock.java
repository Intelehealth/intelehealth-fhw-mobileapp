package org.intelehealth.app.ayu.visit.common;

import android.widget.EditText;

import androidx.core.content.ContextCompat;

import org.intelehealth.app.R;

/**
 * Locks an input that can no longer be changed, e.g. a BP reading or RBS of an NCD screening visit.
 * A locked field is disabled and shown with a grey box and grey text.
 */
public final class InputLock {

    private InputLock() {
    }

    public static void set(EditText field, boolean locked) {
        field.setEnabled(!locked);
        if (!locked) return;
        // The screens' validation resets these fields to this same box, so the grey stays
        field.setBackgroundResource(R.drawable.bg_input_field_lockable);
        field.setTextColor(ContextCompat.getColorStateList(field.getContext(), R.color.input_text_lockable));
    }
}

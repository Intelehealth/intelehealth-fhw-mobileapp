package org.intelehealth.ezazi.ui.shared;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by Vaghela Mithun R. on 01-09-2023 - 16:51.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public abstract class TextChangeListener implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }
}

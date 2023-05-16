package org.intelehealth.ezazi.utilities;

import android.graphics.Paint;
import android.widget.Button;

import com.google.android.material.button.MaterialButton;

import org.intelehealth.ezazi.R;

/**
 * Created by Vaghela Mithun R. on 16-05-2023 - 13:31.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class TextThemeUtils {
    public static void applyUnderline(Button button) {
        button.setPaintFlags(button.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }
}

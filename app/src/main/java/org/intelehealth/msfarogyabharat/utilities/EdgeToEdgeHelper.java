package org.intelehealth.msfarogyabharat.utilities;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Apps targeting SDK 35+ are drawn edge-to-edge on Android 15+ and can no longer opt out
 * (windowOptOutEdgeToEdgeEnforcement is ignored from SDK 36). This restores the pre-Android 15
 * look for every activity: the window content is padded by the system bar / cutout / keyboard
 * insets, and the status and navigation bar areas are painted with the theme colors.
 */
public final class EdgeToEdgeHelper {

    private EdgeToEdgeHelper() {
    }

    /**
     * Call after the activity's onCreate (i.e. after setContentView).
     */
    public static void apply(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return;

        Window window = activity.getWindow();
        if (window == null) return;
        View root = findContentRoot(window);
        if (root == null) return;

        int[] attrs = {android.R.attr.statusBarColor, android.R.attr.navigationBarColor};
        TypedArray a = activity.getTheme().obtainStyledAttributes(attrs);
        int statusBarColor = a.getColor(0, Color.BLACK);
        int navigationBarColor = a.getColor(1, Color.BLACK);
        a.recycle();

        SystemBarsDrawable barsDrawable = new SystemBarsDrawable(statusBarColor, navigationBarColor);
        Drawable existing = root.getBackground();
        root.setBackground(existing == null ? barsDrawable : new LayerDrawable(new Drawable[]{existing, barsDrawable}));

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            int bottom = bars.bottom;
            // adjustResize no longer resizes the window when drawing edge-to-edge, so make room
            // for the keyboard ourselves. adjustPan / adjustNothing screens are left as they were.
            if (resizesForKeyboard(window)) {
                bottom = Math.max(bottom, windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom);
            }
            v.setPadding(bars.left, bars.top, bars.right, bottom);
            barsDrawable.setInsets(bars);
            // Consumed here so layouts using fitsSystemWindows do not pad a second time.
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(root);
    }

    /**
     * The direct child of the DecorView that hosts the window content (including the action bar).
     */
    @Nullable
    private static View findContentRoot(Window window) {
        View decor = window.getDecorView();
        View view = decor.findViewById(android.R.id.content);
        if (view == null) return null;
        ViewParent parent = view.getParent();
        while (parent instanceof View && parent != decor) {
            view = (View) parent;
            parent = view.getParent();
        }
        return parent == decor ? view : null;
    }

    private static boolean resizesForKeyboard(Window window) {
        int adjust = window.getAttributes().softInputMode & WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST;
        return adjust != WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
                && adjust != WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
    }

    /**
     * Paints the status bar color in the top inset and the navigation bar color in the
     * bottom / side insets, like the system did before edge-to-edge.
     */
    private static final class SystemBarsDrawable extends Drawable {
        private final Paint statusPaint = new Paint();
        private final Paint navigationPaint = new Paint();
        private Insets insets = Insets.NONE;

        SystemBarsDrawable(int statusBarColor, int navigationBarColor) {
            statusPaint.setColor(statusBarColor);
            navigationPaint.setColor(navigationBarColor);
        }

        void setInsets(Insets insets) {
            if (!this.insets.equals(insets)) {
                this.insets = insets;
                invalidateSelf();
            }
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            Rect b = getBounds();
            if (insets.top > 0) {
                canvas.drawRect(b.left, b.top, b.right, b.top + insets.top, statusPaint);
            }
            if (insets.bottom > 0) {
                canvas.drawRect(b.left, b.bottom - insets.bottom, b.right, b.bottom, navigationPaint);
            }
            if (insets.left > 0) {
                canvas.drawRect(b.left, b.top + insets.top, b.left + insets.left, b.bottom - insets.bottom, navigationPaint);
            }
            if (insets.right > 0) {
                canvas.drawRect(b.right - insets.right, b.top + insets.top, b.right, b.bottom - insets.bottom, navigationPaint);
            }
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }
}

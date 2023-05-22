package org.intelehealth.unicef.ui2.customToolip;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;

import androidx.annotation.ColorInt;

public class ArrowDrawableCustom extends ColorDrawable {
    public static final int ARROW_UP = 1;
    public  static final int ARROW_DOWN = 2;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mShadowPaint = new Paint(mPaint);
    private final int gravity;
    private final int stroke;

    private Path mPath;
    private Path mStrokePath;

    ArrowDrawableCustom(int gravity, @ColorInt int foregroundColor, int strokeWidth, int strokeColor)
    {
        this.gravity = gravity;
        this.stroke = strokeWidth;
        mPaint.setColor(foregroundColor);
        mShadowPaint.setColor(strokeColor);
    }

    @Override protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        updatePath(bounds);
        updateShadowPath(bounds);
    }

    private synchronized void updatePath(Rect bounds) {
        mPath = new Path();
        switch (gravity) {
            case ARROW_UP:

                mPath.moveTo(stroke, bounds.height());
                mPath.lineTo(bounds.width()/2, stroke);
                mPath.lineTo(bounds.width()-stroke, bounds.height());
                break;
            case ARROW_DOWN:
                mPath.moveTo(stroke, 0);
                mPath.lineTo(bounds.width()/2, bounds.height()-stroke);
                mPath.lineTo(bounds.width()-stroke, 0);
                break;
        }
        mPath.close();
    }

    private synchronized void updateShadowPath(Rect bounds) {
        mStrokePath = new Path();
        switch (gravity) {
            case ARROW_UP:
                mStrokePath.moveTo(0, bounds.height());
                mStrokePath.lineTo(bounds.width()/2, 0);
                mStrokePath.lineTo(bounds.width(), bounds.height());
                break;
            case ARROW_DOWN:
                mStrokePath.moveTo(0, 0);
                mStrokePath.lineTo(bounds.width()/2, bounds.height());
                mStrokePath.lineTo(bounds.width(), 0);
                break;
        }
        mStrokePath.close();
    }

    @Override public void draw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        if (mPath == null) {
            updatePath(getBounds());
            updateShadowPath(getBounds());
        }
        canvas.drawPath(mStrokePath, mShadowPaint);
        canvas.drawPath(mPath, mPaint);
    }

    public void setColor(@ColorInt int color) {
        mPaint.setColor(color);
    }

    @Override public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override public int getOpacity() {
        if (mPaint.getColorFilter() != null) {
            return PixelFormat.TRANSLUCENT;
        }

        switch (mPaint.getColor() >>> 24) {
            case 255:
                return PixelFormat.OPAQUE;
            case 0:
                return PixelFormat.TRANSPARENT;
        }
        return PixelFormat.TRANSLUCENT;
    }
}

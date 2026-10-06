package org.intelehealth.app.ayu.visit.pocdevice.wave;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class WaveformView extends View {

    private List<Integer> amplitudes = new ArrayList<>();
    private Paint paint = new Paint();

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(4f);
    }

    public void addAmplitude(int amp) {
        amplitudes.add(amp);
        if (amplitudes.size() > 100) {
            amplitudes.remove(0);
        }
        invalidate(); // redraw
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float middle = height / 2;

        float xStep = width / amplitudes.size();

        for (int i = 1; i < amplitudes.size(); i++) {
            float x1 = (i - 1) * xStep;
            float y1 = middle - amplitudes.get(i - 1);
            float x2 = i * xStep;
            float y2 = middle - amplitudes.get(i);

            canvas.drawLine(x1, y1, x2, y2, paint);
        }
    }
}


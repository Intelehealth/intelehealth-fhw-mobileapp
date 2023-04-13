package org.intelehealth.app.widget.materialprogressbar.rhemos_widget.wave;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import org.intelehealth.app.widget.materialprogressbar.rhemos_widget.WaveView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Prajwal Waingankar
 * on February 2023.
 * Github: prajwalmw
 */
public abstract class DrawWave<T> {

    protected final List<T> dataList = new ArrayList<>();
    protected float allDataSize;
    protected View view;

    public DrawWave() {
    }

    public abstract void initWave(float width, float height);

    public void addData(T t) {
        dataList.add(t);
        if (dataList.size() > allDataSize) {
            dataList.remove(0);
        }
        if (view != null) view.postInvalidate();
    }

    /*必须在UI线程更新*/
    public void addDataList(List<T> dataList) {
        if (dataList != null && !dataList.isEmpty()) {
            this.dataList.addAll(dataList);
            if (view != null) view.requestLayout();
        }
    }

    public int getDataSize() {
        return dataList.size();
    }

    public List<T> getDataList() {
        return dataList;
    }

    public void clear() {
        dataList.clear();
        if (view != null) view.postInvalidate();
    }

    public abstract void drawWave(Canvas canvas);

    protected abstract float getX(int value, int size);

    protected abstract float getY(T t);

    public int getWidthMeasureSpec() {
        return 0;
    }

    protected Paint newPaint(int color, float strokeWidth) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.FILL);
        return paint;
    }

    public void setView(WaveView view) {
        this.view = view;
    }
}


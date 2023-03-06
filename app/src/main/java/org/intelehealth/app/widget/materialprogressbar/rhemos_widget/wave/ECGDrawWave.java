package org.intelehealth.app.widget.materialprogressbar.rhemos_widget.wave;

import android.graphics.Canvas;
import android.graphics.Paint;

import org.intelehealth.app.widget.materialprogressbar.rhemos_widget.EcgBackgroundView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Prajwal Waingankar
 * on February 2023.
 * Github: prajwalmw
 */
public class ECGDrawWave extends DrawWave<Integer> {

    //定义ECG波的颜色
    private final static int waveColor = 0xffff0000;
    //定义波的线粗
    private final static float waveStrokeWidth = 2f;


    private float mViewWidth;
    private float mViewHeight;
    private float xS;
    private float dataSpacing;
    private Paint mWavePaint;
    private PaperSpeed paperSpeed = PaperSpeed.VAL_25MM_PER_SEC;
    private Gain gain = Gain.VAL_10MM_PER_MV;

    public ECGDrawWave() {
        super();
        mWavePaint = newPaint(waveColor, waveStrokeWidth);
    }

    @Override
    public void initWave(float width, float height) {
        mViewWidth = width;
        mViewHeight = height;
        xS = EcgBackgroundView.xS;//控件每格子(mm)占的像素值

        final float dataPerLattice = EcgBackgroundView.DATA_PER_SEC / (25.0f * paperSpeed.getScale());//每格波形数据点数
        allDataSize = (int) (EcgBackgroundView.totalLattices * dataPerLattice);
        dataSpacing = xS / dataPerLattice;//每个数据点间距。
    }

    @Override
    public int getWidthMeasureSpec() {
        return (int) ((2 + dataList.size()) * dataSpacing);
    }

    @Override
    public void drawWave(Canvas canvas) {
        final List<Integer> list = new ArrayList<>(dataList);
        int size = list.size();
        if (size >= 2) {
            for (int i = 0; i < size - 1; i++) {
                Integer dataCurr;
                Integer dataNext;
                try {
                    dataCurr = list.get(i);
                } catch (IndexOutOfBoundsException e) {
                    dataCurr = list.get(i - 1);
                }
                try {
                    dataNext = list.get(i + 1);
                } catch (IndexOutOfBoundsException e) {
                    dataNext = list.get(i);
                }
                float x1 = getX(i, size);
                float x2 = getX(i + 1, size);
                float y1 = getY(dataCurr);
                float y2 = getY(dataNext);
                canvas.drawLine(x1, y1, x2, y2, mWavePaint);
            }
        }
    }

    @Override
    protected float getX(int value, int size) {
        try {
            return dataSpacing * value;
        } catch (NullPointerException e) {
            return 0;
        }
    }

    @Override
    protected float getY(Integer data) {
        try {
            return mViewHeight / 2 + data * 18.3f / 128 * xS / 100 * gain.getScale();
        } catch (NullPointerException e) {
            return 0;
        }
    }

    /**
     * 设置增益
     *
     * @param gain y轴增益
     * @see Gain
     */
    public void setGain(Gain gain) {
        if (!this.gain.equals(gain)) {
            this.gain = gain;
            if (view != null) view.postInvalidate();
        }
    }

    /**
     * 设置时间基准（走纸速度）
     *
     * @param paperSpeed 时间基准（走纸速度）值.
     * @see PaperSpeed
     */
    public void setPaperSpeed(PaperSpeed paperSpeed) {
        if (!this.paperSpeed.equals(paperSpeed)) {
            this.paperSpeed = paperSpeed;
            final float dataPerLattice = EcgBackgroundView.DATA_PER_SEC / (25.0f * this.paperSpeed.getScale());//每格波形数据点数
            allDataSize = (int) (EcgBackgroundView.totalLattices * dataPerLattice);
            dataSpacing = xS / dataPerLattice;//每个数据点间距。
            if (view != null) view.postInvalidate();
        }
    }

    public Gain getGain() {
        return this.gain;
    }

    public PaperSpeed getPaperSpeed() {
        return this.paperSpeed;
    }

    /**
     * 时间基准(走纸速度)
     */
    public enum PaperSpeed implements ECGValImp {
        VAL_25MM_PER_SEC(1.0f, "25mm/s"),
        VAL_50MM_PER_SEC(2.0f, "50mm/s");

        private final float scale;
        private final String desc;

        PaperSpeed(float scale, String desc) {
            this.scale = scale;
            this.desc = desc;
        }

        @Override
        public float getScale() {
            return scale;
        }

        @Override
        public String getDesc() {
            return desc;
        }
    }


    /**
     * Y轴增益
     */
    public enum Gain implements ECGValImp {

        VAL_05MM_PER_MV(0.5f, "5mm/mV"),
        VAL_10MM_PER_MV(1.0f, "10mm/mV"),
        VAL_20MM_PER_MV(2.0f, "20mm/mV");

        private final float scale;
        private final String desc;

        Gain(float scale, String desc) {
            this.scale = scale;
            this.desc = desc;
        }

        @Override
        public float getScale() {
            return scale;
        }

        @Override
        public String getDesc() {
            return desc;
        }
    }

    public interface ECGValImp {
        float getScale();

        String getDesc();
    }
}


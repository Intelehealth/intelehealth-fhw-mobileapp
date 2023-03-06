package org.intelehealth.app.widget.materialprogressbar.rhemos_widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import org.intelehealth.app.R;

/**
 * Created by Prajwal Waingankar
 * on February 2023.
 * Github: prajwalmw
 */
public class EcgBackgroundView extends View {

    /**
     * 平均每秒上报的波形数据点数。
     */
    public final static int DATA_PER_SEC = 512;

    private int mSmallGirdColor;
    private int mLargeGridColor;

    private Paint mPaintLargeGrid;
    private Paint mPaintSmallGrid;
    private int sizeX;
    private int sizeY;
    public static float xS;//每格子(mm)占的像素值
    public static float totalLattices;//平均总格子数
    private float mViewWidth;

    private float mViewHeight;
    private float mViewHalfWidth;
    private float mViewHalfHeight;
    private final static float mm2Inches = 0.03937f;

    public EcgBackgroundView(Context context) {
        super(context);
        init();
    }

    public EcgBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTypedArray(context, attrs);
        init();
    }

    public EcgBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTypedArray(context, attrs);
        init();
    }

    @SuppressLint("NewApi")
    public EcgBackgroundView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initTypedArray(context, attrs);
        init();
    }

    private void initTypedArray(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EcgBackgroundView);
        mSmallGirdColor = typedArray.getColor(R.styleable.EcgBackgroundView_smallGridColor, 0xff545767);
        mLargeGridColor = typedArray.getColor(R.styleable.EcgBackgroundView_largeGridColor, 0xff11172a);
        typedArray.recycle();
    }

    private void init() {
        mPaintLargeGrid = new Paint();
        mPaintLargeGrid.setAntiAlias(true);
        mPaintLargeGrid.setColor(mLargeGridColor);
        mPaintLargeGrid.setStrokeWidth(1.0f);
        mPaintLargeGrid.setStyle(Paint.Style.FILL);

        mPaintSmallGrid = new Paint();
        mPaintSmallGrid.setAntiAlias(true);
        mPaintSmallGrid.setColor(mSmallGirdColor);
        mPaintSmallGrid.setStrokeWidth(1.0f);
        mPaintSmallGrid.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < sizeX; i++) {
            final float x1 = mViewHalfWidth - i * xS;
            final float x2 = mViewHalfWidth + i * xS;
            if (i % 5 == 0) {
                canvas.drawLine(x1, 0, x1, mViewHeight, mPaintLargeGrid);
                if (i > 0) canvas.drawLine(x2, 0, x2, mViewHeight, mPaintLargeGrid);
            } else {
                canvas.drawLine(x1, 0, x1, mViewHeight, mPaintSmallGrid);
                canvas.drawLine(x2, 0, x2, mViewHeight, mPaintSmallGrid);
            }
        }
        for (int i = 0; i < sizeY; i++) {
            final float y1 = mViewHalfHeight - i * xS;
            final float y2 = mViewHalfHeight + i * xS;
            if (i % 5 == 0) {
                canvas.drawLine(0, y1, mViewWidth, y1, mPaintLargeGrid);
                if (i > 0) canvas.drawLine(0, y2, mViewWidth, y2, mPaintLargeGrid);
            } else {
                canvas.drawLine(0, y1, mViewWidth, y1, mPaintSmallGrid);
                canvas.drawLine(0, y2, mViewWidth, y2, mPaintSmallGrid);
            }
        }
        //中心点
//        canvas.drawCircle(mViewHalfWidth, mViewHalfHeight, 4f, mPaintSmallGrid);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        initDrawLatticeParams();
        super.onLayout(changed, left, top, right, bottom);
    }

    private void initDrawLatticeParams() {
        mViewWidth = getWidth();
        mViewHeight = getHeight();
        mViewHalfWidth = mViewWidth / 2.0f;
        mViewHalfHeight = mViewHeight / 2.0f;
        final DisplayMetrics dm = getResources().getDisplayMetrics();
        float density = dm.density;
        final float xdpi = dm.xdpi;
        final float ydpi = dm.ydpi;
        final float diffX = xdpi - 160.0f;
        final float diffY = ydpi - 160.0f;
        Log.e("CCL", "xdpi:" + xdpi + ", ydpi:" + ydpi);
        Log.e("CCL", "dm.widthPixels:" + dm.widthPixels + ", dm.heightPixels:" + dm.heightPixels);
        float scale = 1.0f;
//        float scale = 0.884f;
        //判断 xdpi和ydpi 是否约等于160.0
        if ((diffX > -1.0f && diffX < 1.0f) && (diffY > -1.0f && diffY < 1.0f)) {
            //若是 根据根据像素密度校正。
            if (density == 1.75) scale = 0.884f;
            if (density == 2.0d) scale = 1.70f;
            if (density == 3.0d) scale = 2.52f;
        }
        Log.e("CCL", "density:" + density + ", scale:" + scale);
        //算出控件物理英寸宽度和高度
        final float viewXInches = mViewWidth / (xdpi * scale);
        final float viewYInches = mViewHeight / (ydpi * scale);
        //根据毫米和英寸的转换率将英寸转换成毫米，
        // 由于需要以控件中点为中心向两轴画线，所以需要以控件的一半去计算，
        // 以此得出X轴方向和Y轴方向一半布局所需画的格子数。
        final float totalViewXmm = viewXInches / mm2Inches;//控件X轴毫米尺寸
        final float totalViewYmm = viewYInches / mm2Inches;
        final float mmXCtr = 0.5f * totalViewXmm;
        sizeX = (int) mmXCtr;
        if (mmXCtr - sizeX >= 0.5f) sizeX++;
        final double mmYCtr = 0.5f * totalViewYmm;
        sizeY = (int) mmYCtr;
        if (mmYCtr - sizeY >= 0.5f) sizeY++;
        totalLattices = totalViewXmm;//平均总格子数,1格子=1mm,所以控件的毫米宽度=格子数
        xS = mViewWidth / totalLattices;
    }
}

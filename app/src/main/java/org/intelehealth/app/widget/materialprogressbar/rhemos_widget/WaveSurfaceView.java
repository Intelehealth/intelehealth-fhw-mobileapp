package org.intelehealth.app.widget.materialprogressbar.rhemos_widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.intelehealth.app.widget.materialprogressbar.rhemos_widget.wave.DrawWave;

/**
 * Created by Prajwal Waingankar
 * on February 2023.
 * Github: prajwalmw
 */
public class WaveSurfaceView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    private boolean isLoop;
    private Thread mDrawWaveThread;
    private final SurfaceHolder mSurfaceHolder;
    protected DrawWave<?> mDrawWave;
    private boolean isPause;

    public WaveSurfaceView(Context context) {
        super(context);
        mSurfaceHolder = getHolder();
    }

    public WaveSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSurfaceHolder = getHolder();
    }

    public WaveSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mSurfaceHolder = getHolder();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isLoop = true;
        //创建并启动一个画图线程。
        mDrawWaveThread = new Thread(this);
        mDrawWaveThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mDrawWave.initWave(getWidth(), getHeight());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //销毁画图线程。
        isLoop = false;
        mDrawWaveThread = null;
    }

    @Override
    public void run() {
        while (isLoop) {
            synchronized (mSurfaceHolder) {
                if (isPause) {
                    try {
                        mSurfaceHolder.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
                try {
                    Canvas canvas = mSurfaceHolder.lockCanvas();
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawWave.drawWave(canvas);
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                } catch (Exception ignored) {
                    isLoop = false;
                }
            }
        }
    }

    /**
     * 恢复SurfaceView的图形刷新循环
     * 当处于暂停时有效。
     */
    public void reply() {
        synchronized (mSurfaceHolder) {
            if (!isPause) return;
            try {
                isPause = false;
                mSurfaceHolder.notify();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 暂停SurfaceView的图形刷新循环
     */
    public void pause() {
        isPause = true;
    }

    /**
     * 在控件初始化时，将一个DrawWave对象放进来
     */
    public <T> void setDrawWave(DrawWave<T> drawWave) {
        this.mDrawWave = drawWave;
        setZOrderOnTop(true);
        setFocusable(true);
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        mSurfaceHolder.addCallback(this);
    }
}

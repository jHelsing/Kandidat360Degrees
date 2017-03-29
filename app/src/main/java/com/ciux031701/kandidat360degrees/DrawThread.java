package com.ciux031701.kandidat360degrees;

import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.SurfaceHolder;

/**
 * Created by John on 2017-03-29.
 */

public class DrawThread extends Thread {


    private SurfaceHolder holder;
    DrawDotSurfaceView surfaceView;
    private boolean run = false;
    public DrawThread(SurfaceHolder holder, DrawDotSurfaceView surfaceView) {
        this.holder = holder;
        this.surfaceView = surfaceView;
        run = false;
    }

    public void setRunning(boolean run) {
        this.run = run;
        }

    @Override
    public void run() {
        Canvas canvas = null;
        while (run) {

                try {
                    canvas = holder.lockCanvas(null);
                    synchronized (holder) {
                        surfaceView.onDraw(canvas);
                        //surfaceView.update();
                    }
                } finally {
                    if (canvas != null) {
                        holder.unlockCanvasAndPost(canvas);
                    }
                }
        }
    }


}


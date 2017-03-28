package com.ciux031701.kandidat360degrees;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.w3c.dom.Attr;

/**
 * Created by Anna on 2017-03-28.
 */
public class DrawDotSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private DrawThread drawThread;
    private Paint paint = new Paint();
    private Point center;
    private ShapeDrawable filledCircle;
    private int targetDegree;
    private int currentDegree;
    private int radius = 40;
    private Canvas canvas;

    public DrawDotSurfaceView(Context context) {
        super(context);
        this.center = new Point(0,0);
        initialize();
    }

    public DrawDotSurfaceView(Context context, AttributeSet attrs){
        super(context,attrs);
        this.center = new Point(0,0);
        initialize();
    }

    public DrawDotSurfaceView(Context context, AttributeSet attrs, int defStyle){
        super(context,attrs,defStyle);
        this.center = new Point(0,0);
        initialize();
    }

    private void initialize() {
        getHolder().addCallback(this);
        paint.setColor(Color.GREEN);
        setFocusable(true);
        filledCircle = new ShapeDrawable(new OvalShape());
        filledCircle.getPaint().setColor(0xff74AC23); //default is black
        filledCircle.setBounds(center.x - radius, center.y - radius, center.x + radius, center.y + radius); //needed, the shape is not drawn
    }

    public void startThread() {
        drawThread = new DrawThread(getHolder(),this);
        drawThread.setRunning(true);
        drawThread.start();
    }

    public void stopThread() {
        drawThread.setRunning(false);
        drawThread.stop();
    }

    @Override
    public void onDraw(Canvas canvas) {
        //Clear the canvas:
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        //Draw a filled circle in the center of the display:
        int deltaDegree = currentDegree-targetDegree; //positive value - right of targetDegree
        Rect rectangle = new Rect();
        Display display = ((Activity)getContext()).getWindowManager().getDefaultDisplay();
        display.getRectSize(rectangle);
        int width = rectangle.width(); //pixlar tror vi

        int degreeToPixels = width * 2/3 * 20 / 360;

        filledCircle.setBounds(center.x-width-degreeToPixels*deltaDegree,center.y-width,center.x+width-degreeToPixels*deltaDegree,center.y+width);
        filledCircle.draw(canvas);
    }

    public void setTargetDegree(int targetDegree) {
        this.targetDegree = targetDegree;
    }

    public void setCurrentDegree(int currentDegree) {
        this.currentDegree = currentDegree;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    class DrawThread extends Thread {
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
            //Canvas canvas = null;
            while (run) {
                try {
                    //canvas = holder.lockCanvas(null);
                    synchronized (holder) {
                        surfaceView.onDraw(canvas);
                        //surfaceView.update();
                    }
                } finally {
                    if (canvas != null) {
                        //holder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawThread = new DrawThread(getHolder(), this);
        drawThread.setRunning(true);
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}

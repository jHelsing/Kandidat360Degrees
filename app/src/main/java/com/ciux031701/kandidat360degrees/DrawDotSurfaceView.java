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
import android.view.View;

import org.w3c.dom.Attr;

/**
 * Created by Anna on 2017-03-28.
 */
public class DrawDotSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private DrawThread drawThread;
    private Paint paint = new Paint();
    private Point center;
    private ShapeDrawable filledCircle;
    private ShapeDrawable aimCircle;
    private int targetDegree;
    private Integer currentDegree;
    private int radius = 40;
    private int unfilledRadius =0;
    private int width;
    private float degToPixFactor;
    private boolean targetAcquired = false;

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
        setFocusable(false);
        filledCircle = new ShapeDrawable(new OvalShape());
        filledCircle.getPaint().setColor(0xff74AC23); //default is black
        filledCircle.setBounds(center.x - radius, center.y - radius, center.x + radius, center.y + radius); //needed, the shape is not drawn

        aimCircle = new ShapeDrawable(new OvalShape());
        aimCircle.getPaint().setStyle(Paint.Style.STROKE);
        aimCircle.getPaint().setStrokeWidth(5);
        unfilledRadius = radius + 15;
        aimCircle.setBounds(center.x-unfilledRadius,center.y-unfilledRadius,center.x+unfilledRadius,center.y+unfilledRadius);
    }


    public void startThread() {
        drawThread.setRunning(true);
        drawThread.start();
    }

    public void stopThread() {
        drawThread.setRunning(false);
    }

//    public boolean acquireTarget(){
//        if (currentDegree != null && !targetAcquired){
//            targetDegree = currentDegree;
//            targetAcquired = true;
//            return true;
//        }else{
//            targetAcquired = false;
//            return false;
//        }
//    }

    @Override
    public void draw(Canvas canvas) {
        //Clear the canvas:
        if(canvas == null) return;
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        //Draw a filled circle in the center of the display:
        //currentDegree += 1;

        aimCircle.draw(canvas);
        if(targetAcquired) {
            int deltaDegree = currentDegree - targetDegree; //positive value - right of targetDegree
            filledCircle.setBounds(getNewBounds(deltaDegree));
            filledCircle.draw(canvas);
        }
    }
    private Rect getNewBounds(int dDeg){
        int dPixel = Math.round(dDeg*degToPixFactor);
        return new Rect(center.x-dPixel-radius,center.y-radius,center.x+radius-dPixel,center.y+radius);
    }

    public void setTargetDegree(int targetDegree) {
        this.targetDegree = targetDegree;
    }

    public void setCurrentDegree(int currentDegree) {
        this.currentDegree = currentDegree;
    }

    public void setTargetAcquired(boolean targetAcquired) {
        this.targetAcquired = targetAcquired;
    }

    public void setCenter(Point center) {
        this.center = center;
        aimCircle.setBounds(center.x-unfilledRadius,center.y-unfilledRadius,center.x+unfilledRadius,center.y+unfilledRadius);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Rect rectangle = new Rect();
        Display display = ((Activity)getContext()).getWindowManager().getDefaultDisplay();
        display.getRectSize(rectangle);
        width = rectangle.width(); //pixlar tror vi
        degToPixFactor = (width*40)/(360*3);

        drawThread = new DrawThread(getHolder(),this);
        startThread();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopThread();
        drawThread = null;
    }
}

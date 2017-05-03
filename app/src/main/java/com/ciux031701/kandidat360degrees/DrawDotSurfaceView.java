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
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Anna on 2017-03-28.
 */
public class DrawDotSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private DrawThread drawThread;
    private Paint paint = new Paint();
    private Point center;
    private ShapeDrawable aimCircle;
    private float targetDegree;
    private float currentDegree;
    private float currentVerticalDegree;
    private int radius = 40;
    private int unfilledRadius =0;
    private float degToPixFactor;
    private boolean targetAcquired = false;
    private boolean isStillShowingGreen;
    private float verticalOffset;
    private float horizontalOffset;

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
        paint.setColor(Color.RED);
        isStillShowingGreen=false;
        setFocusable(false);

        aimCircle = new ShapeDrawable(new OvalShape());

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

    public boolean isStillShowingGreen(){
        return isStillShowingGreen;
    }



    public void setCircleColor(final int color){
        if(color==Color.GREEN){
            isStillShowingGreen=true;
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    paint.setColor(Color.RED);
                    isStillShowingGreen=false;
                }
            }, 1000);
        }
        paint.setColor(color);
    }

    @Override
    public void draw(Canvas canvas) {
        //Clear the canvas:
        if(canvas == null) return;
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        //Draw a filled circle in the center of the display:
        //currentDegree += 1;

        aimCircle.draw(canvas);
        if(targetAcquired) {
            horizontalOffset = getHorizontalOffset(currentDegree); // currentdegree-targetdegree
            verticalOffset = getVerticalOffset(currentVerticalDegree);

            //Checks if the dot should snap to the center
            if(horizontalOffset >= -2 && horizontalOffset <= 2 && verticalOffset >= -2 && verticalOffset <= 2 || isStillShowingGreen){
                horizontalOffset=0;
                verticalOffset=0;
            }
            float dPixelVertical = Math.round(verticalOffset*degToPixFactor);
            float dPixel = Math.round(horizontalOffset*degToPixFactor);
            System.out.println("horizontal offset: " + horizontalOffset + ", current: " + currentDegree + ", target: " + targetDegree);
            canvas.drawCircle(center.x-dPixel,center.y-dPixelVertical,radius,paint);
        }
    }

    public float getHorizontalOffset(float degree){
        float returnDegree;
        if(degree>180){
            if(targetDegree>=180){
                returnDegree = (targetDegree-degree)*-1;
            }else {
                returnDegree = (360 - (degree - targetDegree)) * -1;
            }
        }else{
            returnDegree = degree-targetDegree;
        }
        return returnDegree;
    }

    public float getVerticalOffset(float degree){
        float returnDegree;
        if(degree>180){
            //*-1 is how we define whats is up and down
            returnDegree = (360-degree)*-1;
        }else{
            returnDegree = degree;
        }
        return returnDegree;
    }

    public void setTargetDegree(float targetDegree) {
        this.targetDegree = targetDegree;
    }

    public void setCurrentDegree(float currentDegree) {
        this.currentDegree = currentDegree;
    }


    public void setTargetAcquired(boolean targetAcquired) {
        if(targetAcquired) {
            this.targetAcquired = targetAcquired;
            aimCircle.getPaint().setStyle(Paint.Style.STROKE);
            aimCircle.getPaint().setStrokeWidth(5);
            unfilledRadius = radius + 20;
            aimCircle.setBounds(center.x - unfilledRadius, center.y - unfilledRadius, center.x + unfilledRadius, center.y + unfilledRadius);
        }
    }

    public void setCenter(Point center) {
        this.center = center;
        aimCircle.setBounds(center.x-unfilledRadius,center.y-unfilledRadius,center.x+unfilledRadius,center.y+unfilledRadius);

    }

    public float getTargetDegree(){
        return this.targetDegree;
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        int width;
        Rect rectangle = new Rect();
        Display display = ((Activity)getContext()).getWindowManager().getDefaultDisplay();
        display.getRectSize(rectangle);
        width = 3*rectangle.width()/5; //60% of actual width
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

    public void setCurrentVerticalDegree(float currentVerticalDegree) {
        this.currentVerticalDegree = currentVerticalDegree;
    }
}

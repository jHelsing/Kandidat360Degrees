package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ciux031701.kandidat360degrees.communication.FTPInfo;
import com.ciux031701.kandidat360degrees.communication.JReqIsLiked;
import com.ciux031701.kandidat360degrees.communication.JReqLikeImage;
import com.ciux031701.kandidat360degrees.communication.JReqUnLikeImage;
import com.ciux031701.kandidat360degrees.communication.JRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static android.content.ContentValues.TAG;


/**
 * Created by boking on 2017-03-14.
 */

public class ImageViewFragment extends Fragment implements SurfaceHolder.Callback,SensorEventListener {

    private ImageButton closeButton;
    private ImageButton arrowLeftButton;
    private ImageButton arrowRightButton;
    private ImageView doneButton;
    private DrawerLayout mDrawerLayout;
    private ImageButton downloadButton;
    private ProgressBar downloadProgressBar;
    private ImageButton sensorModeButton;

    private String origin;
    private String imageid;
    private Drawable image;
    private Bitmap panoramaImage;
    private boolean liked;

    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
    Display display;
    Point size;
    private float top;
    private float left;
    Point touchPoint = new Point();
    private float lastDiff;
    private boolean isTouchingScreen;
    private Bitmap scaledBitmap;
    private boolean imageLargerThanScreen;
    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor;
    float cX, cY;
    float diff, zoomValue, modLeft;
    private boolean isSensorModeActive;
    float currentDegrees;
    float lastDegree;
    private float[] mRotationMatrix;
    private float startGyroDegree;
    private float orientation[];
    private boolean isVertical;
    private boolean isFirstSensorChanged;
    private float normalModeOffset;
    private float lastSensorLeftValue;
    private float lastViewingAngle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_imageview, container, false);
        origin = getArguments().getString("origin");
        imageid = getArguments().getString("imageid");
        lastDiff =0;
        isTouchingScreen = false;
        display = getActivity().getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        isSensorModeActive = false;
        orientation = new float[3];
        mRotationMatrix = new float[9];
        isVertical=false;
        isFirstSensorChanged=true;
        normalModeOffset =0;
        lastSensorLeftValue =0;
        lastViewingAngle = 0;

        SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        Sensor rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(this, rotationVector,SensorManager.SENSOR_DELAY_UI);

        scaleGestureDetector = new ScaleGestureDetector(getActivity(), new ScaleListener());
        scaleFactor = 1.0f;

        surfaceView = (SurfaceView)root.findViewById(R.id.imageViewSurface);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        sensorModeButton = (ImageButton) root.findViewById(R.id.sensorModeButton);
        sensorModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSensorModeActive){
                    sensorModeButton.setImageResource(R.drawable.sensormodeicon);
                    //lastDiff=0;
                    isFirstSensorChanged=true;
                    isSensorModeActive=false;
                    //lastSensorLeftValue =left;
                    //left=lastSensorLeftValue;
                    tryDrawing(surfaceHolder);
                    //lastDiff=-currentDegrees*?
                }else{//switching to sensor mode
                    sensorModeButton.setImageResource(R.drawable.normalmode);
                    //startGyroDegree=left*360/panoramaImage.getWidth();

                    if(left>0){
                        lastViewingAngle=360-((left/panoramaImage.getWidth())*360);
                    }else{
                        System.out.println("settings swag");
                        lastViewingAngle=((left/panoramaImage.getWidth())*360)*-1;
                    }

                    System.out.println("setting lastViewingAngle to: " + lastViewingAngle + ", panoramawidth: " + panoramaImage.getWidth() + ", left: " + left);
                    isSensorModeActive=true;
                }

            }
        });
        downloadButton = (ImageButton) root.findViewById(R.id.downloadButton);


        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                scaleGestureDetector.onTouchEvent(event);

                if(!isSensorModeActive) {
                    if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                        isTouchingScreen = true;
                        touchPoint.x = (int) event.getX();
                        touchPoint.y = (int) event.getY();
                    } else if (event.getAction() == android.view.MotionEvent.ACTION_MOVE) {
                        if (!imageLargerThanScreen) {
                            diff = (touchPoint.x - event.getX()) / 15;
                            left = ((lastDiff + diff) * -1) % panoramaImage.getWidth();
                            if (lastDiff > zoomValue) {
                                lastDiff = zoomValue;
                            } else if (lastDiff < -zoomValue) {
                                lastDiff = -zoomValue;
                            } else {
                                lastDiff = (lastDiff + diff) % panoramaImage.getWidth();
                                System.out.println("new lastdiff: " + lastDiff);
                            }
                            if (left < -zoomValue) {
                                left = zoomValue * -1;
                            } else if (left > zoomValue) {
                                left = zoomValue;
                            } else if (scaleFactor < 1f && left > 0) {
                                System.out.println("left=0;");
                                left = 0;
                            }
                            System.out.println("lastdiff. left: " + left);
                        } else {
                            if (scaleFactor > 1.2) {
                                diff = (touchPoint.x - event.getX()) / 11;
                            } else {
                                diff = (touchPoint.x - event.getX()) / 9;
                            }
                            lastDiff = (lastDiff + diff) % panoramaImage.getWidth();
                            System.out.println("left gained from drag: " + (((lastDiff + diff) * -1) % panoramaImage.getWidth()) + ", left gained from sensor: " + (panoramaImage.getWidth()-((360-fromDegreeToProgress(currentDegrees))*panoramaImage.getWidth()/360))*-1);
                            left = (((lastDiff + diff) * -1) % panoramaImage.getWidth() + (panoramaImage.getWidth()-((360-fromDegreeToProgress(currentDegrees))*panoramaImage.getWidth()/360)))%panoramaImage.getWidth();
                            System.out.println("setting lastdiff: " + lastDiff + ", diff: " + diff);
                            lastDiff = (lastDiff + diff) % panoramaImage.getWidth();
                            System.out.println("new lastdiff: " + lastDiff);
                            System.out.println("left: " +left);
                        }
                        Canvas canvas = surfaceHolder.lockCanvas();
                        drawMyStuff(canvas);
                        surfaceHolder.unlockCanvasAndPost(canvas);

                    } else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                        isTouchingScreen = false;
                    }
                }
                return true;
            }
        });



        if(origin.equals("profile") || origin.equals("explore")) {
            File file = new File(getActivity().getFilesDir() + FTPInfo.PANORAMA_LOCAL_LOCATION + imageid + FTPInfo.FILETYPE);
            image = Drawable.createFromPath(file.getPath());
            panoramaImage =  ((BitmapDrawable)image).getBitmap();
            downloadButton.setVisibility(View.GONE);
            final TextView usernameView = (TextView) root.findViewById(R.id.imageViewUsernameTextView);
            final TextView favView = (TextView) root.findViewById(R.id.imageviewFavouriteTextView);
            favView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d("Panorama Viewer", "Noticed onTouch");
                    final int DRAWABLE_RIGHT = 2;
                    if(event.getAction() == MotionEvent.ACTION_UP) {
                            /**
                             * Makes the logged in user to like or unlike a specific image. It returns true if the image
                             * managed to be liked or unliked.
                             */
                            Log.d("Panorama Viewer", "Found correct event");

                            if(!liked){
                                JReqLikeImage likeImageReq = new JReqLikeImage(imageid);
                                likeImageReq.setJResultListener(new JRequest.JResultListener() {
                                    @Override
                                    public void onHasResult(JSONObject result) {
                                        boolean error;
                                        try {
                                            error = result.getBoolean("error");
                                        } catch (JSONException e) {
                                            error = true;
                                        }
                                        if(!error){
                                            Drawable fav = (Drawable) getResources().getDrawable(R.drawable.ic_favorite);
                                            favView.setCompoundDrawablesWithIntrinsicBounds(null, null, fav, null);
                                            liked = true;
                                            String count = favView.getText().toString();
                                            favView.setText((Integer.parseInt(count)+1) + "");
                                        } else
                                            Toast.makeText(getActivity(), "Something went wrong with the server, try again later.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                likeImageReq.sendRequest();
                            } else {
                                JReqUnLikeImage unLikeImageReq = new JReqUnLikeImage(imageid);
                                unLikeImageReq.setJResultListener(new JRequest.JResultListener() {
                                    @Override
                                    public void onHasResult(JSONObject result) {
                                        boolean error;
                                        try {
                                            error = result.getBoolean("error");
                                        } catch (JSONException e) {
                                            error = true;
                                        }
                                        if(!error){
                                            Drawable fav = (Drawable) getResources().getDrawable(R.drawable.ic_favorite_no_clicked_white);
                                            favView.setCompoundDrawablesWithIntrinsicBounds(null, null, fav, null);
                                            liked = false;
                                            String count = favView.getText().toString();
                                            favView.setText((Integer.parseInt(count)-1) + "");
                                        } else
                                            Toast.makeText(getActivity(), "Something went wrong with the server, try again later.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                unLikeImageReq.sendRequest();
                            }
                            return true;

                    }
                    return true;
                }
            });
            Typeface tf = Typeface.defaultFromStyle(Typeface.BOLD_ITALIC);
            usernameView.setText(getArguments().getString("username"));
            usernameView.setTypeface(tf);
            usernameView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) getActivity()).showProfile(usernameView.getText().toString());
                }
            });

            favView.setText(getArguments().getString("likes"));
            JReqIsLiked request = new JReqIsLiked(imageid);
            request.setJResultListener(new JRequest.JResultListener() {
                @Override
                public void onHasResult(JSONObject result) {
                    boolean error;
                    String message = null;
                    try {
                        error = result.getBoolean("error");
                    } catch(JSONException je){
                        error = true;
                        Log.d("View Panorama", "Error in parsing result or result from server. Result is: " + result.toString());
                    /*
                    4 timmar
                     */
                    }

                    if(!error) {
                        try {
                            JSONArray arr = result.getJSONArray("isliked");
                            Log.d("View Panorama", "arr: " + arr);
                            if(arr.length() == 0) {
                                // Image not liked
                                liked = false;
                            } else {
                                liked = true;
                                favView.setCompoundDrawables(null, null, getResources().getDrawable(R.drawable.ic_favorite), null);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("View Panorama", "Failed parsing array");
                        }
                    }

                }
            });
            request.sendRequest();
        } else {
            View v = root.findViewById(R.id.imageviewInformationLayout);
            v.setVisibility(View.GONE);
        }

        doneButton = (ImageButton)root.findViewById(R.id.sendToShareButton);

        if(origin.equals("camera")||origin.equals("upload")){
            panoramaImage = getArguments().getParcelable("image");
            if (panoramaImage == null){
                Log.i(TAG,"Panorama image is null");
            }
            doneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    Bundle args = new Bundle();
                    args.putParcelable("picture", panoramaImage);

                    Fragment fragment = new ShareFragment();
                    FragmentManager fragmentManager = getFragmentManager();
                    fragment.setArguments(args);
                    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, "SHARE_FRAGMENT").commit();

                }
            });
        }else{//Only show the picture
            doneButton.setVisibility(View.GONE);
        }
        mDrawerLayout = (DrawerLayout)getActivity().findViewById(R.id.drawer_layout);

        closeButton = (ImageButton)root.findViewById(R.id.viewingCloseButton);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.popBackStack();
                }
            }
        });

        arrowLeftButton = (ImageButton)root.findViewById(R.id.imageviewArrowLeft);
        arrowRightButton = (ImageButton)root.findViewById(R.id.imageviewArrowRight);

        arrowLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                left+=150;
                lastDiff = (lastDiff-150)% panoramaImage.getWidth();
                tryDrawing(surfaceHolder);
            }
        });
        arrowRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                left-=150;
                lastDiff = (lastDiff+150)% panoramaImage.getWidth();
                tryDrawing(surfaceHolder);
            }
        });

        downloadProgressBar = (ProgressBar) root.findViewById(R.id.downloadProgressBar);
        downloadProgressBar.setVisibility(View.GONE);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadButton.setVisibility(View.GONE);
                downloadProgressBar.setVisibility(View.VISIBLE);

                //start a new thread to process job
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity mainActivity = (MainActivity) getActivity();
                        mainActivity.downloadPanoramaLocal(panoramaImage);
                        //Done like this since no other thread
                        //can modify the view other than the main
                        handler.sendEmptyMessage(0);
                    }
                }).start();
            }
        });

        if(panoramaImage.getWidth()<=size.x){
            imageLargerThanScreen=false;
            arrowLeftButton.setVisibility(View.GONE);
            arrowRightButton.setVisibility(View.GONE);
        }else{
            imageLargerThanScreen=true;
            sensorModeButton.setVisibility(View.VISIBLE);
        }

        return root;
    }

    private void tryDrawing(SurfaceHolder holder) {
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            Log.e(TAG, "Cannot draw onto the canvas as it's null");
        } else {
            drawMyStuff(canvas);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawMyStuff(final Canvas canvas) {
        modLeft = left % panoramaImage.getWidth();

        cX = canvas.getWidth()/2.0f; //Width/2 gives the horizontal centre
        cY = canvas.getHeight()/2.0f; //Height/2 gives the vertical centre

        canvas.save();
        canvas.scale(scaleFactor, scaleFactor,cX,cY);
        //clear canvas
        canvas.drawColor(Color.BLACK);

        if(!imageLargerThanScreen){
            canvas.drawBitmap(panoramaImage,  modLeft, (canvas.getHeight()-panoramaImage.getHeight())/2, null);
        }else {

            //Bitmap scaled = Bitmap.createScaledBitmap(panoramaImage, surfaceView.getWidth(), surfaceView.getHeight(), true);
            canvas.drawBitmap(panoramaImage, modLeft, (canvas.getHeight()-panoramaImage.getHeight())/2, null);
            canvas.drawBitmap(panoramaImage, (modLeft + panoramaImage.getWidth()), (canvas.getHeight()-panoramaImage.getHeight())/2, null);
            canvas.drawBitmap(panoramaImage, (modLeft - panoramaImage.getWidth()), (canvas.getHeight()-panoramaImage.getHeight())/2, null);
            if(scaleFactor<0.6f) {
                canvas.drawBitmap(panoramaImage, (modLeft - panoramaImage.getWidth() * 2), (canvas.getHeight() - panoramaImage.getHeight()) / 2, null);
            }
        }
        canvas.restore();
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            downloadButton.setVisibility(View.VISIBLE);
            downloadProgressBar.setVisibility(View.GONE);
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        left = 0;
        top = 0;
        surfaceView.setWillNotDraw(false);
        tryDrawing(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        tryDrawing(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, magnetometer);
        sensorManager.unregisterListener(this, rotationVector);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (isSensorModeActive) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                // Convert the rotation-vector to a 4x4 matrix.
                SensorManager.getRotationMatrixFromVector(mRotationMatrix, sensorEvent.values);
                SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, mRotationMatrix);
                SensorManager.getOrientation(mRotationMatrix, orientation);

                // Optionally convert the result from radians to degrees
                orientation[0] = (float) Math.toDegrees(orientation[0]);
                orientation[1] = (float) Math.toDegrees(orientation[1]);
                orientation[2] = (float) Math.toDegrees(orientation[2]);

                if (orientation[1] > -12 && orientation[1] < 12) {
                    if (!isVertical) {
                        isVertical = true;
                    }
                } else {
                    if (isVertical) {
                        isVertical = false;
                    }
                }
                System.out.println("orientation[0]: " + orientation[0]);
                currentDegrees = fromOrientationToDegrees(orientation[0]);
                if (isFirstSensorChanged) {
                    isFirstSensorChanged = false;
                    startGyroDegree = currentDegrees;
                }

                /*if(isFirstSensorChanged){
                    startGyroDegree=currentDegrees;
                    isFirstSensorChanged=false;
                }*/
                left = (((360-lastViewingAngle)*panoramaImage.getWidth()/360)+(panoramaImage.getWidth()-((360-fromDegreeToProgress(currentDegrees))*panoramaImage.getWidth()/360))*-1)%panoramaImage.getWidth();
                //left =(360-fromDegreeToProgress(currentDegrees)*panoramaImage.getWidth())/360;
                System.out.println("left gained from sensor: " + (panoramaImage.getWidth()-((360-fromDegreeToProgress(currentDegrees))*panoramaImage.getWidth()/360))*-1);
                System.out.println("startdegree: " + startGyroDegree + ", currentdegree: " + currentDegrees + ", progress: " + fromDegreeToProgress(currentDegrees)+ ", left: " + left);
            }
            tryDrawing(surfaceHolder);
        }
    }

    public float fromOrientationToDegrees(double orientation){
        if(orientation<0){
            return (float)(360-Math.abs(orientation));
        }else
            return (float)orientation;
    }

    public float fromDegreeToProgress(float degree) {
        float progress =0;
        if (degree >= startGyroDegree) {
            progress = degree - startGyroDegree;
        } else {
            progress = -(startGyroDegree - degree);
        }
        return progress;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class ScaleListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 2.0f));
            zoomValue = panoramaImage.getWidth()/scaleFactor*0.5f;
            surfaceView.invalidate();
            tryDrawing(surfaceHolder);
            return true;
        }
    }
}

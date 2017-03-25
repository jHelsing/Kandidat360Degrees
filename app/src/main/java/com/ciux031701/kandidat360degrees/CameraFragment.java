package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.graphics.drawable.shapes.OvalShape;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by boking on 2017-02-17.
 */

public class CameraFragment extends Fragment implements SensorEventListener {

    private TextView holdVerticallyText;
    private ImageButton backButton;
    private ImageView holdVerticallyImage;
    private ImageButton captureButton;
    private ImageView previewImage;

    private SurfaceView mSurfaceView, mSurfaceViewDraw;
    private Camera mCam;

    //Sensor stuff
    private Sensor accelerometer;
    private Sensor magnetometer;
    private SensorManager sensorManager;
    float[] mGravity;
    float[] mGeomagnetic;
    double currentDegrees;
    double lastDegree;

    private boolean isVertical;
    private boolean captureInProgress;

    private boolean safeToTakePicture = true; //is it safe to capture a picture?

    private DrawerLayout mDrawerLayout;
    private ArrayList<Mat> listOfTakenImages;

    private Bundle args;
    private ProgressBar angleProgressBar;
    private double startGyroDegree;
    private double highestGyroDegree;
    private int lastProgressAngle;
    private boolean isHalfWay; //temporary until we get actual picture steps going
    private float orientation[];
    float rField[], iField[];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_camera, container, false);

        mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);

        isVertical = false;
        captureInProgress = false;
        highestGyroDegree = 0;
        isHalfWay = false;
        lastProgressAngle = 0;
        lastDegree = 0;
        //For the sensors:
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);

        //GUI: buttons & views
        angleProgressBar = (ProgressBar) root.findViewById(R.id.angleProgressBar);
        angleProgressBar.setVisibility(View.GONE);
        //previewImage = (ImageView)root.findViewById(R.id.previewImage);
        captureButton = (ImageButton) root.findViewById(R.id.sendToShareButton);
        holdVerticallyText = (TextView) root.findViewById(R.id.holdVerticallyText);
        holdVerticallyImage = (ImageView) root.findViewById(R.id.holdVerticallyImage);
        backButton = (ImageButton) root.findViewById(R.id.backButton);
        backButton.setBackgroundResource(R.drawable.temp_return);
        captureButton.setVisibility(View.GONE);
        //previewImage.setVisibility(View.GONE);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                Fragment fragment = new ExploreFragment();
                FragmentManager fragmentManager = getActivity().getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                //fragmentTransaction.addToBackStack(null);

            }
        });

        //If not taking pictures or in finalization -- take a picture.
        //If in finalization - switch to upload-fragment.
        //If taken a picture - go to finalization (show the panorama)
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!captureInProgress) {
                    //Take a picture
                    captureInProgress = true;
                    //angleProgressBar.setVisibility(View.VISIBLE);
                    startGyroDegree = lastDegree;
                    backButton.setVisibility(View.GONE);
                    if (mCam != null && safeToTakePicture) {
                        //set the flag to false so we don't take two pictures at the same time
                        safeToTakePicture = false;
                        mCam.takePicture(null, null, jpegCallback);
                    }
                } else {
                    args = new Bundle();
                    args.putString("origin", "camera");
                    ImageViewFragment fragment = new ImageViewFragment();
                    fragment.setArguments(args);
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("camera").commit();
                }

            }
        });

        //Views to show the camera and the most recently taken picture in:
        mSurfaceView = (SurfaceView) root.findViewById(R.id.surfaceView);
        mSurfaceView.getHolder().addCallback(mSurfaceCallback);
        mSurfaceView.setVisibility(View.GONE);

        mSurfaceViewDraw = (SurfaceView) root.findViewById(R.id.surfaceViewDraw);
        mSurfaceViewDraw.setZOrderOnTop(true);
        mSurfaceViewDraw.getHolder().addCallback(mSurfaceCallbackDraw);
        mSurfaceViewDraw.getHolder().setFormat(PixelFormat.TRANSPARENT);
        mSurfaceViewDraw.setVisibility(View.GONE);

        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    //To save pictures & show the last taken picture:
    private final Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //byte[] --> bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            //Rotate the picture to fit portrait mode
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

            //Convert the image to Mat, to be able to use openCV
            Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(), 16); //type of Mat needs to 16, CV_8UC3, to be able to use matToBitmap(..) later
            Utils.bitmapToMat(bitmap, mat);
            listOfTakenImages.add(mat);

//            Canvas canvas = null;
//            try {
//                canvas = mSurfaceViewOnTop.getHolder().lockCanvas(null);
//                synchronized (mSurfaceViewOnTop.getHolder()){
//                    //Clear canvas
//                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//                    //Scale the image to fit the surfaceview
//                    float scale = 1.0f * mSurfaceView.getHeight()/bitmap.getHeight();
//                    Bitmap scaleImage = Bitmap.createScaledBitmap(bitmap,(int)(scale*bitmap.getWidth()),mSurfaceView.getHeight(),false);
//                    Paint paint = new Paint();
//                    //Set the opacity of the image
//                    paint.setAlpha(200);
//                    //Draw 1/3 of the image:
//                    canvas.drawBitmap(scaleImage,-scaleImage.getWidth()*2/3,0,paint);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                if (canvas != null){
//                    mSurfaceViewOnTop.getHolder().unlockCanvasAndPost(canvas);
//                }
//            }
            //Start preview of the camera & set safe to take pictures to true
            mCam.startPreview();
            safeToTakePicture = true;
        }

    };

    //For the surfaceview to draw on:
    //How to do this: https://developer.android.com/guide/topics/graphics/2d-graphics.html
    private SurfaceHolder.Callback mSurfaceCallbackDraw = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Canvas canvas = null;
            try {
                canvas = mSurfaceViewDraw.getHolder().lockCanvas(null);
                synchronized (mSurfaceViewDraw.getHolder()) {
                    //Find center of screen to put the circle there:
                    Display mDisp = getActivity().getWindowManager().getDefaultDisplay();
                    int centerX = mDisp.getWidth()/2;
                    int centerY = mDisp.getHeight()/2;

                    //Draw a filled circle in the center of the display:
                    ShapeDrawable filledCircle = new ShapeDrawable(new OvalShape());
                    filledCircle.getPaint().setColor(0xff74AC23); //default is black
                    int filledRadius = 40;
                    filledCircle.setBounds(centerX-filledRadius,centerY-filledRadius,centerX+filledRadius,centerY+filledRadius); //needed, the shape is not drawn otherwise
                    filledCircle.draw(canvas);

                    //Draw an unfilled circle in the center of the display:
                    ShapeDrawable unfilledCircle = new ShapeDrawable(new OvalShape());
                    unfilledCircle.getPaint().setStyle(Paint.Style.STROKE);
                    unfilledCircle.getPaint().setStrokeWidth(5);
                    int unfilledRadius = filledRadius + 15;
                    unfilledCircle.setBounds(centerX-unfilledRadius,centerY-unfilledRadius,centerX+unfilledRadius,centerY+unfilledRadius);
                    unfilledCircle.draw(canvas);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    mSurfaceViewDraw.getHolder().unlockCanvasAndPost(canvas);
                }
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    };

    //For the surfaceview showing the camera:
    private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                //Tell the camera to display the frame on this surfaceview:
                mCam.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            //Get the default parameters for camera
            Camera.Parameters myParameters = mCam.getParameters();
            //Select the best preview size
            Camera.Size myBestSize = getBestPreviewSize(myParameters);
            if (myBestSize != null) {
                //Set the preview size
                myParameters.setPreviewSize(myBestSize.width, myBestSize.height);
                //Set the parameters to the camera
                mCam.setParameters(myParameters);
                //Rotate the display frame 90 degree to view in portrait mode
                mCam.setDisplayOrientation(90);
                //Start the preview
                mCam.startPreview();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if (mCam != null) {
            mCam.stopPreview();
            mCam.release();
            mCam = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mCam = Camera.open(0); // 0 = back camera
    }


    //Sensors:
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = sensorEvent.values;
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = sensorEvent.values;
        if (mGravity != null && mGeomagnetic != null) {
            rField = new float[9];
            iField = new float[9];
            boolean success = SensorManager.getRotationMatrix(rField, iField, mGravity, mGeomagnetic);
            if (success) {
                orientation = new float[3];
                SensorManager.getOrientation(rField, orientation);
                currentDegrees = fromSensorToDegrees(orientation[0]);
                if (currentDegrees > lastDegree) {
                    currentDegrees++;
                } else {
                    currentDegrees--;
                }
                lastDegree = currentDegrees;
                if (captureInProgress) {
                    int newProgressAngle = (int) fromDegreeToProgress(lastDegree);
                    System.out.println(newProgressAngle);
                    if (newProgressAngle < 200 && newProgressAngle > 160 && isHalfWay != true) {
                        System.out.println("halfway=true");
                        isHalfWay = true;
                    }

                    //To prevent weird jumps
                    if (Math.abs(lastProgressAngle - newProgressAngle) < 50 && Math.abs(newProgressAngle - lastProgressAngle) < 50) {
                        lastProgressAngle = newProgressAngle;
                        //Compare with 180 abd ifHalfWay so it doesn't register when we go from 1,0,360,359,...
                        if (newProgressAngle < 180 && isHalfWay == false && highestGyroDegree < newProgressAngle) {
                            highestGyroDegree = newProgressAngle;
                            angleProgressBar.setProgress((int) highestGyroDegree);
                        } else if (newProgressAngle >= 180 && isHalfWay == true && highestGyroDegree < newProgressAngle) {
                            highestGyroDegree = newProgressAngle;
                            angleProgressBar.setProgress((int) highestGyroDegree);
                        }
                        if (angleProgressBar.getProgress() > 357) {
                            captureButton.performClick();
                        }
                    }
                }

                if (orientation[1] < 1.75 && orientation[1] > 1.25 || orientation[1] < -1.25 && orientation[1] > -1.75) {
                    if (!isVertical) {
                        isVertical = true;
                        if (!captureInProgress) {
                            holdVerticallyImage.setVisibility(View.GONE);
                            holdVerticallyText.setVisibility(View.GONE);
                            captureButton.setVisibility(View.VISIBLE);
                            mSurfaceView.setVisibility(View.VISIBLE);
                            mSurfaceViewDraw.setVisibility(View.VISIBLE);
                        }
                    }

                } else {
                    if (isVertical) {
                        isVertical = false;
                        if (!captureInProgress) {
                            holdVerticallyImage.setVisibility(View.VISIBLE);
                            holdVerticallyText.setVisibility(View.VISIBLE);
                            captureButton.setVisibility(View.GONE);
                            mSurfaceView.setVisibility(View.GONE);
                            mSurfaceViewDraw.setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
    }

    public double fromDegreeToProgress(double degree) {
        if (degree >= startGyroDegree) {
            return degree - startGyroDegree;
        } else {
            return 360 - (startGyroDegree - degree);
        }
    }

    public double fromSensorToDegrees(float sensorValue) {
        if (sensorValue < 0) {
            return 60 * sensorValue + 360;
        } else {
            return 60 * sensorValue;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private Camera.Size getBestPreviewSize(Camera.Parameters parameters) {
        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        bestSize = sizeList.get(0);
        for (int j = 1; j < sizeList.size(); j++) {
            if ((sizeList.get(j).width * sizeList.get(j).height) > (bestSize.width * bestSize.height)) {
                bestSize = sizeList.get(j);
            }
        }
        return bestSize;
    }

}

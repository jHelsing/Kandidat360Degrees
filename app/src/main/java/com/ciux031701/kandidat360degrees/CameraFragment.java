package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
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

    private SurfaceView mSurfaceViewBelow, mSurfaceViewTop;
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
    private boolean isSafeToTakePicture = true; //is it safe to capture a picture?
    private int nbrOfPicturesTaken = 0; //number of pictures taken in the panorama

    ProgressDialog progressDialog;

    private DrawerLayout mDrawerLayout;

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

        mDrawerLayout = (DrawerLayout)getActivity().findViewById(R.id.drawer_layout);

        isVertical = false;
        captureInProgress = false;
        highestGyroDegree=0;
        isHalfWay=false;
        lastProgressAngle=0;
        lastDegree = 0;
        //For the sensors:
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this,magnetometer,SensorManager.SENSOR_DELAY_UI);

        //GUI: buttons & views
        angleProgressBar = (ProgressBar)root.findViewById(R.id.angleProgressBar);
        angleProgressBar.setVisibility(View.GONE);
        previewImage = (ImageView)root.findViewById(R.id.previewImage);
        captureButton = (ImageButton)root.findViewById(R.id.sendToShareButton);
        holdVerticallyText = (TextView)root.findViewById(R.id.holdVerticallyText);
        holdVerticallyImage = (ImageView)root.findViewById(R.id.holdVerticallyImage);
        backButton = (ImageButton)root.findViewById(R.id.backButton);
        backButton.setBackgroundResource(R.drawable.temp_return);
        captureButton.setVisibility(View.GONE);
        previewImage.setVisibility(View.GONE);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                Fragment fragment = new ExploreFragment();
                FragmentManager fragmentManager = getActivity().getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame,fragment).commit();
                //fragmentTransaction.addToBackStack(null);

            }
        });

        //If not taking pictures or in finalization -- take a picture.
        //If in finalization - switch to upload-fragment.
        //If taken a picture - go to finalization (show the panorama)
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!captureInProgress) {
                    //Take a picture
                    captureInProgress = true;
                    //angleProgressBar.setVisibility(View.VISIBLE);
                    startGyroDegree=lastDegree;
                    backButton.setVisibility(View.GONE);
                    if(mCam != null && isSafeToTakePicture){
                        //set the flag to false so we don't take two pictures at the same time
                        isSafeToTakePicture = false;
                        mCam.takePicture(null,null,jpegCallback);
                        nbrOfPicturesTaken++;
                    }
                    if(nbrOfPicturesTaken == 3){
                        //Save the image
                        saveAndMakePanorama();
                    }
                }else {
                    args = new Bundle();
                    args.putString("origin","camera");
                    ImageViewFragment fragment = new ImageViewFragment();
                    fragment.setArguments(args);
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("camera").commit();
                }

            }
        });

        //Views to show the camera (below) and the most recently taken picture (top) in:
        mSurfaceViewBelow = (SurfaceView)root.findViewById(R.id.surfaceViewBelow);
        mSurfaceViewBelow.getHolder().addCallback(mSurfaceCallback);
        mSurfaceViewBelow.setVisibility(View.GONE);

        mSurfaceViewTop = (SurfaceView)root.findViewById(R.id.surfaceViewTop);
        mSurfaceViewTop.setZOrderOnTop(true);
        mSurfaceViewTop.getHolder().setFormat(PixelFormat.TRANSPARENT);
        mSurfaceViewTop.setVisibility(View.GONE);

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
    private final Camera.PictureCallback jpegCallback = new Camera.PictureCallback(){
        public void onPictureTaken(byte[] data, Camera camera) {
            //byte[] --> bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
            //Rotate to portrait mode
            Matrix rotationMatrix = new Matrix();
            rotationMatrix.postRotate(90);
            bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),rotationMatrix,false);

            //TODO: save the image to a List (to openCV)

            //To draw 1/3 of the most recently taken picture:
            Canvas canvas = null;
            try {
                canvas = mSurfaceViewTop.getHolder().lockCanvas(null);
                synchronized (mSurfaceViewTop.getHolder()){
                    //Clear canvas from other pictures
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    //To fit the view -- scale the image:
                    float scale = 1.0f * mSurfaceViewBelow.getHeight()/bitmap.getHeight();
                    Bitmap scaleImage = Bitmap.createScaledBitmap(bitmap,(int)(scale*bitmap.getWidth()),mSurfaceViewBelow.getHeight(),false);
                    Paint paint = new Paint();

                    paint.setAlpha(200);
                    //Draw 1/3 of the image:
                    canvas.drawBitmap(scaleImage,-scaleImage.getWidth()*2/3,0,paint);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null){
                    mSurfaceViewTop.getHolder().unlockCanvasAndPost(canvas);
                }
            }
            //Start preview of the camera & set safe to take pictures to true
            mCam.startPreview();
            isSafeToTakePicture = true;
        }

    };

    //For the surfaceviews:
    private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback(){
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder){
            try{
                //To display the camera on this surfaceview:
                mCam.setPreviewDisplay(surfaceHolder);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height){
            //Obtain current parameters of the camera
            Camera.Parameters myParameters = mCam.getParameters();
            //Get the best preview size
            Camera.Size bestSize = getBestPreviewSize(myParameters);
            if(bestSize != null){
                //Set preview size to best size
                myParameters.setPreviewSize(bestSize.width,bestSize.height);
                mCam.setParameters(myParameters);
                //Rotate to portrait mode (90 degrees)
                mCam.setDisplayOrientation(90);

                mCam.startPreview();
            }
        }
        @Override
        public void surfaceDestroyed(SurfaceHolder holder){
        }
    };

    private void saveAndMakePanorama(){
        Thread worker = new Thread(processImageRunnable);
        worker.start();
    }

    private Runnable processImageRunnable = new Runnable() {
        @Override
        public void run(){
            showProgressDialog();
            //TODO: openCV parts
            closeProgressDialog();
        }
    };

    //To stop the camera preview during computations
    private void showProgressDialog(){
        getActivity().runOnUiThread(new Runnable(){
            @Override
            public void run() {
                mCam.stopPreview();
                progressDialog = ProgressDialog.show(getActivity(),"","Panorama",true);
                progressDialog.setCancelable(false);
            }
        });
    }

    //To start camera preview when computations are done
    private void closeProgressDialog(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCam.startPreview();
                progressDialog.dismiss();
            }
        });
    }



    @Override
    public void onPause() {
        super.onPause();
        if(mCam != null){
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
                if(currentDegrees > lastDegree){
                    currentDegrees++;
                }else {
                    currentDegrees--;
                }
                lastDegree = currentDegrees;
                if(captureInProgress){
                    int newProgressAngle = (int)fromDegreeToProgress(lastDegree);
                    System.out.println(newProgressAngle);
                    if(newProgressAngle < 200 && newProgressAngle > 160 && isHalfWay!=true){
                        System.out.println("halfway=true");
                        isHalfWay=true;
                    }

                    //To prevent weird jumps
                    if(Math.abs(lastProgressAngle-newProgressAngle)<50 && Math.abs(newProgressAngle-lastProgressAngle)<50) {
                        lastProgressAngle=newProgressAngle;
                        //Compare with 180 abd ifHalfWay so it doesn't register when we go from 1,0,360,359,...
                        if (newProgressAngle < 180 && isHalfWay == false && highestGyroDegree < newProgressAngle) {
                            highestGyroDegree = newProgressAngle;
                            angleProgressBar.setProgress((int) highestGyroDegree);
                        } else if (newProgressAngle >= 180 && isHalfWay == true && highestGyroDegree < newProgressAngle) {
                            highestGyroDegree = newProgressAngle;
                            angleProgressBar.setProgress((int) highestGyroDegree);
                        }
                        if(angleProgressBar.getProgress()>357){
                            captureButton.performClick();
                        }
                    }
                }

                if(orientation[1] < 1.75 && orientation[1] > 1.25 || orientation[1] < -1.25 && orientation[1] > -1.75){
                    if(!isVertical) {
                        isVertical = true;
                        if(!captureInProgress) {
                            holdVerticallyImage.setVisibility(View.GONE);
                            holdVerticallyText.setVisibility(View.GONE);
                            captureButton.setVisibility(View.VISIBLE);
                            mSurfaceViewBelow.setVisibility(View.VISIBLE);
                            mSurfaceViewTop.setVisibility(View.VISIBLE);
                        }
                    }

                }else{
                    if(isVertical) {
                        isVertical = false;
                        if(!captureInProgress) {
                            holdVerticallyImage.setVisibility(View.VISIBLE);
                            holdVerticallyText.setVisibility(View.VISIBLE);
                            captureButton.setVisibility(View.GONE);
                            mSurfaceViewBelow.setVisibility(View.GONE);
                            mSurfaceViewTop.setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
    }

    public double fromDegreeToProgress(double degree){
        if(degree>=startGyroDegree){
            return degree-startGyroDegree;
        }else{
            return 360-(startGyroDegree-degree);
        }
    }

    public double fromSensorToDegrees(float sensorValue){
        if(sensorValue<0){
            return 60*sensorValue+360;
        }else{
            return 60*sensorValue;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private Camera.Size getBestPreviewSize(Camera.Parameters parameters){
        Camera.Size bestSize = null;
        List<Camera.Size> listOfSizes = parameters.getSupportedPreviewSizes();
        bestSize = listOfSizes.get(0);
        for(int i=1; i < listOfSizes.size(); i++){
            if((listOfSizes.get(i).width * listOfSizes.get(i).height) > (bestSize.width * bestSize.height)){
                bestSize = listOfSizes.get(i);
            }
        }
        return bestSize;
    }

}

package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

/**
 * Created by boking on 2017-02-17.
 */

public class CameraFragment extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2, SensorEventListener {

    private TextView holdVerticallyText;
    private CameraBridgeViewBase mOpenCvCameraView;
    private ImageButton backButton;
    private ImageView holdVerticallyImage;
    private ImageButton captureButton;

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
    private double startDegree;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_camera, container, false);

        isVertical = false;
        captureInProgress = false;
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this,magnetometer,SensorManager.SENSOR_DELAY_UI);

        lastDegree = 0;

        captureButton = (ImageButton)root.findViewById(R.id.captureButton);
        holdVerticallyText = (TextView)root.findViewById(R.id.holdVerticallyText);
        holdVerticallyImage = (ImageView)root.findViewById(R.id.holdVerticallyImage);
        backButton = (ImageButton)root.findViewById(R.id.backButton);
        captureButton.setVisibility(View.GONE);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new ExploreFragment();
                FragmentManager fragmentManager = getActivity().getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }
        });
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!captureInProgress) {
                    captureInProgress = true;
                    startDegree=lastDegree;
                    backButton.setVisibility(View.GONE);
                }else{
                    captureInProgress = false;
                    backButton.setVisibility(View.VISIBLE);
                }

            }
        });

        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mOpenCvCameraView = (CameraBridgeViewBase) root.findViewById(R.id.javaCameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.GONE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        //mOpenCvCameraView.setMaxFrameSize(720, 480);
        return root;
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this.getActivity()) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("opencvtag", "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("opencv", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this.getActivity(), mLoaderCallback);
        } else {
            Log.d("opencv", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return inputFrame.rgba();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //Maybe we want to do this like once every fifth
        //second in another thread rather than all the time
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = sensorEvent.values;
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = sensorEvent.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                currentDegrees = fromSensorToDegrees(orientation[0]);
                if(currentDegrees > lastDegree){
                    currentDegrees++;
                }else {
                    currentDegrees--;
                }
                lastDegree = currentDegrees;

                if(orientation[1] < 1.75 && orientation[1] > 1.25 || orientation[1] < -1.25 && orientation[1] > -1.75){
                    if(!isVertical) {
                        isVertical = true;
                        if(!captureInProgress) {
                            mOpenCvCameraView.setVisibility(View.VISIBLE);
                            //mOpenCvCameraView.setAlpha(1);
                            holdVerticallyImage.setVisibility(View.GONE);
                            holdVerticallyText.setVisibility(View.GONE);
                            captureButton.setVisibility(View.VISIBLE);
                        }
                    }

                }else{
                    if(isVertical) {
                        isVertical = false;
                        if(!captureInProgress) {
                            mOpenCvCameraView.setVisibility(View.GONE);
                            //mOpenCvCameraView.setAlpha(0);
                            holdVerticallyImage.setVisibility(View.VISIBLE);
                            holdVerticallyText.setVisibility(View.VISIBLE);
                            captureButton.setVisibility(View.GONE);
                        }
                    }
                }
            }
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
}

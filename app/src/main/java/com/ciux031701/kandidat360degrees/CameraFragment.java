package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
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
import android.widget.Toast;


import org.opencv.core.Mat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import com.ciux031701.kandidat360degrees.representation.NativePanorama;

import org.opencv.android.Utils;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;


/**
 * Created by boking on 2017-02-17.
 */

public class CameraFragment extends Fragment implements SensorEventListener {

    private TextView holdVerticallyText;
    private ImageButton backButton;
    private ImageView holdVerticallyImage;
    private ImageButton captureButton;
    private ImageView previewImage;
    private ImageView angleImage;

    private SurfaceView mSurfaceView;
    private DrawDotSurfaceView mSurfaceViewDraw;
    private Camera mCam;

    //Sensor stuff
    private Sensor accelerometer;
    private Sensor magnetometer;
    private SensorManager sensorManager;
    private Sensor rotationVector;
    float[] mGravity;
    float[] mGeomagnetic;
    double currentDegrees;
    double lastDegree;
    private float[] mRotationMatrix;

    private boolean isVertical;
    private boolean captureInProgress;
    private boolean isSafeToTakePicture = true; //is it safe to capture a picture?
    private int nbrOfPicturesTaken = 0; //number of pictures taken in the panorama

    private ProgressDialog progressDialog;
    private Mat resultPanorama;
    private Bitmap resultPanoramaBmp;

    private DrawerLayout mDrawerLayout;
    private ArrayList<Mat> listOfTakenImages = new ArrayList<>();
    private int nbrOfImages = 20;
    private double targetDegree;

    private Bundle args;
    private ProgressBar angleProgressBar;
    private double startGyroDegree;
    private int lastProgressAngle;
    private boolean isHalfWay; //temporary until we get actual picture steps going
    private float orientation[];
    float rField[], iField[];
    private LinkedList<Double> previousAngles;
    private boolean isFirstSensorChanged;
    private boolean timerInProcess;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_camera, container, false);

        mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        timerInProcess = false;
        previousAngles = new LinkedList();

        isVertical = false;
        isFirstSensorChanged = true;
        captureInProgress = false;

        isHalfWay = false;
        lastProgressAngle = 0;

        lastDegree = 0;
        //For the sensors:
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this,rotationVector,SensorManager.SENSOR_DELAY_UI);

        orientation = new float[3];
        mRotationMatrix = new float[9];

        //GUI: buttons & views
        angleImage = (ImageView) root.findViewById(R.id.angleImage);
        angleProgressBar = (ProgressBar) root.findViewById(R.id.angleProgressBar);
        angleProgressBar.setVisibility(View.GONE);
        captureButton = (ImageButton) root.findViewById(R.id.sendToShareButton);
        holdVerticallyText = (TextView) root.findViewById(R.id.holdVerticallyText);
        holdVerticallyImage = (ImageView) root.findViewById(R.id.holdVerticallyImage);
        backButton = (ImageButton) root.findViewById(R.id.backButton);
        backButton.setBackgroundResource(R.drawable.temp_return);
        captureButton.setVisibility(View.GONE);


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
                //Take a picture
                captureInProgress = true;
                angleProgressBar.setVisibility(View.VISIBLE);
                System.out.println("Startgyrodegree: " + startGyroDegree);
                backButton.setVisibility(View.GONE);
                if (mCam != null && isSafeToTakePicture) {
                    //set the flag to false so we don't take two pictures at the same time
                    isSafeToTakePicture = false;
                    mCam.takePicture(null, null, jpegCallback);
                    nbrOfPicturesTaken++;
                }
                if (nbrOfPicturesTaken == 3) {
                    saveAndMakePanorama();

                    args = new Bundle();
                    args.putString("origin", "camera");
                    args.putParcelable("image", resultPanoramaBmp);
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

        mSurfaceViewDraw = (DrawDotSurfaceView) root.findViewById(R.id.surfaceViewDraw);
        mSurfaceViewDraw.setZOrderOnTop(true);
        //mSurfaceViewDraw.getHolder().addCallback(mSurfaceCallbackDraw);
        mSurfaceViewDraw.getHolder().setFormat(PixelFormat.TRANSPARENT);
        mSurfaceViewDraw.setVisibility(View.GONE);

        Display mDisp = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        mDisp.getSize(size);
        int centerX = size.x / 2;
        int centerY = size.y / 2;
        Point center = new Point(centerX, centerY);
        mSurfaceViewDraw.setCenter(center);

        holdVerticallyImage.setVisibility(View.GONE);
        holdVerticallyText.setVisibility(View.GONE);
        captureButton.setVisibility(View.VISIBLE);
        mSurfaceView.setVisibility(View.VISIBLE);
        mSurfaceViewDraw.setVisibility(View.VISIBLE);

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

            targetDegree = startGyroDegree + listOfTakenImages.size() * (360 / nbrOfImages);
            mSurfaceViewDraw.setTargetDegree((int) targetDegree);
            mSurfaceViewDraw.setTargetAcquired(true);
            //Start preview of the camera & set safe to take pictures to true
            mCam.startPreview();
            isSafeToTakePicture = true;
        }

    };

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
                //Rotate to portrait mode (90 degrees)
                mCam.setDisplayOrientation(90);

                mCam.startPreview();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    };

    private void saveAndMakePanorama() {
        showProgressDialog();
        //openCV-parts:
        try {
            int nbrOfImages = listOfTakenImages.size();
            long[] imageAddresses = new long[nbrOfImages];
            for (int i = 0; i < nbrOfImages; i++) {
                imageAddresses[i] = listOfTakenImages.get(i).getNativeObjAddr();
            }
            resultPanorama = new Mat(); //a mat to store the final panorama in
            NativePanorama.processPanorama(imageAddresses, resultPanorama.getNativeObjAddr());
            //Save the image to internal memory ------------------ not working :(
            File path = new File(getActivity().getFilesDir() + "/panoramas/");
            path.mkdirs();
            File file = new File(path, "image.png");
            final String fileName = file.getPath();
            boolean success = Imgcodecs.imwrite(fileName, resultPanorama);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "File saved at (internal): " + fileName, Toast.LENGTH_LONG).show();
                }
            });
            if (success) {
                Log.i(TAG, "SUCCESS writing image to internal storage");
            } else {
                Log.i(TAG, "Fail writing image to internal storage");
            }
            //Convert Mat to Bitmap so we can view the image in ImageViewFragment:
            Log.i(TAG, "Type of Mat: " + resultPanorama.type()); //type = 16 --> CV_8UC3, then it "works", is sometimes 0??
            resultPanoramaBmp = Bitmap.createBitmap(resultPanorama.cols(), resultPanorama.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(resultPanorama, resultPanoramaBmp); //work with type CV_8UC3

            listOfTakenImages.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //end of openCV-parts
        closeProgressDialog();
    }


    //To stop the camera preview during computations
    private void showProgressDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCam != null) {
                    mCam.stopPreview();
                }
                progressDialog = ProgressDialog.show(getActivity(), "", "Creating panorama", true);
                progressDialog.setCancelable(false);
            }
        });
    }

    //To start camera preview when computations are done
    private void closeProgressDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCam != null) {
                    mCam.startPreview();
                }
                progressDialog.dismiss();
            }
        });
    }


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
        if (ContextCompat.checkSelfPermission(ThreeSixtyWorld.getAppContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            //ask for authorisation
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{android.Manifest.permission.CAMERA}, 50);
        else
            mCam = Camera.open(0); // 0 = back camera
    }

    //Low-pass filter
    protected float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + 0.15f * (input[i] - output[i]);
        }
        return output;
    }

    //Sensors:
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR)
        {
            // Convert the rotation-vector to a 4x4 matrix.
            SensorManager.getRotationMatrixFromVector(mRotationMatrix,sensorEvent.values);
            SensorManager.remapCoordinateSystem(mRotationMatrix,SensorManager.AXIS_X, SensorManager.AXIS_Z,mRotationMatrix);
            SensorManager.getOrientation(mRotationMatrix, orientation);

            // Optionally convert the result from radians to degrees
            orientation[0] = (float) Math.toDegrees(orientation[0]);
            orientation[1] = (float) Math.toDegrees(orientation[1]);
            orientation[2] = (float) Math.toDegrees(orientation[2]);

            if (orientation[1] > -10 && orientation[1] <15) {
                if (!isVertical) {
                    isVertical = true;
                    holdVerticallyImage.setVisibility(View.GONE);
                    holdVerticallyText.setVisibility(View.GONE);
                    captureButton.setVisibility(View.VISIBLE);
                    mSurfaceView.setVisibility(View.VISIBLE);
                    mSurfaceViewDraw.setVisibility(View.VISIBLE);
                }

            } else {
                if (isVertical) {
                    isVertical = false;
                    holdVerticallyImage.setVisibility(View.VISIBLE);
                    holdVerticallyText.setVisibility(View.VISIBLE);
                    captureButton.setVisibility(View.GONE);
                    mSurfaceView.setVisibility(View.GONE);
                    mSurfaceViewDraw.setVisibility(View.GONE);
                }
            }

            if(captureInProgress&&isVertical){

                currentDegrees = fromOrientationToDegrees(orientation[0]);

                if(isFirstSensorChanged){
                    isFirstSensorChanged=false;
                    startGyroDegree=currentDegrees;
                }

                int newProgressAngle = (int)fromDegreeToProgress(currentDegrees);
                System.out.println("orintation[0]: " + orientation[0] + " orintation[1]: " + orientation[1]);
                System.out.println("current: " + currentDegrees);
                System.out.println("progress: " + newProgressAngle);
                System.out.println("fromorientationtodegrees: " + fromOrientationToDegrees(orientation[0]));

                lastDegree = currentDegrees;
                mSurfaceViewDraw.setCurrentDegree((int)currentDegrees);
                angleImage.setRotation(newProgressAngle);
                angleProgressBar.setProgress(newProgressAngle);
            }
        }
    }

    public double fromOrientationToDegrees(double orientation){
        if(orientation<0){
            return 360-Math.abs(orientation);
        }else
            return orientation;
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
        List<Camera.Size> listOfSizes = parameters.getSupportedPreviewSizes();
        bestSize = listOfSizes.get(0);
        for (int i = 1; i < listOfSizes.size(); i++) {
            if ((listOfSizes.get(i).width * listOfSizes.get(i).height) > (bestSize.width * bestSize.height)) {
                bestSize = listOfSizes.get(i);
            }
        }
        return bestSize;
    }

}

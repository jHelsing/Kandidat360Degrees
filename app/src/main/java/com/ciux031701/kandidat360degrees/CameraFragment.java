package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.nio.ByteBuffer;
import org.opencv.core.Mat;
import java.io.IOException;
import java.util.ArrayList;

import com.ciux031701.kandidat360degrees.communication.LocationHandler;
import com.ciux031701.kandidat360degrees.imageprocessing.ImageProcessor;
import com.ciux031701.kandidat360degrees.imageprocessing.JniMatHolder;
import com.ciux031701.kandidat360degrees.representation.CaptureState;
import com.ciux031701.kandidat360degrees.representation.NativePanorama;
import org.opencv.android.Utils;
import org.opencv.core.Rect;


import java.util.List;
import static android.content.ContentValues.TAG;

/**
 * Created by boking on 2017-02-17.
 */

public class CameraFragment extends Fragment implements SensorEventListener, StitchingAsyncResponse {
    private CaptureState cState;
    private TextView holdVerticallyText;
    private ImageButton backButton;
    private ImageView holdVerticallyImage;
    private ImageButton captureButton;

    private SurfaceView mSurfaceView;
    private DrawDotSurfaceView mSurfaceViewDraw;
    private Camera mCam;

    float currentDegrees;
    float lastDegree;
    private float[] mRotationMatrix;

    PowerManager.WakeLock wl;
    private boolean isVertical;
    private int nbrOfPicturesTaken = 0; //nbr of currently taken pictures

    private ProgressDialog progressDialog;
    private Bitmap resultPanoramaBmp;

    private DrawerLayout mDrawerLayout;
    private ArrayList<ByteBuffer> matHandles = new ArrayList<>();
    private int nbrOfImages = 20;

    private float startGyroDegree;
    private float orientation[];

    private boolean isFirstSensorChanged;
    private boolean proximityCheckerInProgress;
    private boolean shouldTryToViewVertically;

    private boolean panoramaCreated = false;
    private boolean previewQueued = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_camera, container, false);
        mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        proximityCheckerInProgress = false;
        isVertical = false;
        isFirstSensorChanged = true;
        shouldTryToViewVertically = true;

        lastDegree = 0;
        //For the sensors:
        SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, rotationVector,SensorManager.SENSOR_DELAY_UI);

        //GUI: buttons & views
        captureButton = (ImageButton) root.findViewById(R.id.sendToShareButton);
        holdVerticallyText = (TextView) root.findViewById(R.id.holdVerticallyText);
        holdVerticallyImage = (ImageView) root.findViewById(R.id.holdVerticallyImage);
        backButton = (ImageButton) root.findViewById(R.id.backButton);

        backButton.setBackgroundResource(R.drawable.temp_return);
        captureButton.setVisibility(View.GONE);

        orientation = new float[3];
        mRotationMatrix = new float[9];

        PowerManager pm = (PowerManager) ThreeSixtyWorld.getAppContext().getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "wakeLock");
        setState(CaptureState.IDLE);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                Fragment fragment = new ExploreFragment();
                FragmentManager fragmentManager = getActivity().getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, "EXPLORE_FRAGMENT").commit();
                //fragmentTransaction.addToBackStack(null);

            }
        });


        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getState() == CaptureState.NEXT){
                    saveAndMakePanorama();
                }else {
                    setState(CaptureState.NEXT);
                    captureButton.setImageResource(R.drawable.temp_check_black);
                    backButton.setVisibility(View.GONE);
                    takePicture();
                }
            }
        });

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
        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        LocationHandler.tryLocationFix(getActivity());
    }

    private final Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //byte[] --> bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = 2;
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length , options);

            //Rotate the picture to fit portrait mode
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
            //Convert the image to Mat, to be able to use openCV
            Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(), 16); //type of Mat needs to 16, CV_8UC3, to be able to use matToBitmap(..) later
            Utils.bitmapToMat(bitmap, mat);
            bitmap.recycle();
            final JniMatHolder matHolder = new JniMatHolder(mat);
            matHandles.add(matHolder.getHandle());
            System.out.println("JPEG rotated and created bitmap");

            //Convert the image to Mat, to be able to use openCV

            //listOfTakenImages.add(mat);

            //float targetDegree = fromDegreeToProgress(startGyroDegree + listOfTakenImages.size() * (360 / nbrOfImages));
            float targetDegree = fromDegreeToProgress(startGyroDegree + matHandles.size() * (360 / nbrOfImages));
            mSurfaceViewDraw.setTargetDegree(targetDegree);
            mSurfaceViewDraw.setTargetAcquired(true);
            //Start preview of the camera & set safe to take pictures to true
            mCam.startPreview();
            setState(CaptureState.NEXT);
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
                myParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
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
        setState(CaptureState.PROCESSING);
        StitchingTask task = new StitchingTask();
        task.delegate = this;
        task.execute();
    }

    @Override
    public void processFinish(boolean output) {
        panoramaCreated = output; //this is the result from StitchingTask - true if could stitch to an image, false otherwise
    }

    private class StitchingTask extends AsyncTask<Void,Void,Boolean> {
        public StitchingAsyncResponse delegate = null;

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                ByteBuffer handle = NativePanorama.processPanoramaFromHandles(matHandles);
                JniMatHolder matHolder = new JniMatHolder(handle);
                Mat resultPanorama = matHolder.getMatAndFreeData();
                if (resultPanorama == null) {
                    ThreeSixtyWorld.showToast(getActivity(),"Something went wrong during image stitching.");
                    recreateFragment();
                }
                else if(resultPanorama.empty()){
                    ThreeSixtyWorld.showToast(getActivity(),"Something went wrong during image stitching.");
                    recreateFragment();
                }
                Rect cropRect = ImageProcessor.getBlackCroppedRect(resultPanorama);
                if (cropRect == null) {
                    ThreeSixtyWorld.showToast(getActivity(), "Skipping image cropping due to error.");
                }
                else{
                    resultPanorama = resultPanorama.submat(cropRect);
                }

                Log.i(TAG, "Type of Mat: " + resultPanorama.type()); //type = 16 --> CV_8UC3, then it "works", is sometimes 0??
                resultPanoramaBmp = Bitmap.createBitmap(resultPanorama.cols(), resultPanorama.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(resultPanorama, resultPanoramaBmp); //work with type CV_8UC3
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPreExecute() {
            if (mCam != null) {
                mCam.stopPreview();
            }
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Creating panorama...");
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.dismiss();
            delegate.processFinish(result);
            if (result) {
                try {
                    sendPanoramaToImageView();
                }catch(IllegalStateException e){
                    //Fragment is paused and onResume will call the function instead.
                    previewQueued = true;
                }
            }
        }
    }


    private void recreateFragment(){
        CameraFragment cameraFragment = new CameraFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, cameraFragment, "CAMERA_FRAGMENT");
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseWakeLock();
        if (mCam != null) {
            mCam.stopPreview();
            mCam.release();
            mCam = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(resultPanoramaBmp != null && previewQueued == true)
            sendPanoramaToImageView();
        if (ContextCompat.checkSelfPermission(ThreeSixtyWorld.getAppContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            //ask for authorisation
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{android.Manifest.permission.CAMERA}, 50);
        else
            mCam = Camera.open(0); // 0 = back camera
        setState(CaptureState.IDLE);
    }

    //Sensors:
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR && (getState() == CaptureState.IDLE || getState() == CaptureState.NEXT)) {
                // Convert the rotation-vector to a 4x4 matrix.
                SensorManager.getRotationMatrixFromVector(mRotationMatrix, sensorEvent.values);
                SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, mRotationMatrix);
                SensorManager.getOrientation(mRotationMatrix, orientation);

                // Optionally convert the result from radians to degrees
                orientation[0] = (float) Math.toDegrees(orientation[0]);
                orientation[1] = (float) Math.toDegrees(orientation[1]);
                orientation[2] = (float) Math.toDegrees(orientation[2]);


                if(shouldTryToViewVertically){
                        //The device is considered vertical if the pitch is in the range -12-12
                    if (orientation[1] > -12 && orientation[1] < 12) {
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
                    shouldTryToViewVertically = false;
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            shouldTryToViewVertically = true;
                        }
                    }, 300);
                }

                //Save current degree if taking a pano and holding phone vertically
                if (getState() == CaptureState.NEXT && isVertical) {
                    currentDegrees = fromOrientationToDegrees(orientation[0]);
                    if (isFirstSensorChanged) {
                        isFirstSensorChanged = false;
                        startGyroDegree = currentDegrees;
                    }

                    //If the difference between target and currentangle are <= 2 (both horizontal and vertical)
                    // and we dont have a proximity timer started, start one
                    float diff = Math.abs(fromDegreeToProgress(currentDegrees) - mSurfaceViewDraw.getTargetDegree());
                    if (!proximityCheckerInProgress && diff <= 2 &&
                            mSurfaceViewDraw.getVerticalOffset(fromOrientationToDegrees(orientation[1])) <= 2 &&
                            mSurfaceViewDraw.getVerticalOffset(fromOrientationToDegrees(orientation[1])) >= -2) {

                        if (!mSurfaceViewDraw.isStillShowingGreen()) {
                            mSurfaceViewDraw.setCircleColor(Color.YELLOW);
                        }

                        proximityCheckerInProgress = true;
                        final Handler handler = new Handler();

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //is the current angle close enough to the target angle still?
                                if (fromDegreeToProgress(currentDegrees) > (mSurfaceViewDraw.getTargetDegree() - 2) && fromDegreeToProgress(currentDegrees) < (mSurfaceViewDraw.getTargetDegree() + 2)) {
                                    mSurfaceViewDraw.setCircleColor(Color.GREEN);
                                    takePicture();

                                    if (nbrOfPicturesTaken == nbrOfImages) {
                                        saveAndMakePanorama();
                                    }
                                } else {
                                    mSurfaceViewDraw.setCircleColor(Color.RED);
                                }
                                proximityCheckerInProgress = false;
                            }
                        }, 20);//1000 milliseconds check
                    }

                    lastDegree = currentDegrees;
                    mSurfaceViewDraw.setCurrentDegree(fromDegreeToProgress(currentDegrees));
                    mSurfaceViewDraw.setCurrentVerticalDegree(fromOrientationToDegrees(orientation[1]));
                }
            }
    }

    private void sendPanoramaToImageView(){
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.showPanoramaBitmap("camera", resultPanoramaBmp);
        previewQueued = false;
    }

    private void takePicture(){
        if (mCam != null && getState() == CaptureState.NEXT) {
            setState(CaptureState.ACQUIRING_FOCUS);
            mCam.autoFocus(new Camera.AutoFocusCallback() {

                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    //if(success) {
                        setState(CaptureState.CAPTURING);
                        mCam.takePicture(null, null, jpegCallback);
                        nbrOfPicturesTaken++;
                        mCam.autoFocus(null);
                    //}
                    //else{
                        //if(nbrOfPicturesTaken == 0)
                            //setState(CaptureState.IDLE);
                        //else
                            //setState(CaptureState.NEXT);
                    //}
                }
            });
        }
    }
    public float fromOrientationToDegrees(double orientation){
        if(orientation<0){
            return (float)(360-Math.abs(orientation));
        }else
            return (float)orientation;
    }

    public float fromDegreeToProgress(float degree) {
        if (degree >= startGyroDegree) {
            return degree - startGyroDegree;
        } else {
            return 360 - (startGyroDegree - degree);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    private Camera.Size getBestPreviewSize(Camera.Parameters parameters) {
        Camera.Size bestSize;
        List<Camera.Size> listOfSizes = parameters.getSupportedPreviewSizes();
        bestSize = listOfSizes.get(0);
        for (int i = 1; i < listOfSizes.size(); i++) {
            if ((listOfSizes.get(i).width * listOfSizes.get(i).height) > (bestSize.width * bestSize.height)) {
                bestSize = listOfSizes.get(i);
            }
        }
        return bestSize;
    }

    private void setState(CaptureState cState){
        this.cState = cState;

        switch(cState){
            case PROCESSING:
                releaseWakeLock();
                SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
                Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                Sensor magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                Sensor rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
                sensorManager.unregisterListener(this, accelerometer);
                sensorManager.unregisterListener(this, magnetometer);
                sensorManager.unregisterListener(this, rotationVector);
                break;
            case IDLE:
                isFirstSensorChanged = true;
                matHandles.clear();
                currentDegrees = 0;
                lastDegree = 0;
                nbrOfPicturesTaken = 0;
                previewQueued = false;
                acquireWakeLock();
                break;

        }
    }
    private CaptureState getState(){
        return cState;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    private void acquireWakeLock(){
        if(!wl.isHeld())
            wl.acquire();
    }

    private void releaseWakeLock(){
        if(wl.isHeld())
            wl.release();
    }

}

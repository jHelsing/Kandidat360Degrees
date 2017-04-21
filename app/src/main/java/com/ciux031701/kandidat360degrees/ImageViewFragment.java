package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

public class ImageViewFragment extends Fragment implements SurfaceHolder.Callback {

    private ImageButton closeButton;
    private ImageButton arrowLeftButton;
    private ImageButton arrowRightButton;
    private ImageView doneButton;
    private DrawerLayout mDrawerLayout;
    private ImageButton downloadButton;
    private ProgressBar downloadProgressBar;

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

        surfaceView = (SurfaceView)root.findViewById(R.id.imageViewSurface);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    isTouchingScreen=true;
                    touchPoint.x = (int) event.getX();
                    touchPoint.y = (int) event.getY();
                }else if (event.getAction() == android.view.MotionEvent.ACTION_MOVE) {

                    float diff = (touchPoint.x - event.getX())/15;
                    left = ((lastDiff+diff)*-1)% size.x;
                    lastDiff = (lastDiff+diff)% size.x;

                    //maybe one way to implement zoom later but needs tweaking
                    //surfaceView.setLayoutParams(new android.widget.RelativeLayout.LayoutParams(surfaceView.getWidth(), surfaceView.getHeight()+(int)diff));

                    Canvas canvas = surfaceHolder.lockCanvas();
                    drawMyStuff(canvas);
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                    isTouchingScreen=false;
                }

                return true;
            }
        });

        if(origin.equals("profile") || origin.equals("explore")) {
            File file = new File(getActivity().getFilesDir() + FTPInfo.PANORAMA_LOCAL_LOCATION + imageid + FTPInfo.FILETYPE);
            image = Drawable.createFromPath(file.getPath());
            panoramaImage =  ((BitmapDrawable)image).getBitmap();
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
                    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

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
                lastDiff = (lastDiff-150)% size.x;
                tryDrawing(surfaceHolder);
            }
        });
        arrowRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                left-=150;
                lastDiff = (lastDiff+150)% size.x;
                tryDrawing(surfaceHolder);
            }
        });

        downloadProgressBar = (ProgressBar) root.findViewById(R.id.downloadProgressBar);
        downloadProgressBar.setVisibility(View.GONE);

        downloadButton = (ImageButton) root.findViewById(R.id.downloadButton);
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
        float modLeft = left % size.x;

        //scale the image to fit the screen
        //TODO: allow surfaceview to take full parents height. If image height is larger than screen height,
        //TODO: it should be scaled to fit the height of the screen, and then keep aspect ratio
        //TODO: if the image is smaller than the width (in pixels), dont allow scrolling.

        Bitmap scaled = Bitmap.createScaledBitmap(panoramaImage, surfaceView.getWidth(), surfaceView.getHeight(), true);

        canvas.drawBitmap(scaled, modLeft, top, null);
        //canvas.drawBitmap(panoramaImage,new Rect((int)modLeft,(int)top,size.x,size.y),new Rect((int)modLeft,(int)top,size.x,size.y),null);

        if (left < 0) {
            canvas.drawBitmap(scaled, modLeft + size.x, top, null);
            //canvas.drawBitmap(panoramaImage,new Rect((int)modLeft + size.x,(int)top,size.x + size.x,size.y),new Rect((int)modLeft + size.x,(int)top,size.x + size.x,size.y),null);
        } else {
            canvas.drawBitmap(scaled, modLeft - size.x, top, null);
            //canvas.drawBitmap(panoramaImage,new Rect((int)modLeft - size.x,(int)top,size.x - size.x,size.y),new Rect((int)modLeft - size.x,(int)top,size.x - size.x,size.y),null);
        }
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
}

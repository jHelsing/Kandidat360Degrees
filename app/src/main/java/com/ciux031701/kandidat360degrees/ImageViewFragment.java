package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ciux031701.kandidat360degrees.communication.FTPInfo;
import com.ciux031701.kandidat360degrees.communication.JReqIsLiked;
import com.ciux031701.kandidat360degrees.communication.JReqLikeImage;
import com.ciux031701.kandidat360degrees.communication.JReqUnLikeImage;
import com.ciux031701.kandidat360degrees.communication.JRequest;
import com.ciux031701.kandidat360degrees.representation.JSONParser;
import com.ciux031701.kandidat360degrees.representation.ProfilePanorama;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import static android.content.ContentValues.TAG;


/**
 * Created by boking on 2017-03-14.
 */

public class ImageViewFragment extends Fragment{

    private ImageView imageView1;
    private HorizontalScrollView scrollView;
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



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_imageview, container, false);
        origin = getArguments().getString("origin");
        imageid = getArguments().getString("imageid");
        imageView1 = (ImageView)root.findViewById(R.id.imageviewfirst);

        if(origin.equals("profile") || origin.equals("explore")) {
            File file = new File(getActivity().getFilesDir() + FTPInfo.PANORAMA_LOCAL_LOCATION + imageid + FTPInfo.FILETYPE);
            image = Drawable.createFromPath(file.getPath());
            imageView1.setImageDrawable(image);
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


        if(origin.equals("camera")){
            panoramaImage = getArguments().getParcelable("image");
            imageView1.setImageBitmap(panoramaImage);
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
        scrollView = (HorizontalScrollView) root.findViewById(R.id.horizontalScrollView);
        mDrawerLayout = (DrawerLayout)getActivity().findViewById(R.id.drawer_layout);

        closeButton = (ImageButton)root.findViewById(R.id.viewingCloseButton);

        //Scroll to middle dependent on image size
        scrollView.scrollTo(200,0);
        scrollView.setSmoothScrollingEnabled(true);
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
                scrollView.smoothScrollBy(-1000,0);
            }
        });

        arrowRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollView.smoothScrollBy(1000,0);
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

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            downloadButton.setVisibility(View.VISIBLE);
            downloadProgressBar.setVisibility(View.GONE);
        }
    };
}

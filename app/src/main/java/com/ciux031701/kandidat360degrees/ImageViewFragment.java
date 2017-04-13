package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import java.util.ArrayList;
import static android.content.ContentValues.TAG;


/**
 * Created by boking on 2017-03-14.
 */

public class ImageViewFragment extends Fragment{

    private ImageView imageView1;
    private HorizontalScrollView scrollView;
    private ImageButton backButton;
    private ImageView doneButton;
    private DrawerLayout mDrawerLayout;
    private ImageButton downloadButton;
    private ProgressBar downloadProgressBar;

    private String origin;
    private String imageid;
    private Drawable image;
    private Bitmap panoramaImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_imageview, container, false);
        origin = getArguments().getString("origin");
        imageid = getArguments().getString("imageid");

        if(origin.equals("profile")) {
            image = ((ArrayList<Drawable>) getArguments().getSerializable("panorama")).get(0);
            imageView1.setImageDrawable(image);
        }

        doneButton = (ImageButton)root.findViewById(R.id.sendToShareButton);
        imageView1 = (ImageView)root.findViewById(R.id.imageviewfirst);
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

        backButton = (ImageButton)root.findViewById(R.id.viewingBackButton);
        //Scroll to middle dependent on image size
        scrollView.scrollTo(200,0);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {
                    FragmentManager fm = getActivity().getFragmentManager();
                    fm.popBackStackImmediate("view", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
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

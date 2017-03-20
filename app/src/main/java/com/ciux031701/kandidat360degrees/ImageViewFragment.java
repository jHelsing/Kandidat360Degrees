package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;

/**
 * Created by boking on 2017-03-14.
 */

public class ImageViewFragment extends Fragment{

    private ImageView imageView1;
    private HorizontalScrollView scrollView;
    private ImageButton backButton;
    private ImageView doneButton;
    private DrawerLayout mDrawerLayout;

    private Bundle args;
    private String origin;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_imageview, container, false);
        args = getArguments();
        origin = args.getString("origin");

        doneButton = (ImageButton)root.findViewById(R.id.sendToShareButton);

        if(origin.equals("camera")){
            doneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    Bitmap tempPicture = BitmapFactory.decodeResource(getResources(), R.drawable.panorama_large2);
                    args = new Bundle();
                    args.putParcelable("picture", tempPicture);

                    Fragment fragment = new ShareFragment();
                    FragmentManager fragmentManager = getFragmentManager();
                    fragment.setArguments(args);
                    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

                }
            });
        }else{//Only show the picture
            doneButton.setVisibility(View.GONE);
        }
        imageView1 = (ImageView)root.findViewById(R.id.imageviewfirst);
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
}

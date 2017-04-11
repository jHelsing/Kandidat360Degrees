package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
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
import android.widget.TextView;

import com.ciux031701.kandidat360degrees.communication.FTPInfo;
import com.ciux031701.kandidat360degrees.communication.JReqIsLiked;

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
    private ImageView doneButton;
    private DrawerLayout mDrawerLayout;

    private String origin;
    private String imageid;
    private Drawable image;
    private Bitmap panoramaImage;



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
            TextView usernameView = (TextView) root.findViewById(R.id.imageViewUsernameTextView);
            TextView favView = (TextView) root.findViewById(R.id.imageviewFavouriteTextView);
            usernameView.setText(getArguments().getString("username"));
            favView.setText(getArguments().getString("likes"));
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
                    Bitmap tempPicture = BitmapFactory.decodeResource(getResources(), R.drawable.panorama_large2);
                    Bundle args = new Bundle();
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
        scrollView = (HorizontalScrollView) root.findViewById(R.id.horizontalScrollView);
        mDrawerLayout = (DrawerLayout)getActivity().findViewById(R.id.drawer_layout);

        closeButton = (ImageButton)root.findViewById(R.id.viewingCloseButton);
        //Scroll to middle dependent on image size
        scrollView.scrollTo(200,0);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.popBackStack();
                }


            }
        });


        return root;
    }
}

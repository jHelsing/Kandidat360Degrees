package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by boking on 2017-02-17.
 */

public class UploadFragment extends Fragment {

    private TextView textView;
    private Toolbar toolbar;
    private ImageButton toolbarMenuButton;
    private DrawerLayout mDrawerLayout;
    private ImageView previewPic;
    private Button shareButton;

    private Bundle args;
    private Bitmap pictureInBitmap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_upload, container, false);

        previewPic = (ImageView)root.findViewById(R.id.previewPic);
        shareButton = (Button)root.findViewById(R.id.shareButton);
        args = getArguments();
        /*Things will be handled differently if the fragment if started from the camera or if it is
        started from the drawermenu. If from the camera the picture should already be "chosen".
        If from drawermenu you should be able to choose a picture from either a local file or
        one directly from the camera. In that case the camera should be opened and all info already
        typed in should be sent to the camera fragment as arguments aswell so the user doesn't have
        to retype.*/
        if(args!=null){
            pictureInBitmap = args.getParcelable("picture");
            previewPic.setImageBitmap(pictureInBitmap);
        }

        toolbar = (Toolbar) root.findViewById(R.id.tool_bar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        mDrawerLayout = (DrawerLayout)getActivity().findViewById(R.id.drawer_layout);
        shareButton = (Button)root.findViewById(R.id.shareButton);
        toolbarMenuButton = (ImageButton)root.findViewById(R.id.toolbarMenuButton);
        toolbarMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //arguments so that the explore view can show some kind of loading Toast "Sharing..."
                Toast.makeText(getActivity(), "Sharing...",
                        Toast.LENGTH_SHORT).show();
                args = new Bundle();
                args.putString("shared", "somekindofID");
                Fragment fragment = new ExploreFragment();
                FragmentManager fragmentManager = getActivity().getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        textView = (TextView) root.findViewById(R.id.textView1);
        textView.setText("upload");
        return root;
    }
}

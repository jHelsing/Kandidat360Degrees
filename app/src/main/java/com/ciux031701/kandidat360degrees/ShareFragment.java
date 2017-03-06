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
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import static com.ciux031701.kandidat360degrees.R.id.previewPic;

/**
 * Created by Anna on 2017-03-06.
 * This fragment is started from an Upload-fragment or Camera-fragment.
 */

public class ShareFragment extends Fragment {

    private TextView textView;
    private Toolbar toolbar;
    private ImageButton toolbarMenuButton;
    private DrawerLayout mDrawerLayout;
    private ImageView previewPic;
    private Button shareButton;
    private Switch publicSwitch;

    private Bundle args;
    private Bitmap pictureInBitmap;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View root = inflater.inflate(R.layout.fragment_share, container, false);

        toolbar = (Toolbar) root.findViewById(R.id.tool_bar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        mDrawerLayout = (DrawerLayout)getActivity().findViewById(R.id.drawer_layout);
        shareButton = (Button)root.findViewById(R.id.shareButton);
        publicSwitch = (Switch)root.findViewById(R.id.publicSwitch);
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
        publicSwitch.setChecked(true);
        publicSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //Switch is ON
                }else{
                    //Switch is OFF
                }
            }
        });

        //The picture to be shared:
        args = getArguments();
        if(args!=null){
            pictureInBitmap = args.getParcelable("picture");
            //previewPic.setImageBitmap(pictureInBitmap);
        }

        return root;
    }

}

package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ciux031701.kandidat360degrees.adaptors.FlowPicture;
import com.ciux031701.kandidat360degrees.adaptors.ProfileFlowAdapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by boking on 2017-02-21.
 */

public class ProfileFragment extends Fragment {
    TextView textView;
    Bundle args;
    String username;
    private Toolbar toolbar;
    private ImageButton toolbarMenuButton;
    private DrawerLayout mDrawerLayout;

    ListView pictureListView;
    ListAdapter profileFlowAdapter;
    ArrayList<FlowPicture> pictures;
    FlowPicture[] pictureArray;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        toolbar = (Toolbar) root.findViewById(R.id.tool_bar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        mDrawerLayout = (DrawerLayout)getActivity().findViewById(R.id.drawer_layout);
        toolbarMenuButton = (ImageButton)root.findViewById(R.id.toolbarMenuButton);
        toolbarMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        //Get for which username this profile is for
        args = getArguments();
        username = args.getString("username");

        //Get pictures, total likes nbr of friends or whatever we decide to display from db
        pictures = new ArrayList<FlowPicture>();
        loadPicturesFromDB();

        //Converts arraylist to array
        pictureArray = new FlowPicture[pictures.size()];
        pictureArray = pictures.toArray(pictureArray);

        pictureListView = (ListView)root.findViewById(R.id.pictureListView);
        profileFlowAdapter = new ProfileFlowAdapter(getActivity(),pictureArray);
        pictureListView.setAdapter(profileFlowAdapter);

        return root;
    }

    //Use this to fill upp pictures.
    public void loadPicturesFromDB(){
        //Example of how to add
        //Drawable currentPic = image from database convertet to a Drawable. Uses template picture without third argument
        pictures.add(new FlowPicture("Gothenburg","2017-02-08",""));
        pictures.add(new FlowPicture("Stockholm","2017-02-28",""));
    }
}

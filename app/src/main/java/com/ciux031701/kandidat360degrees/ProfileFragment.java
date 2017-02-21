package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    ListView pictureListView;
    ListAdapter profileFlowAdapter;
    ArrayList<FlowPicture> pictures;
    FlowPicture[] pictureArray;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

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

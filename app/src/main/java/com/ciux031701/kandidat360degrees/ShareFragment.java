package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.ciux031701.kandidat360degrees.adaptors.ShareAdapter;
import com.ciux031701.kandidat360degrees.representation.FriendTuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
    private final ArrayList<FriendTuple> friendList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private boolean firstView = false;

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

        //The friends list:
        mRecyclerView = (RecyclerView) root.findViewById(R.id.share_friends_recycle_view);

        if(!firstView){
            //TODO: Retrieve data from database to friendList instead!
            //Test for implementing a sorted arraylist that is sorted by names
            friendList.add(new FriendTuple( "Jonathan", getActivity()));
            friendList.add(new FriendTuple( "John", getActivity()));
            friendList.add(new FriendTuple( "Amar", getActivity()));
            friendList.add(new FriendTuple( "Bertil", getActivity()));
            friendList.add(new FriendTuple( "Åsa", getActivity()));
            friendList.add(new FriendTuple( "Bengt", getActivity()));
            friendList.add(new FriendTuple( "Peter", getActivity()));
            friendList.add(new FriendTuple( "Sigrid", getActivity()));
            friendList.add(new FriendTuple( "Marcus", getActivity()));
            friendList.add(new FriendTuple( "Daniel", getActivity()));
            friendList.add(new FriendTuple( "Astrid", getActivity()));
            friendList.add(new FriendTuple( "Linea", getActivity()));
            friendList.add(new FriendTuple( "Olof", getActivity()));
            friendList.add(new FriendTuple( "Fredrik", getActivity()));
            friendList.add(new FriendTuple( "Isabell", getActivity()));
            friendList.add(new FriendTuple( "Greta", getActivity()));
            friendList.add(new FriendTuple( "Alexander", getActivity()));
            friendList.add(new FriendTuple( "Linda", getActivity()));
            friendList.add(new FriendTuple( "Sebastian", getActivity()));
            friendList.add(new FriendTuple( "Axel", getActivity()));
            friendList.add(new FriendTuple( "Steve", getActivity()));

            sortFriendlistByName();
            addSectionHeadersToFriendlist();
            firstView = true;
        }

        ShareAdapter adapter = new ShareAdapter(getActivity(),friendList);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return root;
    }

    public void sortFriendlistByName() {
        Collections.sort(friendList, new Comparator<FriendTuple>() {
            @Override
            public int compare(FriendTuple o1, FriendTuple o2) {
                String s1 = o1.getUserName();
                String s2 = o2.getUserName();
                return s1.compareToIgnoreCase(s2);
            }
        });
    }

    public void addSectionHeadersToFriendlist() {
        char currentLetter = friendList.get(0).getUserName().charAt(0);
        friendList.add(0,new FriendTuple(currentLetter + "", getActivity()));
        for(int i = 1; i < friendList.size()-1; i++){
            currentLetter = friendList.get(i).getUserName().charAt(0);
            char nextLetter = friendList.get(i+1).getUserName().charAt(0);
            if(currentLetter != nextLetter){
                friendList.add(i+1,new FriendTuple(nextLetter + "", getActivity()));
            }
        }
    }

}

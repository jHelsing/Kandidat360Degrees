package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ciux031701.kandidat360degrees.adaptors.FriendsAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by boking on 2017-02-17. Modified by Amar on 2017-02-16
 */

public class FriendsFragment extends Fragment {
    private Toolbar toolbar;
    private ImageButton toolbarMenuButton;
    private DrawerLayout mDrawerLayout;
    private TextView toolbarTitle;
    private final ArrayList<FriendTuple> friendList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private boolean firstView = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_friends, container, false);

        //The toolbar:
        setHasOptionsMenu(true);
        toolbar = (Toolbar) root.findViewById(R.id.tool_bar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbarTitle = (TextView)root.findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Friends");
        mDrawerLayout = (DrawerLayout)getActivity().findViewById(R.id.drawer_layout);
        toolbarMenuButton = (ImageButton)root.findViewById(R.id.toolbarMenuButton);
        toolbarMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        //The ListView:
        mRecyclerView = (RecyclerView) root.findViewById(R.id.friends_recycle_view);

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

        FriendsAdapter adapter = new FriendsAdapter(getActivity(),friendList);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //TODO: Add separators to the RecyclerView

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.toolmenu_search, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }
}

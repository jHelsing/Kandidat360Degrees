package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.content.Context;
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
import com.ciux031701.kandidat360degrees.representation.FriendList;
import com.ciux031701.kandidat360degrees.representation.FriendRequestList;
import com.ciux031701.kandidat360degrees.representation.FriendTuple;
import com.ciux031701.kandidat360degrees.communication.JRequest.JResultListener;
import com.ciux031701.kandidat360degrees.communication.*;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private final FriendList friendList = new FriendList();
    private final FriendRequestList friendRequestList = new FriendRequestList();
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

        toolbarTitle = (TextView) root.findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Friends");
        mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        toolbarMenuButton = (ImageButton) root.findViewById(R.id.toolbarMenuButton);
        toolbarMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        //The ListView:
        mRecyclerView = (RecyclerView) root.findViewById(R.id.friends_recycle_view);

        if (!firstView) {
            JReqFriendRequests jReqFriendRequests = new JReqFriendRequests();
            jReqFriendRequests.setJResultListener(
                    new JResultListener() {

                        @Override
                        public void onHasResult(JSONObject result) {
                            boolean error = false;
                            JSONArray friendrequests;
                            try {
                                error = result.getBoolean("error");

                                if (!error) {
                                    friendrequests = result.getJSONArray("friendrequests");
                                    for (int i = 0; i < friendrequests.length(); i++)
                                        friendRequestList.add(new FriendTuple(friendrequests.getJSONObject(i).getString("name"), getActivity()));
                                    mRecyclerView.setAdapter(new FriendsAdapter(getActivity(), friendList, friendRequestList));
                                }
                            } catch (JSONException je) {

                            }

                        }
                    }
            );
            jReqFriendRequests.sendRequest();
            JReqFriends jReqFriends = new JReqFriends();
            jReqFriends.setJResultListener(
                    new JResultListener() {

                        @Override
                        public void onHasResult(JSONObject result) {
                            boolean error = false;
                            JSONArray friends;
                            try {
                                error = result.getBoolean("error");

                                if (!error) {
                                    friends = result.getJSONArray("friends");
                                    for (int i = 0; i < friends.length(); i++)
                                        friendList.add(new FriendTuple(friends.getJSONObject(i).getString("name"), getActivity()));
                                    mRecyclerView.setAdapter(new FriendsAdapter(getActivity(), friendList, friendRequestList));
                                }
                            } catch (JSONException je) {

                            }

                        }
                    }
            );
            jReqFriends.sendRequest();
            //Test for implementing a sorted arraylist that is sorted by names
            firstView = true;
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //TODO: Add separators to the RecyclerView

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.toolmenu_search, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
}

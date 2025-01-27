package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ciux031701.kandidat360degrees.adaptors.FriendSearchAdapter;
import com.ciux031701.kandidat360degrees.adaptors.FriendsAdapter;
import com.ciux031701.kandidat360degrees.communication.Friends;
import com.ciux031701.kandidat360degrees.communication.FriendRequests;
import com.ciux031701.kandidat360degrees.representation.UserTuple;
import com.ciux031701.kandidat360degrees.communication.JRequest.JResultListener;
import com.ciux031701.kandidat360degrees.communication.*;
import com.ciux031701.kandidat360degrees.representation.UserRelationship;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by boking on 2017-02-17. Modified by Amar on 2017-02-16
 */

public class FriendsFragment extends Fragment implements SearchView.OnQueryTextListener {
    private Toolbar toolbar;
    private Menu toolbarMenu;
    private SearchView searchView;
    private ImageButton toolbarMenuButton;
    private DrawerLayout mDrawerLayout;
    private TextView toolbarTitle;
    private final Friends friendList = new Friends();
    private final FriendRequests friendRequestList = new FriendRequests();
    private FastScrollRecyclerView mRecyclerView;
    private boolean firstView = false;
    private ArrayList<UserTuple> searchResult;
    private EditText searchEditText;

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
        mRecyclerView = (FastScrollRecyclerView) root.findViewById(R.id.friends_recycle_view);
        mRecyclerView.setAdapter(new FriendsAdapter(getActivity()));

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //TODO: Add separators to the RecyclerView

        Friends.setFriendsAdapter((FriendsAdapter) mRecyclerView.getAdapter());

        return root;
    }

    private void searchSetup(View root){

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolmenu_search_friends, menu);
        this.toolbarMenu = menu;
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        Drawable searchIcon = getResources().getDrawable(R.drawable.search_icon);
        searchMenuItem.setIcon(searchIcon);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mRecyclerView.setAdapter(new FriendsAdapter(getActivity()));
                return true;
            }
        });
        searchView.setQueryHint("Search users");
        int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = searchView.findViewById(searchPlateId);
        if (searchPlate != null) {
            searchPlate.setBackgroundColor(Color.DKGRAY);
            int searchTextId = searchPlate.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
            TextView searchText = (TextView) searchPlate.findViewById(searchTextId);
            if (searchText != null) {
                searchText.setTextColor(Color.WHITE);
                searchText.setHintTextColor(Color.WHITE);
            }
        }
        searchEditText = ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text));
        searchEditText.setTextColor(Color.BLACK);

        Field f = null;
        try {
            f =TextView.class.getDeclaredField("mCursorDrawableRes");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        f.setAccessible(true);
        try {
            f.set(searchEditText, R.drawable.cursor);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setTextColor(Color.BLACK);
        ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setHintTextColor(Color.LTGRAY);
        searchView.setBackgroundColor(Color.WHITE);
        searchView.setOnQueryTextListener(this);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        JReqSearchUser jReqSearchUser = new JReqSearchUser(query);
        jReqSearchUser.setJResultListener(
                new JResultListener() {
                    @Override
                    public void onHasResult(JSONObject result) {
                        try{
                            boolean error = result.getBoolean("error");
                            if(!error){
                                searchResult = new ArrayList<>();
                                JSONObject user = result.getJSONObject("user");
                                int relationship = 0;
                                if(user != null) {
                                    searchResult.add(new UserTuple(user.getString("name"), getActivity()));
                                    relationship = Integer.parseInt(result.getString("relationship"));
                                }
                                mRecyclerView.setAdapter(new FriendSearchAdapter(getActivity(), friendRequestList, friendList, searchResult, new UserRelationship(relationship)));
                            }
                        }catch(JSONException je){

                        }
                    }
                }
        );
        jReqSearchUser.sendRequest();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}

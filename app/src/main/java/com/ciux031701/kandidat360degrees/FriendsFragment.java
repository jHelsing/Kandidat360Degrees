package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.ciux031701.kandidat360degrees.adaptors.FriendsAdapter;

import java.util.ArrayList;

/**
 * Created by boking on 2017-02-17.
 */

public class FriendsFragment extends Fragment {
    private Toolbar toolbar;
    private ImageButton toolbarMenuButton;
    private DrawerLayout mDrawerLayout;
    private TextView toolbarTitle;

    private ListView mListView;

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
        mListView = (ListView)root.findViewById(R.id.friends_list_view);
        //TODO: Retrieve data from database instead
        final ArrayList<String> data = new ArrayList<>();
        data.add("username1"); data.add("username2"); data.add("username3"); data.add("username4"); data.add("username5"); data.add("username6");
        FriendsAdapter adapter = new FriendsAdapter(getActivity(),data);
        mListView.setAdapter(adapter);
        //TODO: Add separators to the ListView

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedUser = data.get(position);
                //TODO: Go to the selectedUser's profile instead of MrCool's
                Fragment fragment = new ProfileFragment();
                Bundle setArgs = new Bundle();
                setArgs.putString("username", selectedUser);
                fragment.setArguments(setArgs);
                FragmentManager fragmentManager = getActivity().getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.toolmenu_search, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }
}

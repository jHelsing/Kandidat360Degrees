package com.ciux031701.kandidat360degrees;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.ciux031701.kandidat360degrees.adaptors.DrawerAdapter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by boking on 2017-02-14.
 */

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private String[] mListOptions;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private Toolbar toolbar;
    private ImageButton toolbarMenuButton;

    private View drawerHeader;
    private View drawerFooter;
    private TextView usernameText;

    private Session loginSession;
    Bundle b;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Retrieves username parameter from login
        b = getIntent().getExtras();
        String userName = ""; // or other values
        if(b != null)
            userName = b.getString("username");

        loginSession = new Session(userName);

        mListOptions = getResources().getStringArray(R.array.list_options);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        drawerHeader = getLayoutInflater().inflate(R.layout.drawer_header, mDrawerList, false);
        drawerFooter = getLayoutInflater().inflate(R.layout.drawer_footer, mDrawerList, false);
        usernameText = (TextView) drawerHeader.findViewById(R.id.userNameText);
        usernameText.setText(userName);

        mDrawerList.addHeaderView(drawerHeader, null, false);
        mDrawerList.addFooterView(drawerFooter, null, false);
        //mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbarMenuButton = (ImageButton)findViewById(R.id.toolbarMenuButton);
        toolbarMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });
        // Set the adapter for the list view
        mDrawerList.setAdapter(new DrawerAdapter(this,getApplicationContext(), mListOptions));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        showExploreView();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tool_menu, menu);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    //Handles drawer item clicks
    private class DrawerItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }

        //for arguments:
        // Bundle args = new Bundle();
        //args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
        //fragment.setArguments(args);
        private void selectItem(int position) {

            //for some reason position seems to be 1 off, and it does not allow for index 6 since it has length 6.
            position--;
            System.out.println("Pos: " + position);

            Fragment fragment = null;
            Class fragmentClass;
            switch(position) {
                case 0:
                    fragmentClass = ExploreFragment.class;
                    break;
                case 1:
                    fragmentClass = NotificationFragment.class;
                    break;
                case 2:
                    fragmentClass = CameraFragment.class;
                    break;
                case 3:
                    fragmentClass = FriendsFragment.class;
                    break;
                case 4:
                    fragmentClass = UploadFragment.class;
                    break;
                case 5:
                    fragmentClass = SettingsFragment.class;
                    break;
                default:
                    fragmentClass = ExploreFragment.class;
            }

            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

            mDrawerList.setItemChecked(position, true);
            mDrawerLayout.closeDrawer(mDrawerList);
            setTitle(mListOptions[position]);
            mDrawerLayout.closeDrawers();
            //mDrawerLayout.closeDrawer(mDrawerList);

        }

    }

    private void showExploreView(){
        ExploreFragment fragment = new ExploreFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        setTitle("Explore");
    }

}

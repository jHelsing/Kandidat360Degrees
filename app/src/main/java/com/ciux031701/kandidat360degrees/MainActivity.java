package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ciux031701.kandidat360degrees.communication.JReqDestroySession;
import com.ciux031701.kandidat360degrees.communication.JRequester;
import com.ciux031701.kandidat360degrees.adaptors.DrawerAdapter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.ciux031701.kandidat360degrees.communication.Session;
import com.google.android.gms.maps.model.MapStyleOptions;

/**
 * Created by boking on 2017-02-14.
 */

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private String[] mListOptions;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private View drawerHeader;
    private View drawerFooter;
    private TextView usernameText;

    Bundle b;
    Bundle setArgs;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Retrieves username parameter from login
        b = getIntent().getExtras();
        String userName = ""; // or other values
        if(b != null)
            userName = b.getString("username");


        mListOptions = getResources().getStringArray(R.array.list_options);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        drawerHeader = getLayoutInflater().inflate(R.layout.drawer_header, mDrawerList, false);
        drawerFooter = getLayoutInflater().inflate(R.layout.drawer_footer, mDrawerList, false);
        usernameText = (TextView) drawerHeader.findViewById(R.id.userNameText);
        usernameText.setText(userName);

        drawerHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProfileFragment fragment = new ProfileFragment();
                FragmentManager fragmentManager = getFragmentManager();
                setArgs = new Bundle();
                setArgs.putString("username", Session.getUser());
                fragment.setArguments(setArgs);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                setTitle("Profile");
                mDrawerLayout.closeDrawer(mDrawerList);
                mDrawerLayout.closeDrawers();
            }
        });

        mDrawerList.addHeaderView(drawerHeader, null, false);
        //mDrawerList.addFooterView(drawerFooter, null, false);

        mDrawerList.setAdapter(new DrawerAdapter(this,getApplicationContext(), mListOptions));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        showExploreView();
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
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
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
                case 6:
                    destroySession();
                    fragmentClass = LoginFragment.class;
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

        }

    }

    private void showExploreView(){
        ExploreFragment fragment = new ExploreFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragment.setArguments(setArgs);
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        setTitle("Explore");
    }

    private void destroySession(){
        Session.delete();
        JReqDestroySession destroySession = new JReqDestroySession();
        JRequester.setRequest(destroySession);
        JRequester.sendRequest();
    }

    public void loadMapStyling(GoogleMap map) {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e("explore", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("explore", "Can't find style. Error: ", e);
        }
    }

    /**
     * Call this method to display the profile fragment. This makes sure that the profile
     * fragment recieves all the necessary information. This method does not load the panorama
     * feed for the parameter username. It only loads the user information, total number of panoramas,
     * total number of favorite-markings, profile image etc.
     *
     * The panorama feed for the user is instead loaded asynchronous in onCreateView in ProfileFragment.
     *
     * @param username - The username to show the profile page for.
     */
    public void showProfile(String username) {
        // TODO Fetch profile information from backend, the specific user information

        int favs = 0;
        int panoramas = 0;
        // Start the fragment and change the view.
        Bundle profileInfo = new Bundle(4);
        profileInfo.putString("USERNAME", username);
        profileInfo.putInt("FAVS", favs);
        profileInfo.putInt("PANORAMAS", panoramas);
    }
}

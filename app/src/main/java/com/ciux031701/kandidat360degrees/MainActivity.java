package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ciux031701.kandidat360degrees.communication.JReqDestroySession;
import com.ciux031701.kandidat360degrees.communication.JReqProfile;
import com.ciux031701.kandidat360degrees.communication.JRequest;
import com.ciux031701.kandidat360degrees.communication.JRequester;
import com.ciux031701.kandidat360degrees.adaptors.DrawerAdapter;
import com.ciux031701.kandidat360degrees.representation.JSONParser;
import com.ciux031701.kandidat360degrees.representation.ProfilePanorama;
import com.ciux031701.kandidat360degrees.representation.RoundImageView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.ciux031701.kandidat360degrees.communication.Session;
import com.google.android.gms.maps.model.MapStyleOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by boking on 2017-02-14. Revised by Jonathan 2017-03-22
 */

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("MyLib");
    }

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

        RoundImageView profileImageView = (RoundImageView) drawerHeader.findViewById(R.id.imageView);
        profileImageView.setImageDrawable(getResources().getDrawable(R.drawable.anonymous_profile));

        drawerHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProfile(Session.getUser());
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
                    showExploreView();
                    break;
                case 1:
                    showNotificationView();
                    break;
                case 2:
                    showCamera();
                    break;
                case 3:
                    showFriendsView();
                    break;
                case 4:
                    showUploadView();
                    break;
                case 5:
                    showSettingsView();
                    break;
                case 6:
                    destroySession();
                    fragmentClass = LoginFragment.class;
                    try {
                        fragment = (Fragment) fragmentClass.newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Insert the fragment by replacing any existing fragment
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                    break;
                default:
                    showExploreView();
            }

            mDrawerList.setItemChecked(position, true);
            mDrawerLayout.closeDrawer(mDrawerList);
            setTitle(mListOptions[position]);
            mDrawerLayout.closeDrawers();

        }

    }

    public void showExploreView(){
        ExploreFragment fragment = new ExploreFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragment.setArguments(setArgs);
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("explore").commit();
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
        JReqProfile profileReq = new JReqProfile(username, Session.getId(), Session.getUser());
        profileReq.setJResultListener(
                new JRequest.JResultListener(){
                    @Override
                    public void onHasResult(JSONObject result) {
                        boolean error;
                        String message = null, username = null, uploaded = null, views = null, favs = null;
                        JSONArray images = new JSONArray();
                        try {
                            error = result.getBoolean("error");
                            message = result.getString("message");
                            username = result.getString("user");
                            uploaded = result.getString("uploaded");
                            views = result.getString("views");
                            favs = result.getString("likes");
                            images = result.getJSONArray("images");
                        } catch(JSONException je){
                            error = true;
                        }

                        if(!error) {
                            ArrayList<ProfilePanorama> imgs = new ArrayList<ProfilePanorama>();
                            for (int i=0; i < images.length(); i++){
                                try {
                                    JSONArray imgArr = images.getJSONArray(i);
                                    Log.d("PROFILE", imgArr.toString());
                                    ProfilePanorama pp = JSONParser.parseToProfilePanorama(imgArr);
                                    if (pp != null)
                                        imgs.add(pp);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            ProfileFragment fragment = new ProfileFragment();
                            FragmentManager fragmentManager = getFragmentManager();
                            Bundle b = new Bundle();
                            b.putString("username",username);
                            b.putString("uploadCount",uploaded);
                            b.putString("viewsCount",views);
                            b.putString("favsCount",favs);
                            b.putSerializable("images", imgs);
                            fragment.setArguments(b);
                            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("profile").commit();
                        } else {
                            Toast.makeText(getApplicationContext(), "Could not reach the server, please try again later.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        JRequester.setRequest(profileReq);
        JRequester.sendRequest();
    }


    public void showNotificationView() {
        NotificationFragment fragment = new NotificationFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("notifications").commit();
        setTitle("Notifications");
    }

    public void showFriendsView() {
        FriendsFragment fragment = new FriendsFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("friends").commit();
        setTitle("Friends");
    }

    public void showCamera() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        Class fragmentClass = CameraFragment.class;
        try {
            Fragment fragment = (Fragment) fragmentClass.newInstance();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void showUploadView() {
        Class fragmentClass = UploadFragment.class;
        try {
            Fragment fragment = (Fragment) fragmentClass.newInstance();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSettingsView() {
        Class fragmentClass = SettingsFragment.class;
        try {
            Fragment fragment = (Fragment) fragmentClass.newInstance();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}

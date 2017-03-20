package com.ciux031701.kandidat360degrees;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.ciux031701.kandidat360degrees.adaptors.ProfileFlowAdapter;
import com.ciux031701.kandidat360degrees.communication.DownloadService;
import com.ciux031701.kandidat360degrees.communication.ImageType;
import com.ciux031701.kandidat360degrees.communication.JReqImageInfoProfile;
import com.ciux031701.kandidat360degrees.communication.JRequest;
import com.ciux031701.kandidat360degrees.communication.JRequester;
import com.ciux031701.kandidat360degrees.communication.Session;
import com.ciux031701.kandidat360degrees.representation.ProfilePanorama;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;


/**
 * Created by boking on 2017-02-21. Modified by Jonathan on 2017-03-16.
 */

public class ProfileFragment extends Fragment {
    private String username;
    private DrawerLayout mDrawerLayout;
    private ImageButton viewSwitchButton;
    private MapView mapView;
    private GoogleMap googleMap;
    private View root;
    private ImageButton profileMenuButton;

    private ListView pictureListView;
    private ListAdapter profileFlowAdapter;
    private ArrayList<ProfilePanorama> pictures;
    private int[] panoramaIDs;

    private boolean listMode = true;
    private boolean first = true;
    private Bundle instanceState;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_profile, container, false);
        instanceState = savedInstanceState;

        viewSwitchButton = (ImageButton) root.findViewById(R.id.profileSwitchModeButton);
        Toolbar toolbar = (Toolbar) root.findViewById(R.id.tool_bar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        TextView toolbarTitle = (TextView) root.findViewById(R.id.toolbarTitle);
        toolbarTitle.setText(getText(R.string.profile));
        mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        ImageButton toolbarMenuButton = (ImageButton) root.findViewById(R.id.toolbarMenuButton);
        toolbarMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });

        //Set up profile information
        setUpProfileInformation();
        loadProfilePicture();
        setUpProfileMenuButton();

        //Get pictures, total likes nbr of friends or whatever we decide to display from db
        pictures = new ArrayList<>();
        loadPicturesFromDB();

        viewSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (first) {
                    mapView = (MapView) root.findViewById(R.id.profileMapView);
                    mapView.onCreate(instanceState);
                    mapView.onResume(); // needed to get the map to display immediately
                    mapView.setVisibility(View.GONE);
                    first = false;
                    instanceState = null;
                }
                if (listMode) {
                    pictureListView.setVisibility(View.GONE);
                    listMode = false;
                    //noinspection deprecation
                    viewSwitchButton.setImageDrawable(getResources()
                            .getDrawable(R.drawable.enabled_map_view_profile_icon));
                    setUpMap();
                    mapView.setVisibility(View.VISIBLE);
                } else {
                    pictureListView.setVisibility(View.VISIBLE);
                    listMode = true;
                    //noinspection deprecation
                    viewSwitchButton.setImageDrawable(getResources()
                            .getDrawable(R.drawable.disable_map_view_icon_profile));
                    mapView.setVisibility(View.GONE);
                }
            }
        });

        pictureListView = (ListView) root.findViewById(R.id.profilePictureListView);
        profileFlowAdapter = new ProfileFlowAdapter(getActivity(), pictures);
        pictureListView.setAdapter(profileFlowAdapter);
        pictureListView.setOnItemClickListener(new FlowItemClickListener());
        return root;
    }

    private class FlowItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }

        private void selectItem(int position) {

            //TODO: Get the real size image for the selected panorama id
            //TODO: like below from the DB and add that as parameter to the imageviewfragment
            ProfilePanorama selectedPanorama = (ProfilePanorama)pictureListView.getAdapter().getItem(position);
            int panoramaID = selectedPanorama.getPanoramaID();
            System.out.println("PanoramaID: " + panoramaID);

            Bundle args = getArguments();
            args.putString("type","view");

            ImageViewFragment fragment = new ImageViewFragment();
            fragment.setArguments(args);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("profile").commit();

        }
    }


    //Get info for specific image from DB here.
    //Use the marker as a reference when doing so.
    private View onMarkerClicked(Marker marker) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.marker_info_window, null);

        // Getting reference to the TextView to set latitude
        TextView infoWindowText = (TextView) v.findViewById(R.id.infoWindowText);

        infoWindowText.setText(marker.getPosition().toString());
        return v;
    }

    //Use this to fill up pictures.
    private void loadPicturesFromDB() {
        panoramaIDs = getArguments().getIntArray("images");

        for(int i=0; i<panoramaIDs.length; i++) {
            // Fetch each image
            Intent intent =  new Intent(getActivity(), DownloadService.class);
            intent.putExtra("IMAGETYPE", "PREVIEW");
            intent.putExtra("IMAGEID", panoramaIDs[i]);
            intent.putExtra("TYPE", "DOWNLOAD");
            getActivity().startService(intent);

            JRequest getImageInfo = new JReqImageInfoProfile(Session.getId(), panoramaIDs[i], Session.getUser());
            getImageInfo.setJResultListener(new JRequest.JResultListener() {
                @Override
                public void onHasResult(JSONObject result) {
                    boolean error;
                    String message = null, username = null, uploaded = null, views = null, favs = null;
                    JSONArray images = new JSONArray();
                    try{
                        error = result.getBoolean("error");
                        message = result.getString("message");
                        username = result.getString("user");
                        uploaded = result.getString("uploaded");
                        views = result.getString("views");
                        favs = result.getString("likes");
                        images = result.getJSONArray("images");
                    }
                    catch(JSONException je){
                        error = true;
                    }
                    if(!error){
                        int imgs[] = new int[images.length()];
                        for (int i=0; i < images.length(); i++){
                            try{
                                imgs[i] = Integer.parseInt(images.get(i).toString());
                            } catch (JSONException e){
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
                        b.putIntArray("images",imgs);
                    } else{
                        Toast.makeText(getActivity(), "Could not reach the server, please try again later.",Toast.LENGTH_SHORT).show();
                    }
                }
            });
            JRequester.setRequest(getImageInfo);
            JRequester.sendRequest();
        }

        //Example of how to add
        //Drawable currentPic = image from database convertet to a Drawable. Uses template picture without third argument
        ProfilePanorama pp = new ProfilePanorama(0, false, "2017-02-08", "Gothenburg", "Gothenburg", 5);
        pictures.add(pp);
        pp = new ProfilePanorama(1, false, "2017-02-28", "Stockholm", "Stockholm", 0);
        pictures.add(pp);
        pp = new ProfilePanorama(2, false, "2017-03-03", "Malmö", "Malmö", 2);
        pictures.add(pp);
    }

    private void setUpMap() {
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        fetchMap();
    }

    /**
     * Starts to fetch the profile picture of the user from the server.
     */
    private void loadProfilePicture() {
        Intent intent =  new Intent(getActivity(), DownloadService.class);
        intent.putExtra("IMAGETYPE", ImageType.PROFILE);
        intent.putExtra("USERNAME", username);
        intent.putExtra("TYPE", "DOWNLOAD");
        intent.setAction(DownloadService.NOTIFICATION);
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.NOTIFICATION);
        getActivity().registerReceiver(new ProfileImageBroadcastReceiver(), filter);
        getActivity().startService(intent);
    }

    private void setUpProfileInformation() {
        TextView userNameView = (TextView) root.findViewById(R.id.profileUserNameTextView);
        TextView panoramaCountView = (TextView) root.findViewById(R.id.profilePanoramaCountTextView);
        TextView favCountView = (TextView) root.findViewById(R.id.profileFavCountTextView);

        // Process information from arguments
        Bundle args = getArguments();
        username = args.getString("username");
        userNameView.setText(username);

        int panoramaCount = 0;
        try{
            if(!((args.getString("uploadCount")).equals(null))){
                panoramaCount = Integer.parseInt(args.getString("uploadCount"));
            }
        } catch (NumberFormatException e){
            e.printStackTrace();
        }

        int favCount = 0;
        try{
            if(!((args.getString("favsCount")).equals(null))){
                favCount = Integer.parseInt(args.getString("favsCount"));
            }
        } catch (NumberFormatException e){
            e.printStackTrace();
        }

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        String panoramaString = null;
        if (panoramaCount >= 1000 && panoramaCount < 1000000) {
            panoramaString = df.format(panoramaCount / 1000.0) + "k";
        } else if (panoramaCount >= 1000000) {
            panoramaString = df.format(panoramaCount / 1000000) + "M";
        }
        panoramaCountView.setText(panoramaString);

        String favString = null;
        if (favCount >= 1000 && favCount < 1000000) {
            favString = df.format(favCount / 1000.0) + "k";
        } else if (favCount >= 1000000) {
            favString = df.format(favCount / 1000000) + "M";
        }
        favCountView.setText(favString);
    }

    private void setUpProfileMenuButton() {
        profileMenuButton = (ImageButton) root.findViewById(R.id.profileSettingsButton);
        profileMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getActivity(), profileMenuButton);
                Menu menu = popupMenu.getMenu();

                if (Session.getUser().equalsIgnoreCase(username)) {
                    menu.add(R.string.acc_settings);
                }else{
                    // TODO Add checks to see if person already is friend
                    menu.add(R.string.add_friend);
                    menu.add(R.string.remove_friend);
                }
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getTitle().toString()) {
                            case "Add friend":
                                // TODO send add friend request to username from session.getUser()
                                break;
                            case "Remove friend":
                                // TODO send request to remove friend from session.getUser() for username
                                break;
                            case "Settings":
                                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.replace(R.id.content_frame, new SettingsFragment(), "Settings");
                                ft.addToBackStack("Settings");
                                ft.commitAllowingStateLoss();
                                getFragmentManager().executePendingTransactions();
                                break;
                        }
                        return false;
                    }
                });
            }
        });
    }


    /**
     * Fetches the map for the mapView
     */
    private void fetchMap() {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {

                googleMap = mMap;
                googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return onMarkerClicked(marker);
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        return null;
                    }
                });

                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        //get to full screen view?
                        marker.setRotation(marker.getRotation() + 20);
                    }
                });

                ((MainActivity) getActivity()).loadMapStyling(googleMap);

                googleMap.getUiSettings().setMapToolbarEnabled(false);
                // For dropping a marker at a point on the Map
                LatLng gothenburg = new LatLng(57.4, 12);
                googleMap.addMarker(new MarkerOptions().position(gothenburg).title("Here we go bois")
                        .snippet("its happening!").icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                // For zooming automatically to the location of the marker
                //CameraPosition cameraPosition = new CameraPosition.Builder().target(gothenburg).zoom(12).build();
                //googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });
    }

    public class ProfileImageBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra("RESULT", -100)  == Activity.RESULT_OK) {
                Log.d("Profile", "Profile image found and results from download are OK.");
            }

            String path = context.getFilesDir() + "/profiles/"
                    + username + ".jpg";
            Drawable profileImage = Drawable.createFromPath(path);
            ((ImageView) root.findViewById(R.id.profileProfileImage)).setImageDrawable(profileImage);
            File file = new File(path);
            if (file.delete()) {
                Log.d("Profile", "Profile image has been deleted");
            }
            context.unregisterReceiver(this);
        }
    }

    /**
     * A class for the receiver of download of preview images.
     */
    public class ProfilePreviewBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra("RESULT", -100)  == Activity.RESULT_OK) {
                Log.d("Profile", "Preview (" + intent.getIntExtra("IMAGEID", -100) + ") found and results from download are OK.");
            }

            String path = context.getFilesDir() + "/preview/"
                    + username + ".jpg";
            Drawable previewDrawable = Drawable.createFromPath(path);

            File file = new File(path);
            if (file.delete()) {
                Log.d("Profile", "Profile image has been deleted");
            }

            context.unregisterReceiver(this);

            // Add the image to the correct panorama in the arraylist
            int imageID = intent.getIntExtra("IMAGEID", -1);
            int i=0;
            while (pictures.get(i).getPanoramaID() != imageID)
                i++;
            pictures.get(i).setPreview(previewDrawable);

        }
    }
}

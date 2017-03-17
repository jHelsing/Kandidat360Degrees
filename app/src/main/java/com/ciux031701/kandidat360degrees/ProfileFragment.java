package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.ciux031701.kandidat360degrees.communication.Session;
import com.ciux031701.kandidat360degrees.adaptors.ProfileFlowAdapter;
import com.ciux031701.kandidat360degrees.representation.PanoramaProfile;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.panorama.Panorama;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;


/**
 * Created by boking on 2017-02-21. Modified by Jonathan on 2017-03-16.
 */

public class ProfileFragment extends Fragment {
    Bundle args;
    private String username;
    private Toolbar toolbar;
    private ImageButton toolbarMenuButton;
    private DrawerLayout mDrawerLayout;
    private ImageButton viewSwitchButton;
    private MapView mapView;
    private GoogleMap googleMap;
    private View root;
    private ImageButton profileMenuButton;

    private TextView infoWindowText;
    private ImageView infoWindowImage;
    private TextView toolbarTitle;

    private ListView pictureListView;
    private ListAdapter profileFlowAdapter;
    private ArrayList<PanoramaProfile> pictures;
    private PanoramaProfile[] pictureArray;

    private boolean listMode = true;
    private boolean first = true;
    private Bundle instanceState;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_profile, container, false);
        instanceState = savedInstanceState;

        viewSwitchButton = (ImageButton) root.findViewById(R.id.profileSwitchModeButton);
        toolbar = (Toolbar) root.findViewById(R.id.tool_bar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbarTitle = (TextView)root.findViewById(R.id.toolbarTitle);
        toolbarTitle.setText(getText(R.string.profile));
        mDrawerLayout = (DrawerLayout)getActivity().findViewById(R.id.drawer_layout);
        toolbarMenuButton = (ImageButton)root.findViewById(R.id.toolbarMenuButton);
        toolbarMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        //Get for which username this profile is for
        args = getArguments();
        username = args.getString("username");

        setUpProfileInformation(1111111, 1001);
        setUpProfileMenuButton();

        //Get pictures, total likes nbr of friends or whatever we decide to display from db
        pictures = new ArrayList<>();
        loadPicturesFromDB();

        //Converts arraylist to array
        pictureArray = new PanoramaProfile[pictures.size()];
        pictureArray = pictures.toArray(pictureArray);

        viewSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(first) {
                    mapView = (MapView)root.findViewById(R.id.profileMapView);
                    mapView.onCreate(instanceState);
                    mapView.onResume(); // needed to get the map to display immediately
                    mapView.setVisibility(View.GONE);
                    first = false;
                    instanceState = null;
                }
                if(listMode){
                    pictureListView.setVisibility(View.GONE);
                    listMode = false;
                    viewSwitchButton.setImageDrawable(getResources()
                            .getDrawable(R.drawable.enabled_map_view_profile_icon));
                    setUpMap();
                    mapView.setVisibility(View.VISIBLE);
                } else {
                    pictureListView.setVisibility(View.VISIBLE);
                    listMode = true;
                    viewSwitchButton.setImageDrawable(getResources()
                            .getDrawable(R.drawable.disable_map_view_icon_profile));
                    mapView.setVisibility(View.GONE);
                }
            }
        });

        pictureListView = (ListView)root.findViewById(R.id.profilePictureListView);
        profileFlowAdapter = new ProfileFlowAdapter(getActivity(),pictureArray);
        pictureListView.setAdapter(profileFlowAdapter);

        return root;
    }

    //Get info for specific image from DB here.
    //Use the marker as a reference when doing so.
    public View onMarkerClicked(Marker marker){
        View v = getActivity().getLayoutInflater().inflate(R.layout.marker_info_window, null);

        // Getting reference to the TextView to set latitude
        infoWindowText = (TextView) v.findViewById(R.id.infoWindowText);
        infoWindowImage = (ImageView) v.findViewById(R.id.infoWindowImage);

        infoWindowText.setText(marker.getPosition().toString());
        return v;
    }

    //Use this to fill up pictures.
    public void loadPicturesFromDB(){
        //Example of how to add
        //Drawable currentPic = image from database convertet to a Drawable. Uses template picture without third argument
        PanoramaProfile pp = new PanoramaProfile(0,null,false, "2017-02-08", "Gothenburg", 5);
        pictures.add(pp);
        pp = new PanoramaProfile(0,null,false, "2017-02-28", "Stockholm", 0);
        pictures.add(pp);
        pp = new PanoramaProfile(0,null,false, "2017-03-03", "Malmö", 2);
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

    private void setUpProfileInformation(int panoramaCount, int favCount) {
        TextView userNameView = (TextView) root.findViewById(R.id.profileUserNameTextView);
        TextView panoramaCountView = (TextView) root.findViewById(R.id.profilePanoramaCountTextView);
        TextView favCountView = (TextView) root.findViewById(R.id.profileFavCountTextView);
        userNameView.setText(username);

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        String panoramaString = null;
        if (panoramaCount >= 1000 && panoramaCount < 1000000) {
            panoramaString = (panoramaCount/1000.0)+"k";
        } else if (panoramaCount >= 1000000) {
            panoramaString = df.format(panoramaCount/1000000) + "M";
        }
        panoramaCountView.setText(panoramaString);

        String favString = null;
        if (favCount >= 1000 && favCount < 1000000) {
            favString = (favCount/1000.0)+"k";
        } else if (favCount >= 1000000) {
            favString = df.format(favCount/1000000) + "M";
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
                // TODO Add checks to see if person already is friend
                menu.add(R.string.add_friend);
                menu.add(R.string.remove_friend);
                if (username == Session.getUser())
                    menu.add(R.string.acc_settings);
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
                        marker.setRotation(marker.getRotation()+20);
                    }
                });

                try {
                    // Customise the styling of the base map using a JSON object defined
                    // in a raw resource file.
                    boolean success = googleMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    getActivity(), R.raw.style_json));

                    if (!success) {
                        Log.e("explore", "Style parsing failed.");
                    }
                } catch (Resources.NotFoundException e) {
                    Log.e("explore", "Can't find style. Error: ", e);
                }


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
}

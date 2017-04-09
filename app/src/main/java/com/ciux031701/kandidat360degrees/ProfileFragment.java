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
import com.ciux031701.kandidat360degrees.communication.DownloadMultiplePreviewsService;
import com.ciux031701.kandidat360degrees.communication.DownloadService;
import com.ciux031701.kandidat360degrees.communication.FTPInfo;
import com.ciux031701.kandidat360degrees.communication.Friends;
import com.ciux031701.kandidat360degrees.communication.ImageType;
import com.ciux031701.kandidat360degrees.communication.JReqRemoveFriend;
import com.ciux031701.kandidat360degrees.communication.JReqSendFriendrequest;
import com.ciux031701.kandidat360degrees.communication.JRequest;
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

    private boolean listMode;
    private boolean first;
    private Bundle instanceState;

    private boolean isFriend;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_profile, container, false);
        instanceState = savedInstanceState;
        listMode=true;
        first= true;
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
        setUpViewSwitchButton();

        //Get pictures, total likes nbr of friends or whatever we decide to display from db
        // We do not need to check this error since we are the ones who send the bundle and are
        // 100% sure of what it contains.
        pictures = (ArrayList<ProfilePanorama>) getArguments().getSerializable("images");
        pictureListView = (ListView) root.findViewById(R.id.profilePictureListView);

        return root;
    }

    private class FlowItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //TODO: Put this event inside Adapter because we will go to imageViewFragment only when we press on the picture (Not the whole item)
            //selectItem(position);
        }

        private void selectItem(int position) {
        }
    }


    //Get info for specific image from DB here.
    //Use the marker as a reference when doing so.
    private View onMarkerClicked(Marker marker) {
        final View v = getActivity().getLayoutInflater().inflate(R.layout.marker_info_window, null);

        TextView infoWindowText = (TextView) v.findViewById(R.id.infoWindowText);
        ImageView infoWindowImage = (ImageView) v.findViewById(R.id.infoWindowImage);
        int i = 0;
        while(!marker.getTitle().equals(pictures.get(i).getPanoramaID())) {
            i++;
        }

        infoWindowText.setText(pictures.get(i).getDate().substring(0,10));

        infoWindowImage.setImageDrawable(pictures.get(i).getPreview());

        return v;
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
     * Starts to fetch all previews of the user from the server.
     */
    private void loadPreviews() {
        Intent intent =  new Intent(getActivity(), DownloadMultiplePreviewsService.class);
        String[] imageIDs = new String[pictures.size()];
        for (int i=0; i<pictures.size(); i++) {
            imageIDs[i] = pictures.get(i).getPanoramaID();
        }
        intent.putExtra("panoramaArray", imageIDs);
        intent.setAction(DownloadMultiplePreviewsService.NOTIFICATION);
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadMultiplePreviewsService.NOTIFICATION);
        getActivity().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("Profile", "OnReceive");
                if (intent.getIntExtra("result", -100)  == Activity.RESULT_OK) {
                    Log.d("Profile", "Previews found and results from download are OK.");
                    String[] imageIDs = intent.getStringArrayExtra("panoramaArray");

                    for(int i=0; i<imageIDs.length; i++) {
                        String panoramaID = imageIDs[i];
                        File localFile = new File(getActivity().getFilesDir() + FTPInfo.PREVIEW_LOCAL_LOCATION + panoramaID + FTPInfo.FILETYPE);
                        Drawable preview = Drawable.createFromPath(localFile.getPath());
                        pictures.get(i).setPreview(preview);

                        if (localFile.delete())
                            Log.d("FTP", "Preview image has been deleted :" + panoramaID);
                        else
                            Log.d("FTP", "Preview image has not been deleted :" + panoramaID);
                    }

                    profileFlowAdapter = new ProfileFlowAdapter(getActivity(), pictures);
                    pictureListView.setAdapter(profileFlowAdapter);
                    pictureListView.setOnItemClickListener(new FlowItemClickListener());
                }
                getActivity().unregisterReceiver(this);
            }
        }, filter);
        getActivity().startService(intent);
    }

    /**
     * Starts to fetch the profile picture of the user from the server.
     */
    private void loadProfilePicture() {
        Intent intent =  new Intent(getActivity(), DownloadService.class);
        intent.putExtra("IMAGETYPE", ImageType.PROFILE);
        intent.putExtra("USERNAME", username);
        intent.putExtra("TYPE", "DOWNLOAD");
        intent.setAction(DownloadService.NOTIFICATION + username + ".jpg");
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.NOTIFICATION + username + ".jpg");
        getActivity().registerReceiver(new ProfileImageBroadcastReceiver(), filter);
        getActivity().startService(intent);
    }

    private void setUpProfileInformation() {
        TextView userNameView = (TextView) root.findViewById(R.id.profileUserNameTextView);
        TextView panoramaCountView = (TextView) root.findViewById(R.id.profilePanoramaCountTextView);
        TextView favCountView = (TextView) root.findViewById(R.id.profileFavCountTextView);

        // Process information from arguments
        username = getArguments().getString("username");
        userNameView.setText(username);

        int panoramaCount = 0;
        try {
            if (!getArguments().getString("uploadCount").equals(null))
                panoramaCount = Integer.parseInt(getArguments().getString("uploadCount"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        int favCount = 0;
        try {
            if(!getArguments().getString("favsCount").equals(null))
                favCount = Integer.parseInt(getArguments().getString("favsCount"));
        } catch (NumberFormatException e){
            e.printStackTrace();
        }

        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.CEILING);

        String panoramaString = null;
        if (panoramaCount >= 1000 && panoramaCount < 1000000) {
            panoramaString = df.format(panoramaCount / 1000.0) + "k";
        } else if (panoramaCount >= 1000000) {
            panoramaString = df.format(panoramaCount / 1000000) + "M";
        } else {
            panoramaString = panoramaCount + "";
        }
        panoramaCountView.setText(panoramaString);

        String favString = null;
        if (favCount >= 1000 && favCount < 1000000) {
            favString = df.format(favCount / 1000.0) + "k";
        } else if (favCount >= 1000000) {
            favString = df.format(favCount / 1000000) + "M";
        } else {
            favString = favCount + "";
        }
        favCountView.setText(favString);

        try {
            if(!getArguments().getString("isFriend").equals(null))
                isFriend = true;
        } catch (NumberFormatException e){
            e.printStackTrace();
        }
    }

    private void setUpProfileMenuButton() {
        profileMenuButton = (ImageButton) root.findViewById(R.id.profileSettingsButton);
        profileMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu popupMenu = new PopupMenu(getActivity(), profileMenuButton);
                final Menu menu = popupMenu.getMenu();

                if (Session.getUser().equalsIgnoreCase(username)) {
                    menu.add(R.string.upload_profile_picture);
                } else {
                    if(isFriend)
                        menu.add("Remove Friend");
                    else
                        menu.add("Add Friend");
                }
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getTitle().toString()) {
                            case "Add Friend":
                                JReqSendFriendrequest jReqSendFriendRequest = new JReqSendFriendrequest(username);
                                jReqSendFriendRequest.setJResultListener(new JRequest.JResultListener() {
                                    @Override
                                    public void onHasResult(JSONObject result) {
                                        boolean error;
                                        try {
                                            error = result.getBoolean("error");
                                        } catch (JSONException e) {
                                            error = true;
                                        }
                                        if(error) {
                                            Toast.makeText(getActivity(), "Could not reach the server, please try again later.",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                jReqSendFriendRequest.sendRequest();
                                break;
                            case "Remove Friend":
                                JReqRemoveFriend jReqRemoveFriend = new JReqRemoveFriend(username);
                                jReqRemoveFriend.setJResultListener(new JRequest.JResultListener() {
                                    @Override
                                    public void onHasResult(JSONObject result) {
                                        boolean error;
                                        try {
                                            error = result.getBoolean("error");
                                        } catch (JSONException e) {
                                            error = true;
                                        }
                                        if(!error){
                                            Friends.remove(Friends.get(username));
                                        }
                                        else
                                            Toast.makeText(getActivity(), "Could not reach the server, please try again later.",Toast.LENGTH_SHORT).show();
                                    }
                                });
                                jReqRemoveFriend.sendRequest();
                                break;
                            case "Change profile picture":
                                // TODO add support for uploading profile picture to server
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
                        //Go to full screen view
                        //TODO: get fullscreen image from DB and send as argument
                        Bundle args =  new Bundle();
                        args.putString("origin","profile");
                        listMode=false;
                        ImageViewFragment fragment = new ImageViewFragment();
                        fragment.setArguments(args);
                        FragmentManager fragmentManager = getActivity().getFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("view").commit();
                    }
                });

                ((MainActivity) getActivity()).loadMapStyling(googleMap);

                googleMap.getUiSettings().setMapToolbarEnabled(false);
                // For dropping a marker at a point on the Map
                for (int i = 0; i < pictures.size(); i++){
                    double latitude = Double.parseDouble(pictures.get(i).getLatitude());
                    double longitude = Double.parseDouble(pictures.get(i).getLongitude());
                    LatLng position = new LatLng(latitude, longitude);
                    googleMap.addMarker(new MarkerOptions().position(position).title(pictures.get(i).getPanoramaID()).icon(BitmapDescriptorFactory.fromResource(R.drawable.public_image_location_icon)));
                }
                // For zooming automatically to the location of the marker
                //CameraPosition cameraPosition = new CameraPosition.Builder().target(gothenburg).zoom(12).build();
                //googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });
    }

    /**
     * A class for receiving broadcasts from the download of the profile picture
     */
    public class ProfileImageBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra("RESULT", -100)  == Activity.RESULT_OK) {
                Log.d("Profile", "Profile image found and results from download are OK.");
                String path = context.getFilesDir() + "/profiles/"
                        + username + ".jpg";
                Drawable profileImage = Drawable.createFromPath(path);
                ((ImageView) root.findViewById(R.id.profileProfileImage)).setImageDrawable(profileImage);
                File file = new File(path);
                if (file.delete()) {
                    Log.d("Profile", "Profile image has been deleted");
                }
            }
            context.unregisterReceiver(this);
            loadPreviews();
        }
    }

    private void setUpViewSwitchButton() {
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
                    viewSwitchButton.setImageDrawable(getResources().getDrawable(R.drawable.enabled_map_view_profile_icon));
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
    }
}

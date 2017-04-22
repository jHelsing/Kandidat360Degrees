package com.ciux031701.kandidat360degrees;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
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
import com.ciux031701.kandidat360degrees.communication.UploadService;
import com.ciux031701.kandidat360degrees.representation.ProfilePanorama;
import com.ciux031701.kandidat360degrees.representation.ThreeSixtyPanoramaCollection;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    private ProgressBar profileProgressBar;

    private ListView pictureListView;
    private ListAdapter profileFlowAdapter;
    private ThreeSixtyPanoramaCollection pictures;
    private int[] panoramaIDs;

    private static final int GET_FROM_GALLERY = 3;

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

        profileProgressBar = (ProgressBar) root.findViewById(R.id.profileProgressBar);
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
        pictures = getArguments().getParcelable("images");
        pictureListView = (ListView) root.findViewById(R.id.profilePictureListView);

        pictureListView.setVisibility(View.GONE);
        profileProgressBar.setVisibility(View.VISIBLE);

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
        while(!marker.getTitle().equals(pictures.get(i).getImageID())) {
            i++;
        }

        infoWindowText.setText(pictures.get(i).getDate().substring(0,10));

        ProfilePanorama pp = (ProfilePanorama)pictures.get(i);
        infoWindowImage.setImageDrawable(pp.getPreview());

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

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            pictureListView.setVisibility(View.VISIBLE);
            profileProgressBar.setVisibility(View.GONE);
        }
    };

    /**
     * Starts to fetch all previews of the user from the server.
     */
    private void loadPreviews() {
        //start a new thread to process job
        new Thread(new Runnable() {
            @Override
            public void run() {

                Intent intent =  new Intent(getActivity(), DownloadMultiplePreviewsService.class);
                String[] imageIDs = new String[pictures.size()];
                for (int i=0; i<pictures.size(); i++) {
                    imageIDs[i] = pictures.get(i).getImageID();
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
                                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                Bitmap preview = BitmapFactory.decodeFile(localFile.getAbsolutePath(),bmOptions);
                                ProfilePanorama pp = (ProfilePanorama)pictures.get(i);
                                pp.setPreview(preview);

                                if (localFile.delete())
                                    Log.d("FTP", "Preview image has been deleted :" + panoramaID);
                                else
                                    Log.d("FTP", "Preview image has not been deleted :" + panoramaID);
                            }

                            profileFlowAdapter = new ProfileFlowAdapter(getActivity(), pictures, username);
                            pictureListView.setAdapter(profileFlowAdapter);
                            pictureListView.setOnItemClickListener(new FlowItemClickListener());
                            handler.sendEmptyMessage(0);
                        }
                        getActivity().unregisterReceiver(this);
                    }
                }, filter);
                getActivity().startService(intent);

            }
        }).start();

    }

    /**
     * Starts to fetch the profile picture of the user from the server.
     */
    private void loadProfilePicture() {
        Intent intent =  new Intent(getActivity(), DownloadService.class);
        intent.putExtra("IMAGETYPE", ImageType.PROFILE);
        intent.putExtra("USERNAME", username);
        intent.putExtra("TYPE", "DOWNLOAD");
        intent.setAction(DownloadService.NOTIFICATION + username + FTPInfo.FILETYPE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.NOTIFICATION + username + FTPInfo.FILETYPE);
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
            isFriend = getArguments().getBoolean("isFriend");
        } catch (NumberFormatException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Check if we are viewing the list or the map
         if(listMode) {
            pictureListView.setAdapter(null);
            pictureListView.invalidate();
            profileFlowAdapter = null;
         } else {
            // We are viewing the map, therefore clear the map
          googleMap.clear();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check if we are viewing the list or the map
        if(listMode) {
            profileFlowAdapter = new ProfileFlowAdapter(getActivity(), pictures, username);;
            pictureListView.setAdapter(profileFlowAdapter);
            pictureListView.invalidate();
        } else {
            // We are viewing the map, therefore clear the map
            fetchMap();
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
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
                                        if(!error) {
                                            Toast.makeText(getActivity(), "A friend request has been sent to " + username,Toast.LENGTH_SHORT).show();
                                        } else
                                            Toast.makeText(getActivity(), "Could not reach the server, please try again later.",Toast.LENGTH_SHORT).show();
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
                                            Toast.makeText(getActivity(), "Removed " + username + " as your friend",Toast.LENGTH_SHORT).show();
                                            isFriend = false;                                        }
                                        else
                                            Toast.makeText(getActivity(), "Could not reach the server, please try again later.",Toast.LENGTH_SHORT).show();
                                    }
                                });
                                jReqRemoveFriend.sendRequest();
                                break;
                            case "Change profile picture":
                                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
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
                    Marker oldMarker;
                    @Override
                    public View getInfoWindow(final Marker marker) {
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.public_image_location_icon_selected));
                        if(oldMarker != null && !(marker.getTitle().equals(oldMarker.getTitle())))
                            oldMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.public_image_location_icon));
                        oldMarker = marker;

                        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                            @Override
                            public void onMapClick(LatLng latLng) {
                                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.public_image_location_icon));
                            }
                        });

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
                        //TODO: get fullscreen image from DB
                        listMode=false;
                        MainActivity mainActivity = (MainActivity) getActivity();
                        String imageId = marker.getTitle();
                        mainActivity.showPanorama("profile", imageId, username, pictures.get(imageId).getLikeCount() + "");
                    }
                });

                ((MainActivity) getActivity()).loadMapStyling(googleMap);

                googleMap.getUiSettings().setMapToolbarEnabled(false);
                // For dropping a marker at a point on the Map
                for (int i = 0; i < pictures.size(); i++){
                    LatLng position = pictures.get(i).getLocation();
                    googleMap.addMarker(new MarkerOptions().position(position).title(pictures.get(i).getImageID()).icon(BitmapDescriptorFactory.fromResource(R.drawable.public_image_location_icon)));
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
                        + username + FTPInfo.FILETYPE;
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
                    googleMap.clear();
                    profileFlowAdapter = new ProfileFlowAdapter(getActivity(), pictures, username);
                    pictureListView.setAdapter(profileFlowAdapter);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            try {
                final Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                final File file = new File(getActivity().getFilesDir() + FTPInfo.PROFILE_LOCAL_LOCATION + Session.getUser() + FTPInfo.FILETYPE);
                OutputStream outputStream = new FileOutputStream(file.getPath());
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);

                //Start the upload service
                Intent intent =  new Intent(getActivity(), UploadService.class);
                intent.putExtra("IMAGETYPE", ImageType.PROFILE);
                intent.putExtra("FILE", file);
                intent.putExtra("IMAGEID", Session.getUser());
                intent.setAction(UploadService.NOTIFICATION + Session.getUser() + FTPInfo.FILETYPE);
                IntentFilter filter = new IntentFilter();
                filter.addAction(UploadService.NOTIFICATION + Session.getUser() + FTPInfo.FILETYPE);
                getActivity().registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (intent.getIntExtra("RESULT", -100)  == Activity.RESULT_OK) {
                            ((ImageView) root.findViewById(R.id.profileProfileImage)).setImageDrawable(new BitmapDrawable(getResources(), bitmap));
                            if (file.delete()) {
                                Log.d("Profile", "Profile image has been deleted after upload");
                            }
                        } else
                            Toast.makeText(getActivity(), "Could not upload the profile picture, please try again later.",Toast.LENGTH_SHORT).show();
                        getActivity().unregisterReceiver(this);
                    }
                }, filter);
                getActivity().startService(intent);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

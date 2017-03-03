package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ciux031701.kandidat360degrees.adaptors.FlowPicture;
import com.ciux031701.kandidat360degrees.adaptors.ProfileFlowAdapter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


/**
 * Created by boking on 2017-02-21.
 */

public class ProfileFragment extends Fragment {
    TextView textView;
    Bundle args;
    String username;
    private Toolbar toolbar;
    private ImageButton toolbarMenuButton;
    private DrawerLayout mDrawerLayout;
    private ImageView mapViewImage;
    private MapView mapView;
    private GoogleMap googleMap;

    private TextView infoWindowText;
    private ImageView infoWindowImage;

    ListView pictureListView;
    ListAdapter profileFlowAdapter;
    ArrayList<FlowPicture> pictures;
    FlowPicture[] pictureArray;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        mapViewImage = (ImageView) root.findViewById(R.id.mapViewImage);
        mapView = (MapView)root.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume(); // needed to get the map to display immediately
        mapView.setVisibility(View.GONE);
        toolbar = (Toolbar) root.findViewById(R.id.tool_bar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

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

        //Get pictures, total likes nbr of friends or whatever we decide to display from db
        pictures = new ArrayList<FlowPicture>();
        loadPicturesFromDB();

        //Converts arraylist to array
        pictureArray = new FlowPicture[pictures.size()];
        pictureArray = pictures.toArray(pictureArray);

        mapViewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pictureListView.getVisibility()==View.VISIBLE){
                    pictureListView.setVisibility(View.GONE);
                    mapView.setVisibility(View.VISIBLE);
                } else {
                    pictureListView.setVisibility(View.VISIBLE);
                    mapView.setVisibility(View.GONE);
                }
            }
        });
        pictureListView = (ListView)root.findViewById(R.id.pictureListView);
        profileFlowAdapter = new ProfileFlowAdapter(getActivity(),pictureArray);
        pictureListView.setAdapter(profileFlowAdapter);

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                googleMap.addMarker(new MarkerOptions().position(gothenburg).title("Here we go bois").snippet("its happening!").icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                // For zooming automatically to the location of the marker
                //CameraPosition cameraPosition = new CameraPosition.Builder().target(gothenburg).zoom(12).build();
                //googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        return root;
    }

    //Get info for specific image from DB here.
    //Use the marker as a reference when doing so.
    public View onMarkerClicked(Marker marker){
        View v = getActivity().getLayoutInflater().inflate(R.layout.marker_info_window, null);

        // Getting reference to the TextView to set latitude
        infoWindowText = (TextView) v.findViewById(R.id.infoWindowText);
        infoWindowImage = (ImageView) v.findViewById(R.id.infoWindowImage);

        infoWindowText.setText("2017-03-03");
        return v;
    }

    //Use this to fill upp pictures.
    public void loadPicturesFromDB(){
        //Example of how to add
        //Drawable currentPic = image from database convertet to a Drawable. Uses template picture without third argument
        pictures.add(new FlowPicture("Gothenburg","2017-02-08",""));
        pictures.add(new FlowPicture("Stockholm","2017-02-28",""));
        pictures.add(new FlowPicture("Malmö","2017-03-03",""));
    }
}

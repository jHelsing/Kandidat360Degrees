package com.ciux031701.kandidat360degrees;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ciux031701.kandidat360degrees.adaptors.ExploreSearchAdapter;
import com.ciux031701.kandidat360degrees.adaptors.ProfileFlowAdapter;
import com.ciux031701.kandidat360degrees.communication.DownloadMultiplePreviewsService;
import com.ciux031701.kandidat360degrees.communication.DownloadService;
import com.ciux031701.kandidat360degrees.communication.FTPInfo;
import com.ciux031701.kandidat360degrees.communication.ImageType;
import com.ciux031701.kandidat360degrees.communication.JReqImages;
import com.ciux031701.kandidat360degrees.communication.JRequest;
import com.ciux031701.kandidat360degrees.communication.Session;
import com.ciux031701.kandidat360degrees.representation.ExplorePanorama;
import com.ciux031701.kandidat360degrees.representation.JSONParser;
import com.ciux031701.kandidat360degrees.representation.ProfilePanorama;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by boking on 2017-02-14. Revisited by Jonathan on 2017-03-22
 */

public class ExploreFragment extends Fragment implements SearchView.OnQueryTextListener {

    private Geocoder geocoder;
    private MapView mMapView;
    private GoogleMap googleMap;

    private DrawerLayout mDrawerLayout;
    private SearchView searchView;
    private ListView searchListView;

    private MenuItem earthButton;

    private Menu toolbarMenu;

    private boolean isShowingPublic;

    private ClusterManager<MyItem> mClusterManager;

    private ExploreSearchAdapter exploreSearchAdapter;
    private ArrayList<String> resultArrayList;
    private List<Address> globalList;
    private ArrayList<ExplorePanorama> imagesToShow;
    private int lastSearchStringLength;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_explore, container, false);
        setHasOptionsMenu(true);
        lastSearchStringLength = 0;

        // Set ups
        isShowingPublic = true;
        setUpNavigationAndToolBar(root);
        setUpSearchFunctionality(root);
        setUpCamera(root);
        setUpMap(root, savedInstanceState);

        JReqImages request = new JReqImages(Session.getId());
        request.setJResultListener(new JRequest.JResultListener() {
            @Override
            public void onHasResult(JSONObject result) {
                Log.d("Explore", result.toString());
                imagesToShow = new ArrayList<ExplorePanorama>();
                try {
                    JSONArray resultArray = result.getJSONArray("images");
                    if (resultArray.length() != 0) {
                        for(int i=0; i<resultArray.length(); i++) {
                            imagesToShow.add(JSONParser.parseToExplorePanorama(resultArray.getJSONArray(i)));
                            // TODO update different stuff, check if friend, update visibility of panorama based on that
                        }
                        showImagesOnMap();

                        // Fetch previews to local storage
                        fetchPreviews();
                    }
                } catch(JSONException e) {
                    e.printStackTrace();
                }
            }

        });
        request.sendRequest();

        return root;
    }

    public View onMarkerClicked(final Marker marker) {
        if (marker.getTitle().equalsIgnoreCase("Your position")) {
            return null;
        }
        View v = getActivity().getLayoutInflater().inflate(R.layout.marker_info_explore_view, null);

        // Getting references to the different views in the marker window
        TextView dateView = (TextView) v.findViewById(R.id.exploreInfoViewDateView);
        TextView userView = (TextView) v.findViewById(R.id.exploreInfoViewUserNameView);
        ImageView previewView = (ImageView) v.findViewById(R.id.exploreInfoViewPreviewView);

        // Find the correct ExplorePanorama for the marker
        final String imageID = marker.getTitle();
        ExplorePanorama markerPanorama = null;
        for (int i=0; i<imagesToShow.size(); i++) {
            if(imagesToShow.get(i).getImageID().equals(imageID)) {
                markerPanorama = imagesToShow.get(i);
                i = imagesToShow.size();
            }
        }

        dateView.setText(markerPanorama.getDate());
        userView.setText(markerPanorama.getUploader());

        File localFile = new File(getActivity().getFilesDir() + FTPInfo.PREVIEW_LOCAL_LOCATION + markerPanorama.getImageID() + FTPInfo.FILETYPE);
        Drawable preview = Drawable.createFromPath(localFile.getPath());
        previewView.setImageDrawable(preview);

        return v;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolmenu_search, menu);
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
                exploreSearchAdapter.clear();
                searchListView.setVisibility(View.GONE);
                return true;
            }
        });
        earthButton = menu.findItem(R.id.togglePermission);
        searchView.setQueryHint("Search!");
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
        ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setTextColor(Color.BLACK);
        ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setHintTextColor(Color.LTGRAY);
        searchView.setBackgroundColor(Color.WHITE);
        searchView.setOnQueryTextListener(this);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.togglePermission:
                if (isShowingPublic) {
                    toolbarMenu.getItem(0).setIcon(R.drawable.temp_earthblack);
                    // TODO Reload markers for the private map
                    isShowingPublic = false;

                } else {
                    toolbarMenu.getItem(0).setIcon(R.drawable.temp_earthwhite);
                    // TODO Reload markers for the public map
                    isShowingPublic = true;
                }
                return true;
            case R.id.action_search:
                if (searchListView.getVisibility() == View.GONE) {
                    searchListView.setVisibility(View.VISIBLE);
                } else {
                    searchListView.setVisibility(View.GONE);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void performSearch(String query) throws IOException {
        geocoder = new Geocoder(getActivity());
        List<Address> list = geocoder.getFromLocationName(query, 1);
        if (!(list.size() == 0)) {
            Address address = list.get(0);
            double lat = address.getLatitude();
            double lng = address.getLongitude();
            LatLng latlng = new LatLng(lat, lng);
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latlng, 10);
            googleMap.moveCamera(update);
        } else {
            Toast.makeText(getActivity(), "Could not find " + query + ".", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        try {
            searchListView.setVisibility(View.GONE);
            View currentFocus = getActivity().getCurrentFocus();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            performSearch(query);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        if (searchListView.getVisibility() == View.GONE) {
            searchListView.setVisibility(View.VISIBLE);
        }
        //Only search for new results if we add characters
        //Should probably be done in another thread.
        if (newText.length() > lastSearchStringLength) {
            lastSearchStringLength = newText.length();
            //searchListView.invalidate();
            geocoder = new Geocoder(getActivity());
            try {
                globalList = geocoder.getFromLocationName(newText, 5);
            } catch (IOException e) {
                e.printStackTrace();
            }
            exploreSearchAdapter.clear();
            for (Address address : globalList) {

                System.out.println("Adding result: " + address.getLocality() + address.getFeatureName() + address.getAdminArea());
                if (address.getLocality() != null) {
                    resultArrayList.add(address.getLocality() + ", " + address.getCountryName());
                }
                if (address.getAdminArea() != null) {
                    resultArrayList.add(address.getAdminArea() + ", " + address.getCountryName());
                }
                if (address.getFeatureName() != null) {
                    resultArrayList.add(address.getFeatureName() + ", " + address.getCountryName());
                }
            }

            exploreSearchAdapter.notifyDataSetChanged();

            return true;
        } else {
            lastSearchStringLength = newText.length();
        }
        return false;
    }

    private void setUpClusterer() {
        // Position the map.

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<MyItem>(getActivity(), googleMap);
        mClusterManager.setRenderer(new CustomMarkerRenderer(getActivity(), googleMap, mClusterManager));
        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        googleMap.setOnCameraIdleListener(mClusterManager);
        googleMap.setOnMarkerClickListener(mClusterManager);
    }


    private void setUpMap(View root, Bundle bundle) {
        mMapView = (MapView) root.findViewById(R.id.mapView);
        mMapView.onCreate(bundle);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {

                googleMap = mMap;
                setUpClusterer();
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

                ((MainActivity) getActivity()).loadMapStyling(googleMap);

                googleMap.getUiSettings().setMapToolbarEnabled(false);

                // Set default position
                LatLng gothenburg = new LatLng(57.688350, 11.979428);
                MarkerOptions position = new MarkerOptions();
                position.position(gothenburg);
                position.title("Your position");
                position.snippet("Default position");
                position.icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                googleMap.addMarker(position);

                // Zoom automatically to the default position
                CameraPosition cameraPosition = new CameraPosition.Builder().target(gothenburg).zoom(10).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });
    }

    private void setUpSearchFunctionality(View root) {
        resultArrayList = new ArrayList<>();
        searchListView = (ListView) root.findViewById(R.id.searchListView);
        exploreSearchAdapter = new ExploreSearchAdapter(resultArrayList, getActivity().getApplicationContext());
        searchListView.setAdapter(exploreSearchAdapter);
        searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    searchListView.setVisibility(View.GONE);
                    performSearch((String)exploreSearchAdapter.getItemWithoutCountry(i));
                    //closes virtual keyboard
                    View currentFocus = getActivity().getCurrentFocus();
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setUpNavigationAndToolBar(View root) {
        // Set up toolbar and navigation drawer.
        mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        Toolbar toolbar = (Toolbar) root.findViewById(R.id.tool_bar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ImageButton toolbarMenuButton = (ImageButton) root.findViewById(R.id.toolbarMenuButton);
        toolbarMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });
    }

    private void setUpCamera(View root) {
        // Set up camera button
        ImageButton cameraButton = (ImageButton) root.findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                CameraFragment fragment = new CameraFragment();
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
            }
        });
    }

    private void showImagesOnMap() {
        for (int i=0; i<imagesToShow.size(); i++) {
            ExplorePanorama ep = imagesToShow.get(i);
            MyItem newImageToShow = new MyItem(ep.getLocation().latitude, ep.getLocation().longitude);
            newImageToShow.setTitle(ep.getImageID());
            newImageToShow.setEp(ep);
            mClusterManager.addItem(newImageToShow);
        }
    }

    private void fetchPreviews() {
        // Fetch all the image IDs that are to be downloaded
        String[] imageIDs = new String[imagesToShow.size()];
        for(int i=0; i<imagesToShow.size(); i++) {
            imageIDs[i] = imagesToShow.get(i).getImageID();
        }

        // Start the service that downloads the previews
        Intent intent =  new Intent(getActivity(), DownloadMultiplePreviewsService.class);
        intent.putExtra("panoramaArray", imageIDs);
        intent.setAction(DownloadMultiplePreviewsService.NOTIFICATION);
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadMultiplePreviewsService.NOTIFICATION);
        getActivity().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.d("Explore", "Recieving notification from downloaded previews");
                if (intent.getIntExtra("result", -100)  == Activity.RESULT_OK) {
                    Log.d("Explore", "Results OK! Unregestring reciever");
                }

            }
        }, filter);
        getActivity().startService(intent);


    }

    public class CustomMarkerRenderer extends DefaultClusterRenderer<MyItem>{

        public CustomMarkerRenderer(Context context, GoogleMap map,
                                    ClusterManager<MyItem> clusterManager) {
            super(context, map, clusterManager);
        }


        @Override
        protected void onBeforeClusterItemRendered(MyItem item,
                                                   MarkerOptions markerOptions) {
            // TODO change which bitmap to display
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.public_image_location_icon));
        }
    }
}


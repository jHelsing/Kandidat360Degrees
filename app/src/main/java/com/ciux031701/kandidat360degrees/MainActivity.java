package com.ciux031701.kandidat360degrees;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ciux031701.kandidat360degrees.communication.DownloadService;
import com.ciux031701.kandidat360degrees.communication.Friends;
import com.ciux031701.kandidat360degrees.communication.ImageType;
import com.ciux031701.kandidat360degrees.communication.JReqDestroySession;
import com.ciux031701.kandidat360degrees.communication.JReqProfile;
import com.ciux031701.kandidat360degrees.communication.JRequest;
import com.ciux031701.kandidat360degrees.adaptors.DrawerAdapter;
import com.ciux031701.kandidat360degrees.representation.JSONParser;
import com.ciux031701.kandidat360degrees.representation.ProfilePanorama;
import com.ciux031701.kandidat360degrees.representation.RoundImageView;
import com.ciux031701.kandidat360degrees.representation.ThreeSixtyPanoramaCollection;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.ciux031701.kandidat360degrees.communication.Session;
import com.google.android.gms.maps.model.MapStyleOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import static android.content.ContentValues.TAG;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

/**
 * Created by boking on 2017-02-14. Revised by Jonathan 2017-03-22. Modified by Amar on 2017-03-31.
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

    private static final int PICK_IMAGE = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        //Retrieves username parameter from login
        b = intent.getExtras();
        String userName = ""; // or other values
        if (b != null)
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

        mDrawerList.setAdapter(new DrawerAdapter(this, getApplicationContext(), mListOptions));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        showExploreView();

        //Handles when the app is started with an image
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_VIEW.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                try {
                    handleViewImage(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void handleViewImage(Intent intent) throws IOException {
        Uri imageUri = intent.getData();
        if (imageUri != null) {
            showPanoramaBitmap("upload",MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri));
        }
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

            switch (position) {
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

    public void showExploreView() {
        ExploreFragment fragment = new ExploreFragment();
        FragmentManager fragmentManager = getFragmentManager();
        Bundle bundle = new Bundle();
        fragment.setArguments(setArgs);
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("explore").commit();
        setTitle("Explore");
    }

    private void destroySession(){
        Session.delete();
        JReqDestroySession destroySession = new JReqDestroySession();
        destroySession.sendRequest();
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
        JReqProfile profileReq = new JReqProfile(username, Session.getId());
        profileReq.setJResultListener(
                new JRequest.JResultListener(){
                    @Override
                    public void onHasResult(JSONObject result) {
                        boolean error;
                        String message = null, username = null, uploaded = null, views = null, favs = null, isFriendString = null, isPendingReqString = null;
                        JSONArray images = new JSONArray();
                        try {
                            error = result.getBoolean("error");
                            message = result.getString("message");
                            username = result.getString("user");
                            uploaded = result.getString("uploaded");
                            views = result.getString("views");
                            favs = result.getString("likes");
                            isFriendString = result.getString("isFriend");
                            isPendingReqString = result.getString("isPendingReq");
                            images = result.getJSONArray("images");
                            Log.d("Hejaa", result.toString());
                        } catch(JSONException je){
                            error = true;
                        }

                        if(!error) {
                            boolean isPendingReq = false;
                            boolean isFriend = false;
                            if(isFriendString.equals("true"))
                                isFriend = true;
                            if(isPendingReqString.equals("true"))
                                isPendingReq = true;
                            ThreeSixtyPanoramaCollection imgs = new ThreeSixtyPanoramaCollection();
                            for (int i=0; i < images.length(); i++){
                                try {
                                    JSONArray imgArr = images.getJSONArray(i);
                                    Log.d("PROFILE", imgArr.toString());
                                    ProfilePanorama pp = JSONParser.parseToProfilePanorama(imgArr);
                                    if (pp != null && (pp.isPublic() || isFriend || username.equals(Session.getUser())))
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
                            b.putBoolean("isFriend", isFriend);
                            b.putBoolean("isPendingReq", isPendingReq);
                            b.putParcelable("images", imgs);
                            fragment.setArguments(b);
                            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("profile").commit();
                        } else {
                            Toast.makeText(getApplicationContext(), "Could not reach the server, please try again later.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        profileReq.sendRequest();
    }


    public void showNotificationView() {
        NotificationFragment fragment = new NotificationFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("notifications").commit();
        setTitle("Notifications");
    }

    public void showFriendsView() {
        Friends.fetch();
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
        //open gallery
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery,PICK_IMAGE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent content){
        super.onActivityResult(requestCode,resultCode,content);

        if(resultCode==RESULT_OK && requestCode== PICK_IMAGE){
            Uri imageUri = content.getData();
            Class fragmentClass = UploadFragment.class;
            try {
                Fragment fragment = (Fragment) fragmentClass.newInstance();
                Bundle bundle = new Bundle();
                bundle.putParcelable("image", imageUri);
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void showSettingsView() {
        Toast.makeText(this, "Settings not implemented",Toast.LENGTH_LONG).show();
    }


    /** Create a File for saving an image */
    private static File getOutputMediaFile(int type){

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "360World");

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("360World", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new Date().toString();
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        }else{return null;}

        return mediaFile;
    }

    public void downloadPanoramaLocal(Bitmap image){
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(ThreeSixtyWorld.COMPRESS_FORMAT, ThreeSixtyWorld.COMPRESSION_QUALITY, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    public void showPanorama (final String origin, final String imageID, final String username, final String likes) {

        //start a new thread to process job
        new Thread(new Runnable() {
            @Override
            public void run() {

                Intent intent =  new Intent(getBaseContext(), DownloadService.class);
                intent.putExtra("IMAGETYPE", ImageType.PANORAMA);
                intent.putExtra("IMAGEID", imageID);
                intent.setAction(DownloadService.NOTIFICATION + imageID + ".jpg");
                IntentFilter filter = new IntentFilter();
                filter.addAction(DownloadService.NOTIFICATION + imageID + ".jpg");
                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (intent.getIntExtra("RESULT", -100)  == Activity.RESULT_OK) {
                            Log.d("MainActivity", "Panorama image found and results from download are OK.");

                            context.unregisterReceiver(this);

                            Bundle args = new Bundle();
                            args.putString("origin", origin);
                            args.putString("imageid", imageID);
                            args.putString("username", username);
                            args.putString("likes", likes);
                            ImageViewFragment fragment = new ImageViewFragment();
                            fragment.setArguments(args);
                            FragmentManager fragmentManager = getFragmentManager();
                            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(origin).commit();

                        }
                    }
                }, filter);
                startService(intent);
            }
        }).start();


    }

    public void showPanoramaBitmap (final String origin, final Bitmap bitmapImage) {
        Bundle args = new Bundle();
        args.putString("origin", origin);
        args.putParcelable("image", bitmapImage);
        ImageViewFragment fragment = new ImageViewFragment();
        fragment.setArguments(args);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(origin).commit();
    }

}

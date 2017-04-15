package com.ciux031701.kandidat360degrees.adaptors;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ciux031701.kandidat360degrees.MainActivity;
import com.ciux031701.kandidat360degrees.R;
import com.ciux031701.kandidat360degrees.communication.JReqLikeImage;
import com.ciux031701.kandidat360degrees.communication.JReqUnLikeImage;
import com.ciux031701.kandidat360degrees.communication.JRequest;
import com.ciux031701.kandidat360degrees.representation.ProfilePanorama;
import com.ciux031701.kandidat360degrees.representation.ThreeSixtyPanorama;
import com.ciux031701.kandidat360degrees.representation.ThreeSixtyPanoramaCollection;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by boking on 2017-02-21.
 */

public class ProfileFlowAdapter extends ArrayAdapter<ThreeSixtyPanorama> {
    private String username;
    public ProfileFlowAdapter(Context context, ThreeSixtyPanoramaCollection pictures, String username) {
        super(context, R.layout.picture_profile_layout,pictures.getArrayList());
        this.username = username;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent){

        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View customView = inflater.inflate(R.layout.picture_profile_layout,parent,false);

        ImageView imageView = (ImageView) customView.findViewById(R.id.panoramaPreview);
        final TextView locationText = (TextView) customView.findViewById(R.id.locationText);
        final TextView favCountText = (TextView) customView.findViewById(R.id.favCounter);
        TextView dateText = (TextView) customView.findViewById(R.id.dateText);

        //Show preview
        ProfilePanorama pp = (ProfilePanorama)getItem(position);
        imageView.setImageDrawable(pp.getPreview());

        //Show adress for the item
        final Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        final LatLng location = getItem(position).getLocation();

        /* We have to create a Asyntask when we want to access something from geocoder, especially
            when we do it several times to make it faster without affecting the UI so that it is slow.
         */
        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                List<Address> address = null;
                try {
                    address = geocoder.getFromLocation(location.latitude, location.longitude, 5);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return address;
            }

            @Override
            protected void onPostExecute(Object result) {
                List<Address> res = (List<Address>) result;
                String city = "Unknown City";
                String country = "";

                // Check if geocoder has a match with the first coordinates given.
                if(res.size() > 0) {
                    // Radius of the circle that we are going to scan for cities.
                    final double radius = 0.02;
                    // Angle to scan for cities, which will go from 0 degrees to 360 degrees.
                    int angle = 0;
                    boolean isCity= false;
                    boolean isLocation = false;
                    /**
                     * This loop will check if it finds a city between 0 degrees to 360 degrees. It will
                     * only stop if a city is found or if it has gone 360 degrees (1 rev).
                     */
                    while (!isLocation || (angle < 360 && !isCity)) {
                        for(int i = 0; i < res.size(); i++){
                            if(res.get(i).getLocality() != null) {
                                city = res.get(i).getLocality();
                                country = res.get(i).getCountryName();
                                isCity = true;
                                break;
                            }
                        }
                        // Check if we have found a city for optimization.
                        if(isCity)
                            break;
                        try {
                            // We look for other coordinates near the real position with the help of circle's equation.
                            res = geocoder.getFromLocation(location.latitude + radius * Math.sin(angle)
                                    , location.longitude + radius * Math.cos(angle), 5);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // Check if geocoder has a match with the new coordinates.
                        if(res.size() > 0)
                            isLocation = true;
                        // We look new coordinates for every 45 angle in the circle till we reach 1 rev.
                        angle += 45;
                    }

                    /* If the angle is above or equal to 360 degrees, it means that we have gone 1 rev
                       and there is no city at all in the search. Thats why we want to set AdminArea rather than
                       nothing (if there is a AdminArea), otherwise "Unknown City".
                     */
                    if(angle >= 360){
                        country = res.get(0).getCountryName();
                        if(res.get(0).getAdminArea() != null)
                            city = res.get(0).getAdminArea();
                    }
                    locationText.setText(city + ", " + country);
                } else
                    locationText.setText("Unknown Location");
            }
        };
        //Start the asyncTask
        asyncTask.execute();

        //Show date for the item
        dateText.setText(getItem(position).getDate().substring(0,10));

        //Show favstext for the item
        setfavCountText(getItem(position).getLikeCount(), favCountText);

        //show if liked for the item
        if(pp.isFavorite()){
            Drawable fav = (Drawable) customView.getResources().getDrawable(R.drawable.ic_favorite_clicked);
            favCountText.setCompoundDrawablesWithIntrinsicBounds(null, null, fav, null);
        }

        favCountText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() > (favCountText.getRight()
                            - (favCountText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width()
                                - Math.round(16 * (getContext().getResources().getDisplayMetrics()
                                    .xdpi / DisplayMetrics.DENSITY_DEFAULT))))) {

                        /**
                         * Makes the logged in user to like or unlike a specific image. It returns true if the image
                         * managed to be liked or unliked.
                         */
                        final ProfilePanorama pp = (ProfilePanorama)getItem(position);
                        if(!pp.isFavorite()){
                            JReqLikeImage likeImageReq = new JReqLikeImage(getItem(position).getImageID());
                            likeImageReq.setJResultListener(new JRequest.JResultListener() {
                                @Override
                                public void onHasResult(JSONObject result) {
                                    boolean error;
                                    try {
                                        error = result.getBoolean("error");
                                    } catch (JSONException e) {
                                        error = true;
                                    }
                                    if(!error){
                                        Drawable fav = (Drawable) customView.getResources().getDrawable(R.drawable.ic_favorite_clicked);
                                        favCountText.setCompoundDrawablesWithIntrinsicBounds(null, null, fav, null);
                                        pp.setFavorite(true);
                                        getItem(position).incLikeCount();
                                        setfavCountText(getItem(position).getLikeCount(), favCountText);
                                    } else
                                        Toast.makeText(getContext(), "Something went wrong with the server, try again later.", Toast.LENGTH_SHORT).show();
                                }
                            });
                            likeImageReq.sendRequest();
                        } else {
                            JReqUnLikeImage unLikeImageReq = new JReqUnLikeImage(getItem(position).getImageID());
                            unLikeImageReq.setJResultListener(new JRequest.JResultListener() {
                                @Override
                                public void onHasResult(JSONObject result) {
                                    boolean error;
                                    try {
                                        error = result.getBoolean("error");
                                    } catch (JSONException e) {
                                        error = true;
                                    }
                                    if(!error){
                                        Drawable fav = (Drawable) customView.getResources().getDrawable(R.drawable.ic_favorite_no_click);
                                        favCountText.setCompoundDrawablesWithIntrinsicBounds(null, null, fav, null);
                                        pp.setFavorite(false);
                                        pp.decLikeCount();
                                        setfavCountText(pp.getLikeCount(), favCountText);
                                    } else
                                        Toast.makeText(getContext(), "Something went wrong with the server, try again later.", Toast.LENGTH_SHORT).show();
                                }
                            });
                            unLikeImageReq.sendRequest();
                        }
                        return true;
                    }
                }
                return true;
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Get the real size image for the selected panorama id
                //TODO: like below from the DB and add that as parameter to the imageviewfragment
                ProfilePanorama selectedPanorama = (ProfilePanorama)getItem(position);
                String panoramaID = selectedPanorama.getImageID();

                MainActivity mainActivity = (MainActivity) getContext();
                mainActivity.showPanorama("profile", panoramaID, username, selectedPanorama.getLikeCount() + "");
            }
        });

        //Set image
        return customView;
    }

    public void setfavCountText(int favCount, TextView favCountText) {

        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.CEILING);

        String favString = null;
        if (favCount >= 1000 && favCount < 1000000) {
            favString = df.format(favCount / 1000.0) + "k";
            if(favString.contains("."))
                favString = favString.substring(0,favString.indexOf('.')) + "k";
        } else if (favCount >= 1000000) {
            favString = df.format(favCount / 1000000) + "M";
            if(favString.contains("."))
                favString = favString.substring(0,favString.indexOf('.')) + "M";
        } else{
            favString = favCount + "";
        }
        favCountText.setText(favString);
    }

}

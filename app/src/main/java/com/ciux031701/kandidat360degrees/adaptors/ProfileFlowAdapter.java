package com.ciux031701.kandidat360degrees.adaptors;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.util.DisplayMetrics;
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

public class ProfileFlowAdapter extends ArrayAdapter<ProfilePanorama> {
    private String username;
    public ProfileFlowAdapter(Context context, ArrayList<ProfilePanorama> pictures, String username) {
        super(context, R.layout.picture_profile_layout,pictures);
        this.username = username;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent){

        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View customView = inflater.inflate(R.layout.picture_profile_layout,parent,false);

        ImageView imageView = (ImageView) customView.findViewById(R.id.panoramaPreview);
        TextView locationText = (TextView) customView.findViewById(R.id.locationText);
        final TextView favCountText = (TextView) customView.findViewById(R.id.favCounter);
        TextView dateText = (TextView) customView.findViewById(R.id.dateText);

        //Show preview
        imageView.setImageDrawable(getItem(position).getPreview());

        //Show adress for the item
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        LatLng location = getItem(position).getLocation();
        try {
            List<Address> address = geocoder.getFromLocation(location.latitude, location.longitude, 1);
            String city;
            String country;
            if(address.size() != 0){
                if(address.get(0).getLocality() != null) {
                    city = address.get(0).getLocality();
                } else {
                    city = "Unknown City";
                }
                country = address.get(0).getCountryName();
                locationText.setText(city + ", " + country);
            } else {
                locationText.setText("Unknown Location");
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        //Show date for the item
        dateText.setText(getItem(position).getDate().substring(0,10));

        //Show favstext for the item
        setfavCountText(getItem(position).getLikeCount(), favCountText);

        //show if liked for the item
        if(getItem(position).isFavorite()){
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

                        if(!getItem(position).isFavorite()){
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
                                        getItem(position).setFavorite(true);
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
                                        getItem(position).setFavorite(false);
                                        getItem(position).decLikeCount();
                                        setfavCountText(getItem(position).getLikeCount(), favCountText);
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
                ProfilePanorama selectedPanorama = getItem(position);
                String panoramaID = selectedPanorama.getImageID();

                MainActivity mainActivity = (MainActivity) getContext();
                mainActivity.showPanorama("profile", panoramaID, username, selectedPanorama.getFavCount() + "");
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

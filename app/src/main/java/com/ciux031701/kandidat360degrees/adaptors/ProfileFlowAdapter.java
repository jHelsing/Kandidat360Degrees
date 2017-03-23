package com.ciux031701.kandidat360degrees.adaptors;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ciux031701.kandidat360degrees.ProfileFragment;
import com.ciux031701.kandidat360degrees.R;
import com.ciux031701.kandidat360degrees.communication.DownloadService;
import com.ciux031701.kandidat360degrees.communication.ImageType;
import com.ciux031701.kandidat360degrees.representation.ProfilePanorama;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by boking on 2017-02-21.
 */

public class ProfileFlowAdapter extends ArrayAdapter<ProfilePanorama> implements AdapterView.OnItemClickListener {
    public ProfileFlowAdapter(Context context, ArrayList<ProfilePanorama> pictures) {
        super(context, R.layout.picture_profile_layout,pictures);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent){

        ProfilePanorama singlePic = getItem(position);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View customView = inflater.inflate(R.layout.picture_profile_layout,parent,false);

        ImageView imageView = (ImageView) customView.findViewById(R.id.panoramaPreview);
        TextView locationText = (TextView) customView.findViewById(R.id.locationText);
        TextView favCountText = (TextView) customView.findViewById(R.id.favCounter);
        TextView dateText = (TextView) customView.findViewById(R.id.dateText);

        //Start fetching a preview for the item
        Intent intent =  new Intent(getContext(), DownloadService.class);
        intent.putExtra("IMAGETYPE", ImageType.PREVIEW);
        intent.putExtra("IMAGEID", singlePic.getPanoramaID());
        intent.putExtra("TYPE", "DOWNLOAD");
        intent.setAction(DownloadService.NOTIFICATION + singlePic.getPanoramaID() + ".jpg");
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.NOTIFICATION + singlePic.getPanoramaID() + ".jpg");
        getContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getIntExtra("RESULT", -100)  == Activity.RESULT_OK) {
                    Log.d("Profile", "Preview (" + intent.getStringExtra("IMAGEID") + ") found and results from download are OK.");
                    String imageID = intent.getStringExtra("IMAGEID");
                    String path = context.getFilesDir() + "/previews/"
                            + imageID;
                    Drawable previewDrawable = Drawable.createFromPath(path);

                    context.unregisterReceiver(this);

                    // Add the image to the correct panorama in the arraylist

                    int i=0;
                    Log.d("Bilder", getItem(i).getPanoramaID() + "");
                    Log.d("Bilder", imageID + "");
                    while (i < getCount() && !(getItem(i).getPanoramaID() + ".jpg").equals(imageID))
                        i++;
                    if(i < getCount()) {
                        getItem(i).setPreview(previewDrawable);
                        ImageView imageView = (ImageView) customView.findViewById(R.id.panoramaPreview);
                        imageView.setImageDrawable(getItem(position).getPreview());
                    }
                }
            }
        }, filter);
        getContext().startService(intent);

        //Show adress for the item
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        double latitude = Double.parseDouble(singlePic.getLatitude());
        double longitude = Double.parseDouble(singlePic.getLongitude());
        try {
            List<Address> address = geocoder.getFromLocation(latitude, longitude, 1);
            String city;
            String country;
            if(address.size() != 0){
                city = address.get(0).getLocality();
                country = address.get(0).getCountryName();
                locationText.setText(city + ", " + country);
            } else {
                locationText.setText("Unknown Location");
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        //Show date for the item
        dateText.setText(singlePic.getDate().substring(0,10));

        //Show favstext for the item
        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.CEILING);

        int favCount = singlePic.getFavCount();
        String favString = null;
        if (favCount >= 1000 && favCount < 1000000) {
            favString = df.format(favCount / 1000.0) + "k";
        } else if (favCount >= 1000000) {
            favString = df.format(favCount / 1000000) + "M";
        } else{
            favString = favCount + "";
        }
        favCountText.setText(favString);

        //show if liked for the item
        if(singlePic.isFavorite()){
            Drawable fav = (Drawable) customView.getResources().getDrawable(R.drawable.ic_favorite_clicked);
            favCountText.setCompoundDrawablesWithIntrinsicBounds(null, null, fav, null);
        }

        //Set image
        return customView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("Clicked", id + "");
    }

}

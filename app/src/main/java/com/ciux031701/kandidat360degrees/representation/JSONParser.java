package com.ciux031701.kandidat360degrees.representation;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.ciux031701.kandidat360degrees.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Calendar;

/**
 * Created by Jonathan on 2017-03-20.
 */

public class JSONParser {

    public static ProfilePanorama parseToProfilePanorama(JSONArray imageArray) {
        if (imageArray.length() == 0) {
            return null;
        }
        String imageID = "";
        boolean publicImage = false;
        String uploadDate = "";
        String longitude = "";
        String latitude = "";
        String description = "";
        int viewCount = -1;
        int favCount = -1;
        boolean favorite = false;
        try {
            imageID = imageArray.get(0).toString();
            int publicInt = Integer.parseInt(imageArray.get(2).toString());
            if (publicInt == 1)
                publicImage = true;
            uploadDate = imageArray.get(3).toString();
            longitude = imageArray.get(4).toString();
            latitude = imageArray.get(5).toString();
            description = imageArray.get(6).toString();
            favCount = Integer.parseInt(imageArray.get(7).toString());
            viewCount = Integer.parseInt(imageArray.get(8).toString());
            // Check if the image is liked
            String favString = imageArray.get(9).toString();
            if (!favString.equals("null")) {
                favorite = true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ProfilePanorama pp = new ProfilePanorama(imageID, viewCount, favorite, uploadDate,
                latitude, longitude, favCount, publicImage, description);
        return pp;

    }

    public static ExplorePanorama parseToExplorePanorama(JSONArray imageArray) {
        if (imageArray.length() == 0) {
            return null;
        }
        String imageID = "";
        String date = "";
        boolean publicImage = false;
        Double longitude = 0.0;
        Double latitude = 0.0;
        String uploader = "";
        try {
            imageID = imageArray.getString(0);
            uploader = imageArray.getString(1);
            date = imageArray.getString(2);
            int publicInt = Integer.parseInt(imageArray.getString(6));
            if (publicInt == 1)
                publicImage = true;
            longitude = Double.parseDouble(imageArray.getString(4));
            latitude = Double.parseDouble(imageArray.getString(5));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new ExplorePanorama(imageID, uploader, latitude, longitude, publicImage, "lol");
    }
}

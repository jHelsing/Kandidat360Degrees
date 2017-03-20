package com.ciux031701.kandidat360degrees.representation;

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
        int imageID = -1;
        boolean publicImage = false;
        String uploadDate = "";
        String longitude = "";
        String latitude = "";
        String description = "";
        int viewCount = -1;
        int favCount = -1;
        boolean favorite = false;
        try {
            imageID = Integer.parseInt(imageArray.get(0).toString());
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

        ProfilePanorama pp = new ProfilePanorama(imageID, favorite, uploadDate,
                latitude, longitude, favCount, viewCount, publicImage, description);
        return pp;
    }
}

package com.ciux031701.kandidat360degrees.representation;

import android.graphics.drawable.Drawable;

/**
 *
 * Represents a panorama image in the profile
 *
 * @author Jonathan
 * @version 0.1
 */
public class ProfilePanorama {

    private int favCount;
    private String location;
    private String date;
    private boolean favorite;
    private Drawable preview;
    private int panoramaID;

    public ProfilePanorama(int panoramaID, Drawable preview, boolean favorite, String date, String location, int favCount) {
        this.panoramaID = panoramaID;
        this.preview = preview;
        this.favorite = favorite;
        this.date = date;
        this.location = location;
        this.favCount = favCount;
    }

    public int getFavCount() {
        return favCount;
    }

    public String getLocation() {
        return location;
    }

    public String getDate() {
        return date;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public Drawable getPreview() {
        return preview;
    }

    public int getPanoramaID() {
        return panoramaID;
    }

}

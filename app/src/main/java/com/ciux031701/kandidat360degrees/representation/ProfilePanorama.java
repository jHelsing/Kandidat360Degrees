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
    private String latitude;
    private String longitude;
    private String date;
    private boolean favorite;
    private Drawable preview;
    private int panoramaID;
    private boolean publicImage;

    public ProfilePanorama(int panoramaID, boolean favorite, String date,
                           String latitude, String longitude, int favCount, boolean publicImage) {
        this.panoramaID = panoramaID;
        this.favorite = favorite;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.favCount = favCount;
        this.publicImage = publicImage;
    }

    public ProfilePanorama(int panoramaID, Drawable preview, boolean favorite, String date,
                           String latitude, String longitude, int favCount, boolean publicImage) {
        this.panoramaID = panoramaID;
        this.preview = preview;
        this.favorite = favorite;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.favCount = favCount;
        this.publicImage = publicImage;
    }

    public int getFavCount() {
        return favCount;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
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

    public boolean isPublicImage() {
        return publicImage;
    }

    public void setPreview(Drawable preview) {
        this.preview = preview;
    }

}

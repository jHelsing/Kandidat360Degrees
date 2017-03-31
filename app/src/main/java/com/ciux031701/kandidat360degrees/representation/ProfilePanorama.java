package com.ciux031701.kandidat360degrees.representation;

import android.graphics.drawable.Drawable;

/**
 *
 * Represents a panorama image in the profile
 *
 * @author Jonathan
 * @version 1.0
 */
public class ProfilePanorama {

    private int favCount;
    private int viewCount;
    private String latitude;
    private String longitude;
    private String date;
    private boolean favorite;
    private Drawable preview;
    private String panoramaID;
    private boolean publicImage;
    private String description;

    public ProfilePanorama(String panoramaID, boolean favorite, String date,
                           String latitude, String longitude, int favCount, int viewCount, boolean publicImage, String description) {
        this.panoramaID = panoramaID;
        this.favorite = favorite;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.favCount = favCount;
        this.viewCount = viewCount;
        this.publicImage = publicImage;
        this.description = description;
    }

    public ProfilePanorama(String panoramaID, Drawable preview, boolean favorite, String date,
                           String latitude, String longitude, int favCount, boolean publicImage, String description) {
        this.panoramaID = panoramaID;
        this.preview = preview;
        this.favorite = favorite;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.favCount = favCount;
        this.viewCount = viewCount;
        this.publicImage = publicImage;
        this.description = description;
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

    public String getPanoramaID() {
        return panoramaID;
    }

    public boolean isPublicImage() {
        return publicImage;
    }

    public void setPreview(Drawable preview) {
        this.preview = preview;
    }

}

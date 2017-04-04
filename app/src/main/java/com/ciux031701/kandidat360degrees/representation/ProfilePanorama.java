package com.ciux031701.kandidat360degrees.representation;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 *
 * Represents a panorama image in the profile
 *
 * @author Jonathan
 * @version 1.0
 */
public class ProfilePanorama implements Parcelable {

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

    public ProfilePanorama(String panoramaID, Drawable preview, boolean favorite, String date,
                           String latitude, String longitude, int favCount, int viewCount, boolean publicImage, String description) {
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

    public ProfilePanorama(String panoramaID, int viewCount, boolean favorite, String date,
                           String latitude, String longitude, int favCount, boolean publicImage, String description) {
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

    public void setFavorite(boolean favorite){ this.favorite = favorite; }

    public void increaseFavCount() { this.favCount++; }

    public void decreaseFavCount() { this.favCount--; }

    protected ProfilePanorama(Parcel in) {
        favCount = in.readInt();
        viewCount = in.readInt();
        latitude = in.readString();
        longitude = in.readString();
        date = in.readString();
        favorite = in.readByte() != 0x00;
        panoramaID = in.readString();
        publicImage = in.readByte() != 0x00;
        description = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(favCount);
        dest.writeInt(viewCount);
        dest.writeString(latitude);
        dest.writeString(longitude);
        dest.writeString(date);
        dest.writeByte((byte) (favorite ? 0x01 : 0x00));
        dest.writeString(panoramaID);
        dest.writeByte((byte) (publicImage ? 0x01 : 0x00));
        dest.writeString(description);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ProfilePanorama> CREATOR = new Parcelable.Creator<ProfilePanorama>() {
        @Override
        public ProfilePanorama createFromParcel(Parcel in) {
            return new ProfilePanorama(in);
        }

        @Override
        public ProfilePanorama[] newArray(int size) {
            return new ProfilePanorama[size];
        }
    };
}

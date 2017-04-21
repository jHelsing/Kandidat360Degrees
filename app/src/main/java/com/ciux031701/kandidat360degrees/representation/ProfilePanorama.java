package com.ciux031701.kandidat360degrees.representation;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.ciux031701.kandidat360degrees.ThreeSixtyWorld;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 *
 * Represents a panorama image in the profile
 *
 * @author Jonathan
 * @version 1.0
 */
public class ProfilePanorama extends ThreeSixtyPanorama implements Parcelable{


    private boolean favorite;
    private Bitmap preview;


    public ProfilePanorama(String imageid, String uploader, int views, boolean favorite, String date,
                           Double latitude, Double longitude, int likes, boolean isPublic, String description) {
        super(imageid, uploader, date, views, likes, new LatLng(latitude, longitude), description, isPublic);
        this.favorite = favorite;

    }


    public boolean isFavorite() {
        return favorite;
    }

    public Drawable getPreview() {
        return new BitmapDrawable(ThreeSixtyWorld.getAppContext().getResources(), preview);
    }
    public void setPreview(Bitmap preview) {
        this.preview = preview;
    }
    public void setFavorite(boolean favorite){ this.favorite = favorite; }



    protected ProfilePanorama(Parcel in) {
        super(in);
        favorite = in.readByte() != 0x00;
        preview = in.readParcelable(getClass().getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte((byte) (favorite ? 0x01 : 0x00));
        dest.writeValue(preview);
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

package com.ciux031701.kandidat360degrees.representation;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Jonathan on 2017-04-05.
 */

public class ExplorePanorama implements Parcelable {

    private String imageID;
    private LatLng location;
    private String uploader;
    private String date;
    private boolean isPublic;
    private boolean canView;

    public ExplorePanorama(String imageID, String uploader, Double lat, Double lng, boolean isPublic, String date) {
        this.imageID = imageID;
        this.uploader = uploader;
        this.date = date;
        this.location = new LatLng(lat, lng);
        this.isPublic = isPublic;
        if (isPublic)
            canView = true;
    }

    public ExplorePanorama(String imageID, String uploader, Double lat, Double lng, boolean isPublic, String date, boolean canView) {
        this.imageID = imageID;
        this.uploader = uploader;
        this.location = new LatLng(lat, lng);
        this.isPublic = isPublic;
        this.date = date;
        this.canView = canView;
    }

    protected ExplorePanorama(Parcel in) {
        imageID = in.readString();
        location = (LatLng) in.readValue(LatLng.class.getClassLoader());
        uploader = in.readString();
        date = in.readString();
        isPublic = in.readByte() != 0x00;
        canView = in.readByte() != 0x00;
    }

    public String getImageID() {
        return imageID;
    }

    public LatLng getLocation() {
        return location;
    }

    public String getUploader() {
        return uploader;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean isCanView() {
        return canView;
    }

    public String getDate() { return date; }

    public void setCanView(boolean canView) {
        this.canView = canView;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageID);
        dest.writeValue(location);
        dest.writeString(uploader);
        dest.writeString(date);
        dest.writeByte((byte) (isPublic ? 0x01 : 0x00));
        dest.writeByte((byte) (canView ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ExplorePanorama> CREATOR = new Parcelable.Creator<ExplorePanorama>() {
        @Override
        public ExplorePanorama createFromParcel(Parcel in) {
            return new ExplorePanorama(in);
        }

        @Override
        public ExplorePanorama[] newArray(int size) {
            return new ExplorePanorama[size];
        }
    };
}

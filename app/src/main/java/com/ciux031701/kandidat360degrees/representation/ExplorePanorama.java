package com.ciux031701.kandidat360degrees.representation;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Jonathan on 2017-04-05.
 */

public class ExplorePanorama extends ThreeSixtyPanorama implements Parcelable {

    private boolean canView;

    public ExplorePanorama(String imageid, String uploader, Double lat, Double lng, boolean isPublic, String date, String description, int views, int likes) {
        super(imageid, uploader, date, views, likes, new LatLng(lat, lng), description, isPublic);
        if (isPublic)
            canView = true;
    }

    public ExplorePanorama(String imageid, String uploader, Double lat, Double lng, boolean isPublic, String date, String description, boolean canView, int views, int likes) {
        super(imageid, uploader, date, views, likes, new LatLng(lat, lng), description, isPublic);
        this.canView = canView;
    }

    protected ExplorePanorama(Parcel in) {
        super(in);
        canView = in.readByte() != 0x00;
    }

    public boolean isCanView() {
        return canView;
    }


    public void setCanView(boolean canView) {
        this.canView = canView;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
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

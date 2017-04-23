package com.ciux031701.kandidat360degrees.representation;

import java.text.SimpleDateFormat;

import android.location.Location;
import android.location.LocationManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.ciux031701.kandidat360degrees.ThreeSixtyWorld;
import com.ciux031701.kandidat360degrees.communication.JReqNewImage;
import com.ciux031701.kandidat360degrees.communication.JRequest;
import com.ciux031701.kandidat360degrees.communication.Session;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.util.Date;

/**
 * Created by Neso on 2017-04-12.
 */

public class ThreeSixtyPanorama implements Parcelable {
    protected String imageid;
    protected String uploader;
    protected String date;
    protected int views;
    protected int likes;
    protected LatLng location;
    protected String description;
    protected boolean isPublic;

    public ThreeSixtyPanorama(String imageid, String uploader, String date, int views, int likes, LatLng location, String description, boolean isPublic){
        this.imageid = imageid;
        this.uploader = uploader;
        this.date = date;
        this.views = views;
        this.likes = likes;
        this.location = location;
        this.description = description;
        this.isPublic = isPublic;
    }

    public ThreeSixtyPanorama(String imageid, String description, Location location, Boolean isPublic){
        this(imageid, Session.getUser(), ThreeSixtyWorld.getDate(), 0, 0, new LatLng(location.getLatitude(), location.getLongitude()), description, isPublic);
    }


    protected ThreeSixtyPanorama(Parcel in) {
        imageid = in.readString();
        uploader = in.readString();
        date = in.readString();
        views = in.readInt();
        likes = in.readInt();
        location = in.readParcelable(LatLng.class.getClassLoader());
        description = in.readString();
        isPublic = in.readByte() != 0;
    }


    public String getImageID() {
        return imageid;
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

    public String getDate() {
        return date;
    }
    public String getDescription() {
        return description;
    }
    public int getLikeCount(){
        return likes;
    }
    public int getViewCount(){
        return views;
    }


    public void incLikeCount() {
        this.likes++;
    }

    public void decLikeCount() {
        this.likes--;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageid);
        dest.writeString(uploader);
        dest.writeString(date);
        dest.writeInt(views);
        dest.writeInt(likes);
        dest.writeParcelable(location, 0);
        dest.writeString(description);
        dest.writeByte((byte) (isPublic ? 0x01 : 0x00));
    }

    public static final Creator<ThreeSixtyPanorama> CREATOR = new Creator<ThreeSixtyPanorama>() {
        @Override
        public ThreeSixtyPanorama createFromParcel(Parcel in) {
            return new ThreeSixtyPanorama(in);
        }

        @Override
        public ThreeSixtyPanorama[] newArray(int size) {
            return new ThreeSixtyPanorama[size];
        }
    };
}

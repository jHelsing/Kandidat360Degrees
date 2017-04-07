package com.ciux031701.kandidat360degrees.representation;

import android.graphics.drawable.Drawable;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Jonathan on 2017-04-05.
 */

public class ExplorePanorama {

    private String imageID;
    private LatLng location;
    private String uploader;
    private String date;
    private boolean isPublic;
    private boolean canView;
    private Drawable preview;

    public ExplorePanorama(String imageID, String uploader, Double lat, Double lng, boolean isPublic, String date) {
        this.imageID = imageID;
        this.uploader = uploader;
        this.date = date;
        this.location = new LatLng(lat, lng);
        this.isPublic = isPublic;
        if (isPublic)
            canView = true;
    }

    public ExplorePanorama(String imageID, String uploader, Double lat, Double lng, boolean isPublic, String date, Drawable preview) {
        this.imageID = imageID;
        this.uploader = uploader;
        this.date = date;
        this.location = new LatLng(lat, lng);
        this.isPublic = isPublic;
        if (isPublic)
            canView = true;
        this.preview = preview;
    }

    public ExplorePanorama(String imageID, String uploader, Double lat, Double lng, boolean isPublic, String date, boolean canView, Drawable preview) {
        this.imageID = imageID;
        this.uploader = uploader;
        this.location = new LatLng(lat, lng);
        this.isPublic = isPublic;
        this.date = date;
        this.canView = canView;
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

    public Drawable getPreview() {
        return preview;
    }

    public String getDate() { return date; }

    public void setPreview(Drawable preview) {
        this.preview = preview;
    }

}

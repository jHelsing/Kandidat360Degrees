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
    private boolean isPublic;
    private boolean canView;
    private Drawable preview;

    public ExplorePanorama(String imageID, String uploader, Double lat, Double lng, boolean isPublic) {
        this.imageID = imageID;
        this.uploader = uploader;
        this.location = new LatLng(lat, lng);
        this.isPublic = isPublic;
        if (isPublic)
            canView = true;
    }

    public ExplorePanorama(String imageID, String uploader, Double lat, Double lng, boolean isPublic, Drawable preview) {
        this.imageID = imageID;
        this.uploader = uploader;
        this.location = new LatLng(lat, lng);
        this.isPublic = isPublic;
        if (isPublic)
            canView = true;
        this.preview = preview;
    }

    public ExplorePanorama(String imageID, String uploader, Double lat, Double lng, boolean isPublic, boolean canView, Drawable preview) {
        this.imageID = imageID;
        this.uploader = uploader;
        this.location = new LatLng(lat, lng);
        this.isPublic = isPublic;
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

    public void setPreview(Drawable preview) {
        this.preview = preview;
    }

}

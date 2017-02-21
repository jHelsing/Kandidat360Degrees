package com.ciux031701.kandidat360degrees.adaptors;

import android.graphics.drawable.Drawable;

/**
 * Created by boking on 2017-02-21.
 */

public class FlowPicture {
    private String location;
    private String date;
    private String image; //have this as drawable?
    public FlowPicture(String location, String date, String image){
        this.location = location;
        this.date = date;
        this.image = image;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}

package com.ciux031701.kandidat360degrees.representation;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by Neso on 2017-04-14.
 */

public class ThreeSixtyPanoramaCollection implements Parcelable {
    private Hashtable<String, ThreeSixtyPanorama> hashData;
    private ArrayList<ThreeSixtyPanorama> listData;

    public ThreeSixtyPanoramaCollection(){
        hashData = new Hashtable<>();
        listData = new ArrayList<>();
    }

    public ThreeSixtyPanorama get(int i){
        return listData.get(i);
    }
    public ThreeSixtyPanorama get(String imageID){
        return hashData.get(imageID);
    }
    public void add(ThreeSixtyPanorama item){
        hashData.put(item.getImageID(), item);
        listData.add(item);
    }
    public void remove(ThreeSixtyPanorama item){
        hashData.remove(item.getImageID());
        listData.remove(item);
    }
    public int size(){
        return listData.size();
    }
    public ArrayList<ThreeSixtyPanorama> getArrayList(){
        return listData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(listData);
    }

    protected ThreeSixtyPanoramaCollection(Parcel in) {
        listData = in.createTypedArrayList(ThreeSixtyPanorama.CREATOR);
        hashData = new Hashtable<String, ThreeSixtyPanorama>();
        for(int i = 0; i < listData.size();i++) {
            ThreeSixtyPanorama img = listData.get(i);
            hashData.put(img.getImageID(), img);
        }
    }
    public static final Creator<ThreeSixtyPanoramaCollection> CREATOR = new Creator<ThreeSixtyPanoramaCollection>() {
        @Override
        public ThreeSixtyPanoramaCollection createFromParcel(Parcel in) {
            return new ThreeSixtyPanoramaCollection(in);
        }

        @Override
        public ThreeSixtyPanoramaCollection[] newArray(int size) {
            return new ThreeSixtyPanoramaCollection[size];
        }
    };
}

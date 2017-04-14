package com.ciux031701.kandidat360degrees.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by Neso on 2017-04-14.
 */

public class ThreeSixtyPanoramaCollection implements Serializable {
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

}

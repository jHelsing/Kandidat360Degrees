package com.ciux031701.kandidat360degrees.imageprocessing;


import java.util.ArrayList;

/**
 * Created by Neso on 2017-04-22.
 */

public class CornerCollection extends ArrayList<Corner> {
    public Corner get(Orientation v, Orientation h){
        for(int i = 0; i < size(); i++){
            Corner c = get(i);
            if(c.vertical == v & c.horizontal == h)
                return c;
        }
        return null;
    }
}

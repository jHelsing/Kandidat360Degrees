package com.ciux031701.kandidat360degrees.imageprocessing;


import com.ciux031701.kandidat360degrees.imageprocessing.Orientation;

import org.opencv.core.Point;

/**
 * Created by Neso on 2017-04-22.
 */

public class Corner extends Point {
    public Orientation vertical;
    public Orientation horizontal;
    public Corner(Point p){
        super();
        this.x = p.x;
        this.y = p.y;
        vertical = Orientation.UNDEFINED;
    }
}

package com.ciux031701.kandidat360degrees.representation;

/**
 * Created by Neso on 2017-04-16.
 */

public enum CaptureState {
    IDLE, //State before user has started taking photos.
    NEXT, //State of waiting for alignment with the dot for the next photo.
    CAPTURING, //State of capturing a photo.
    PROCESSING //State of stitching and other processing.
}

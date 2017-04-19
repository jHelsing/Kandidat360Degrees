package com.ciux031701.kandidat360degrees.representation;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by Anna on 2017-03-22.
 */

public class NativePanorama {
    public native static void processPanorama(long[] imageAddressArray,long outputAddress);
    public native static void processPanoramaFromHandles(ArrayList<ByteBuffer> handles, long outputAddress);
}

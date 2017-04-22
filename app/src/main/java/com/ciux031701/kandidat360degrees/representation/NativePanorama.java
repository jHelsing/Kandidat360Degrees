package com.ciux031701.kandidat360degrees.representation;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by Anna on 2017-03-22.
 */

public class NativePanorama {

    static{
        System.loadLibrary("native-lib");
    }
    public native static ByteBuffer processPanoramaFromHandles(ArrayList<ByteBuffer> handles);

}

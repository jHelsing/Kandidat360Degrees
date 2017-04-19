package com.ciux031701.kandidat360degrees.imageprocessing;

import org.opencv.core.Mat;

import java.nio.ByteBuffer;

import static org.opencv.core.CvType.*;

/**
 * Created by Neso on 2017-04-19.
 */

public class JniMatHolder {
    private ByteBuffer handle = null;
    static{
        System.loadLibrary("MyLib");
    }
    private native ByteBuffer jniStoreMatData(Mat mat);
    private native Mat jniGetMatFromStoredData(ByteBuffer handle);
    private native void jniFreeMatData(ByteBuffer handle);

    public JniMatHolder(Mat mat){
        if(mat.empty())
            throw new RuntimeException("JniMatHolder: Mat is empty.");
        handle = jniStoreMatData(mat);
    }
    public Mat getMat(){
        return jniGetMatFromStoredData(handle);
    }
    public ByteBuffer getHandle(){
        return handle;
    }

}

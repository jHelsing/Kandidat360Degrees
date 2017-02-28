package com.ciux031701.kandidat360degrees;

import android.os.Bundle;
import com.panoramagl.PLCylindricalPanorama;
import com.panoramagl.PLImage;
import com.panoramagl.PLView;
import com.panoramagl.utils.PLUtils;


/**
 * Created by boking on 2017-02-28.
 */

public class ImageViewActivity extends PLView {

    PLCylindricalPanorama panorama;
    private Bundle args;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        panorama = new PLCylindricalPanorama();
        //panorama.setHeight(3.0f);
        panorama.getCamera().setPitchRange(0.0f, 0.0f);
        panorama.getCamera().setFov(75,false);
        panorama.getCamera().setFovMin(50);
        panorama.getCamera().setFovMax(82);
        panorama.setImage(new PLImage(PLUtils.getBitmap(this, R.drawable.panorama_example_2), false));
        this.setPanorama(panorama);
    }

}

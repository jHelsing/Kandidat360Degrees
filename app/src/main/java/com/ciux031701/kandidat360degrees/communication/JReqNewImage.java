package com.ciux031701.kandidat360degrees.communication;

import com.ciux031701.kandidat360degrees.representation.ThreeSixtyPanorama;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Nezo on 2017-03-14.
 */

public class JReqNewImage extends JRequest {
    public JReqNewImage(ThreeSixtyPanorama panorama) {
        super();
        String description = "";
        try{
            description = URLEncoder.encode(panorama.getDescription(), "UTF-8");
        }catch(UnsupportedEncodingException e){

        }
        PHP_NAME = "newimage.php";
        URL = PHP_ROOT + PHP_NAME + "?" + "sessionid="+ Session.getId() + "&imageid=" + panorama.getImageID() + "&desc=" + description +
                "&public=" + panorama.isPublic() + "&long=" + Double.toString(panorama.getLocation().longitude) +
                "&lat=" + Double.toString(panorama.getLocation().latitude);
    }
}

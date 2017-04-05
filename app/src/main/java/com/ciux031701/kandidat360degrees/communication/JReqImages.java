package com.ciux031701.kandidat360degrees.communication;

/**
 * Created by Jonathan on 2017-04-05.
 */

public class JReqImages extends JRequest {

    public JReqImages(String sessionid){
        super();
        PHP_NAME = "allimages.php";
        URL = PHP_ROOT + PHP_NAME + "?" + "sessionid=" + sessionid;
    }

}

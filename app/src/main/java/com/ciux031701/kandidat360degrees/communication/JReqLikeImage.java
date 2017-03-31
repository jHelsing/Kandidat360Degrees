package com.ciux031701.kandidat360degrees.communication;

/**
 * Created by AMAR on 2017-03-30.
 */

public class JReqLikeImage extends JRequest {
    public JReqLikeImage(String imageid) {
        super();
        PHP_NAME = "like.php";
        URL = PHP_ROOT + PHP_NAME + "?" + "sessionid=" + Session.getId() + "&" + "imageid=" + imageid;
    }
}

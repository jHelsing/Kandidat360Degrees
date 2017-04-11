package com.ciux031701.kandidat360degrees.communication;

/**
 * Created by jonat on 11/04/2017.
 */

public class JReqIsLiked extends JRequest {

    public JReqIsLiked(String imageID) {
        super();
        PHP_NAME = "isimageliked.php";
        URL = PHP_ROOT + PHP_NAME + "?" + "sessionid=" + Session.getId() + "&imageid=" + imageID;
    }

}

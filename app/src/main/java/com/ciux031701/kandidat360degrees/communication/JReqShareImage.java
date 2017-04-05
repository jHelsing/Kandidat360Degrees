package com.ciux031701.kandidat360degrees.communication;

/**
 * Created by Nezo on 2017-03-14.
 */

public class JReqShareImage extends JRequest {
    public JReqShareImage(String imageid, String usernames) {
        super();
        PHP_NAME = "share.php";
        URL = PHP_ROOT + PHP_NAME + "?" + "sessionid="+ Session.getId() + "&imageid=" + imageid + "&users=" + usernames;
    }
}

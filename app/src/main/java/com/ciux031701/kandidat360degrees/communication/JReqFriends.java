package com.ciux031701.kandidat360degrees.communication;

/**
 * Created by Nezo on 2017-03-14.
 */

public class JReqFriends extends JRequest {
    public JReqFriends() {
        super();
        PHP_NAME = "friends.php";
        URL = PHP_ROOT + PHP_NAME + "?" + "sessionid=" + Session.getId();
    }
}

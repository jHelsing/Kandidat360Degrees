package com.ciux031701.kandidat360degrees.communication;

/**
 * Created by Nezo on 2017-03-14.
 */

public class JReqFriendRequests extends JRequest {
    public JReqFriendRequests() {
        super();
        PHP_NAME = "friendrequests.php";
        URL = PHP_ROOT + PHP_NAME + "?" + "sessionid=" + Session.getId();
    }
}

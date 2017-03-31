package com.ciux031701.kandidat360degrees.communication;

/**
 * Created by Nezo on 2017-03-14.
 */

public class JReqAcceptFriend extends JRequest {
    public JReqAcceptFriend(String username) {
        super();
        PHP_NAME = "friendaccept.php";
        URL = PHP_ROOT + PHP_NAME + "?" + "sessionid=" + Session.getId() + "&user=" + username;
    }
}

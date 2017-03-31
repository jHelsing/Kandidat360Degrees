package com.ciux031701.kandidat360degrees.communication;

/**
 * Created by Nezo on 2017-03-14.
 */

public class JReqRemoveFriend extends JRequest {
    public JReqRemoveFriend(String username) {
        super();
        PHP_NAME = "friendremove.php";
        URL = PHP_ROOT + PHP_NAME + "?" + "sessionid=" + Session.getId() + "&user=" + username;
    }
}

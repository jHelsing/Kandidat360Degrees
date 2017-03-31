package com.ciux031701.kandidat360degrees.communication;

/**
 * Created by Nezo on 2017-03-14.
 */

public class JReqSendFriendrequest extends JRequest {
    public JReqSendFriendrequest(String username) {
        super();
        PHP_NAME = "friendrequest.php";
        URL = PHP_ROOT + PHP_NAME + "?" + "sessionid=" + Session.getId() + "&user=" + username;
    }
}

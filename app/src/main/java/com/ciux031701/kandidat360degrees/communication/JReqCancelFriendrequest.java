package com.ciux031701.kandidat360degrees.communication;

/**
 * Created by Nezo on 2017-03-14.
 */

public class JReqCancelFriendrequest extends JRequest {
    public JReqCancelFriendrequest(String username) {
        super();
        PHP_NAME = "withdrawfriendrequest.php";
        URL = PHP_ROOT + PHP_NAME + "?" + "sessionid=" + Session.getId() + "&user=" + username;
    }
}

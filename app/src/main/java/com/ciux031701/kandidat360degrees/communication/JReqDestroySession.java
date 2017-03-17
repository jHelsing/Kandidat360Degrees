package com.ciux031701.kandidat360degrees.communication;

/**
 * Created by Nezo on 2017-03-16.
 */

public class JReqDestroySession extends JRequest {
    public JReqDestroySession(){
        PHP_NAME = "destroysession.php";
        URL = PHP_ROOT + PHP_NAME + "?" + "sessionid=" + Session.getId();
    }
}

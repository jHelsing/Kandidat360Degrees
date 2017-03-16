package com.ciux031701.kandidat360degrees.Communication;

/**
 * Created by Nezo on 2017-03-16.
 */

public class JReqCheckSession extends JRequest {
    public JReqCheckSession(){
        PHP_NAME = "checksession.php";
        URL = PHP_ROOT + PHP_NAME + "?" + "sessionid=" + Session.getId();
    }
}

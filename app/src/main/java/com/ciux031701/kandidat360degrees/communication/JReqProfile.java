package com.ciux031701.kandidat360degrees.communication;

/**
 * Created by AMAR on 2017-03-18.
 */

public class JReqProfile extends JRequest{
    public JReqProfile(String username, String sessionid){
        super();
        PHP_NAME = "profile.php";
        URL = PHP_ROOT + PHP_NAME + "?" + "user=" + username + "&" + "sessionid=" + sessionid;
    }
}

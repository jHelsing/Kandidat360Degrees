package com.ciux031701.kandidat360degrees.communication;

/**
 * Created by Jonathan on 2017-03-20.
 */

public class JReqImageInfoProfile extends JRequest {

    public JReqImageInfoProfile(String sessionid, int imageid, String username){
        super();
        PHP_NAME = "imageinfoprofile.php";
        URL = PHP_ROOT + PHP_NAME + "?" + "user=" + username + "&" + "sessionid=" + sessionid + "&" + "imageid=" + imageid;
    }

}

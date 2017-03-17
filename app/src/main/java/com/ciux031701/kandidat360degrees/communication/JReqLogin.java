package com.ciux031701.kandidat360degrees.communication;

/**
 * Created by Nezo on 2017-03-14.
 */

public class JReqLogin extends JRequest {
    public JReqLogin(String username, String password){
        super();
        PHP_NAME = "login.php";
        URL = PHP_ROOT + PHP_NAME + "?" + "user=" + username + "&" + "password=" + password;
    }
}

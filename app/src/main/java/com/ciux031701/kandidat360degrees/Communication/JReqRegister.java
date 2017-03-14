package com.ciux031701.kandidat360degrees.Communication;

/**
 * Created by Nezo on 2017-03-14.
 */

public class JReqRegister extends JRequest {
    public JReqRegister(String username, String password, String email) {
        super();
        PHP_NAME = "register.php";
        URL = PHP_ROOT + PHP_NAME + "?" + "user=" + username + "&" + "password=" + password + "&" + "email=" + email;
    }
}

package com.ciux031701.kandidat360degrees;

/**
 * Created by boking on 2017-02-21.
 * The idea with the session class is to store non-sensitive information
 * so that we only need to ask for it from the main server once.
 */

public class Session {
    private static String username;

    public Session(String username){
        this.username = username;
    }

    public static String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

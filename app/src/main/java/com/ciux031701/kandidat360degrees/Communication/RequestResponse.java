package com.ciux031701.kandidat360degrees.Communication;

/**
 * Created by Nezo on 2017-03-11.
 * This the type for generic responses from the server. Used when nothing else is
 * needed other than whether there was success or failure, e.g when a user is attempting
 * to share or like an image.
 */

public class RequestResponse {
    private boolean error = false;
    private String message = "";
    public RequestResponse(boolean error, String msg){
        this.error = error;
        this.message = msg;
    }
    public boolean error(){return error;}
    public String message(){return message;}
}

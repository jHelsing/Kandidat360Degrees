package com.ciux031701.kandidat360degrees.communication;

/**
 * Created by Nezo on 2017-03-14.
 */


import org.json.JSONObject;

public class JRequest {
    protected final String PHP_ROOT = "http://saga.olf.sgsnet.se/android_connection/";
    protected String PHP_NAME = "";
    protected String URL = "";
    protected JResultListener listener;

    public JRequest(){
        listener = null;
    }

    public String getUrl() {
        return URL;
    }

    public interface JResultListener{
        void onHasResult(JSONObject result);
    }
    public void setJResultListener(JResultListener listener){
        this.listener = listener;
    }
    public void fireResultListener(JSONObject result){
        if(this.listener != null){
            this.listener.onHasResult(result);
        }
    }
}

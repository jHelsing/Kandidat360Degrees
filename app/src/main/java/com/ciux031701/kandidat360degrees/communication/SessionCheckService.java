package com.ciux031701.kandidat360degrees.communication;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.ciux031701.kandidat360degrees.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Neso on 2017-04-22.
 */

public class SessionCheckService extends IntentService {

    public SessionCheckService() {

        super("SessionCheckService");
    }
    public SessionCheckService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //Check if a session is saved locally.
        final Intent i = new Intent();
        i.setAction("com.ciux031701.kandidat360degrees.communication.SessionCheckService");
        if (Session.load()) {
            //Send a request to see if it matches a remote session.
            //If it does, skip login.
            JReqCheckSession checkSessionReq = new JReqCheckSession();
            checkSessionReq.setJResultListener(
                    new JRequest.JResultListener(){
                        @Override
                        public void onHasResult(JSONObject result) {
                            try {
                                boolean error = result.getBoolean("error");
                                if(!error){
                                    i.putExtra("RESULT", Activity.RESULT_OK);
                                    sendBroadcast(i);
                                }
                                else{
                                    i.putExtra("RESULT", Activity.RESULT_CANCELED);
                                    sendBroadcast(i);
                                }
                            }
                            catch(JSONException je){
                                je.printStackTrace();
                            }

                        }
                    }
            );
            checkSessionReq.sendRequest();
        }
        else{
            i.putExtra("RESULT", Activity.RESULT_CANCELED);
            sendBroadcast(i);
        }
    }
}



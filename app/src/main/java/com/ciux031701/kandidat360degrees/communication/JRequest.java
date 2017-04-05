package com.ciux031701.kandidat360degrees.communication;

/**
 * Created by Nezo on 2017-03-14.
 */


import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ciux031701.kandidat360degrees.ThreeSixtyWorld;

import org.json.JSONException;
import org.json.JSONObject;

public class JRequest {
    protected final String PHP_ROOT = "http://saga.olf.sgsnet.se/android_connection/";
    protected String PHP_NAME = "";
    protected String URL = "";
    protected JResultListener listener;

    private JSONObject JSONResult = null;

    public JSONObject getResult() {
        return JSONResult;
    }

    public void sendRequest() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, this.getUrl(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        response = response.trim();
                        try {
                            JSONResult = new JSONObject(response);
                            Log.d("Profile", response);
                            fireResultListener(JSONResult);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        VolleySingleton.getInstance(ThreeSixtyWorld.getAppContext()).addToRequestQueue(stringRequest);
    }

    public JRequest() {
        listener = null;
    }

    public String getUrl() {
        return URL;
    }

    public interface JResultListener {
        void onHasResult(JSONObject result);
    }

    public void setJResultListener(JResultListener listener) {
        this.listener = listener;
    }

    public void fireResultListener(JSONObject result) {
        if (this.listener != null) {
            this.listener.onHasResult(result);
        }
    }
}

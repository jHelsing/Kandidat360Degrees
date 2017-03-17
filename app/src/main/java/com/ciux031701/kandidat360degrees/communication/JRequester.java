package com.ciux031701.kandidat360degrees.communication;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ciux031701.kandidat360degrees.ThreeSixtyWorld;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Nezo on 2017-03-11.
 * This class is static and contains common JSON requests to the server. They are sent
 * via the RequestQueue in VolleySingleton.
 */

public class JRequester {
    public static String PHP_ROOT = "http://saga.olf.sgsnet.se/android_connection/";
    private static JSONObject JSONResult = null;
    private static JRequest jRequest = null;
    public static void setRequest(JRequest jReq){
        jRequest = jReq;
    }

    public static JSONObject getResult(){
        return JSONResult;
    }

    public static void sendRequest(){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, jRequest.getUrl(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        response = response.trim();
                        try {
                            JSONResult = new JSONObject(response);
                            jRequest.fireResultListener(JSONResult);
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
}

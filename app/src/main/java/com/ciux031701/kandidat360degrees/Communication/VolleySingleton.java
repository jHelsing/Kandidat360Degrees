package com.ciux031701.kandidat360degrees.Communication;
import com.android.volley.*;
import com.android.volley.toolbox.Volley;
import android.content.Context;

/**
 * Created by Nezo on 2017-03-11.
 * Singleton for volley requests. Should be created with application context, and not
 * activitiy context.
 */

public class VolleySingleton {
    public static VolleySingleton jrInstance;
    private RequestQueue jrQueue;
    private Context jrCtx;

    private VolleySingleton(Context context){
        jrCtx = context;
        jrQueue = getRequestQueue();
    }

    public static synchronized VolleySingleton getInstance(Context context) {
        if (jrInstance == null) {
            jrInstance = new VolleySingleton(context);
        }
        return jrInstance;
    }

    public RequestQueue getRequestQueue() {
        if (jrQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            jrQueue = Volley.newRequestQueue(jrCtx.getApplicationContext());
        }
        return jrQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}

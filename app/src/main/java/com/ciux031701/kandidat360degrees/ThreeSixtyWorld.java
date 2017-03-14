package com.ciux031701.kandidat360degrees;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Nezo on 2017-03-14.
 */

public class ThreeSixtyWorld extends Application {
    private static Context context;
    public static final String ApplicationPreferences = "AppPrefs";

    public void onCreate() {
        super.onCreate();
        ThreeSixtyWorld.context = getApplicationContext();

    }

    public static Context getAppContext() {
        return ThreeSixtyWorld.context;
    }
    public static SharedPreferences getSharedPrefs(){
        return getAppContext().getSharedPreferences(ApplicationPreferences, Context.MODE_PRIVATE);
    }
}

package com.ciux031701.kandidat360degrees;

import android.*;
import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.ciux031701.kandidat360degrees.communication.FTPInfo;
import com.google.android.gms.maps.model.LatLng;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Nezo on 2017-03-14.
 */

public class ThreeSixtyWorld extends Application {
    public static final Bitmap.CompressFormat COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    public static final int COMPRESSION_QUALITY = 100; //Ignored for CompressFormat.PNG
    private static Context context;
    public static final String ApplicationPreferences = "AppPrefs";

    public void onCreate() {
        super.onCreate();
        ThreeSixtyWorld.context = getApplicationContext();

    }

    public static Context getAppContext() {
        return ThreeSixtyWorld.context;
    }

    public static SharedPreferences getSharedPrefs() {
        return getAppContext().getSharedPreferences(ApplicationPreferences, Context.MODE_PRIVATE);
    }

    public static String getDate(){
        Date now = new Date();
        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(now);
    }

    public static void showToast(final Context context, final String text) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        });
    }
}

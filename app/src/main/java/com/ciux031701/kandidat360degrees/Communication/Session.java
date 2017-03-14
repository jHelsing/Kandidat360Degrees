package com.ciux031701.kandidat360degrees.Communication;
import com.ciux031701.kandidat360degrees.ThreeSixtyWorld;
import android.content.SharedPreferences;
import com.google.gson.Gson;

/**
 * Created by boking on 2017-02-21.
 * The idea with the session class is to store non-sensitive information
 * so that we only need to ask for it from the main server once.
 */

public class Session {
    private static String id;
    private static String user;


    public static String getId() {
        return id;
    }
    public static String getUser() {return user;}
    public static void setId(String sId){ id = sId;}
    public static void setUser(String username){ user = username;}
    public static boolean load(){
        id = ThreeSixtyWorld.getSharedPrefs().getString("Session", "");
        user = ThreeSixtyWorld.getSharedPrefs().getString("User", "");
        return !id.equals("");
    }
    public static void save(){
        SharedPreferences.Editor editor = ThreeSixtyWorld.getSharedPrefs().edit();
        editor.putString("Session", id);
        editor.putString("User", user);
        editor.commit();
    }

    public static void delete(){
        SharedPreferences.Editor editor = ThreeSixtyWorld.getSharedPrefs().edit();
        editor.putString("Session", "");
        editor.putString("User", "");
        editor.commit();
    }
}

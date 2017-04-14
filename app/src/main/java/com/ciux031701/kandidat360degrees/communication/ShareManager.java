package com.ciux031701.kandidat360degrees.communication;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import com.ciux031701.kandidat360degrees.ThreeSixtyWorld;
import com.ciux031701.kandidat360degrees.representation.ThreeSixtyPanorama;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by Neso on 2017-04-12.
 */

public class ShareManager extends BroadcastReceiver{
    private static final String UPLOAD_NOT_STARTED = "";
    private static ThreeSixtyPanorama imageInfo;
    private static String targetUsers;
    private static boolean locked;
    private static Context context;
    private static String uploadBitmap(Bitmap bmp){
        FileOutputStream fout = null;
        File tmp = new File(ThreeSixtyWorld.getAppContext().getFilesDir() + "/tmp" + FTPInfo.FILETYPE);
        try {
            tmp.createNewFile();
            fout = new FileOutputStream(tmp);
            bmp.compress(ThreeSixtyWorld.COMPRESS_FORMAT, ThreeSixtyWorld.COMPRESSION_QUALITY, fout);
            fout.close();
        }catch(IOException e){
            return UPLOAD_NOT_STARTED;
        }
        String imageid = MD5.fromFile(tmp);
        Intent uploadIntent = new Intent(ThreeSixtyWorld.getAppContext(), UploadService.class);
        uploadIntent.putExtra("FILE", tmp);
        uploadIntent.putExtra("IMAGETYPE", ImageType.PANORAMA);
        uploadIntent.putExtra("IMAGEID", imageid);
        ThreeSixtyWorld.getAppContext().startService(uploadIntent);
        return imageid;
    }
    public static void share(Context ctx, String targets, Bitmap bmp, String description, boolean isPublic){
        if(!isLocked()) {
            context = ctx;
            String imageid;
            if ((imageid = uploadBitmap(bmp)) != UPLOAD_NOT_STARTED) {
                setLocked(true);
                targetUsers = targets;
                imageInfo = new ThreeSixtyPanorama(imageid, description, isPublic);
            }
        }
    }

    private static void pushImage(ThreeSixtyPanorama imageInfo){
        JReqNewImage jReqNewImage = new JReqNewImage(imageInfo);
        jReqNewImage.setJResultListener(new JRequest.JResultListener() {
            @Override
            public void onHasResult(JSONObject result) {
                boolean error = false;
                try{
                    error = result.getBoolean("error");
                } catch(JSONException je){
                    Log.e("Image push", "Something went wrong while parsing JSONObject.");
                    handleError();
                }
                if(!error)
                    handlePushSuccess();
                else
                    handleError();
            }
        });
        jReqNewImage.sendRequest();
    }

    private static void setLocked(boolean lock){
        locked = lock;
    }
    private static boolean isLocked(){
        return locked;
    }
    
    private static void handlePushSuccess(){
        //Image is in database, let's try to share it.
        if(!targetUsers.isEmpty()) {
            JReqShareImage jReqShareImage = new JReqShareImage(imageInfo.getImageID(), targetUsers);
            jReqShareImage.setJResultListener(
                    new JRequest.JResultListener() {
                        @Override
                        public void onHasResult(JSONObject result) {
                            try {
                                boolean error = result.getBoolean("error");
                                if(!error)
                                    ThreeSixtyWorld.showToast(context, "Successfully shared image!");
                                else
                                    handleError();

                            }
                            catch(JSONException je){
                                handleError();
                            }
                        }
                    }
            );
            jReqShareImage.sendRequest();
        }
    }
    
    private static void handleError(){
        ThreeSixtyWorld.showToast(context, "Something went wrong while attempting to share image.");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //If bitmap upload was ok, try to push the info to the database.
        if(intent.getIntExtra("RESULT", Activity.RESULT_CANCELED) == Activity.RESULT_OK){
            pushImage(imageInfo);
        }
    }
}

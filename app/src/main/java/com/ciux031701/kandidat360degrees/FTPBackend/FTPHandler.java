package com.ciux031701.kandidat360degrees.FTPBackend;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.util.Log;

/**
 * @author Jonathan
 * @version 0.1
 */
public class FTPHandler {

    public FTPHandler() {

    }

    public Image downloadImage(Context context, int imageID) {
        // Start service
        Intent i = new Intent(context, DownloadService.class);
        i.putExtra("FILENAME", imageID+"");
        i.putExtra("FILETYPE", "PANORAMA");

        context.startService(i);
        Log.d("FTP", "Started Service");
        // Remove service
        return null;
    }

    public void uploadImage(Context context, Image image) {
        // Start service.
        
        // Stop service
    }

}

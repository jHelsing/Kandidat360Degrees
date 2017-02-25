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

    public Image downloadPanoramaImage(Context context, int imageID) {
        // Start service
        Intent i = new Intent(context, DownloadService.class);
        i.putExtra("IMAGEID", imageID);
        i.putExtra("IMAGETYPE", "PANORAMA");

        context.startService(i);
        Log.d("FTP", "Started Service");
        // Remove service
        return null;
    }

    public Image downloadProfileImage(Context context, int imageID) {
        // Start service
        Intent i = new Intent(context, DownloadService.class);
        i.putExtra("IMAGEID", imageID);
        i.putExtra("IMAGETYPE", "PROFILE");

        context.startService(i);
        Log.d("FTP", "Started Service");
        // Remove service
        return null;
    }

    public Image downloadPreviewImage(Context context, int imageID) {
        // Start service
        Intent i = new Intent(context, DownloadService.class);
        i.putExtra("IMAGEID", imageID);
        i.putExtra("IMAGETYPE", "PREVIEW");

        context.startService(i);
        Log.d("FTP", "Started Service");
        // Remove service
        return null;
    }

    public void uploadPanoramaImage(Context context, int imageID) {
        // Start service
        Intent i = new Intent(context, UploadService.class);
        i.putExtra("FILENAME", imageID);
        i.putExtra("FILETYPE", "PANORAMA");

        context.startService(i);
    }

    public void uploadProfileImage(Context context, int imageID) {
        // Start service
        Intent i = new Intent(context, UploadService.class);
        i.putExtra("FILENAME", imageID);
        i.putExtra("FILETYPE", "PROFILE");

        context.startService(i);
    }

    public void uploadPreviewImage(Context context, int imageID) {
        // Start service
        Intent i = new Intent(context, UploadService.class);
        i.putExtra("FILENAME", imageID);
        i.putExtra("FILETYPE", "PREVIEW");

        context.startService(i);
    }

}

package com.ciux031701.kandidat360degrees.Communication;


import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.util.Log;

/**
 * @author Jonathan
 * @version 0.1
 */
public class RequestHandler {

    public RequestHandler() {

    }

    public Image sendRequest(Context context, RequestType tskType, ImageType imgType, String imageID) {
        // Start service
        Intent i = null;
        switch(tskType) {
            case DOWNLOAD:
                i = new Intent(context, DownloadService.class);
                break;
            case UPLOAD:
                i = new Intent(context, UploadService.class);
                break;
        }
        i.putExtra("IMAGEID", imageID);
        i.putExtra("IMAGETYPE", imgType);

        context.startService(i);
        Log.d("FTP", "Started Service");
        // Remove service
        return null;
    }
}


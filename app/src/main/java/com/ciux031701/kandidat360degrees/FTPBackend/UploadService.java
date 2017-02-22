package com.ciux031701.kandidat360degrees.FTPBackend;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class UploadService extends IntentService {

    private static final String UPLOAD_IMAGE = "com.ciux031701.kandidat360degrees.FTPBackend.upload.image";

    public UploadService() {
        super("UploadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int imageID = intent.getIntExtra("FILENAME", 0);
        String fileType = intent.getStringExtra("FILETYPE");
        String filename = imageID + FTPInfo.FILETYPE;

        // Start FTP communication
        FTPClient ftpClient = null;
        File output =  new File("/var/www/360world" + "/" + fileType + "/" + filename);
        try {
            ftpClient = new FTPClient();
            ftpClient.connect(FTPInfo.DOMAIN, FTPInfo.PORT);
            if(ftpClient.login(FTPInfo.getUsername(),FTPInfo.getPassword())){

            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

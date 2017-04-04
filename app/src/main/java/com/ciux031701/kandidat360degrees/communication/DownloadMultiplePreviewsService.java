package com.ciux031701.kandidat360degrees.communication;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.ciux031701.kandidat360degrees.representation.ProfilePanorama;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DownloadMultiplePreviewsService extends IntentService {
    private int result = Activity.RESULT_CANCELED;
    public static final String NOTIFICATION = "com.ciux031701.kandidat360degrees.FTPBackend.action.downloadMultiplePreviews";

    public DownloadMultiplePreviewsService() {
        super("DownloadMultiplePreviewsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Create the correct path for the file to be downloaded, both on the server and locally
        String serverFilePath = FTPInfo.PREVIEW_SERVER_LOCATION;

        ArrayList<ProfilePanorama> panoramaList = intent.getParcelableArrayListExtra("panoramaArray");

        // Start up FTP connection and connect to the FTP server
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(FTPInfo.DOMAIN, FTPInfo.PORT);
            Log.d("FTP", "Phone connected to server: " + ftpClient.getReplyString());
        } catch (IOException e){
            Log.d("FTP", "Phone failed to connect to server: " + ftpClient.getReplyString());
            publishResults(Activity.RESULT_CANCELED, "1");
        }
        try {

            // Tries to log onto the server and if it fails it will return
            // failure, disconnect from server and terminate service
            if (!ftpClient.login(FTPInfo.getUsername(), FTPInfo.getPassword()))
                throw new IOException();

        } catch (IOException e) {
            // Failed the login
            Log.d("FTP", "Phone failed to log in to the FTP-server: " + ftpClient.getReplyString());

            try {
                ftpClient.disconnect();
            } catch (IOException e2) {
                Log.d("FTP", "Phone failed to disconnect the FTP-server: " + ftpClient.getReplyString());
                publishResults(Activity.RESULT_CANCELED, "2");
            }
            publishResults(Activity.RESULT_CANCELED, "3");
        }

        // Login successful
        Log.d("FTP", "Phone logged-in to server: " + ftpClient.getReplyString());

        // Make the server ready for downloading the files to our device
        ftpClient.enterLocalPassiveMode();

        try{
            // Set the correct filetype of the file on the server. For images it is as follows;
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            Log.d("FTP", "Correct file type is binary: " + ftpClient.getReplyString());
        } catch (IOException e) {
            Log.d("FTP", "Failed to change file type to binary: " + ftpClient.getReplyString());
            publishResults(Activity.RESULT_CANCELED, "4");
        }

        try {
            // Set the correct server location for the image to be uploaded
            ftpClient.changeWorkingDirectory(serverFilePath);
            Log.d("FTP", "Changed Directory: " + ftpClient.getReplyString());
        } catch (IOException e) {
            Log.d("FTP", "Failed to change directory: " + ftpClient.getReplyString());
            publishResults(Activity.RESULT_CANCELED, "5");
        }
        File localFolder = new File(getApplicationContext().getFilesDir() + FTPInfo.PREVIEW_LOCAL_LOCATION);
        if(!localFolder.exists())
            localFolder.mkdirs();

        for(int i=0; i<panoramaList.size(); i++){
            String panoramaID = panoramaList.get(i).getPanoramaID();

            File localFile = new File(getApplicationContext().getFilesDir() + FTPInfo.PREVIEW_LOCAL_LOCATION + panoramaID + FTPInfo.FILETYPE);
            if(!localFile.exists()) {
                try {
                    localFile.createNewFile();
                } catch (IOException e) {
                    Log.d("FTP", "Failed to create local file: " + panoramaID);
                    publishResults(Activity.RESULT_CANCELED, "6");
                }
            }
            try {
                OutputStream outputStream = new FileOutputStream(localFile.getPath());
                ftpClient.retrieveFile(panoramaID + ".jpg", outputStream);
            } catch (FileNotFoundException e) {
                Log.d("FTP", "Failed to find local file: " + panoramaID);
                publishResults(Activity.RESULT_CANCELED, "7");
            } catch (IOException e) {
                Log.d("FTP", "Failed to fetch file from FTP-server: " + ftpClient.getReplyString());
                publishResults(Activity.RESULT_CANCELED, "8");
            }
        }
        try {
            // Log out and disconnect from server so we do not take up unnecessary connections at server.
            ftpClient.logout();
            Log.d("FTP", "Logout: " + ftpClient.getReplyString());
        } catch (IOException e){
            Log.d("FTP", "Failed to logout from FTP-server: " + ftpClient.getReplyString());
        }
        try {
            ftpClient.disconnect();
            Log.d("FTP", "Disconnect: " + ftpClient.getReplyString());
        } catch (IOException e){
            Log.d("FTP", "Failed to disconnect from FTP-server: " + ftpClient.getReplyString());
        }
        publishResults(Activity.RESULT_OK, panoramaList);
    }

    /**
     * Broadcasts the results from the file download.
     * @param result - The result of the download, -1 if sucessful, 0 if failure
     */
    private void publishResults(int result, String error) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra("result", result);
        intent.putExtra("error",error);
        sendBroadcast(intent);
        this.stopSelf();
    }

    private void publishResults(int result, ArrayList<ProfilePanorama> panoramaList) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra("result", result);
        intent.putParcelableArrayListExtra("panoramaArray", panoramaList);
        sendBroadcast(intent);
        this.stopSelf();
    }
}

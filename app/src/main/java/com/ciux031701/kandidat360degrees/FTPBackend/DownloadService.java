package com.ciux031701.kandidat360degrees.FTPBackend;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.net.SocketException;

/**
 * @author Jonathan
 * @version 0.1
 */

public class DownloadService extends IntentService {

    private int result = Activity.RESULT_CANCELED;
    public static final String NOTIFICATION = "com.ciux031701.kandidat.360degrees.action.download";

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Create the correct path for the file to be downloaded
        String filePath = "";
        switch (intent.getStringExtra("FILETYPE")) {
            case "PREVIEW":
                filePath = FTPInfo.PREVIEWURL;
                break;
            case "PANORAMA":
                filePath = FTPInfo.PANORAMAURL;
                break;
            case "PROFILE":
                filePath = FTPInfo.PROFILEURL;
                break;
            default:
                this.stopSelf();
                break;
        }
        String filename = intent.getIntExtra("FILENAME", 0) + FTPInfo.FILETYPE;

        File outputDir = new File(getApplicationContext().getCacheDir() + filePath);
        if (!outputDir.exists())
           outputDir.mkdirs();

       // Grab username and password
        String username = FTPInfo.getUsername();
        String password = FTPInfo.getPassword();

        // Start FTP communication
        FTPClient ftpClient = null;
        File output =  new File(getApplicationContext().getCacheDir() + filePath + filename);
        try {
           ftpClient =  new FTPClient();
           ftpClient.connect(FTPInfo.DOMAIN, FTPInfo.PORT);
           Log.d("FTP", "Phone connected to server: " + ftpClient.getReplyString());

           if (!ftpClient.login(username, password)) {
              Log.d("FTP", ftpClient.getReplyString());
           }

           Log.d("FTP", "Phone logged-in to server: " + ftpClient.getReplyString());

           ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
           Log.d("FTP", "Phone in download mode: " + ftpClient.getReplyString());

           ftpClient.changeWorkingDirectory("/var/www/360world/panoramas/");
           Log.d("FTP", "Changed Directory: " + ftpClient.getReplyString()); // Working until here

           ftpClient.enterLocalPassiveMode();

           OutputStream outputStream = null;
           try {
               output.createNewFile();
               outputStream = new DataOutputStream(new FileOutputStream(output.getPath()));
               DataInputStream input = new DataInputStream(ftpClient.retrieveFileStream("111.jpg"));
               int next = -1;

               while ((next = input.read()) != -1) {
                  outputStream.write(next);
               }

               input.close();

               result = Activity.RESULT_OK;
               Log.d("FTP", "Completed");

           } finally {
               if (outputStream != null)
                   outputStream.close();
           }
           ftpClient.logout();
           Log.d("FTP", "Logout: " + ftpClient.getReplyString());
           ftpClient.disconnect();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        publishResults(output.getAbsolutePath(), result, intent.getIntExtra("FILENAME", 0)+"");

    }

    private void publishResults(String outputPath, int result, String fileName) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra("FILENAME", fileName);
        intent.putExtra("RESULT", result);
        sendBroadcast(intent);
    }
}

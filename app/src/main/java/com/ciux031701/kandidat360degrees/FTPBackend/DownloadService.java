package com.ciux031701.kandidat360degrees.FTPBackend;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Build;
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onHandleIntent(Intent intent) {
        // Create the correct path for the file to be downloaded
        String filePath = "";
        switch (intent.getStringExtra("FILETYPE")) {
            case "ICON":
                filePath = filePath + FTPInfo.PREVIEWURL;
                break;
            case "PANORAMA":
                filePath = filePath + FTPInfo.PANORAMAURL;
                break;
            case "PROFILE":
                filePath = filePath + FTPInfo.PROFILEURL;
                break;
            default:
                this.stopSelf();
                break;
        }
        String filename = intent.getStringExtra("FILENAME") + FTPInfo.FILETYPE;

        File outputDir = new File(getApplicationContext().getDataDir() + "/360world/");
        if (!outputDir.exists())
           outputDir.mkdirs();

       // Grab username and password
        String username = getUsername();
        String password = getPassword();

        // Start FTP communication
        FTPClient ftpClient = null;
        File output =  new File(getApplicationContext().getDataDir() + "/360world/" + filename);
        try {
           ftpClient =  new FTPClient();
           ftpClient.connect(FTPInfo.DOMAIN, FTPInfo.PORT);
           Log.d("FTP", "Phone connected to server");

           ftpClient.login(username, password);
           Log.d("FTP", "Phone logged-in to server");

           ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
           Log.d("FTP", "Phone in download mode");

           ftpClient.enterLocalPassiveMode();

           OutputStream outputStream = null;
           try {
               output.createNewFile();
               outputStream = new BufferedOutputStream(new FileOutputStream(output));
               result = Activity.RESULT_OK;
              Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG);
           } finally {
               if (outputStream != null)
                   outputStream.close();
           }
           ftpClient.logout();
           ftpClient.disconnect();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        publishResults(output.getAbsolutePath(), result, intent.getStringExtra("FILENAME"));

    }

    /**
     * Loads the username to the FTP server from storage
     * @return The username to log in with
     */
    private String getUsername() {
        return "superftpprofile";
    }

    /**
     * Loads the password to the FTP server from storage
     * @return The password to log in with
     */
    private String getPassword() {
        return "Ue0EXHSdjR717yAx";
    }

    private void publishResults(String outputPath, int result, String fileName) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra("FILENAME", fileName);
        intent.putExtra("RESULT", result);
        sendBroadcast(intent);
    }
}

package com.ciux031701.kandidat360degrees.FTPBackend;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.net.SocketException;
import java.net.URL;

/**
 * @author Jonathan
 * @version 0.1
 */

public class DownloadService extends IntentService {

    private int result = Activity.RESULT_CANCELED;
    private static final String DOMAIN = "saga.olf.sgsnet.se";
    private static final int PORT = 21;
    private static final String PROFILEURL = "\\profiles\\";
    private static final String PANORAMAURL = "\\panoramas\\";
    private static final String PREVIEWURL = "\\previews\\";
    private static final String FILETYPE = ".jpg";
    public static final String NOTIFICATION = "com.ciux031701.kandidat.360degrees";

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Create the correct path for the file to be downloaded
        String filePath = "";
        switch (intent.getStringExtra("FILETYPE")) {
            case "ICON":
                filePath = filePath + PREVIEWURL;
                break;
            case "PANORAMA":
                filePath = filePath + PANORAMAURL;
                break;
            case "PROFILE":
                filePath = filePath + PROFILEURL;
                break;
            default:
                this.stopSelf();
                break;
        }
        String filename = intent.getStringExtra("FILENAME") + FILETYPE;
        File output = new File(Environment.getExternalStorageDirectory(), filePath + filename);
        if (output.exists()) {
            output.delete();
        }

        // Grab username and password
        String username = getUsername();
        String password = getPassword();

        // Start FTP communication
        FTPClient ftpClient = null;

        try {
           ftpClient =  new FTPClient();
           ftpClient.connect(DOMAIN, PORT);
           Log.d("FTP", "Phone connected to server");

           ftpClient.login(username, password);
           Log.d("FTP", "Phone logged-in to server");

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Open connection to FTP server and download the correct file
        InputStream stream = null;
        FileOutputStream fileOutputStream = null;
        try {
            URL url = new URL("ftp", DOMAIN, filePath);

            stream = url.openConnection().getInputStream();
            InputStreamReader reader = new InputStreamReader(stream);
            fileOutputStream = new FileOutputStream(output.getPath());
            int next = -1;
            while ((next = reader.read()) != -1) {
                fileOutputStream.write(next);
            }
            // Download completed successfully
            result = Activity.RESULT_OK;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the stream
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        publishResults(output.getAbsolutePath(), result, intent.getStringExtra("FILENAME"));

    }

    /**
     * Loads the username to the FTP server from storage
     * @return The username to log in with
     */
    private String getUsername() {
        return null;
    }

    /**
     * Loads the password to the FTP server from storage
     * @return The password to log in with
     */
    private String getPassword() {
        return null;
    }

    private void publishResults(String outputPath, int result, String fileName) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra("FILENAME", fileName);
        intent.putExtra("RESULT", result);
        sendBroadcast(intent);
    }
}

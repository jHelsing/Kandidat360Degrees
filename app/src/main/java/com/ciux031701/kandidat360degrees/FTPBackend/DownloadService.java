package com.ciux031701.kandidat360degrees.FTPBackend;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;

import java.io.*;
import java.net.URL;

/**
 * @author Jonathan
 * @version 0.1
 */

public class DownloadService extends IntentService {

    private int result = Activity.RESULT_CANCELED;
    private static final String DOMAIN = "saga.olf.sgsnet.se";
    private static final String PROFILEURL = "profiles";
    private static final String PANORAMAURL = "panoramas";
    private static final String PREVIEWURL = "previews";
    public static final String NOTIFICATION = "com.ciux031701.kandidat.360degrees";

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Create the correct path for the file to be downloaded
        String fileName = "";
        switch (intent.getStringExtra("FILETYPE")) {
            case "ICON":
                fileName = fileName + PREVIEWURL;
                break;
            case "PANORAMA":
                fileName = fileName + PANORAMAURL;
                break;
            case "PROFILE":
                fileName = fileName + PROFILEURL;
                break;
        }
        fileName = fileName + "\\" + intent.getStringExtra("FILENAME");
        File output = new File(Environment.getExternalStorageDirectory(), fileName);
        if (output.exists()) {
            output.delete();
        }

        // Open connection to FTP server and download the correct file
        InputStream stream = null;
        FileOutputStream fileOutputStream = null;
        try {
            URL url = new URL("ftp", DOMAIN, fileName);

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

    private void publishResults(String outputPath, int result, String fileName) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra("FILENAME", fileName);
        intent.putExtra("RESULT", result);
        sendBroadcast(intent);
    }
}

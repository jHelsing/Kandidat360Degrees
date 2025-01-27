package com.ciux031701.kandidat360degrees.communication;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

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
    public static final String NOTIFICATION = "com.ciux031701.kandidat360degrees.FTPBackend.action.download";

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Create the correct path for the file to be downloaded, both on the server and locally
        String localFilePath = "";
        String serverFilePath = "";
        String filename = "";
        ImageType type = (ImageType) intent.getSerializableExtra("IMAGETYPE");
        switch (type) {
            case PREVIEW:
                localFilePath = FTPInfo.PREVIEW_LOCAL_LOCATION;
                serverFilePath = FTPInfo.PREVIEW_SERVER_LOCATION;
                filename = intent.getStringExtra("IMAGEID") + FTPInfo.FILETYPE;
                break;
            case PANORAMA:
                localFilePath = FTPInfo.PANORAMA_LOCAL_LOCATION;
                serverFilePath = FTPInfo.PANORAMA_SERVER_LOCATION;
                filename = intent.getStringExtra("IMAGEID") + FTPInfo.FILETYPE;
                break;
            case PROFILE:
                localFilePath = FTPInfo.PROFILE_LOCAL_LOCATION;
                serverFilePath = FTPInfo.PROFILE_SERVER_LOCATION;
                filename = intent.getStringExtra("USERNAME") + FTPInfo.FILETYPE;
                break;
            default:
                this.stopSelf();
                break;
        }

        // Make sure that the folder exists where we will store the picture
        File outputDir = new File(getApplicationContext().getFilesDir() + localFilePath);
        if (!outputDir.exists())
            outputDir.mkdirs();

        // Create the output file, this is where the downloaded image will be stored
        File outputFile = new File(getApplicationContext().getFilesDir() + localFilePath + filename);

        // Checks so that the file already doesn't exist. If it do exist we will publish failure and stop the service.
        // If it does not exist we will create the empty file and if that fails,
        // we will publish failure and stop the service.
        if (!outputFile.exists()) {
            try {
                outputFile.createNewFile();
            } catch (java.io.IOException e) {
                Log.d("FTP", "File could not be created at location " + outputFile.getPath());
                publishResults(outputFile.getPath(), Activity.RESULT_CANCELED,
                        intent.getStringExtra("IMAGEID") + "", type);
            }
        } else {
            publishResults(outputFile.getPath(), Activity.RESULT_OK,
                    intent.getStringExtra("IMAGEID") + "", type);
        }

        // Start FTP communication
        FTPClient ftpClient = null;

        try {
            // Start up FTP connection and connect to the FTP server
            ftpClient = new FTPClient();
            ftpClient.connect(FTPInfo.DOMAIN, FTPInfo.PORT);
            Log.d("FTP", "Phone connected to server: " + ftpClient.getReplyString());

            // Tries to log onto the server and if it fails it will return
            // failure, disconnect from server and terminate service
            if (!ftpClient.login(FTPInfo.getUsername(), FTPInfo.getPassword())) {
                // Failed the login
                Log.d("FTP", "FAILED: " + ftpClient.getReplyString());
                Log.d("FTP", "Returning failed result");
                Log.d("FTP", "Published results. Closing connection and stopping service.");
                ftpClient.disconnect();

                publishResults(outputFile.getPath(), Activity.RESULT_CANCELED,
                        intent.getStringExtra("IMAGEID") + "", type);
            } else {
                // Login successful
                Log.d("FTP", "Phone logged-in to server: " + ftpClient.getReplyString());
            }

            // Make the server ready for uploading a file to our device
            ftpClient.enterLocalPassiveMode();

            // Set the correct filetype of the file on the server. For images it is as follows;
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            Log.d("FTP", "Correct file type of binary: " + ftpClient.getReplyString());

            // Set the correct location for the image to be downloaded
            ftpClient.changeWorkingDirectory(serverFilePath);
            Log.d("FTP", "Changed Directory: " + ftpClient.getReplyString());
            FileOutputStream outputStream = null;

            try {
                // Start downloading the file from the server and write to the local file
                outputStream = new FileOutputStream(outputFile);
                ftpClient.setBufferSize(1024 * 1024);
                if (ftpClient.retrieveFile(filename, outputStream)) {
                    // Service results correct. Service has completed it's task and downloaded the correct file
                    result = Activity.RESULT_OK;
                    Log.d("FTP", "File downloaded correctly");
                }
                //Else if failed
                else
                    result = Activity.RESULT_CANCELED;
            } finally {
                if (outputStream != null)
                    outputStream.close();
            } // Done reading file (try-catch)

            // Log out and disconnect from server so we do not take up unnecessary connections at server.
            ftpClient.logout();
            Log.d("FTP", "Logout: " + ftpClient.getReplyString());
            ftpClient.disconnect();

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } // Done downloading file (try-catch)

        // Publish the results and then we are done here
        publishResults(outputFile.getAbsolutePath(), result, filename, type);

    }

    /**
     * Broadcasts the results from the file download.
     *
     * @param outputPath - The Path where the file is
     * @param result     - The result of the download, -1 if sucessful, 0 if failure
     * @param fileName   - The ID of the image that was downloaded.
     */
    private void publishResults(String outputPath, int result, String fileName, ImageType type) {
        Intent intent = new Intent(NOTIFICATION + fileName);
        intent.putExtra("IMAGEID", fileName);
        Log.d("Bilder", fileName + " : In Download service");
        intent.putExtra("RESULT", result);
        intent.putExtra("FILEPATH", outputPath);
        sendBroadcast(intent);
        this.stopSelf();
    }
}

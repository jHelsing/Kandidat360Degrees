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
        switch ((ImageType)intent.getSerializableExtra("IMAGETYPE")) {
            case PREVIEW:
                localFilePath = FTPInfo.PREVIEW_LOCAL_LOCATION;
                serverFilePath = FTPInfo.PREVIEW_SERVER_LOCATION;
                break;
            case PANORAMA:
                localFilePath = FTPInfo.PANORAMA_LOCAL_LOCATION;
                serverFilePath = FTPInfo.PANORAMA_SERVER_LOCATION;
                break;
            case PROFILE:
                localFilePath = FTPInfo.PROFILE_LOCAL_LOCATION;
                serverFilePath = FTPInfo.PROFILE_SERVER_LOCATION;
                break;
            default:
                this.stopSelf();
                break;
        }
        // Create the name of the file from the ID of the image and the filetype (JPG)
        String filename = intent.getIntExtra("IMAGEID", -1) + FTPInfo.FILETYPE;

        // Make sure that the folder exists where we will store the picture
        File outputDir = new File(getApplicationContext().getFilesDir() + localFilePath);
        if (!outputDir.exists())
           outputDir.mkdirs();

        // Create the output file, this is where the downloaded image will be stored
        File outputFile =  new File(getApplicationContext().getFilesDir() + localFilePath + filename);

        // Checks so that the file already doesn't exist. If it do exist we will publish failure and stop the service.
        // If it does not exist we will create the empty file and if that fails,
        // we will publish failure and stop the service.
        if (!outputFile.exists()) {
           try {
              outputFile.createNewFile();
           } catch (java.io.IOException e) {
              Log.d("FTP", "File could not be created at location " + outputFile.getPath());
              publishResults(outputFile.getPath(), Activity.RESULT_CANCELED,
                      intent.getIntExtra("IMAGEID", -1) + "");
           }
        } else {
           publishResults(outputFile.getPath(), Activity.RESULT_OK,
                   intent.getIntExtra("IMAGEID", -1) + "");
        }

        // Start FTP communication
        FTPClient ftpClient = null;

        try {
           // Start up FTP connection and connect to the FTP server
           ftpClient =  new FTPClient();
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
                      intent.getIntExtra("IMAGEID", -1) + "");
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

           // Start downloading the file from the server and write to the local file
           FileOutputStream outputStream = null;
           BufferedInputStream input = null;
           try {
               // Grab the correct input and output streams
               outputStream = new FileOutputStream(outputFile.getPath());
               input = new BufferedInputStream(ftpClient.retrieveFileStream(filename));

               // Start writing the file and and store it, int for int
               int next = -1;
               while ((next = input.read()) != -1) {
                  outputStream.write(next);
               }

               // make sure to close the input and output streams
               input.close();
               outputStream.close();

               // Service results correct. Service has completed it's task and downloaded the correct file
               result = Activity.RESULT_OK;
               Log.d("FTP", "File downloaded correctly");
           } finally {
               if (outputStream != null)
                   outputStream.close();
               if (input != null)
                  input.close();
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
        publishResults(outputFile.getAbsolutePath(), result, intent.getIntExtra("FILENAME", 0)+"");

    }

   /**
    * Broadcasts the results from the file download.
    * @param outputPath - The Path where the file is
    * @param result - The result of the download, -1 if sucessful, 0 if failure
    * @param fileName - The ID of the image that was downloaded.
    */
    private void publishResults(String outputPath, int result, String fileName) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra("IMAGEID", fileName);
        intent.putExtra("RESULT", result);
        intent.putExtra("FILEPATH", outputPath);
        sendBroadcast(intent);
        this.stopSelf();
    }
}

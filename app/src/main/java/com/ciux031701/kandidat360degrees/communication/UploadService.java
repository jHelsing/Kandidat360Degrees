package com.ciux031701.kandidat360degrees.communication;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
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

    private int result = Activity.RESULT_CANCELED;
    public static final String NOTIFICATION = "com.ciux031701.kandidat360degrees.FTPBackend.action.upload";

    public UploadService() {
        super("UploadService");
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

        // Create the output file, that is the local directory of the file that is going
        // to be uploaded to the server
        File intputFile =  new File(getApplicationContext().getFilesDir() + localFilePath + filename);

        if (!intputFile.exists()) {
            publishResults(Activity.RESULT_CANCELED);
        }
        Log.d("FTP", "File exists and can upload");
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

                publishResults(Activity.RESULT_CANCELED);
            } else {
                // Login successful
                Log.d("FTP", "Phone logged-in to server: " + ftpClient.getReplyString());
            }

            // Make the server ready for uploading a file to our device
            ftpClient.enterLocalPassiveMode();

            // Set the correct filetype of the file on the server. For images it is as follows;
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            Log.d("FTP", "Correct file type of binary: " + ftpClient.getReplyString());

            // Set the correct server location for the image to be uploaded
            ftpClient.changeWorkingDirectory(serverFilePath);
            Log.d("FTP", "Changed Directory: " + ftpClient.getReplyString());

            // Start uploading the file from the local device and write to the FTP-server
            FileInputStream inputStream = new FileInputStream(intputFile);
            boolean result = ftpClient.storeFile(filename, inputStream);
            inputStream.close();

            if(result){
                Log.d("FTP", "File uploaded correctly");
                ftpClient.logout();
                ftpClient.disconnect();
                publishResults(Activity.RESULT_OK);
            } else {
                Log.d("FTP", "File didn't upload");
                publishResults(Activity.RESULT_CANCELED);
            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Broadcasts the results from the file upload.
     * @param result - The result of the upload, -1 if sucessful, 0 if failure
     */
    private void publishResults(int result) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra("RESULT", result);
        sendBroadcast(intent);
        this.stopSelf();
    }
}

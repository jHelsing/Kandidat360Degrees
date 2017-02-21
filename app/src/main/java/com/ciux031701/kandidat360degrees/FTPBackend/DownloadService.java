package com.ciux031701.kandidat360degrees.FTPBackend;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.google.firebase.storage.StorageException;

public class DownloadService extends Service {
    public DownloadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return Service.START_REDELIVER_INTENT;
    }
}

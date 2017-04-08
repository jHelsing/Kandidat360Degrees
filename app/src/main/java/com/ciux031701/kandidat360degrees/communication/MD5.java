package com.ciux031701.kandidat360degrees.communication;

import android.util.Log;

import com.ciux031701.kandidat360degrees.R;
import com.ciux031701.kandidat360degrees.ThreeSixtyWorld;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Neso on 2017-04-08.
 */

public class MD5 {
    public static final String TAG = "MD5";
    public static String fromFile(File file){
        MessageDigest digest;
        try{
            digest = MessageDigest.getInstance("MD5");
        }
        catch(NoSuchAlgorithmException e){
            digest = null;
            Log.e(TAG, "Algorithm not found.");
        }
        InputStream instream;
        try{
            instream = new FileInputStream(file);
        }
        catch(FileNotFoundException fe){
            instream = null;
            Log.e(TAG, "File not found.");
        }

        byte[] buffer = new byte[8196];
        int read;
        try{
            while((read = instream.read(buffer)) > 0){
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger md5sInt = new BigInteger(1, md5sum);
            String out = md5sInt.toString(16);
            out = String.format("%32s", out).replace(' ', '0');
            return out;
        }
        catch(IOException e){
            throw new RuntimeException("Error reading file for MD5 hash.", e);
        }
        finally{
            try {
                instream.close();
            }
            catch(IOException e){
                Log.e(TAG, "Couldn't close InputStream.");
            }
        }
    }
}

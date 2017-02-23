package com.ciux031701.kandidat360degrees.FTPBackend;

/**
 *
 * Contains constant values for information used in connecting to and authenticating users.
 *
 * @author Jonathan
 * @version 0.1
 */
public class FTPInfo {

   public static final String DOMAIN = "saga.olf.sgsnet.se";
   public static final int PORT = 21;
   public static final String PROFILEURL = "/profiles/";
   public static final String PANORAMAURL = "/panoramas/";
   public static final String PREVIEWURL = "/previews/";
   public static final String FILETYPE = ".jpg";

   public static final String SERVERPANORAMA = "/var/www/360world/panoramas/";
   public static final String SERVERPROFILE = "/var/www/360world/profiles/";
   public static final String SERVERPREVIEW = "/var/www/360world/previews/";



   /**
    * Loads the username to the FTP server from storage
    * @return The username to log in with
    */
   public static String getUsername() {
      return "superftpprofile";
   }

   /**
    * Loads the password to the FTP server from storage
    * @return The password to log in with
    */
   public static String getPassword() {
      return "Ue0EXHSdjR717yAx";
   }

}

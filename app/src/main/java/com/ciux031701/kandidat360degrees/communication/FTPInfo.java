package com.ciux031701.kandidat360degrees.communication;

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
   public static final String PROFILE_LOCAL_LOCATION = "/profiles/";
   public static final String PANORAMA_LOCAL_LOCATION = "/panoramas/";
   public static final String PREVIEW_LOCAL_LOCATION = "/previews/";
   public static final String FILETYPE = ".JPEG";

   public static final String PANORAMA_SERVER_LOCATION = "/var/www/360world/panoramas/";
   public static final String PROFILE_SERVER_LOCATION = "/var/www/360world/profiles/";
   public static final String PREVIEW_SERVER_LOCATION = "/var/www/360world/previews/";



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

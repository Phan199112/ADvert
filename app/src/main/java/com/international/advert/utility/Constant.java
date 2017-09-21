package com.international.advert.utility;

/**
 * Created by softm on 15-Sep-17.
 */

public class Constant {

    public final static String SERVER_URL = "http://192.168.2.120/advert/index.php/";
    public final static String POST_IMG = "http://192.168.2.120/advert/uploadimages/poster/";
    public final static String AVATAR_IMG = "http://192.168.2.120/advert/uploadimages/avatar/";

    public final static String STATUS = "status";
    public final static String AUTO_LOGIN = "auto_login";

    public final static String USER_ID = "userid";
    public final static String USER_NAME = "username";
    public final static String USER_EMAIL = "email";
    public final static String USER_PASSWORD = "password";
    public final static String USER_UID = "uid";
    public final static String USER_TOKEN = "token";
    public final static String USER_PATH = "path";

    public final static String POSTER_ID = "post_id";
    public final static String POSTER_TITLE = "post_title";
    public final static String POSTER_CONTENT = "post_content";
    public final static String POSTER_TIME = "post_time";
    public final static String POSTER_PATH = "post_path";

    public final static String ONLINE_SENDERNAME = "sendername";
    public final static String ONLINE_SENDERID = "senderid";
    public final static String ONLINE_RECEIVERNAME = "receivername";
    public final static String ONLINE_IS_ENTER = "is_enter";

    //camera
    public final static int FROM_GALLERY = 101;
    public final static int FROM_CAMERA  = 100;
    public final static String CAMERA_URI = "camera_uri";

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
}

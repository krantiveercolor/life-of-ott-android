package com.life.android;

public class AppConfig {
    /**
     * APIS URLS : All the server urls is in the below sections
     */
    //public static final String DOMAIN = "https://actionott.com/";
    public static final String DOMAIN = "https://dev.lifeofott.com/";
    public static final String API_SERVER_URL = DOMAIN + "rest_api/";
    public static final String TERMS_URL = DOMAIN + "privacy-policy.html";


    public static final String API_KEY = "75xi3uer76tb7krer3mjgqei";
    public static final String YOUTUBE_API_KEY = "AIzaSyBlJmyeaVZ4Qlt9nYBnr0f0-PZSAucxmxY";
    public static final String Device_Type = "android";

    // download option for non subscribed user
    public static final boolean ENABLE_DOWNLOAD_TO_ALL = true;

    //enable RTL
    public static boolean ENABLE_RTL = true;

    //youtube video auto play
    public static boolean YOUTUBE_VIDEO_AUTO_PLAY = false;

    //enable external player
    public static final boolean ENABLE_EXTERNAL_PLAYER = false;

    //default theme
    public static boolean DEFAULT_DARK_THEME_ENABLE = true;

    // First, you have to configure firebase to enable facebook, phone and google login
    // facebook authentication
    public static final boolean ENABLE_FACEBOOK_LOGIN = false;

    //Phone authentication
    public static final boolean ENABLE_PHONE_LOGIN = false;

    //Google authentication
    public static final boolean ENABLE_GOOGLE_LOGIN = false;
}

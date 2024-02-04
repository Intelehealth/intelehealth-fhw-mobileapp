package org.intelehealth.klivekit.utils;

import android.content.Context;

public class Constants {
    //TODO: this base url will be same as user input server url, but port no is fixed here.
    // if server will change this port we have change this port no
    public static final String BASE_URL = Manager.getInstance().getBaseUrl() + ":3004";
    //public static final String BASE_URL = "https://testing.intelehealth.org:3004";

    public static final String ICE_SERVER_1_URL = "stun:stun.l.google.com:19302";
    public static final String ICE_SERVER_2_URL = "stun:stun1.l.google.com:19302";

    public static final String ICE_SERVER_3_URL = "turn:demo.intelehealth.org:3478";
    public static final String ICE_SERVER_3_USER = "ihuser";
    public static final String ICE_SERVER_3_PASSWORD = "keepitsecrect";

    public static final String ICE_SERVER_4_URL = "turn:testing.intelehealth.org:3478";
    public static final String ICE_SERVER_4_USER = "ihuser";
    public static final String ICE_SERVER_4_PASSWORD = "keepitsecrect";

//     {
//      "username": "dc2d2894d5a9023620c467b0e71cfa6a35457e6679785ed6ae9856fe5bdfa269",
//      "credential": "tE2DajzSJwnsSbc123",
//      "urls": "turn:global.turn.twilio.com:3478?transport=udp"
//    },
//    {
//      "username": "dc2d2894d5a9023620c467b0e71cfa6a35457e6679785ed6ae9856fe5bdfa269",
//      "credential": "tE2DajzSJwnsSbc123",
//      "urls": "turn:global.turn.twilio.com:3478?transport=tcp"
//    },

    public static final String ICE_SERVER_5_URL = "turn:global.turn.twilio.com:3478?transport=udp";
    public static final String ICE_SERVER_5_USER = "dc2d2894d5a9023620c467b0e71cfa6a35457e6679785ed6ae9856fe5bdfa269";
    public static final String ICE_SERVER_5_PASSWORD = "tE2DajzSJwnsSbc123";

    public static final String ICE_SERVER_6_URL = "turn:global.turn.twilio.com:3478?transport=tcp";
    public static final String ICE_SERVER_6_USER = "dc2d2894d5a9023620c467b0e71cfa6a35457e6679785ed6ae9856fe5bdfa269";
    public static final String ICE_SERVER_6_PASSWORD = "tE2DajzSJwnsSbc123";



    /*T2*/
   /* public static final String ICE_SERVER_1_URL = "stun:stun.l.google.com:19302";
    public static final String ICE_SERVER_2_URL = "stun:stun1.l.google.com:19302";

    public static final String ICE_SERVER_3_URL = "turn:uiux.intelehealth.org:3478";
    public static final String ICE_SERVER_3_USER = "uiux";
    public static final String ICE_SERVER_3_PASSWORD = "uiux";*/
    /*T2 END*/
    /*TEXTING*/
    /*public static final String ICE_SERVER_1_URL = "stun:bn-turn1.xirsys.com";

    public static final String ICE_SERVER_3_URL = "turn:bn-turn1.xirsys.com:80?transport=udp";
    public static final String ICE_SERVER_3_USER = "MvoeAGyQkHfadBQK3FYv4DVKig4Njm3MgwbfwHAP111_l3xfDHcWqQX969ZkI0lDAAAAAGQr_wlhbnVyYWc=";
    public static final String ICE_SERVER_3_PASSWORD = "5e5a5a28-d2d5-11ed-b3dc-0242ac140004";

    public static final String ICE_SERVER_4_URL = "turn:bn-turn1.xirsys.com:3478?transport=udp";
    public static final String ICE_SERVER_4_USER = "MvoeAGyQkHfadBQK3FYv4DVKig4Njm3MgwbfwHAP111_l3xfDHcWqQX969ZkI0lDAAAAAGQr_wlhbnVyYWc=";
    public static final String ICE_SERVER_4_PASSWORD = "5e5a5a28-d2d5-11ed-b3dc-0242ac140004";

    public static final String ICE_SERVER_5_URL = "turn:bn-turn1.xirsys.com:80?transport=tcp";
    public static final String ICE_SERVER_5_USER = "MvoeAGyQkHfadBQK3FYv4DVKig4Njm3MgwbfwHAP111_l3xfDHcWqQX969ZkI0lDAAAAAGQr_wlhbnVyYWc=";
    public static final String ICE_SERVER_5_PASSWORD = "5e5a5a28-d2d5-11ed-b3dc-0242ac140004";

    public static final String ICE_SERVER_6_URL = "turn:bn-turn1.xirsys.com:3478?transport=tcp";
    public static final String ICE_SERVER_6_USER = "MvoeAGyQkHfadBQK3FYv4DVKig4Njm3MgwbfwHAP111_l3xfDHcWqQX969ZkI0lDAAAAAGQr_wlhbnVyYWc=";
    public static final String ICE_SERVER_6_PASSWORD = "5e5a5a28-d2d5-11ed-b3dc-0242ac140004";

    public static final String ICE_SERVER_7_URL = "turns:bn-turn1.xirsys.com:443?transport=tcp";
    public static final String ICE_SERVER_7_USER = "MvoeAGyQkHfadBQK3FYv4DVKig4Njm3MgwbfwHAP111_l3xfDHcWqQX969ZkI0lDAAAAAGQr_wlhbnVyYWc=";
    public static final String ICE_SERVER_7_PASSWORD = "5e5a5a28-d2d5-11ed-b3dc-0242ac140004";

    public static final String ICE_SERVER_8_URL = "turns:bn-turn1.xirsys.com:5349?transport=tcp";
    public static final String ICE_SERVER_8_USER = "MvoeAGyQkHfadBQK3FYv4DVKig4Njm3MgwbfwHAP111_l3xfDHcWqQX969ZkI0lDAAAAAGQr_wlhbnVyYWc=";
    public static final String ICE_SERVER_8_PASSWORD = "5e5a5a28-d2d5-11ed-b3dc-0242ac140004";*/


    public static final String SEND_MESSAGE_URL = BASE_URL + "/api/messages/sendMessage";
    //@GET('https://testing.intelehealth.org:3004/messages/${fromUser}/${toUser}/${patientId}')
    public static final String GET_ALL_MESSAGE_URL = BASE_URL + "/api/messages/";
    public static final String SET_READ_STATUS_OF_MESSAGE_URL = BASE_URL + "/api/messages/read/"; //  https://uiux.intelehealth.org:3004/api/messages/read/881
    public static final String SAVE_FCM_TOKEN_URL = BASE_URL + "/api/mindmap/user_settings";

    public static final int LEFT_ITEM_DOCT = 1;
    public static final int RIGHT_ITEM_HW = 2;


    public static final String IMAGE_CAPTURE_DONE_INTENT_ACTION = "org.intelehealth.app.IMAGE_CAPTURE_DONE_INTENT_ACTION";
    public static final String IMAGE_CAPTURE_REQUEST_INTENT_ACTION = "org.intelehealth.app.IMAGE_CAPTURE_REQUEST_INTENT_ACTION";

    public static final String NOTIFICATION_RECEIVER = "NOTIFICATION_RECEIVER";

    public static String getNotificationReceiver(Context context) {
        return context.getApplicationContext().getPackageName() + "." + NOTIFICATION_RECEIVER;
    }
}

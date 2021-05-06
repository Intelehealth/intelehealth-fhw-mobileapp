package com.shivam.androidwebrtc.data;

public class Constants {
    public static final String BASE_URL = "https://testing.intelehealth.org:3004";

    public static final String ICE_SERVER_1_URL = "stun:stun.l.google.com:19302";
    public static final String ICE_SERVER_2_URL = "stun:stun1.l.google.com:19302";

    public static final String ICE_SERVER_3_URL = "turn:40.80.93.209:3478";
    public static final String ICE_SERVER_3_USER = "chat";
    public static final String ICE_SERVER_3_PASSWORD = "nochat";

    public static final String ICE_SERVER_4_URL = "turn:numb.viagenie.ca";
    public static final String ICE_SERVER_4_USER = "sultan1640@gmail.com";
    public static final String ICE_SERVER_4_PASSWORD = "98376683";

    public static final String SEND_MESSAGE_URL = BASE_URL+"/api/messages/sendMessage";
    //@GET('https://testing.intelehealth.org:3004/messages/${fromUser}/${toUser}/${patientId}')
    public static final String GET_ALL_MESSAGE_URL = BASE_URL+"/api/messages/";
    public static final String SAVE_FCM_TOKEN_URL = BASE_URL+"/api/mindmap/user_settings";

    public static final int LEFT_ITEM = 1;
    public static final int RIGHT_ITEM = 2;


}

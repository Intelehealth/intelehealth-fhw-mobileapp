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

    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_INFORMATION_MFG_NAME = "EXTRAS_DEVICE_INFORMATION_MFG_NAME";
    public static final String EXTRAS_DEVICE_INFORMATION_SERIAL_NUMBER = "EXTRAS_DEVICE_INFORMATION_SERIAL_NUMBER";
    public static final String EXTRAS_DEVICE_INFORMATION_MODEL_NUMBER = "EXTRAS_DEVICE_INFORMATION_MODEL_NUMBER";
    public static final String EXTRAS_DEVICE_BATTERY = "EXTRAS_DEVICE_BATTERY";
    public static final int syncFlag = 10;
    public static final int key_def_m1 = 3987;
    public static final int key_def_m2 = 9990;
    public static final int key_def_m3 = 2030;
    public static final int key_def_m4 = 5278;
    public static final int key_def_m5 = 3880;
    public static final int key_def_m6 = 1008;
    public static final int key_def_hip = 0;
    public static final int key_def_eq = 1;
    public static final int key_def_mf = 1000;
    public static final String KEY_TIMESTAMP = "unixtime";
    public static final String KEY_RESULTS = "results";
    public static final String language = "language";
    public static final String key_last = "last";
    public static final String key_cFlag = "cflag";
    public static final String key_notifyDataFlag = "notifydataflag";
    public static final String key_autoconnectflag = "autoflag";
    public static final String key_autoconnecbtname = "autoname";
    public static final String key_autoconnectaddress = "autoaddress";
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String key_M1 = "m1";
    public static final String key_M2 = "m2";
    public static final String key_M3 = "m3";
    public static final String key_M4 = "m4";
    public static final String key_Eq = "equation";
    public static final String key_HP = "highmode";
    public static final String key_M5 = "m5";
    public static final String key_M6 = "m6";
    public static final String key_MuFact = "mufact";
    public static final String key_mybluetoothaddress = "MyBluetoothAddress";
    public static final String key_devmfgname = "manufacture_name";
    public static final String key_devmfgdate = "manufacture_date";
    public static final String key_devsrno = "manufacture_srno";
    public static final String key_devname = "device_name";
    public static final String add_blood_flag = "2000";
    public static final String test_progress_flag = "3000";
    public static final String used_strip_error_flag1 = "2001";
    public static final String used_strip_error_flag2 = "2002";
    public static final String data_sync_done_flag = "2003";
    public static final String incomplete_test_flag1 = "3003";
    public static final String incomplete_test_flag_temp = "3004";
    public static final String incomplete_test_flag2 = "3005";
    public static final String incomplete_test_flag3 = "3006";
    public static final String qrcode_start_text = "BCS";
    public static final int devId_Sync = 1;
    public static final int devId_A1Chek = 2;
    public static final int devId_BP = 3;
    public static final int devId_Temp = 4;
    public static final int devId_HB = 5;
    public static final int devId_Lipid = 6;
    public static final String System_id_charac = "00002a23-0000-1000-8000-00805f9b34fb";
    public static final String Firmware_revision_charac = "00002a26-0000-1000-8000-00805f9b34fb";
    public static final String Hardware_revision_charac = "00002a27-0000-1000-8000-00805f9b34fb";
    public static final String Pnp_id_charac = "00002a50-0000-1000-8000-00805f9b34fb";
    public static final String Ieee_charac = "00002a2a-0000-1000-8000-00805f9b34fb";
    public static final String Sync_service = "0003abcd-0000-1000-8000-00805f9b0131";
    public static final String Sync_charac = "00031235-0001-0008-0000-0805F9B01310";
    public static final String Test_service = "0003abcd-0000-1000-8000-00805f9b0131";
    public static final String Test_charac = "00031234-0000-1000-8000-00805f9b0131";
    public static final String Write_first_service = "0003abcd-0000-1000-8000-00805f9b0131";
    public static final String Write_first_charac = "00000000-0000-1000-8000-00805f9b34fb";
    public static final String Write_second_service = "0003abcd-0000-1000-8000-00805f9b0131";
    public static final String Write_second_charac = "4bcec4d8-53ae-4853-8640-da6c3024dca9";
    public static final String Battery_level_service = "0000180f-0000-1000-8000-00805f9b34fb";
    public static final String Battery_level_charac = "00002a19-0000-1000-8000-00805f9b34fb";
    public static final String Device_info_service = "0000180a-0000-1000-8000-00805f9b34fb";
    public static final String Manufac_name_charac = "00002a29-0000-1000-8000-00805f9b34fb";
    public static final String Serial_number_charac = "00002a25-0000-1000-8000-00805f9b34fb";
    public static final String Model_number_charac = "00002a24-0000-1000-8000-00805f9b34fb";
    public static final String BP_Service_UUID = "0000fff0-0000-1000-8000-00805f9b34fb";
    public static final String BP_Char_UUID = "0000fff6-0000-1000-8000-00805f9b34fb";
    public static final String A1Chek_Service_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static final String A1Chek_Char_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static final String Tempreture_Service_UUID = "00001910-0000-1000-8000-00805f9b34fb";
    public static final String Tempreture_Char_UUID = "0000fff2-0000-1000-8000-00805f9b34fb";
    public static final String Lipid_Service_UUID = "0000fee9-0000-1000-8000-00805f9b34fb";
    public static final String Lipid_Char_UUID = "d44bc439-abfd-45a2-b575-925416129600";
    public static final String HB_Service_UUID = "773B37F4-56FC-4AE2-B20B-6D3AD301B892";
    public static final String HB_Char_UUID = "ee642efb-2086-4625-9541-370bf710b4c2";


}

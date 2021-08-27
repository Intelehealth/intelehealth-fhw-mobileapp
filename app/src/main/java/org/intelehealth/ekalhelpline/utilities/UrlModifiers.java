package org.intelehealth.ekalhelpline.utilities;

import android.util.Log;

import org.intelehealth.ekalhelpline.app.IntelehealthApplication;

public class UrlModifiers {
    private SessionManager sessionManager = null;

    public String loginUrl(String CLEAN_URL) {

        String urlModifier = "session";

        String BASE_URL = "https://" + CLEAN_URL + "/openmrs/ws/rest/v1/";
        return BASE_URL + urlModifier;
    }

    /**
     * @param CLEAN_URL : The base url that user has entered in the editText of setup screen.
     * @param USER_UUID : The uuid of the provider who has been authenticated in the app.
     * @return : formatted completed url to the hit by RX.
     */
    public String loginUrlProvider_phone(String CLEAN_URL, String USER_UUID) {
        return String.format("https://%s/openmrs/ws/rest/v1/provider?user=%s&v=custom:(uuid,person:(uuid,display,gender),attributes)",
                CLEAN_URL, USER_UUID);
    }


    public String loginUrlProvider(String CLEAN_URL, String USER_UUID) {

        String provider = "provider?user=" + USER_UUID;

        String BASE_URL = "https://" + CLEAN_URL + "/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }

    public String patientProfileImageUrl(String patientUuid) {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String provider = "personimage/" + patientUuid;

        String BASE_URL = "https://" + sessionManager.getServerUrl() + "/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }

    public String setPatientProfileImageUrl() {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String provider = "personimage";

        String BASE_URL = "https://" + sessionManager.getServerUrl() + "/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }


    public String obsImageUrl(String obsUuid) {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String provider = "obs/" + obsUuid + "/value";

        String BASE_URL = "https://" + sessionManager.getServerUrl() + "/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }

    public String obsImageDeleteUrl(String obsUuid) {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String provider = "obs/" + obsUuid;

        String BASE_URL = "https://" + sessionManager.getServerUrl() + "/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }

    public String setObsImageUrl() {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String provider = "obs";

        String BASE_URL = "https://" + sessionManager.getServerUrl() + "/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }

    /**
     * @return BASE_URL which returns the partial url for whatsapp prescription share feature.
     */
    public String setwhatsappPresciptionUrl() {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String BASE_URL = "https://" + sessionManager.getServerUrl() +
                "/preApi/index.jsp?v=";
        return BASE_URL;
    }

    public String getIvrCallUrl(String caller, String receiver) {
        String api_key = "A4f98feaafc067dd6d8d5223762e9ad44";
        return String.format("https://api-voice.kaleyra.com/v1/?api_key=%s&method=dial.click2call&caller=%s&receiver=%s", api_key, caller, receiver);
    }

    public String getIvrCall_ResponseUrl(String receiver, String todayDate) {
        String api_key = "A4f98feaafc067dd6d8d5223762e9ad44";

        return String.format("https://api-voice.kaleyra.com/v1/?method=dial.c2cstatus&api_key=%s&callto=%s&format=json&fromdate=%s",
                api_key, receiver, todayDate);
    }

    public String getBucketListUrl() {
        return "https://hellosaathi.uninhibited.org.in/buckets";
    }

    public String getSubscriptionStatusUrl(String number) {
        return String.format("https://hellosaathi.uninhibited.org.in/status/%s", number);
    }

    public String getSubscriptionUrl() {
        return "https://hellosaathi.uninhibited.org.in/subscribe";
    }

    public String setSMSPresciptionUrl( String visitUUid, String openMRSID) {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String BASE_URL = "https://" + sessionManager.getServerUrl() +
                "/preApi/index.jsp?v=%s&pid=%s";
        return String.format(BASE_URL,visitUUid,openMRSID);
    }

    public String getSendSmsUrl() {
        return "https://api.kaleyra.io/v1/HXIN1701481071IN/messages";
    }
}

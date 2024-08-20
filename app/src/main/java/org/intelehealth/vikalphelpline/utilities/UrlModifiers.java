package org.intelehealth.vikalphelpline.utilities;

import org.intelehealth.vikalphelpline.app.IntelehealthApplication;

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

    public String obsImageFilenameDownlaodUrl(String patientUuid, String obsUuid) {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
       // https://testhelpline.intelehealth.org/pullimage/7682cd54-8862-4652-ae6c-404f088c292c/a3fd3573-3bb7-41a8-a904-60e45b187b5c

        String BASE_URL = String.format("https://" + sessionManager.getServerUrl() + "/pullimage/%s/%s", patientUuid, obsUuid);
        return BASE_URL;
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
        String api_key = "Ad999102331cd1308668e72c0f60cffdc";
        return String.format("https://api-voice.kaleyra.com/v1/?api_key=%s&method=dial.click2call&caller=%s&receiver=%s", api_key, caller, receiver);
    }

    public String getSendSmsUrl() {
        return "https://api.kaleyra.io/v1/HXIN1701481071IN/messages";
    }

    public String additional_image_filename_url() {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String BASE_URL = "https://" + sessionManager.getServerUrl()+ "/pushimage";
        return BASE_URL;
    }

    public String getGiveResolutionUrl() {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String BASE_URL = "https://" + sessionManager.getServerUrl()+ "/openmrs/ws/rest/v1/encounter";
        return BASE_URL;
    }
}

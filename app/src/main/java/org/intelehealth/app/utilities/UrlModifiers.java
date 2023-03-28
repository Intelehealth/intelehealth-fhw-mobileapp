package org.intelehealth.app.utilities;

import org.intelehealth.app.app.IntelehealthApplication;

public class UrlModifiers {
    private SessionManager sessionManager = null;

    public String loginUrl(String CLEAN_URL) {

        String urlModifier = "session";

        String BASE_URL = "https://" + CLEAN_URL + "/openmrs/ws/rest/v1/";
        return BASE_URL + urlModifier;
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
    public String getADPImageUrl(String patientUuid) {
        // https://development.mysmartcaredoc.com/personimages/f9badd80-ab76-11e2-9e96-0800200c9a66
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String provider = "personimages/" + patientUuid;

        String BASE_URL = "https://" + sessionManager.getServerUrl() + "/";
        return BASE_URL + provider;
    }

    public String setPatientProfileImageUrl() {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String provider = "personimage";

        String BASE_URL = "https://" + sessionManager.getServerUrl() + "/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }

    public String setPatientADPImageUrl() {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String adp = "uploadpersonimage";

        String BASE_URL = "https://" + sessionManager.getServerUrl() + "/";
        return BASE_URL + adp;
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

    public String setDeletePrescItemUrl(String obsUuid) {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String BASE_URL = "https://" + sessionManager.getServerUrl() + "/openmrs/ws/rest/v1/";
        String endpoint = "obs/" + obsUuid;

        return BASE_URL + endpoint;

    }


}

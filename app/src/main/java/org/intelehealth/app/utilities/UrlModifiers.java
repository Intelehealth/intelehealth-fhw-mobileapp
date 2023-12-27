package org.intelehealth.app.utilities;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;

public class UrlModifiers {
    private SessionManager sessionManager = null;

    public String loginUrl(String CLEAN_URL) {

        String urlModifier = "session";

        String BASE_URL = CLEAN_URL + "/openmrs/ws/rest/v1/";
        return BASE_URL + urlModifier;
    }

    public String loginUrlProvider(String CLEAN_URL, String USER_UUID) {

        String provider = "provider?user=" + USER_UUID;

        String BASE_URL = CLEAN_URL + "/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }

    public String patientProfileImageUrl(String patientUuid) {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String provider = "personimage/" + patientUuid;

        String BASE_URL = sessionManager.getServerUrl() + "/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }

    public String setPatientProfileImageUrl() {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String provider = "personimage";

        String BASE_URL = sessionManager.getServerUrl() + "/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }


    public String obsImageUrl(String obsUuid) {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String provider = "obs/" + obsUuid + "/value";

        String BASE_URL = sessionManager.getServerUrl() + "/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }

    public String obsImageDeleteUrl(String obsUuid) {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String provider = "obs/" + obsUuid;

        String BASE_URL = sessionManager.getServerUrl() + "/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }

    public String setObsImageUrl() {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String provider = "obs";

        String BASE_URL = sessionManager.getServerUrl() + "/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }

    /**
     * @return BASE_URL which returns the partial url for whatsapp prescription share feature.
     */
    public String setwhatsappPresciptionUrl() {
        //https://uiux.intelehealth.org/intelehealth/index.html#/i/7d
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String BASE_URL = sessionManager.getServerUrl() +
                "/intelehealth/index.html";
        return BASE_URL;
    }

    public String setDeletePrescItemUrl(String obsUuid) {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String BASE_URL = sessionManager.getServerUrl() + "/openmrs/ws/rest/v1/";
        String endpoint = "obs/" + obsUuid;

        return BASE_URL + endpoint;

    }

    //ui2.0 for provider profile upload
    public String setProviderProfileImageUrl() {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());

        String BASE_URL = sessionManager.getServerUrl() + "/uploaddocimage";
        return BASE_URL;
    }
    //ui2.0 for provider profile download
    public String getProviderProfileImageUrl(String providerUuid) {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String BASE_URL = sessionManager.getServerUrl() + "/di/";
        String urlFinal = BASE_URL + providerUuid + "_image.png";
        return urlFinal;
    }

    public String profileAgeUpdateUrl(String USER_UUID) {
        String provider = "person/" + USER_UUID;
        String BASE_URL = AppConstants.DEMO_URL + "/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }

    public String getHWProfileDetails(String USER_UUID)
    {
        String provider = "provider?user=" + USER_UUID + "&v=custom:(uuid,person:(uuid,display,gender,age,birthdate,preferredName),attributes)";
        String BASE_URL = AppConstants.DEMO_URL + "/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }
}

package io.intelehealth.client.utilities;

import io.intelehealth.client.app.IntelehealthApplication;

public class UrlModifiers {
    SessionManager sessionManager = null;

    public String loginUrl(String CLEAN_URL) {

        String urlModifier = "session";

        String BASE_URL = "http://" + CLEAN_URL + ":8080/openmrs/ws/rest/v1/";
        return BASE_URL + urlModifier;
    }

    public String loginUrlProvider(String CLEAN_URL, String USER_UUID) {

        String provider = "provider?user=" + USER_UUID;

        String BASE_URL = "http://" + CLEAN_URL + ":8080/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }

    public String patientProfileImageUrl(String patientUuid) {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String provider = "personimage/" + patientUuid;

        String BASE_URL = "http://" + sessionManager.getServerUrl() + ":8080/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }

}

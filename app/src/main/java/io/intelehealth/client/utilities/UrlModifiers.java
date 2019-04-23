package io.intelehealth.client.utilities;

public class UrlModifiers {

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

}

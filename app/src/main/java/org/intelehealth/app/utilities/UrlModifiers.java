package org.intelehealth.app.utilities;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.app.IntelehealthApplication;

public class UrlModifiers {
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
        String provider = "personimage/" + patientUuid;
        String BASE_URL = BuildConfig.SERVER_URL + "/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }

    public String setPatientProfileImageUrl() {
        String provider = "personimage";
        String BASE_URL = BuildConfig.SERVER_URL + "/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }


    public String obsImageUrl(String obsUuid) {
        String provider = "obs/" + obsUuid + "/value";
        String BASE_URL = BuildConfig.SERVER_URL + "/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }

    public String obsImageDeleteUrl(String obsUuid) {
        String provider = "obs/" + obsUuid;
        String BASE_URL = BuildConfig.SERVER_URL + "/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }

    public String setObsImageUrl() {
        String provider = "obs";
        String BASE_URL = BuildConfig.SERVER_URL + "/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }

    /**
     * @return BASE_URL which returns the partial url for whatsapp prescription share feature.
     */
    public String setwhatsappPresciptionUrl() {
        return BuildConfig.SERVER_URL + "//preApi/index.jsp?v=";
    }

    public String setwhatsappPresciptionUrlArabic() {
        return BuildConfig.SERVER_URL + "//preApi/index-ar.jsp?v=";
    }

    public String setDeletePrescItemUrl(String obsUuid) {
        String BASE_URL = BuildConfig.SERVER_URL + "/openmrs/ws/rest/v1/";
        String endpoint = "obs/" + obsUuid;

        return BASE_URL + endpoint;

    }
}

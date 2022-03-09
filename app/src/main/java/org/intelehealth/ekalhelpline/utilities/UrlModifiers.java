package org.intelehealth.ekalhelpline.utilities;

import org.intelehealth.ekalhelpline.app.IntelehealthApplication;

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

    /**
     * @param CLEAN_URL : The base url that user has entered in the editText of setup screen.
     * @param USER_UUID : The uuid of the provider who has been authenticated in the app.
     * @return : formatted completed url to the hit by RX.
     */
    //TODO: Generalise the url after testing
    public String loginUrlProvider_phone(String CLEAN_URL, String USER_UUID) {
        return String.format("https://%s/openmrs/ws/rest/v1/provider?user=%s&v=custom:(uuid,person:(uuid,display,gender),attributes)", CLEAN_URL, USER_UUID);
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

    public String getCallPatientExotelUrl() {
        return "https://d788e0b618b3143e0f2df466fdad155561aaee3b32e4327d:49df1e7a635d9c575365fc82cc07d1ac3eb770e2284fd25e@api.exotel.com/";
    }

    public String getDoctorDetails()
    {   sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        return "https://"+ sessionManager.getServerUrl() +":3004/api/openmrs/getDoctorDetails"; }

}

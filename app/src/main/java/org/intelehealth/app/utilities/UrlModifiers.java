package org.intelehealth.app.utilities;

import android.util.Log;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.app.AppConstants;
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
        //https://uiux.intelehealth.org/intelehealth/index.html#/i/7d
        String BASE_URL = BuildConfig.SERVER_URL + "/intelehealth/index.html";
        return BASE_URL;
    }

    public String setDeletePrescItemUrl(String obsUuid) {
        String BASE_URL = BuildConfig.SERVER_URL + "/openmrs/ws/rest/v1/";
        String endpoint = "obs/" + obsUuid;

        return BASE_URL + endpoint;

    }

    //ui2.0 for provider profile upload
    public String setProviderProfileImageUrl() {
        return BuildConfig.SERVER_URL + "/uploaddocimage";
    }

    //ui2.0 for provider profile download
    public String getProviderProfileImageUrl(String providerUuid) {
        String BASE_URL = BuildConfig.SERVER_URL + "/di/";
        String urlFinal = BASE_URL + providerUuid + "_image.png";
        return urlFinal;
    }

    public String profileAgeUpdateUrl(String USER_UUID) {
        String provider = "person/" + USER_UUID;
        String BASE_URL = BuildConfig.SERVER_URL + "/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }

    public String getHWProfileDetails(String USER_UUID) {
        String provider = "provider?user=" + USER_UUID + "&v=custom:(uuid,person:(uuid,display,gender,age,birthdate,preferredName),attributes)";
        String BASE_URL = BuildConfig.SERVER_URL + "/openmrs/ws/rest/v1/";
        return BASE_URL + provider;
    }

    public static String getABDM_TokenUrl() {
        return BuildConfig.SERVER_URL + "/abha/getToken";
    }

    public static String getAadharOTPVerificationUrl() {
        Log.d("TAG", "getAadharOTPVerificationUrl: " + BuildConfig.SERVER_URL + "/abha/enrollOTPReq");
        return BuildConfig.SERVER_URL + "/abha/enrollOTPReq";
    }

    public static String getOTPForVerificationUrl() {
        return BuildConfig.SERVER_URL + "/abha/enrollByAadhar";
    }

    public static String getEnrollABHASuggestionUrl() {
        return BuildConfig.SERVER_URL + "/abha/enrollSuggestion";
    }
    public static String getSetPreferredABHAAddressUrl() {
        return BuildConfig.SERVER_URL + "/abha/setPreferredAddress";
    }
    public static String getMobileLoginVerificationUrl() {
        return BuildConfig.SERVER_URL + "/abha/loginOTPReq";
    }
    public static String getOTPForMobileLoginVerificationUrl() {
        return BuildConfig.SERVER_URL + "/abha/loginOTPVerify";
    }
    public static String getABHAProfileUrl() {
        return BuildConfig.SERVER_URL + "/abha/profile";
    }
    public static String getABHACardUrl() {
        return BuildConfig.SERVER_URL + "/abha/getProfile";
    }
}

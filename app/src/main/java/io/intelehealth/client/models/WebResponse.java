package io.intelehealth.client.models;

/**
 * Container for response code and string from server.
 * <p>
 * Created by Amal Afroz Alam on 12, August, 2016.
 * Contact me: contact@amal.io
 */

public class WebResponse {

    int responseCode = 1000;
    String responseString = "";
    String responseObject = "";

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseString() {
        return responseString;
    }

    public void setResponseString(String responseString) {
        this.responseString = responseString;
    }

    public String getResponseObject() {
        return responseObject;
    }

    public void setResponseObject(String responseObject) {
        this.responseObject = responseObject;
    }
}

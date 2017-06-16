package io.intelehealth.client.objects;

/**
 * Container for response code and string from server.
 * <p>
 * Created by Amal Afroz Alam on 12, August, 2016.
 * Contact me: contact@amal.io
 */

public class WebResponse {

    int responseCode = 1000;
    String responseString = "";

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
}

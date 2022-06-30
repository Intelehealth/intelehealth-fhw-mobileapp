package org.intelehealth.msfarogyabharat.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.intelehealth.msfarogyabharat.app.AppConstants;

public class WelcomeSms {
    public WelcomeSms(String to) {
        this.apiKey = AppConstants.SMS_API_KEY;
        this.to = to;
        this.body = "Thank you for calling and registering with MSF ArogyaBharat Helpline. If you are feeling stressed, or anxious, our Counsellors are here to support you. Please call on our toll-free no. 18001203710 - Powered by Intelehealth. Link to diet plan - https://msf-arogyabharat.intelehealth.org/intelehealth/index.html#/l/wc";
        this.type = "TXN";
        this.sender = "TIFDOC";
        this.source = "API";
        this.template_id = AppConstants.SMS_TEMPLATE_ID;
    }

    @SerializedName("api-key")
    @Expose
    private String apiKey;
    @SerializedName("to")
    @Expose
    private String to;

    @SerializedName("body")
    @Expose
    private String body;

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("sender")
    @Expose
    private String sender;

    @SerializedName("source")
    @Expose
    private String source;

    @SerializedName("template_id")
    @Expose
    private String template_id;
}

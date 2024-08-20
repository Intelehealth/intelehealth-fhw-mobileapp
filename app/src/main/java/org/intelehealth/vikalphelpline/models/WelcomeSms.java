package org.intelehealth.vikalphelpline.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.intelehealth.vikalphelpline.app.AppConstants;

public class WelcomeSms {
    public WelcomeSms(String to) {
        this.apiKey = AppConstants.SMS_API_KEY;
        this.to = to;
        this.body = "Thank you for calling and registering with MSF ArogyaBharat Helpline, to connect with our counsellor please call on toll-free no. 18003094144.";
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

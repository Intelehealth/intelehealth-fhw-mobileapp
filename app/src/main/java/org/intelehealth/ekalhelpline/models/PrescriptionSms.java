package org.intelehealth.ekalhelpline.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PrescriptionSms {

    public PrescriptionSms(String to, String body) {
        this.apiKey = "A39e1e65900618ef9b6e16da473f8894d";
        this.to = to;
        this.body = "Thank you for calling and registering with MSF ArogyaBharat Helpline, to connect with our counsellor please call on toll-free no. 18001203710.";
        this.type = "TXN";
        this.sender = "TIFDOC";
        this.source = "API";
        this.template_id = "1107162427070618591";
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


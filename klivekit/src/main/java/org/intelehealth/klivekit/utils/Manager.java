package org.intelehealth.klivekit.utils;

public class Manager {
    // static variable single_instance of type Singleton
    private static Manager singleInstance = null;

    // variable of type String
    private String baseUrl = "";

    // private constructor restricted to this class itself
    private Manager() {
    }

    // static method to create instance of Singleton class
    public static Manager getInstance() {
        if (singleInstance == null)
            singleInstance = new Manager();

        return singleInstance;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}

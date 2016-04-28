package edu.jhu.bme.cbid.healthassistantsclient.objects;

/**
 * Created by Amal Afroz Alam on 28, April, 2016.
 * Contact me: contact@amal.io
 */
public class MedicalHistory {

    public MedicalHistory(String n, String d) {
        this.name = n;
        this.data = d;
    }

    private String name;

    private String data;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}

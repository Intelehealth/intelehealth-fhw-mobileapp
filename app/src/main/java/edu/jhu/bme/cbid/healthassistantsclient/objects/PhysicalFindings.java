package edu.jhu.bme.cbid.healthassistantsclient.objects;

/**
 * Created by Amal Afroz Alam on 28, April, 2016.
 * Contact me: contact@amal.io
 */
public class PhysicalFindings {

    private String name;
    private String data;

    public PhysicalFindings() {
    }

    public PhysicalFindings(String name, String data) {
        this.name = name;
        this.data = data;
    }

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

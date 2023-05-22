package org.intelehealth.unicef.models;

/**
 * Created by Prajwal Waingankar
 * on 15-Jul-20.
 * Github: prajwalmw
 */


public class Uuid_Value {
    private String uuid;
    private String value;

    public Uuid_Value(String uuid, String value) {
        this.uuid = uuid;
        this.value = value;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

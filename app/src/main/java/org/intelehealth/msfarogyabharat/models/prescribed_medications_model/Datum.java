
package org.intelehealth.msfarogyabharat.models.prescribed_medications_model;

import com.google.gson.annotations.Expose;

public class Datum {

    @Expose
    private Long qty;

    @Expose
    private String unit;

    @Expose
    private String value;

    public Long getQty() {
        return qty;
    }

    public void setQty(Long qty) {
        this.qty = qty;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}

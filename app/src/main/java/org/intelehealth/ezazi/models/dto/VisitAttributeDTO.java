package org.intelehealth.ezazi.models.dto;

import java.util.UUID;

public class VisitAttributeDTO {
    private String uuid;
    private String visit_uuid;
    private String value;
    private String visit_attribute_type_uuid;
    private int voided;

    private String sync = "false";

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getVisitUuid() {
        return visit_uuid;
    }

    public void setVisit_uuid(String visit_uuid) {
        this.visit_uuid = visit_uuid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getVisitAttributeTypeUuid() {
        return visit_attribute_type_uuid;
    }

    public void setVisit_attribute_type_uuid(String visit_attribute_type_uuid) {
        this.visit_attribute_type_uuid = visit_attribute_type_uuid;
    }

    public int getVoided() {
        return voided;
    }

    public void setVoided(int voided) {
        this.voided = voided;
    }

    public void setSync(String sync) {
        this.sync = sync;
    }

    public String getSync() {
        return sync;
    }

    public static VisitAttributeDTO generateNew(String visitId, String value, String typeId) {
        VisitAttributeDTO attribute = new VisitAttributeDTO();
        attribute.setUuid(UUID.randomUUID().toString());
        attribute.setVoided(0);
        attribute.setVisit_attribute_type_uuid(typeId);
        attribute.setValue(value);
        attribute.setVisit_uuid(visitId);
        attribute.setSync("0");
        return attribute;
    }
}

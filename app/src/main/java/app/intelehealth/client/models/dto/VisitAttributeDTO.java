package app.intelehealth.client.models.dto;

public class VisitAttributeDTO {
    private String uuid;
    private String visit_uuid;
    private String value;
    private String visit_attribute_type_uuid;
    private int voided;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getVisit_uuid() {
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

    public String getVisit_attribute_type_uuid() {
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
}

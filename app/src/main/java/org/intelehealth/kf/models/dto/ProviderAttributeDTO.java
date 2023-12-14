package org.intelehealth.kf.models.dto;

public class ProviderAttributeDTO {
    private String uuid;
    private String provider_uuid;
    private String value;
    private String provider_attribute_type_uuid;
    private int voided;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getProvider_uuid() {
        return provider_uuid;
    }

    public void setProvider_uuid(String provider_uuid) {
        this.provider_uuid = provider_uuid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getProvider_attribute_type_uuid() {
        return provider_attribute_type_uuid;
    }

    public void setProvider_attribute_type_uuid(String provider_attribute_type_uuid) {
        this.provider_attribute_type_uuid = provider_attribute_type_uuid;
    }

    public int getVoided() {
        return voided;
    }

    public void setVoided(int voided) {
        this.voided = voided;
    }
}

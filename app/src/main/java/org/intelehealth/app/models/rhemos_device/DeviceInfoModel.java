package org.intelehealth.app.models.rhemos_device;

import java.io.Serializable;

public class DeviceInfoModel implements Serializable {
    private String power_level;
    private String device_id;
    private String device_key;
    private String software_version;
    private String hardware_version;
    private String firmware_version;

    public DeviceInfoModel() {
    }

    public DeviceInfoModel(String power_level, String device_id, String device_key,
                           String software_version, String hardware_version,
                           String firmware_version) {
        this.power_level = power_level;
        this.device_id = device_id;
        this.device_key = device_key;
        this.software_version = software_version;
        this.hardware_version = hardware_version;
        this.firmware_version = firmware_version;
    }

    public String getPower_level() {
        return power_level;
    }

    public void setPower_level(String power_level) {
        this.power_level = power_level;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getDevice_key() {
        return device_key;
    }

    public void setDevice_key(String device_key) {
        this.device_key = device_key;
    }

    public String getSoftware_version() {
        return software_version;
    }

    public void setSoftware_version(String software_version) {
        this.software_version = software_version;
    }

    public String getHardware_version() {
        return hardware_version;
    }

    public void setHardware_version(String hardware_version) {
        this.hardware_version = hardware_version;
    }

    public String getFirmware_version() {
        return firmware_version;
    }

    public void setFirmware_version(String firmware_version) {
        this.firmware_version = firmware_version;
    }

    @Override
    public String toString() {
        return "power_level = " + power_level + "\n" +
                "device_id = " + device_id + "\n" +
                "device_key = " + device_key + "\n" +
                "software_version = " + software_version + "\n" +
                "hardware_version = " + hardware_version + "\n" +
                "firmware_version = " + firmware_version + "\n";
    }
}

package org.intelehealth.app.models;

public class UploadResponse {


    public String uuid;
    String visit_uuid;
    String creator_uuid;
    String sound_type;
    String position;
    String tracker;
    String status;
    public String message;

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

    public String getCreator_uuid() {
        return creator_uuid;
    }

    public void setCreator_uuid(String creator_uuid) {
        this.creator_uuid = creator_uuid;
    }

    public String getSound_type() {
        return sound_type;
    }

    public void setSound_type(String sound_type) {
        this.sound_type = sound_type;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getTracker() {
        return tracker;
    }

    public void setTracker(String tracker) {
        this.tracker = tracker;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

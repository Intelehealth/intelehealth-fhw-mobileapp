package org.intelehealth.app.ayu.visit.model;

public class HeartLungRecordModel {
    public String id;
    public String patientUuid;
    public String visitUuid;
    public String encounterUuid;
    public String type;
    public String position;
    public String recordingStatus;
    public String audioPath;
    public String result;

    public HeartLungRecordModel() {
        this.id = id;
        this.patientUuid = patientUuid;
        this.visitUuid = visitUuid;
        this.encounterUuid = encounterUuid;
        this.type = type;
        this.position = position;
        this.recordingStatus = recordingStatus;
        this.audioPath = audioPath;
        this.result = result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public String getEncounterUuid() {
        return encounterUuid;
    }

    public void setEncounterUuid(String encounterUuid) {
        this.encounterUuid = encounterUuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getRecordingStatus() {
        return recordingStatus;
    }

    public void setRecordingStatus(String recordingStatus) {
        this.recordingStatus = recordingStatus;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
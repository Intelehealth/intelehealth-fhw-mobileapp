package org.intelehealth.app.models.queue;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * A single row of the queue list, mirroring one element of
 * {@code data.items[]} in the {@code /api/queue/list} response.
 *
 * <p>Free-form objects in the payload ({@code vitals}) are kept as
 * {@link JsonObject} because their shape is dynamic ("additionalProp*").
 */
public class QueueItem {

    @SerializedName("queueEntryId")
    @Expose
    private long queueEntryId;

    @SerializedName("visitUuid")
    @Expose
    private String visitUuid;

    @SerializedName("speciality")
    @Expose
    private String speciality;

    @SerializedName("status")
    @Expose
    private String status;

    @SerializedName("emergencyLevel")
    @Expose
    private String emergencyLevel;

    @SerializedName("caseType")
    @Expose
    private String caseType;

    @SerializedName("escalated")
    @Expose
    private boolean escalated;

    @SerializedName("position")
    @Expose
    private int position;

    @SerializedName("etaMinutes")
    @Expose
    private int etaMinutes;

    @SerializedName("etaAt")
    @Expose
    private String etaAt;

    @SerializedName("etaModelUsed")
    @Expose
    private String etaModelUsed;

    @SerializedName("assignedDoctorUuid")
    @Expose
    private String assignedDoctorUuid;

    @SerializedName("queuedAt")
    @Expose
    private String queuedAt;

    @SerializedName("assignedAt")
    @Expose
    private String assignedAt;

    @SerializedName("connectedAt")
    @Expose
    private String connectedAt;

    @SerializedName("completedAt")
    @Expose
    private String completedAt;

    @SerializedName("requeueCount")
    @Expose
    private int requeueCount;

    @SerializedName("heartbeatFlagged")
    @Expose
    private boolean heartbeatFlagged;

    @SerializedName("hwUserUuid")
    @Expose
    private String hwUserUuid;

    @SerializedName("patientUuid")
    @Expose
    private String patientUuid;

    @SerializedName("locationUuid")
    @Expose
    private String locationUuid;

    @SerializedName("flagged")
    @Expose
    private boolean flagged;

    @SerializedName("escalatedAt")
    @Expose
    private String escalatedAt;

    @SerializedName("chiefComplaint")
    @Expose
    private String chiefComplaint;

    @SerializedName("vitals")
    @Expose
    private JsonObject vitals;

    @SerializedName("waitedMinutes")
    @Expose
    private int waitedMinutes;

    @SerializedName("priorityScore")
    @Expose
    private double priorityScore;

    public long getQueueEntryId() {
        return queueEntryId;
    }

    public String getVisitUuid() {
        return visitUuid;
    }

    public String getSpeciality() {
        return speciality;
    }

    public String getStatus() {
        return status;
    }

    public String getEmergencyLevel() {
        return emergencyLevel;
    }

    public String getCaseType() {
        return caseType;
    }

    public boolean isEscalated() {
        return escalated;
    }

    public int getPosition() {
        return position;
    }

    public int getEtaMinutes() {
        return etaMinutes;
    }

    public String getEtaAt() {
        return etaAt;
    }

    public void setEtaAt(String etaAt) {
        this.etaAt = etaAt;
    }

    public String getEtaModelUsed() {
        return etaModelUsed;
    }

    public String getAssignedDoctorUuid() {
        return assignedDoctorUuid;
    }

    public String getQueuedAt() {
        return queuedAt;
    }

    public String getAssignedAt() {
        return assignedAt;
    }

    public String getConnectedAt() {
        return connectedAt;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public int getRequeueCount() {
        return requeueCount;
    }

    public boolean isHeartbeatFlagged() {
        return heartbeatFlagged;
    }

    public String getHwUserUuid() {
        return hwUserUuid;
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public String getLocationUuid() {
        return locationUuid;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public String getEscalatedAt() {
        return escalatedAt;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public JsonObject getVitals() {
        return vitals;
    }

    public int getWaitedMinutes() {
        return waitedMinutes;
    }

    public double getPriorityScore() {
        return priorityScore;
    }

    public void setQueueEntryId(long queueEntryId) {
        this.queueEntryId = queueEntryId;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setEmergencyLevel(String emergencyLevel) {
        this.emergencyLevel = emergencyLevel;
    }

    public void setCaseType(String caseType) {
        this.caseType = caseType;
    }

    public void setEscalated(boolean escalated) {
        this.escalated = escalated;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setEtaMinutes(int etaMinutes) {
        this.etaMinutes = etaMinutes;
    }

    public void setEtaModelUsed(String etaModelUsed) {
        this.etaModelUsed = etaModelUsed;
    }

    public void setAssignedDoctorUuid(String assignedDoctorUuid) {
        this.assignedDoctorUuid = assignedDoctorUuid;
    }

    public void setQueuedAt(String queuedAt) {
        this.queuedAt = queuedAt;
    }

    public void setAssignedAt(String assignedAt) {
        this.assignedAt = assignedAt;
    }

    public void setConnectedAt(String connectedAt) {
        this.connectedAt = connectedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    public void setRequeueCount(int requeueCount) {
        this.requeueCount = requeueCount;
    }

    public void setHeartbeatFlagged(boolean heartbeatFlagged) {
        this.heartbeatFlagged = heartbeatFlagged;
    }

    public void setHwUserUuid(String hwUserUuid) {
        this.hwUserUuid = hwUserUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public void setLocationUuid(String locationUuid) {
        this.locationUuid = locationUuid;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public void setEscalatedAt(String escalatedAt) {
        this.escalatedAt = escalatedAt;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public void setVitals(JsonObject vitals) {
        this.vitals = vitals;
    }

    public void setWaitedMinutes(int waitedMinutes) {
        this.waitedMinutes = waitedMinutes;
    }

    public void setPriorityScore(double priorityScore) {
        this.priorityScore = priorityScore;
    }
}

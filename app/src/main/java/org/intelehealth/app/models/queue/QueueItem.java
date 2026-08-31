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
}

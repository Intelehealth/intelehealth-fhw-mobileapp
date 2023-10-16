package org.intelehealth.app.models.dispenseAdministerModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DispenseAdministerModel {
    @SerializedName("medicationUuidList")
    @Expose
    private List<String> medicationUuidList;

    @SerializedName("medicationNotesList")
    @Expose
    private List<String> medicationNotesList;

    @SerializedName("documentsList")
    @Expose
    private List<String> documentsList;

    @SerializedName("hwName")
    @Expose
    private String hwName;

    @SerializedName("dateTime")
    @Expose
    private String dateTime;

    public DispenseAdministerModel() {
    }

    public List<String> getMedicationUuidList() {
        return medicationUuidList;
    }

    public void setMedicationUuidList(List<String> medicationUuidList) {
        this.medicationUuidList = medicationUuidList;
    }

    public List<String> getMedicationNotesList() {
        return medicationNotesList;
    }

    public void setMedicationNotesList(List<String> medicationNotesList) {
        this.medicationNotesList = medicationNotesList;
    }

    public List<String> getDocumentsList() {
        return documentsList;
    }

    public void setDocumentsList(List<String> documentsList) {
        this.documentsList = documentsList;
    }

    public String getHwName() {
        return hwName;
    }

    public void setHwName(String hwName) {
        this.hwName = hwName;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return "DispenseAdministerModel{" +
                "medicationUuidList=" + medicationUuidList +
                ", medicationNotesList=" + medicationNotesList +
                ", documentsList=" + documentsList +
                ", hwName='" + hwName + '\'' +
                ", dateTime='" + dateTime + '\'' +
                '}';
    }
}

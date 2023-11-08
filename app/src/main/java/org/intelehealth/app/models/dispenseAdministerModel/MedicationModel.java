package org.intelehealth.app.models.dispenseAdministerModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class MedicationModel extends AidModel implements Serializable {
    @SerializedName("medicationUuidList")
    @Expose
    private List<String> medicationUuidList;

    @SerializedName("medicationNotesList")
    @Expose
    private List<String> medicationNotesList;

//    @SerializedName("documentsList")
//    @Expose
//    private List<String> documentsList;

   /* @SerializedName("hwUuid")
    @Expose
    private String hwUuid;
    @SerializedName("hwName")
    @Expose
    private String hwName;*/

//    @SerializedName("dateTime")
//    @Expose
//    private String dateTime;

    public MedicationModel() {
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
        return super.getDocumentsList();
    }

    public void setDocumentsList(List<String> documentsList) {
        super.setDocumentsList(documentsList);
    }

    public String getHwName() {
        return super.getHwName();
    }

    public void setHwName(String hwName) {
        super.setHwName(hwName);
    }

    public String getHwUuid() {
        return super.getHwUuid();
    }

    public void setHwUuid(String hwUuid) {
        super.setHwUuid(hwUuid);
    }

    public String getDateTime() {
        return super.getDateTime();
    }

    public void setDateTime(String dateTime) {
        super.setDateTime(dateTime);
    }

    @Override
    public String toString() {
        return "MedicationModel{" +
                "medicationUuidList=" + medicationUuidList +
                ", medicationNotesList=" + medicationNotesList +
                ", documentsList=" + super.getDocumentsList() +
                ", hwName='" + super.getHwName() + '\'' +
                ", dateTime='" + super.getDateTime() + '\'' +
                '}';
    }
}

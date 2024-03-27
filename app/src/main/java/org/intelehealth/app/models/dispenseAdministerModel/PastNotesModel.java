package org.intelehealth.app.models.dispenseAdministerModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by - Prajwal W. on 08/11/23.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
public class PastNotesModel extends MedicationAidModel{
    private String notes;
    @SerializedName("additional_remark")
    @Expose
    private String additional_remark;

    public PastNotesModel() {
    }

    public PastNotesModel(String notes, String dateTime) {
        this.notes = notes;
        super.setDateTime(dateTime);
    }

    public PastNotesModel(String notes) {
        this.notes = notes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDateTime() {
        return super.getDateTime();
    }

    public void setDateTime(String dateTime) {
        super.setDateTime(dateTime);
    }

    public String getAdditional_remark() {
        return additional_remark;
    }

    public void setAdditional_remark(String additional_remark) {
        this.additional_remark = additional_remark;
    }

    @Override
    public String toString() {
        return "PastNotesModel{" +
                "notes='" + notes + '\'' +
                ", dateTime='" + super.getDateTime() + '\'' +
                '}';
    }
}

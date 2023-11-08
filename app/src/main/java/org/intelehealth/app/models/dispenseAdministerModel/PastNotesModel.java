package org.intelehealth.app.models.dispenseAdministerModel;

import java.util.List;

/**
 * Created by - Prajwal W. on 08/11/23.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
public class PastNotesModel extends MedicationAidModel{
    private String notes;

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

    @Override
    public String toString() {
        return "PastNotesModel{" +
                "notes='" + notes + '\'' +
                ", dateTime='" + super.getDateTime() + '\'' +
                '}';
    }
}

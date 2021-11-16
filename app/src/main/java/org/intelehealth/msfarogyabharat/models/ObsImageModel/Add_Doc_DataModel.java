package org.intelehealth.msfarogyabharat.models.ObsImageModel;

/**
 * Created By: Prajwal Waingankar on 24-Aug-21
 * Github: prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class Add_Doc_DataModel {
    private String obsuuid;
    private String filename;
    private String encounteruid;

    public Add_Doc_DataModel(String obsuuid, String filename, String encounteruid) {
        this.obsuuid = obsuuid;
        this.filename = filename;
        this.encounteruid = encounteruid;
    }

    public String getObsuuid() {
        return obsuuid;
    }

    public void setObsuuid(String obsuuid) {
        this.obsuuid = obsuuid;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getEncounteruid() {
        return encounteruid;
    }

    public void setEncounteruid(String encounteruid) {
        this.encounteruid = encounteruid;
    }
}

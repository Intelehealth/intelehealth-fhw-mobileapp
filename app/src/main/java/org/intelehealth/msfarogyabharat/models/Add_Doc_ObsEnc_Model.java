package org.intelehealth.msfarogyabharat.models;

/**
 * Created By: Prajwal Waingankar on 27-Oct-21
 * Github: prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class Add_Doc_ObsEnc_Model {
    private String obsuid;
    private String encounteruid;

    public Add_Doc_ObsEnc_Model(String obsuid, String encounteruid) {
        this.obsuid = obsuid;
        this.encounteruid = encounteruid;
    }

    public String getObsuid() {
        return obsuid;
    }

    public void setObsuid(String obsuid) {
        this.obsuid = obsuid;
    }

    public String getEncounteruid() {
        return encounteruid;
    }

    public void setEncounteruid(String encounteruid) {
        this.encounteruid = encounteruid;
    }
}

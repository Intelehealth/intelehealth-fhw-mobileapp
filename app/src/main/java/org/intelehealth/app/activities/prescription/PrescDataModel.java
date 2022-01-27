package org.intelehealth.app.activities.prescription;

/**
 * Created by Prajwal Maruti Waingankar on 20-01-2022, 17:01
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

public class PrescDataModel {
    private String uuid;
    private String value;
    private String encounterVisitNoteUuid;
    private String conceptUuid;

    public PrescDataModel(String uuid, String value, String encounterVisitNoteUuid, String conceptUuid) {
        this.uuid = uuid;
        this.value = value;
        this.encounterVisitNoteUuid = encounterVisitNoteUuid;
        this.conceptUuid = conceptUuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String encounterVisitNoteUuid() {
        return encounterVisitNoteUuid;
    }

    public void encounterVisitNoteUuid(String encounterVisitNoteUuid) {
        this.encounterVisitNoteUuid = encounterVisitNoteUuid;
    }

    public String getConceptUuid() {
        return conceptUuid;
    }

    public void setConceptUuid(String conceptUuid) {
        this.conceptUuid = conceptUuid;
    }
}

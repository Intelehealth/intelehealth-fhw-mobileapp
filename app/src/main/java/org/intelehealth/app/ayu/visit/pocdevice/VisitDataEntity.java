package org.intelehealth.app.ayu.visit.pocdevice;

import androidx.room.PrimaryKey;

public class VisitDataEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String patientUuid;
    public String patientName;
    public String visitUuid;
    public String encounterUuidVitals;
    public String encounterUuidAdultInitial;

    public String type;
    public String position;
    public String filePath;

    public int recordingStatus;
}

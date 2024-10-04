package org.intelehealth.app.ayu.visit.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CommonVisitData implements Parcelable {
    private String privacyNote = "";
    private String patientUuid;
    private String visitUuid;
    private String encounterUuidVitals;
    private String encounterUuidAdultIntial;
    private String EncounterAdultInitialLatestVisit;
    private String state;
    private String patientName;
    private String patientGender;
    private String intentTag = "new";
    private float patientAgeYearMonth;

    private boolean hasPrescription;
    private int editFor;
    private boolean isPastVisit;


    protected CommonVisitData(Parcel in) {
        privacyNote = in.readString();
        patientUuid = in.readString();
        visitUuid = in.readString();
        encounterUuidVitals = in.readString();
        encounterUuidAdultIntial = in.readString();
        EncounterAdultInitialLatestVisit = in.readString();
        state = in.readString();
        patientName = in.readString();
        patientGender = in.readString();
        intentTag = in.readString();
        patientAgeYearMonth = in.readFloat();
        hasPrescription = in.readByte() != 0;
        editFor = in.readInt();
        isPastVisit = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(privacyNote);
        dest.writeString(patientUuid);
        dest.writeString(visitUuid);
        dest.writeString(encounterUuidVitals);
        dest.writeString(encounterUuidAdultIntial);
        dest.writeString(EncounterAdultInitialLatestVisit);
        dest.writeString(state);
        dest.writeString(patientName);
        dest.writeString(patientGender);
        dest.writeString(intentTag);
        dest.writeFloat(patientAgeYearMonth);
        dest.writeByte((byte) (hasPrescription ? 1 : 0));
        dest.writeInt(editFor);
        dest.writeByte((byte) (isPastVisit ? 1 : 0));
    }

    public static final Creator<CommonVisitData> CREATOR = new Creator<CommonVisitData>() {
        @Override
        public CommonVisitData createFromParcel(Parcel in) {
            return new CommonVisitData(in);
        }

        @Override
        public CommonVisitData[] newArray(int size) {
            return new CommonVisitData[size];
        }
    };

    public boolean isHasPrescription() {
        return hasPrescription;
    }

    public void setHasPrescription(boolean hasPrescription) {
        this.hasPrescription = hasPrescription;
    }

    public int getEditFor() {
        return editFor;
    }

    public void setEditFor(int editFor) {
        this.editFor = editFor;
    }

    public CommonVisitData() {

    }


    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public String getEncounterUuidVitals() {
        return encounterUuidVitals;
    }

    public void setEncounterUuidVitals(String encounterUuidVitals) {
        this.encounterUuidVitals = encounterUuidVitals;
    }

    public String getEncounterUuidAdultIntial() {
        return encounterUuidAdultIntial;
    }

    public void setEncounterUuidAdultIntial(String encounterUuidAdultIntial) {
        this.encounterUuidAdultIntial = encounterUuidAdultIntial;
    }

    public String getEncounterAdultInitialLatestVisit() {
        return EncounterAdultInitialLatestVisit;
    }

    public void setEncounterAdultInitialLatestVisit(String encounterAdultInitialLatestVisit) {
        EncounterAdultInitialLatestVisit = encounterAdultInitialLatestVisit;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public void setPatientGender(String patientGender) {
        this.patientGender = patientGender;
    }

    public String getIntentTag() {
        return intentTag;
    }

    public void setIntentTag(String intentTag) {
        this.intentTag = intentTag;
    }

    public float getPatientAgeYearMonth() {
        return patientAgeYearMonth;
    }

    public void setPatientAgeYearMonth(float patientAgeYearMonth) {
        this.patientAgeYearMonth = patientAgeYearMonth;
    }


    public String getPrivacyNote() {
        return privacyNote;
    }

    public void setPrivacyNote(String privacyNote) {
        this.privacyNote = privacyNote;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public boolean isPastVisit() {
        return isPastVisit;
    }

    public void setPastVisit(boolean pastVisit) {
        isPastVisit = pastVisit;
    }
}

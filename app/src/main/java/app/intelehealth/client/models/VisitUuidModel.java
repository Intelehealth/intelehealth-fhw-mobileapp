package app.intelehealth.client.models;

import android.os.Parcel;
import android.os.Parcelable;

public class VisitUuidModel implements Parcelable {

    public static final Creator<VisitUuidModel> CREATOR = new Creator<VisitUuidModel>() {
        @Override
        public VisitUuidModel createFromParcel(Parcel in) {
            return new VisitUuidModel(in);
        }

        @Override
        public VisitUuidModel[] newArray(int size) {
            return new VisitUuidModel[size];
        }
    };
    private String encounterVitals;
    private String encounterAdultIntials;
    private String visitUuidString;

    public VisitUuidModel(Parcel in) {
        encounterVitals = in.readString();
        encounterAdultIntials = in.readString();
        visitUuidString = in.readString();
    }

    public VisitUuidModel(String vitals, String encounterVitals, String encounterAdultIntials) {
        this.encounterVitals = encounterVitals;
        this.encounterAdultIntials = encounterAdultIntials;
        this.visitUuidString = vitals;
    }

    public VisitUuidModel(String encounterVitals, String encounterAdultIntials) {
        this.encounterVitals = encounterVitals;
        this.encounterAdultIntials = encounterAdultIntials;
    }

    public String getEncounterVitals() {
        return encounterVitals;
    }

    public void setEncounterVitals(String encounterVitals) {
        this.encounterVitals = encounterVitals;
    }

    public String getEncounterAdultIntials() {
        return encounterAdultIntials;
    }

    public void setEncounterAdultIntials(String encounterAdultIntials) {
        this.encounterAdultIntials = encounterAdultIntials;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(encounterVitals);
        dest.writeString(encounterAdultIntials);
        dest.writeString(visitUuidString);
    }
}

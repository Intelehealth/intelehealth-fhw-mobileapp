package io.intelehealth.client.network.visitModel;

public class VisitModel {
    private String visituuid;
    private boolean isVisitExists;

    public String getVisituuid() {
        return visituuid;
    }

    public void setVisituuid(String visituuid) {
        this.visituuid = visituuid;
    }

    public boolean isVisitExists() {
        return isVisitExists;
    }

    public void setVisitExists(boolean visitExists) {
        isVisitExists = visitExists;
    }
}

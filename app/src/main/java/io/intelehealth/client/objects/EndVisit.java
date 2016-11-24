package io.intelehealth.client.objects;

/**
 * End-of-visit information class for Gson data serialization
 * To be implemented in a future version.
 */
public class EndVisit {
    private boolean printed;
    private boolean paid;
    private Integer meds;


    public boolean isPrinted() {
        return printed;
    }

    public void setPrinted(boolean printed) {
        this.printed = printed;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public Integer getMeds() {
        return meds;
    }

    public void setMeds(Integer meds) {
        this.meds = meds;
    }
}

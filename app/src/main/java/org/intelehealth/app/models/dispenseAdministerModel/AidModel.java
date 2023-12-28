package org.intelehealth.app.models.dispenseAdministerModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.utilities.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class AidModel {

    @SerializedName("aidUuidList")
    @Expose
    List<String> aidUuidList;

    @SerializedName("aidNotesList")
    @Expose
    List<String> aidNotesList;

    @SerializedName("totalCost")
    @Expose
    String totalCost;

    @SerializedName("vendorDiscount")
    @Expose
    String vendorDiscount;

    @SerializedName("coveredCost")
    @Expose
    String coveredCost;

    @SerializedName("outOfPocket")
    @Expose
    String outOfPocket;

    @SerializedName("otherAids")
    @Expose
    String otherAids;

    @SerializedName("documentsList")
    @Expose
    private List<String> documentsList;

    @SerializedName("hwUuid")
    @Expose
    private String hwUuid;
    @SerializedName("hwName")
    @Expose
    private String hwName;

    @SerializedName("dateTime")
    @Expose
    private String dateTime;

    public AidModel() {
    }

    public List<String> getAidUuidList() {
        return aidUuidList;
    }

    public void setAidUuidList(List<String> aidUuidList) {
        this.aidUuidList = aidUuidList;
    }

    public List<String> getAidNotesList() {
        return aidNotesList;
    }

    public void setAidNotesList(List<String> aidNotesList) {
        this.aidNotesList = aidNotesList;
    }

    public String getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(String totalCost) {
        this.totalCost = totalCost;
    }

    public String getVendorDiscount() {
        return vendorDiscount;
    }

    public void setVendorDiscount(String vendorDiscount) {
        this.vendorDiscount = vendorDiscount;
    }

    public String getCoveredCost() {
        return coveredCost;
    }

    public void setCoveredCost(String coveredCost) {
        this.coveredCost = coveredCost;
    }

    public String getOutOfPocket() {
        return outOfPocket;
    }

    public void setOutOfPocket(String outOfPocket) {
        this.outOfPocket = outOfPocket;
    }

    public String getOtherAids() {
        return otherAids;
    }

    public void setOtherAids(String otherAids) {
        this.otherAids = otherAids;
    }

    public List<String> getDocumentsList() {
        return documentsList;
    }

    public void setDocumentsList(List<String> documentsList) {
        List<String> withWebUrlDocList = new ArrayList<>();
        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        for (String doc : documentsList) {
            withWebUrlDocList.add("https://" + sessionManager.getServerUrl() + "/openmrs/ws/rest/v1/obs/" + doc + "/value");
        }
        this.documentsList = withWebUrlDocList;
//        https://training.sila.care/openmrs/ws/rest/v1/obs/
    }

    public String getHwUuid() {
        return hwUuid;
    }

    public void setHwUuid(String hwUuid) {
        this.hwUuid = hwUuid;
    }

    public String getHwName() {
        return hwName;
    }

    public void setHwName(String hwName) {
        this.hwName = hwName;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}

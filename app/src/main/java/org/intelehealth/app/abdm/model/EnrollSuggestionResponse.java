package org.intelehealth.app.abdm.model;

/**
 * Created by - Prajwal W. on 05/02/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EnrollSuggestionResponse {

    @SerializedName("txnId")
    @Expose
    private String txnId;
    @SerializedName("abhaAddressList")
    @Expose
    private List<String> abhaAddressList;

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public List<String> getAbhaAddressList() {
        return abhaAddressList;
    }

    public void setAbhaAddressList(List<String> abhaAddressList) {
        this.abhaAddressList = abhaAddressList;
    }

    @Override
    public String toString() {
        return "EnrollSuggestionResponse{" +
                "txnId='" + txnId + '\'' +
                ", abhaAddressList=" + abhaAddressList +
                '}';
    }
}
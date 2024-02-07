package org.intelehealth.app.abdm.model;

/**
 * Created by - Prajwal W. on 05/02/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
public class EnrollSuggestionRequestBody {

    @SerializedName("txnId")
    @Expose
    private String txnId;
    @SerializedName("abhaAddress")
    @Expose
    private String abhaAddress;

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getAbhaAddress() {
        return abhaAddress;
    }

    public void setAbhaAddress(String abhaAddress) {
        this.abhaAddress = abhaAddress;
    }

    @Override
    public String toString() {
        return "EnrollSuggestionRequestBody{" +
                "txnId='" + txnId + '\'' +
                ", abhaAddress='" + abhaAddress + '\'' +
                '}';
    }
}

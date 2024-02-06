package org.intelehealth.app.abdm.model;

/**
 * Created by - Prajwal W. on 06/02/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AbhaProfileRequestBody {

    @SerializedName("txnId")
    @Expose
    private String txnId;
    @SerializedName("abhaNumber")
    @Expose
    private String abhaNumber;

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getAbhaNumber() {
        return abhaNumber;
    }

    public void setAbhaNumber(String abhaNumber) {
        this.abhaNumber = abhaNumber;
    }

}

package org.intelehealth.msfarogyabharat.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MissedCallModel {
    @SerializedName("data")
    @Expose
    private List<Datum> data = null;
    @SerializedName("status")
    @Expose
    private String status;

    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public class Datum {

        @SerializedName("Noanswer")
        @Expose
        private String noanswer;

        public String getNoanswer() {
            return noanswer;
        }

        public void setNoanswer(String noanswer) {
            this.noanswer = noanswer;
        }
    }

}


package app.intelehealth.client.models.pushResponseApiCall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PushResponseApiCall {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("data")
    @Expose
    private Data data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

}

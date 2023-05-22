package org.intelehealth.unicef.models.providerImageRequestModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProviderProfile {

    @SerializedName("providerid")
    @Expose
    private String providerid;

    public String getProviderid() {
        return providerid;
    }

    public void setProviderid(String providerid) {
        this.providerid = providerid;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @SerializedName("file")
    @Expose
    private String file;
}

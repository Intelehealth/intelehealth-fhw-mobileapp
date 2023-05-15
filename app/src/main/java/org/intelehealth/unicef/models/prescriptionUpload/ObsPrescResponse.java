package org.intelehealth.unicef.models.prescriptionUpload;

/**
 * Created by Prajwal Maruti Waingankar on 22-12-2021, 16:34
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ObsPrescResponse {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("display")
    @Expose
    private String display;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

}
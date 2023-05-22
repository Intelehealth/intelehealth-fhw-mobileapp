package org.intelehealth.unicef.models.prescriptionUpload;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Prajwal Maruti Waingankar on 20-01-2022, 16:24
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

public class EncounterProvider {

    @SerializedName("encounterRole")
    @Expose
    private String encounterRole;
    @SerializedName("provider")
    @Expose
    private String provider;

    public String getEncounterRole() {
        return encounterRole;
    }

    public void setEncounterRole(String encounterRole) {
        this.encounterRole = encounterRole;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

}

package org.intelehealth.app.models.prescriptionUpload;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.intelehealth.app.models.ClsDoctorDetails;

/**
 * Created by Prajwal Maruti Waingankar on 20-01-2022, 16:25
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

public class Ob {

    @SerializedName("concept")
    @Expose
    private String concept;
    @SerializedName("value")
    @Expose
    private String value;

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}

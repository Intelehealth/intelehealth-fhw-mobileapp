package org.intelehealth.app.models.patientImageModelRequest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Prajwal Waingankar
 * on March 2023.
 * Github: prajwalmw
 */
public class ADPImageModel {

    @SerializedName("file")
    @Expose
    private String file;
    @SerializedName("patientuuid")
    @Expose
    private String patientuuid;
    @SerializedName("f_name")
    @Expose
    private String fName;

    private String filePath;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getPatientuuid() {
        return patientuuid;
    }

    public void setPatientuuid(String patientuuid) {
        this.patientuuid = patientuuid;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
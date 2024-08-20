package org.intelehealth.vikalphelpline.models.ObsImageModel;

/**
 * Created By: Prajwal Waingankar on 24-Aug-21
 * Github: prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class Add_Doc_DataModel {
    private String obsuuid;
    private String filename;


    public Add_Doc_DataModel(String obsuuid, String filename) {
        this.obsuuid = obsuuid;
        this.filename = filename;
    }

    public String getObsuuid() {
        return obsuuid;
    }

    public void setObsuuid(String obsuuid) {
        this.obsuuid = obsuuid;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}

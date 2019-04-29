
package io.intelehealth.client.dto;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "tbl_patientattributesmaster")
public class PatientAttributeTypeMasterDTO {
    @PrimaryKey
    @NonNull
    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("name")
    @Expose
    private String name;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

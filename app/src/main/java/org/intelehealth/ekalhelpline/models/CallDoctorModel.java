package org.intelehealth.ekalhelpline.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CallDoctorModel {

    @SerializedName("success")
    @Expose
    private boolean success;

    @SerializedName("count")
    @Expose
    private int list_count;

    @SerializedName("data")
    @Expose
    private List<DoctorDetailsModel> doctorList;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getList_count() {
        return list_count;
    }

    public void setList_count(int list_count) {
        this.list_count = list_count;
    }

    public List<DoctorDetailsModel> getDoctorList() {
        return doctorList;
    }

    public void setDoctorList(List<DoctorDetailsModel> doctorList) {
        this.doctorList = doctorList;
    }
}


package org.intelehealth.msfarogyabharat.models.prescribed_medications_model;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Hi {

    @Expose
    private List<Datum> data;

    @SerializedName("meal_type")
    private String mealType;

    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
        this.data = data;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

}

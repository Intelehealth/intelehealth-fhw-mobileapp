package org.intelehealth.ezazi.partogram.model;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.intelehealth.klivekit.chat.model.ItemHeader;

import java.io.Serializable;

/**
 * Created by Vaghela Mithun R. on 31-08-2023 - 13:28.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class Medication implements Serializable, ItemHeader {
    @SerializedName("type")
    private String type;

    @SerializedName("strength")
    private String strength;

    @SerializedName("infusionRate")
    private String infusionRate;

    @SerializedName("infusionStatus")
    private String infusionStatus;
    private String createdAt;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getInfusionRate() {
        return infusionRate;
    }

    public void setInfusionRate(String infusionRate) {
        this.infusionRate = infusionRate;
    }

    public String getInfusionStatus() {
        return infusionStatus;
    }

    public void setInfusionStatus(String infusionStatus) {
        this.infusionStatus = infusionStatus;
    }

    public boolean isValidIVFluid() {
        return type != null && type.length() > 0 && isValidInfusion();
    }

    public boolean isValidOxytocin() {
        return strength != null && strength.length() > 0 && isValidInfusion();
    }

    private boolean isValidInfusion() {
        return infusionRate != null && infusionRate.length() > 0
                && infusionStatus != null && infusionStatus.length() > 0;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public boolean isHeader() {
        return false;
    }

    @NonNull
    @Override
    public String createdDate() {
        return createdAt;
    }
}

package org.intelehealth.ezazi.partogram.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.klivekit.chat.model.ItemHeader;
import org.intelehealth.klivekit.utils.DateTimeUtils;

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
    @SerializedName("otherType")
    private String otherType;

    public String getCreatedAt() {
        return DateTimeUtils.utcToLocalDate(createdAt, AppConstants.UTC_FORMAT, AppConstants.VISIT_FORMAT);
    }

    public String getOtherType() {
        return otherType;
    }

    public void setOtherType(String otherType) {
        this.otherType = otherType;
    }


    private Medication medication;

    @SerializedName("infusionStatus")
    private String infusionStatus;
    private String createdAt;
    private String obsUuid;

    public String getObsUuid() {
        return obsUuid;
    }

    public void setObsUuid(String obsUuid) {
        this.obsUuid = obsUuid;
    }

    private String creatorName;

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

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
        return infusionRate != null && infusionRate.length() > 0 && infusionStatus != null && infusionStatus.length() > 0;
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

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String dateWithDrName() {
        return creatorName + " " + createdAt;
    }

    public String content() {
        if (type != null && !type.isEmpty()) {
            return type + ", Infusion Rate: " + infusionRate;
        } else {
            return "Strength: " + strength + ", Infusion Rate: " + infusionRate;
        }
    }
}

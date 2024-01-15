package org.intelehealth.ezazi.partogram.model;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.utilities.UuidDictionary;

import java.io.Serializable;

/**
 * Created by Vaghela Mithun R. on 06-09-2023 - 17:14.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/

public class Medicine implements Serializable {
    private String obsUuid;
    private String name;
    private String strength;
    private String dosage;
    @SerializedName("doseUnit")
    private String dosageUnit;
    private String route;
    private String frequency;

    private String form;
    private String duration;
    private String durationUnit;

    public String getMedicineFullName() {
        return medicineFullName;
    }

    public void setMedicineFullName(String medicineFullName) {
        this.medicineFullName = medicineFullName;
    }

    private String remark;
    private String medicineFullName;

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDurationUnit() {
        return durationUnit;
    }

    public void setDurationUnit(String durationUnit) {
        this.durationUnit = durationUnit;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    private String type = "Tablet";

    public void setObsUuid(String obsUuid) {
        this.obsUuid = obsUuid;
    }

    public String getObsUuid() {
        return obsUuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getDosageUnit() {
        return dosageUnit;
    }

    public void setDosageUnit(String dosageUnit) {
        this.dosageUnit = dosageUnit;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String toDBFormat() {
        ///NEW format ::  `<Form> | <MedicineName> | <Strength> | <Dosage>::<DosageUnit> | <Frequency> | <RouteOfMedicine> | <Duration>::<DurationUnit> | <Remark>`
        // old format ::  <medicineName> | <strength> | <dosage>::<dosageUnit> | <duration> | <typeOfMedicine> | <routeOfMedicine>
        /*return name + " | " +
                strength + " | " +
                dosage + "::" + dosageUnit + " | " +
                frequency + " | " +
                type + " | " +
                route;*/

        return form + " | " + name + " | " + strength + " | " +
                dosage + "::" + dosageUnit + " | " + frequency + " | " +
                route + " | " + duration + "::" + durationUnit + getRemarkValue();
    }

    private String getRemarkValue() {
        return remark != null && remark.length() > 0 ? " | " + remark : "";
    }

    public boolean isValidMedicine() {
        return name != null && name.length() > 0 && strength != null
                && strength.length() > 0 && dosage != null && dosage.length() > 0 &&
                dosageUnit != null && dosageUnit.length() > 0 && frequency != null &&
                frequency.length() > 0 && form != null && form.length() > 0 && route != null &&
                route.length() > 0 && duration != null && duration.length() > 0 && durationUnit != null
                && durationUnit.length() > 0;
    }

    public void dbFormatToMedicineObject(String data) {
        if (data != null && data.length() > 0) {
            String[] params = data.split("\\|");
            ///NEW format ::  `<Form> | <MedicineName> | <Strength> | <Dosage>::<DosageUnit> || <Frequency> | <RouteOfMedicine> | <Duration>::<DurationUnit> | <Remark>`
         /*   name = params[0].trim();
            strength = params[1].trim();
            dosage = params[2].trim().split("::")[0];
            dosageUnit = params[2].trim().split("::")[1];
            frequency = params[3].trim();
            type = params[4].trim();
            route = params[5].trim();*/
            form = params[0].trim();
            name = params[1].trim();
            strength = params[2].trim();
            dosage = params[3].trim().split("::")[0];
            dosageUnit = params[3].trim().split("::")[1];
            frequency = params[4].trim();
            route = params[5].trim();
            duration = params[6].trim().split("::")[0];
            durationUnit = params[6].trim().split("::")[1];
            if (params.length > 7)
                remark = params[7].trim();

        }
    }

    public ObsDTO toObs(String encounterId, String creator) {
        ObsDTO obs = new ObsDTO();
        obs.setUuid(obsUuid);
        obs.setConceptuuid(UuidDictionary.MEDICINE);
        obs.setValue(toDBFormat().trim());
        obs.setCreator(creator);
        obs.setEncounteruuid(encounterId);
        Log.e("Medicine", "toObs: " + new Gson().toJson(obs));
        return obs;
    }
}
package org.intelehealth.ezazi.partogram.model;

import android.util.Log;

import com.google.gson.Gson;

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
    private String dosageUnit;
    private String route;
    private String frequency;

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
        // format <medicineName> | <strength> | <dosage>::<dosageUnit> | <duration> | <typeOfMedicine> | <routeOfMedicine>
        return name + " | " +
                strength + " | " +
                dosage + "::" + dosageUnit + " | " +
                frequency + " | " +
                type + " | " +
                route;
    }

    public boolean isValidMedicine() {
        return name != null && name.length() > 0
                && strength != null && strength.length() > 0
                && dosage != null && dosage.length() > 0
                && dosageUnit != null && dosageUnit.length() > 0
                && frequency != null && frequency.length() > 0
                && type != null && type.length() > 0
                && route != null && route.length() > 0;
    }

    public void dbFormatToMedicineObject(String data) {
        if (data != null && data.length() > 0) {
            String[] params = data.split("\\|");
            name = params[0].trim();
            strength = params[1].trim();
            dosage = params[2].trim().split("::")[0];
            dosageUnit = params[2].trim().split("::")[1];
            frequency = params[3].trim();
            type = params[4].trim();
            route = params[5].trim();
        }
    }

    public ObsDTO toObs(String encounterId, String creator) {
        ObsDTO obs = new ObsDTO();
        obs.setUuid(obsUuid);
        obs.setConceptuuid(UuidDictionary.MEDICINE);
        obs.setValue(toDBFormat());
        obs.setCreator(creator);
        obs.setEncounteruuid(encounterId);
        Log.e("Medicine", "toObs: " + new Gson().toJson(obs));
        return obs;
    }
}

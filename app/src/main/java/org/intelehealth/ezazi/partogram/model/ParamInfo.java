package org.intelehealth.ezazi.partogram.model;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.intelehealth.ezazi.database.dao.ObsDAO;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.partogram.PartogramConstants;
import org.intelehealth.ezazi.partogram.adapter.PartogramQueryListingAdapter;
import org.intelehealth.ezazi.utilities.DateAndTimeUtils;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.UuidDictionary;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ParamInfo implements Serializable {
    private String paramSectionName;
    private String paramName;
    private String paramDateType;
    private String[] options;
    private String[] values;
    private boolean isHalfHourField;
    private boolean isFifteenMinField;
    private boolean isOnlyOneHourField;
    private String capturedValue;
    private String conceptUUID;

    private int currentStage;

    private String[] radioOptions;
    private String[] status;
    private boolean eachEncounterField;
//    private HashMap<String, String> jsonMap;

    private Medication medication;
    private List<Medication> medicationList;

    public boolean isEachEncounterField() {
        return eachEncounterField;
    }

    public void setEachEncounterField(boolean eachEncounterField) {
        this.eachEncounterField = eachEncounterField;
    }

    private RadioOptions checkedRadioOption = RadioOptions.NO_VALUE;

    private List<Medicine> medicines;
    private List<Medication> ivFluidsList;
    private List<Medication> oxytocinList;
    private List<ObsDTO> plansList;
    private List<ObsDTO> assessmentsList;

    public boolean isFiveHourField() {
        return isFiveHourField;
    }

    public void setFiveHourField(boolean fiveHourField) {
        isFiveHourField = fiveHourField;
    }

    private List<Medicine> deletedMedicines;
    private boolean isFiveHourField;
    private List<Medicine> prescribedMedicines;
    private List<ObsDTO> deletedPlans;
    private String createdDate;

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public enum RadioOptions {
        YES, NO, NO_VALUE
    }

    public String getParamSectionName() {
        return paramSectionName;
    }

    public void setParamSectionName(String paramSectionName) {
        this.paramSectionName = paramSectionName;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamDateType() {
        return paramDateType;
    }

    public void setParamDateType(String paramDateType) {
        this.paramDateType = paramDateType;
    }

    public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    public boolean isHalfHourField() {
        return isHalfHourField;
    }

    public void setHalfHourField(boolean halfHourField) {
        isHalfHourField = halfHourField;
    }

    public boolean isFifteenMinField() {
        return isFifteenMinField;
    }

    public void setFifteenMinField(boolean fifteenMinField) {
        isFifteenMinField = fifteenMinField;
    }

    public String getCapturedValue() {
        return capturedValue;
    }

    public void setCapturedValue(String capturedValue) {
        this.capturedValue = capturedValue;
    }

    public String getConceptUUID() {
        return conceptUUID;
    }

    public void setConceptUUID(String conceptUUID) {
        this.conceptUUID = conceptUUID;
    }

    public String[] getStatus() {
        return status;
    }

    public void setStatus(String[] status) {
        this.status = status;
    }

    public boolean isOnlyOneHourField() {
        return isOnlyOneHourField;
    }

    public void setOnlyOneHourField(boolean onlyOneHourField) {
        isOnlyOneHourField = onlyOneHourField;
    }

    public void setCurrentStage(int currentStage) {
        this.currentStage = currentStage;
    }

    public int getCurrentStage() {
        return currentStage;
    }

    public void setRadioOptions(String[] radioOptions) {
        this.radioOptions = radioOptions;
    }

    public String[] getRadioOptions() {
        return radioOptions;
    }

    public void setMedication(Medication medication) {
        this.medication = medication;
    }

    public Medication getMedication() {
        if (medication == null) medication = new Medication();
        return medication;
    }

    public void saveJson() {
        if ((getConceptUUID().equals(UuidDictionary.IV_FLUIDS) && getMedication().isValidIVFluid()) || (getConceptUUID().equals(UuidDictionary.OXYTOCIN_UL_DROPS_MIN) && getMedication().isValidOxytocin()))
            setCapturedValue(getMedication().toJson());
        else setCapturedValue(RadioOptions.NO.name());
    }

//    public void saveValueInJsonMap(String key, String value) {
//        if (jsonMap == null) jsonMap = new HashMap<>();
//        jsonMap.put(key, value);
//    }
//
//    public void convertJsonMapToJSONAndSaveCapturedValue() {
//        if (jsonMap == null) return;
//        setCapturedValue(new Gson().toJson(jsonMap));
//    }

    public boolean isValidJson() {
        Log.e("ParamInfo", "isValidJson: " + getParamName() + " checked: " + checkedRadioOption);
        Log.d("ParamInfo", "isValidJson:getConceptUUID():: " + getConceptUUID());
        Log.d("ParamInfo", "isValidJson:getCapturedValue():: " + getCapturedValue());

        if (getConceptUUID().equals(UuidDictionary.IV_FLUIDS) || getConceptUUID().equals(UuidDictionary.OXYTOCIN_UL_DROPS_MIN)) {
            if (getCapturedValue() == null) return true;
            else if (checkedRadioOption == RadioOptions.NO) return true;
            else if (checkedRadioOption == RadioOptions.NO_VALUE) return true;
            else {
                if (checkedRadioOption != null && getConceptUUID().equals(UuidDictionary.IV_FLUIDS)) {
                    return getMedication().isValidIVFluid();
                } else if (checkedRadioOption != null && getConceptUUID().equals(UuidDictionary.OXYTOCIN_UL_DROPS_MIN)) {
                    return getMedication().isValidOxytocin();
                }
            }
        }
        return true;
    }

    public boolean isValidMedicine() {
        Log.e("ParamInfo", "isValidJson: " + getParamName() + " checked: " + checkedRadioOption);
        Log.e("ParamInfo", "isValidJson: " + getParamName() + " value: " + getMedication().toJson());
        if (getConceptUUID().equals(UuidDictionary.MEDICINE)) {
            if (getCapturedValue() == null) return true;
            else if (checkedRadioOption == RadioOptions.NO) return true;
            else if (checkedRadioOption == RadioOptions.NO_VALUE) return true;
            else {
                return checkedRadioOption == RadioOptions.YES && medicines.size() > 0;
            }
        }
        return true;
    }

    public void setCheckedRadioOption(RadioOptions checkedRadioOption) {
        this.checkedRadioOption = checkedRadioOption;
    }

    public RadioOptions getCheckedRadioOption() {
        return checkedRadioOption;
    }

    public void setMedicines(List<Medicine> medicines) {
        this.medicines = medicines;
    }

    public List<Medicine> getMedicines() {
        if (medicines == null) medicines = new ArrayList<>();
        return medicines;
    }

    public List<Medicine> getPrescribedMedicines() {
        if (prescribedMedicines == null) prescribedMedicines = new ArrayList<>();
        return prescribedMedicines;
    }

    public void convertToMedicine(String obsUuid, String value, String createdDate) {
        Log.d("TAG", "convertToMedicine: value :: " + value);
        Medicine medicine = new Medicine();
        medicine.setObsUuid(obsUuid);
        medicine.dbFormatToMedicineObject(value);
        medicine.setCreatedAt(createdDate);
       //String creatorName = new ObsDAO().getCreatorNameByObsUuidMedicine(obsUuid);
        String creatorName = new ObsDAO().getCreatorNameByObsUuid(obsUuid);
        medicine.setCreatorName(creatorName);
        getMedicines().add(medicine);
    }

    public void setDeletedMedicines(List<Medicine> deletedMedicines) {
        this.deletedMedicines = deletedMedicines;
    }

    public List<Medicine> getDeletedMedicines() {
        if (deletedMedicines == null) deletedMedicines = new ArrayList<>();
        if (checkedRadioOption == RadioOptions.NO && getMedicines().size() > 0) {
            deletedMedicines.addAll(getMedicines());
        }
        return deletedMedicines;
    }

    public List<ObsDTO> getMedicinesObsList(String encounterId, String creator) {
        ArrayList<ObsDTO> obsList = new ArrayList<>();
        for (Medicine medicine : getMedicines()) {

            obsList.add(medicine.toObs(encounterId, creator, medicine.getCreatedAt()));
        }
        return obsList;
    }

    public List<String> getVoidedMedicineUuid() {
        ArrayList<String> voided = new ArrayList<>();
        for (Medicine med : getDeletedMedicines()) {
            if (med.getObsUuid() != null && med.getObsUuid().length() > 0) {
                voided.add(med.getObsUuid());
            }
        }
        return voided;
    }

    public boolean isValidBaselineFHR() {
        Log.e("ParamInfo", "isValidJson: " + getParamName() + " checked: " + checkedRadioOption);
        if (getConceptUUID().equals(UuidDictionary.BASELINE_FHR)) {
            if (getCapturedValue() == null) return true;
            else if (checkedRadioOption == RadioOptions.NO_VALUE) return true;
            else {
                if (checkedRadioOption != null && getConceptUUID().equals(UuidDictionary.OXYTOCIN_UL_DROPS_MIN)) {
                    return getMedication().isValidOxytocin();
                }
            }
        }
        return true;
    }

    public void convertToPrescribedMedicine(String obsUuid, String value) {
        Log.d("TAG", "convertToPrescribedMedicine: value :: " + value);
        Medicine medicine = new Medicine();
        medicine.setObsUuid(obsUuid);
        medicine.dbFormatToMedicineObject(value);
        getPrescribedMedicines().add(medicine);
    }

    /*  public void convertToIvFluidMedication(String obsUuid, String value) {
          Log.d("TAG", "convertToIvFluidMedication: value :: " + value);
          Medication medication = new Medication();
          medication.setObsUuid(obsUuid);
          medication.getMedication(value);
          getMedicines().add(medication);
      }*/


    public Medication getMedication(String obsUUid, String value, String createdDate, String conceptUUID) {
        Log.d("TAG", "getMedication: value : " + value);
        Log.d("TAG", "getMedication: createdDate : " + createdDate);

        try {
            Gson gson = new Gson();
            Medication medicationData = gson.fromJson(value, Medication.class);
            if (medicationData == null) {
                medicationData = getMedication();
            }
            Medication medication = new Medication();
            medication = gson.fromJson(value, Medication.class);
            medication.setObsUuid(obsUUid);

            medication.setCreatedAt(createdDate);

            String status = medicationData.getInfusionStatus();
            String statusAdminister = "";
            if (status.equalsIgnoreCase("start")) {
                statusAdminister = "Started";
            } else if (status.equalsIgnoreCase("continue")) {
                statusAdminister = "Continued";
            } else if (status.equalsIgnoreCase("stop")) {
                statusAdminister = "Stopped";
            }
            Log.d("TAG", "getMedication: conceptUUID : " + conceptUUID);
            medicationData.setInfusionStatus(statusAdminister);
            //medication.setInfusionStatus(statusAdminister);

            //medication = getMedication();
            if (conceptUUID != null && !conceptUUID.isEmpty()) {
                if (conceptUUID.equals(UuidDictionary.IV_FLUIDS)) {
                    String ivFluidType = medication.getType();

                    if (ivFluidType.equals("Ringer Lactate") || ivFluidType.equals("Normal Saline") || ivFluidType.equals("Dextrose 5% (D5)")) {
                        medication.setType(ivFluidType);
                    } else {
                        medication.setType("Other");
                        medication.setOtherType(medication.getOtherType());
                    }
                    getMedicationsForFluid().add(medication);

                } else if (conceptUUID.equals(UuidDictionary.OXYTOCIN_UL_DROPS_MIN)) {
                    String strength = medicationData.getStrength() + " (U/L)";
                    medicationData.setStrength(strength);
                    medication.setStrength(strength);
                    getMedicationsForOxytocin().add(medication);
                    List<Medication> datalist = getMedicationsForOxytocin();
                    Log.d("TAG", "getMedication: datalist: " + new Gson().toJson(datalist));
                }
            }
            return medicationData;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return getMedication();
        }
    }


    public List<Medication> getMedicationsForFluid() {
        if (ivFluidsList == null) ivFluidsList = new ArrayList<>();
        return ivFluidsList;
    }

    public void setMedicationsForFluid(List<Medication> ivFluidsList) {
        this.ivFluidsList = ivFluidsList;
    }

    private String formatDateTime(String inputDateTime) {
        Log.d("TAG", "formatDateTime: inputDateTime : " + inputDateTime);
        try {
            // Input format
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            // Output format
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());

            // Parse input datetime
            Date date = inputFormat.parse(inputDateTime);

            // Format the date
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return ""; // Handle the exception according to your requirements
        }
    }

    public String formatDateTimeNew(String inputDateString) {
        String formattedDate = "";
        if (inputDateString != null && !inputDateString.isEmpty()) {
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
            Date date;
            try {
                date = inputDateFormat.parse(inputDateString);
            } catch (ParseException e) {
                e.printStackTrace();
                return "";
            }

            SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault());
            formattedDate = outputDateFormat.format(date);
            return formattedDate;
        }
        return formattedDate;

    }


    public List<Medication> getMedicationsForOxytocin() {
        if (oxytocinList == null) oxytocinList = new ArrayList<>();
        return oxytocinList;
    }

    public void setMedicationsForOxytocin(List<Medication> oxytocinList) {
        this.oxytocinList = oxytocinList;
    }

    public void setPlans(List<ObsDTO> plansList) {
        this.plansList = plansList;
    }

    public List<ObsDTO> getPlans() {
        if (plansList == null) plansList = new ArrayList<>();
        return plansList;
    }

    public void collectAllPlansInList(String obsUuid, String value, String createdDate) {
        Log.d("TAG", "collectAllPlansInList:createdDate: " + createdDate);
        try {
            ObsDTO plan = new ObsDTO();
            Log.d("TAG", "collectAllPlansInList: obsUuid: " + obsUuid);
            plan.setUuid(obsUuid);
            plan.setValue(value);
            //String createdAt = formatDateTimeNew(createdDate);
            plan.setCreatedDate(createdDate);
            String creatorName = new ObsDAO().getCreatorNameByObsUuid(obsUuid);
            plan.setName(creatorName);
            getPlans().add(plan);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "collectAllPlansInList: " + e.getLocalizedMessage());
        }

    }

    public List<ObsDTO> getPlansObsList(String encounterId, String creator) {
        ArrayList<ObsDTO> obsList = new ArrayList<>();
        for (ObsDTO obsDTO : getPlans()) {

            obsList.add(obsDTO.toObs(encounterId, creator, obsDTO.getCreatedDate(true), UuidDictionary.PLAN));
        }
        return obsList;
    }

    public List<String> getVoidedPlansUuid() {
        ArrayList<String> voided = new ArrayList<>();
        for (ObsDTO obsDTO : getDeletedPlans()) {
            if (obsDTO.getUuid() != null && obsDTO.getUuid().length() > 0) {
                voided.add(obsDTO.getUuid());
            }
        }
        return voided;
    }

    public void setDeletedPlans(List<ObsDTO> deletedPlans) {
        this.deletedPlans = deletedPlans;
    }

    public List<ObsDTO> getDeletedPlans() {
        if (deletedPlans == null) deletedPlans = new ArrayList<>();
        //if (checkedRadioOption == RadioOptions.NO && getMedicines().size() > 0) {
        if (getPlans().size() > 0) {
            deletedPlans.addAll(getPlans());
        }
        return deletedPlans;
    }

    public void collectAllAssessmentsInList(String obsUuid, String value, String createdDate) {
        Log.d("TAG", "collectAllAssessmentsInList:createdDate: " + createdDate);
        try {
            ObsDTO assessment = new ObsDTO();
            Log.d("TAG", "collectAllAssessmentsInList: obsUuid: " + obsUuid);
            assessment.setUuid(obsUuid);
            assessment.setValue(value);
            //String createdAt = formatDateTimeNew(createdDate);
            assessment.setCreatedDate(createdDate);
            String creatorName = new ObsDAO().getCreatorNameByObsUuid(obsUuid);
            assessment.setName(creatorName);
            getAssessments().add(assessment);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "collectAllAssessmentsInList: " + e.getLocalizedMessage());
        }

    }

    public List<ObsDTO> getAssessments() {
        if (assessmentsList == null) assessmentsList = new ArrayList<>();
        return assessmentsList;
    }

    public void setAssessments(List<ObsDTO> assessmentsList) {
        this.assessmentsList = assessmentsList;
    }

    public List<ObsDTO> getAssessmentsObsList(String encounterId, String creator) {
        ArrayList<ObsDTO> obsList = new ArrayList<>();
        for (ObsDTO obsDTO : getAssessments()) {

            obsList.add(obsDTO.toObs(encounterId, creator, obsDTO.getCreatedDate(true), UuidDictionary.ASSESSMENT));
        }
        return obsList;
    }
}

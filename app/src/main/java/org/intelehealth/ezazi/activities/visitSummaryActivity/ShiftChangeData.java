package org.intelehealth.ezazi.activities.visitSummaryActivity;

import com.google.gson.annotations.SerializedName;

import org.intelehealth.ezazi.models.FamilyMemberRes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kaveri Zaware on 22-09-2023
 * email - kaveri@intelehealth.org
 **/
public class ShiftChangeData {

    @SerializedName("toHwUserUuid")
    private String toHwUserUuid;

    @SerializedName("providerID")
    private String providerID;

    @SerializedName("tag")
    private String tag;

    @SerializedName("assignorNurse")
    private String assignorNurse;

    private String patients;

    private String visitIds;

    private String openMrsIds;

    public String getProviderID() {
        return providerID;
    }

    public void setProviderID(String providerID) {
        this.providerID = providerID;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getAssignorNurse() {
        return assignorNurse;
    }

    public void setAssignorNurse(String assignorNurse) {
        this.assignorNurse = assignorNurse;
    }

    public void generatePatientsInfo(List<FamilyMemberRes> patients) {
        StringBuilder patientBuilder = new StringBuilder();
        StringBuilder visitBuilder = new StringBuilder();
        StringBuilder openMrsBuilder = new StringBuilder();
        if (patients != null && patients.size() > 0) {
            patientBuilder.append(patients.get(0).getName());
            visitBuilder.append(patients.get(0).getVisitUuid());
            openMrsBuilder.append(patients.get(0).getOpenMRSID());

            for (int i = 1; i < patients.size(); i++) {
                patientBuilder.append(",").append(patients.get(i).getName());
                visitBuilder.append(",").append(patients.get(i).getVisitUuid());
                openMrsBuilder.append(",").append(patients.get(i).getOpenMRSID());
            }

            this.patients = patientBuilder.toString();
            this.visitIds = visitBuilder.toString();
            this.openMrsIds = openMrsBuilder.toString();
        }
    }


    public String getToHwUserUuid() {
        return toHwUserUuid;
    }

    public void setToHwUserUuid(String toHwUserUuid) {
        this.toHwUserUuid = toHwUserUuid;
    }

    public String getTitle() {
        if (patients != null && buildPatients().size() > 1)
            return "New patients shifted to you";
        else return "New patient shifted to you";
    }

    public String getBody() {
        StringBuilder builder = new StringBuilder();
        builder.append(assignorNurse);
        if (patients != null && buildPatients().size() > 1)
            builder.append(" has shifted some patients to you ");
        else builder.append(" has shifted a patient to you ");
        builder.append("\n");
        for (FamilyMemberRes patient : buildPatients()) {
            builder.append("•").append(" ").append(patient.getName())
                    .append(" - ").append(patient.getOpenMRSID()).append("\n");
        }
        return builder.toString();
    }

    public List<FamilyMemberRes> buildPatients() {
        String[] names = this.patients.split(",");
        String[] visits = this.visitIds.split(",");
        String[] openMrsIds = this.openMrsIds.split(",");
        if (names.length == visits.length && visits.length == openMrsIds.length) {
            if (names.length > 0) {
                return getPatients(names, visits, openMrsIds);
            }
        }
        return new ArrayList<>();
    }

    private List<FamilyMemberRes> getPatients(String[] names, String[] visits, String[] openMrsIds) {
        List<FamilyMemberRes> patients = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            FamilyMemberRes patient = new FamilyMemberRes();
            patient.setName(names[i]);
            patient.setVisitUuid(visits[i]);
            patient.setOpenMRSID(openMrsIds[0]);
            patients.add(patient);
        }
        return patients;
    }
}

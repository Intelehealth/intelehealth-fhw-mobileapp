
package org.intelehealth.app.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.intelehealth.config.network.response.ConfigResponse;

import java.util.List;

public class Data {

    @SerializedName("patientlist")
    @Expose
    private List<PatientDTO> patientDTO = null;
    @SerializedName("pullexecutedtime")
    @Expose
    private String pullexecutedtime;
    @SerializedName("patientAttributeTypeListMaster")
    @Expose
    private List<PatientAttributeTypeMasterDTO> patientAttributeTypeMasterDTO = null;
    @SerializedName("patientAttributesList")
    @Expose
    private List<PatientAttributesDTO> patientAttributesDTO = null;
    @SerializedName("visitlist")
    @Expose
    private List<VisitDTO> visitDTO = null;
    @SerializedName("encounterlist")
    @Expose
    private List<EncounterDTO> encounterDTO = null;
    @SerializedName("obslist")
    @Expose
    private List<ObsDTO> obsDTO = null;
    @SerializedName("locationlist")
    @Expose
    private List<LocationDTO> locationDTO = null;
    @SerializedName("providerlist")
    @Expose
    private List<ProviderDTO> providerlist = null;
    @SerializedName("providerAttributeTypeList")
    @Expose
    private List<Object> providerAttributeTypeList = null;
    @SerializedName("providerAttributeList")
    @Expose
    private List<ProviderAttributeListDTO> providerAttributeList = null;
    @SerializedName("visitAttributeTypeList")
    @Expose
    private List<VisitAttributeTypeDTO> visitAttributeTypeList = null;
    @SerializedName("visitAttributeList")
    @Expose
    private List<VisitAttributeDTO> visitAttributeList = null;

    @SerializedName("pageNo")
    @Expose
    private int pageNo;
    @SerializedName("totalCount")
    @Expose
    private int totalCount;

    @SerializedName("propertyContents")
    private ConfigResponse configResponse;



    public List<PatientDTO> getPatientDTO() {
        return patientDTO;
    }

    public void setPatientDTO(List<PatientDTO> patientDTO) {
        this.patientDTO = patientDTO;
    }

    public String getPullexecutedtime() {
        return pullexecutedtime;
    }

    public void setPullexecutedtime(String pullexecutedtime) {
        this.pullexecutedtime = pullexecutedtime;
    }

    public List<PatientAttributeTypeMasterDTO> getPatientAttributeTypeMasterDTO() {
        return patientAttributeTypeMasterDTO;
    }

    public void setPatientAttributeTypeMasterDTO(List<PatientAttributeTypeMasterDTO> patientAttributeTypeMasterDTO) {
        this.patientAttributeTypeMasterDTO = patientAttributeTypeMasterDTO;
    }

    public List<PatientAttributesDTO> getPatientAttributesDTO() {
        return patientAttributesDTO;
    }

    public void setPatientAttributesDTO(List<PatientAttributesDTO> patientAttributesDTO) {
        this.patientAttributesDTO = patientAttributesDTO;
    }

    public List<VisitDTO> getVisitDTO() {
        return visitDTO;
    }

    public void setVisitDTO(List<VisitDTO> visitDTO) {
        this.visitDTO = visitDTO;
    }

    public List<EncounterDTO> getEncounterDTO() {
        return encounterDTO;
    }

    public void setEncounterDTO(List<EncounterDTO> encounterDTO) {
        this.encounterDTO = encounterDTO;
    }

    public List<ObsDTO> getObsDTO() {
        return obsDTO;
    }

    public void setObsDTO(List<ObsDTO> obsDTO) {
        this.obsDTO = obsDTO;
    }

    public List<LocationDTO> getLocationDTO() {
        return locationDTO;
    }

    public void setLocationDTO(List<LocationDTO> locationDTO) {
        this.locationDTO = locationDTO;
    }

    public List<ProviderDTO> getProviderlist() {
        return providerlist;
    }

    public void setProviderlist(List<ProviderDTO> providerlist) {
        this.providerlist = providerlist;
    }

    public List<Object> getProviderAttributeTypeList() {
        return providerAttributeTypeList;
    }

    public void setProviderAttributeTypeList(List<Object> providerAttributeTypeList) {
        this.providerAttributeTypeList = providerAttributeTypeList;
    }

    public List<ProviderAttributeListDTO> getProviderAttributeList() {
        return providerAttributeList;
    }

    public void setProviderAttributeList(List<ProviderAttributeListDTO> providerAttributeList) {
        this.providerAttributeList = providerAttributeList;
    }

    public List<VisitAttributeTypeDTO> getVisitAttributeTypeList() {
        return visitAttributeTypeList;
    }

    public void setVisitAttributeTypeList(List<VisitAttributeTypeDTO> visitAttributeTypeList) {
        this.visitAttributeTypeList = visitAttributeTypeList;
    }

    public List<VisitAttributeDTO> getVisitAttributeList() {
        return visitAttributeList;
    }

    public void setVisitAttributeList(List<VisitAttributeDTO> visitAttributeList) {
        this.visitAttributeList = visitAttributeList;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }


    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public ConfigResponse getConfigResponse() {
        return configResponse;
    }
}

package org.intelehealth.core.network.model
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.intelehealth.coreroomdb.entity.*

/**
 * Created by - Prajwal W. on 14/10/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
data class ResponseModel (
    @SerializedName("status")
    @Expose
    private var status: String,

    @SerializedName("data")
    @Expose
    private var data: Data
)

data class Data (

    @SerializedName("patientlist") @Expose
    private var patientlist: List<Patient>,

    @SerializedName("pullexecutedtime") @Expose
    private var pullexecutedtime: String,

    @SerializedName("patientAttributeTypeListMaster") @Expose
    private var patientAttributeTypeListMaster: List<PatientAttributeTypeMaster>,

    @SerializedName("patientAttributesList") @Expose
    private var patientAttributesList: List<PatientAttribute>,

    @SerializedName("visitlist") @Expose
    private var visitlist: List<Visit>,

    @SerializedName("encounterlist") @Expose
    private var encounterlist: List<Encounter>,

    @SerializedName("obslist") @Expose
    private var obslist: List<Observation>,

    @SerializedName("locationlist") @Expose
    private var locationlist: List<PatientLocation>,

    @SerializedName("providerlist") @Expose
    private var providerlist: List<Provider>,

    @SerializedName("providerAttributeTypeList") @Expose
    private var providerAttributeTypeList: List<Object>,

    @SerializedName("providerAttributeList") @Expose
    private var providerAttributeList: List<ProviderAttribute>,

    @SerializedName("visitAttributeTypeList") @Expose
    private var visitAttributeTypeList: List<VisitAttribute>,

    @SerializedName("visitAttributeList") @Expose
    private var visitAttributeList: List<VisitAttribute>,

    @SerializedName("pageNo") @Expose
    private var pageNo: Int,

    @SerializedName("totalCount") @Expose
    private var totalCount: Int,

    /*@SerializedName("propertyContents") @Expose
    private var propertyContents: ConfigResponse*/
)
package org.intelehealth.app.sync.network.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.intelehealth.coreroomdb.entity.Encounter
import org.intelehealth.coreroomdb.entity.Observation
import org.intelehealth.coreroomdb.entity.Patient
import org.intelehealth.coreroomdb.entity.PatientAttributeTypeMaster
import org.intelehealth.coreroomdb.entity.PatientLocation
import org.intelehealth.coreroomdb.entity.Provider
import org.intelehealth.coreroomdb.entity.ProviderAttribute
import org.intelehealth.coreroomdb.entity.Visit
import org.intelehealth.coreroomdb.entity.VisitAttribute

data class SyncResponseData(

    @SerializedName("patientlist")
    @Expose
    val patientList: List<Patient>? = null,

    @SerializedName("patientAttributeTypeListMaster")
    @Expose
    val patientAttributeTypeMasterList: List<PatientAttributeTypeMaster>? = null,

    @SerializedName("visitlist")
    @Expose
    var visitList: List<Visit>? = null,

    @SerializedName("encounterlist")
    @Expose
    val encounterList: List<Encounter>? = null,

    @SerializedName("obslist")
    @Expose
    val observationList: List<Observation>? = null,

    @SerializedName("locationlist")
    @Expose
    val locationList: List<PatientLocation>? = null,

    @SerializedName("providerlist")
    @Expose
    val providerList: List<Provider>? = null,

    @SerializedName("providerAttributeTypeList")
    @Expose
    val providerAttributeTypeList: List<ProviderAttribute>? = null,

    @SerializedName("visitAttributeList")
    @Expose
    val visitAttributeList: List<VisitAttribute>? = null,

    @SerializedName("pullexecutedtime")
    @Expose
    val pullExecutedTime: String? = null,

    @SerializedName("pageNo")
    @Expose
    val pageNo: Int = 0,

    @SerializedName("totalCount")
    @Expose
    val totalCount: Int = 0

)

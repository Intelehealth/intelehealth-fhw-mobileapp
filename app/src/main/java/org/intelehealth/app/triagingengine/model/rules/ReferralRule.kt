package org.intelehealth.app.triagingengine.model.rules

import com.google.gson.annotations.SerializedName

/**
 * Created by Lincon Pradhan on  03-02-2025.
 **/
data class ReferralRule(
    @SerializedName("condition") val condition: String,
    @SerializedName("name") val name: String,
    @SerializedName("name-hi") val nameHi: String,
    @SerializedName("result_facility_type") val resultFacilityType: String,
    @SerializedName("result_facility_category") val resultFacilityCategory: String,
    @SerializedName("result_health_provider_designations") val resultHealthProviderDesignations: List<String>
)
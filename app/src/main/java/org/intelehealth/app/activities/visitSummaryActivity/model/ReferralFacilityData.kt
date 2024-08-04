package org.intelehealth.app.activities.visitSummaryActivity.model

import com.google.gson.annotations.SerializedName

/**
 * Created By Tanvir Hasan on 8/4/24 4:50 PM
 * Email: tanvirhasan553@gmail.com
 */

data class ReferralFacilityData(
    val id: Long,
    @SerializedName("District")
    val district: String,
    @SerializedName("District-hi")
    val districtHi: String,
    @SerializedName("Block")
    val block: String,
    @SerializedName("Block-hi")
    val blockHi: String,
    @SerializedName("Facility type")
    val facilityType: String,
    @SerializedName("Facility type-hi")
    val facilityTypeHi: String,
    @SerializedName("Facility Name")
    val facilityName: String,
    @SerializedName("Facility Name-hi")
    val facilityNameHi: String,
    @SerializedName("Category")
    val category: String,
    @SerializedName("Category-hi")
    val categoryHi: String,
    @SerializedName("Name of MOIC/Incharge")
    val nameOfMoicIncharge: String,
    @SerializedName("Name of MOIC/Incharge-hi")
    val nameOfMoicInchargeHi: String,
    @SerializedName("Designation")
    val designation: String,
    @SerializedName("Designation-hi")
    val designationHi: String,
    @SerializedName("Address")
    val address: String,
    @SerializedName("Address-hi")
    val addressHi: String,
    @SerializedName("Contact Number")
    val contactNumber: Long,
)

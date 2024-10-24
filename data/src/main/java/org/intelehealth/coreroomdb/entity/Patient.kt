package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tbl_patient")
data class Patient(
    @PrimaryKey
    @SerializedName("uuid") var uuid: String,
    @ColumnInfo("openmrs_id") @SerializedName("openmrs_id") val openMrsId: String? = null,
    @ColumnInfo("first_name") @SerializedName("first_name") val firstName: String? = null,
    @ColumnInfo("middle_name") @SerializedName("middle_name") val middleName: String? = null,
    @ColumnInfo("last_name") @SerializedName("last_name") val lastName: String? = null,
    @ColumnInfo("date_of_birth") @SerializedName("date_of_birth") val dateOfBirth: String? = null,
    @ColumnInfo("phone_number") @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("address2") val address2: String? = null,
    @SerializedName("address1") val address1: String? = null,
    @ColumnInfo("city_village") @SerializedName("city_village") val cityVillage: String? = null,
    @ColumnInfo("state_province") @SerializedName("state_province") val stateProvince: String? = null,
    @ColumnInfo("postal_code") @SerializedName("postal_code") val postalCode: String? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("sdw") val sdw: String? = null,
    @SerializedName("occupation") val occupation: String? = null,
    @ColumnInfo("creatoruuid") @SerializedName("creatoruuid") val creatorUuid: String? = null,
    @ColumnInfo("education_status") @SerializedName("education_status") val educationStatus: String? = null,
    @ColumnInfo("economic_status") @SerializedName("economic_status") val economicStatus: String? = null,
    @ColumnInfo("patient_photo") @SerializedName("patient_photo") val patientPhoto: String? = null,
    @SerializedName("caste") val caste: String? = null,
    @SerializedName("dead") val dead: String? = null,
    @ColumnInfo("modified_date") @SerializedName("modified_date") val modifiedDate: String? = null,
    @SerializedName("dateCreated") val dateCreated: String? = null,
    @SerializedName("voided") val voided: Int = 0,
    @SerializedName("sync") val sync: Boolean = false,
    @ColumnInfo("abha_number") @SerializedName("abha_number") val abhaNumber: String? = null,
    @ColumnInfo("abha_address") @SerializedName("abha_address") val abhaAddress: String? = null,
) : Parcelable

data class PatientProfile(
    @SerializedName("person")
    @Expose
    val person: String,

    @SerializedName("base64EncodedImage")
    @Expose
    val base64EncodedImage: String
)
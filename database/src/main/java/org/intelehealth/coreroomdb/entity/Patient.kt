package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tbl_patient")
data class Patient(
    @PrimaryKey
    @SerializedName("uuid")
    var uuid: String,
    @SerializedName("openmrs_id")
    val openMrsId: String? = null,
    @SerializedName("first_name")
    val firstName: String? = null,
    @SerializedName("middle_name")
    val middleName: String? = null,
    @SerializedName("last_name")
    val lastName: String? = null,
    @SerializedName("date_of_birth")
    val dateOfBirth: String? = null,
    @SerializedName("phone_number")
    val phoneNumber: String? = null,
    @SerializedName("address2")
    val address2: String? = null,
    @SerializedName("address1")
    val address1: String? = null,
    @SerializedName("city_village")
    val cityVillage: String? = null,
    @SerializedName("state_province")
    val stateProvince: String? = null,
    @SerializedName("postal_code")
    val postalCode: String? = null,
    @SerializedName("country")
    val country: String? = null,
    @SerializedName("gender")
    val gender: String? = null,
    @SerializedName("sdw")
    val sdw: String? = null,
    @SerializedName("occupation")
    val occupation: String? = null,
    @SerializedName("creatoruuid")
    val creatorUuid: String? = null,
    @SerializedName("education_status")
    val educationStatus: String? = null,
    @SerializedName("economic_status")
    val economicStatus: String? = null,
    @SerializedName("patient_photo")
    val patientPhoto: String? = null,
    @SerializedName("caste")
    val caste: String? = null,
    @SerializedName("dead")
    val dead: String? = null,
    @SerializedName("modified_date")
    val modifiedDate: String? = null,
    @SerializedName("dateCreated")
    val dateCreated: String? = null,
    @SerializedName("voided")
    val voided: Int = 0,
    @SerializedName("sync")
    val sync: Boolean = false,
) : Parcelable
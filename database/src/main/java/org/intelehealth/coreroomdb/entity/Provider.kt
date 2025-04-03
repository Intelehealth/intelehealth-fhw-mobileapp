package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tbl_provider")
data class Provider(
    @PrimaryKey
    @SerializedName("uuid")
    var uuid: String,
    @SerializedName("identifier")
    var identifier: String? = null,
    @SerializedName("given_name")
    var givenName: String? = null,
    @SerializedName("family_name")
    var familyName: String? = null,

    @SerializedName("emailId")
    var emailId: String,
    @SerializedName("telephoneNumber")
    var telephoneNumber: String? = null,
    @SerializedName("dateofbirth")
    var dateOfBirth: String? = null,
    @SerializedName("gender")
    var gender: String? = null,

    @SerializedName("imagePath")
    var imagePath: String,
    @SerializedName("countryCode")
    var countryCode: String? = null,

    @SerializedName("voided")
    var voided: Int? = null,
    var role: String? = null,
    @SerializedName("useruuid")
    var userUuid: String? = null,
    @SerializedName("modified_date")
    val modifiedDate: String? = null,
    @SerializedName("sync")
    val sync: Boolean = false,
) : Parcelable

package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tbl_provider_attribute")
data class ProviderAttribute(
    @PrimaryKey
    @SerializedName("uuid") var uuid: String,

    @ColumnInfo("provider_uuid")
    @SerializedName("provider_uuid", alternate = ["provideruuid"])
    val providerUuid: String? = null,

    @ColumnInfo("provider_attribute_type_uuid")
    @SerializedName("provider_attribute_type_uuid", alternate = ["attributetypeuuid"])
    val providerAttrTypeUuid: String? = null,

    @SerializedName("value") val value: String? = null,
    @SerializedName("voided") val voided: Int = 0,
    @SerializedName("sync") var sync: Boolean = false
) : Parcelable
package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Not in use
 */
@Parcelize
@Entity(tableName = "tbl_location")
data class PatientLocation(
    @SerializedName("name")
    private var name: String? = null,
    @PrimaryKey
    @SerializedName("locationuuid")
    private val locationUuid: String? = null,
    @SerializedName("retired")
    private val retired: Int? = null,
    @SerializedName("modified_date")
    var modifiedDate: String? = null,
    var voided: Int = 0,
    var sync: Boolean = false
) : Parcelable
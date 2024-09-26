package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
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
    @SerializedName("name") private var name: String? = null,
    @PrimaryKey
    @ColumnInfo("locationuuid") @SerializedName("locationuuid") private val locationUuid: String? = null,
    @SerializedName("retired") private val retired: Int? = null,
    @ColumnInfo("modified_date") @SerializedName("modified_date") var modifiedDate: String? = null,
    var voided: Int = 0,
    var sync: Boolean = false
) : Parcelable
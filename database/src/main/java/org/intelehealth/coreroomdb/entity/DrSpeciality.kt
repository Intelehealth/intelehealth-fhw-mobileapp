package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Created by Vaghela Mithun R. on 29-03-2024 - 19:18.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
/**
 * Not in use
 */
@Parcelize
@Entity(tableName = "tbl_dr_speciality")
data class DrSpeciality(
    @PrimaryKey
    @SerializedName("uuid")
    var uuid: String? = null,

    @SerializedName("provideruuid")
    var providerUuid: Int = 0,

    @SerializedName("attributetypeuuid")
    var attributeTypeUuid: String? = null,

    @SerializedName("value")
    var value: String? = null,

    @SerializedName("voided")
    var voided: Int = 0
):Parcelable

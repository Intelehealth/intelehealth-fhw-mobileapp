package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tbl_visit_attribute")
data class VisitAttribute(
    var uuid: String? = null,
    @SerializedName("visit_uuid")
    var visitUuid: String? = null,
    var value: String? = null,
    @SerializedName("visit_attribute_type_uuid")
    var visitAttributeTypeUuid: String? = null,
    var voided: Int = 0,
    var sync: Boolean = false
) : Parcelable
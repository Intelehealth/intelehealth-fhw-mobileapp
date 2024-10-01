package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Created by - Prajwal W. on 26/09/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/

@Parcelize
@Entity
class FollowupScheduleNotification(
    @PrimaryKey
    @SerializedName("id") val id: String,
    @ColumnInfo("date_time") @SerializedName("date_time") var dateTime: String,
    @SerializedName("duration") val duration: String,
    @SerializedName("value") var value: String,
    @SerializedName("name") val name: String,
    @ColumnInfo("openmrs_id") @SerializedName("openmrs_id") val openmrsId: String,
    @ColumnInfo("patient_uuid") @SerializedName("patient_uuid") val patientUuid: String,
    @ColumnInfo("visit_uuid") @SerializedName("visit_uuid") val visitUuid: String,
    @ColumnInfo("request_code") @SerializedName("request_code") val requestCode: String
) : Parcelable
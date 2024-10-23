package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tbl_rtc_connection_log")
data class RtcConnectionLog(
    @PrimaryKey var uuid: String,
    @ColumnInfo("visit_uuid") @SerializedName("visit_uuid") var visitUuid: String? = null,
    @ColumnInfo("connection_info") @SerializedName("connection_info") var connectionInfo: String? = null
) : Parcelable
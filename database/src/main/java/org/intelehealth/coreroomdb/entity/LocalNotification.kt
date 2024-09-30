package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Created by Vaghela Mithun R. on 29-03-2024 - 19:04.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Parcelize
@Entity(tableName = "tbl_notifications")
data class LocalNotification(
    @PrimaryKey
    @SerializedName("uuid") var uuid: String,
    @SerializedName("description") var description: String? = null,
    @ColumnInfo("notification_type") @SerializedName("notification_type") var notificationType: String? = null,
    @ColumnInfo("obs_server_modified_date") @SerializedName("obs_server_modified_date")
    var obsServerModifiedDate: String? = null,
    @ColumnInfo("isdeleted") @SerializedName("isdeleted") var deleted: Boolean = false,
) : Parcelable

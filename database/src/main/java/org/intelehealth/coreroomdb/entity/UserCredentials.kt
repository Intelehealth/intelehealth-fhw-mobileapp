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
@Entity(tableName = "tbl_user_credentials")
data class UserCredentials(
    @ColumnInfo("username") @SerializedName("username") var userName: String,
    @PrimaryKey
    @SerializedName("password") var password: String,
    @ColumnInfo("creator_uuid_cred") @SerializedName("creator_uuid_cred") var creatorUuidCred: String,
    @ColumnInfo("chwname") @SerializedName("chwname") var chwName: String,
    @ColumnInfo("provider_uuid_cred") @SerializedName("provider_uuid_cred") var providerUuidCred: String
) : Parcelable
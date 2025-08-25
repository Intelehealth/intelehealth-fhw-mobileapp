package org.intelehealth.ncd.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_obs")
data class Obs(
    @PrimaryKey
    @ColumnInfo(name = "uuid")
    val uuid: String,

    @ColumnInfo(name = "encounteruuid")
    val encounteruuid: String? = null,

    @ColumnInfo(name = "conceptuuid")
    val conceptuuid: String? = null,

    @ColumnInfo(name = "value")
    val value: String? = null,

    @ColumnInfo(name = "creator")
    val creator: String? = null,

    @ColumnInfo(name = "voided", defaultValue = "'0'")
    val voided: String? = null,

    @ColumnInfo(name = "obsservermodifieddate")
    val obsservermodifieddate: String? = null,

    @ColumnInfo(name = "modified_date")
    val modified_date: String? = null,

    @ColumnInfo(name = "created_date", defaultValue = "CURRENT_TIMESTAMP")
    val created_date: String? = null,

    @ColumnInfo(name = "sync", defaultValue = "'false'")
    val sync: String? = null,

    @ColumnInfo(name = "comments")
    val comments: String? = null
)
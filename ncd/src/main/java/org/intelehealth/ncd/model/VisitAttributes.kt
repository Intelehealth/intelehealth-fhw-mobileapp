package org.intelehealth.ncd.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_visit_attribute")
data class VisitAttributes(
    @PrimaryKey
    @ColumnInfo(name = "uuid")
    val uuid: String,
    @ColumnInfo(name = "value")
    val value: String? = null,
    @ColumnInfo(name = "visit_attribute_type_uuid")
    val visitAttributeTypeUuid: String? = null,
    @ColumnInfo(name = "visit_uuid")
    val visitUuid: String? = null,
    @ColumnInfo(name = "voided")
    val voided: String?,
    @ColumnInfo(name = "sync")
    val sync: String?
)
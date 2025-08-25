package org.intelehealth.ncd.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
open class BaseEntity(
    @PrimaryKey
    open var uuid: String = "",
    @ColumnInfo("created_at")
    open var createdAt: String? = null,
    @ColumnInfo("updated_at")
    open var updatedAt: String? = null,
    open var synced: Boolean = false,
    var voided: Int = 0,
)
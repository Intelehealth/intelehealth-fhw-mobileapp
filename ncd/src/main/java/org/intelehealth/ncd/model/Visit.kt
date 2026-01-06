package org.intelehealth.ncd.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.intelehealth.ncd.utils.DateAndTimeUtils
import java.util.Date


@Entity(tableName = "tbl_visit")
data class Visit(
    @PrimaryKey
    @ColumnInfo(name = "uuid")
    val uuid: String,

    @ColumnInfo(name = "patientuuid")
    val patientuuid: String? = null,

    @ColumnInfo(name = "startdate")
    var startdate: String? = null,

    @ColumnInfo(name = "enddate")
    val enddate: String? = null,

    @ColumnInfo(name = "visit_type_uuid")
    val visit_type_uuid: String? = null,

    @ColumnInfo(name = "locationuuid")
    val locationuuid: String? = null,

    @ColumnInfo(name = "creator")
    val creator: String? = null,

    @ColumnInfo(name = "modified_date")
    var modified_date: String? = null,

    @ColumnInfo(name = "isdownloaded", defaultValue = "'false'")
    val isdownloaded: String? = "false",

    @ColumnInfo(name = "voided", defaultValue = "'0'")
    val voided: String? = "0",

    @ColumnInfo(name = "sync", defaultValue = "'false'")
    val sync: String? = "false",

    @ColumnInfo(name = "issubmitted")
    val issubmitted: Int? = 0
) /*{
    init {
        if (startdate == null) {
            startdate = DateAndTimeUtils.formatDateFromOnetoAnother(
                Date().toString(),
                "EEE MMM dd HH:mm:ss zzz yyyy",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
            )
        }
        if (modified_date == null) {
            modified_date = DateAndTimeUtils.currentDateTime()
        }
    }
}*/

/*
@Entity(tableName = "tbl_visit")
data class Visit(
    @PrimaryKey
    @ColumnInfo(name = "uuid")
    val uuid: String,

    @ColumnInfo(name = "patientuuid")
    val patientuuid: String? = null,

    @ColumnInfo(name = "locationuuid")
    val locationuuid: String? = null,

    @ColumnInfo(name = "visit_type_uuid")
    val visit_type_uuid: String? = null,

    @ColumnInfo(name = "creator")
    val creator: String? = null,

    @ColumnInfo(name = "startdate")
    var startdate: String? = null,

    @ColumnInfo(name = "enddate")
    val enddate: String? = null,

    @ColumnInfo(name = "modified_date")
    var modified_date: String? = null,

    @ColumnInfo(name = "sync", defaultValue =  "'false'")
    val sync: String? = null,

    @ColumnInfo(name = "isdownloaded", defaultValue =  "'false'")
    val isdownloaded: String? = null,

    @ColumnInfo(name = "voided", defaultValue = "'0'")
    val voided: String? = null,

    @ColumnInfo(name = "issubmitted")
    val issubmitted: Int = 0
) {
    init {
        if (startdate == null) {
            startdate = DateAndTimeUtils.formatDateFromOnetoAnother(
                Date().toString(),
                "EEE MMM dd HH:mm:ss zzz yyyy",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
            )
        }
        if (modified_date == null) {
            modified_date = DateAndTimeUtils.currentDateTime()
        }
    }
}*/

package org.intelehealth.coreroomdb.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import org.intelehealth.coreroomdb.entity.MediaRecord

/**
 * Created by Vaghela Mithun R. on 02-04-2024 - 10:24.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Dao
interface MediaRecordDao : CoreDao<MediaRecord> {
    @Query("SELECT * FROM tbl_image_records")
    override fun getAll(): LiveData<List<MediaRecord>>

    @Query("SELECT * FROM tbl_image_records WHERE uuid = :uuid")
    fun getMediaRecordByUuid(uuid: String): LiveData<MediaRecord>

    @Query("SELECT * FROM tbl_image_records WHERE visituuid = :visitId")
    fun getMediaRecordByVisitId(visitId: String): LiveData<List<MediaRecord>>

    @Query("SELECT * FROM tbl_image_records WHERE patientuuid = :patientId")
    fun getMediaRecordByPatientId(patientId: String): LiveData<List<MediaRecord>>

    @Query("SELECT * FROM tbl_image_records WHERE encounteruuid = :encounterId")
    fun getMediaRecordByEncounterId(encounterId: String): LiveData<List<MediaRecord>>

    @Query("SELECT * FROM tbl_image_records WHERE image_type = :imageType")
    fun getMediaRecordByImageType(imageType: String): LiveData<List<MediaRecord>>
}
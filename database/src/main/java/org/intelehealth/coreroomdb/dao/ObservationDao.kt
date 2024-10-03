package org.intelehealth.coreroomdb.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import org.intelehealth.coreroomdb.entity.Observation

/**
 * Created by Vaghela Mithun R. on 02-04-2024 - 10:24.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Dao
interface ObservationDao : CoreDao<Observation> {
    @Query("SELECT * FROM tbl_obs")
    override fun getAll(): LiveData<List<Observation>>

    @Query("SELECT * FROM tbl_obs WHERE uuid = :uuid")
    fun getObservationByUuid(uuid: String): LiveData<Observation>

    @Query("SELECT * FROM tbl_obs WHERE encounterUuid = :encounterId")
    fun getObservationByEncounterId(encounterId: String): LiveData<List<Observation>>

    @Query("SELECT * FROM tbl_obs WHERE conceptUuid = :conceptId")
    fun getObservationByConceptId(conceptId: String): LiveData<List<Observation>>

    @Query("SELECT * FROM tbl_obs WHERE creator = :creatorId")
    fun getObservationByCreator(creatorId: String): LiveData<List<Observation>>

    @Query("UPDATE tbl_obs SET comment = :comment WHERE uuid = :uuid")
    suspend fun updateComment(uuid: String, comment: String)

    @Query("UPDATE tbl_obs SET sync = :isSync WHERE uuid = :uuid")
    suspend fun updateSyncStatus(uuid: String, isSync: Boolean)

    @Query("UPDATE tbl_obs SET obsServerModifiedDate = :obsModifiedDate WHERE uuid = :uuid")
    suspend fun updateServerModifiedDate(obsModifiedDate: String, uuid: String)

    @Query("UPDATE tbl_obs SET modifiedDate = :modifiedDate WHERE uuid = :uuid")
    suspend fun updateModifiedDate(modifiedDate: String, uuid: String)

    @Query("UPDATE tbl_obs SET value = :value WHERE uuid = :uuid")
    suspend fun updateValue(uuid: String, value: String)
}
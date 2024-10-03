package org.intelehealth.coreroomdb.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import org.intelehealth.coreroomdb.entity.Encounter

/**
 * Created by Vaghela Mithun R. on 02-04-2024 - 10:24.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Dao
interface EncounterDao : CoreDao<Encounter> {
    @Query("SELECT * FROM tbl_encounter")
    override fun getAll(): LiveData<List<Encounter>>

    @Query("SELECT * FROM tbl_encounter WHERE uuid = :uuid")
    fun getEncounterByUuid(uuid: String): LiveData<Encounter>

    @Query("SELECT * FROM tbl_encounter WHERE visitUuid = :visitId")
    fun getEncounterByVisitId(visitId: String): LiveData<List<Encounter>>

    @Query("SELECT * FROM tbl_encounter WHERE providerUuid = :providerId")
    fun getEncounterByProviderId(providerId: String): LiveData<List<Encounter>>

    @Query("SELECT * FROM tbl_encounter WHERE encounterTypeUuid = :encounterTypeId")
    fun getEncounterByTypeId(encounterTypeId: String): LiveData<List<Encounter>>

    @Query("SELECT * FROM tbl_encounter WHERE visitUuid = :visitId AND providerUuid = :providerId")
    fun getProviderVisitEncounters(visitId: String, providerId: String): LiveData<List<Encounter>>

    @Query("SELECT * FROM tbl_encounter WHERE visitUuid = :visitId AND encounterTypeUuid = :encounterTypeId")
    fun getVisitEncounterByTypeId(visitId: String, encounterTypeId: String): LiveData<List<Encounter>>

    @Query("SELECT * FROM tbl_encounter WHERE visitUuid = :visitId AND providerUuid = :providerId AND encounterTypeUuid = :encounterTypeId")
    fun getProviderVisitEncounterByTypeId(visitId: String, providerId: String, encounterTypeId: String): LiveData<List<Encounter>>
}
package org.intelehealth.coreroomdb.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import org.intelehealth.coreroomdb.entity.Concept

/**
 * Created by Vaghela Mithun R. on 02-04-2024 - 10:24.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Dao
interface ConceptDao : CoreDao<Concept> {
    @Query("SELECT * FROM tbl_uuid_dictionary")
    override fun getAll(): LiveData<List<Concept>>

    @Query("SELECT * FROM tbl_uuid_dictionary WHERE uuid = :uuid")
    fun getConceptByUuid(uuid: String): LiveData<Concept>

    @Query("SELECT * FROM tbl_uuid_dictionary WHERE name = :name")
    fun getConceptByName(name: String): LiveData<Concept>
}
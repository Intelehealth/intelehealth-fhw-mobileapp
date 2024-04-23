package org.intelehealth.config.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.intelehealth.config.room.entity.ConfigDictionary
import org.intelehealth.config.room.entity.Specialization

/**
 * Created by Vaghela Mithun R. on 15-03-2024 - 16:10.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Dao
interface SpecializationDao : CoreDao<Specialization> {
    @Query("SELECT * FROM tbl_specialization")
    suspend fun getAllRecord(): List<Specialization>

    @Query("SELECT * FROM tbl_specialization where sKey = :key")
    suspend fun getRecord(key: String): Specialization

    @Query("SELECT * FROM tbl_specialization where name = :name")
    fun getRecordByName(name: String): LiveData<Specialization>

    @Query("SELECT * FROM tbl_specialization")
    fun getAllLiveRecord(): LiveData<List<Specialization>>

    @Query("SELECT * FROM tbl_specialization where sKey = :key")
    fun getLiveRecord(key: String): LiveData<Specialization>

}
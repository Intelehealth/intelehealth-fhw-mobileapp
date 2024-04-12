package org.intelehealth.config.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.intelehealth.config.room.entity.ConfigDictionary

/**
 * Created by Vaghela Mithun R. on 15-03-2024 - 16:10.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Dao
interface LanguageDao : CoreDao<LanguageDao> {

    @Query("SELECT * FROM tbl_language")
    override suspend fun getAllRecord(): List<LanguageDao>

    @Query("SELECT * FROM tbl_language where code = :key")
    override suspend fun getRecord(key: String): LanguageDao

    @Query("SELECT * FROM tbl_language")
    override fun getAllLiveRecord(): LiveData<List<LanguageDao>>

    @Query("SELECT * FROM tbl_language where code = :key")
    override fun getLiveRecord(key: String): LiveData<LanguageDao>
}
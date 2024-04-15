package org.intelehealth.config.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.intelehealth.config.room.entity.ActiveLanguage
import org.intelehealth.config.room.entity.ConfigDictionary

/**
 * Created by Vaghela Mithun R. on 15-03-2024 - 16:10.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Dao
interface LanguageDao : CoreDao<ActiveLanguage> {

    @Query("SELECT * FROM tbl_language")
    suspend fun getAllRecord(): List<ActiveLanguage>

    @Query("SELECT * FROM tbl_language where code = :key")
    suspend fun getRecord(key: String): ActiveLanguage

    @Query("SELECT * FROM tbl_language")
    fun getAllLiveRecord(): LiveData<List<ActiveLanguage>>

    @Query("SELECT * FROM tbl_language where code = :key")
    fun getLiveRecord(key: String): LiveData<ActiveLanguage>
}
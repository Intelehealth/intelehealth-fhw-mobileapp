package org.intelehealth.config.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.intelehealth.config.room.entity.ActiveLanguage
import org.intelehealth.config.room.entity.ActiveSection
import org.intelehealth.config.room.entity.ConfigDictionary

/**
 * Created by Lincon Pradhan
 * Email : lincon@intelehealth.org
 **/
@Dao
interface ActiveSectionDao : CoreDao<ActiveSection> {

    @Query("SELECT * FROM tbl_active_section")
    suspend fun getAllRecord(): List<ActiveSection>

    @Query("SELECT * FROM tbl_active_section where `key` = :key")
    suspend fun getRecord(key: String): ActiveSection

    @Query("SELECT * FROM tbl_active_section")
    fun getAllLiveRecord(): LiveData<List<ActiveSection>>

    @Query("SELECT * FROM tbl_active_section where `key` = :key")
    fun getLiveRecord(key: String): LiveData<ActiveSection>
}
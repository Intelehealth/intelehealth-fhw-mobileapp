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
interface ConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfiguration(specializations: List<ConfigDictionary>)

    @Query("SELECT * FROM config_dictionary WHERE dicKey = :key ")
    fun getLiveConfigValueByKey(key: String): LiveData<ConfigDictionary>

    @Query("SELECT * FROM config_dictionary WHERE dicKey = :key ")
    suspend fun getConfigValueByKey(key: String): ConfigDictionary
}
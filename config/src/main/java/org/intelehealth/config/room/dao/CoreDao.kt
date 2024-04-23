package org.intelehealth.config.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy

/**
 * Created by Vaghela Mithun R. on 10-04-2024 - 17:51.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
interface CoreDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(data: List<T>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(data: T)

//    suspend fun getAllRecord(): List<T>
//
//    suspend fun getRecord(key: String): T
//
//    fun getAllLiveRecord(): LiveData<List<T>>
//
//    fun getLiveRecord(key: String): LiveData<T>
}
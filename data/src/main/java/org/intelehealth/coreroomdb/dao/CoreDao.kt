package org.intelehealth.coreroomdb.dao

import androidx.lifecycle.LiveData
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

/**
 * Created by Vaghela Mithun R. on 02-04-2024 - 10:26.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
interface CoreDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<T>)

   /* @Delete
    suspend fun clear()*/

    fun getAll(): LiveData<List<T>>

    @Update
    suspend fun update(obj: T)
}
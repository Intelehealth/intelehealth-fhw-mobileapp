package com.intelehealth.appointment.data.local.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy

interface CoreDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(data: List<T>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(data: T)
}
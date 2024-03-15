package org.intelehealth.config.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.intelehealth.config.room.entity.DoctorSpecialization

/**
 * Created by Vaghela Mithun R. on 15-03-2024 - 16:10.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Dao
interface DRSpecializationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSpecializations(specializations: List<DoctorSpecialization>)

    @Query("SELECT id, name FROM dr_specialization WHERE enable = 1")
    fun getActiveSpecializations(): LiveData<List<DoctorSpecialization>>

    @Query("SELECT id, name FROM dr_specialization WHERE id = :id")
    fun getSpecializationById(id: Int): DoctorSpecialization
}
package org.intelehealth.config.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.intelehealth.config.room.dao.DRSpecializationDao
import org.intelehealth.config.room.entity.DoctorSpecialization

/**
 * Created by Vaghela Mithun R. on 15-03-2024 - 16:21.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class DrSpecializationRepository(private val drSpecializationDao: DRSpecializationDao) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun saveSpecialization(specializations: List<DoctorSpecialization>) {
        scope.launch { drSpecializationDao.saveSpecializations(specializations) }
    }

    fun getActiveSpecializations() = drSpecializationDao.getActiveSpecializations()

    fun getSpecializationById(id: Int) = drSpecializationDao.getSpecializationById(id)
}
package org.intelehealth.config.presenter.specialization.data

import org.intelehealth.config.room.dao.SpecializationDao

/**
 * Created by Vaghela Mithun R. on 10-04-2024 - 18:11.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class SpecializationRepository(private val specializationDao: SpecializationDao) {
    suspend fun getAllRecord() = specializationDao.getAllRecord()

    suspend fun getRecord(key: String) = specializationDao.getRecord(key)

    fun getRecordByName(name: String) = specializationDao.getRecordByName(name)

    fun getAllLiveRecord() = specializationDao.getAllLiveRecord()

    fun getLiveRecord(key: String) = specializationDao.getLiveRecord(key)
}
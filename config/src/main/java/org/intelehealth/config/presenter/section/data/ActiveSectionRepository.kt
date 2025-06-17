package org.intelehealth.config.presenter.section.data

import org.intelehealth.config.room.dao.ActiveSectionDao

/**
 * Created by Lincon Pradhan
 * Email : lincon@intelehealth.org
 **/

class ActiveSectionRepository(private val activeSectionDao: ActiveSectionDao) {
    fun getAllActiveSection() = activeSectionDao.getAllLiveRecord()

    suspend fun getAllRecord() = activeSectionDao.getAllRecord()

    suspend fun getRecord(key: String) = activeSectionDao.getRecord(key)

    fun getLiveRecord(key: String) = activeSectionDao.getLiveRecord(key)
}
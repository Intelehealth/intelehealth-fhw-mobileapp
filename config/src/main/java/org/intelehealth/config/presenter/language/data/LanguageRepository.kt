package org.intelehealth.config.presenter.language.data

import org.intelehealth.config.room.dao.LanguageDao

/**
 * Created by Vaghela Mithun R. on 10-04-2024 - 18:11.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class LanguageRepository(private val languageDao: LanguageDao) {
    fun getAllSupportedLanguage() = languageDao.getAllLiveRecord()

    suspend fun getAllRecord() = languageDao.getAllRecord()

    suspend fun getRecord(key: String) = languageDao.getRecord(key)

    fun getLiveRecord(key: String) = languageDao.getLiveRecord(key)
}
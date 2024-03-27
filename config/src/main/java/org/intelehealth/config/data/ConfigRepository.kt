package org.intelehealth.config.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.intelehealth.config.room.dao.ConfigDao
import org.intelehealth.config.room.entity.ConfigDictionary
import org.intelehealth.config.utility.KEY_SPECIALIZATIONS

/**
 * Created by Vaghela Mithun R. on 15-03-2024 - 16:21.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class ConfigRepository(private val configDao: ConfigDao) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
//
//    fun saveAllConfig(configs: List<ConfigDictionary>) {
//        scope.launch { configDao.saveConfiguration(configs) }
//    }
//
//    fun getSpecificConfigByKey(key: String) = configDao.getLiveConfigValueByKey(key)
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    fun test(): ConfigDictionary = scope.async {
//        return@async configDao.getConfigValueByKey(KEY_SPECIALIZATIONS)
//    }.getCompleted()

}
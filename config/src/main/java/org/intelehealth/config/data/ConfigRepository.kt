package org.intelehealth.config.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.intelehealth.config.Config
import org.intelehealth.config.network.response.ConfigResponse
import org.intelehealth.config.room.ConfigDatabase
import org.intelehealth.config.room.dao.ConfigDao
import org.intelehealth.config.room.entity.ConfigDictionary
import org.intelehealth.config.utility.KEY_SPECIALIZATIONS
import org.intelehealth.core.network.state.Result

/**
 * Created by Vaghela Mithun R. on 15-03-2024 - 16:21.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class ConfigRepository(
    private val configDb: ConfigDatabase,
    private val dataSource: ConfigDataSource,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {

    fun fetchAndUpdateConfig(onCompleted: (Result<*>) -> Unit) {
        scope.launch {
            dataSource.getConfig().collect {
                if (it.isSuccess()) {

                } else onCompleted(it)
            }
        }
    }

    fun saveAllConfig(config: ConfigResponse) {
        scope.launch {
            configDb.specializationDao().save(config.specialization)
            configDb.languageDao()
        }
    }
//
//    fun getSpecificConfigByKey(key: String) = configDao.getLiveConfigValueByKey(key)
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    fun test(): ConfigDictionary = scope.async {
//        return@async configDao.getConfigValueByKey(KEY_SPECIALIZATIONS)
//    }.getCompleted()
}
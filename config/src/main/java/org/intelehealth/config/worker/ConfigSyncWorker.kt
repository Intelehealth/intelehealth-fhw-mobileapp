package org.intelehealth.config.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intelehealth.config.data.ConfigDataSource
import org.intelehealth.config.data.ConfigRepository
import org.intelehealth.config.network.WebClient
import org.intelehealth.config.network.provider.WebClientProvider
import org.intelehealth.config.room.ConfigDatabase
import org.intelehealth.core.network.helper.NetworkHelper

/**
 * Created by Vaghela Mithun R. on 12-04-2024 - 13:17.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class ConfigSyncWorker(
    private val ctx: Context, private val params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        var workerResult = Result.failure()
        val webClient: WebClient = WebClientProvider.getApiClient()
        val networkHelper = NetworkHelper(ctx)
        val dataSource = ConfigDataSource(webClient, networkHelper)
        val database = ConfigDatabase.getInstance(ctx)
        withContext(Dispatchers.IO) {
            val configRepository = ConfigRepository(database, dataSource, this)
            configRepository.fetchAndUpdateConfig {
                workerResult = if (it.isSuccess()) Result.success() else Result.failure()
            }
        }
        return workerResult
    }

//    private fun getResult(configResult: org.intelehealth.core.network.state.Result<*>) =
//        when (configResult) {
//            is Error -> Result.failure(workDataOf(Pair(WORKER_RESULT, configResult)))
//            is Fail -> Result.retry()
//            is Loading -> Result.success(workDataOf(Pair(WORKER_RESULT, configResult)))
//            is Success -> Result.success(workDataOf(Pair(WORKER_RESULT, configResult)))
//        }

    companion object {
        fun startConfigSyncWorker(context: Context, onResult: (String) -> Unit) {
            val configWorkRequest = OneTimeWorkRequestBuilder<ConfigSyncWorker>().build()
            val workManager = WorkManager.getInstance(context.applicationContext)
            workManager.enqueue(configWorkRequest)
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            scope.launch {
                workManager.getWorkInfoByIdFlow(configWorkRequest.id).collect {
                    Log.d(
                        "ConfigSyncWorker",
                        "startConfigSyncWorker: ${Gson().toJson(it.outputData)}"
                    )
                    onResult(it.state.name)
                }
            }
        }
    }
}
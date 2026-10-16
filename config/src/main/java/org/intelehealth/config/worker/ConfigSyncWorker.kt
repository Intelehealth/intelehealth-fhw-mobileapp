package org.intelehealth.config.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import kotlinx.coroutines.CancellationException
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.await
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.transformWhile
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
        private const val UNIQUE_WORK_NAME = "config_sync"

        /**
         * Fetches and saves the server config. Runs as unique work: saveAllConfig() clears and
         * rewrites the config tables, so overlapping runs (splash, pull sync, post-update refresh)
         * could interleave and corrupt them. A caller arriving while a run is in progress joins
         * that run (KEEP) and gets its states. Stops observing once the run finishes, so the
         * callback (and anything it captures) isn't retained afterwards.
         */
        fun startConfigSyncWorker(context: Context, onResult: (String) -> Unit) {
            val configWorkRequest = OneTimeWorkRequestBuilder<ConfigSyncWorker>().build()
            val workManager = WorkManager.getInstance(context.applicationContext)
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            scope.launch {
                // WorkManager failures (e.g. it couldn't record the work) are reported as a failed
                // sync - the splash shows its retry dialog - instead of crashing on an uncaught
                // exception. Only WorkManager calls are guarded: an exception thrown by the
                // caller's own onResult must surface, not turn into a second FAILED result.
                try {
                    // Wait for the enqueue to apply so the flow below doesn't report a previous,
                    // already-finished run under the same name.
                    workManager.enqueueUniqueWork(
                        UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, configWorkRequest
                    ).await()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("ConfigSyncWorker", "startConfigSyncWorker: enqueue failed", e)
                    onResult(WorkInfo.State.FAILED.name)
                    return@launch
                }
                workManager.getWorkInfosForUniqueWorkFlow(UNIQUE_WORK_NAME)
                    .mapNotNull { it.firstOrNull() }
                    .transformWhile { info ->
                        emit(info)
                        !info.state.isFinished
                    }
                    .map { info ->
                        Log.d(
                            "ConfigSyncWorker",
                            "startConfigSyncWorker: ${Gson().toJson(info.outputData)}"
                        )
                        info.state.name
                    }
                    // Upstream only: exceptions thrown inside collect (onResult) aren't caught here.
                    .catch { e ->
                        Log.e("ConfigSyncWorker", "startConfigSyncWorker: observe failed", e)
                        emit(WorkInfo.State.FAILED.name)
                    }
                    .collect { state -> onResult(state) }
            }
        }
    }
}
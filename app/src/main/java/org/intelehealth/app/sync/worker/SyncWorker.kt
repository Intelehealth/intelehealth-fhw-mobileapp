package org.intelehealth.app.sync.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intelehealth.app.sync.data.SyncDataSource
import org.intelehealth.app.sync.data.SyncRepository
import org.intelehealth.app.sync.network.WebClient
import org.intelehealth.app.sync.network.provider.WebClientProvider
import org.intelehealth.core.network.helper.NetworkHelper
import org.intelehealth.coreroomdb.IHDatabase

class SyncWorker(
    private val ctx: Context,
    private val params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        var workerResult = Result.failure()
        val webClient: WebClient = WebClientProvider.getApiClient()
        val networkHelper = NetworkHelper(ctx)
        val dataSource = SyncDataSource(webClient, networkHelper)
        val database = IHDatabase.getInstance(ctx)

        withContext(Dispatchers.IO) {
            val syncRepository = SyncRepository(database, dataSource, this)
            syncRepository.pullAndSaveData("", "") {
                workerResult = if (it.isSuccess()) Result.success() else Result.failure()
            }
        }
        return workerResult
    }

    companion object {
        fun startSyncWorker(context: Context, onResult: (String) -> Unit) {
            val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
            val workManager = WorkManager.getInstance(context.applicationContext)
            workManager.enqueue(syncWorkRequest)
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            scope.launch {
                workManager.getWorkInfoByIdFlow(syncWorkRequest.id).collect {
                    onResult(it.state.name)
                }
            }
        }
    }
}
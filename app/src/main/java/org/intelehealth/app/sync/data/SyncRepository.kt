package org.intelehealth.app.sync.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.intelehealth.app.models.dto.ResponseDTO
import org.intelehealth.app.sync.network.provider.WebClientProvider
import org.intelehealth.config.utility.NO_DATA_FOUND
import org.intelehealth.core.network.helper.NetworkHelper
import org.intelehealth.core.network.state.Result
import org.intelehealth.coreroomdb.IHDatabase

class SyncRepository(
    private val ihDatabase: IHDatabase,
    private val dataSource: SyncDataSource,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {

    constructor(context: Context) : this(
        ihDatabase = IHDatabase.getInstance(context),
        SyncDataSource(
            WebClientProvider.getApiClient(),
            NetworkHelper(context)
        ), scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    )

    fun pullAndSaveData(
        locationUuid: String,
        pullExecutedTime: String,
        onCompleted: (Result<*>) -> Unit
    ) {
        scope.launch {
            dataSource.pullData(locationUuid, pullExecutedTime).collect { result ->
                if (result.isSuccess()) {
                    result.data?.let {
                        saveDataIntoDb(it) { onCompleted(result) }
                    } ?: onCompleted(Result.Fail<Any>(NO_DATA_FOUND))
                } else onCompleted(result)
            }
        }
    }

    private fun saveDataIntoDb(response: ResponseDTO, onCompleted: () -> Unit) {

    }
}
package org.intelehealth.app.sync.data

import org.intelehealth.app.sync.network.WebClient
import org.intelehealth.core.data.BaseDataSource
import org.intelehealth.core.network.helper.NetworkHelper

class SyncDataSource(
    private val webClient: WebClient,
    private val networkHelper: NetworkHelper
) : BaseDataSource(networkHelper = networkHelper) {

    fun pullData(
        locationUuid: String,
        pullExecutedTime: String
    ) = getResult { webClient.pullData(locationUuid, pullExecutedTime) }

}
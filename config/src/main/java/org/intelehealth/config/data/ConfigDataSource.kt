package org.intelehealth.config.data

import org.intelehealth.config.network.WebClient
import org.intelehealth.core.network.data.BaseDataSource

/**
 * Created by Vaghela Mithun R. on 10-04-2024 - 18:12.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class ConfigDataSource(
    private val webClient: WebClient,
    private val networkHelper: org.intelehealth.core.network.helper.NetworkHelper
) : BaseDataSource(networkHelper = networkHelper) {

    fun getConfig() = getResult { webClient.getPublishedConfig() }
}
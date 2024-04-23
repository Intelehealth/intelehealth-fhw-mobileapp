package org.intelehealth.config.data

import org.intelehealth.config.network.WebClient
import org.intelehealth.core.data.BaseDataSource
import org.intelehealth.core.network.helper.NetworkHelper

/**
 * Created by Vaghela Mithun R. on 10-04-2024 - 18:12.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class ConfigDataSource(
    private val webClient: WebClient,
    private val networkHelper: NetworkHelper
) : BaseDataSource(networkHelper = networkHelper) {

    fun getConfig() = getResult { webClient.getPublishedConfig() }
}
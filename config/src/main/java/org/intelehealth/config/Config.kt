package org.intelehealth.config

/**
 * Created by Vaghela Mithun R. on 10-04-2024 - 17:35.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
object Config {
    lateinit var baseUrl: String

    class Builder(baseUrl: String) {
        init {
            Config.baseUrl = baseUrl
        }
    }
}
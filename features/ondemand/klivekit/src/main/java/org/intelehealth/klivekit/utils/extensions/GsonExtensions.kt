package org.intelehealth.klivekit.utils.extensions

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


/**
 * Created by Vaghela Mithun R. on 17-03-2023 - 19:23.
 * Email : vaghela@codeglo.com
 * Mob   : +919727206702
 **/
inline fun <reified T> Gson.fromJson(json: String) = fromJson<T>(json, object : TypeToken<T>() {}.type)
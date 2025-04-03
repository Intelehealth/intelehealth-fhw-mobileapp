package org.intelehealth.klivekit.utils.extensions

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Created by Vaghela Mithun R. on 19-09-2023 - 16:32.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/

inline fun <reified T> Map<Any, Any>.toObject(type: Type): T {
    val gson = Gson()
    return gson.fromJson(gson.toJson(this), type)
}
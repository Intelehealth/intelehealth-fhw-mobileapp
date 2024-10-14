package org.intelehealth.feature.chat.listener

/**
 * Created by Vaghela Mithun R. on 08-07-2023 - 10:37.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
interface EventCallback<T> {
    fun onSuccess(result: T)
    fun onFail()
}
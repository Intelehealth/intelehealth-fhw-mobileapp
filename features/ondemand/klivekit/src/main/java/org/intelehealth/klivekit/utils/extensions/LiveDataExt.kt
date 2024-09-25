package org.intelehealth.klivekit.utils.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

fun <T> MutableLiveData<T>.hide(): LiveData<T> = this
fun <T> LiveData<T>.hide(): LiveData<T> = this
fun <T> MutableStateFlow<T>.hide(): StateFlow<T> = this
fun <T> Flow<T>.hide(): Flow<T> = this

inline fun <T, R> Flow<T?>.flatMapLatestOrNull(
    crossinline transform: suspend (value: T) -> Flow<R>
): Flow<R?> {
    return flatMapLatest {
        if (it == null) {
            flowOf(null)
        } else {
            transform(it)
        }
    }
}
package org.intelehealth.core.network.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import org.intelehealth.core.network.constants.NO_NETWORK
import org.intelehealth.core.network.state.*
import retrofit2.Response

/**
 * Abstract Base Data source class with error handling
 */
abstract class BaseDataSource(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val networkHelper: org.intelehealth.core.network.helper.NetworkHelper? = null
) {
    companion object {
        private const val TAG = "BaseDataSource"
        const val NOT_FOUND_404 = 404
        const val BAD_REQUEST_400 = 400
    }

    protected fun <T> getResult(call: suspend () -> Response<T>) = flow {
        if (isInternetAvailable()) {
            val response = call()
            if (response.isSuccessful) {
                println("API SUCCESS")
                val result = Result.Success(
                    response.body(),
                    response.message()
                )
                emit(result)
            } else {
                println("API ERROR ${response.message()}")
                emit(Result.Error<T>(response.message()))
            }
        } else Result.Fail<T>(NO_NETWORK)
    }.onStart {
        emit(Result.Loading<T>("Please wait..."))
    }.flowOn(dispatcher)

    private fun isInternetAvailable(): Boolean = networkHelper?.isNetworkConnected() ?: false
}


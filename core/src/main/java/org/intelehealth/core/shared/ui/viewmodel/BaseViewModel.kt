package org.intelehealth.core.shared.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.intelehealth.core.network.helper.NetworkHelper
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import org.intelehealth.core.network.service.ServiceResponse
import org.intelehealth.core.network.state.Result
import org.intelehealth.core.utility.NO_NETWORK

open class BaseViewModel(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val networkHelper: NetworkHelper? = null
) : ViewModel() {

    private val loadingData = MutableLiveData<Boolean>()

    @JvmField
    var loading: LiveData<Boolean> = loadingData

    private val failResult = MutableLiveData<String>()

    @JvmField
    var failDataResult: LiveData<String> = failResult

    private val errorResult = MutableLiveData<Throwable>()

    @JvmField
    var errorDataResult: LiveData<Throwable> = errorResult
    var dataConnectionStatus = MutableLiveData<Boolean>(true)

    fun <T> getDataResult(call: suspend () -> ServiceResponse<T>) = flow {
        if (isInternetAvailable()) {
            val response = call()
            if (response.status == 200) {
                println("API SUCCESS")
                val result = Result.Success(response.data, response.message)
                emit(result)
            } else {
                println("API ERROR ${response.message}")
                emit(Result.Error<T>(response.message))
            }
        } else dataConnectionStatus.postValue(false)
    }.onStart {
        emit(Result.Loading<T>("Please wait..."))
    }.flowOn(dispatcher)

    private fun isInternetAvailable(): Boolean = networkHelper?.isNetworkConnected() ?: false

    /**
     * Handle response here in base with loading and error message
     *
     */
    fun <T> handleResponse(it: Result<T>, callback: (data: T) -> Unit) {
        println("handleResponse status ${it.status} ${it.message}")
        when (it.status) {
            Result.State.LOADING -> {
                loadingData.postValue(true)
            }

            Result.State.FAIL -> {
                loadingData.postValue(false)
                if (it.message == NO_NETWORK)
                    dataConnectionStatus.postValue(false)
                else failResult.postValue("")
            }

            Result.State.SUCCESS -> {
                loadingData.postValue(false)
                it.data?.let { data ->
                    println("data ${Gson().toJson(data)}")
                    callback(data)
                } ?: failResult.postValue(it.message ?: "")
            }

            Result.State.ERROR -> {
                println("ERROR ${it.message}")
                loadingData.postValue(false)
                errorResult.postValue(Throwable(it.message))
            }
        }
    }
}
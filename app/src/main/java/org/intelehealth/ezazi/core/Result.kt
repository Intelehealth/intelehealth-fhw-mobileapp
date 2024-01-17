package org.intelehealth.ezazi.core


sealed class Result<out T>(val status: Status, val data: T?, var message: String?) {
    fun isSuccess(): Boolean {
        return this.status == Status.SUCCESS
    }

    fun isError(): Boolean {
        return this.status == Status.ERROR
    }

    class Success<T>(result: T?, message: String?) : Result<T>(Status.SUCCESS, result, message)
    class Fail<T>(message: String?) : Result<T>(Status.FAIL, null, message)
    class Error<T>(message: String?) : Result<T>(Status.ERROR, null, message)
    class Loading<T>(message: String?) : Result<T>(Status.LOADING, null, message)

    enum class Status {
        SUCCESS,
        FAIL,
        ERROR,
        LOADING
    }
}
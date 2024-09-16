package org.intelehealth.abdm.data.network

import com.google.gson.JsonParseException
import java.io.IOException

object ErrorFactory {
    fun getErrorMessage(e: Exception): String {
        return when (e) {
            is IOException -> ErrorMessage.IO_EXCEPTION
            is JsonParseException -> "${ErrorMessage.JSON_PARSE_EXCEPTION}: ${e.message}"
            else -> "${ErrorMessage.UNEXPECTED_ERROR}: ${e.localizedMessage}"
        }
    }

    fun getErrorMessageFromCode(code: Int): String {
        return when (code) {
            500 -> ErrorMessage.MESSAGE_500
            501 -> ErrorMessage.MESSAGE_501
            502 -> ErrorMessage.MESSAGE_502
            503 -> ErrorMessage.MESSAGE_503
            504 -> ErrorMessage.MESSAGE_504
            505 -> ErrorMessage.MESSAGE_505
            else -> ErrorMessage.DEFAULT_MESSAGE
        }
    }
}
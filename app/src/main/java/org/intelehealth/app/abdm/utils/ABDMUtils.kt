package org.intelehealth.app.abdm.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import org.intelehealth.app.abdm.model.ABDMErrorModel
import org.intelehealth.app.abdm.model.MobileLoginOnOTPVerifiedResponse
import org.intelehealth.app.abdm.model.OTPVerificationResponse
import retrofit2.Response
import java.lang.Exception

object ABDMUtils {
    fun formatIntoAbhaString(input: String): String? {
        val result = StringBuilder()
        val length = input.length
        val groupSizes = intArrayOf(2, 4, 4, 4) // The size of each group
        var startIndex = 0
        for (groupSize in groupSizes) {
            val endIndex = startIndex + groupSize
            result.append(input.substring(startIndex, endIndex))
            if (endIndex < length) {
                result.append("-")
            }
            startIndex = endIndex
        }
        return result.toString()
    }

    @JvmStatic
    fun isValidAbhaAddress(input: String): Boolean {
        val regex = "^[A-Za-z0-9]([A-Za-z0-9]*[._]?[A-Za-z0-9]*){6,16}[A-Za-z0-9]$".toRegex()
        return regex.matches(input)
    }


    @JvmStatic
    fun getErrorMessage(response: Response<OTPVerificationResponse>): String? {
        return try {
            val gson = Gson()
            val type = object : TypeToken<ABDMErrorModel>() {}.type
            val errorResponse: ABDMErrorModel? = gson.fromJson(response.errorBody()!!.charStream(), type)
            errorResponse?.message
        } catch (e: Exception) {
            "Something went wrong"
        }

    }


    @JvmStatic
    fun getErrorMessage1(responseBody: ResponseBody): String? {
        return try {
            val gson = Gson()
            val type = object : TypeToken<ABDMErrorModel>() {}.type
            val errorResponse: ABDMErrorModel? = gson.fromJson(responseBody.charStream(), type)
            errorResponse?.message
        } catch (e: Exception) {
            "Something went wrong"
        }

    }

}
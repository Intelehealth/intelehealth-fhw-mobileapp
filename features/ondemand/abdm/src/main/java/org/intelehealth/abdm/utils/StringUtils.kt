package org.intelehealth.abdm.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.intelehealth.abdm.model.OTPVerificationResponse
import org.intelehealth.app.abdm.model.ABDMErrorModel
import retrofit2.Response

object StringUtils {
    @JvmStatic
    fun getErrorMessage(response: Response<OTPVerificationResponse>): String? {
        return try {
            val gson = Gson()
            val type = object : TypeToken<ABDMErrorModel>() {}.type
            val errorResponse: ABDMErrorModel? =
                gson.fromJson(response.errorBody()!!.charStream(), type)
            errorResponse?.message
        } catch (e: Exception) {
            "Something went wrong"
        }

    }

    @JvmStatic
    fun extractLastFour(abhaNumber: String): String {
        return abhaNumber.substring(abhaNumber.length - 4)
    }
}
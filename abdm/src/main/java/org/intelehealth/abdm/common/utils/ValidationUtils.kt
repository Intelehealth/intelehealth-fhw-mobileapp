package org.intelehealth.abdm.common.utils

import java.util.regex.Pattern

object ValidationUtils {
     fun isValidAbhaRegex(input: String?): Boolean {
        val regex = "^(?!.*[._]{2})(?![._])[a-zA-Z0-9]+([._]?[a-zA-Z0-9]+)*$"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(input)
        return matcher.matches()
    }

    fun isValidAadhaar(num: String): Boolean {
        var c = 0
        val myArray = VerifyAadhaarAlgorithm.StringToReversedIntArray(num)
        for (i in myArray.indices) {
            c = VerifyAadhaarAlgorithm.d[c][VerifyAadhaarAlgorithm.p[i % 8][myArray[i]]]
        }

        return (c == 0)
    }
}
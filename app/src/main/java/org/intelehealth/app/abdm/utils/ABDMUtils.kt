package org.intelehealth.app.abdm.utils

object ABDMUtils{
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
}
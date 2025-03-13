package org.intelehealth.app.utilities

class CountryCodeUtils {
    companion object {
        @JvmStatic
        fun getPhoneCodeByCountry(local: String): Int {
            return if (local == "kz") {
                +996
            } else {
                +91
            }
        }
    }
}
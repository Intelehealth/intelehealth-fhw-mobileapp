package org.intelehealth.app.models


/**
 * Data class representing a single NCD (Non-Communicable Disease) reading
 *
 * @property date The date of the reading in format "dd MMM, yy"
 * @property bp Blood Pressure reading in format "systolic/diastolic"
 * @property hb Hemoglobin level in gm/dL
 * @property rbs Random Blood Sugar level in mg/dL
 */
data class NCDReading(
    val date: String?,
    val bp: String?,
    val hb: String?,
    val rbs: String?
) {
    companion object {
        const val NA = "N/A"

        /**
         * Sample data for demonstration
         */
        fun getSampleData(): List<NCDReading> {
            return listOf(
                NCDReading("15 Oct, 25", "148/92", "21", "90"),
                NCDReading("12 Oct, 25", "110/79", "22", null),
                NCDReading("11 Oct, 25", null, "15", "180"),
                NCDReading("10 Oct, 25", "148/99", "14", null),
                NCDReading("09 Oct, 25", "147/92", null, "190")
            )
        }
    }

    /**
     * Get display value for BP with N/A fallback
     */
    fun getBpDisplay(): String = bp ?: NA

    /**
     * Get display value for HB with N/A fallback
     */
    fun getHbDisplay(): String = hb ?: NA

    /**
     * Get display value for RBS with N/A fallback
     */
    fun getRbsDisplay(): String = rbs ?: NA

    /**
     * Get text color based on value status
     */
    fun getBpColor(context: android.content.Context): Int {
        return when {
            bp == null -> android.graphics.Color.parseColor("#9E9E9E")
            else -> android.graphics.Color.parseColor("#E91E63")
        }
    }

    fun getHbColor(context: android.content.Context): Int {
        return when {
            hb == null -> android.graphics.Color.parseColor("#9E9E9E")
            else -> android.graphics.Color.parseColor("#F44336")
        }
    }

    fun getRbsColor(context: android.content.Context): Int {
        return when {
            rbs == null -> android.graphics.Color.parseColor("#9E9E9E")
            else -> android.graphics.Color.parseColor("#4CAF50")
        }
    }
}

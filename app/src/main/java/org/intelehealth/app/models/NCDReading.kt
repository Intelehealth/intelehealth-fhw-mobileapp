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
    }
}

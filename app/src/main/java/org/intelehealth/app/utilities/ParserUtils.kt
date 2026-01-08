package org.intelehealth.app.utilities

import org.intelehealth.app.activities.visit.model.PrescribedMedicineModel
import java.util.regex.Matcher
import java.util.regex.Pattern


class ParserUtils {
    companion object{
        /**
         * Parses blood pressure from formatted text
         * @param text The text containing BP information
         * @return BloodPressure object or null if not found
         */
        @JvmStatic
        fun parseBP(text: String?): String? {
            if (text == null || text.isEmpty()) {
                return ""
            }


            // Pattern to match BP format: "BP Measurement - 121/80"
            // This pattern looks for numbers/numbers format after "BP Measurement"
            val pattern: Pattern =
                Pattern.compile("BP Measurement\\s*-\\s*(\\d+)/(\\d+)", Pattern.CASE_INSENSITIVE)
            val matcher: Matcher = pattern.matcher(text)

            if (matcher.find()) {
                try {
                    val systolic = matcher.group(1)
                    val diastolic = matcher.group(2)
                    return "$systolic/$diastolic"
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    return ""
                }
            }

            return ""
        }


        /**
         * Parses hemoglobin value from formatted text
         * @param text The text containing Hb information
         * @return Hemoglobin object or null if not found
         */
        @JvmStatic
        fun parseHemoglobin(text: String?): String? {
            if (text == null || text.isEmpty()) {
                return ""
            }


            // Pattern to match Hb format: "hb_measurement - Hemoglobin(Hb) Measurement - 17.0"
            // This pattern looks for decimal numbers after "Hemoglobin" or "Hb" measurement
            val pattern = Pattern.compile(
                "(?:hb_measurement|Hemoglobin|Hb)(?:\\s*\\([^)]*\\))?\\s+Measurement\\s*-\\s*([0-9]+\\.?[0-9]*)",
                Pattern.CASE_INSENSITIVE
            )
            val matcher = pattern.matcher(text)

            if (matcher.find()) {
                try {
                    val value = matcher.group(1)
                    return value
                } catch (e: java.lang.NumberFormatException) {
                    e.printStackTrace()
                    return ""
                }
            }

            return ""
        }


        /**
         * Pattern to match RBS format: "Random Blood Sugar(mg/dL) - 56"
         */
        @JvmStatic
        fun parseRBS(text: String?): String? {
            if (text == null || text.isEmpty()) {
                return ""
            }

            val pattern = Pattern.compile(
                "Random\\s+Blood\\s+Sugar\\s*\\(mg/dL\\)\\s*-\\s*([0-9]+\\.?[0-9]*)",
                Pattern.CASE_INSENSITIVE
            )
            val matcher = pattern.matcher(text)

            if (matcher.find()) {
                try {
                    val value = matcher.group(1)
                    return value
                } catch (e: java.lang.NumberFormatException) {
                    e.printStackTrace()
                    return ""
                }
            }

            return ""
        }
        fun parseMedication(data: String): Any? {
            //possible format
            //1. Acetazolamide: 250mg, Tablet 30 minutes before food (Subcutaneous) 0 - 0 - 1 for 12 days
            //2. Artesunate + Sulphadoxine Pyrimethamine: 250, Tablet 1 - 0 - 0 for 2 days
            //3. Artesunate + Sulphadoxine Pyrimethamine: 250, Tablet 1 - 0 - 0
            val regex = Regex(
                """^([^:]+):\s*([^,]+),\s*(.*?)(?:\s*\(([^)]+)\))?\s*([\d\s-]+)(?:\s*for\s*(.+))?$""",
                RegexOption.IGNORE_CASE
            )
            val match = regex.find(data.trim())
            if (match != null) {
                match.let {
                    val medicine = PrescribedMedicineModel()
                    medicine.medicineName = it.groupValues[1].trim()
                    medicine.strength = it.groupValues[2].trim()
                    medicine.remark = it.groupValues[3].trim()
                    //medicine.route = it.groupValues[4].trim()
                    medicine.timing = it.groupValues[5].trim()
                    medicine.noOfDays = it.groupValues[6].trim()

                    return medicine
                }
            } else {
                return data
            }
        }
    }
}
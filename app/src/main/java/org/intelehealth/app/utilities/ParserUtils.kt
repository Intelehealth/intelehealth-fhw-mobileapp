package org.intelehealth.app.utilities

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
               "(?:hb_measurement|Hemoglobin|Hb).*?Measurement\\s*-\\s*([0-9]+\\.?[0-9]*)",
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

   }
}
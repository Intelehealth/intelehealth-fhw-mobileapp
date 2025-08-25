package org.intelehealth.ncd.fhir

import org.json.JSONObject
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

object QuestionnaireUtils {

    fun questionnaireResponseToSummary(title: String, response: JSONObject): String {
        val bullets = mutableListOf<String>()
        val itemsArr = response.getJSONArray("item")

        for (i in 0 until itemsArr.length()) {
            val page = itemsArr.getJSONObject(i)
            val children = page.optJSONArray("item") ?: continue

            // 1. Find any nested _display text
            var label = ""
            fun findLabel(arr: JSONArray) {
                for (k in 0 until arr.length()) {
                    val obj = arr.getJSONObject(k)
                    if (obj.optString("linkId").endsWith("_display")) {
                        label = obj.optString("text")
                        return
                    }
                    obj.optJSONArray("item")?.let { findLabel(it) }
                }
            }
            findLabel(children)

            // 2. Collect all answer values under this page (within any nested level)
            val values = mutableListOf<String>()
            fun collectAnswers(arr: JSONArray) {
                for (k in 0 until arr.length()) {
                    val obj = arr.getJSONObject(k)
                    obj.optJSONArray("answer")?.let { ansArr ->
                        ansArr.optJSONObject(0)?.let { ans ->
                            ans.optInt("valueInteger").takeIf { it != 0 }
                                ?.let { values.add(it.toString()) }
                            ans.optString("valueString").takeIf { it.isNotEmpty() }
                                ?.let { values.add(it) }
                            ans.optString("valueDate").takeIf { it.isNotEmpty() }?.let { dateStr ->
                                val inFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                val outFmt = SimpleDateFormat("dd/MMM/yyyy", Locale.US)
                                values.add(outFmt.format(inFmt.parse(dateStr)!!))
                            }
                        }
                    }
                    obj.optJSONArray("item")?.let { collectAnswers(it) }
                }
            }
            collectAnswers(children)

            if (label.isNotEmpty() && values.isNotEmpty()) {
                val finalValue =
                    if (values.size == 2 && values[0].all { it.isDigit() } && values[1].all { it.isDigit() }) {
                        "${values[0]}/${values[1]}"
                    } else {
                        values.joinToString(", ")
                    }
                bullets.add("• $label - $finalValue")
            }
        }

        val bulletStr = bullets.joinToString("<br/>")
        return "►<b>${title}</b>: <br/>$bulletStr.<br/>"
    }

    // working fine except the BP
    fun questionnaireResponseToSummaryV1(
        title: String,
        questionnaire: JSONObject,
        response: JSONObject
    ): String {

        val bullets = mutableListOf<String>()

        fun collectAnswers(arr: JSONArray) {
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)

                val questionText = obj.optString("text").ifEmpty { obj.optString("linkId") }
                val ansArr = obj.optJSONArray("answer")

                if (ansArr != null) {
                    val vals = mutableListOf<String>()
                    for (a in 0 until ansArr.length()) {
                        val ans = ansArr.getJSONObject(a)

                        // Integer answers
                        if (ans.has("valueInteger")) {
                            vals.add(ans.getInt("valueInteger").toString())
                        }

                        // Decimal answers
                        if (ans.has("valueDecimal")) {
                            vals.add(ans.getDouble("valueDecimal").toString())
                        }

                        // Date answers
                        if (ans.has("valueDate")) {
                            val v = ans.getString("valueDate")
                            if (v.isNotEmpty()) {
                                val inf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                val outf = SimpleDateFormat("dd/MMM/yyyy", Locale.US)
                                vals.add(outf.format(inf.parse(v)!!))
                            }
                        }

                        // String answers
                        if (ans.has("valueString")) {
                            vals.add(ans.getString("valueString"))
                        }

                        // Coding answers
                        if (ans.has("valueCoding")) {
                            val coding = ans.getJSONObject("valueCoding")
                            vals.add(
                                coding.optString("display").ifEmpty {
                                    coding.optString("code")
                                }
                            )
                        }
                    }

                    if (vals.isNotEmpty()) {
                        bullets.add("• $questionText - ${vals.joinToString(", ")}")
                    }
                }

                // Check nested items
                obj.optJSONArray("item")?.let { collectAnswers(it) }
            }
        }

        response.optJSONArray("item")?.let { collectAnswers(it) }

        val bulletStr = bullets.joinToString("<br/>")
        return "►<b>${title}</b>: <br/>$bulletStr.<br/>"
    }


    fun questionnaireResponseToSummaryV3(
        title: String,
        questionnaire: JSONObject,
        response: JSONObject,
        showAllMeasurements: Boolean
    ): String {

        val bullets = mutableListOf<String>()

        /*fun collectAnswers(arr: JSONArray, parentTitle: String? = null) {
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)

                val questionText = obj.optString("text").ifEmpty { obj.optString("linkId") }
                val fullText = parentTitle?.let { "$it - $questionText" } ?: questionText

                val ansArr = obj.optJSONArray("answer")
                if (ansArr != null) {
                    val vals = mutableListOf<String>()
                    for (a in 0 until ansArr.length()) {
                        val ans = ansArr.getJSONObject(a)

                        if (ans.has("valueInteger")) vals.add(ans.getInt("valueInteger").toString())
                        if (ans.has("valueDecimal")) vals.add(ans.getDouble("valueDecimal").toString())

                        if (ans.has("valueDate")) {
                            val v = ans.getString("valueDate")
                            if (v.isNotEmpty()) {
                                val inf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                val outf = SimpleDateFormat("dd/MMM/yyyy", Locale.US)
                                vals.add(outf.format(inf.parse(v)!!))
                            }
                        }

                        if (ans.has("valueString")) vals.add(ans.getString("valueString"))

                        if (ans.has("valueCoding")) {
                            val coding = ans.getJSONObject("valueCoding")
                            vals.add(coding.optString("display").ifEmpty { coding.optString("code") })
                        }
                    }

                    if (vals.isNotEmpty()) {
                        bullets.add("• $fullText - ${vals.joinToString(", ")}")
                    }
                }

                val nextParent = if (ansArr == null && obj.optJSONArray("item") != null) questionText else parentTitle
                obj.optJSONArray("item")?.let { collectAnswers(it, nextParent) }
            }
        }*/
        fun collectAnswers(arr: JSONArray, parentTitle: String? = null) {
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)

                var questionText = obj.optString("text").ifEmpty { obj.optString("linkId") }
                // Skip parent title if it ends with _page
                val effectiveParent =
                    if (parentTitle?.endsWith("_page") == true) null else parentTitle
                val fullText = effectiveParent?.let { "$it - $questionText" } ?: questionText

                val ansArr = obj.optJSONArray("answer")
                if (ansArr != null) {
                    val vals = mutableListOf<String>()
                    for (a in 0 until ansArr.length()) {
                        val ans = ansArr.getJSONObject(a)

                        if (ans.has("valueInteger")) vals.add(ans.getInt("valueInteger").toString())
                        if (ans.has("valueDecimal")) vals.add(
                            ans.getDouble("valueDecimal").toString()
                        )

                        if (ans.has("valueDate")) {
                            val v = ans.getString("valueDate")
                            if (v.isNotEmpty()) {
                                val inf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                val outf = SimpleDateFormat("dd/MMM/yyyy", Locale.US)
                                vals.add(outf.format(inf.parse(v)!!))
                            }
                        }

                        if (ans.has("valueString")) vals.add(ans.getString("valueString"))

                        if (ans.has("valueCoding")) {
                            val coding = ans.getJSONObject("valueCoding")
                            vals.add(
                                coding.optString("display").ifEmpty { coding.optString("code") })
                        }
                    }

                    if (vals.isNotEmpty()) {
                        bullets.add("• $fullText - ${vals.joinToString(", ")}")
                    }
                }

                val nextParent =
                    if (ansArr == null && obj.optJSONArray("item") != null) questionText else effectiveParent
                obj.optJSONArray("item")?.let { collectAnswers(it, nextParent) }
            }
        }

        // Collect all answers
        /*response.optJSONArray("item")?.let { collectAnswers(it) }

        val filteredBullets = if (!showAllMeasurements) {
            val sbp = bullets.filter { it.contains("Systolic Blood Pressure", ignoreCase = true) }
                .lastOrNull()
            val dbp = bullets.filter { it.contains("Diastolic Blood Pressure", ignoreCase = true) }
                .lastOrNull()

            val adjusted = mutableListOf<String>()
            sbp?.let { adjusted.add(it.replace(Regex("BP Measurement \\d+"), "BP Measurement")) }
            dbp?.let { adjusted.add(it.replace(Regex("BP Measurement \\d+"), "BP Measurement")) }

            // Add remaining items except BP readings
            bullets.filter {
                !it.contains("Systolic Blood Pressure", ignoreCase = true) &&
                        !it.contains("Diastolic Blood Pressure", ignoreCase = true)
            }.let { adjusted.addAll(it) }

            adjusted
        } else {
            bullets
        }

        val bulletStr = filteredBullets.joinToString("<br/>")
        return "►<b>${title}</b>: <br/>$bulletStr.<br/>"*/

        // Collect all answers
        response.optJSONArray("item")?.let { collectAnswers(it) }

        for (i in bullets.indices) {
            val bulletPrefix = if (bullets[i].startsWith("•")) "• " else ""
            val content = bullets[i].removePrefix("•").trim()

            bullets[i] = bulletPrefix + content
                .split("-")
                .map { it.trim() }
                .filterNot { it.endsWith("_measurement", ignoreCase = true) }
                .joinToString(" - ")
        }



        /*val filteredBullets = if (!showAllMeasurements) {
            val sbpVal = bullets.lastOrNull { it.contains("Systolic Blood Pressure", ignoreCase = true) }
                ?.substringAfterLast("-")?.trim()
            val dbpVal = bullets.lastOrNull { it.contains("Diastolic Blood Pressure", ignoreCase = true) }
                ?.substringAfterLast("-")?.trim()

            val adjusted = mutableListOf<String>()
            if (sbpVal != null && dbpVal != null) {
                adjusted.add("• BP Measurement - SBP($sbpVal)/DBP($dbpVal)")
            }

            // Add remaining non-BP items
            bullets.filter {
                !it.contains("Systolic Blood Pressure", ignoreCase = true) &&
                        !it.contains("Diastolic Blood Pressure", ignoreCase = true)
            }.let { adjusted.addAll(it) }

            adjusted
        } else {
            bullets
        }*/
        val filteredBullets = if (!showAllMeasurements) {
            val sbpVal = bullets.findLast { it.contains("sbp_", ignoreCase = true) }
                ?.substringAfterLast("-")?.trim()
            val dbpVal = bullets.findLast { it.contains("dbp_", ignoreCase = true) }
                ?.substringAfterLast("-")?.trim()

            val adjusted = mutableListOf<String>()
            if (sbpVal != null && dbpVal != null) {
                adjusted.add("• BP Measurement - $sbpVal/$dbpVal")
            }

            // Add remaining items except BP readings
            bullets.filter {
                !it.contains("sbp_", ignoreCase = true) &&
                        !it.contains("dbp_", ignoreCase = true)
            }.let { adjusted.addAll(it) }

            adjusted
        } else {
            // Group SBP and DBP for each measurement number
            val bpGroups = mutableMapOf<String, Pair<String?, String?>>()

            bullets.forEach {
                val match = Regex("""BP Measurement\s*(\d+)""").find(it)
                val measurementNum = match?.groupValues?.get(1) ?: return@forEach

                val value = it.substringAfterLast("-").trim()
                if (it.contains("Systolic Blood Pressure", ignoreCase = true) ||
                    it.contains("sbp_", ignoreCase = true)) {
                    bpGroups[measurementNum] = (value to (bpGroups[measurementNum]?.second))
                } else if (it.contains("Diastolic Blood Pressure", ignoreCase = true) ||
                    it.contains("dbp_", ignoreCase = true)) {
                    bpGroups[measurementNum] = (bpGroups[measurementNum]?.first to value)
                }
            }

            val adjusted = mutableListOf<String>()
            bpGroups.toSortedMap(compareBy { it.toInt() }).forEach { (num, pair) ->
                val sbp = pair.first ?: "?"
                val dbp = pair.second ?: "?"
                adjusted.add("• BP Measurement $num - $sbp/$dbp")
            }

            // Add other bullets
            bullets.filter {
                !it.contains("Systolic Blood Pressure", ignoreCase = true) &&
                        //!it.contains("Diastolic Blood Pressure", ignoreCase = true) &&
                        !it.contains("sbp_", ignoreCase = true) &&
                        !it.contains("dbp_", ignoreCase = true)
            }.let { adjusted.addAll(it) }

            adjusted
        }


        val bulletStr = filteredBullets.joinToString("<br/>")
        return "►<b>${title}</b>: <br/>$bulletStr.<br/>"
    }


    fun questionnaireResponseToSummaryV4(
        title: String,
        questionnaire: JSONObject,
        response: JSONObject,
        showAllMeasurements: Boolean = false
    ): String {

        val bullets = mutableListOf<String>()

        fun cleanTitle(text: String): String {
            return text.replace("_page", "", ignoreCase = true).trim()
        }

        fun collectAnswers(arr: JSONArray, parentTitle: String? = null) {
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)

                val questionText =
                    cleanTitle(obj.optString("text").ifEmpty { obj.optString("linkId") })
                val fullText = parentTitle?.let { "$it - $questionText" } ?: questionText

                val ansArr = obj.optJSONArray("answer")
                if (ansArr != null) {
                    val vals = mutableListOf<String>()
                    for (a in 0 until ansArr.length()) {
                        val ans = ansArr.getJSONObject(a)

                        if (ans.has("valueInteger")) vals.add(ans.getInt("valueInteger").toString())
                        if (ans.has("valueDecimal")) vals.add(
                            ans.getDouble("valueDecimal").toString()
                        )

                        if (ans.has("valueDate")) {
                            val v = ans.getString("valueDate")
                            if (v.isNotEmpty()) {
                                val inf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                val outf = SimpleDateFormat("dd/MMM/yyyy", Locale.US)
                                vals.add(outf.format(inf.parse(v)!!))
                            }
                        }

                        if (ans.has("valueString")) vals.add(ans.getString("valueString"))

                        if (ans.has("valueCoding")) {
                            val coding = ans.getJSONObject("valueCoding")
                            vals.add(
                                coding.optString("display").ifEmpty { coding.optString("code") })
                        }
                    }

                    if (vals.isNotEmpty()) {
                        bullets.add("• $fullText - ${vals.joinToString(", ")}")
                    }
                }

                val nextParent =
                    if (ansArr == null && obj.optJSONArray("item") != null) questionText else parentTitle
                obj.optJSONArray("item")?.let { collectAnswers(it, nextParent) }
            }
        }

        // Collect all answers
        response.optJSONArray("item")?.let { collectAnswers(it) }

        val filteredBullets = if (!showAllMeasurements) {
            val sbpVal =
                bullets.filter { it.contains("Systolic Blood Pressure", ignoreCase = true) }
                    .lastOrNull()?.replace(Regex(".* - "), "") ?: ""
            val dbpVal =
                bullets.filter { it.contains("Diastolic Blood Pressure", ignoreCase = true) }
                    .lastOrNull()?.replace(Regex(".* - "), "") ?: ""

            val adjusted = mutableListOf<String>()
            if (sbpVal.isNotEmpty() || dbpVal.isNotEmpty()) {
                adjusted.add("• BP Measurement - SBP($sbpVal)/DBP($dbpVal)")
            }

            // Add remaining items except BP readings
            bullets.filter {
                !it.contains("Systolic Blood Pressure", ignoreCase = true) &&
                        !it.contains("Diastolic Blood Pressure", ignoreCase = true)
            }.let { adjusted.addAll(it) }

            adjusted
        } else {
            bullets
        }

        val bulletStr = filteredBullets.joinToString("<br/>")
        return "►<b>${title}</b>: <br/>$bulletStr.<br/>"
    }

    fun questionnaireResponseToSummary(
        title: String,
        questionnaire: JSONObject,
        response: JSONObject
    ): String {

        fun lookupDisplayFromQuestionnaire(code: String): String {
            questionnaire.getJSONArray("item")
                .firstOrNull { (it as JSONObject).optString("linkId") == "referral_page" }
                ?.let { (it as JSONObject).getJSONArray("item") }
                ?.firstOrNull { (it as JSONObject).optString("linkId") == "referral" }
                ?.let { ref ->
                    val opts = (ref as JSONObject).getJSONArray("answerOption")
                    for (i in 0 until opts.length()) {
                        val opt = opts.getJSONObject(i)
                        val coding = opt.getJSONObject("valueCoding")
                        if (coding.optString("code") == code) {
                            return coding.optString("display")
                        }
                    }
                }
            return code
        }

        val bullets = mutableListOf<String>()
        val itemsArr = response.getJSONArray("item")

        for (i in 0 until itemsArr.length()) {
            val page = itemsArr.getJSONObject(i)
            val children = page.optJSONArray("item") ?: continue

            var label = ""
            fun findLabel(arr: JSONArray) {
                for (k in 0 until arr.length()) {
                    val obj = arr.getJSONObject(k)
                    if (obj.optString("linkId").endsWith("_display")) {
                        label = obj.optString("text")
                        return
                    }
                    obj.optJSONArray("item")?.let { findLabel(it) }
                }
            }
            findLabel(children)

            val values = mutableListOf<String>()
            fun collectAnswers(arr: JSONArray) {
                for (k in 0 until arr.length()) {
                    val obj = arr.getJSONObject(k)
                    obj.optJSONArray("answer")?.let { ansArr ->
                        ansArr.optJSONObject(0)?.let { ans ->
                            ans.optInt("valueInteger").takeIf { it != 0 }
                                ?.let { values.add(it.toString()) }
                            ans.optString("valueDate")?.takeIf { it.isNotEmpty() }?.let { v ->
                                val inf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                val outf = SimpleDateFormat("dd/MMM/yyyy", Locale.US)
                                values.add(outf.format(inf.parse(v)!!))
                            }
                            ans.optString("valueString")?.takeIf { it.isNotEmpty() }?.let { v ->
                                // map outcome codes to display text
                                if (label.contains("Outcome")) {
                                    values.add(lookupDisplayFromQuestionnaire(v))
                                } else {
                                    values.add(v)
                                }
                            }
                        }
                    }
                    obj.optJSONArray("item")?.let { collectAnswers(it) }
                }
            }
            collectAnswers(children)

            if (label.isNotEmpty() && values.isNotEmpty()) {
                val finalValue =
                    if (values.size == 2 && values[0].all { it.isDigit() } && values[1].all { it.isDigit() }) {
                        "${values[0]}/${values[1]}"
                    } else {
                        values.joinToString(", ")
                    }
                bullets.add("• $label - $finalValue")
            }
        }

        val bulletStr = bullets.joinToString("<br/>")
        return "►<b>${title}</b>: <br/>$bulletStr.<br/>"
    }

    private fun JSONArray.firstOrNull(predicate: (Any) -> Boolean): Any? {
        for (i in 0 until length()) {
            val o = get(i)
            if (predicate(o)) return o
        }
        return null
    }

}
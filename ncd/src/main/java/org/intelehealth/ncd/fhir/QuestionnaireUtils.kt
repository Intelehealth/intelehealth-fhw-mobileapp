package org.intelehealth.ncd.fhir

import android.content.Context
import org.hl7.fhir.r4.model.Questionnaire
import org.intelehealth.ncd.R
import org.json.JSONObject
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

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

    fun findQuestionByLinkId(itemArray: JSONArray, linkId: String): JSONObject? {
        for (i in 0 until itemArray.length()) {
            val item = itemArray.getJSONObject(i)
            if (item.optString("linkId") == linkId) return item
            val childItems = item.optJSONArray("item")
            if (childItems != null) {
                val found = findQuestionByLinkId(childItems, linkId)
                if (found != null) return found
            }
        }
        return null
    }

    fun getLocalText(questionnaire: JSONObject, linkId: String, locale: String = "en"): String {
        val items = questionnaire.optJSONArray("item") ?: JSONArray()
        val questionObj = findQuestionByLinkId(items, linkId) ?: return linkId

        // If English, just return "text"
        if (locale.equals("en", ignoreCase = true)) return questionObj.optString("text")

        // Otherwise, look for _text.extension with the locale
        val textObj = questionObj.optJSONObject("_text")
        val extArray = textObj?.optJSONArray("extension")
        if (extArray != null) {
            for (i in 0 until extArray.length()) {
                val extItem = extArray.getJSONObject(i)
                if (extItem.optString("url") == "http://hl7.org/fhir/StructureDefinition/translation") {
                    val innerExt = extItem.optJSONArray("extension")
                    if (innerExt != null) {
                        var langCode: String? = null
                        var content: String? = null
                        for (j in 0 until innerExt.length()) {
                            val e = innerExt.getJSONObject(j)
                            when (e.optString("url")) {
                                "lang" -> langCode = e.optString("valueCode")
                                "content" -> content = e.optString("valueString")
                            }
                        }
                        if (langCode.equals(locale, ignoreCase = true)) return content
                            ?: questionObj.optString("text")
                    }
                }
            }
        }

        return questionObj.optString("text") // fallback to English
    }

    fun getCodingDisplayWithLocale(
        valueCoding: JSONObject,
        locale: String = "en"
    ): Pair<String, String> {
        // Default English display
        val displayEn = valueCoding.optString("display").ifEmpty { valueCoding.optString("code") }

        // Check for _display.extension for locale
        val textLocale = try {
            val displayObj = valueCoding.optJSONObject("_display")
            val extArray = displayObj?.optJSONArray("extension")
            if (extArray != null) {
                for (i in 0 until extArray.length()) {
                    val extItem = extArray.getJSONObject(i)
                    if (extItem.optString("url") == "http://hl7.org/fhir/StructureDefinition/translation") {
                        val innerExt = extItem.optJSONArray("extension")
                        if (innerExt != null) {
                            var langCode: String? = null
                            var content: String? = null
                            for (j in 0 until innerExt.length()) {
                                val e = innerExt.getJSONObject(j)
                                when (e.optString("url")) {
                                    "lang" -> langCode = e.optString("valueCode")
                                    "content" -> content = e.optString("valueString")
                                }
                            }
                            if (langCode.equals(locale, ignoreCase = true)) return Pair(
                                displayEn,
                                content ?: displayEn
                            )
                        }
                    }
                }
            }
            displayEn
        } catch (e: Exception) {
            displayEn
        }

        return Pair(displayEn, textLocale)
    }

    fun checkChoiceType(questionnaire: JSONObject, linkId: String): Boolean {
        val items = questionnaire.optJSONArray("item") ?: JSONArray()
        val questionObj = findQuestionByLinkId(items, linkId) ?: return false

        val type = questionObj.optString("type")
        return type.equals("choice", ignoreCase = true) || type.equals(
            "open-choice",
            ignoreCase = true
        )
    }

    fun getAnswerOptionDisplay(
        answerOptions: JSONArray,
        valueString: String,
        locale: String = "en"
    ): Pair<String, String> {
        for (i in 0 until answerOptions.length()) {
            val option = answerOptions.getJSONObject(i)
            val coding = option.optJSONObject("valueCoding") ?: continue

            val code = coding.optString("code")
            val displayEn = coding.optString("display").ifEmpty { code }

            if (displayEn.equals(valueString, ignoreCase = true) || code.equals(
                    valueString,
                    ignoreCase = true
                )
            ) {
                // Check _display.extension for locale
                val displayLocale = try {
                    val displayObj = coding.optJSONObject("_display")
                    val extArray = displayObj?.optJSONArray("extension")
                    if (extArray != null) {
                        for (j in 0 until extArray.length()) {
                            val extItem = extArray.getJSONObject(j)
                            if (extItem.optString("url") == "http://hl7.org/fhir/StructureDefinition/translation") {
                                val innerExt = extItem.optJSONArray("extension")
                                if (innerExt != null) {
                                    var langCode: String? = null
                                    var content: String? = null
                                    for (k in 0 until innerExt.length()) {
                                        val e = innerExt.getJSONObject(k)
                                        when (e.optString("url")) {
                                            "lang" -> langCode = e.optString("valueCode")
                                            "content" -> content = e.optString("valueString")
                                        }
                                    }
                                    if (langCode.equals(locale, ignoreCase = true)) return Pair(
                                        displayEn,
                                        content ?: displayEn
                                    )
                                }
                            }
                        }
                    }
                    displayEn
                } catch (e: Exception) {
                    displayEn
                }

                return Pair(displayEn, displayLocale)
            }
        }

        // fallback
        return Pair(valueString, valueString)
    }
    fun getQuestionnaireTitle(questionnaire: JSONObject, locale: String = "en"): Pair<String, String> {
        // Default English title
        val titleEn = questionnaire.optString("title")

        // Look for _title.extension
        val titleLocale = try {
            val titleObj = questionnaire.optJSONObject("_title")
            val extArray = titleObj?.optJSONArray("extension")
            if (extArray != null) {
                for (i in 0 until extArray.length()) {
                    val extItem = extArray.getJSONObject(i)
                    if (extItem.optString("url") == "http://hl7.org/fhir/StructureDefinition/translation") {
                        val innerExt = extItem.optJSONArray("extension")
                        if (innerExt != null) {
                            var langCode: String? = null
                            var content: String? = null
                            for (j in 0 until innerExt.length()) {
                                val e = innerExt.getJSONObject(j)
                                when (e.optString("url")) {
                                    "lang" -> langCode = e.optString("valueCode")
                                    "content" -> content = e.optString("valueString")
                                }
                            }
                            if (langCode.equals(locale, ignoreCase = true)) return Pair(titleEn, content ?: titleEn)
                        }
                    }
                }
            }
            titleEn
        } catch (e: Exception) {
            titleEn
        }

        return Pair(titleEn, titleLocale)
    }

    fun questionnaireResponseToSummaryV3(
        context: Context,
        title: String,
        questionnaire: JSONObject,
        response: JSONObject,
        showAllMeasurements: Boolean,
        localeLang: String
    ): Pair<String, String> {

        val bullets = mutableListOf<String>()
        val bulletsEnglish = mutableListOf<String>()
        val bulletsLocal = mutableListOf<String>()

        // Collect all answers
        fun collectAnswers(arr: JSONArray, parentTitle: String? = null) {
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)

                var questionText = obj.optString("text").ifEmpty { obj.optString("linkId") }
                var questionTextLocal =
                    getLocalText(questionnaire, obj.optString("linkId"), localeLang).takeIf { it.isNotEmpty() } ?: questionText
                var questionTextEnglish = getLocalText(questionnaire, obj.optString("linkId"), "en").takeIf { it.isNotEmpty() } ?: questionText
                // Skip parent title if it ends with _page
                val effectiveParent =
                    if (parentTitle?.endsWith("_page") == true) null else parentTitle
                val fullText = effectiveParent?.let { "$it - $questionText" } ?: questionText
                val fullTextEnglish =
                    effectiveParent?.let { "$it - $questionTextEnglish" } ?: questionTextEnglish
                val fullTextLocal =
                    effectiveParent?.let { "$it - $questionTextLocal" } ?: questionTextLocal

                val ansArr = obj.optJSONArray("answer")
                if (ansArr != null) {
                    val vals = mutableListOf<String>()
                    val valsEnglish = mutableListOf<String>()
                    val valsLocal = mutableListOf<String>()
                    for (a in 0 until ansArr.length()) {
                        val ans = ansArr.getJSONObject(a)

                        if (ans.has("valueInteger")) {
                            vals.add(ans.getInt("valueInteger").toString())
                            valsEnglish.add(ans.getInt("valueInteger").toString())
                            valsLocal.add(ans.getInt("valueInteger").toString())
                        }
                        if (ans.has("valueDecimal")) {
                            vals.add(ans.getDouble("valueDecimal").toString())
                            valsEnglish.add(ans.getDouble("valueDecimal").toString())
                            valsLocal.add(ans.getDouble("valueDecimal").toString())
                        }

                        if (ans.has("valueDate")) {
                            val v = ans.getString("valueDate")
                            if (v.isNotEmpty()) {
                                val inf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                var outf = SimpleDateFormat("dd/MMM/yyyy", Locale.US)
                                vals.add(outf.format(inf.parse(v)!!))
                                valsEnglish.add(outf.format(inf.parse(v)!!))
                                outf = SimpleDateFormat("dd/MMM/yyyy", Locale(localeLang))
                                valsLocal.add(outf.format(inf.parse(v)!!))
                            }
                        }

                        if (ans.has("valueString")) {
                            val valueStr = ans.getString("valueString")
                            val isChoiceType =
                                checkChoiceType(questionnaire, obj.optString("linkId"))

                            if (isChoiceType) {
                                // Find the answerOption array in the question
                                val questionObj = findQuestionByLinkId(
                                    questionnaire.optJSONArray("item")!!,
                                    obj.optString("linkId")
                                )
                                val answerOptions =
                                    questionObj?.optJSONArray("answerOption") ?: JSONArray()

                                val (displayEn, displayLocale) = getAnswerOptionDisplay(
                                    answerOptions,
                                    valueStr,
                                    localeLang
                                )

                                vals.add(displayEn)
                                valsEnglish.add(displayEn)
                                valsLocal.add(displayLocale)
                            } else {
                                vals.add(valueStr)
                                valsEnglish.add(valueStr)
                                valsLocal.add(valueStr)
                            }
                        }

                        if (ans.has("valueCoding")) {
                            val coding = ans.getJSONObject("valueCoding")
                            val (displayEn, displayLocale) = getCodingDisplayWithLocale(
                                coding,
                                localeLang
                            ) // or "bn", "gu", etc.
                            /*vals.add(
                                coding.optString("display").ifEmpty { coding.optString("code") })*/
                            vals.add(displayEn)
                            valsEnglish.add(displayEn)
                            valsLocal.add(displayLocale)
                        }
                    }

                    if (vals.isNotEmpty()) {
                        bullets.add("• $fullText - ${vals.joinToString(", ")}")
                    }

                    if (vals.isNotEmpty()) {
                        bulletsEnglish.add("• $fullTextEnglish - ${vals.joinToString(", ")}")
                    }
                    if (valsLocal.isNotEmpty()) {
                        bulletsLocal.add("• $fullTextLocal - ${valsLocal.joinToString(", ")}")
                    }
                }

                val nextParent =
                    if (ansArr == null && obj.optJSONArray("item") != null) questionTextEnglish else effectiveParent
                obj.optJSONArray("item")?.let { collectAnswers(it, nextParent) }
            }
        }

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

        for (i in bulletsLocal.indices) {
            val bulletPrefix = if (bulletsLocal[i].startsWith("•")) "• " else ""
            val content = bulletsLocal[i].removePrefix("•").trim()

            bulletsLocal[i] = bulletPrefix + content
                .split("-")
                .map { it.trim() }
                .filterNot { it.endsWith("_measurement", ignoreCase = true) }
                .joinToString(" - ")
        }

        val (filteredBulletsEnglish, filteredBulletsLocal) = if (!showAllMeasurements) {
            val sbpVal = bullets.findLast { it.contains("sbp_", ignoreCase = true) }
                ?.substringAfterLast("-")?.trim()
            val dbpVal = bullets.findLast { it.contains("dbp_", ignoreCase = true) }
                ?.substringAfterLast("-")?.trim()

            val adjustedEnglish = mutableListOf<String>()
            val adjustedLocal = mutableListOf<String>()
            if (sbpVal != null && dbpVal != null) {
                // for en
                adjustedEnglish.add("• BP Measurement - $sbpVal/$dbpVal")
                // for locale lang
                val bpMeasurement = context.getString(R.string.bp_measurement, sbpVal, dbpVal)

                adjustedLocal.add("• $bpMeasurement")
            }

            // Add remaining items except BP readings
            bulletsEnglish.filter {
                !it.contains("sbp_", ignoreCase = true) &&
                        !it.contains("dbp_", ignoreCase = true)
            }.let { adjustedEnglish.addAll(it) }

            bulletsLocal.filter {
                !it.contains("sbp_", ignoreCase = true) &&
                        !it.contains("dbp_", ignoreCase = true)
            }.let { adjustedLocal.addAll(it) }

            // return both
            Pair (adjustedEnglish,adjustedLocal)


        } else {
            //TODO: this else section is completed out of scope now so translation is not managed for this case
            // so if required then we need to manage that
            // Group SBP and DBP for each measurement number
            val bpGroups = mutableMapOf<String, Pair<String?, String?>>()

            bullets.forEach {
                val match = Regex("""BP Measurement\s*(\d+)""").find(it)
                val measurementNum = match?.groupValues?.get(1) ?: return@forEach

                val value = it.substringAfterLast("-").trim()
                if (it.contains("Systolic Blood Pressure", ignoreCase = true) ||
                    it.contains("sbp_", ignoreCase = true)
                ) {
                    bpGroups[measurementNum] = (value to (bpGroups[measurementNum]?.second))
                } else if (it.contains("Diastolic Blood Pressure", ignoreCase = true) ||
                    it.contains("dbp_", ignoreCase = true)
                ) {
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

            Pair(adjusted, mutableListOf<String>())
        }

        val bulletStrEnglish = filteredBulletsEnglish.joinToString("<br/>")
        val bulletStrLocal = filteredBulletsLocal.joinToString("<br/>")
        val (titleEn, titleLocal) = getQuestionnaireTitle(questionnaire, localeLang)
        val finalStrEnglish = "►<b>${titleEn}</b>: <br/>$bulletStrEnglish.<br/>"
        val finalStrLocal = "►<b>${titleLocal}</b>: <br/>$bulletStrLocal.<br/>"
        return Pair(finalStrEnglish, finalStrLocal)
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


    /**
     * Check required questions in a FHIR Questionnaire against a QuestionnaireResponse,
     */


    fun checkRequiredWithConditionalsKotlin(
        questionnaireJsonStr: String,
        responseJsonStr: String
    ): List<String> {
        val questionnaire = JSONObject(questionnaireJsonStr)
        val response = JSONObject(responseJsonStr)

        // 1) Index response items by linkId (may be multiple entries)
        val respIndex: MutableMap<String, MutableList<JSONObject>> = mutableMapOf()

        fun indexResponse(items: JSONArray?) {
            if (items == null) return
            for (i in 0 until items.length()) {
                val it = items.getJSONObject(i)
                val lid = it.optString("linkId")
                if (lid.isNotEmpty()) {
                    respIndex.computeIfAbsent(lid) { mutableListOf() }.add(it)
                }
                if (it.has("item")) indexResponse(it.getJSONArray("item"))
            }
        }

        indexResponse(response.optJSONArray("item"))

        // 2) Helper: does this response item count as answered?
        fun responseItemHasAnswerOneLevel(rItem: JSONObject): Boolean {
            val ans = rItem.optJSONArray("answer")
            if (ans != null && ans.length() > 0) return true

            val childItems = rItem.optJSONArray("item")
            if (childItems != null) {
                for (ci in 0 until childItems.length()) {
                    val child = childItems.getJSONObject(ci)
                    val cans = child.optJSONArray("answer")
                    if (cans != null && cans.length() > 0) return true
                }
            }
            return false
        }

        // 3) Build respAnsweredMap: linkId -> answered(boolean)
        val respAnsweredMap: MutableMap<String, Boolean> = mutableMapOf()
        for ((linkId, entries) in respIndex) {
            var anyAnswered = false
            for (entry in entries) {
                if (responseItemHasAnswerOneLevel(entry)) {
                    anyAnswered = true
                    break
                }
            }
            respAnsweredMap[linkId] = anyAnswered
        }

        // 4) Index questionnaire items by linkId for quick lookup (deep)
        val qIndex: MutableMap<String, JSONObject> = mutableMapOf()

        fun indexQuestionnaire(items: JSONArray?) {
            if (items == null) return
            for (i in 0 until items.length()) {
                val it = items.getJSONObject(i)
                val lid = it.optString("linkId")
                if (lid.isNotEmpty()) qIndex[lid] = it
                if (it.has("item")) indexQuestionnaire(it.getJSONArray("item"))
            }
        }

        indexQuestionnaire(questionnaire.optJSONArray("item"))

        val missing = mutableListOf<String>()

        // 5) For each linkId present in response index, if it's not answered check if questionnaire marks it required
        for ((linkId, answered) in respAnsweredMap) {
            if (!answered) {
                // skip display suffix ids
                if (linkId.endsWith("_display")) continue
                if (linkId.endsWith("_page")) continue
                val qItem = qIndex[linkId]
                if (qItem != null && qItem.optBoolean("required", false)) {
                    missing.add(qItem.optString("text").ifEmpty { linkId })
                }
            }
        }

        // 6) Also check questionnaire items that are required but have no entry in response at all
        /*for ((linkId, qItem) in qIndex) {
            if (linkId.endsWith("_display")) continue
            val required = qItem.optBoolean("required", false)
            if (!required) continue

            val answered = respAnsweredMap[linkId] ?: false
            // if no response entry or response exists but not answered, add to missing
            if (!answered) {
                // avoid duplicates
                val label = qItem.optString("text").ifEmpty { linkId }
                if (!missing.contains(label)) missing.add(label)
            }
        }*/

        return missing
    }

    /*fun getQuestionText(question: Questionnaire.QuestionnaireItemComponent, locale: Locale): String {
        question.text?.let { text ->
            // Check if _text and extensions exist
            question.extension?.forEach { ext ->
                if (ext.url == "http://hl7.org/fhir/StructureDefinition/translation") {
                    val langExt = ext.extension.find { it.url == "lang" }?.valueCode
                    val contentExt = ext.extension.find { it.url == "content" }?.valueString
                    if (langExt != null && langExt.equals(locale.language, ignoreCase = true)) {
                        return contentExt ?: text
                    }
                }
            }
            return text // default English
        }
        return ""
    }*/


}
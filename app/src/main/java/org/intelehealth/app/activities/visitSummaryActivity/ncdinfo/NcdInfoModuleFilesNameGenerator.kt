package org.intelehealth.app.activities.visitSummaryActivity.ncdinfo

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import org.intelehealth.ncd.constants.Constants.NCD_HEALTH_INFO_MODULES
import java.util.regex.Pattern


class NcdInfoModuleFilesNameGenerator {
    private val TAG = "NcdInfoModuleFilesNameG"

    private fun splitComplaintBlocks(text: String): List<String> {
        return text.split("►")
            .map { it.trim() }
            .filter { it.startsWith("<b>", ignoreCase = true) }
    }
    private fun extractChiefComplaint(text: String): String {
        val pattern = Pattern.compile("<b>(.*?)</b>", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)
        return if (matcher.find()) matcher.group(1).trim() else ""
    }
    private fun extractInformationModules(text: String): List<String> {
        val pattern = Pattern.compile(
            "Information modules - (.*?)(<br/>|$)",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = pattern.matcher(text)

        return if (matcher.find()) {
            matcher.group(1)
                ?.split(", ") // split modules by comma + space
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?: emptyList()
        } else {
            emptyList()
        }
    }

    private fun isInfoModuleAllowed(chiefComplaint: String): Boolean {
        val name = chiefComplaint.lowercase()
        return name.contains("followup") || name.toLowerCase().contains("diabetes screening")
    }
    fun generateModulesNew(
        complaintDetails: String,
        languageCode: String,
        context: Context
    ): List<HealthModuleItem> {

        val result = mutableListOf<HealthModuleItem>()
        val blocks = splitComplaintBlocks(complaintDetails)

        val commonModules = setOf("exercise") // modules to keep only once
        val seenCommonModules = mutableSetOf<String>()
        val exerciseItems = mutableListOf<HealthModuleItem>()

        blocks.forEach { block ->
            val chiefComplaint = extractChiefComplaint(block)
            if (chiefComplaint.isEmpty()) return@forEach
            if (!isInfoModuleAllowed(chiefComplaint)) return@forEach
            val modules = extractInformationModules(block)
            if (modules.isEmpty()) return@forEach

            val baseName = chiefComplaint.lowercase().replace("\\s+".toRegex(), "_")

            modules.forEach { module ->
                val moduleLower = module.lowercase()

                val normalizedModuleNameForUrl = moduleLower.replace("[\\s-]+".toRegex(), "_")
                val moduleFileName = "${baseName}_${normalizedModuleNameForUrl}_${languageCode}.pdf"

                val item = HealthModuleItem(
                    moduleName = module,
                    url = "$NCD_HEALTH_INFO_MODULES$moduleFileName"
                )
                item.displayName = HealthModuleTitleMapper.getDisplayName(context, module, chiefComplaint)

                if (moduleLower in commonModules) {
                    // store exercise separately to add later at the bottom
                    if (seenCommonModules.add(moduleLower)) {
                        exerciseItems.add(item)
                    }
                } else {
                    result.add(item) // add normal modules immediately
                }
            }
        }
        result.addAll(exerciseItems)

        return result
    }

}


package org.intelehealth.app.activities.visitSummaryActivity.ncdinfo

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import java.util.regex.Pattern


class NcdInfoModuleFilesNameGenerator {
    private val TAG = "NcdInfoModuleFilesNameG"

    private val baseUrl = "https://afitraining.ekalarogya.org:3004/ncdinfo/"

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

        blocks.forEach { block ->
            val chiefComplaint = extractChiefComplaint(block)
            if (chiefComplaint.isEmpty()) return@forEach
            Log.d(TAG, "generateModulesNew: chiefComplaint : "+chiefComplaint)
            // Allow only follow-up protocols + diabetes screening
            if (!isInfoModuleAllowed(chiefComplaint)) return@forEach
            Log.d(TAG, "generateModulesNew: chiefComplaint 11: "+chiefComplaint)

            val modules = extractInformationModules(block)
            if (modules.isEmpty()) return@forEach

            val baseName = chiefComplaint.lowercase().replace("\\s+".toRegex(), "_")

            modules.forEach { module ->
                val moduleLower = module.lowercase()

                // Deduplicate only common modules
                if (moduleLower in commonModules && !seenCommonModules.add(moduleLower)) return@forEach

                val normalizedModuleNameForUrl = moduleLower.replace("[\\s-]+".toRegex(), "_")

                val moduleFileName = "${baseName}_${normalizedModuleNameForUrl}_${languageCode}.pdf"

                val item = HealthModuleItem(
                    moduleName = module,
                    url = "$baseUrl$moduleFileName"
                )
                item.displayName = HealthModuleTitleMapper.getDisplayName(context, module,chiefComplaint)

                result.add(item)
            }
        }

        return result
    }

}


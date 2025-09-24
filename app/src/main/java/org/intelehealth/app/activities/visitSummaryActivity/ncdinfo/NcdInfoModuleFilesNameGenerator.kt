package org.intelehealth.app.activities.visitSummaryActivity.ncdinfo

import android.util.Log
import java.util.regex.Pattern


class NcdInfoModuleFilesNameGenerator {

    private val baseUrl = "https://afitraining.ekalarogya.org:3004/ncdinfo/"

    // Extract chief complaint
    private fun extractChiefComplaint(text: String): String {
        val pattern = Pattern.compile("<b>(.*?)</b>", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)
        return if (matcher.find()) matcher.group(1).trim() else ""
    }

    // Extract information modules list
    private fun extractInformationModules(text: String): List<String> {
        val pattern = Pattern.compile("Information modules - (.*?)<br/>", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)

        return if (matcher.find()) {
            matcher.group(1)
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?: emptyList()
        } else {
            emptyList()
        }
    }

    // Generate filenames
    fun generateFileNames(complaintDetails: String, languageCode: String): List<String> {
        val chiefComplaint = extractChiefComplaint(complaintDetails)
        if (chiefComplaint.isEmpty()) return emptyList()

        val modules = extractInformationModules(complaintDetails)
        if (modules.isEmpty()) return emptyList()

        val baseName = chiefComplaint.lowercase().replace("\\s+".toRegex(), "_")

        return modules.map { module ->
            val moduleName = module.lowercase().replace("\\s+".toRegex(), "_")
            "${baseName}_${moduleName}_${languageCode}.pdf"
        }
    }

    // Generate full URLs for each file
    fun generateFileUrls(complaintDetails: String, languageCode: String): List<String> {
        val fileNames = generateFileNames(complaintDetails, languageCode)
        return fileNames.map { "$baseUrl$it" }
    }
}


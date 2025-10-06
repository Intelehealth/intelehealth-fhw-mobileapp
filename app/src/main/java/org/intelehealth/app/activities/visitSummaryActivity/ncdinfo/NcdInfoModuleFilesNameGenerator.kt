package org.intelehealth.app.activities.visitSummaryActivity.ncdinfo

import android.util.Log
import com.google.gson.Gson
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
        Log.d("TAG", "extractInformationModules: text : "+text)
        val pattern = Pattern.compile("Information modules - (.*?)<br/>", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)

        return if (matcher.find()) {
            val     i = 0
            matcher.group(1)
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?: emptyList()
        } else {
            emptyList()
        }
    }


    // Generate list of HealthModuleItem with moduleName + URL
    fun generateModulesNew(complaintDetails: String, languageCode: String): List<HealthModuleItem> {
        val chiefComplaint = extractChiefComplaint(complaintDetails)
        if (chiefComplaint.isEmpty()) return emptyList()

        val modules = extractInformationModules(complaintDetails)
        Log.d("TAG", "generateModulesNew: modules : "+modules.size)
        Log.d("TAG", "generateModulesNew: modules : "+Gson().toJson(modules))

        if (modules.isEmpty()) return emptyList()

        val baseName = chiefComplaint.lowercase().replace("\\s+".toRegex(), "_")

        return modules.map { module ->
            val moduleFileName = "${baseName}_${module.lowercase().replace("\\s+".toRegex(), "_")}_${languageCode}.pdf"
            HealthModuleItem(
                moduleName = module,         //name
                url = "$baseUrl$moduleFileName" // full URL
            )
        }
    }

}


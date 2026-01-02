package org.intelehealth.app.activities.visitSummaryActivity.ncdinfo

import android.content.Context
import android.util.Log
import org.intelehealth.app.R
object HealthModuleTitleMapper {

    fun getDisplayName(
        context: Context,
        moduleName: String,
        chiefComplaint: String
    ): String {

        val name = moduleName.lowercase()
        val complaint = chiefComplaint.lowercase()
        return when {

            // -------- ANEMIA --------
            complaint.contains("anemia") && "iron" in name ->
                context.getString(R.string.anemia_food_advice)

            complaint.contains("anemia") && "deworm" in name ->
                context.getString(R.string.anemia_deworm_hygiene)

            complaint.contains("anemia") && ("alcohol" in name || "tobacco" in name) ->
                context.getString(R.string.anemia_quit_alcohol_tobacco)

            // -------- DIABETES --------
            complaint.contains("diabetes") && "sugar" in name ->
                context.getString(R.string.diabetes_reduce_sugar)

            complaint.contains("diabetes") && ("alcohol" in name || "tobacco" in name) ->
                context.getString(R.string.diabetes_quit_alcohol_tobacco)

            // -------- HYPERTENSION --------
            complaint.contains("hypertension") && "salt" in name ->
                context.getString(R.string.hypertension_reduce_salt)

            complaint.contains("hypertension") && ("alcohol" in name || "tobacco" in name) ->
                context.getString(R.string.hypertension_quit_alcohol_tobacco)

            // -------- COMMON --------
            "exercise" in name ->
                context.getString(R.string.exercise_regularly)

            else ->
                moduleName.replaceFirstChar { it.uppercase() }
        }
    }
}

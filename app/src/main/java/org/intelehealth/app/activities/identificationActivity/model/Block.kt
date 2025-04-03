package org.intelehealth.app.activities.identificationActivity.model

import com.google.gson.annotations.SerializedName
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.utilities.SessionManager

/**
 * Created by Vaghela Mithun R. on 18-06-2024 - 19:27.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
data class Block(
    val name: String?,
    @SerializedName("name-hi")
    val nameHindi: String?,
    @SerializedName("Gram Panchayat")
    val gramPanchayats: List<GramPanchayat>?,
    @SerializedName("name-mr")
    val nameMarathi: String?,
) {
    override fun toString(): String {
        val sessionManager = SessionManager.getInstance(IntelehealthApplication.getAppContext())
        return when (sessionManager.appLanguage) {
            "hi" -> nameHindi ?: name ?: "No Value"
            "mr" -> nameMarathi ?: name ?: "No Value"
            else -> name ?: "No Value"
        }
    }
}

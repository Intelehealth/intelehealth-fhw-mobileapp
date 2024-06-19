package org.intelehealth.app.activities.identificationActivity.model

import com.google.gson.annotations.SerializedName

/**
 * Created by Vaghela Mithun R. on 18-06-2024 - 19:28.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
data class GramPanchayat(
    val name: String?,
    @SerializedName("village")
    val villages: List<String>?
)
package org.intelehealth.config.network.response

import com.google.gson.annotations.SerializedName
import org.intelehealth.config.room.entity.ActiveLanguage
import org.intelehealth.config.room.entity.Specialization

/**
 * Created by Vaghela Mithun R. on 10-04-2024 - 17:31.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
data class ConfigResponse(
    val specialization: List<Specialization>,
    val language: List<ActiveLanguage>,
    @SerializedName("patient_registration")
    val patientRegFields: PatientRegFieldConfig,
    val version: Int = 0
)
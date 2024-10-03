package org.intelehealth.config.network.response

import com.google.gson.annotations.SerializedName
import org.intelehealth.config.room.entity.ActiveLanguage
import org.intelehealth.config.room.entity.FeatureActiveStatus
import org.intelehealth.config.room.entity.PatientVital
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
    @SerializedName("patient_vitals")
    val patientVitals: List<PatientVital>,
    @SerializedName("patient_visit_summary")
    val patientVisitSummery: FeatureActiveStatus,
    @SerializedName("patient_vitals_section")
    val patientVitalSection: Boolean,
    @SerializedName("webrtc")
    val webrtcStatus: WebRtcActiveStatus,
    @SerializedName("webrtc_section")
    val webrtcSection: Boolean = true,
    @SerializedName("patient_reg_address")
    var activeStatusPatientAddress: Boolean = true,
    @SerializedName("patient_reg_other")
    var activeStatusPatientOther: Boolean = true,
    @SerializedName("abha_section")
    var activeStatusAbha: Boolean = true,
    val version: Int = 0
)
package org.intelehealth.app.activities.visit.staticEnabledFields

import org.intelehealth.config.room.entity.Specialization

object SpecializationsEnabledFieldsHelper {
    fun getSpecializations(isNCDVisit: Boolean) = mutableListOf(
        Specialization(
            sKey = "",
            // if isNCDVisit then sent name is different
            name = if (isNCDVisit) "NCD Consultation" else "General Physician",
        )
    )
}
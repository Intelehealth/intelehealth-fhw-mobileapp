package org.intelehealth.app.ui.prescriptionwithotp

object PrescriptionDetailsDataKeys {
        // Vitals
        object Vitals {
            const val HEIGHT = "Height(cm)"
            const val WEIGHT = "Weight(kg)"
            const val BMI = "BMI"
            const val TEMPERATURE = "Temperature"
            const val SPO2 = "SPO2"
            const val BP = "BP"
            const val RESPIRATORY_RATE = "Respiratory Rate"
            const val PULSE = "Pulse"
            const val BLOOD_GROUP = "Blood Group"
        }

        // Diagnostics
        object Diagnostics {
            const val GLUCOSE_RANDOM = "Glucose (Random)"
            const val BLOOD_GLUCOSE_FASTING = "Glucose (Fasting)"
            const val BLOOD_GLUCOSE_POST_PRANDIAL = "Glucose (Post-Prandial)"
            const val URIC_ACID = "Uric Acid"
            const val TOTAL_CHOLESTEROL = "Total Cholesterol"
            const val HAEMOGLOBIN = "Haemoglobin"
        }

        // Complaints
        object Complaints {
            const val PRESENTING_COMPLAINTS = "Presenting Complaint(s)"
        }

        // Diagnosis
        object Diagnosis {
            const val PRIMARY = "Primary Diagnosis"
        }

        // Medication Plan
        object MedicationPlan {
            const val MEDICINE_DETAILS = "Medicine"
          /*  const val DOSAGE = "Dosage"
            const val FREQUENCY = "Frequency"
            const val DURATION = "Duration"*/
        }

        // General Advice
        object GeneralAdvice {
            const val ADVICE = "Advice"
        }

        // Follow-up
        object FollowUp {
            const val DATE = "Follow-up Date"
        }
    object Tests {
        const val TESTS = "Tests"
    }
    object Referral {
        const val REFERRAL = "Referred Specialist"
    }
    object NotesPrecautions {
        const val NOTES = "Notes & Precautions"
    }
    fun getLabelForKey(key: String): String {
        return when (key) {
            Vitals.HEIGHT -> Vitals.HEIGHT
            Vitals.WEIGHT -> Vitals.WEIGHT
            Vitals.BMI -> Vitals.BMI
            Vitals.BP -> Vitals.BP
            Vitals.TEMPERATURE -> Vitals.TEMPERATURE
            Vitals.SPO2 -> Vitals.SPO2
            Vitals.RESPIRATORY_RATE -> Vitals.RESPIRATORY_RATE
            Diagnostics.GLUCOSE_RANDOM -> Diagnostics.GLUCOSE_RANDOM
            Diagnostics.BLOOD_GLUCOSE_FASTING -> Diagnostics.BLOOD_GLUCOSE_FASTING
            Diagnostics.BLOOD_GLUCOSE_POST_PRANDIAL -> Diagnostics.BLOOD_GLUCOSE_POST_PRANDIAL
            Diagnostics.URIC_ACID -> Diagnostics.URIC_ACID
            Diagnostics.TOTAL_CHOLESTEROL -> Diagnostics.TOTAL_CHOLESTEROL
            Diagnostics.HAEMOGLOBIN -> Diagnostics.HAEMOGLOBIN
            Complaints.PRESENTING_COMPLAINTS -> Complaints.PRESENTING_COMPLAINTS
            Diagnosis.PRIMARY -> Diagnosis.PRIMARY
            MedicationPlan.MEDICINE_DETAILS -> MedicationPlan.MEDICINE_DETAILS
            GeneralAdvice.ADVICE -> GeneralAdvice.ADVICE
            FollowUp.DATE -> FollowUp.DATE
            Tests.TESTS -> Tests.TESTS
            Referral.REFERRAL -> Referral.REFERRAL
            else -> "" // In case of an unknown key
        }
    }
    const val VITALS_SECTION = "Vitals"
    const val DIAGNOSTICS_SECTION = "Diagnostics"

    enum class EncounterType(val key: String) {
        VITAL("vitalEncounter"),
        ADULT_INITIAL("adultInitialEncounter"),
        VISIT_COMPLETE("visitCompleteNoteEncounter");

        companion object {
            fun fromKey(key: String): EncounterType? {
                return values().find { it.key == key }
            }
        }
    }

}


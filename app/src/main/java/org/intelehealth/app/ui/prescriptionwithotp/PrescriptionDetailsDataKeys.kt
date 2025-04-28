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


        }

        // Diagnostics
        object Diagnostics {
            const val GLUCOSE_RANDOM = "Glucose (Random)"
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
        const val REFERRAL = "Referral"
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
            Diagnostics.HAEMOGLOBIN -> Diagnostics.HAEMOGLOBIN
            Complaints.PRESENTING_COMPLAINTS -> Complaints.PRESENTING_COMPLAINTS
            Diagnosis.PRIMARY -> Diagnosis.PRIMARY
            MedicationPlan.MEDICINE_DETAILS -> MedicationPlan.MEDICINE_DETAILS
          /*  MedicationPlan.DOSAGE -> MedicationPlan.DOSAGE
            MedicationPlan.FREQUENCY -> MedicationPlan.FREQUENCY
            MedicationPlan.DURATION -> MedicationPlan.DURATION*/
            GeneralAdvice.ADVICE -> GeneralAdvice.ADVICE
            FollowUp.DATE -> FollowUp.DATE
            Tests.TESTS -> Tests.TESTS
            Referral.REFERRAL -> Referral.REFERRAL
            else -> "Unknown" // In case of an unknown key
        }
    }
    const val VITALS_SECTION = "Vitals"
    const val DIAGNOSTICS_SECTION = "Diagnostics"
    }

    /* object Vitals {
         const val HEIGHT = "height"
         const val WEIGHT = "weight"
         const val BMI = "bmi"
         const val PULSE = "pulse"
         const val BP_SYS = "bp_sys"
         const val BP_DIA = "bp_dia"
         const val TEMPERATURE = "temperature"
         const val SPO2 = "spo2"
         const val VITAL_SIGNS = "vital_signs"
         const val BODY_MEASUREMENTS = "body_measurements"
     }

     object Diagnostics {
         const val URIC_ACID = "uric_acid"
         const val TOTAL_CHOLESTEROL = "total_cholestrol"
         const val BLOOD_GLUCOSE_NON_FASTING = "blood_glucose_non_fasting"
         const val BLOOD_GLUCOSE_RANDOM = "blood_glucose_random"
         const val BLOOD_GLUCOSE_POST_PRANDIAL = "blood_glucose_post_prandial"
         const val BLOOD_GLUCOSE_FASTING = "blood_glucose_fasting"
         const val HAEMOGLOBIN = "haemoglobin"
     }
     object Diagnosis {
         const val PRIMARY = "primary_diagnosis"
         const val PROVISIONAL = "provisional_diagnosis"
     }

     object MedicationPlan {
         const val MEDICINE_NAME = "medicine_name"
         const val DOSAGE = "dosage"
         const val FREQUENCY = "frequency"
         const val DURATION = "duration"
     }

     object GeneralAdvice {
         const val ADVICE = "advice"
     }

     object Complaints {
         const val PRESENTING_COMPLAINTS = "presenting_complaints"
     }

     object Investigations {
         const val GLUCOSE_RANDOM = "glucose_random"
         const val GLUCOSE_FASTING = "glucose_fasting"
         const val HBA1C = "hba1c"
         const val CHOLESTEROL = "cholesterol"
         const val URIC_ACID = "uric_acid"
     }

     object FollowUp {
         const val DATE = "follow_up_date"
     }*/

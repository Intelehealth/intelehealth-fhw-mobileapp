package org.intelehealth.app.ui.prescriptionwithotp

import android.database.Cursor
import android.database.sqlite.SQLiteCursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intelehealth.app.models.Patient
import org.intelehealth.app.models.dto.ObsDTO
import org.intelehealth.app.utilities.UuidDictionary

class SharePrescriptionDataRepository(private val db: SQLiteDatabase) {
    suspend fun getPatientDetails(patientUuid: String): Patient {
        return withContext(Dispatchers.IO) {
            val cursor = db.query("tbl_patient", arrayOf("openmrs_id", "first_name", "middle_name", "last_name", "date_of_birth", "address1", "address2", "phone_number", "gender", "abha_number"), "uuid = ?", arrayOf(patientUuid), null, null, null)
            cursor.use {
                if (it.moveToFirst()) {
                    val patient = Patient().apply {
                        openmrs_id = it.getString(it.getColumnIndexOrThrow("openmrs_id"))
                        first_name = it.getString(it.getColumnIndexOrThrow("first_name"))
                        middle_name = it.getString(it.getColumnIndexOrThrow("middle_name"))
                        last_name = it.getString(it.getColumnIndexOrThrow("last_name"))
                        date_of_birth = it.getString(it.getColumnIndexOrThrow("date_of_birth"))
                        address1 = it.getString(it.getColumnIndexOrThrow("address1"))
                        address2 = it.getString(it.getColumnIndexOrThrow("address2"))
                        phone_number = it.getString(it.getColumnIndexOrThrow("phone_number"))
                        gender = it.getString(it.getColumnIndexOrThrow("gender"))
                        abhaNumber = it.getString(it.getColumnIndexOrThrow("abha_number"))
                    }
                    patient
                } else {
                    throw Exception("Patient not found")
                }
            }
        }
    }

    suspend fun getAdultInitialData(encounterUuid: String): HashMap<String, String> {
        return withContext(Dispatchers.IO) {
            val adultInitial = HashMap<String, String>()
            val cursor = db.query("tbl_obs", arrayOf("value", "conceptuuid"), "encounteruuid = ? AND conceptuuid != ? AND conceptuuid != ? AND voided != '1'", arrayOf(encounterUuid, UuidDictionary.COMPLEX_IMAGE_AD, UuidDictionary.COMPLEX_IMAGE_PE), null, null, null)
            cursor.use {
                while (it.moveToNext()) {
                    parseAdultInitialData(adultInitial, it)
                }
            }
            adultInitial
        }
    }

    suspend fun getEncountersUuidByVisitUuid(visitUuid: String): Map<String, String> {
        return withContext(Dispatchers.IO) {
            val cursor = db.query("tbl_encounter", arrayOf("uuid", "encounter_type_uuid"), "visituuid = ? AND voided != '1'", arrayOf(visitUuid), null, null, null)
            val result = mutableMapOf<String, String>()
            val vitalEncounter = UuidDictionary.ENCOUNTER_VITALS
            val adultInitial = UuidDictionary.ENCOUNTER_ADULTINITIAL
            val encounterVisitNote = UuidDictionary.ENCOUNTER_VISIT_NOTE


            cursor.use {
                while (it.moveToNext()) {
                    val encounterUuid = it.getString(it.getColumnIndexOrThrow("uuid"))
                    val typeUuid = it.getString(it.getColumnIndexOrThrow("encounter_type_uuid"))

                    when (typeUuid) {
                        vitalEncounter -> result[PrescriptionDetailsDataKeys.EncounterType.VITAL.key] = encounterUuid
                        adultInitial -> result[PrescriptionDetailsDataKeys.EncounterType.ADULT_INITIAL.key] = encounterUuid
                        encounterVisitNote -> result[PrescriptionDetailsDataKeys.EncounterType.VISIT_COMPLETE.key] = encounterUuid
                    }
                }
            }

            result
        }
    }

    suspend fun getVisitCompleteEncounterData(encounterUuid: String): HashMap<String, String> {
        return withContext(Dispatchers.IO) {
            val result = HashMap<String, String>()
            val cursor = db.query("tbl_obs", arrayOf("value", "conceptuuid"), "encounteruuid = ? AND voided != '1'", arrayOf(encounterUuid), null, null, null)
            cursor.use {
                while (it.moveToNext()) {
                    val value = cursor.getString(cursor.getColumnIndexOrThrow("value"))
                    parseVisitCompleteEncounterData(result, it)
                }
            }
            result
        }
    }

    private fun parseDiagnosticsDataData(diagnosticsMap: MutableMap<String, String>, cursor: Cursor) {
        val conceptUuid = cursor.getString(cursor.getColumnIndexOrThrow("conceptuuid"))
        val diagnosticName = when (conceptUuid) {
            UuidDictionary.BLOOD_GLUCOSE_RANDOM -> PrescriptionDetailsDataKeys.Diagnostics.GLUCOSE_RANDOM
            UuidDictionary.BLOOD_GLUCOSE_FASTING -> PrescriptionDetailsDataKeys.Diagnostics.BLOOD_GLUCOSE_FASTING
            UuidDictionary.BLOOD_GLUCOSE_POST_PRANDIAL -> PrescriptionDetailsDataKeys.Diagnostics.BLOOD_GLUCOSE_POST_PRANDIAL
            UuidDictionary.URIC_ACID -> PrescriptionDetailsDataKeys.Diagnostics.URIC_ACID
            UuidDictionary.TOTAL_CHOLESTEROL -> PrescriptionDetailsDataKeys.Diagnostics.TOTAL_CHOLESTEROL
            UuidDictionary.HEMOGLOBIN -> PrescriptionDetailsDataKeys.Diagnostics.HAEMOGLOBIN
            else -> ""
        }

        if (diagnosticName.isNotEmpty()) {
            val value = cursor.getString(cursor.getColumnIndexOrThrow("value"))
            diagnosticsMap[diagnosticName] = value
        }
    }
    suspend fun getVitals(encounterUuid: String): LinkedHashMap<String, String> =
        withContext(Dispatchers.IO) {
            val tempMap = mutableMapOf<String, String>()
            var systolic: String? = null
            var diastolic: String? = null

            val cursor = db.query("tbl_obs", arrayOf("value", "conceptuuid"), "encounteruuid = ? AND voided != '1'", arrayOf(encounterUuid), null, null, null)

            cursor.use {
                while (it.moveToNext()) {
                    val conceptUuid = it.getString(it.getColumnIndexOrThrow("conceptuuid"))
                    val value = it.getString(it.getColumnIndexOrThrow("value"))
                    when (conceptUuid) {
                        UuidDictionary.TEMPERATURE -> tempMap[PrescriptionDetailsDataKeys.Vitals.TEMPERATURE] = value
                        UuidDictionary.RESPIRATORY -> tempMap[PrescriptionDetailsDataKeys.Vitals.RESPIRATORY_RATE] = value
                        UuidDictionary.SPO2 -> tempMap[PrescriptionDetailsDataKeys.Vitals.SPO2] = value
                        UuidDictionary.PULSE -> tempMap[PrescriptionDetailsDataKeys.Vitals.PULSE] = value
                        UuidDictionary.HEIGHT -> tempMap[PrescriptionDetailsDataKeys.Vitals.HEIGHT] = value
                        UuidDictionary.WEIGHT -> tempMap[PrescriptionDetailsDataKeys.Vitals.WEIGHT] = value
                        UuidDictionary.SYSTOLIC_BP -> systolic = value
                        UuidDictionary.DIASTOLIC_BP -> diastolic = value
                        UuidDictionary.BLOOD_GROUP ->  tempMap[PrescriptionDetailsDataKeys.Vitals.BLOOD_GROUP] = value
                    }
                }
            }
            val orderedMap = LinkedHashMap<String, String>()
            vitalsOrder.forEach { key ->
                if (key == PrescriptionDetailsDataKeys.Vitals.BP) {
                    if (!systolic.isNullOrEmpty() || !diastolic.isNullOrEmpty()) {
                        val bpValue = "${systolic ?: ""}/${diastolic ?: ""}".trimEnd('/')
                        orderedMap[key] = bpValue
                    }
                } else {
                    tempMap[key]?.let { orderedMap[key] = it }
                }
            }

            orderedMap
        }


    private fun parseAdultInitialData(adultInitialMap: MutableMap<String, String>, cursor: Cursor) {
        val conceptUuid = cursor.getString(cursor.getColumnIndexOrThrow("conceptuuid"))
        val adultInitial = when (conceptUuid) {
            UuidDictionary.CURRENT_COMPLAINT -> PrescriptionDetailsDataKeys.Complaints.PRESENTING_COMPLAINTS
            else -> ""
        }

        if (adultInitial.isNotEmpty()) {
            val value = cursor.getString(cursor.getColumnIndexOrThrow("value"))
            adultInitialMap[adultInitial] = value
        }
    }

    private fun parseVisitCompleteEncounterData(adultInitialMap: MutableMap<String, String>, cursor: Cursor) {
        val conceptUuid = cursor.getString(cursor.getColumnIndexOrThrow("conceptuuid"))
        val key = when (conceptUuid) {
            UuidDictionary.FOLLOW_UP_VISIT -> PrescriptionDetailsDataKeys.FollowUp.DATE
            UuidDictionary.REQUESTED_TESTS -> PrescriptionDetailsDataKeys.Tests.TESTS
            UuidDictionary.TELEMEDICINE_DIAGNOSIS -> PrescriptionDetailsDataKeys.Diagnosis.PRIMARY
            UuidDictionary.JSV_MEDICATIONS -> PrescriptionDetailsDataKeys.MedicationPlan.MEDICINE_DETAILS
            UuidDictionary.REFERRED_SPECIALIST -> PrescriptionDetailsDataKeys.Referral.REFERRAL
            UuidDictionary.MEDICAL_ADVICE -> PrescriptionDetailsDataKeys.GeneralAdvice.ADVICE
            else -> ""
        }

        if (key.isNotEmpty()) {
            val rawValue: String = cursor.getString(cursor.getColumnIndexOrThrow("value"))
            val newValue: String = when (key) {
                // Strip a leading "<code>::" or "NA::" prefix (e.g. "115902018::Acute
                // Gastroenteritis:Primary & Under Evaluation") - the diagnosis concept id
                // isn't meant to be shown, only the diagnosis text that follows it.
                // Mirrors the fix in PrescriptionActivity.parseData() (commit 140bb8fbe)
                // so the WhatsApp preview/PDF path shows the same cleaned value.
                PrescriptionDetailsDataKeys.Diagnosis.PRIMARY ->
                    rawValue.replaceFirst(Regex("(?i)^(?:na|\\d+)::\\s*"), "")
                // When the doctor leaves the follow-up remark blank, the synced value
                // ends in a literal "Remark: null" - show the same "NA" placeholder
                // PrescriptionBuilder already uses for the same field, so the End
                // Visit screen, View/Print, WhatsApp preview and WhatsApp PDF (all of
                // which read this value) stay consistent instead of showing "null".
                PrescriptionDetailsDataKeys.FollowUp.DATE ->
                    rawValue.replace(Regex("(?i)Remark:\\s*(null)?\\s*$"), "Remark: NA")
                else -> rawValue
            }
            val existingValue = adultInitialMap[key]

            if (existingValue.isNullOrBlank()) {
                // Add bullet for the first item
                adultInitialMap[key] = "• $newValue"
            } else if (!existingValue.contains(newValue)) {
                // Append bullet and newline for additional items
                adultInitialMap[key] = "$existingValue\n• $newValue"
            }
        }
    }

    suspend fun getDiagnostics(encounterUuid: String): HashMap<String, String> {
        return withContext(Dispatchers.IO) {
            val vitalsMap = HashMap<String, String>()
            val cursor = db.query("tbl_obs", arrayOf("value", "conceptuuid"), "encounteruuid = ? AND voided != '1'", arrayOf(encounterUuid), null, null, null)
            cursor.use {
                while (it.moveToNext()) {
                    parseDiagnosticsDataData(vitalsMap, it)
                }
            }
            vitalsMap
        }
    }

    private val vitalsOrder = listOf(
        PrescriptionDetailsDataKeys.Vitals.HEIGHT,
        PrescriptionDetailsDataKeys.Vitals.WEIGHT,
        PrescriptionDetailsDataKeys.Vitals.BP,
        PrescriptionDetailsDataKeys.Vitals.RESPIRATORY_RATE,
        PrescriptionDetailsDataKeys.Vitals.SPO2,
        PrescriptionDetailsDataKeys.Vitals.PULSE,
        PrescriptionDetailsDataKeys.Vitals.TEMPERATURE,)

}
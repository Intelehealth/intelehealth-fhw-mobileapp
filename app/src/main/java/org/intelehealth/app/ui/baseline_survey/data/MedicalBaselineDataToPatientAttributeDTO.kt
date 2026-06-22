package org.intelehealth.app.ui.baseline_survey.data

import com.google.gson.Gson
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.models.dto.PatientAttributesDTO
import org.intelehealth.app.ui.baseline_survey.model.AlcoholConsumptionHistory
import org.intelehealth.app.ui.baseline_survey.model.Baseline
import org.intelehealth.app.ui.baseline_survey.model.MedicalHistory
import org.intelehealth.app.ui.baseline_survey.model.SmokingHistory
import org.intelehealth.app.ui.baseline_survey.model.TobaccoHistory
import org.intelehealth.app.utilities.extensions.storeHyphenIfEmpty
import java.util.UUID

fun bindMedicalBaselinePatientAttributes(
    baseline: Baseline,
    patientId: String,
    patientsDAO: PatientsDAO
): List<PatientAttributesDTO> = ArrayList<PatientAttributesDTO>().apply {
    add(
        createPatientAttribute(
            patientId, PatientAttributesDTO.Column.SUGAR_CHECKED.value,
            baseline.sugarCheck.storeHyphenIfEmpty(),
            patientsDAO
        )
    )
    add(
        createPatientAttribute(
            patientId, PatientAttributesDTO.Column.BP_CHECKED.value,
            baseline.bpCheck.storeHyphenIfEmpty(),
            patientsDAO
        )
    )
    add(
        createPatientAttribute(
            patientId, PatientAttributesDTO.Column.HB_CHECKED.value,
            baseline.hbCheck.storeHyphenIfEmpty(),
            patientsDAO
        )
    )
    add(
        createPatientAttribute(
            patientId, PatientAttributesDTO.Column.OTHER_MEDICAL_HISTORY.value,
            getMedicalHistory(
                bpCheck = baseline.bpValue,
                diabetesCheck = baseline.diabetesValue,
                arthritisCheck = baseline.arthritisValue,
                anemiaCheck = baseline.anemiaValue,
                surgeryCheck = baseline.surgeryValue,
                reasonForSurgery = baseline.surgeryReason,
                medicationForAnemia = baseline.takingAnyMedicationForAnemia,
                healthWorkerForAnemia = baseline.haveYouSeenToHWinPastOneYearForAnemia,
                reasonForNoAnemiaMedication = baseline.reasonForNotTakingAnemiaMedication,
                medicationForBP = baseline.takingAnyMedicationForBP,
                healthWorkerForBP = baseline.haveYouSeenToHWinPastOneYearForBP,
                reasonForNoBPMedication = baseline.reasonForNotTakingBPMedication,
                medicationForDiabetes = baseline.takingAnyMedicationForDiabetes,
                healthWorkerForDiabetes = baseline.haveYouSeenToHWinPastOneYearForDiabetes,
                reasonForNoDiabetesMedication = baseline.reasonForNotTakingDiabetesMedication
            ),
            patientsDAO
        )
    )
    add(
        createPatientAttribute(
            patientId, PatientAttributesDTO.Column.SMOKING_STATUS.value,
            getSmokingHistory(
                baseline.smokingHistory,
                baseline.smokingRate,
                baseline.smokingDuration,
                baseline.smokingFrequency
            ),
            patientsDAO
        )
    )
    add(
        createPatientAttribute(
            patientId, PatientAttributesDTO.Column.TOBACCO_STATUS.value,
            getTobaccoHistory(baseline.chewTobacco),
            patientsDAO
        )
    )
    add(
        createPatientAttribute(
            patientId, PatientAttributesDTO.Column.ALCOHOL_CONSUMPTION_STATUS.value,
            getAlcoholHistory(
                baseline.alcoholHistory,
                baseline.alcoholRate,
                baseline.alcoholDuration,
                baseline.alcoholFrequency
            ),
            patientsDAO
        )
    )
}

private fun getAlcoholHistory(
    alcoholCheck: String,
    alcoholRate: String,
    alcoholDuration: String,
    alcoholFrequency: String
): String = Gson().toJson(
    listOf(
        AlcoholConsumptionHistory(
            alcoholCheck.storeHyphenIfEmpty(),
            alcoholRate.storeHyphenIfEmpty(),
            alcoholDuration.storeHyphenIfEmpty(),
            alcoholFrequency.storeHyphenIfEmpty()
        )
    )
)

private fun getTobaccoHistory(
    tobaccoCheck: String
): String = Gson().toJson(
    listOf(
        TobaccoHistory(tobaccoCheck.storeHyphenIfEmpty())
    )
)

private fun getMedicalHistory(
    bpCheck: String,
    diabetesCheck: String,
    arthritisCheck: String,
    anemiaCheck: String,
    surgeryCheck: String,
    reasonForSurgery: String,
    medicationForAnemia: String,
    healthWorkerForAnemia: String,
    reasonForNoAnemiaMedication: String,
    medicationForBP: String,
    healthWorkerForBP: String,
    reasonForNoBPMedication: String,
    medicationForDiabetes: String,
    healthWorkerForDiabetes: String,
    reasonForNoDiabetesMedication: String
): String = Gson().toJson(
    listOf(
        MedicalHistory(
            bp = bpCheck.storeHyphenIfEmpty(),
            diabetes = diabetesCheck.storeHyphenIfEmpty(),
            arthritis = arthritisCheck.storeHyphenIfEmpty(),
            anemia = anemiaCheck.storeHyphenIfEmpty(),
            anaemia = anemiaCheck.storeHyphenIfEmpty(),
            anySurgeries = surgeryCheck.storeHyphenIfEmpty(),
            reasonForSurgery = reasonForSurgery.storeHyphenIfEmpty(),
            medicationForAnemia = medicationForAnemia.storeHyphenIfEmpty(),
            healthWorkerForAnemia = healthWorkerForAnemia.storeHyphenIfEmpty(),
            reasonForNoAnemiaMedication = reasonForNoAnemiaMedication.storeHyphenIfEmpty(),
            medicationForBP = medicationForBP.storeHyphenIfEmpty(),
            healthWorkerForBP = healthWorkerForBP.storeHyphenIfEmpty(),
            reasonForNoHypertensionMedication = reasonForNoBPMedication.storeHyphenIfEmpty(),
            hypertension = bpCheck.storeHyphenIfEmpty(),
            medicationForDiabetes = medicationForDiabetes.storeHyphenIfEmpty(),
            healthWorkerForDiabetes = healthWorkerForDiabetes.storeHyphenIfEmpty(),
            reasonForNoDiabetesMedication = reasonForNoDiabetesMedication.storeHyphenIfEmpty()
        )
    )
)

private fun getSmokingHistory(
    smokingCheck: String,
    rateOfSmoking: String,
    durationOfSmoking: String,
    frequencyOfSmoking: String
): String = Gson().toJson(
    listOf(
        SmokingHistory(
            smokingCheck.storeHyphenIfEmpty(),
            rateOfSmoking.storeHyphenIfEmpty(),
            durationOfSmoking.storeHyphenIfEmpty(),
            frequencyOfSmoking.storeHyphenIfEmpty()
        )
    )
)

private fun createPatientAttribute(
    patientId: String,
    attrName: String,
    value: String?,
    patientsDAO: PatientsDAO
): PatientAttributesDTO {
    return PatientAttributesDTO().apply {
        uuid = UUID.randomUUID().toString()
        patientuuid = patientId
        personAttributeTypeUuid = patientsDAO.getUuidForAttribute(attrName)
        this.value = value
    }
}
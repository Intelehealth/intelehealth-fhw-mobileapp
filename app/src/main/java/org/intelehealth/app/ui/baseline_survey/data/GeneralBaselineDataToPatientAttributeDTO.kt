package org.intelehealth.app.ui.baseline_survey.data

import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.models.dto.PatientAttributesDTO
import org.intelehealth.app.ui.baseline_survey.model.Baseline
import org.intelehealth.app.utilities.extensions.storeHyphenIfEmpty
import java.util.UUID

fun bindGeneralBaselinePatientAttributes(
    baseline: Baseline,
    patientId: String,
    patientsDAO: PatientsDAO
): List<PatientAttributesDTO> {
    return ArrayList<PatientAttributesDTO>().apply {
        add(
            createPatientAttribute(
                patientId,
                PatientAttributesDTO.Column.OCCUPATION.value,
                baseline.occupation.storeHyphenIfEmpty(),
                patientsDAO
            )
        )

        add(
            createPatientAttribute(
                patientId,
                PatientAttributesDTO.Column.CASTE.value,
                baseline.caste.storeHyphenIfEmpty(),
                patientsDAO
            )
        )

        add(
            createPatientAttribute(
                patientId,
                PatientAttributesDTO.Column.EDUCATION.value,
                baseline.education.storeHyphenIfEmpty(),
                patientsDAO
            )
        )

        add(
            createPatientAttribute(
                patientId,
                PatientAttributesDTO.Column.AYUSHMAN_CARD_STATUS.value,
                baseline.ayushmanCard.storeHyphenIfEmpty(),
                patientsDAO
            )
        )

        add(
            createPatientAttribute(
                patientId,
                PatientAttributesDTO.Column.MGNREGA_CARD_STATUS.value,
                baseline.mgnregaCard.storeHyphenIfEmpty(),
                patientsDAO
            )
        )

        add(
            createPatientAttribute(
                patientId,
                PatientAttributesDTO.Column.BANK_ACCOUNT.value,
                baseline.bankAccount.storeHyphenIfEmpty(),
                patientsDAO
            )
        )

        add(
            createPatientAttribute(
                patientId,
                PatientAttributesDTO.Column.MOBILE_PHONE_TYPE.value,
                baseline.phoneOwnership.storeHyphenIfEmpty(),
                patientsDAO
            )
        )

        add(
            createPatientAttribute(
                patientId,
                PatientAttributesDTO.Column.USE_WHATSAPP.value,
                baseline.familyWhatsApp.storeHyphenIfEmpty(),
                patientsDAO
            )
        )

        add(
            createPatientAttribute(
                patientId, PatientAttributesDTO.Column.MARTIAL_STATUS.value,
                baseline.martialStatus.storeHyphenIfEmpty(),
                patientsDAO
            )
        )
        add(
            createPatientAttribute(
                patientId, PatientAttributesDTO.Column.SELF_OR_FAMILY_WHATSAPP.value,
                baseline.selfOrFamilyWhatsappNumber.storeHyphenIfEmpty(),
                patientsDAO
            )
        )
        add(
            createPatientAttribute(
                patientId, PatientAttributesDTO.Column.CAN_EKAL_SEND_WHATSAPP_MESSAGE.value,
                baseline.canEkalSendFreeWhatsAppMessageForVisitSummary.storeHyphenIfEmpty(),
                patientsDAO
            )
        )

    }
}

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
package org.intelehealth.ncd.model

data class PrescriptionExistsResult(
    val visitId: String?,      // visit UUID
    val prescriptionExists: Boolean // 1 = exists, 0 = does not exist
)

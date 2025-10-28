package org.intelehealth.ncd.model

data class VisitAttributeResult(
    val visitId: String?,       // visit UUID
    val typeUuid: String,       // visit_attribute_type_uuid
    val value: String           // value of the attribute
)
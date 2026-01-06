package org.intelehealth.ncd.linelisting

data class NcdCategory(
    val key: String,
    val displayName: String,
    val type: Type
) {
    enum class Type {
        SCREENING, FOLLOW_UP, GENERAL
    }
}
package org.intelehealth.app.utilities

import org.intelehealth.app.models.dto.Identifier

fun getUUID(list: List<Identifier>?, systemValue: String): String? {
    return list?.firstOrNull { it.system == systemValue }?.id
}
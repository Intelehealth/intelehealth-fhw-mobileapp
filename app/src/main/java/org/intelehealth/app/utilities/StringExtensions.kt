package org.intelehealth.app.utilities

fun String.ensureTrailingSlash(): String = if (endsWith("/")) this else "$this/"

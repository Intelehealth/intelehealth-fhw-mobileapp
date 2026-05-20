package org.intelehealth.app.utilities.extensions

fun String?.safe(): String = this ?: ""

fun Double?.safe(): Double = this ?: 0.0

fun List<*>?.safeEmpty(): Boolean = this.isNullOrEmpty()
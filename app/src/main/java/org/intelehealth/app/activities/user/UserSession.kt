package org.intelehealth.app.user

data class UserSession(
    val sessionId: Int? = null,
    var userId: String,
    var startTime: String,
    var endTime: String,
    var sessionDuration: String,
    var sync: String
)
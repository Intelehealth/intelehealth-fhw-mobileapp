package com.intelehealth.appointment

object AppointmentBuilder {
    lateinit var baseUrl: String
    lateinit var token: String

    class Builder(baseUrl: String,token: String) {
        init {
            AppointmentBuilder.baseUrl = baseUrl
            AppointmentBuilder.token = token
        }
    }
}
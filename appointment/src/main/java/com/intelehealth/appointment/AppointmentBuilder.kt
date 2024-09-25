package com.intelehealth.appointment

object AppointmentBuilder {
    lateinit var baseUrl: String

    class Builder(baseUrl: String) {
        init {
            AppointmentBuilder.baseUrl = baseUrl
        }
    }
}
package com.intelehealth.appointment

object AppointmentBuilder {
    lateinit var baseUrl: String
    lateinit var token: String
    var language: String = "en"

    class Builder(baseUrl: String,token: String) {
        init {
            AppointmentBuilder.baseUrl = baseUrl
            AppointmentBuilder.token = token
        }
    }
    class SetLanguage(language:String){
        init {
            AppointmentBuilder.language = language
        }
    }
}
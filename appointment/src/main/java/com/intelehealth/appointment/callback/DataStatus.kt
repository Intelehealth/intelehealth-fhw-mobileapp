package com.intelehealth.appointment.callback

interface DataStatus {
    fun success(msg: String)
    fun failed(error: String)
}
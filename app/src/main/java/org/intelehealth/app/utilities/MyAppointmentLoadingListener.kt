package org.intelehealth.app.utilities

/**
 * Created by Tanvir Hasan on 15-05-2024 : 14-51.
 * Email: mhasan@intelehealth.org
 */
interface MyAppointmentLoadingListener {
    fun onStartUpcoming()
    fun onStartPast()
    fun onStopUpcoming()
    fun onStopPast()
}
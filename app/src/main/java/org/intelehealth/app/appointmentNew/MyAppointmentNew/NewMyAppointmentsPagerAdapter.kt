package org.intelehealth.app.appointmentNew.MyAppointmentNew

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.intelehealth.app.utilities.MyAppointmentLoadingListener

class NewMyAppointmentsPagerAdapter(
    fm: FragmentManager?,
    var tabCount: Int,
    var context: Context?
) : FragmentStateAdapter(
    (context as FragmentActivity?)!!
) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UpcomingAppointmentsFragment(context as MyAppointmentLoadingListener)
            1 -> PastAppointmentsFragment(context as MyAppointmentLoadingListener)
            else -> PastAppointmentsFragment(context as MyAppointmentLoadingListener)
        }
    }

    override fun getItemCount(): Int {
        return tabCount
    }
}
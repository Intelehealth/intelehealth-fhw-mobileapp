package org.intelehealth.app.appointmentNew.MyAppointmentNew

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.intelehealth.app.utilities.MyAppointmentLoadingListener

class NewMyAppointmentsPagerAdapter(
    fm: FragmentManager?,
    private var tabCount: Int,
    var context: Context?
) : FragmentStateAdapter(
    (context as FragmentActivity?)!!
) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {

            0 -> {
                val fragment = UpcomingAppointmentsFragment()
                fragment.setListener(context as MyAppointmentLoadingListener)
                fragment
            }
            1 -> {
                val fragment = PastAppointmentsFragment()
                fragment.setListener(context as MyAppointmentLoadingListener)
                fragment
            }
            else -> {
                val fragment = PastAppointmentsFragment()
                fragment.setListener(context as MyAppointmentLoadingListener)
                fragment
            }
        }
    }

    override fun getItemCount(): Int {
        return tabCount
    }
}
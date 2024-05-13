package org.intelehealth.app.appointmentNew.MyAppointmentNew

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.intelehealth.app.appointmentNew.AllAppointmentsFragment

class NewMyAppointmentsPagerAdapter(
    fm: FragmentManager?,
    var tabCount: Int,
    var context: Context?
) : FragmentStateAdapter(
    (context as FragmentActivity?)!!
) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UpcomingAppointmentsFragment()
            1 -> PastAppointmentsFragment()
            else -> PastAppointmentsFragment()
        }
    }

    override fun getItemCount(): Int {
        return tabCount
    }
}
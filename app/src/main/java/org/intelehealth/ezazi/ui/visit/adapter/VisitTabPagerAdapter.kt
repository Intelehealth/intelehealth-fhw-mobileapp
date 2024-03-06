package org.intelehealth.ezazi.ui.visit.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.ui.visit.fragment.CompletedVisitFragment
import org.intelehealth.ezazi.ui.visit.fragment.OutcomePendingVisitFragment
import org.intelehealth.ezazi.ui.visit.fragment.UpcomingVisitFragment

/**
 * Created by Vaghela Mithun R. on 04-06-2023 - 00:01.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 */
class VisitTabPagerAdapter(
    private val context: Context,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    private val tabs = context.resources.getStringArray(R.array.visit_status_tabs)
    private val fragments = arrayListOf<Fragment>(
        OutcomePendingVisitFragment.newInstance(),
        UpcomingVisitFragment.newInstance(),
//        CompletedVisitFragment.newInstance()
    )

    override fun createFragment(position: Int): Fragment {
        return fragments[position] as Fragment
    }

    override fun getItemCount(): Int {
        return tabs.size
    }

    fun getTitle(position: Int): String {
        return tabs[position]
    }
}
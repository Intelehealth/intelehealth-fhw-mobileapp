package org.intelehealth.ezazi.ui.elcg.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.intelehealth.ezazi.partogram.PartogramConstants
import org.intelehealth.ezazi.ui.elcg.fragment.ELCGDataFragment
import org.intelehealth.ezazi.ui.elcg.model.ELCGGraph

/**
 * Created by Vaghela Mithun R. on 04-06-2023 - 00:01.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 */
class ELCGTabPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun createFragment(position: Int): Fragment {
        return ELCGDataFragment.newInstance(ELCGGraph.values()[position])
    }

    override fun getItemCount(): Int {
        return ELCGGraph.values().size
    }

    fun getTitle(position: Int): String {
        return ELCGGraph.values()[position].section
    }
}
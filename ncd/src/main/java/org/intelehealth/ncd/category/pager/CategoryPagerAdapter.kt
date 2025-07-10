package org.intelehealth.ncd.category.pager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class CategoryPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val fragments: List<Fragment>
) : FragmentStateAdapter(fragmentActivity) {

    private val fragmentMap = mutableMapOf<Int, Fragment>()

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        val fragment = fragments[position]
        fragmentMap[position] = fragment
        return fragment
    }

    fun getFragment(position: Int): Fragment? {
        return fragmentMap[position]
    }
}
package org.intelehealth.ncd.category.pager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class CategoryPagerAdapter(
    activity: FragmentActivity,
    private val fragmentList: List<Fragment>
) : FragmentStateAdapter(activity) {

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getItemCount(): Int {
        return fragmentList.size
    }
}

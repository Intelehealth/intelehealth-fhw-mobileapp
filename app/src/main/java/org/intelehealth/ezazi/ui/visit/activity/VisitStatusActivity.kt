package org.intelehealth.ezazi.ui.visit.activity

import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.databinding.ActivityVisitStatusBinding
import org.intelehealth.ezazi.ui.shared.BaseActivity
import org.intelehealth.ezazi.ui.visit.adapter.VisitTabPagerAdapter

/**
 * Created by Vaghela Mithun R. on 16-01-2024 - 00:39.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class VisitStatusActivity : BaseActivity() {
    private lateinit var binding: ActivityVisitStatusBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVisitStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar()
        setupTabs()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.title = resources.getString(R.string.visit)
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupTabs() {
        if (::binding.isInitialized) {
            val adapter = VisitTabPagerAdapter(this, supportFragmentManager, lifecycle)
            binding.viewPagerVisitStatus.adapter = adapter
            TabLayoutMediator(
                binding.tabsVisitStatus,
                binding.viewPagerVisitStatus
            ) { tab: TabLayout.Tab, position: Int ->
                tab.text = adapter.getTitle(position)
            }.attach()
        }
    }
}